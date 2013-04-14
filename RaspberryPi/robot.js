/* -----------------------------------------------------------------------
 * Programmer:   Cody Hazelwood
 * Date:         November 17, 2012
 * Platform:     Node.JS
 * Description:  
 * Dependencies: Node.JS
 *               WebSocket-Node
 *               Johnny-Five
 * -----------------------------------------------------------------------
 * Copyright © 2013  Cody Hazelwood.
 *              
 * Research project funded by:
 *
 *        Undergraduate Research Experience and Creative Activity
 *                 Middle Tennessee State University
 *                    Mentor: Dr. Saleh Sbenaty
 * -----------------------------------------------------------------------
 */
 
//Environment Setting
var webSocketPort  = 5001;
process.title      = 'Robot-Server';
var serialPort     = '/dev/ttyACM0';  //RaspberryPi Serial Port
//var serialPort     = '/dev/tty.usbmodem1411';  //Mac Serial Port
var baudrate       = 115200;

//Requires
var req_wsServer   = require('websocket').server;
var req_httpServer = require('http');
var req_serial     = require('serialport');

//Global Variables
var clients   = []; //Array of connected clients

var Message = {
    STOP_ALL            : "1",
    LEDS_ON             : "2",
    LEDS_OFF            : "3",
    AUTONOMY_ON         : "4",
    AUTONOMY_OFF        : "5",
    DRIVE_LEFT          : "10",
    DRIVE_RIGHT         : "11",
    DRIVE_FORWARD       : "12",
    DRIVE_BACKWARD      : "13",
    DRIVE_FORWARD_LEFT  : "14",
    DRIVE_FORWARD_RIGHT : "15",
    DRIVE_STOP          : "16",
    CAMERA_LEFT         : "20",
    CAMERA_RIGHT        : "21",
    CAMERA_UP           : "22",
    CAMERA_DOWN         : "23",
    CAMERA_PAN_STOP     : "24",
    CAMERA_TILT_STOP    : "25",
    CAMERA_STOP         : "26",
    CAMERA_DIRECT       : "29"
};

/* 
 *  Begin Application 
 */

console.log('Robot Server');
console.log('--------------------------\n');

log('Initializing Serial Port...');
var serial = new req_serial.SerialPort(serialPort, {
    baudrate: baudrate,
    parser:   req_serial.parsers.readline("\n")
});

serial.on('open', function() {
    /*
     *  HTTP Server
     */

    log('Initializing HTTP Server...');

    //Create the server
    var server = req_httpServer.createServer(function(request, response) { });

    server.listen(webSocketPort, function() {
        log('Now listening on port ' + webSocketPort);
    });

    log('Initializing WebSocket Server...');

    //Create the WebSocket server
    var socketServer = new req_wsServer({
        httpServer: server,
        autoAcceptConnections: false
    });

    serial.on('data', function(data) {
        log('Received Data: ' + data);
        for (var i = 0; i < clients.length; i++) {
            clients[i].sendUTF(data);
        }
    });

    //Function is called when a user connects
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
        });
    
        //Message Received
        connection.on('message', function(message) {
            try {
                var json = JSON.parse(message.utf8Data);
                switch (json.comp) {
                case "stop":
                    serialWrite(Message.STOP_ALL);
                    break;
                case "drive":
                    switch (json.dir) {
                    case "forward":
                        serialWrite(Message.DRIVE_FORWARD);
                        break;
                    case "backward":
                        serialWrite(Message.DRIVE_BACKWARD);
                        break;
                    case "left":
                        serialWrite(Message.DRIVE_LEFT);
                        break;
                    case "right":
                        serialWrite(Message.DRIVE_RIGHT);
                        break;
                    case "forward_right":
                        serialWrite(Message.DRIVE_FORWARD_RIGHT);
                        break;
                    case "forward_left":
                        serialWrite(Message.DRIVE_FORWARD_LEFT);
                        break;
                    case "stop":
                        serialWrite(Message.DRIVE_STOP);
                        break;
                    }
                    break;
                case "camera":
                    switch (json.dir) {
                    case "up":
                        serialWrite(Message.CAMERA_UP);
                        break;
                    case "down":
                        serialWrite(Message.CAMERA_DOWN);
                        break;
                    case "left":
                        serialWrite(Message.CAMERA_LEFT);
                        break;
                    case "right":
                        serialWrite(Message.CAMERA_RIGHT);
                        break;
                    case "pan_stop":
                        serialWrite(Message.CAMERA_PAN_STOP);
                        break;
                    case "tilt_stop":
                        serialWrite(Message.CAMERA_TILT_STOP);
                        break;
                    case "stop":
                        serialWrite(Message.CAMERA_STOP);
                        break;
                    case "direct":
                        var msg = Message.CAMERA_DIRECT + "," + json.x + "," + json.y;
                        serialWrite(msg);
                        break;
                    }
                    break;
                case "leds":
                    if (json.s) {
                        serialWrite(Message.LEDS_ON);
                    } else {
                        serialWrite(Message.LEDS_OFF);
                    }
                    break;
                case "autonomy":
                    if (json.s) {
                        serialWrite(Message.AUTONOMY_ON);
                    } else {
                        serialWrite(Message.AUTONOMY_OFF);
                    }
                    break;
                }
            } catch (e) {
                log('Exception:');
                log(e);
                log(message.utf8Data);
            }
        });
    });
});

function serialWrite(msg) {
    serial.write(msg , function(err, results) {
        log('Sending message:  ' + msg);
        log('   Write error:   ' + err);
        log('   Write results: ' + results);
    });
}

function log(msg) {
    console.log((new Date()).toLocaleTimeString() + ': ' + msg);
}
