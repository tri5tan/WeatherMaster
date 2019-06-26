
var http = require('http')
var express = require('express')
var bodyParser = require('body-parser')
var jsonfile = require('jsonfile')
var fs = require('fs')
var timestamp = new Date();
var sensor = require('node-dht-sensor'); // For the inside sensor that's attached directly to the Raspberry Pi
var file = '/home/pi/weatherapp/data/weather.json';

// This variable is sent back to the ESP8266. Control of this here allows for easy testing.
var battMinimum = 3100; 

// For the inside sensor that's attached directly to the Raspberry Pi
var in_temperature;
var in_humidity;


// Store data over 5 days which the Processing display uses
var lastDay = 0;
var daily_rainmm;
var fivedays_rain = [0, 0, 0, 0, 0];
var fivedays_rainmm;


var isStart = 1;





function hourly() {
	daily_rainmm = parseFloat(daily_rainmm) + parseFloat(body["rainmm"]);
	fivedays_rain[0] = daily_rainmm;
	fivedays_rainmm = 0;
	for (var i = 0; i < (fivedays_rain.length); i++) {
      		fivedays_rainmm += fivedays_rain[i];
    	}
}
function daily() {
	// Shift fiveday_rain array
      for (i = (fivedays_rain.length - 1); i > 0; i--) {
        fivedays_rain[i] = fivedays_rain[i-1];        
      }
      fivedays_rain[0] = 0; // Reset day 0 for the new day
      daily_rainmm = 0;    // Reset the running daily rain guage
}

function timeCheck() {
	var date = new Date();
	var mins = date.getMinutes();
	var day = date.getDay();	
	if(mins == 0){
		hourly();
	}
	if(day != lastDay){
		lastDay = day;
		daily();
	}
}
setInterval(timeCheck, 29000);


function getInsideData(){
    sensor.read(22, 4, function(err, temperature, humidity) {
        if (!err) {
            //in_temperature = temperature.toFixed(1);
            in_temperature = temperature.toFixed(0);
            in_humidity = humidity.toFixed(0);
            console.log('temp: ' + in_temperature + '°C, ' +
                'humidity: ' + in_humidity + '%'
            );

            saveWeatherJSON()
        }
        });
}

setInterval(getInsideData, 30000); 


function getDayNight() {
  var date = new Date()
  var hour = date.getHours()
  var msg = 'received on day: ' + date.getDay() + '\n'
  msg += 'battMinimum: ' + battMinimum + '\n'
  if(hour > 7 && hour < 21) { // Between 7am and 9pm the ESP8266 should sleep for 2mins unless the battery is below the minimum
	// Day Time
	if(body_toSave["batteryVoltage"] < battMinimum) {
		msg += 'sleepTime_min: 15'
	} else {
		msg += 'sleepTime_min: 02'
	}
  }
  if(hour > 0 && hour < 7) { // Between midnight and 7am the ESP8266 should sleep for 30mins
	// Deep Night Time
	if(body_toSave["batteryVoltage"] < battMinimum) {
		msg += 'sleepTime_min: 90'
	} else {
		msg += 'sleepTime_min: 30'
	}
  }
  else { // Between 9pm and midnight the ESP8266 should sleep for 10mins
	// Night Time
	if(body_toSave["batteryVoltage"] < battMinimum) {
		msg += 'sleepTime_min: 360' // Sleep for 6 hours. If the battery is that flat it wont charge until the sun is out anyway, so let it sleep.
	} else {
		msg += 'sleepTime_min: 10'
	}
  }  
  return msg
}



function getWindString(wind){
	var windString = "";
	if (wind <= 23){
	 windString = "N"; // 0
	}
   	else if (wind <= 68){ 
	windString = "NE"; // 45
	}
	else if (wind <= 113){
	 windString = "E";  // 90
	}
	else if (wind <= 158){
	 windString = "SE"; // 135    
	}
	else if (wind <= 203){
	 windString = "S";  // 180
	}
	else if (wind <= 248){
	 windString = "SW"; // 225
	}
	else if (wind <= 293){
	 windString = "W"; // 270
	}
	else if (wind <= 338){
	 windString = "NW"; // 315
	}
	else if (wind <= 383){
	 windString = "N"; // 360
	}
	else {
	 windString = "NA";
	}
	return windString;
}


var app = express()

var body; // Request Body from POST
var body_toSave; // With Timestamp


app.use(express['static'](__dirname ));
app.use(bodyParser.json())



app.get('/test', function (req, res) {
  res.status(200).send('Hello Ball Bags!')
  console.log('Shit Happened')  // It's always fun to test things
})

app.get('/weatherdata/:id', function (req, res) {
  res.status(200).send(body_toSave);
  console.log('weatherdata accessed')
})


// Express route for incoming GET requests
app.get('/', function (req, res) {
  res.status(200).send(body);
  console.log('GET data(body) sent')
})


// Express route for incoming weather POST requests from ESP8266
app.post('/submitweather', function (req, res) {
  body = req.body
  //console.log('Post Request Body:')
  //console.log(body)
  body_toSave = body
  timestamp = new Date().toLocaleTimeString()
  timestamp += " " + new Date().toLocaleDateString()
  body_toSave["Saved"] = timestamp
  saveWeatherJSON()
  
  //timestamp = new Date()
  var returnMsg = getDayNight()
  res.status(202).send(returnMsg)
  console.log("Body saved as: ")
  console.log(body_toSave)
})

function saveWeatherJSON() {
  //startup();	
  body_toSave["tempC"] = Math.round(body["tempC"])
  if(body_toSave["windspdkph_avg2m"] > 10){
	body_toSave["windspdkph_avg2m"] = Math.round(body["windspdkph_avg2m"])
  }
  if(body_toSave["windgustkph_10m"] > 10){
	body_toSave["windgustkph_10m"] = Math.round(body["windgustkph_10m"])
  }
  body_toSave["windString"] = getWindString(body["winddir_avg2m"])
  body_toSave["insideTemp"] = in_temperature
  body_toSave["insideHumi"] = in_humidity  // body_toSave["insideHumi"] = Math.round(in_humidity)
  body_toSave["daily_rainmm"] = daily_rainmm.toFixed(0)
  body_toSave["fivedays_rainmm"] = fivedays_rainmm.toFixed(0)
  body_toSave["fivedays_rain"] = fivedays_rain
  jsonfile.writeFile(file, body_toSave, function (err) {
	  console.error(err)
  })
}






// Express route for any other unrecognised incoming requests
app.get('*', function(req, res) {
  res.status(404).send('Unrecognised API call')
})

// Express route to handle errors
app.use(function(err, req, res, next) {
  if (req.xhr) {
    res.status(500).send('Oops, Something went wrong!')
  } else {
    next(err)
  }
})

app.listen(5000, function () {
  console.log('Tasty Weather being Served on 5000')
})