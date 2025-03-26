package frc.robot.triggermaps;

import choreo.auto.AutoFactory;
import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import edu.wpi.first.wpilibj2.command.button.Trigger;
import frc.robot.subsystems.swerveDrive.SwerveDriveSubsystem;

import static java.lang.Math.pow;
import static java.lang.Math.toDegrees;

public class DriveMap {

    public enum ReefPosition {
        LEFT,
        CENTER,
        RIGHT
    }


    private final CommandXboxController driver;
    private final double deadband;
    private final SwerveDriveSubsystem swerveDriveSubsystem;
    private final AutoFactory autoFactory;

    public DriveMap(
            CommandXboxController driver,
            double deadband,
            SwerveDriveSubsystem swerveDriveSubsystem,
            AutoFactory autoFactory) {
        this.driver = driver;
        this.deadband = deadband;
        this.swerveDriveSubsystem = swerveDriveSubsystem;
        this.autoFactory = autoFactory;

        bindFieldCentricDrive();
        bindRobotCentricDrive();
        bindClockDrive();

        bindZeroWheel();
        bindSeedFieldCentric();

        bindSetFieldFromCamera();
        bindGoTo90();
        bindGotTo270();
        bindGoTo1();
        bindGoTo0();
    }

    private double getMaxVelocitySetpointScalar() {
        double x = -MathUtil.applyDeadband(driver.getLeftY(), deadband);
        double y = -MathUtil.applyDeadband(driver.getLeftX(), deadband);
        double velocity = Math.hypot(x, y);
        return Math.min(velocity, 1);
    }

    private double getMaxVelocitySetpointSquaredScalar() {
        return pow(getMaxVelocitySetpointScalar(), 2);
    }

    private double getMaxAngularVelocitySetpointScalar() {
        double leftTrigger = MathUtil.applyDeadband(driver.getLeftTriggerAxis(), deadband);
        double rightTrigger = MathUtil.applyDeadband(driver.getRightTriggerAxis(), deadband);
        return leftTrigger - rightTrigger;
    }

    private double getHeadingDegrees() {
        double x = -MathUtil.applyDeadband(driver.getLeftY(), deadband);
        double y = -MathUtil.applyDeadband(driver.getLeftX(), deadband);
        return toDegrees(Math.atan2(y, x));
    }

    private double getRotationDegrees() {
        double x = -MathUtil.applyDeadband(driver.getRightY(), deadband);
        double y = -MathUtil.applyDeadband(driver.getRightX(), deadband);
        return toDegrees(Math.atan2(y, x));
    }

    private double getRightStickValue() {
        double x = -MathUtil.applyDeadband(driver.getRightY(), deadband);
        double y = -MathUtil.applyDeadband(driver.getRightX(), deadband);
        return Math.hypot(x, y);
    }

    private void bindFieldCentricDrive() {
        Trigger leftStickTrigger = new Trigger(() -> getMaxVelocitySetpointSquaredScalar() != 0.0);
        Trigger rightStickTrigger = new Trigger(() -> getRightStickValue() != 0.0);
        Trigger notClockDrive = leftStickTrigger.and(rightStickTrigger.negate());
        Trigger rotationalVelocity = new Trigger(() -> getMaxAngularVelocitySetpointScalar() != 0.0);
        (notClockDrive.or(rotationalVelocity)).and(driver.y().negate())
                .whileTrue(swerveDriveSubsystem.fieldCentricDrive(
                        this::getMaxVelocitySetpointSquaredScalar,
                        this::getHeadingDegrees,
                        this::getMaxAngularVelocitySetpointScalar));
    }

    private void bindRobotCentricDrive() {
        Trigger leftStickTrigger = new Trigger(() -> getMaxVelocitySetpointSquaredScalar() != 0.0);
        Trigger rightStickTrigger = new Trigger(() -> getRightStickValue() != 0.0);
        Trigger notClockDrive = leftStickTrigger.and(rightStickTrigger.negate());
        Trigger rotationalVelocity = new Trigger(() -> getMaxAngularVelocitySetpointScalar() != 0.0);
        (notClockDrive.or(rotationalVelocity)).and(driver.y())
                .whileTrue(swerveDriveSubsystem.robotCentricDrive(
                        this::getMaxVelocitySetpointSquaredScalar,
                        this::getHeadingDegrees,
                        this::getMaxAngularVelocitySetpointScalar));
    }

    private void bindClockDrive() {
        Trigger rightStickTrigger = new Trigger(() -> getRightStickValue() != 0.0);
        Trigger rotationalVelocity = new Trigger(() -> getMaxAngularVelocitySetpointScalar() != 0.0);
        rightStickTrigger.and(rotationalVelocity.negate())
                .whileTrue(swerveDriveSubsystem.clockDrive(
                        this::getMaxVelocitySetpointSquaredScalar,
                        this::getHeadingDegrees,
                        this::getRotationDegrees));


    }

    private void bindZeroWheel() {
        driver.back().whileTrue(swerveDriveSubsystem.zeroWheels());
    }

    private void bindSeedFieldCentric() {
        driver.start().onTrue(swerveDriveSubsystem.seedFieldCentric());
    }

    private void bindSetFieldFromCamera() {
        driver.b().whileTrue(swerveDriveSubsystem.setPose(new Pose2d()));
    }

    private void bindGoTo90() {
        driver.povLeft().whileTrue(swerveDriveSubsystem.goToAngle(90));
    }
    private void bindGotTo270() {
        driver.povRight().whileTrue(swerveDriveSubsystem.goToAngle(-90));
    }

    private void bindGoTo1() {
        driver.povUp().whileTrue(swerveDriveSubsystem.goToPose(1, 0, 0));
    }

    private void bindGoTo0(){
        driver.a().whileTrue(swerveDriveSubsystem.goToPose(0, 0, 0));
    }


}
