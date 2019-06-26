import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.net.*; 
import org.gicentre.utils.stat.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class WeatherDroid_2_1 extends PApplet {



    // For chart classes.



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
public void settings() {
  fullScreen();
} 

public void setup() {
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
  centerX = PApplet.parseInt(display_width*0.5f);
  centerY = PApplet.parseInt(display_height*0.5f);
  golden_a = PApplet.parseInt(display_width / 1.6180339887f); // Goldener Schnitt
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


public void draw() {  
  second = PApplet.parseByte(second());
  minute = PApplet.parseByte(minute());
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


public void updateData() {
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
    tempC_int = PApplet.parseInt(tempC_float); // int() round()
    tempC_ = nf(tempC_int, 2);
    //if ( tempC_float > 9.9 ) {
    //  tempC_ = str(round(tempC_float));
    //} else {
    String temp = String.format("%.1f", tempC_float);
    if (tempC_float > 9.999f) {
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
    humidityIn = PApplet.parseInt(weatherJSON.getFloat("insideHumi"));
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


public void displayWeather() {
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


public void displayTime() {
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








public void setGradient(int x, int y, float w, float h, int c1, int c2, int axis ) {
  int Y_AXIS = 1;
  int X_AXIS = 2;
  noFill();

  if (axis == Y_AXIS) {  // Top to bottom gradient
    for (int i = y; i <= y+h; i++) {
      float inter = map(i, y, y+h, 0, 1);
      int c = lerpColor(c1, c2, inter);
      stroke(c);
      line(x, i, x+w, i);
    }
  } else if (axis == X_AXIS) {  // Left to right gradient
    for (int i = x; i <= x+w; i++) {
      float inter = map(i, x, x+w, 0, 1);
      int c = lerpColor(c1, c2, inter);
      stroke(c);
      line(i, y, i, y+h);
    }
  }
}

public void mousePressed() {
  if (mouseX >= 5 && mouseX <= 25 && mouseY >= 5 && mouseY <= 25) {
    exit();
  }
}

public void testConnection() {
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

public void toolbar() {
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
  text(clock, width*0.5f, 14);
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
class GraphDisplay {

  int cx, cy; // Centre   
  int f_width; // Use this to scale everything to
  int f_height;
  int graph_width; 
  int graph_height;
  PGraphics graphFrame;

  // float[] rainPoints = new float[36];
  /* float[] rainPoints = {
   15, 16, 29, 34, 33, 24, 25, 16, 29, 34, 33, 54, 25, 16, 29, 34, 33, 24, 25, 16, 29, 34, 33, 24};
   */
  //float[] rainPoints = { 25, 34, 23, 12, 34, 33, 24};
  //int mins;
  float rain_current; 
  float[] dailyrain;
  //  float[] time24 = new float[24];
  float[] hour13 = new float[13]; //  float[] hour7 = new float[7];
  float[] rainPoints13 = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}; // float[] rainPoints7 = {0, 0, 0, 0, 0, 0, 0};
  //float[] rainPoints7 = {13, 6, 3, 4, 3, 5, 0};



  GraphDisplay(int x, int y, int fW, int fH) {
    cx = x;
    cy = y;    
    f_width = fW;
    f_height = fH;
    graph_width = PApplet.parseInt(f_width * 1.3f); //0.8  int(f_width * 0.9);
    graph_height = PApplet.parseInt(f_height * 1.2f); //0.7 int(f_height * 0.8);
    graphFrame = createGraphics(f_width, f_height);

    /* for (int i = 0; i < 24; i++) { 
     time24[i] = i;
     }    
     print("time24: "); 
     println(time24); */

    /*lineChart.setData(new float[] {1900, 1910, 1920, 1930, 1940, 1950, 
     1960, 1970, 1980, 1990, 2000}, 
     new float[] { 6322, 6489, 6401, 7657, 9649, 9767, 
     12167, 15154, 18200, 23124, 28645}); */



    // Axis formatting and labels.
    lineChart.showXAxis(true); 
    lineChart.showYAxis(true); 
    lineChart.setMinY(0);
    // lineChart.setMinX(0);
    //lineChart.setMaxX(12);


    //lineChart.setYFormat("$###,###");  // Monetary value in $US
    //lineChart.setXFormat("0000");      // Year
    lineChart.setYFormat("###mm");  // in mm
    lineChart.setXFormat("##:'00'");      // Hours
    lineChart.setAxisValuesColour(primaryDarkColour);

    // Symbol colours
    lineChart.setPointColour(color(180, 50, 50, 100));
    lineChart.setPointSize(5); // 5
    lineChart.setLineWidth(2);
    lineChart.setLineColour(primaryDarkColour);
    //lineChart.setYAxisLabel("Rainfall");
    lineChart.setXAxisLabel("Last 6 hours");
  }


  public void display() {

    drawAxis();
    //  graphFrame.beginDraw();
    //graphFrame.background(primaryLightColour);

    /* for (int i = 0; i < rainPoints.length-1; i++) {
     float x1 = i * 10;
     float y1 = rainPoints[i];
     float x2 = (i+1) * 10;
     float y2 = rainPoints[(i+1)];
     graphFrame.line(x1, y1, x2, y2);
     } */

    pushStyle();
    pushMatrix();
    // translate(cx*0.2, cy*0.75);
    translate(-170, cy*0.75f);
    textSize(9);
    // lineChart.draw(15, 15, width-30, height-30);
    lineChart.draw(0, 0, graph_width, graph_height);
    // lineChart.draw(0, 0, 400, 300);
    popMatrix();
    popStyle();

    //graphFrame.endDraw();

    //pushMatrix();
    // scale(1, 1);
    //translate(0, -50);
    //  imageMode(CENTER);
    // image(graphFrame, (f_width/2), cy);
    //image(graphFrame, cx, cy);
    // popMatrix();

    /*
     pushStyle();
     fill(highlighterPINK);
     rectMode(CENTER);
     rect(cx, cy, f_width, f_height);    
     popStyle();
     */
  }


  public void update(float rainHour) {

    //  print("minute: " + minute());
    //   String mins = nf(minute(), 2);
    //print(" - mins: " + mins);
    // String min = mins.substring(1);
    //println(" - min: " + min);

    /*   if (min == "0") {
     println("In the building");
     
     rainPoints = append(rainPoints, rain_current); // Add new value to array
     
     for (int i = 0; i < rainPoints.length-1; i++) { // Shift each value down one in the array
     rainPoints[i] = rainPoints[(i+1)];
     }
     
     rainPoints = shorten(rainPoints); // Finally remove the last value to keep a consistant length
     
     }
     */

    int lastPosition = hour13.length-1; // = same as  rainPoints7.length-1 
    hour13[lastPosition] = hour();

    for (int i = (lastPosition-1); i >= 0; i--) { // Shift down the array
      hour13[i] = hour() - (lastPosition - i);
    }
    //for(int i = 0; i < hour7.length-1; i++){

    //}

    //  print("hour7: "); 
    //  println(hour7);

    for (int i = 0; i < lastPosition; i++) {
      rainPoints13[i] = rainPoints13[i+1];
    }
    rainPoints13[lastPosition] = rainHour;



    lineChart.setData(hour13, rainPoints13);
  }

  public void drawAxis() {
    graphFrame.beginDraw();
    graphFrame.background(primaryLightColour); //  testing: lightpurple
    graphFrame.stroke(c1, 120);
    // graphFrame.line(30, 30, 30, f_height-30);
    //  graphFrame.line(30, f_height-30, f_width-30, f_height-30);
  //  graphFrame.fill(c1, 120); // primaryLightColour
    //graphFrame.textFont(numFont);
  //  graphFrame.textSize(h5);
   // graphFrame.textAlign(CENTER, TOP);
    
    graphFrame.fill(primaryDarkColour, 120); 
    graphFrame.textAlign(LEFT, CENTER);    
    graphFrame.textSize(h4);
    
    graphFrame.text("rainfall", f_width*0.2f, f_height*0.05f);
    graphFrame.fill(primaryDarkColour); 
    graphFrame.textAlign(CENTER, TOP);
    graphFrame.textSize(h6);
    graphFrame.text("Last 12 hours", f_width*0.5f, f_height*0.9f);
    graphFrame.endDraw();

    pushMatrix();
    // scale(1, 1);
    translate(cx, cy);
    imageMode(CENTER);
    // image(graphFrame, (f_width/2), cy);
    //image(graphFrame, cx, cy);
    image(graphFrame, 0, 0);
    popMatrix();
  }
}
class PressureDisplay {  // class PressureFrame extends WeatherFrame
  //float pascals;  // raw data comes in as pascals
  String hPascals;  // Display data as Hectopascals
  int inc = 15; // 15 is the min
  int inc_max = 70;
  int inc_min = 20;
  int circles;
  int circles_max = 17;
  int circles_min = 5;

  // This could all come under extends WeatherFrame
  int cx, cy; // Centre
  int diam;
  int text_cx, text_cy; // Text Centre
  int f_width; // Use this to scale everything to
  int f_height;

  PImage mask = createImage(50, 34, RGB);
  PGraphics pressureFrame;

  PressureDisplay(int x, int y, int fW, int fH) {
    cx = x;
    cy = y;
    text_cx = 58;// = x1;
    text_cy = 108; // = y1 - 5;
    f_width = fW;
    f_height = fH;
    diam = 100;
    circles = 5;
    //mask.loadPixels();
    //for (int i=0; i<mask.pixels.length; i++) {
    //mask.pixels[i] = color(0);
    //}
    //mask.updatePixels();

    pressureFrame = createGraphics(f_width, f_height);
  }


  public void display() {
    pressureFrame.beginDraw();
    pressureFrame.background(primaryLightColour);
    pressureFrame.pushMatrix();
    pressureFrame.translate(58.5f, 113);
    drawCircles();
    pressureFrame.popMatrix();
    pressureFrame.pushStyle();   
    pressureFrame.fill(primaryLightColour, 220);
    pressureFrame.noStroke();
    // pressureFrame.stroke(primaryLightColour, 220);
    //pressureFrame.rectMode(CENTER);
    pressureFrame.ellipse(text_cx, text_cy, 90, 90); // this is a mask
    pressureFrame.textAlign(CENTER, CENTER);
    pressureFrame.textSize((h3+5));
    pressureFrame.fill(c1); // primaryLightColour
    pressureFrame.textSize(h4);
    pressureFrame.text(hPascals, text_cx, text_cy); // this is also a mask
    pressureFrame.textSize(h6);
    pressureFrame.text("hPa", text_cx+35, text_cy-1); 
    // image(mask, cx, cy);
    //blend(mask, 0, 0, 50, 34, (cx-25), (cy-17), 50, 34, SUBTRACT); // https://processing.org/reference/blend_.html 
    pressureFrame.fill(0);
    pressureFrame.textSize(h4); // 25
    // pressureFrame.text(hPascals, text_cx, text_cy);
    pressureFrame.popStyle();

    pressureFrame.endDraw();  
    // imageMode(CENTER);
    image(pressureFrame, cx, cy);

    // Draw frame -------------------------------------    
    /*
    noFill();
     stroke(frameBoxColour);
     strokeWeight(1.3);
     rectMode(CENTER);
     rect(cx, cy, f_width, f_height);
     */

    /*
     pushStyle();
     fill(highlighterPINK);
     rectMode(CENTER);
     rect(cx, cy, f_width, f_height);    
     popStyle();
     */
  }

  public void update(int p) {
    //pressure_ = p;  // raw input is in pascals
   // int hectopascals = int(p/100);
    // hPascals = str(hectopascals);
    hPascals = str(p); 

    inc = PApplet.parseInt(map(p, 980, 1015, inc_min, inc_max)); // replace 850 and 1100 with actual recorded low and high
    // println("inc " + inc);
    circles = PApplet.parseInt(map(inc, inc_min, inc_max, circles_max, circles_min));
    // println("circles " + circles);
  }

  public void drawCircles() {   
    for (int i = 0; i < circles; i++) {
      pressureFrame.noFill();
      pressureFrame.stroke(bluegrey, map(inc, inc_min, inc_max, 180, 50));
      pressureFrame.strokeWeight(2);
      pressureFrame.ellipseMode(CENTER);
      pressureFrame.ellipse(0, 0, diam, diam);      
      diam += inc;
    }
    diam = inc;

    pushStyle();
    ellipseMode(RADIUS); // https://processing.org/examples/radialgradient.html

    popStyle();
  }
}
class RainGauge {

  int cx, cy; // Centre   
  int f_width; // Use this to scale everything to
  int f_height;
  int title_x, title_y, title2_x, title2_y;
  int text_cx, text_cy; 
  PGraphics rainFrame, rainFrame_invert;

  //float rain_fivedays; 
  //float daily_total;
  String day_current = "0";
  String fiveday = "0";



  RainGauge(int x, int y, int fW, int fH) {
    cx = x;
    cy = y;    
    f_width = fW; // 117
    f_height = fH; // 226
    title_x = PApplet.parseInt(f_width * 0.5f);
    title_y = PApplet.parseInt(f_height * 0.05f); // int(f_height * 0.25) f_height - 5
    title2_x = PApplet.parseInt(f_width * 0.5f);
    title2_y = PApplet.parseInt(f_height * 0.53f); 
    text_cx = 80; // 58
    text_cy = PApplet.parseInt(f_height * 0.45f); // 0.65 108
    rainFrame = createGraphics(f_width, f_height);
    rainFrame_invert = createGraphics(f_width, f_height);
  }


  public void display() {
    /* pushStyle();
     fill(highlighterPINK);
     rectMode(CENTER);
     stroke(highlighterGREEN);
     strokeWeight(6);
     rect(cx, cy, f_width, f_height);    
     popStyle(); */

    rainFrame.beginDraw();
    rainFrame.background(primaryLightColour);
    rainFrame.pushStyle(); 
    rainFrame.fill(c1, 120); // primaryLightColour
    rainFrame.textSize(h6);
    rainFrame.textAlign(CENTER, TOP);
    rainFrame.text("Daily Rain Gauge", title_x, title_y);
    rainFrame.text("5 Day Rain Gauge", title2_x, title2_y);
    rainFrame.fill(c1); // primaryLightColour
    rainFrame.textSize(h2);
    rainFrame.textAlign(RIGHT, BOTTOM);
    if (day_current.length() >= 3) {   
      //rainFrame.textSize(h3);
      rainFrame.text(day_current, text_cx+22, text_cy);
      rainFrame.textSize(h7);
      rainFrame.textAlign(LEFT, BOTTOM);
      rainFrame.text("mm", text_cx+17, text_cy-8);
    } else if (day_current.length() >= 2) {      
      rainFrame.text(day_current, text_cx+7, text_cy);
      rainFrame.textSize(h5);
      rainFrame.textAlign(LEFT, BOTTOM);
      rainFrame.text("mm", text_cx+5, text_cy-8);
    } else {
      rainFrame.text(day_current, text_cx-10, text_cy);
      rainFrame.textSize(h5);
      rainFrame.textAlign(LEFT, BOTTOM);
      rainFrame.text("mm", text_cx-10, text_cy-8);
    }

    rainFrame.textAlign(RIGHT, BOTTOM);
    rainFrame.textSize(h2);
    if (fiveday.length() >= 3) {  
      rainFrame.text(fiveday, text_cx+22, text_cy+110);
      rainFrame.textSize(h7);
      rainFrame.textAlign(LEFT, BOTTOM);
      rainFrame.text("mm", text_cx+17, text_cy+102);
    } else if (fiveday.length() >= 2) {  
      rainFrame.text(fiveday, text_cx+7, text_cy+110);
      rainFrame.textSize(h5);
      rainFrame.textAlign(LEFT, BOTTOM);
      rainFrame.text("mm", text_cx+5, text_cy+102);
    } else {
      rainFrame.text(fiveday, text_cx-10, text_cy+110);
      rainFrame.textSize(h5);
      rainFrame.textAlign(LEFT, BOTTOM);
      rainFrame.text("mm", text_cx-10, text_cy+102);
    }

    //rainFrame.text("mm", text_cx+10, text_cy);
    rainFrame.popStyle();
    rainFrame.endDraw();

    image(rainFrame, cx, cy);
  }


  public void update(float rainFloat, float rainFiveFloat) {
    //rainFloat = 10; // for format testing
  //  rainFiveFloat = 7;
    String rainString = nf(PApplet.parseInt(rainFloat), 1);

    if (rainString.length() > 1) { // if rain value is greater than 1 digit and doesn't equal zero
      day_current = rainString;
    } else if (rainString.length() == 1 && rainFloat != 0) { // (int(rainFloat) != 0)   
      day_current = nf(rainFloat, 1, 1); // Formats the string to have 1 decimal place
    } else {
      day_current = rainString;
    }

    fiveday = nf(PApplet.parseInt(rainFiveFloat), 1);
  }
}
class TempHumidDisplay {

  int cx, cy; // Centre 
  int tx_1, ty_1, tx_1b, ty_1b, tx_2, ty_2, tx_3, ty_3; // Temperature Text Centre
  int lx_1, ly_1; // "Latest"
  int hx_1, hy_1, hx_2, hy_2, hx_3, hy_3; // Humidity Text Centre
  int location_x, location_y;
  int f_width; // Use this to scale everything to
  int f_height;
  PGraphics tempHumiFrame;
  boolean inside;

  String temp = "22.9"; // e.g. 
  String temp_whole = "22";
  String temp_decimal = ".9";
  int humi;
  String humi_String = "00";

  TempHumidDisplay(int x, int y, int fW, int fH, boolean in) {
    cx = x;
    cy = y;    
    f_width = fW;
    f_height = fH;
    inside = in;    
    tempHumiFrame = createGraphics(f_width, f_height);
    tx_1 = PApplet.parseInt(f_width * 0.98f); // Current Temp
    ty_1 = PApplet.parseInt(f_height * 0.33f);
    tx_1b = PApplet.parseInt(f_width * 0.53f); // Current Temp Decimal
    ty_1b = PApplet.parseInt(f_height * 0.455f);
    tx_2 = PApplet.parseInt(f_width * 0.9f); // High Temp
    ty_2 = PApplet.parseInt(f_height * 0.65f);
    tx_3 = PApplet.parseInt(f_width * 0.5f); // Low Temp
    ty_3 = PApplet.parseInt(f_height * 0.65f);
    lx_1 = PApplet.parseInt(f_width * 0.07f); 
    ly_1 = PApplet.parseInt(f_height * 0.65f);
    hx_1 = PApplet.parseInt(f_width * 0.07f); // int(f_width * 0.1666); // Current Humi
    hy_1 = PApplet.parseInt(f_height * 0.68f);
    hx_2 = PApplet.parseInt(f_width * 0.58f); // percentage
    hy_2 = PApplet.parseInt(f_height * 0.64f);
    hx_3 = PApplet.parseInt(f_width * 0.8333f); // Low Humi
    hy_3 = PApplet.parseInt(f_height * 0.8333f);
    location_x = PApplet.parseInt(f_width * 0.07f);
    location_y = PApplet.parseInt(f_height * 0.07f);
  }

  public void update(String t, int h) {
    temp = t;
    humi = h;
    humi_String = nf(h, 2);
  }
  public void update(String t, String h) {
    temp = t;
    humi_String = h;
  }
  public void update(String whole, String decimal, int h) {
    temp_whole = whole;
    temp_decimal = decimal;
    humi = h;
    humi_String = nf(h, 2);
    println(humi_String);
  }


  public void display() {   
    tempHumiFrame.beginDraw();
    if (inside) {
      tempHumiFrame.background(primaryLightColour); // primaryDarkColour
      tempHumiFrame.noFill();
     // tempHumiFrame.rectMode(CENTER);
      tempHumiFrame.stroke(primaryDarkColour);
      tempHumiFrame.strokeWeight(4);
      tempHumiFrame.rect(0, 0, f_width+10, f_height+10);
      tempHumiFrame.textAlign(RIGHT, CENTER);
      tempHumiFrame.fill(primaryDarkColour); // primaryLightColour
      tempHumiFrame.textSize(h0);
      // tempHumiFrame.text(temp_whole + "\u00b0", tx_1, ty_1); // This is the int version without rounding.
      tempHumiFrame.text(temp + "\u00b0", tx_1, ty_1); 
      /*
      tempHumiFrame.textAlign(LEFT, CENTER); 
       tempHumiFrame.fill(primaryLightColour, 80);
       tempHumiFrame.textSize(h3);
       tempHumiFrame.text(temp_decimal, tx_1b, ty_1b); // This is all for the decimal numbers of tempC
       */
      tempHumiFrame.fill(primaryDarkColour, 120); // fill(primaryLightColour, 120)  fill(primaryLightColour);
      tempHumiFrame.textAlign(LEFT, TOP);
      tempHumiFrame.textSize(h6);
      tempHumiFrame.text("humidity", hx_1, hy_1);
      tempHumiFrame.textAlign(CENTER, TOP);
      tempHumiFrame.textSize(h2);
      tempHumiFrame.text(humi_String, hx_2, hy_2);
      tempHumiFrame.textSize(h4);
      tempHumiFrame.text("%", hx_2+45, hy_1);
     /* tempHumiFrame.textAlign(CENTER, TOP);
      tempHumiFrame.textSize(h4); // LOW HIGH Temp
      tempHumiFrame.fill(highColour2);
      tempHumiFrame.text("20\u00b0", tx_2, ty_2);
      tempHumiFrame.fill(lowColour2);
      tempHumiFrame.text("11\u00b0", tx_3, ty_3); */
      /*tempHumiFrame.textSize(h5);  // LOW HIGH Humidity
       tempHumiFrame.fill(lowColour);
       tempHumiFrame.text("20%", hx_2, hy_2);
       tempHumiFrame.fill(highColour);
       tempHumiFrame.text("80%", hx_3, hy_3);*/
      //tempHumiFrame.fill(primaryLightColour);
      tempHumiFrame.textAlign(LEFT, CENTER);
      tempHumiFrame.textSize(h4);
      tempHumiFrame.text("inside", location_x, location_y);
    } else {
      tempHumiFrame.background(primaryLightColour);
      tempHumiFrame.textAlign(RIGHT, CENTER);
      tempHumiFrame.fill(primaryDarkColour); //themeGreen2 bluegrey
      tempHumiFrame.textSize(h0);
      // tempHumiFrame.text(temp_whole + "\u00b0", tx_1, ty_1);
      tempHumiFrame.text(tempC_int + "\u00b0", tx_1, ty_1);
      /*tempHumiFrame.textAlign(LEFT, CENTER);
       tempHumiFrame.fill(primaryDarkColour, 80);
       tempHumiFrame.textSize(h3);
       tempHumiFrame.text(temp_decimal, tx_1b, ty_1b); */
      tempHumiFrame.fill(primaryDarkColour, 120); 
      tempHumiFrame.textAlign(LEFT, TOP);
      tempHumiFrame.textSize(h6);
      tempHumiFrame.text("humidity", hx_1, hy_1);
      tempHumiFrame.textAlign(CENTER, TOP);
      tempHumiFrame.textSize(h2);
      tempHumiFrame.text(humi_String, hx_2, hy_2);
      tempHumiFrame.textSize(h4);
      tempHumiFrame.text("%", hx_2+45, hy_1);
     /* tempHumiFrame.textAlign(LEFT, TOP);
      tempHumiFrame.textSize(h5);
      tempHumiFrame.text("latest -", lx_1, ly_1);
      tempHumiFrame.textAlign(CENTER, TOP);
      tempHumiFrame.textSize(h5); // LOW HIGH Temp
      tempHumiFrame.fill(highColour);
      tempHumiFrame.text("20\u00b0", tx_2, ty_2); 
      tempHumiFrame.fill(lowColour);
      tempHumiFrame.text("11\u00b0", tx_3, ty_3);   */  
      /*tempHumiFrame.textSize(h5); // LOW HIGH Humidity
       tempHumiFrame.fill(lowColour);
       tempHumiFrame.text("20%", hx_2, hy_2);
       tempHumiFrame.fill(highColour);
       tempHumiFrame.text("80%", hx_3, hy_3); */
      //tempHumiFrame.fill(primaryDarkColour);
      tempHumiFrame.textAlign(LEFT, CENTER);
      tempHumiFrame.textSize(h4);
      tempHumiFrame.text("outside", location_x, location_y);
    }


    tempHumiFrame.endDraw(); 
    imageMode(CENTER);
    image(tempHumiFrame, cx, cy);


    // Draw frame box -------------------------------------
    noFill();
    stroke(frameBoxColour);
    strokeWeight(1.3f);
    rectMode(CENTER);
    // rect(cx, cy, f_width, f_height);
  }
}
class Tide {
  float x;
  float y;
  String time; // Time (hours, minutes - 24 hour clock) eg 0549 = 5.49am
  float meters; // Height of tide (metres) eg 1.9 metres (NB this height is greater than the second tide of the day, therefore this is high tide)
  boolean isHigh = false;


  Tide() {   
    x = 0;
    y = 0;
    time = "null";
    meters = 0.0f;
    isHigh = false;
  }

  public void update(float x_, float y_) {
    x = x_;
    y = y_;
  }
}
class TideDisplay {
  float px, py, px2, py2;
  float angle;
  float radius = 40; //100;
  float frequency = 3;
  float x, x2;
  int cx, cy; // Centre
  int y_axis;
  //int graphPoints = 720; //  360 = 1 rotation. The graph spans: 14 hours = 840 minutes. 1 pixel per minute to display.
  int graphPoints = 216;
  float pointSize = 2.5f;
  float startMin;
  /*  color lineColour = #19CB67;
   color accentColour; // = #30327E; // 710EC9
   color LightAccentColour; // = #C1F6FF; 
   color primaryColour; // = #D9D7E3; // #E5E3ED;
   color primaryLightColour; // = #F7F5FF;
   color primaryDarkColour = #7D7C81; */

  int diam;
  int f_width; // Use this to scale everything to
  int f_height;
  int thisFontSize; 
  //int day = day();    // Values from 1 - 31
  //int month = month();  // Values from 1 - 12
  // int year = year();   // 2003, 2004, 2005, etc.
  // int s;  // Values from 0 - 59
  int min = 45;  // Values from 0 - 59
  //int hour;    // Values from 0 - 23
  Table table;
  int todaysRowID; // index for current row
  //String firstTide, secondTide, thirdTide;
  String forthTide = "0";
  Float firstHeight, secondHeight, thirdHeight, forthHeight;
  String nextLow, nextHigh;
  Float lowHeight, highHeight;
  boolean tideRising; 
  //String lastTide;
  Float lastHeight;
  String tomorrowTide, tomorrowTide2;  
  Float tomorrowHeight, tomorrowHeight2;
  PVector bottomRight, bottomLeft, centerTop, topRight, topLeft, centerBottom;

  Tide[] tides = new Tide[8]; //6
  Tide lastTide, nextTide, secondTide;
  Tide tide_A, tide_B, tide_C, tide_D; // Tides that are displayed, they cycle through tides[] throughout the day.
  // tide_A, tide_B are past tides
  PGraphics tideFrame, tideInfoFrame;

  int tideGraph_width, tideGraph_height;
  //float[] heights = new float[5];
  float[] heights = {0.3f, 1.9f, 0.4f, 1.8f, 0.3f};
  //float[] times = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24};
  float[] times = {00.12f, 6.02f, 12.13f, 16.26f, 22.30f};

  float p0_x, p0_y, p1_x, p1_y, p2_x, p2_y, p3_x, p3_y, p4_x, p4_y, p5_x, p5_y, p6_x, p6_y, p7_x, p7_y;
  PVector[] textPosition = new PVector[8];
  PVector titlePosition, todayPosition, tomorrowPosition;

  TideDisplay(int x, int y, int fW, int fH) {
    cx = x;
    cy = y;
    y_axis = PApplet.parseInt(cy/2);
    diam = PApplet.parseInt(cx * 1.2f);
    f_width = fW;
    f_height = fH;
    println("frame: " + frame);   
    tideInfoFrame = createGraphics(f_width, f_height);
    thisFontSize = f_width/4;
    // println("thisFontSize: " + thisFontSize);

    for (int i = 0; i < tides.length; i++) {
      tides[i] = new Tide();
      print("tides[" + i + "]: "); 
      println(tides[i].time + " " + tides[i].meters + " " + tides[i].isHigh);
    } 

    titlePosition = new PVector((f_width * 0.07f), (f_height * 0.07f));// = new PVector((f_width/2), (f_height/8));
    todayPosition = new PVector((f_width/2), (f_height/5*1));
    textPosition[0] = new PVector((f_width/3)-10, (f_height/5)*1.9f);
    textPosition[1] = new PVector((f_width/3)*2+10, (f_height/5)*1.9f);
    textPosition[2] = new PVector((f_width/3)-10, (f_height/5)*2.4f);
    textPosition[3] = new PVector((f_width/3)*2+10, (f_height/5)*2.4f);
    tomorrowPosition = new PVector((f_width/2), (f_height/5)*3);
    textPosition[4] = new PVector((f_width/3)-10, (f_height/5)*3.9f);
    textPosition[5] = new PVector((f_width/3)*2+10, (f_height/5)*3.9f);
    textPosition[6] = new PVector((f_width/3)-10, (f_height/5)*4.4f);
    textPosition[7] = new PVector((f_width/3)*2+10, (f_height/5)*4.4f);
    //print("textPosition[0]: ");
    //println(textPosition[0].x + ", " + textPosition[0].y);

    loadTideTable(); //getTodaysRow();
  }


  public void loadTideTable() {
    try {
      String tideTable = "Whakatane "+ year() + ".csv"; // "Whakatane 2016.csv"
      table = loadTable(tideTable, "csv");      
      getTodaysRow();
      println("loadTable success");
    }
    catch (Exception e) // MalformedURL, IO
    {
      println("Error in loadTideTable");
      e.printStackTrace();
    }
    setupText();
  }

  public void getTodaysRow() {    
    //lastTide = tides[3]; // Yesterday's forthTide is saved before it gets updated with the current date
    boolean threeTideDay = false;

    try {
      int i = 0;
      for (TableRow row : table.rows()) {
        i++;
        int day_ = row.getInt(0); // A
        int month_ = row.getInt(2); // C
        //String firstTide = row.getString(E);
        //  println(day() + " / " + month()); 
        // println(day_ + " / " + month_); 

        // Search for the row at current date/time:
        if (month_ == month()) { 
          println("month yes");
          if (day_ == day()) {
            println("day yes");
            println(day_ + " / " + month_); 
            todaysRowID = i;
            println("Row ID " + i);

            tides[0].time = row.getString(4); // E
            tides[0].meters = row.getFloat(5); // F
            tides[1].time = row.getString(6); // G
            tides[1].meters = row.getFloat(7); // H
            tides[2].time = row.getString(8); // I
            tides[2].meters = row.getFloat(9); // J
            if (null != row.getString(10)) {              
              tides[3].time = row.getString(10); // K
              tides[3].meters = row.getFloat(11); // L
            } else {
              threeTideDay = true;
            }
            break; // break out of the for loop after correct month and day are found
          }
        }
      }      
      // Yesterday - needed?
      //TableRow yesterday = row(todaysRowID - 1);

      // Check to see if it's the 31st of December
      if (day() == 31 && month() == 12) {
        println("shieeet x2"); //  get the 1st of the 1st for the following year
        String tideTable = "Whakatane "+ (year() + 1) + ".csv"; // "Whakatane 2016.csv"
        Table table2 = loadTable(tideTable, "csv");
        for (TableRow row2 : table2.rows()) {      
          int day_ = row2.getInt(0); // A
          int month_ = row2.getInt(2); // C
          if (month_ == 1) { 
            if (day_ == 1) {
              tides[4].time = row2.getString(4); // E
              tides[4].meters = row2.getFloat(5); // F
              tides[5].time = row2.getString(6); // G
              tides[5].meters = row2.getFloat(7); // H
              tides[6].time = row2.getString(8);
              tides[6].meters = row2.getFloat(9);
              if (null != row2.getString(10)) {
                tides[7].time = row2.getString(10);
                tides[7].meters = row2.getFloat(11);
              }
            }
          }
        }
      } else {
        // TableRow tomorRow = table.getRow(todaysRowID + 1);
        int tomorRowID = todaysRowID; // todaysRowID + 1; This was giving the day after tomorrow        
        if (threeTideDay) {
          //tides[3].time = tomorRow.getString(4); // E
          //tides[3].meters = tomorRow.getFloat(5); // F
          tides[3].time = table.getString(tomorRowID, 4); // E
          tides[3].meters = table.getFloat(tomorRowID, 5); // F
          tides[4].time = table.getString(tomorRowID, 6); // G
          tides[4].meters = table.getFloat(tomorRowID, 7); // H
          tides[5].time = table.getString(tomorRowID, 8); // I
          tides[5].meters = table.getFloat(tomorRowID, 9); // J
          tides[6].time = table.getString(tomorRowID, 8);
          tides[6].meters = table.getFloat(tomorRowID, 9);
          /* not required
           if (null != tomorRow.getString(10)) {
           tides[7].time = tomorRow.getString(10);
           tides[7].meters = tomorRow.getFloat(11);
           }*/
        } else {          
          tides[4].time = table.getString(tomorRowID, 4); // E
          tides[4].meters = table.getFloat(tomorRowID, 5); // F
          tides[5].time = table.getString(tomorRowID, 6); // G
          tides[5].meters = table.getFloat(tomorRowID, 7); // H
          tides[6].time = table.getString(tomorRowID, 8); // I
          tides[6].meters = table.getFloat(tomorRowID, 9); // J
          if (null != table.getString(tomorRowID, 10)) {            
            tides[7].time = table.getString(tomorRowID, 10);
            tides[7].meters = table.getFloat(tomorRowID, 11);
          }
        }
      }
      println("fuck yes try");
    }
    catch (Exception e) // MalformedURL, IO
    {
      println("Error in getTodaysRow");
      e.printStackTrace();
    }
  }



  public void display() {
    image(tideInfoFrame, cx, cy);
  }


  public void setupText() {
    tideInfoFrame.beginDraw();
    tideInfoFrame.background(primaryLightColour);    
    tideInfoFrame.fill(primaryDarkColour, 120); 
    tideInfoFrame.textAlign(LEFT, CENTER);    
    tideInfoFrame.textSize(h4);
    tideInfoFrame.text("tides", titlePosition.x, titlePosition.y);
    tideInfoFrame.fill(primaryDarkColour); 
    tideInfoFrame.textAlign(CENTER);
    tideInfoFrame.textSize(h6);
    tideInfoFrame.text("Today", todayPosition.x, todayPosition.y);
    tideInfoFrame.text("Tomorrow", tomorrowPosition.x, tomorrowPosition.y);
    if (tides[0].meters > tides[1].meters) {
      tideInfoFrame.text("High                     Low", todayPosition.x, todayPosition.y+15);
    } else {
      tideInfoFrame.text(" Low                     High", todayPosition.x, todayPosition.y+15);
    }
    if (tides[4].meters > tides[5].meters) {
      tideInfoFrame.text("High                     Low", tomorrowPosition.x, tomorrowPosition.y+15);
    } else {       
      tideInfoFrame.text(" Low                     High", tomorrowPosition.x, tomorrowPosition.y+15);
    }
    tideInfoFrame.textSize(h4);

    for (int i = 0; i < tides.length; i++) {
      String temp;
      if (i % 2 == 0) {
        temp = tides[i].time;
      } else { 
        temp = tides[i].time;
      }
      print(i + ": ");
      println(temp);
      //if (tides[i].isHigh) {
      //  tideInfoFrame.fill(highColour);
      //} else {
      //  tideInfoFrame.fill(primaryDarkColour);
      //}   
      //print("textPosition[" + i + "]: ");
      //float tempX = textPosition[i].x;
      //float tempY = textPosition[i].y;
      //println(tempX + ", " + tempY);
      tideInfoFrame.text(temp, textPosition[i].x, textPosition[i].y);
    }

    //tideInfoFrame.stroke(highlighterPINK); // testing
    //tideInfoFrame.strokeWeight(4);
    //tideInfoFrame.fill(highlighterPINK);
    //tideInfoFrame.rect(0, 0, 20, 20);

    tideInfoFrame.endDraw();

    image(tideInfoFrame, cx, cy);
  }

  // END
}

class WindDisplay {
  // String windHeading = ""; // Either "NE"; "SE"; E"; "NW"; "N"; "SW"; "S"; "W";
  int windAngle; // This might be an easier way to bring the data in and put it straight into use
  //String windS = "4.5";
  float speedkph;

  int cx, cy; // Centre
  int diam;
  int textCenX, textCenY; // Text Centre

  PVector center, windPos;
  PVector position1, position2, position3;
  float windRadians;
  float triWidth;
  float triRadius;
  int textOffsetX = -1;
  int textOffsetY = 6;
  int f_width; // Use this to scale everything to
  int f_height;
  int thisFontSize;
  int gustX, gustY;

  PGraphics windFrame;

  WindDisplay(int x, int y, int fW, int fH) {
    cx = x;
    cy = y;    
    f_width = fW;
    f_height = fH;   // println("frame: " + frame);
    windFrame = createGraphics(f_width, f_height);
    diam = PApplet.parseInt((fW*0.5f) * 1.4f); // int(cX * 1.35);
    triRadius = (fW*0.5f) * 0.196f; // cX * 0.196;
    windRadians = radians(windAngle);
    windPos = PVector.fromAngle(windRadians);
    windPos.normalize();
    windPos.mult(diam*0.5f);    
    textCenX = PApplet.parseInt(f_width * 0.63f); // int(cX * 0.63);
    textCenY = PApplet.parseInt(f_height * 0.73f); // int(cY * 0.59);
    println(textCenY);
    thisFontSize = f_width/4;
    println("thisFontSize: " + thisFontSize);
    gustX = 0;
    gustY = 0;
  }




  public void display(int windDirection, float windSpeed, int gustDirection, float gustSpeed) { 
    // Convert wind data into useable display format
    // ------------------------------------------------
    String heading = getWindString(windDirection); // e.g "N", "W", "SE" etc
    //windAngle = getWindAngle(heading);    
    //windRadians = radians(windAngle);
    windRadians = getWindRadians(windDirection); //windRadians = radians(windDirection);
    windPos = PVector.fromAngle(windRadians);
    windPos.normalize();
    windPos.mult(diam*0.5f);
    speedkph = windSpeed; // Update local float, (Used in other methods)
    //String speed = str(windSpeed); // String is used for displaying
    //String speed = nf(int(speedkph), 1);
    // ------------------------------------------------
    String gustText = "Gust " + getWindString(gustDirection) + " " + PApplet.parseInt(gustSpeed) + "km/h";

    // Draw and position lines -------------------------------------
    windFrame.beginDraw();
    windFrame.background(primaryLightColour);    
    windFrame.pushMatrix();
    windFrame.translate(cx, cy);
    windFrame.rotate(HALF_PI);
    windFrame.rotate(windRadians);
    drawLines();
    windFrame.popMatrix();
    windFrame.endDraw(); 
    image(windFrame, cx, cy);

    // Draw faint circle -------------------------------------
    fill(primaryLightColour, 180); //bkColor  fill(themeGreen2, 80); // 
    // noStroke();
    stroke(200);
    strokeWeight(1.3f);
    ellipse(cx, cy, diam, diam );

    // Draw Wind SPEED Circle first -------------------------------------
    noFill();    
    stroke(lightpurple);
    strokeWeight(3);
    ellipse(cx, cy, diam, diam );


    // Draw Wind Triangle -------------------------------------

    pushMatrix(); //windAngle
    pushStyle();
    translate(cx, cy);

    rotate(-HALF_PI); // Processing starts 45 degrees around, this offsets it.
    //stroke(200, 10, 12);
    strokeWeight(1.3f);
    // line(0, 0, windPos.x, windPos.y);
    //pushMatrix();
    // Draw Tri
    translate(windPos.x, windPos.y);

    rotate(windRadians); // to corispond with wind angle
    simpleTri();
    // popMatrix();
    //windTri();
    popStyle();

    // Draw Wind Direction text ---------------------------------
    pushStyle();
    // pushMatrix();
    // translate(windPos.x, windPos.y);
    rotate(-windRadians); // To counter act on triangles needs
    rotate(HALF_PI); // To counter act the offset
    // println(getWindDirection(windAngle));
    fill(primaryLightColour); // bkColor
    textAlign(CENTER);
    textFont(winddirFont);
    textSize(h6); // textSize(thisFontSize/4); // textSize(20); 
    text(heading, textOffsetX, textOffsetY); // eg "NE"
    popStyle();
    popMatrix();

    // Draw Speed on top ---------------------------------
    /* pushStyle();
     fill(0);
     textSize(thisFontSize);
     textAlign(CENTER, CENTER); //RIGHT
     if (speed.length() > 1) text(speed, textCenX, textCenY);
     else text(speed, textCenX-(textCenX*0.08), textCenY);
     
     textSize(thisFontSize/3); // /6   14
     fill(255);
     textAlign(LEFT);
     if (speed.length() > 1) text("km/h", textCenX, textCenY);
     else text("km/h", textCenX-(textCenX*0.08), textCenY);
     popStyle(); */

    pushStyle();
    // fill(getSpeedBrightness());
    fill(c1); // bluegrey
    textSize(h1); // textSize(98);
    textAlign(RIGHT); // textAlign(CENTER, CENTER); //
    //windSpeed = nf(int(windSpeedKph), 1);


    String speed = nf(PApplet.parseInt(speedkph), 1);
    if (speed.length() > 1 && speedkph != 0) { // if the speed is 10kph or greater then format without a decimal place
      text(speed, textCenX, textCenY);
      textFont(winddirFont);
      textSize(h6); // textSize(14); thisFontSize/3
      textAlign(LEFT);
      text("km/h", textCenX, textCenY);
    } else if (PApplet.parseInt(speedkph) != 0) {
      // windSpeed = nf(int(windSpeedKph), 1, 1);
      speed = nf((speedkph), 1, 1);  // Formats the speed to have a decimal place when it's under 10kph
      text(speed, textCenX+(textCenX*0.1f), textCenY); //text(speed, textCenX+(textCenX*0.1), textCenY);
      textFont(winddirFont);
      textSize(h5); // textSize(14); thisFontSize/3
      textAlign(LEFT);
      text("km/h", textCenX+(textCenX*0.08f), textCenY);
    } else {
      text(speed, textCenX-(textCenX*0.065f), textCenY);
      textFont(winddirFont);
      textSize(h6); // textSize(14); thisFontSize/3
      textAlign(LEFT);
      text("km/h", textCenX-(textCenX*0.05f), textCenY);
    }
    popStyle();

    // Draw Gust info ---------------------------------
    pushMatrix();
    translate(cx, cy);    
    textAlign(LEFT, TOP);
    textSize(h6); 
    text(gustText, -f_width*0.49f, -f_height*0.49f);
    popMatrix();

    // Draw frame -------------------------------------    
    noFill();
    stroke(frameBoxColour);
    strokeWeight(1.3f);
    rectMode(CENTER);
   // rect(cx, cy, f_width, f_height);
   
    /*pushStyle();
    fill(highlighterPINK);
    rectMode(CENTER);
    rect(cx, cy, f_width, f_height);    
    popStyle(); */
  }



  public void simpleTri() {
    pushMatrix();
    rotate(PI); // So that it points inwards. PI = 180 degree turn
    float a1 = radians(0);
    position1 = PVector.fromAngle(a1);
    position1.normalize();
    position1.mult(triRadius);
    float a2 = radians(120);
    position2 = PVector.fromAngle(a2);
    position2.normalize();
    position2.mult(triRadius);
    float a3 = radians(240);
    position3 = PVector.fromAngle(a3);
    position3.normalize();
    position3.mult(triRadius);

    stroke(primaryLightColour); // bkColor Used to 'White out' part of the circle
    strokeWeight(13);
    triangle(position2.x, position2.y, position3.x, position3.y, position1.x, position1.y);
    stroke(themeGreen2); // bluegrey
    strokeWeight(1.3f);
    fill(themeGreen2); // bluegrey, Black
    triangle(position2.x, position2.y, position3.x, position3.y, position1.x, position1.y);

    popMatrix();
  }


  public void drawLines() {
    float rads = map(speedkph, 0, 20, 0, 1.55f); // Convert Speed into radians
    //  print(rads + " : ");
    float a =  sin(rads);
    // print(a + " : ");
    float lineSpacing = map(a, 0, 1, f_width*0.52f, 5); 
    // println(lineSpacing);


    //windFrame.stroke(bluegrey, (map(speedkph, 0, 66, 15, 60))); // lightpurple
    windFrame.stroke(bluegrey, (map(speedkph, 0, 66, 15, 80))); // lightpurple
    windFrame.strokeWeight(1);

    int j = 0;
    for (int i = 0; i > -f_width; i -= lineSpacing) {
      windFrame.line(-f_width, i, f_width, i);
      if (j != 0) windFrame.line(-f_width, j, f_width, j);  // Don't draw the first j line because the first ones are in the same spot and it bleeds.
      j+=lineSpacing;
    }
  }



  public int getSpeedBrightness() {
    return PApplet.parseInt(map(speedkph, 0, 66, 0, 300));
  }

  public String getWindString(int wind) {
    String windString = "";
    if (wind <= 5) windString = "N"; // 0
    else if (wind <= 50) windString = "NE"; // 45
    else if (wind <= 95) windString = "E";  // 90
    else if (wind <= 140) windString = "SE"; // 135    
    else if (wind <= 185) windString = "S";  // 180
    else if (wind <= 230) windString = "SW"; // 225
    else if (wind <= 275) windString = "W"; // 270
    else if (wind <= 320) windString = "NW"; // 315
    else if (wind <= 365) windString = "N"; // 360
    return windString;
  }
  public float getWindRadians(int wind) {
    float windRads = 0;
    if (wind <= 5) windRads = TWO_PI; // "N"; // 0
    else if (wind <= 50) windRads = QUARTER_PI; // "NE"; // 45
    else if (wind <= 95) windRads = HALF_PI; // "E";  // 90
    else if (wind <= 140) windRads = 3*QUARTER_PI; // "SE"; // 135    
    else if (wind <= 185) windRads = PI; // "S";  // 180
    else if (wind <= 230) windRads = 5*QUARTER_PI; // "SW"; // 225
    else if (wind <= 275) windRads = 6*QUARTER_PI; // "W"; // 270
    else if (wind <= 320) windRads = 7*QUARTER_PI; // "NW"; // 315
    else if (wind <= 365) windRads = 8*QUARTER_PI; // "N"; // 360 or 0 or TWO_PI
    return windRads;
  }
}


// ---------------------- FONT/TEXT
PFont numFont, clockFont, winddirFont, onlineFont;
int h0, h1, h2, h3, h4, h5, h6, h7; // Preset font sizes
int dateSize = 14;
int clockSize = 17;
// ----------------------
/*
// ---------------------- COLOUR FIXED
color lineColour = #19CB67;
color mainAccentColour = #710EC9;
color LightAccentColour = #C1F6FF; 
color primaryColour = #D9D7E3; // #E5E3ED; int backgroundTint = 240;
color primaryLightColour = #F7F5FF; //int clockTint = 230;
color primaryDarkColour = #7D7C81;

// ---------------------- COLOUR
int clockTint = 230;
color highColour, lowColour, highColour2, lowColour2;
color frameBoxColour = primaryColour;
color themeGreen;
color themeGreen2 = #749B81;
color bkColor;
color brown, lightbrown, bluegrey, lightpurple;
// ---------------------- */
// ---------------------- COLOUR INVERT
int c1, c2, c3; // highlighted text from brightest to least.
int lineColour = 0xff19CB67;
int mainAccentColour = 0xff710EC9;
int LightAccentColour = 0xffC1F6FF; 
int primaryColour = 0xffD9D7E3; // #E5E3ED; int backgroundTint = 240;
int primaryLightColour = 0xff171717; // = #F7F5FF; //int clockTint = 230;
int offlineColor = 0xff2E2E2E;
int primaryDarkColour = 0xff7D7C81;
int clockTint = 230;
int highColour, lowColour, highColour2, lowColour2;
int frameBoxColour = primaryColour;
int themeGreen;
int themeGreen2 = 0xff749B81;
int bkColor;
int brown, lightbrown, bluegrey, lightpurple;
int highlighterPINK = 0xffE53ADD;
int highlighterGREEN = 0xff23E54E;
// ----------------------



public void styleSetup() {
  smooth();   
  noStroke();
  fill(0);
  imageMode(CENTER);
  colorMode(HSB, 360, 100, 100);
  highColour = color(6, 45, 50);
  lowColour = color(139, 24, 30);
  //lowColour = color(themeGreen2);
  highColour2 = color(hue(highColour), saturation(highColour), 90);
  lowColour2 = color(hue(lowColour), saturation(lowColour), 80);
  themeGreen = 0xff15FF5C; // #15FF84; // #33DB82; // #75BF83; // color(131, 20, 53);
  bkColor = color(350, 8, 2); // = color(0, 0, 100);
  brown = color(26, 60, 62);
  lightbrown = color(35, 32, 93);
  bluegrey = color(244, 10, 81); // color(244, 10, 56);
  lightpurple = color(260, 4, 84);
  c1 = color(350, 8, 57); //kitchen drawers    //#E7E5FF;
  c2 = 0xffD0CEE5;
  c2 = 0xffB9B7CB;
  h0 = PApplet.parseInt(width/7.5f);
  h1 = PApplet.parseInt(width/11.2f); // width/11; // 800/10 = 85;
  h2 = width/14;
  h3 = width/25; // 800/25 = 32;
  h4 = width/40;
  h5 = width/53; // 800/50 = 16;
  h6 = width/62; // 800/50 = 16;
  h7 = width/71;
  
  numFont = createFont("MicrosoftYaHeiUILight-120", 80);
  clockFont = createFont("MicrosoftYaHeiUILight-120", 320);
  winddirFont = createFont("Dialog.bold-20", 16);
  onlineFont = createFont("Roboto-Bold-48", 16);
  textFont(numFont); // Set as inital/default
  
  //fill(c2);
  //textSize(h2);
}
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "WeatherDroid_2_1" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
