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
    meters = 0.0;
    isHigh = false;
  }

  void update(float x_, float y_) {
    x = x_;
    y = y_;
  }
}