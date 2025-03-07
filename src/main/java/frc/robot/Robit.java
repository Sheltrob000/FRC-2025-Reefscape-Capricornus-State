// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import au.grapplerobotics.CanBridge;
import choreo.auto.AutoFactory;
import edu.wpi.first.net.PortForwarder;
import edu.wpi.first.wpilibj.DataLogManager;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.button.CommandJoystick;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import frc.robot.autos.AutoRoutines;
import frc.robot.commands.*;
import frc.robot.subsystems.arm.ArmSubsystemConstants;
import frc.robot.subsystems.climber.ClimberSubsystem;
import frc.robot.subsystems.climber.ClimberSubsystemConstants;
import frc.robot.subsystems.elevator.ElevatorSubsystemConstants;
import frc.robot.subsystems.intakeWheel.AlgaeIntakeSubsystemConstants;
import frc.robot.subsystems.intakeWheel.CoralIntakeSubsystemConstants;
import frc.robot.subsystems.lidarSensor.LidarSensorSubsystemConstants;
import frc.robot.subsystems.pneumatics.PneumaticsSubsystemConstants;
import frc.robot.subsystems.swerveDrive.SwerveDriveSubsystem;
import frc.robot.subsystems.swerveDrive.SwerveDriveSubsystemConstants;
import frc.robot.subsystems.wrist.WristSubsystemConstants;
import frc.robot.triggermaps.*;

public class Robit extends TimedRobot {

    public Robit() {
        SmartDashboard.putString("Robot Comments", Constants.robotComments);
        PortForwarder.add(5800, "orangepi5.local", 5800);
        CanBridge.runTCP();

        double deadband = 0.05;
        CommandXboxController driverController = new CommandXboxController(0);
        CommandXboxController operatorController = new CommandXboxController(1);
        CommandJoystick climberController = new CommandJoystick(2);
        CommandXboxController manualController = new CommandXboxController(3);

        SwerveDriveSubsystem swerveDriveSubsystem = SwerveDriveSubsystemConstants.createCTRESwerveDrive();
        AutoFactory autoFactory = SwerveDriveSubsystemConstants.getAutoFactory();

        Manipulator manipulator = new Manipulator(
                ArmSubsystemConstants.create(),
                PneumaticsSubsystemConstants.createAlgaeClaw(),
                PneumaticsSubsystemConstants.createCoralClaw(),
                ElevatorSubsystemConstants.create(),
                AlgaeIntakeSubsystemConstants.create(),
                CoralIntakeSubsystemConstants.create(),
                LidarSensorSubsystemConstants.create(),
                WristSubsystemConstants.create());
        ClimberSubsystem climberSubsystem = ClimberSubsystemConstants.create();

        AlgaePickup algaePickup = new AlgaePickup(manipulator);
        AlgaeScore algaeScore = new AlgaeScore(manipulator);
        CoralPickup coralPickup = new CoralPickup(manipulator);
        CoralScore coralScore = new CoralScore(manipulator);
        Manual manual = new Manual(manipulator);

        new DriveMap(driverController, deadband, swerveDriveSubsystem);
        new AlgaePickupMap(operatorController, deadband, algaePickup);
        new AlgaeScoreMap(driverController, operatorController, deadband, algaeScore);
        new CoralPickupMap(operatorController, coralPickup);
        new CoralScoreMap(driverController, operatorController, coralScore);
        new ClimberMap(climberController, deadband, climberSubsystem);
        new ManualMap(manualController, operatorController, deadband, manual);
        new AutoRoutines(autoFactory, coralPickup, coralScore);
        SmartDashboard.putData(CommandScheduler.getInstance());
        DataLogManager.start();
        DriverStation.startDataLog(DataLogManager.getLog(), true);
        addPeriodic(CommandScheduler.getInstance()::run, 0.020);
    }
}
