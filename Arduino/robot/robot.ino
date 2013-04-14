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

//Constants
#define CENTER 90

//Digital Pins
#define DEBUG_PIN    2
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

//Message States
#define LEDS_ON            0
#define LEDS_OFF           1

#define DRIVE_LEFT         2
#define DRIVE_FRONTLEFT    3
#define DRIVE_FRONT        4
#define DRIVE_FRONTRIGHT   5
#define DRIVE_RIGHT        6
#define DRIVE_BACKRIGHT    7
#define DRIVE_BACK         8
#define DRIVE_BACKLEFT     9
#define DRIVE_STOP         10

#define CAMERA_LEFT        12
#define CAMERA_UPLEFT      13
#define CAMERA_UP          14
#define CAMERA_UPRIGHT     15
#define CAMERA_RIGHT       16
#define CAMERA_DOWNRIGHT   17
#define CAMERA_DOWN        18
#define CAMERA_DOWNLEFT    19
#define CAMERA_STOP        20

#define AUTONOMY_ON        22
#define AUTONOMY_OFF       23

//Global States
bool autonomyEnabled = false;
int  defaultSpeed    = 100;
int  cameraState     = CAMERA_STOP;
bool panRight        = false;       //Used to decide sweep direction of sensor panner
byte serialInput;

//Servos
Servo motorL;
Servo motorR;
Servo motorCPan;
Servo motorCTilt;
Servo motorSPan;

void setup() {
    //Serial Setup
    Serial.begin(SERIAL_BAUD);             //Setup Serial
    Serial.setTimeout(50);                 //Fix parseInt delay
    
    //Thermal Sensor Setup
    i2c_init();                            //Initialize I2C
    PORTC = (1 << PORTC4) | (1 << PORTC5); //Enable pullup resistors
    
    //Servo Setup
    motorL.attach(MOTOR_L);
    motorL.write(CENTER);
    motorR.attach(MOTOR_R);
    motorR.write(CENTER);
    motorCPan.attach(MOTOR_C_PAN);
    motorCPan.write(CENTER);
    motorCTilt.attach(MOTOR_C_TILT);
    motorCTilt.write(CENTER);
    motorSPan.attach(MOTOR_S_PAN);
    motorSPan.write(CENTER);
    
    //LED Setup
    pinMode(LEDS, OUTPUT);
    
    //Test Pin
    pinMode(DEBUG_PIN, INPUT);
}

void loop() {
    Serial.println(pingDistance());
    Serial.println(temperature());
    motorSPan.write(10);
    delay(500);
    Serial.println(pingDistance());
    Serial.println(temperature());
    motorSPan.write(90);
    delay(500);
    Serial.println(pingDistance());
    Serial.println(temperature());
    motorSPan.write(170);
    delay(500);
    Serial.println(pingDistance());
    Serial.println(temperature());
    motorSPan.write(90);
    delay(500);
}

/*
void loop() {
    if (autonomyEnabled)
        loop_autonomous();
    else if (digitalRead(DEBUG_PIN) == HIGH)
        loop_test();
    else
        loop_teleoperation();
}

//The main control loop for Teleoperation
void loop_teleoperation() {
    if (Serial.available()) {
        processCommand(Serial.parseInt());
    } else {
        //delay(10);
    }
    moveSensorServo();
    processCameraMovement();
    if (millis() % 100 == 0) {
       Serial.print(temperature());
    }
}

//The main control loop for Autonomy
void loop_autonomous() {
    if (Serial.available()) {
        processCommand(Serial.parseInt());
    }
}


//Sensor diagnostics
void loop_test() {
    //Serial.print("Ping Sensor:   ");
    //Serial.println(pingDistance());
    Serial.print("Temperature:   ");
    Serial.println(temperature());
    //Serial.print("IR Distance L: ");
    //Serial.println(irDistanceL());
    //Serial.print("IR Distance R: ");
    //Serial.println(irDistanceR());
    
    Serial.println("Test Sensor Servo");
    for (int i=0; i<300; i++) {
        moveSensorServo();
        Serial.print(motorSPan.read());
        Serial.print(" ");
        delay(10);
    }
    Serial.println(" ");
    
    Serial.println("Test Left and Right Drive Servos");
    driveForward(10);
    delay(500);
    driveStop();
    
    Serial.println("Camera Pan");
    motorCPan.write(0);
    delay(500);
    motorCPan.write(180);
    delay(500);
    motorCPan.write(CENTER);
    
    Serial.println("Camera Tilt");
    motorCTilt.write(100);
    delay(500);
    motorCTilt.write(50);
    delay(500);
    motorCTilt.write(CENTER);
    
    ledsOn();
    
    delay(500);
    ledsOff();
}*/
/*
//Process Command
void processCommand(int input) {
    switch (input) {
    case DRIVE_FRONT:
        if (!autonomyEnabled)
            driveForward(defaultSpeed);
        break;
    case DRIVE_FRONTLEFT:
        if (!autonomyEnabled)
            driveForwardLeft(defaultSpeed);
        break;
    case DRIVE_FRONTRIGHT:
        if (!autonomyEnabled)
            driveForwardRight(defaultSpeed);
        break;
    case DRIVE_BACK:
        if (!autonomyEnabled)
            driveBackward(defaultSpeed);
        break;
    case DRIVE_BACKLEFT:
        if (!autonomyEnabled)
            driveBackwardRight(defaultSpeed);
        break;
    case DRIVE_BACKRIGHT:
        if (!autonomyEnabled)
            driveBackwardRight(defaultSpeed);
        break;
    case DRIVE_LEFT:
        if (!autonomyEnabled)
            driveLeft(defaultSpeed);
        break;
    case DRIVE_RIGHT:
        if (!autonomyEnabled)
            driveRight(defaultSpeed);
        break;
    case DRIVE_STOP:
        if (!autonomyEnabled)
            driveStop();
        break;
    case CAMERA_UP:
        cameraState = CAMERA_UP;
        break;
    case CAMERA_UPLEFT:
        cameraState = CAMERA_UPLEFT;
        break;
    case CAMERA_UPRIGHT:
        cameraState = CAMERA_UPRIGHT;
        break;
    case CAMERA_DOWN:
        cameraState = CAMERA_DOWN;
        break;
    case CAMERA_DOWNLEFT:
        cameraState = CAMERA_DOWNLEFT;
        break;
    case CAMERA_DOWNRIGHT:
    	cameraState = CAMERA_DOWNRIGHT;
    	break;
    case CAMERA_LEFT:
        cameraState = CAMERA_LEFT;
        break;
    case CAMERA_RIGHT:
        cameraState = CAMERA_RIGHT;
        break;
    case CAMERA_STOP:
        cameraState = CAMERA_STOP;
        break;
    case LEDS_ON:
        ledsOn();
        break;
    case LEDS_OFF:
        ledsOff();
        break;
    case AUTONOMY_ON:
        autonomyEnabled = true;
        break;
    case AUTONOMY_OFF:
        autonomyEnabled = false;
        break;
    }
}
*/
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

//Makes robot move Forwards
void driveForward (int speed) {
    motorL.write(-speed * 1.8);
    motorR.write(speed * 1.8);
}

void driveForwardLeft (int speed) {
    motorL.write(-speed);
    motorR.write(speed * 1.8);
}

void driveForwardRight (int speed) {
    motorL.write(-speed * 1.8);
    motorR.write(speed);
}

//Makes robot move Backwards
void driveBackward (int speed) {
    motorL.write(speed * 1.8);
    motorR.write(-speed * 1.8);
}

void driveBackwardLeft (int speed) {
    motorL.write(speed);
    motorR.write(-speed * 1.8);
}

void driveBackwardRight (int speed) {
    motorL.write(speed * 1.8);
    motorR.write(-speed);
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
    motorL.write(CENTER);
    motorR.write(CENTER);
}

//A task for executing camera movements
void processCameraMovement() {
    switch(cameraState) {
    case CAMERA_UP:
        motorCTilt.write(motorCTilt.read() - 1);
        break;
    case CAMERA_UPLEFT:
        motorCTilt.write(motorCTilt.read() - 1);
        motorCPan.write(motorCPan.read() - 1);
        break;
    case CAMERA_UPRIGHT:
        motorCTilt.write(motorCTilt.read() - 1);
        motorCPan.write(motorCPan.read() + 1);
        break;
    case CAMERA_DOWN:
        motorCTilt.write(motorCTilt.read() + 1);
        break;
    case CAMERA_DOWNLEFT:
        motorCTilt.write(motorCTilt.read() + 1);
        motorCPan.write(motorCPan.read() - 1);
        break;
    case CAMERA_DOWNRIGHT:
        motorCTilt.write(motorCTilt.read() + 1);
        motorCPan.write(motorCPan.read() + 1);
        break;
    case CAMERA_LEFT:
        motorCPan.write(motorCPan.read() - 1);
        break;
    case CAMERA_RIGHT:
        motorCPan.write(motorCPan.read() + 1);
        break;
    }
    
    delay(18);
}

//LEDs On
void ledsOn() {
    digitalWrite(LEDS, HIGH);
}

//LEDs Off
void ledsOff() {
    digitalWrite(LEDS, LOW);
}

//Used to move the sensor servo (should be used in a loop)
void moveSensorServo() {
    if (motorSPan.read() > 175 || motorSPan.read() < 5) {
        panRight = !panRight;
    }
    
    if (panRight) {
        motorSPan.write(motorSPan.read() + 1);
    } else {
        motorSPan.write(motorSPan.read() - 1);
    }
}
