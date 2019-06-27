
/*

  Using the Arduino Pro Mini
  By: Tristan Gray
  This code has used samples from a lot of different sources which I've tried to include references to. However there 
  may be some that I have left out, my appologies if I haven't given the required credit where it should be.

  I used this code for an arduino pro mini which communicates to a Adafruit Huzzah ESP8266 via serial.


  https://github.com/sparkfun/Wimp_Weather_Station
  
  Weather Shield Example
  By: Nathan Seidle
  SparkFun Electronics
  Date: November 16th, 2013
  License: This code is public domain but you buy me a beer if you use this and we meet someday (Beerware license).

  Much of this is based on Mike Grusin's USB Weather Board code: https://www.sparkfun.com/products/10586

  This is a more advanced example of how to utilize every aspect of the weather shield. See the basic
  example if you're just getting started.

  This code reads all the various sensors (wind speed, direction, rain gauge, humidty, pressure, light, batt_lvl)
  and reports it over the serial comm port. This can be easily routed to an datalogger (such as OpenLog) or
  a wireless transmitter (such as Electric Imp).

  Measurements are reported once a second but windspeed and rain gauge are tied to interrupts that are
  calcualted at each report.

  This example code assumes the GPS module is not used.

*/
const int sketchVersion = 9;
boolean printStream = true; // Set to true to print transmited weather data to the serial

#include <SoftwareSerial.h> // For sending data to Huzzah ESP8266
#include <DHT.h>   //Humidity/Temp sensor
#include <SPI.h>
#include <Adafruit_Sensor.h>
#include <Adafruit_BMP280.h>

//define slave i2c address
#define I2C_SLAVE_ADDRESS 9

#define DHTPIN 8     // what digital pin we're connected to-
#define DHTTYPE DHT22   // DHT 11
DHT dht(DHTPIN, DHTTYPE);


#define BMP_SCK 13
#define BMP_MISO 12
#define BMP_MOSI 11
#define BMP_CS 10

// hardware SPI
Adafruit_BMP280 bmp(BMP_CS, BMP_MOSI, BMP_MISO,  BMP_SCK);


//Hardware pin definitions
//-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
// digital I/O pins
const byte WSPEED = 2; // Interrupt Pin. if Uno 3
const byte RAIN = 3; // Interrupt Pin. if Uno 2


// analog I/O pins
const byte WDIR = A0;
//-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=

//Global Variables
//-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
long lastSecond; //The millis counter to see when a second rolls by
byte seconds; //When it hits 60, increase the current minute
byte seconds_2m; //Keeps track of the "wind speed/dir avg" over last 2 minutes array of data
byte minutes; //Keeps track of where we are in various arrays of data
byte minutes_10m; //Keeps track of where we are in wind gust/dir over last 10 minutes array of data
byte day;
byte lastDay;

long lastWindCheck = 0;
volatile long lastWindIRQ = 0;
volatile byte windClicks = 0;

//We need to keep track of the following variables:
//Wind speed/dir each update (no storage)
//Wind gust/dir over the day (no storage)
//Wind speed/dir, avg over 2 minutes (store 1 per second)
//Wind gust/dir over last 10 minutes (store 1 per minute)
//Rain over the past hour (store 1 per minute)
//Total rain over date (store one per day)

byte windspdavg[120]; //120 bytes to keep track of 2 minute average

#define WIND_DIR_AVG_SIZE 120
int winddiravg[WIND_DIR_AVG_SIZE]; //120 ints to keep track of 2 minute average
float windgust_10m[10]; //10 floats to keep track of 10 minute max
int windgustdirection_10m[10]; //10 ints to keep track of 10 minute max
volatile float rainHour[60]; //60 floating numbers to keep track of 60 minutes of rain

//These are all the weather values that wunderground expects:
int winddir = 0; // [0-360 instantaneous wind direction]
String windHeading = ""; // get_wind_directionStr();
float windspeedkph = 0; // [kph instantaneous wind speed]
float windgustkph = 0; // [mph current wind gust, using software specific time period]
int windgustdir = 0; // [0-360 using software specific time period]
float windspdkph_avg2m = 0; // [mph 2 minute average wind speed mph]
int winddir_avg2m = 0; // [0-360 2 minute average wind direction]
float windgustkph_10m = 0; // [mph past 10 minutes wind gust mph ]
int windgustdir_10m = 0; // [0-360 past 10 minutes wind gust direction]
int dhtHumidity = 0; // [%]
float dhtTemp = 0;
float tempC = 0; // [temperature C]
float rainmm = 0; // [rain inches over the past hour)] -- the accumulated rainfall in the past 60 min
volatile float dailyrainmm = 0; // [rain inches so far today in local time]
int pressure;


// volatiles are subject to modification by IRQs
volatile unsigned long raintime, rainlast, raininterval, rain;


int AnalogWindDirValue; // I added
byte winddirByte = 45;  // 0-7 to represent the 8 posible positions that it can be at on a compass.


//-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=

//Interrupt routines (these are called by the hardware interrupts, not by the main code)
//-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
void rainIRQ()
// Count rain gauge bucket tips as they occur
// Activated by the magnet and reed switch in the rain gauge, attached to input D2
{
  raintime = millis(); // grab current time
  raininterval = raintime - rainlast; // calculate interval between this and last event

  if (raininterval > 10) // ignore switch-bounce glitches less than 10mS after initial edge
  {
    dailyrainmm += 0.2794;  // dailyrainin += 0.011; //Each dump is 0.011" of water (or 0.2794mm)
    rainHour[minutes] += 0.2794; //Increase this minute's amount of rain

    rainlast = raintime; // set up for next event
  }
}

void wspeedIRQ()
// Activated by the magnet in the anemometer (2 ticks per rotation), attached to input D3 ( or D2 for Leonardo )
{
  if (millis() - lastWindIRQ > 10) // Ignore switch-bounce glitches less than 10ms (142MPH max reading) after the reed switch closes
  {
    lastWindIRQ = millis(); //Grab the current time
    windClicks++; //There is 1.492MPH for each click per second.
    // Serial.println(" windClicks:  " + windClicks); // For testing
  }
}



SoftwareSerial mySerial(6, 7); // on esp8266(4, 5); (green wire, yellow wire)

int serial_inc = 0; // Increment through the variables to send via mySerial
char received = 'R';



void setup()
{
  Serial.begin(115200);


  mySerial.begin(115200);


  Serial.println("WeatherPro Initiating..");
  dht.begin(); // Temp & Humidity


  pinMode(WSPEED, INPUT_PULLUP); // input from wind meters windspeed sensor
  pinMode(RAIN, INPUT_PULLUP); // input from wind meters rain gauge sensor


  if (!bmp.begin()) {
    Serial.println("Could not find a valid BMP280 sensor, check wiring!");
    // while (1);
  }

  seconds = 0;
  lastSecond = millis();

  // attach external interrupt pins to IRQ functions
  attachInterrupt(digitalPinToInterrupt(3), rainIRQ, FALLING); // Pin 3 on Arduino Leonardo; Pin 2 on Uno
  attachInterrupt(digitalPinToInterrupt(2), wspeedIRQ, FALLING); // Pin 2 on Arduino Leonardo; Pin 3 on Uno.
  /* Was FALLING, HIGH seems to resolve the spike when the anemometer stops.
     No it doesn't, try to solve it in code. Use an array and delete the highest value in the array, only if it's a large increase
  */

  // turn on interrupts
  interrupts();

  //pinMode(2, INPUT);
  digitalWrite(13, LOW);
  Serial.print("WeatherPro_"); Serial.print(sketchVersion); Serial.println(" active!");

}

void loop()
{


  //Keep track of which minute it is
  if (millis() - lastSecond >= 1000)
  {

    lastSecond += 1000;

    //Take a speed and direction reading every second for 2 minute average
    if (++seconds_2m > 119) seconds_2m = 0;

    //Calc the wind speed and direction every second for 120 second to get 2 minute average
    float currentSpeed = get_wind_speed();
    windspeedkph = currentSpeed;
    //float currentSpeed = random(5); //For testing
    int currentDirection = get_wind_direction();
    windspdavg[seconds_2m] = (int)currentSpeed;
    winddiravg[seconds_2m] = currentDirection;
    //if(seconds_2m % 10 == 0) displayArrays(); //For testing

    //Check to see if this is a gust for the minute
    if (currentSpeed > windgust_10m[minutes_10m])
    {
      windgust_10m[minutes_10m] = currentSpeed;
      windgustdirection_10m[minutes_10m] = currentDirection;
    }

    //Check to see if this is a gust for the day
    if (currentSpeed > windgustkph)
    {
      windgustkph = currentSpeed;
      windgustdir = get_wind_direction(); //get_wind_directionStr(); // currentDirection;
    }

    if (++seconds > 59)
    {
      seconds = 0;

      if (++minutes > 59) {
        minutes = 0;
        Serial.println("minute");
        //printWeather(); //Report all readings every minute
      }
      if (++minutes_10m > 9) minutes_10m = 0;

      rainHour[minutes] = 0; //Zero out this minute's rainfall amount
      windgust_10m[minutes_10m] = 0; //Zero out this minute's gust
    }

    //Report all readings every second
    printWeather(); 
    receiveDay();
  }


  delay(100);
  // Instead of delay();
  // lowPower.powerDown(SLEEP_FOREVER, ADC_OFF, SPI_OFF, USART0_OFF, TWI_ON); // TWI could act as the sleep interrupter, if WiDo is connected vis i2c (TWI) then when it sends it's request every 5mins it wakes it up. Every 30mins in dark hours.
  // or slave merely takes an 8s break and sends wake up call to WiDo every 5mins.
  // lowPower.powerDown(SLEEP_8s, ADC_OFF, BOD_OFF);


}




//Calculates each of the variables that wunderground is expecting
void calcWeather()
{
  //Calc winddir
  winddir = get_wind_direction();
  windHeading = get_wind_directionStr();

  //Calc windspeed
  //windspeedmph = get_wind_speed(); //This is calculated in the main loop

  //Calc windgustmph
  //Calc windgustdir
  //These are calculated in the main loop

  //Calc windspdmph_avg2m
  float temp = 0;
  for (int i = 0 ; i < 120 ; i++)
    temp += windspdavg[i];
  temp /= 120.0;
  windspdkph_avg2m = temp;

  //Calc winddir_avg2m, Wind Direction
  //You can't just take the average. Google "mean of circular quantities" for more info
  //We will use the Mitsuta method because it doesn't require trig functions
  //And because it sounds cool.
  //Based on: http://abelian.org/vlf/bearings.html
  //Based on: http://stackoverflow.com/questions/1813483/averaging-angles-again
  long sum = winddiravg[0];
  int D = winddiravg[0];
  for (int i = 1 ; i < WIND_DIR_AVG_SIZE ; i++)
  {
    int delta = winddiravg[i] - D;

    if (delta < -180)
      D += delta + 360;
    else if (delta > 180)
      D += delta - 360;
    else
      D += delta;

    sum += D;
  }
  winddir_avg2m = sum / WIND_DIR_AVG_SIZE;
  if (winddir_avg2m >= 360) winddir_avg2m -= 360;
  if (winddir_avg2m < 0) winddir_avg2m += 360;

  //Calc windgustmph_10m
  //Calc windgustdir_10m
  //Find the largest windgust in the last 10 minutes
  windgustkph_10m = 0;
  windgustdir_10m = 0;
  //Step through the 10 minutes
  for (int i = 0; i < 10 ; i++)
  {
    if (windgust_10m[i] > windgustkph_10m)
    {
      windgustkph_10m = windgust_10m[i];
      windgustdir_10m = windgustdirection_10m[i];
    }
  }




  // Reading temperature or humidity takes about 250 milliseconds!
  // Sensor readings may also be up to 2 seconds 'old' (its a very slow sensor)
  dhtHumidity = dht.readHumidity();
  // dhtHumidity = 45; // for testing
 // Serial.print("dhtHumidity:  "); Serial.print(dhtHumidity); Serial.println(" %");

  // Read temperature as Celsius (the default)
  dhtTemp = dht.readTemperature(); // testing dhtTemp = 13;
  //Serial.print("dhtTemp:  "); Serial.print(dhtTemp); Serial.println(" *C");

  // Check if any reads failed and exit early (to try again).
  if (isnan(dhtHumidity) || isnan(dhtTemp)) {  // int   isnan(double __x) // returns 1 if "not a number"
    Serial.println("Failed to read from DHT sensor!");
    return;
  }


  //Total rainfall for the day is calculated within the interrupt
  //Calculate amount of rainfall for the last 60 minutes
  rainmm = 0;
  for (int i = 0 ; i < 60 ; i++)
    rainmm += rainHour[i];


  float pressureRAW =  bmp.readPressure();
  pressure = round(pressureRAW / 100); // Convert to hPa and round to an int

  //  Serial.print("Pressure = ");
  // Serial.print(pressure);
  //Serial.println(" hPa"); // eg: Pressure = 101097.39 Pa
}


//Returns the instataneous wind speed
float get_wind_speed()
{
  float deltaTime = millis() - lastWindCheck; //750ms

  deltaTime /= 1000.0; //Covert to seconds

  float windSpeed = (float)windClicks / deltaTime; //3 / 0.750s = 4

  windClicks = 0; //Reset and start watching for new wind
  lastWindCheck = millis();

  // windSpeed *= 1.492; //4 * 1.492 = 5.968MPH  // If the switch is closed once per second then the speed is 1.492 MPH or 2.4011412 km/h
  windSpeed *= 2.4011412; //  e.g. 4 * 2.4011412 = 9.604565 km/h

  // Serial.println();
  // Serial.print("Wind Speed kph:");
  // Serial.println(windSpeed);

  return (windSpeed);
}

//Read the wind direction sensor, return heading in degrees
int get_wind_direction()
{
  unsigned int adc;

  adc = analogRead(WDIR); // get the current reading from the sensor
  AnalogWindDirValue = adc;  // For testing raw analog value

  // The following table is ADC readings for the wind direction sensor output, sorted from low to high.
  // Each threshold is the midpoint between adjacent headings. The output is degrees for that ADC reading.
  // Note that these are not in compass degree order! See Weather Meters datasheet for more information.

  // The Weather Station I purchased from JayCar has an acuracy to 8 position instead of 16.
  // New analog readings since setting it up with the ArduinoPro. I added 10 to each value to increase its range in just case.
  if (adc < 565)  return (45); // NE   if (adc < 360)
  if (adc < 607)  return (135); // SE  if (adc < 387)
  if (adc < 657)  return (90); // E  if (adc < 420)
  if (adc < 759)  return (315); // NW  if (adc < 487)
  if (adc < 803)  return (0); // N  if (adc < 518)
  if (adc < 843)  return (225); // SW  if (adc < 543)
  if (adc < 900)  return (180); // S  if (adc < 582)
  if (adc < 957)  return (270); // W  if (adc < 620)

  return (-1); // error, disconnected?

}
//Read the wind direction sensor, return heading in degrees
String get_wind_directionStr()
{
  unsigned int adc;

  adc = analogRead(WDIR); // get the current reading from the sensor
  AnalogWindDirValue = adc;  // For testing raw analog value

  // The following table is ADC readings for the wind direction sensor output, sorted from low to high.
  // Each threshold is the midpoint between adjacent headings. The output is degrees for that ADC reading.
  // Note that these are not in compass degree order! See Weather Meters datasheet for more information.

  // The Weather Station I purchased from JayCar has an acuracy to 8 position instead of 16.
  // New analog readings since setting it up with the ArduinoPro. I added 10 to each value to increase its range just in case.
  if (adc < 565)  return "045"; // return (045); // NE   if (adc < 360)
  if (adc < 607)  return "135"; //  return (135); // SE  if (adc < 387)
  if (adc < 657)  return "090"; // return (090); // E  if (adc < 420)
  if (adc < 759)  return "315"; // return (315); // NW  if (adc < 487)
  if (adc < 803)  return "000"; //  return (000); // N  if (adc < 518)
  if (adc < 843)  return "225"; //  return (225); // SW  if (adc < 543)
  if (adc < 900)  return "180"; //  return (180); // S  if (adc < 582)
  if (adc < 957)  return "270"; //  return (270); // W  if (adc < 620)

  return ("-1"); // error, disconnected?
}



//Prints the various variables directly to the port
//I don't like the way this function is written but Arduino doesn't support floats under sprintf
void printWeather()
{
  calcWeather(); //Go calc all the various sensors

  serialSend();  // mySerial.print  now in main loop, every 1min

  // Serial.print("(testing) Analog Wind Direction: "); // testing
  // Serial.println(AnalogWindDirValue);  // testing
  // Serial.print(F("\"winddir\":\"")); // testing
  // Serial.print(winddir); // testing
}



void serialSend() {

  noInterrupts();

  //Serial.print(serial_inc); Serial.print(": ");
  switch (serial_inc) {
    case 1:
      serialString("B", dhtTemp); // tempC: bmp sensor
      break;
    case 2:
      serialString("C", pressure);
      break;
    case 3:
      serialString("D", dhtHumidity);
      break;
    case 4:
      serialString("E", winddir);
      break;
    case 5:
      serialString("F", windspeedkph);
      break;
    case 6:
      serialString("G", windgustdir);
      break;
    case 7:
      serialString("H", windgustkph);
      break;
    case 8:
      serialString("I", winddir_avg2m);
      break;
    case 9:
      serialString("J", windspdkph_avg2m);
      break;
    case 10:
      serialString("K", windgustdir_10m);
      break;
    case 11:
      serialString("L", windgustkph_10m);
      break;
    case 12:
      serialString("M", rainmm);
      break;
    case 13:
      serialString("N", dailyrainmm);
      break;
    default:
      serialString("A", sketchVersion);
      break;
  }
  interrupts();
 
  serial_inc ++; // Cycle through the data set
  if (serial_inc == 14) serial_inc = 0;
 

  delay(10);
}

void serialString(String key, int value) {
  String data = key + String(value) + "\n";
  mySerial.print(data);
  if (printStream) {
    Serial.print(value); Serial.print("(int): "); Serial.println(data);
  }
}
void serialString(String key, float value) {
  String data = key + String(value) + "\n";
  mySerial.print(data);
  if (printStream) {
    Serial.print(value); Serial.print("(float): "); Serial.println(data);
  }
}


void receiveDay() {
  if (mySerial.available()) {
    day = mySerial.read();
    Serial.print("Day: "); Serial.println(day);
  }
  if (day != lastDay) {
    lastDay = day;
    dailyrainmm = 0;
  }
}



/*
  B 1 = tempC
  C 2 = pressure
  D 3 = humidity
  E 4 = winddir
  F 5 = windspeedkph
  G 6 = windgustdir
  H 7 = windgustkph
  I 8 = winddir_avg2m
  J 9 = windspdkph_avg2m
  K 10 = windgustdir_10m
  L 11 = windgustkph_10m
  M 12 = rainmm
  N 13 = dailyrainmm  */


