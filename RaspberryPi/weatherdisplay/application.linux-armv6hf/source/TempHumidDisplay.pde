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
    tx_1 = int(f_width * 0.98); // Current Temp
    ty_1 = int(f_height * 0.33);
    tx_1b = int(f_width * 0.53); // Current Temp Decimal
    ty_1b = int(f_height * 0.455);
    tx_2 = int(f_width * 0.9); // High Temp
    ty_2 = int(f_height * 0.65);
    tx_3 = int(f_width * 0.5); // Low Temp
    ty_3 = int(f_height * 0.65);
    lx_1 = int(f_width * 0.07); 
    ly_1 = int(f_height * 0.65);
    hx_1 = int(f_width * 0.07); // int(f_width * 0.1666); // Current Humi
    hy_1 = int(f_height * 0.68);
    hx_2 = int(f_width * 0.58); // percentage
    hy_2 = int(f_height * 0.64);
    hx_3 = int(f_width * 0.8333); // Low Humi
    hy_3 = int(f_height * 0.8333);
    location_x = int(f_width * 0.07);
    location_y = int(f_height * 0.07);
  }

  void update(String t, int h) {
    temp = t;
    humi = h;
    humi_String = nf(h, 2);
  }
  void update(String t, String h) {
    temp = t;
    humi_String = h;
  }
  void update(String whole, String decimal, int h) {
    temp_whole = whole;
    temp_decimal = decimal;
    humi = h;
    humi_String = nf(h, 2);
    println(humi_String);
  }


  void display() {   
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
      // tempHumiFrame.text(temp_whole + "°", tx_1, ty_1); // This is the int version without rounding.
      tempHumiFrame.text(temp + "°", tx_1, ty_1); 
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
      tempHumiFrame.text("20°", tx_2, ty_2);
      tempHumiFrame.fill(lowColour2);
      tempHumiFrame.text("11°", tx_3, ty_3); */
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
      // tempHumiFrame.text(temp_whole + "°", tx_1, ty_1);
      tempHumiFrame.text(tempC_int + "°", tx_1, ty_1);
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
      tempHumiFrame.text("20°", tx_2, ty_2); 
      tempHumiFrame.fill(lowColour);
      tempHumiFrame.text("11°", tx_3, ty_3);   */  
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
    strokeWeight(1.3);
    rectMode(CENTER);
    // rect(cx, cy, f_width, f_height);
  }
}