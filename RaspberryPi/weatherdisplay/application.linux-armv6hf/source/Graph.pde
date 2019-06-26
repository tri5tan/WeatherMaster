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
    graph_width = int(f_width * 1.3); //0.8  int(f_width * 0.9);
    graph_height = int(f_height * 1.2); //0.7 int(f_height * 0.8);
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


  void display() {

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
    translate(-170, cy*0.75);
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


  void update(float rainHour) {

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

  void drawAxis() {
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
    
    graphFrame.text("rainfall", f_width*0.2, f_height*0.05);
    graphFrame.fill(primaryDarkColour); 
    graphFrame.textAlign(CENTER, TOP);
    graphFrame.textSize(h6);
    graphFrame.text("Last 12 hours", f_width*0.5, f_height*0.9);
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