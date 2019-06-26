
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
    diam = int((fW*0.5) * 1.4); // int(cX * 1.35);
    triRadius = (fW*0.5) * 0.196; // cX * 0.196;
    windRadians = radians(windAngle);
    windPos = PVector.fromAngle(windRadians);
    windPos.normalize();
    windPos.mult(diam*0.5);    
    textCenX = int(f_width * 0.63); // int(cX * 0.63);
    textCenY = int(f_height * 0.73); // int(cY * 0.59);
    println(textCenY);
    thisFontSize = f_width/4;
    println("thisFontSize: " + thisFontSize);
    gustX = 0;
    gustY = 0;
  }




  void display(int windDirection, float windSpeed, int gustDirection, float gustSpeed) { 
    // Convert wind data into useable display format
    // ------------------------------------------------
    String heading = getWindString(windDirection); // e.g "N", "W", "SE" etc
    //windAngle = getWindAngle(heading);    
    //windRadians = radians(windAngle);
    windRadians = getWindRadians(windDirection); //windRadians = radians(windDirection);
    windPos = PVector.fromAngle(windRadians);
    windPos.normalize();
    windPos.mult(diam*0.5);
    speedkph = windSpeed; // Update local float, (Used in other methods)
    //String speed = str(windSpeed); // String is used for displaying
    //String speed = nf(int(speedkph), 1);
    // ------------------------------------------------
    String gustText = "Gust " + getWindString(gustDirection) + " " + int(gustSpeed) + "km/h";

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
    strokeWeight(1.3);
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
    strokeWeight(1.3);
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


    String speed = nf(int(speedkph), 1);
    if (speed.length() > 1 && speedkph != 0) { // if the speed is 10kph or greater then format without a decimal place
      text(speed, textCenX, textCenY);
      textFont(winddirFont);
      textSize(h6); // textSize(14); thisFontSize/3
      textAlign(LEFT);
      text("km/h", textCenX, textCenY);
    } else if (int(speedkph) != 0) {
      // windSpeed = nf(int(windSpeedKph), 1, 1);
      speed = nf((speedkph), 1, 1);  // Formats the speed to have a decimal place when it's under 10kph
      text(speed, textCenX+(textCenX*0.1), textCenY); //text(speed, textCenX+(textCenX*0.1), textCenY);
      textFont(winddirFont);
      textSize(h5); // textSize(14); thisFontSize/3
      textAlign(LEFT);
      text("km/h", textCenX+(textCenX*0.08), textCenY);
    } else {
      text(speed, textCenX-(textCenX*0.065), textCenY);
      textFont(winddirFont);
      textSize(h6); // textSize(14); thisFontSize/3
      textAlign(LEFT);
      text("km/h", textCenX-(textCenX*0.05), textCenY);
    }
    popStyle();

    // Draw Gust info ---------------------------------
    pushMatrix();
    translate(cx, cy);    
    textAlign(LEFT, TOP);
    textSize(h6); 
    text(gustText, -f_width*0.49, -f_height*0.49);
    popMatrix();

    // Draw frame -------------------------------------    
    noFill();
    stroke(frameBoxColour);
    strokeWeight(1.3);
    rectMode(CENTER);
   // rect(cx, cy, f_width, f_height);
   
    /*pushStyle();
    fill(highlighterPINK);
    rectMode(CENTER);
    rect(cx, cy, f_width, f_height);    
    popStyle(); */
  }



  void simpleTri() {
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
    strokeWeight(1.3);
    fill(themeGreen2); // bluegrey, Black
    triangle(position2.x, position2.y, position3.x, position3.y, position1.x, position1.y);

    popMatrix();
  }


  void drawLines() {
    float rads = map(speedkph, 0, 20, 0, 1.55); // Convert Speed into radians
    //  print(rads + " : ");
    float a =  sin(rads);
    // print(a + " : ");
    float lineSpacing = map(a, 0, 1, f_width*0.52, 5); 
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



  int getSpeedBrightness() {
    return int(map(speedkph, 0, 66, 0, 300));
  }

  String getWindString(int wind) {
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
  float getWindRadians(int wind) {
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