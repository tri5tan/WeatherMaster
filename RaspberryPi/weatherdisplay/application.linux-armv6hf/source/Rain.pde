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
    title_x = int(f_width * 0.5);
    title_y = int(f_height * 0.05); // int(f_height * 0.25) f_height - 5
    title2_x = int(f_width * 0.5);
    title2_y = int(f_height * 0.53); 
    text_cx = 80; // 58
    text_cy = int(f_height * 0.45); // 0.65 108
    rainFrame = createGraphics(f_width, f_height);
    rainFrame_invert = createGraphics(f_width, f_height);
  }


  void display() {
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


  void update(float rainFloat, float rainFiveFloat) {
    //rainFloat = 10; // for format testing
  //  rainFiveFloat = 7;
    String rainString = nf(int(rainFloat), 1);

    if (rainString.length() > 1) { // if rain value is greater than 1 digit and doesn't equal zero
      day_current = rainString;
    } else if (rainString.length() == 1 && rainFloat != 0) { // (int(rainFloat) != 0)   
      day_current = nf(rainFloat, 1, 1); // Formats the string to have 1 decimal place
    } else {
      day_current = rainString;
    }

    fiveday = nf(int(rainFiveFloat), 1);
  }
}