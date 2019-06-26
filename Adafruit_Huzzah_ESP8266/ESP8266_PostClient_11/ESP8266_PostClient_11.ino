
#include <ESP8266WiFi.h>

ADC_MODE(ADC_VCC); // This allows us to use ESP.getVcc() to get a read on the input voltage from the battery

// WiFi parameters
const char* ssid = "YOUR_SSID_HERE"; // Make sure you changes these accordingly
const char* password = "YOUR_PASSWORD_HERE"; // 

// ***** Info
int sketchVersion = 11;
String hardware = "ESP8266";
float conciouseness; // Just in case....

// Set Static IP Address
IPAddress ip(192, 168, 1, 66);
IPAddress gateway(192, 168, 1, 1);
IPAddress subnet(255, 255, 255, 0);

// ******************************************** To Connect to Muriwai Pi
const char* muriwaipi = "STATIC_IP_ADDRESS"; // Change this e.g. "192.168.1.27"  The IP address of the Muriwai Pi (Raspberry Pi)
const int port = 5000; // You may need to change this
String path = "/submitweather";
// ********************************************

boolean WiFi_MODE = false;
byte MODE = 0;
byte last_MODE = 0;
// 0 = receive data from weatherPro
// 1 = transmit(post) data over network for raspberry pi to collect
// 3 = sleep mode, sleeps for x mins
int sleepTime_min = 5; // Is dictated by MuriwaiPi, 5 min default
int battMinimum = 3000; // The battery minimum that is required to operate, is set by Muriwai Pi

// ******************************************************** Connect to weatherPro via Serial, variables that will be recieved
#include <SoftwareSerial.h>
SoftwareSerial meSerial(12, 13); // (RX, TX); weatherPro: (6, 7); (yellow wire, green wire) respectively

// Variables from weatherPro 
int weatherPro; // Sketch Version No. of weatherPro. For sanity purposes to know which version I uploaded to it without connecting it to the computer again.
float tempC;
int pressure;
int humidity;
int winddir;
float windspeedkph;
int windgustdir; 
float windgustkph;
int winddir_avg2m;
float windspdkph_avg2m;
int windgustdir_10m;
float windgustkph_10m;
float rainmm;
float dailyrainmm;
uint32_t battVcc;
// ********************************************************


char* buffer_in; // for debugging

long lastSecond;
byte seconds = 0;
byte seconds_30 = 0;
byte minutes;
byte day;
byte lastDay = -1; // Ensures that it resets the dailyRainmm






void setup(void)
{
  // Start Serial
  Serial.begin(115200);
  
  // Start SoftwareSerial to connect to weatherPro
  meSerial.begin(115200);

  Serial.print(hardware + " - Sketch Version: " + sketchVersion);
  Serial.println(" ...Initiating");
  
  
  // Static IP Setup Info Here...
  WiFi.config(ip, gateway, subnet);

  lastSecond = millis();
  Serial.println("Receiving Data");
}





void loop() {

  if (millis() - lastSecond >= 1000) {
    lastSecond += 1000;

    if (++seconds_30 > 29) { // Receive Mode and Serve Mode last 30 seconds each. Sleep Mode time diffined seperately
      seconds_30 = 0;
      MODE++; // SleepMode will reset it back to 0. Not needed: if (++MODE > 2) MODE = 0;
      //Serial.print("MODE = "); Serial.println(MODE);


      battVcc = ESP.getVcc();
      Serial.print("Battery Voltage: "); Serial.println(battVcc); Serial.println();      
      if (battVcc < battMinimum) MODE = 2; // If the Battery Voltage is too low skip straight to sleep mode

      // Check if it is a new day
      if (day != lastDay) {
        lastDay = day;
        // Send day variable to arduino pro so it can reset dailyrain etc
        meSerial.write(day);
      }
    }
  }


  // Check to see if WiFi should be turned on. Only happens after wake up from deep sleep.
  if (last_MODE == 0 && MODE == 1) {
    Serial.println("Reconnecting WiFi");
    WiFi.forceSleepWake(); // Turn WiFi back on
    connectWiFi();
  }


  last_MODE = MODE;

  switch (MODE) {
    case 0:
      receiveWeather(); // Recieves serial data from weatherPro (Arduino Pro Mini)
      break;
    case 1:
      postClient(); // Post data via wifi to Muriwai Pi (Raspberry Pi)
      break;
    case 2:
      sleepMode(); // Save power, take a nap.
      break;
  }
}


void connectWiFi() {
  // Connect to WiFi
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("");
  Serial.println("WiFi connected");

  // Print the IP address
  Serial.println(WiFi.localIP());
  Serial.println();
}





// ********************** Receive weather data from the Arduino Pro via serial *************************
void receiveWeather() {
  // Compiles each data set into the correct variable
  // Variable KEY at the bottom of the page
  String dataString = "";
  String index;
  String data = "";

  if (meSerial.available() > 0) {
    dataString = meSerial.readStringUntil('\n'); // Read until new line
  }

  index = dataString.substring(0, 1);
  //Serial.print("dataString: "); Serial.println(dataString);
  data = dataString.substring(1);
  //Serial.print("data: "); Serial.println(data);


  if (dataString.startsWith("A")) { // *** weatherPro sketch version
    weatherPro = data.toInt();
    Serial.print(index); Serial.print(": "); Serial.println(weatherPro);
  }

  if (dataString.startsWith("B")) { // *** temperature
    if (data.length() > 0) {
      tempC = data.toFloat();
    }
    Serial.print(index); Serial.print(": "); Serial.println(tempC); // tempC
  }

  if (dataString.startsWith("C")) {// *** Pressure
    if (data.length() > 0) {
      pressure = data.toInt();
      //Serial.println("pressure converted");
    }
    Serial.print(index); Serial.print(": "); Serial.println(pressure);
  }


  if (dataString.startsWith("D")) { // *** Humidity
    if (data.length() > 0) {
      humidity = data.toInt();
    }
    Serial.print(index); Serial.print(": "); Serial.println(humidity);
  }

  if (dataString.startsWith("E")) { // *** Wind Direction - int
    if (data.length() > 0) {
      winddir = data.toInt();
    }
    Serial.print(index); Serial.print(": "); Serial.println(winddir);
  }

  if (dataString.startsWith("F")) { // *** Wind Speed - kph
    if (data.length() > 0) {
      windspeedkph = data.toFloat();
    }
    Serial.print(index); Serial.print(": "); Serial.println(windspeedkph);
  }

  if (dataString.startsWith("G")) { // *** Wind Gust Direction
    if (data.length() > 0) {
      windgustdir = data.toInt();
    }
    Serial.print(index); Serial.print(": "); Serial.println(windgustdir);
  }

  if (dataString.startsWith("H")) { // *** windgustkph
    if (data.length() > 0) {
      windgustkph = data.toFloat();
    }
    Serial.print(index); Serial.print(": "); Serial.println(windgustkph);
  }

  if (dataString.startsWith("I")) { // *** Wind Direction_avg2m
    if (data.length() > 0) {
      winddir_avg2m = data.toInt();
    }
    Serial.print(index); Serial.print(": "); Serial.println(winddir_avg2m);
  }

  if (dataString.startsWith("J")) { // *** windspdkph_avg2m
    if (data.length() > 0) {
      windspdkph_avg2m = data.toFloat();
    }
    Serial.print(index); Serial.print(": "); Serial.println(windspdkph_avg2m);
  }

  if (dataString.startsWith("K")) { // windgustdir_10m
    if (data.length() > 0) {
      windgustdir_10m = data.toInt();
    }
    Serial.print(index); Serial.print(": "); Serial.println(windgustdir_10m);
  }

  if (dataString.startsWith("L")) { // windgustkph_10m
    if (data.length() > 0) {
      windgustkph_10m = data.toFloat();
    }
    Serial.print(index); Serial.print(": "); Serial.println(windgustkph_10m);
  }

  if (dataString.startsWith("M")) { // rainmm
    if (data.length() > 0) {
      rainmm = data.toFloat();
    }
    Serial.print(index); Serial.print(": "); Serial.println(rainmm);
  }

  if (dataString.startsWith("N")) { // dailyrainmm
    if (data.length() > 0) {
      dailyrainmm = data.toFloat();
    }
    Serial.print(index); Serial.print(": "); Serial.println(dailyrainmm); Serial.println();
  }  
}







// ********************** Post the data to the Raspberry Pi Server via WiFi *************************
void postClient() {
  Serial.println("Compiling POST (client)");

  WiFiClient client;

  if (!client.connect(muriwaipi, port)) {
    Serial.println("connection failed");
    return;
  }

  // Compile message into one string
  String postMsg = String("{\"tempC\":\"" + String(tempC) + "\", \"pressure\":\"" + pressure + "\", \"humidity\":\"" + humidity + "\", \"winddir\":\"" + winddir + "\", \"windspeedkph\":\"" + windspeedkph + "\", \"windgustdir\":\"" + windgustdir + "\", \"windgustkph\":\"" + windgustkph + "\", \"winddir_avg2m\":\"" + winddir_avg2m + "\", \"windspdkph_avg2m\":\"" + windspdkph_avg2m + "\", \"windgustdir_10m\":\"" + windgustdir_10m + "\", \"windgustkph_10m\":\"" + windgustkph_10m + "\", \"rainmm\":\"" + rainmm + "\", \"dailyrainmm\":\"" + dailyrainmm + "\", \"batteryVoltage\":\"" + battVcc + "\", \"sketchVersion\":\"" + sketchVersion + "\", \"weatherPro_v\":\"" + weatherPro + "\", \"hardware\":\"" + hardware + "\" }");
  Serial.print("Post Message: "); Serial.println(postMsg);


  // This will post the request to the raspberry pi server
  client.print(String("POST ") + path + " HTTP/1.1\r\n" +
               "Host: " + muriwaipi + "\r\n" +
               "User-Agent: Arduino/1.0\r\n" +
               "Connection: close\r\n" +
               "Content-Type: application/json\r\n" +
               "Content-Length: " + postMsg.length() + "\r\n\r\n" +
               postMsg + "\r\n");
  unsigned long timeout = millis();
  while (client.available() == 0) {
    if (millis() - timeout > 5000) {
      Serial.println(">>> Client Timeout !");
      client.stop();
      return;
    }
  }

  // Read all the lines of the reply (payload) from server and print them to Serial
  String payload = "";
  while (client.available()) {
    String line = client.readStringUntil('\r');
    //Serial.print(line);
    payload += line;
  }
  Serial.print("Payload: "); Serial.println(payload);

  int index = find_text("received on day:", payload);
  //Serial.print("Index of received: "); Serial.println(index);
  sleepTime_min = find_value("sleepTime_min:", payload, 2);
  battMinimum = find_value("battMinimum:", payload, 4);

  if (index > -1) {
    Serial.println("Weather Data Received by the Server");
    MODE++; // This will skip the remainder of the 30 second period so it can continue to sleep mode
  }

  Serial.println();
  Serial.println("closing connection");
  Serial.println();
  delay(100);
}

// ********************** Put the ESP8266 to sleep between posts to save on power *************************
void sleepMode() {
  MODE = 0; // Reset the Mode for when the unit wakes up from the deep sleep reset
  Serial.println("Disconnecting from server and wifi");
  WiFi.forceSleepBegin(); // Turns off just WiFi
  Serial.print("Enter Deep Sleep mode for "); Serial.print(sleepTime_min); Serial.println("mins");
  Serial.println();
  ESP.deepSleep(sleepTime_min * 60 * 1000000);  
}




int find_text(String needle, String haystack) {
  int foundpos = -1;
  for (int i = 0; i <= haystack.length() - needle.length(); i++) {
    if (haystack.substring(i, needle.length() + i) == needle) {
      foundpos = i;
      int startPos = foundpos + needle.length() + 1; // + 1 for " " before the day number
      extractDay(haystack.substring(startPos, startPos + 1));
    }
  }
  return foundpos;
}


int find_value(String needle, String haystack, int value_length) {
  int result = 5; // Default
  for (int i = 0; i <= haystack.length() - needle.length(); i++) {
    if (haystack.substring(i, i + needle.length()) == needle) {
      int foundpos = i;
      int startPos = foundpos + needle.length() + 1; // + 1 for " " (space) before the number
      int num = (haystack.substring(startPos, startPos + value_length)).toInt(); // eg 'sleepTime_min: 02' ; +2 because the int has two digits
    
      result = num;
    }
  }
  return result;
}


void extractDay(String dayStr) {
  day = byte(dayStr.toInt());
  // Serial.print("Day: "); Serial.println(day);
}




// KEY
/* A 0 = sketchversion
   B 1 = (tempC*100)
   C 2 = (pressure*100)
   D 3 = humidity
   E 4 = winddir
   F 5 = (windspeedkph*100)
   G 6 = windgustdir
   H 7 = (windgustkph*100)
   I 8 = winddir_avg2m
   J 9 = (windspdkph_avg2m*100)
   K 10 = windgustdir_10m
   L 11 = (windgustkph_10m*100)
   M 12 = (rainmm*100)
   N 13 = (dailyrainmm*100)

*/
