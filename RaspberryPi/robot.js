/* -----------------------------------------------------------------------
 * Programmer:   Cody Hazelwood
 * Date:         April 18, 2013
 * Platform:     Node.JS
 * Description:  
 * Dependencies: Node.JS
 *               WebSocket-Node
 * -----------------------------------------------------------------------
 * Developed by the MTSU Raider Robotics Team for the NASA Lunabotics
 * Competition 2013.
 *
 * Middle Tennessee State University
 * -----------------------------------------------------------------------
 */
 
//Environment Setting
var webSocketPort        = 5001;
process.title            = 'Robot-Server';
var serialPortArduino    = '/dev/ttyACM0';  //RaspberryPi Serial Port
var serialPortSabretooth =
//var serialPort     = '/dev/tty.usbmodem1411';  //Mac Serial Port
var baudrate             = 115200;

//Requires
var req_wsServer   = require('websocket').server;
var req_httpServer = require('http');
var req_serial     = require('serialport');

//Global Variables
var clients   = []; //Array of connected clients

var Message = {
	DRIVE_STOP		: "00",
	DRIVE_FORWARD	: "01",
	DRIVE_BACKWARD	: "02",
	DRIVE_LEFT		: "03",
	DRIVE_RIGHT		: "04",
	ACTUATOR_C_UP	: "05",
	ACTUATOR_C_DOWN	: "06",
	ACTUATOR_C_STOP	: "07",
	ACTUATOR_R_UP   : "08",
	ACTUATOR_R_DOWN : "09",
	ACTUATOR_R_STOP : "10",
	BRUSH_AT_FULL	: "11",
	BRUSH_AT_75		: "12",
	BRUSH_AT_50		: "13",
	BRUSH_AT_25		: "14",
	BRUSH_STOP		: "15",
	OPEN_BIN		: "16",
	CLOSE_BIN		: "17",
	DIG				: "18",
	DIG_STOP		: "19",
	DUMP			: "20",
	AUTONOMY_ON     : "21",
	AUTONOMY_OFF    : "22",
	GLOBAL_STOP		: "23"
};

/* 
 *  Begin Application 
 */

console.log('MTSU Lunabotics Server');
console.log('--------------------------\n');


//Initialize the Sabretooth Serial Port

log('Initializing Sabretooth Serial Port...');

var serial_sabretooth = new req_serial.SerialPort(serialPortSabretooth, {
    baudrate: baudrate,
    parser:   req_serial.parsers.readline("\n")
});


//Once the Sabretooth Serial connection is open,
//start the Arduino Serial connection

serial_sabretooth.on('open', function() {

	log('Initializing Arduino Serial Port...');
	
	var serial_arduino = new req_serial.SerialPort(serialPortArduino, {
		baudrate: baudrate,
		parser:   req_serial.parsers.readline("\n")
	});
});


//Once both serial connections are open, start the network server

serial_arduino.on('open', function() {

    //Start the HTTP Server
    log('Initializing HTTP Server...');
    
    var server = req_httpServer.createServer(function(request, response) { });

    server.listen(webSocketPort, function() {
        log('Now listening on port ' + webSocketPort);
    });

	//Start the WebSocket Server
    log('Initializing WebSocket Server...');

    var socketServer = new req_wsServer({
        httpServer: server,
        autoAcceptConnections: false
    });

    //When the Arduino sends us data, relay it to all connected clients
    serial_arduino.on('data', function(data) {
        log('Received Data: ' + data);
        for (var i = 0; i < clients.length; i++) {
            clients[i].sendUTF(data);
        }
    });

    //Function is called when a network client connects
    socketServer.on('request', function(request) {

        //Accept Connection
        var connection   = request.accept(null, request.origin);    
        var clientNumber = clients.push(connection) - 1;

        log('Connection accepted.');

        log('Client connected');
        log('Current number of clients: ' + (clients.length));

        //User Disconnects
        connection.on('close', function(connection) {
            log('Client disconnected.');
            clients.splice(clientNumber, 1);
            log('Client disconnected');
            log('Current number of clients: ' + (clients.length));
            
            //If there are no control clients connected, stop moving
            if (clients.length < 1) {
                globalStop();
            }
        });
    
        //WebSocket Message Received
        connection.on('message', function(message) {
			switch (message.utf8Data) {
			case Message.DRIVE_STOP:
				driveStop();
				break;
			case Message.DRIVE_FORWARD:
				driveForward();
				break;
			}
        });
    });
});

/*
function serialWrite(msg) {
    serial.write(msg , function(err, results) {
        log('Sending message:  ' + msg);
        log('   Write error:   ' + err);
        log('   Write results: ' + results);
    });
}*/

function log(msg) {
    console.log((new Date()).toLocaleTimeString() + ': ' + msg);
}

/*
 *    Sabretooth Code
 */

function globalStop() {
	driveStop();
	actuatorCenterStop();
	actuatorRearStop();
	brushAt(0);
}

function driveStop() {
	log("Drive Stop");
	
}

function driveForward() {
	log("Drive Forward");
	
}

function driveBackward() {
	log("Drive Backward");

}

function driveLeft() {
	log("Drive Left");

}

function driveRight() {
	log("Drive Right");

}

function actuatorCenterUp() {
	log("Actuator Center Up");
	
}

function actuatorCenterDown() {
	log("Actuator Center Down");
	
}

function actuatorCenterStop() {
	log("Actuator Center Stop");
	
}

function actuatorRearUp() {
	log("Actuator Rear Up");
	
}

function actuatorRearDown() {
	log("Actuator Rear Down");
	
}

function actuatorRearStop() {
	log("Actuator Rear Stop");
	
}

function brushAt(int percent) {
	log("Brush At " + percent + "%");

}

function openBin() {

}

function closeBin() {

}

function dig() {

}

function digStop() {

}

function dump() {

}
