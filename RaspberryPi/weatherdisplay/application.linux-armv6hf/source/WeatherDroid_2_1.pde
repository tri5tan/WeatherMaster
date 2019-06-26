import java.net.*;

import org.gicentre.utils.stat.*;    // For chart classes.



// ---------------------- 
WindDisplay wind;
// WindDisplay gust; // Not used, combined with wind
TideDisplay tide;
TempHumidDisplay outside;
TempHumidDisplay inside;
PressureDisplay pressure;
RainGauge rain;
GraphDisplay graph;
// ----------------------

// ---------------------- RAW INPUT from JSON
JSONObject weatherJSON;
String weatherURL = "http://192.168.1.27:5000";
//String weatherPATH = "http://192.168.1.27:5000/data/weather.json"; // Address of Adafruit Huzzah ESP8266 WiFi
String weatherPATH = "/home/pi/weatherapp/data/weather.json"; 

String tempC_;
int tempC_int;
String tempRound;
String tempDecimal;
int pressure_; // = 12195; // raw input is in pascals
int humidity_;
String humidityStr; // Padded to 2 digits i.e. 01% 
int winddir_;
//String windHeading_ = "SW"; // Either "NE"; "SE"; "E"; "NW"; "N"; "SW"; "S"; "W";
float windspeedkph_; // = 94.2;  on weatherPro it's updated every second
int windgustdir_;
float windgustkph_;
int winddir_avg2m_; // 2 minute average
float windspdkph_avg2m_; // 2 minute average
int windgustdir_10m_; // 10 minute average
float windgustkph_10m_; // 10 minute average
float rainmm_; // 60 minute average
float dailyrainmm_; // Not used?
float daily_rainmm;
//float fivedays_rain[] = {0.0, 0.0, 0.0, 0.0, 0.0}; // last five days stored
float fivedays_rainmm; // five day total

// ---------------------- RAW INPUT from PI
String tempIn;
int humidityIn;
// ----------------------


int display_width, display_height;
int toolbarHeight = 30; // in pixels
int centerX, centerY;
//       WIND
int x1 = 124; //123.5;
int y1 = 141; //113 + 28 (toolbar height)
//      TIDE
int x2 = 370; //370.5;
int y2 = 141; //113 + 28 (toolbar height)
int x3 = 553; //552.5;
int y3 = 141; //113 + 28 (toolbar height)
int x4 = 706; //705.5;
int y4 = 141; //113 + 28 (toolbar height)
int x5 = 247;
int y5 = 367; //339 + 28 (toolbar height)
int x6 = 553; //552.5
int y6 = 367; //339 + 28 (toolbar height)
int x7 = 706;
int y7 = 367; //339 + 28 (toolbar height)
//int frameWidth, frameWidth_b, frameHeight;

boolean showWind = true;

// Graph
XYChart lineChart;
//XYChart tideChart;


// Timer
long lastSecond;
byte second;
byte seconds_30 = 0;
byte minute;
byte lastMinute = 99;
byte minutes_2 = 0;
//int hour;
int lastHour = 99;
//int day;
int lastDay = 99;
//byte days_3 = 0;
//byte week = 0;
//byte month = 1;
//int year = 2017;
int lastYear = 2017;

int golden_a, golden_b;

boolean offTheLine = true;

// Uncomment when using the Raspberry Pi Touchscreen Display. 
void settings() {
  fullScreen();
} 

void setup() {
  //size(800, 480);
  // textMode(SHAPE);
  // fullScreen();
  noCursor();
  styleSetup();  
  print("year: ");
  println(year());
  // weatherJSON = loadJSONObject(weatherPATH);
  //println(weatherJSON);
  updateData();
  display_width = width;
  display_height = height - toolbarHeight; 
  centerX = int(display_width*0.5);
  centerY = int(display_height*0.5);
  golden_a = int(display_width / 1.6180339887); // Goldener Schnitt
  golden_b = display_width - golden_a; 
  /*print("golden_a = "); 
   println(golden_a);
   print("golden_b = "); 
   println(golden_b);
   println(width); 
   println("x1 = "+ x1);
   println("y1 = "+ y1);*/

  wind = new WindDisplay(x1, y1, 247, 226);
  //gust = new WindDisplay(x1, y1, frameWidth, frameHeight);
  // tideChart = new XYChart(this);
  tide = new TideDisplay(x2, y2, 247, 226);
  pressure = new PressureDisplay(x3, y3, 117, 226);
  outside = new TempHumidDisplay(x4, y4, 189, 226, false);
  lineChart = new XYChart(this);
  //graph = new GraphDisplay(x5, y5, 494, 226); // The Graph is twice as wide // (frameWidth*2)
  graph = new GraphDisplay(x5, y5, 494, 226); // The Graph is twice as wide // (frameWidth*2)
  rain = new RainGauge(x6, y6, 117, 226);
  inside = new TempHumidDisplay(x7, y7, 189, 226, true);


  testConnection();

  lastSecond = millis();
}


void draw() {  
  second = byte(second());
  minute = byte(minute());
  // hour = hour();
  // day = day();
  // month = month();
  // println(hour + ":" + minute + ":" + second + " - " + day + "/" + month + "/" + year); 

  background(primaryLightColour); //   primaryLightColour  bkColor
  toolbar();

  // ************* Continual Functions *************    

  pressure.update(pressure_);
  //rain.update(rainmm_); //rain.update(rainmm_, dailyrainmm_);
  displayWeather();
  // *********************************************

  // if (second() % 12 == 0) {  // This will be true twice in a minute, i.e. every 30 seconds.

  if (minute != lastMinute) {
    lastMinute = minute;
    // ************* Minute Functions *************
    updateData();
    outside.update(tempC_, humidityStr);
    //outside.update(tempRound, tempDecimal, humidity_);
    //humidityIn = 5;
    inside.update(tempIn, humidityIn);
    rain.update(daily_rainmm, fivedays_rainmm); // RainGauge has the running tally of rain fall for today 
    testConnection();
    // ********************************************
  }
  if (hour() != lastHour) {
    lastHour = hour();
    // ************* Hour Functions *************
    graph.update(rainmm_);

  //  daily_rainmm += rainmm_;
    //fivedays_rain[4] = daily_rainmm; // Keeps the last day up to date with today's value
   // fivedays_rainmm = 0; // reset the five day total to count it again
  //  for (int i = 0; i < (fivedays_rain.length-1); i++) {
  //    fivedays_rainmm += fivedays_rain[i];
  //  }

    // rain.update(daily_rainmm, fivedays_rainmm); // RainGauge has the running tally of rain fall for today    

    // ******************************************
    if (day() != lastDay) {
      lastDay = day(); 
      // ************* Daily Functions *************
     // daily_rainmm = 0;    // Reset the daily rain guage
      tide.loadTideTable(); // If it's past midnight then update the date.
      // *******************************************
      if (year() != lastYear) {
        lastYear = year(); // year = year();
        // ************* Annual Functions *************
        //tide.loadTideTable(); // Loads the next tide chart for the new year
        // *******************************************
      }
    }
  }
  // stroke(0);
  //  line(0, toolbarHeight, width, toolbarHeight);
}


void updateData() {
  try
  {
    // Update Weather Data from Server
    weatherJSON = loadJSONObject(weatherPATH);
  }
  catch (Exception e) // MalformedURL, IO
  {
    println("Error");
    e.printStackTrace();
  }

  try
  {
    float tempC_float = weatherJSON.getFloat("tempC");
    tempC_int = int(tempC_float); // int() round()
    tempC_ = nf(tempC_int, 2);
    //if ( tempC_float > 9.9 ) {
    //  tempC_ = str(round(tempC_float));
    //} else {
    String temp = String.format("%.1f", tempC_float);
    if (tempC_float > 9.999) {
      //tempC_ = String.format("%.1f", tempC_float); // Rounds it to one decimal place as it converts it to a string
      tempRound = temp.substring(0, 2);
      tempDecimal = temp.substring(2);
    } else {
      //tempC_ = String.format("%.1f", tempC_float); // Rounds it to one decimal place as it converts it to a string
      tempRound = temp.substring(0, 1);
      tempDecimal = temp.substring(1);
    }

    //}
    pressure_ = weatherJSON.getInt("pressure");
    humidity_ = weatherJSON.getInt("humidity");
    humidityStr = nf(humidity_, 2);

    winddir_ = weatherJSON.getInt("winddir");
    //windHeading_ = weatherJSON.getString("windHeading");
    windspeedkph_ = weatherJSON.getFloat("windspeedkph");
    windgustdir_ = weatherJSON.getInt("windgustdir");
    windgustkph_ = weatherJSON.getFloat("windgustkph");
    winddir_avg2m_ = weatherJSON.getInt("winddir_avg2m");
    windspdkph_avg2m_ = weatherJSON.getFloat("windspdkph_avg2m");
    windgustdir_10m_ = weatherJSON.getInt("windgustdir_10m");
    windgustkph_10m_ = weatherJSON.getFloat("windgustkph_10m");
    rainmm_ = weatherJSON.getFloat("rainmm");
   // dailyrainmm_ = weatherJSON.getFloat("dailyrainmm");
    daily_rainmm = weatherJSON.getFloat("daily_rainmm");
    fivedays_rainmm = weatherJSON.getFloat("fivedays_rainmm");
    tempIn = weatherJSON.getString("insideTemp");    
    humidityIn = int(weatherJSON.getFloat("insideHumi"));
  }
  catch (Exception e) // MalformedURL, IO
  {
    println("get Error");
    e.printStackTrace();
  }
}


/* void getRawInput() {
 try
 {
 URL url= new URL(weatherURL);
 URLConnection connection = url.openConnection();
 connection.setRequestProperty("User-Agent", "I am a real browser like Mozilla or MSIE" );
 //String[] results = loadStrings(connection.getInputStream());
 //println(results);
 
 // Update Weather Data from Server
 weatherJSON = loadJSONObject(weatherPATH);
 
 tempC_ = weatherJSON.getFloat("tempC"); 
 pressure_ = weatherJSON.getFloat("pressure");
 humidity_ = weatherJSON.getInt("humidity");
 winddir_ = weatherJSON.getInt("winddir");
 windHeading_ = weatherJSON.getString("windHeading");
 windspeedkph_ = weatherJSON.getFloat("windspeedkph");
 windgustdir_ = weatherJSON.getString("windgustdir");
 windgustkph_ = weatherJSON.getFloat("windgustkph");
 winddir_avg2m_ = weatherJSON.getString("winddir_avg2m");
 windspdkph_avg2m_ = weatherJSON.getFloat("windspdkph_avg2m");
 windgustdir_10m_ = weatherJSON.getString("windgustdir_10m");
 windgustkph_10m_ = weatherJSON.getFloat("windgustkph_10m");
 rainmm_ = weatherJSON.getFloat("rainmm");
 dailyrainmm_ = weatherJSON.getFloat("dailyrainmm");
 // println(temp + ", " + humi);
 }
 catch (Exception e) // MalformedURL, IO
 {
 println("Error");
 tempC_ = 15.3;
 humidity_ = 80;
 e.printStackTrace();
 }
 
 //windspeedkph_ = map(mouseX, 0, width, 0, 66);
 //pressure_ = map(mouseX, 0, width, 85000.0, 110000.0);
 // tempC_ = 15.3;
 // humidity_ = 80;
 //tempIn = 21;
 //humidityIn = 30;
 } */

/*
void getTestInput() {
 windspeedkph_ = map(mouseX, 0, width, 0, 66);
 pressure_ = map(mouseX, 0, width, 85000.0, 110000.0);
 tempC_ = 15.3;
 humidity_ = 80;
 tempIn = 21;
 humidityIn = 30;
 }
 */


void displayWeather() {
  //displayTime();
  graph.display();
  //if(showWind) 
  wind.display(winddir_avg2m_, windspdkph_avg2m_, windgustdir_10m_, windgustkph_10m_); // (winddir_, windspeedkph_)
  //else
  //gust.display(windgustdir_10m_, windgustkph_10m_);
  // tide.run();  //  
  //tide.calculate(); 
  tide.display(); 
  pressure.display();
  outside.display();  
  //graph.display();
  rain.display();
  inside.display();
}


void displayTime() {
  String m = nf(minute(), 2);  // Values from 0 - 59
  // if (minute
  // print("minute String: "); 
  // println(m);
  int h = hour();
  String currentTime = (h + ":" + m);
  pushStyle();
  textFont(clockFont);
  textAlign(CENTER, TOP);
  fill(clockTint);
  text(currentTime, centerX, centerY-90);
  popStyle();
}








void setGradient(int x, int y, float w, float h, color c1, color c2, int axis ) {
  int Y_AXIS = 1;
  int X_AXIS = 2;
  noFill();

  if (axis == Y_AXIS) {  // Top to bottom gradient
    for (int i = y; i <= y+h; i++) {
      float inter = map(i, y, y+h, 0, 1);
      color c = lerpColor(c1, c2, inter);
      stroke(c);
      line(x, i, x+w, i);
    }
  } else if (axis == X_AXIS) {  // Left to right gradient
    for (int i = x; i <= x+w; i++) {
      float inter = map(i, x, x+w, 0, 1);
      color c = lerpColor(c1, c2, inter);
      stroke(c);
      line(i, y, i, y+h);
    }
  }
}

void mousePressed() {
  if (mouseX >= 5 && mouseX <= 25 && mouseY >= 5 && mouseY <= 25) {
    exit();
  }
}

void testConnection() {
  try {
    //Process ping = Runtime.getRuntime().exec("ping " + " -c " + "www.google.com");
    URL url= new URL("https://www.google.com");
    URLConnection connection = url.openConnection();
    connection.setRequestProperty("User-Agent", "I am a real browser like Mozilla or MSIE" );
    InputStream input = connection.getInputStream();
    //String[] results = loadStrings(input);

    println("On the Line");
    offTheLine = false;
  }
  catch(IOException e) {
    e.printStackTrace();
    println("Off the Line");
    offTheLine =  true;
  }
}

void toolbar() {
  pushStyle();
  //noFill(); 
  //stroke(0);
  //strokeWeight(5);
  fill(0);
  noStroke();
  rectMode(CORNER);
  rect(0, 0, width, toolbarHeight);
  popStyle();


  /*PGraphics exitButton;
   exitButton = createGraphics(20, 20);
   exitButton.beginDraw();
   exitButton.stroke(highColour2);
   exitButton.line(5, 10, 15, 10);
   exitButton.line(10, 5, 10, 15);
   exitButton.endDraw();
   pushMatrix();
   translate(15, 15);
   rotate(QUARTER_PI);
   image(exitButton, 0, 0);
   popMatrix(); */



  pushStyle();
  String m = nf(minute(), 2);
  String clock = hour() + ":" + m; // ******* 14:03 is displaying as 14:3  need to format string to 2 digits
  textSize(clockSize);
  textAlign(CENTER, CENTER);
  fill(primaryDarkColour);
  text(clock, width*0.5, 14);
  textSize(h6);
  textAlign(LEFT, CENTER);
  text("Muriwai Weather", 10, 14); // text("Muriwai Weather", width*0.1, 14);

  if (offTheLine) fill(offlineColor);
  textFont(onlineFont);
  //textFont(winddirFont);
  //textFont(numFont);
  textSize(h6);
  text("Online", 115, 14); //  text("Online", width*0.19, 14);

  fill(primaryDarkColour);
  String date = day() + "/" + month() + "/" + year();
  textAlign(RIGHT, CENTER);
  textSize(dateSize); // dateSize
  fill(primaryDarkColour);
  text((date + " "), width-5, 14);
  popStyle();
}