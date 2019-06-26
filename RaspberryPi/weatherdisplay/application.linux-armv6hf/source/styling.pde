

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
color c1, c2, c3; // highlighted text from brightest to least.
color lineColour = #19CB67;
color mainAccentColour = #710EC9;
color LightAccentColour = #C1F6FF; 
color primaryColour = #D9D7E3; // #E5E3ED; int backgroundTint = 240;
color primaryLightColour = #171717; // = #F7F5FF; //int clockTint = 230;
color offlineColor = #2E2E2E;
color primaryDarkColour = #7D7C81;
int clockTint = 230;
color highColour, lowColour, highColour2, lowColour2;
color frameBoxColour = primaryColour;
color themeGreen;
color themeGreen2 = #749B81;
color bkColor;
color brown, lightbrown, bluegrey, lightpurple;
color highlighterPINK = #E53ADD;
color highlighterGREEN = #23E54E;
// ----------------------



void styleSetup() {
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
  themeGreen = #15FF5C; // #15FF84; // #33DB82; // #75BF83; // color(131, 20, 53);
  bkColor = color(350, 8, 2); // = color(0, 0, 100);
  brown = color(26, 60, 62);
  lightbrown = color(35, 32, 93);
  bluegrey = color(244, 10, 81); // color(244, 10, 56);
  lightpurple = color(260, 4, 84);
  c1 = color(350, 8, 57); //kitchen drawers    //#E7E5FF;
  c2 = #D0CEE5;
  c2 = #B9B7CB;
  h0 = int(width/7.5);
  h1 = int(width/11.2); // width/11; // 800/10 = 85;
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