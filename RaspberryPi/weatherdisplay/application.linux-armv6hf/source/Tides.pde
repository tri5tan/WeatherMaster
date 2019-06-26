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
  float pointSize = 2.5;
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
  float[] heights = {0.3, 1.9, 0.4, 1.8, 0.3};
  //float[] times = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24};
  float[] times = {00.12, 6.02, 12.13, 16.26, 22.30};

  float p0_x, p0_y, p1_x, p1_y, p2_x, p2_y, p3_x, p3_y, p4_x, p4_y, p5_x, p5_y, p6_x, p6_y, p7_x, p7_y;
  PVector[] textPosition = new PVector[8];
  PVector titlePosition, todayPosition, tomorrowPosition;

  TideDisplay(int x, int y, int fW, int fH) {
    cx = x;
    cy = y;
    y_axis = int(cy/2);
    diam = int(cx * 1.2);
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

    titlePosition = new PVector((f_width * 0.07), (f_height * 0.07));// = new PVector((f_width/2), (f_height/8));
    todayPosition = new PVector((f_width/2), (f_height/5*1));
    textPosition[0] = new PVector((f_width/3)-10, (f_height/5)*1.9);
    textPosition[1] = new PVector((f_width/3)*2+10, (f_height/5)*1.9);
    textPosition[2] = new PVector((f_width/3)-10, (f_height/5)*2.4);
    textPosition[3] = new PVector((f_width/3)*2+10, (f_height/5)*2.4);
    tomorrowPosition = new PVector((f_width/2), (f_height/5)*3);
    textPosition[4] = new PVector((f_width/3)-10, (f_height/5)*3.9);
    textPosition[5] = new PVector((f_width/3)*2+10, (f_height/5)*3.9);
    textPosition[6] = new PVector((f_width/3)-10, (f_height/5)*4.4);
    textPosition[7] = new PVector((f_width/3)*2+10, (f_height/5)*4.4);
    //print("textPosition[0]: ");
    //println(textPosition[0].x + ", " + textPosition[0].y);

    loadTideTable(); //getTodaysRow();
  }


  void loadTideTable() {
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

  void getTodaysRow() {    
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



  void display() {
    image(tideInfoFrame, cx, cy);
  }


  void setupText() {
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