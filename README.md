# Weather Master



### An Arduino and Raspberry Pi weather station and display.

![Raspberry Pi Display](Misc/pi_screen.png)




I converted a Weather Station to fit a Arduino Pro Mini, which collects the data from its sensors around the clock. The data is then sent via serial to an Adafruit Huzzah ESP8266 Breakout board. This board cycles through different modes, first collecting the information from the Arduino Pro Mini then sending the data as a post message to a Raspberry Pi server. The server has a basic website to present the information, however it's main task is to present the data on a 7" Display. I used Proceesing to make the display software.








