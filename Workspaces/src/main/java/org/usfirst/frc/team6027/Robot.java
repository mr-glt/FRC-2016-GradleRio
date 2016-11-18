package org.usfirst.frc.team6027;

import edu.wpi.first.wpilibj.*;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import java.io.IOException;

public class Robot extends IterativeRobot{
    //Drivetrain
    RobotDrive merlin; //Create New Robot Drive

    //Controllers
    Joystick stick; //Create a new stick for our 3D Pro
    Joystick controller; //Creates a new stick for our XBox Controller

    //Solenoids
    DoubleSolenoid ballPlungerSol = new DoubleSolenoid(2, 3); //This solenoid runs the the ball pusher on the dust pan
    DoubleSolenoid dustPanSol = new DoubleSolenoid(4, 5); //This solenoid runs the moving of the dust pan.
    DoubleSolenoid stops = new DoubleSolenoid(1, 6); //This solenoid runs the stop on the side of the bot.

    //Speed Controllers
    CANTalon flyWheel = new CANTalon(0); //Fly Wheel Speed Controller

    //Gyro
    ADXRS450_Gyro gyro; //SPI gyro from FIRST Choice

    //Compressor
    Compressor c = new Compressor(0); //Create compressor 'c' on 0
    //Ultrasonic Sensor
    AnalogInput ultrasonic;
    //Strings
    String dustMode = "Up";

    //Booleans
    boolean plungerButton; //Create a bool for our plunger button
    double upButton; //Create a bool for our dust pan up button
    boolean locksButtonValue; //Create a bool for our locks open button
    boolean locksButtonCloseValue; //Create a bool for our lock close button
    boolean invertButton; //Create a bool for our inversion button
    double downButton; //Create a bool for our dust pan down button
    boolean spinShooterwheelForward; //Create a bool for our fly wheel out button
    boolean spinShooterwheelBackward; //Create a bool for our fly wheel in button
    boolean slowModeButton; //Create a bool for our slow mode button
    boolean gyroSetButton; //Create a bool for our gyro set button
    boolean locksEngaded = false; //Create a bool for locking the dust pan
    boolean dustpanUpStatus; //Create a bool for dust pan status
    boolean autoStop = false; //Create a bool for stopping auto mode
    boolean triggerLocks = false; //Create a bool to trigger locks
    boolean triggerClose = false; //Create a bool to shut locks
    boolean ignoreDistance = false;

    //Doubles
    double Kp = 0.03; //Constant used to drive forward in a line
    double driveSchedulerX; //Used to hold X drive value so it can be modified multiple times
    double driveSchedulerY; //Used to hold Y drive value so it can be modified multiple times
    double currentDistance = 0; //Used to hold distance
    final double valueToInches = 0.125; //Used for ultrasonic

    //Ints
    int atonLoopCounter; //Loop used to order auto mode

    public void robotInit() {
        ultrasonic = new AnalogInput(3);
        //Drivetrain
        merlin = new RobotDrive(0,9,1,8); //Assign robotdrive on the 4 pwm pins

        //Controllers
        stick = new Joystick(1); //Assign to a joystick on port 1
        controller = new Joystick(0); //Assign to a controller on port 0

        //Gyro
        gyro = new ADXRS450_Gyro(); //Create a new object for our SPI gyro
        gyro.calibrate(); //Calibrate our gyro



        //Grip: More info here: https://github.com/WPIRoboticsProjects/GRIP/wiki/Tutorial:-Run-GRIP-from-a-CPP,-Java,-or-LabVIEW-FRC-program
        try {
            new ProcessBuilder("/home/lvuser/grip").inheritIO().start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void autonomousInit() {

    }

    public void autonomousPeriodic() {
        //Compressor
        c.setClosedLoopControl(true); //Run the compressor on a closed loop

        //Gyro
        double angle = gyro.getAngle(); //Set angle equal to the gyro's angle
        SmartDashboard.putNumber("Angle: ", angle); //Send the angle to the dashboard
        //Ultrasonic
        currentDistance = ultrasonic.getValue()*valueToInches;
        SmartDashboard.putNumber("Distance Forward", currentDistance);
        if(!autoStop){ //Check if we are done
            if(atonLoopCounter < 50){ //Check if we have done 50 loops(About 1 seconds)
                stops.set(DoubleSolenoid.Value.kReverse); //Put out stop
                dustPanSol.set(DoubleSolenoid.Value.kReverse); //Drop dust pan

                SmartDashboard.putString("Auto Status: ", "Down and Calibrate"); //Send status to dashboard
                SmartDashboard.putNumber("Loop Number: ", atonLoopCounter);	//Send loop to dashboard
                atonLoopCounter++; //Add 1 to our counter
            }
            if(atonLoopCounter > 49 && atonLoopCounter < 350){ //After 1 second, drive for 6 seconds
                SmartDashboard.putNumber("Error", (angle*Kp)); //Send X error to dashboard
                driveSchedulerY = 0.0; //Drive the the oppiste of our x error to correct
                driveSchedulerX = -0.85; //Drive backwards  at 90%
                SmartDashboard.putString("Auto Status: ", "Driving Backward"); //Send status to dashboard
                SmartDashboard.putNumber("Loop Number: ", atonLoopCounter);	//Send loop to dashboard
                atonLoopCounter++; //Add 1 to our counter
            }
            if(atonLoopCounter >349 && atonLoopCounter < 355){
                driveSchedulerX = 0.0; //Turn off motors
                driveSchedulerY = 0.0; //Turn off motors
                autoStop = true; //Tell auto to stop
                atonLoopCounter++; //Add 1 to our counter
                SmartDashboard.putString("Auto Status: ", "Stopped"); //Send Status to dashboard
                SmartDashboard.putNumber("Loop Number: ", atonLoopCounter);	//Send loop to dashboard
            }
            merlin.arcadeDrive(driveSchedulerX, driveSchedulerY); //Drive the robot
        }
    }

    public void teleopPeriodic() {
        //Compressor
        c.setClosedLoopControl(true); //Run the compressor on a closed loop

        //Gyro
        double angle = gyro.getAngle(); //Set angle equal to the gyro's angle
        SmartDashboard.putNumber("Angle: ", angle); //Send Angle to SmartDashboard
        gyroSetButton = stick.getRawButton(5); //Create a button to calibrate the gyro
        if(gyroSetButton){ //Check for button status
            gyro.calibrate(); //Calibrate gyro to current angle
        }

        //Ultrasonic
        currentDistance = ultrasonic.getValue()*valueToInches;
        SmartDashboard.putNumber("Distance Forward", currentDistance);
        //if(currentDistance > 84){
        //	SmartDashboard.putBoolean("Ready to Shoot", false);

        //Drivetrain
        invertButton = controller.getRawButton(5); //Create a button to invert the steering
        if(!invertButton){ //Check for button update
            double controllerLY = controller.getRawAxis(4) * -1; //Invert the axis
            double controllerRX = controller.getRawAxis(1) * -1; //Invert the axis

            driveSchedulerY = controllerLY; //Drive to controller
            driveSchedulerX = controllerRX; //Drive to controller
            SmartDashboard.putString("Inverted Drive: ", "Off"); //Send status to SmartDashboard
        }
        else{
            double controllerLY = controller.getRawAxis(4) * 0.7; //Drive to controller
            double controllerRX = controller.getRawAxis(1) * 1; //Drive to controller
            driveSchedulerY = controllerLY; //Drive
            driveSchedulerX = controllerRX; // Drive
            SmartDashboard.putString("Inverted Drive: ", "On"); //Send status to Smartdashboard
        }
        slowModeButton = controller.getRawButton(6); //Create a button for a slow drive mode
        if(slowModeButton){
            double controllerLY = controller.getRawAxis(4) * -0.4; //Multiplier
            double controllerRX = controller.getRawAxis(1) * -0.7; //Multiplier
            driveSchedulerY = controllerLY; //Drive
            driveSchedulerX = controllerRX; //Drive
            SmartDashboard.putString("Slow Mode: ", "On"); //Send status to SmartDashboard
        }
        else{
            SmartDashboard.putString("Slow Mode: ", "Off"); //Send status to SmartDashboard
        }

        //Shooter Wheel
        spinShooterwheelForward = stick.getRawButton(4); //Create a button for shooter wheel forward
        spinShooterwheelBackward = stick.getRawButton(3); //Create a button for shooter wheel backward
        if(spinShooterwheelForward && !spinShooterwheelBackward){
            flyWheel.set(-0.95); //Spin wheel forward
            SmartDashboard.putString("Shooter Wheel: ", "Shooting");
        }
        if(spinShooterwheelBackward && !spinShooterwheelForward){
            flyWheel.set(0.95); //Spin wheel backward
            SmartDashboard.putString("Shooter Wheel: ", "Picking Up");
        }
        if(!spinShooterwheelBackward && !spinShooterwheelForward){
            flyWheel.set(0); //Stop wheel
            SmartDashboard.putString("Shooter Wheel: ", "Off");
        }
        //Dust Pan Moving Code
        upButton = stick.getY(); //Create a button to move dust pan up
        //downButton = stick.getMagnitude()(10); //Create a button to move dust pan down
        SmartDashboard.putNumber("hing", upButton);
        if(upButton == 1){
            dustPanSol.set(DoubleSolenoid.Value.kForward); //The dust pan cylinders are driven up by air and let to free fall
            SmartDashboard.putString("Dustpan Status: ", "Up"); //Send status to Dashboard
            dustpanUpStatus = true; //Bool to say dust pan is up
            triggerClose = true; //Trigger the locks
        }
        if(upButton == -1){
            dustPanSol.set(DoubleSolenoid.Value.kReverse);
            SmartDashboard.putString("Dustpan Status: ", "Down");
            dustpanUpStatus = false; //Bool to say dust pan is down
            triggerLocks = true; //Trigger locks
        }

        //Dust Pan Locks
        locksButtonValue = stick.getRawButton(11); //Create button to toggle locks
        if(locksButtonValue || locksEngaded || triggerLocks){
            stops.set(DoubleSolenoid.Value.kReverse); //Set locks out
            locksEngaded = true;
            triggerLocks = false;
        }
        else{
            stops.set(DoubleSolenoid.Value.kForward);
        }

        locksButtonCloseValue = stick.getRawButton(12); //Create a button to close locks
        if(locksButtonCloseValue || triggerClose){
            stops.set(DoubleSolenoid.Value.kForward); //Shut locks
            locksEngaded = false;
            triggerClose = false;
        }
        if(!locksEngaded){
            SmartDashboard.putString("Locks Status: ", "Out"); //Send status
        }
        if(locksEngaded){
            SmartDashboard.putString("Locks Status: ", "In"); //Send status
        }

        //Ball Plunger
        plungerButton = stick.getRawButton(1); //Create a button to control the plunger
        if(plungerButton){
            ballPlungerSol.set(DoubleSolenoid.Value.kForward); //Set plunger out
            SmartDashboard.putString("Plunger Status: ", "Out"); //Send status
        }
        else{
            ballPlungerSol.set(DoubleSolenoid.Value.kReverse); //Set plunger in
            SmartDashboard.putString("Plunger Status: ", "In"); //Send status
        }

        //Drivetrain
        SmartDashboard.putNumber("X Drive Value: ", driveSchedulerX); //Send status
        SmartDashboard.putNumber("Y Drive Value: ", driveSchedulerY); //Send status
        merlin.arcadeDrive(driveSchedulerX, driveSchedulerY); //Drive the robot
    }

    public void testPeriodic() { //Used to pressurize before a match
        //Ultrasonic
        currentDistance = ultrasonic.getValue()*valueToInches;
        SmartDashboard.putNumber("Distance Forward", currentDistance);
        //c.setClosedLoopControl(true); //Turn compressor on closed loop
        dustPanSol.set(DoubleSolenoid.Value.kForward); //Set dustpan up
        dustMode = "Up";
    }
}