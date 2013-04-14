/* -----------------------------------------------------------------------
 * Programmer:   Cody Hazelwood
 * Date:         March 13, 2013
 * Platform:     Arduino Uno
 * Description:  Robot Control Code
 * Dependencies: 
 * -----------------------------------------------------------------------
 * Copyright 2013 Cody Hazelwood.
 *              
 * Research project funded by:
 *
 *        Undergraduate Research Experience and Creative Activity
 *                 Middle Tennessee State University
 *                    Mentor: Dr. Saleh Sbenaty
 * -----------------------------------------------------------------------
 */
 
#include <i2cmaster.h>
#include <Servo.h>

//Environment
#define SERIAL_BAUD 115200

//Digital Pins
#define MOTOR_L      3
#define MOTOR_R      5
#define MOTOR_S_PAN  6
#define SENSOR_PING  7
#define MOTOR_C_PAN  9
#define MOTOR_C_TILT 10
#define LEDS         13

//Analog Pins
#define SENSOR_IR_L  2
#define SENSOR_IR_R  3
#define I2C_SDA      4
#define I2C_SCL      5

//Serial Messages
#define STOP_ALL          1
#define LEDS_ON           2
#define LEDS_OFF          3
#define AUTONOMY_ON       4
#define AUTONOMY_OFF      5
#define DRIVE_LEFT        10
#define DRIVE_RIGHT       11
#define DRIVE_FORWARD     12
#define DRIVE_BACKWARD    13
#define DRIVE_FRONT_LEFT  14
#define DRIVE_FRONT_RIGHT 15
#define DRIVE_STOP        16
#define CAMERA_LEFT       20
#define CAMERA_RIGHT      21
#define CAMERA_UP         22
#define CAMERA_DOWN       23
#define CAMERA_PAN_STOP   24
#define CAMERA_TILT_STOP  25
#define CAMERA_STOP       26
#define CAMERA_DIRECT     29
#define DEBUG             50
#define DEBUG_END         55

//Servos
Servo motorL;
Servo motorR;
Servo motorCPan;
Servo motorCTilt;
Servo motorSPan;

enum State {
    STOP,
    LEFT,
    RIGHT,
    FORWARD,
    BACKWARD,
    UP,
    DOWN
};

struct Component {
    bool  enabled;
    State y;      //Pan/Turn
    State x;      //Tilt/Forward/Backward
    int   panPosition;
    int   tiltPosition;
};

//States
bool      autonomyEnabled;  //Enable autonomy
bool      debugEnabled;     //Enable debug mode
bool      panRight;         //State for sensor panner
int       loopCount;        //Counts the number of loops processed
int       motorSPanState;   //Position of Sensor Panner Servo
Component camera;           //Stores camera servo states
Component drive;            //Stores drive system states

//Arduino Setup Function
void setup() {
    //Serial Setup
    Serial.begin(SERIAL_BAUD);             //Setup Serial
    Serial.setTimeout(50);                 //Fix parseInt delay
    
    //Thermal Sensor Setup
    i2c_init();                            //Initialize I2C
    PORTC = (1 << PORTC4) | (1 << PORTC5); //Enable pullup resistors
    
    //Servo Setup
    motorL.attach(MOTOR_L);
    motorL.write(90);
    motorR.attach(MOTOR_R);
    motorR.write(90);
    motorCPan.attach(MOTOR_C_PAN);
    motorCPan.write(90);
    motorCTilt.attach(MOTOR_C_TILT);
    motorCTilt.write(90);
    motorSPan.attach(MOTOR_S_PAN);
    motorSPan.write(90);
    
    //LED Setup
    pinMode(LEDS, OUTPUT);
    
    //State Initialization
    autonomyEnabled     = false;
    debugEnabled        = false;
    camera.y            = STOP;
    camera.x            = STOP;
    loopCount           = 0;
    camera.panPosition  = 90;
    camera.tiltPosition = 90;
    motorSPanState      = 90;
}

//Control Loop
void loop() {
    if (autonomyEnabled)
        autonomous_loop();
    else if (debugEnabled)  //Check and see if DEBUG_PIN is HIGH
        test_loop();
    else
        teleoperation_loop();
    
    //Used for processes that shouldn't occur every time
    loopCount++;
}

//Serial Processing (called between loops)
void serialEvent() {
    int cmd = Serial.parseInt();
    
    if (cmd != CAMERA_DIRECT) {      
        processCommand(cmd);
    } else {
        int x = Serial.parseInt();
        int y = Serial.parseInt();
        
        moveCameraTo(x, y);
    }
}

//Test Logic
void test_loop() {
    //Test Sensors
    Serial.print("Ping Sensor:   ");
    Serial.println(pingDistance());
    Serial.print("Temperature:   ");
    Serial.println(temperature());
    Serial.print("IR Distance L: ");
    Serial.println(irDistanceL());
    Serial.print("IR Distance R: ");
    Serial.println(irDistanceR());
    
    //Test Sensor Servo
    Serial.println("Test Sensor Servo");
    for (int i=10; i<170; i++) {
        motorSPan.write(i);
        delay(10);
    }
    motorSPan.write(90);
    
    //Test Drive Servos
    Serial.println("Test Left and Right Drive Servos");
    driveForward(10);
    delay(500);
    driveStop();
    
    //Test Camera Servos
    Serial.println("Camera Pan");
    motorCPan.write(0);
    delay(500);
    motorCPan.write(180);
    delay(500);
    motorCPan.write(90);
    
    Serial.println("Camera Tilt");
    motorCTilt.write(100);
    delay(500);
    motorCTilt.write(50);
    delay(500);
    motorCTilt.write(90);
    
    //Test Light
    PORTB = PORTB | B00100000;
    delay(500);
    PORTB = PORTB & B11011111;
}

//Autonomous Logic
void autonomous_loop() {

}

/**************************
 *   Teleoperation Code   *
 **************************/
 
//Teleoperation Logic
void teleoperation_loop() {    
    //Processing
    processTemperature();
    processCameraMovement();
    processSensorPan();
    
    //Drive motors 'remember' their state, so no updating is needed
    
    delay(15);
}

/*********************
 *   Control Code    *
 *********************/
 
//Process Serial Commands
void processCommand(int command) {
    switch (command) {
    case DRIVE_LEFT:
        drive.x = STOP;
        drive.y = LEFT;
        executeDrive();
        break;
    case DRIVE_RIGHT:
        drive.x = STOP;
        drive.y = RIGHT;
        executeDrive();
        break;
    case DRIVE_FORWARD:
        drive.x = FORWARD;
        drive.y = STOP;
        executeDrive();
        break;
    case DRIVE_BACKWARD:
        drive.x = BACKWARD;
        drive.y = STOP;
        executeDrive();
        break;
    case DRIVE_FRONT_LEFT:
        drive.x = FORWARD;
        drive.y = LEFT;
        executeDrive();
        break;
    case DRIVE_FRONT_RIGHT:
        drive.x = FORWARD;
        drive.y = RIGHT;
        executeDrive();
        break;
    case DRIVE_STOP:
        drive.x = STOP;
        drive.y = STOP;
        executeDrive();
        break;
    case CAMERA_LEFT:
        camera.y = LEFT;
        break;
    case CAMERA_RIGHT:
        camera.y = RIGHT;
        break;
    case CAMERA_UP:
        camera.x = UP;
        break;
    case CAMERA_DOWN:
        camera.x = DOWN;
        break;
    case CAMERA_PAN_STOP:
        camera.y = STOP;
        break;
    case CAMERA_TILT_STOP:
        camera.x = STOP;
        break;
    case CAMERA_STOP:
        camera.x = STOP;
        camera.y = STOP;
        break;
    case LEDS_ON:
        PORTB = PORTB | B00100000;
        break;
    case LEDS_OFF:
        PORTB = PORTB & B11011111;
        break;
    case AUTONOMY_ON:
        autonomyEnabled = true;
        break;
    case AUTONOMY_OFF:
        autonomyEnabled = false;
        break;
    case STOP_ALL:
        camera.x = STOP;
        camera.y = STOP;
        //driveStop();
        PORTB = PORTB & B11011111; //Disable LEDS
        autonomyEnabled = false;
        break;
    case DEBUG:
        debugEnabled = true;
        break;
    case DEBUG_END:
        debugEnabled = false;
        break;
    }
}


//Process Camera Movement
void processCameraMovement() {
    if (camera.y == LEFT) {            
        if (!(camera.panPosition <= 10)) {
            motorCPan.write(--camera.panPosition);
        }
	} else if (camera.y == RIGHT) {
		if (!(camera.panPosition >= 170)) {
			motorCPan.write(++camera.panPosition);
		}
	}
	
	if (camera.x == UP) {     
		if (!(camera.tiltPosition <= 20)) {
			motorCTilt.write(--camera.tiltPosition);
		}
	} else if (camera.x == DOWN) {
		if (!(camera.tiltPosition >= 160)) {
			motorCTilt.write(++camera.tiltPosition);
		}
	}
}

//Move Camera to a Specific Position
void moveCameraTo(int x, int y) {
    int xpos = x;
    int ypos = y;
    
    if (xpos < 10) {
       xpos = 10;
    } else if (xpos > 170) {
       xpos = 170;
    }
    
    if (ypos < 20) {
       ypos = 20;
    } else if (ypos > 160) {
       ypos = 160;
    }
    
    camera.y            = STOP;
    camera.x            = STOP;
    camera.panPosition  = x;
    camera.tiltPosition = y;

    motorCPan.write(x);
    motorCTilt.write(y);
}

//Send Temperature Value
void processTemperature() {
    if (loopCount % 20 == 0) {
        Serial.println(temperature());
    }
}

//Used to move the sensor servo (should be used in a loop)
void processSensorPan() {
    if (motorSPan.read() > 175 || motorSPan.read() < 5) {
        panRight = !panRight;
    }
    
    if (panRight) {
        motorSPan.write(++motorSPanState);
    } else {
        motorSPan.write(--motorSPanState);
    }
}

//Makes robot move Forwards
void driveForward (int speed) {
    motorL.write(-speed * 1.8);
    motorR.write(speed * 1.8);
}

void driveForwardLeft (int speed) {
    motorL.write(85);
    motorR.write(180);
}

void driveForwardRight (int speed) {
    motorL.write(0);
    motorR.write(95);
}

//Makes robot move Backwards
void driveBackward (int speed) {
    motorL.write(speed * 1.8);
    motorR.write(-speed * 1.8);
}

//Makes robot move Left
void driveLeft (int speed) {
    motorL.write(speed * 1.8);
    motorR.write(speed * 1.8);
}

//Makes robot move Right
void driveRight (int speed) {
    motorL.write(-speed * 1.8);
    motorR.write(-speed * 1.8);
}

//Makes robot Stop moving
void driveStop () {
    motorL.write(90);
    motorR.write(90);
}

void executeDrive() {
    if (drive.x == FORWARD && drive.y == LEFT) {
        driveForwardLeft(100);
    }
    else if (drive.x == FORWARD && drive.y == RIGHT) {
        driveForwardRight(100);
    }
    else if (drive.x == FORWARD && drive.y == STOP) {
        driveForward(100);
    }
    else if (drive.x == BACKWARD) {
        driveBackward(100);
    }
    else if (drive.y == LEFT && drive.x == STOP) {
        driveLeft(100);
    }
    else if (drive.y == RIGHT && drive.x == STOP) {
        driveRight(100);
    }
    else if (drive.x == STOP && drive.y == STOP) {
        driveStop();
    }
}

/********************
 *   Sensor Code    *
 ********************/

//Reads and returns the temperature from the IR Sensor
float temperature() {
    int dev       = 0x5A<<1;
    int data_low  = 0;
    int data_high = 0;
    int pec       = 0;
    
    i2c_start_wait(dev+I2C_WRITE);
    i2c_write(0x07);
    
    // read
    i2c_rep_start(dev+I2C_READ);
    
    data_low  = i2c_readAck();   //Read 1 byte and then send ack
    data_high = i2c_readAck();   //Read 1 byte and then send ack
    pec       = i2c_readNak();
    
    i2c_stop();
    
    //This converts high and low bytes together and processes temperature,
    //MSB is an error bit and is ignored for temps
    float tempFactor = 0.02;   //0.02 degrees per LSB 
                                //(measurement resolution of the MLX90614)
    float tempData   = 0x0000; //zero out the data
    
    //This masks off the error bit of the high byte, 
    //then moves it left 8 bits and adds the low byte.
    tempData = (float)(((data_high & 0x007F) << 8) + data_low);
    tempData = (tempData * tempFactor)-0.01;
    
    float celcius    = tempData - 273.15;
    float fahrenheit = (celcius*1.8) + 32;

    return fahrenheit;
}

//Reads and returns the distance in cm from the Ping Sensor
float pingDistance() {
    float duration;
    
    //Trigger the Ping Sensor by sending a high pulse of
    //two or more microseconds
    pinMode(SENSOR_PING, OUTPUT);
    digitalWrite(SENSOR_PING, LOW);
    delayMicroseconds(2);              //2 in documentation
    digitalWrite(SENSOR_PING, HIGH);
    delayMicroseconds(5);              //5 in documentation
    digitalWrite(SENSOR_PING, LOW);
    
    //Read the results from the Ping Sensor
    pinMode(SENSOR_PING, INPUT);
    duration = pulseIn(SENSOR_PING, HIGH);
    
    return duration / 29 / 2;
}

//Distance in cm from Left IR Sensor
float irDistanceL() {
    return 65 * pow(analogRead(SENSOR_IR_L) * 0.0048828125 , -1.10);
}

//Distance in cm from Right IR Sensor
float irDistanceR() {
    return 65 * pow(analogRead(SENSOR_IR_R) * 0.0048828125 , -1.10);
}