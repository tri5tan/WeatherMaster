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


  void display() {
    pressureFrame.beginDraw();
    pressureFrame.background(primaryLightColour);
    pressureFrame.pushMatrix();
    pressureFrame.translate(58.5, 113);
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

  void update(int p) {
    //pressure_ = p;  // raw input is in pascals
   // int hectopascals = int(p/100);
    // hPascals = str(hectopascals);
    hPascals = str(p); 

    inc = int(map(p, 980, 1015, inc_min, inc_max)); // replace 850 and 1100 with actual recorded low and high
    // println("inc " + inc);
    circles = int(map(inc, inc_min, inc_max, circles_max, circles_min));
    // println("circles " + circles);
  }

  void drawCircles() {   
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