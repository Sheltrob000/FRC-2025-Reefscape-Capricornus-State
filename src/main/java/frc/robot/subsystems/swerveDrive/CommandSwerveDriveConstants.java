package frc.robot.subsystems.swerveDrive;

import static edu.wpi.first.units.Units.*;
import static frc.robot.subsystems.swerveDrive.CommandSwerveDriveConstants.Simulation.*;
import static org.photonvision.PhotonPoseEstimator.*;

import com.ctre.phoenix6.configs.*;
import com.ctre.phoenix6.hardware.CANcoder;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.*;
import com.ctre.phoenix6.swerve.*;
import com.ctre.phoenix6.swerve.SwerveModuleConstants.*;

import com.ctre.phoenix6.swerve.utility.PhoenixPIDController;
import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.apriltag.AprilTagFields;
import edu.wpi.first.math.MatBuilder;
import edu.wpi.first.math.Matrix;
import edu.wpi.first.math.Nat;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.math.numbers.N1;
import edu.wpi.first.math.numbers.N3;
import edu.wpi.first.units.measure.*;

import digilib.cameras.CameraConstants;
import digilib.cameras.PhotonVisionCamera;
import digilib.swerve.CTRESwerveDrive;
import digilib.swerve.SwerveDriveTelemetry;
import frc.robot.Constants;
import org.photonvision.PhotonCamera;

public class CommandSwerveDriveConstants {

    static final class Simulation {
        static final Time simLoopPeriod = Seconds.of(0.001);
    }

    public static final class OurCameraConstants {
        private static final AprilTagFieldLayout layout = AprilTagFieldLayout.loadField(AprilTagFields.kDefaultField);
        private static final PoseStrategy primaryStrategy = PoseStrategy.MULTI_TAG_PNP_ON_COPROCESSOR;
        private static final PoseStrategy fallbackPoseStrategy = PoseStrategy.LOWEST_AMBIGUITY;
        private static final Matrix<N3, N1> singleTagStdDev = MatBuilder.fill(Nat.N3(), Nat.N1(), 0.0, 0.0, 0.0);

        public static final class Camera0 {
            private static final String name = "Thrifty_Cam_2025";
            private static final Distance cameraX = Meter.of(0.0);
            private static final Distance cameraY = Meter.of(0.0);
            private static final Distance cameraZ = Meter.of(0.0);
            private static final Angle roll = Degrees.of(0.0);
            private static final Angle pitch = Degrees.of(0.0);
            private static final Angle yaw = Degrees.of(0.0);
            private static final Rotation3d cameraAngle = new Rotation3d(roll, pitch, yaw);
            private static final Transform3d robotToCamera = new Transform3d(
                    cameraX,
                    cameraY,
                    cameraZ,
                    cameraAngle);
            private static final CameraConstants constants = new CameraConstants(
                    name,
                    robotToCamera,
                    layout,
                    primaryStrategy,
                    fallbackPoseStrategy,
                    singleTagStdDev);
        }

        public static final class Camera1 {
            private static final String name = "OV9782-07";
            private static final Distance cameraX = Meter.of(0.0);
            private static final Distance cameraY = Meter.of(0.0);
            private static final Distance cameraZ = Meter.of(0.0);
            private static final Angle roll = Degrees.of(0.0);
            private static final Angle pitch = Degrees.of(0.0);
            private static final Angle yaw = Degrees.of(0.0);
            private static final Rotation3d cameraAngle = new Rotation3d(roll, pitch, yaw);
            private static final Transform3d robotToCamera = new Transform3d(
                    cameraX,
                    cameraY,
                    cameraZ,
                    cameraAngle);
            private static final CameraConstants constants = new CameraConstants(
                    name,
                    robotToCamera,
                    layout,
                    primaryStrategy,
                    fallbackPoseStrategy,
                    singleTagStdDev);
        }

        public static final class Camera2 {
            private static final String name = "OV9782-08";
            private static final Distance cameraX = Meter.of(0.0);
            private static final Distance cameraY = Meter.of(0.0);
            private static final Distance cameraZ = Meter.of(0.0);
            private static final Angle roll = Degrees.of(0.0);
            private static final Angle pitch = Degrees.of(0.0);
            private static final Angle yaw = Degrees.of(0.0);
            private static final Rotation3d cameraAngle = new Rotation3d(roll, pitch, yaw);
            private static final Transform3d robotToCamera = new Transform3d(
                    cameraX,
                    cameraY,
                    cameraZ,
                    cameraAngle);
            private static final CameraConstants constants = new CameraConstants(
                    name,
                    robotToCamera,
                    layout,
                    primaryStrategy,
                    fallbackPoseStrategy,
                    singleTagStdDev);
        }
    }



    /* Keep track if we've ever applied the operator perspective before or not */

    // Both sets of gains need to be tuned to your individual robot.

    // The steer motor uses any SwerveModule.SteerRequestType control request with the
    // output type specified by SwerveModuleConstants.SteerMotorClosedLoopOutput
    private static final Slot0Configs module0SteerGains = new Slot0Configs()
            .withKP(81.49).withKI(0).withKD(0.5)
            .withKS(0.09556).withKV(3.0882).withKA(0.18325)
            .withStaticFeedforwardSign(StaticFeedforwardSignValue.UseClosedLoopSign);
    // When using closed-loop control, the drive motor uses the control
    // output type specified by SwerveModuleConstants.DriveMotorClosedLoopOutput
    private static final Slot0Configs module0DriveGains = new Slot0Configs()
            .withKP(0.1).withKI(0).withKD(0)
            .withKS(0.38569).withKV(0.12431).withKA(0.010011);

    private static final Slot0Configs module1SteerGains = new Slot0Configs()
            .withKP(63.667).withKI(0).withKD(0.5)
            .withKS(0.093716).withKV(3.0941).withKA(0.21437)
            .withStaticFeedforwardSign(StaticFeedforwardSignValue.UseClosedLoopSign);
    // When using closed-loop control, the drive motor uses the control
    // output type specified by SwerveModuleConstants.DriveMotorClosedLoopOutput
    private static final Slot0Configs module1DriveGains = new Slot0Configs()
            .withKP(0.37357).withKI(0).withKD(0)
            .withKS(0.22567).withKV(0.12133).withKA(0.0067662);

    private static final Slot0Configs module2SteerGains = new Slot0Configs()
            .withKP(93.829).withKI(0).withKD(0.5)
            .withKS(0.032676).withKV(3.1058).withKA(0.20666)
            .withStaticFeedforwardSign(StaticFeedforwardSignValue.UseClosedLoopSign);
    // When using closed-loop control, the drive motor uses the control
    // output type specified by SwerveModuleConstants.DriveMotorClosedLoopOutput
    private static final Slot0Configs module2DriveGains = new Slot0Configs()
            .withKP(0.19223).withKI(0).withKD(0)
            .withKS(0.19595).withKV(0.11845).withKA(0.002047);

    private static final Slot0Configs module3SteerGains = new Slot0Configs()
            .withKP(84.86).withKI(0).withKD(0.5)
            .withKS(0.056407).withKV(3.1383).withKA(0.29554)
            .withStaticFeedforwardSign(StaticFeedforwardSignValue.UseClosedLoopSign);
    // When using closed-loop control, the drive motor uses the control
    // output type specified by SwerveModuleConstants.DriveMotorClosedLoopOutput
    private static final Slot0Configs module3DriveGains = new Slot0Configs()
            .withKP(0.35523).withKI(0).withKD(0)
            .withKS(0.2072).withKV(0.12913).withKA(0.010622);

    // The closed-loop output type to use for the steer motors;
    // This affects the PID/FF gains for the steer motors
    private static final ClosedLoopOutputType STEER_CLOSED_LOOP_OUTPUT = ClosedLoopOutputType.Voltage;
    // The closed-loop output type to use for the drive motors;
    // This affects the PID/FF gains for the drive motors
    private static final ClosedLoopOutputType DRIVE_CLOSED_LOOP_OUTPUT = ClosedLoopOutputType.Voltage;

    // The type of motor used for the drive motor
    private static final DriveMotorArrangement DRIVE_MOTOR_TYPE = DriveMotorArrangement.TalonFX_Integrated;
    // The type of motor used for the drive motor
    private static final SteerMotorArrangement STEER_MOTOR_TYPE = SteerMotorArrangement.TalonFX_Integrated;

    // The remote sensor feedback type to use for the steer motors;
    // When not Pro-licensed, FusedCANcoder/SyncCANcoder automatically fall back to RemoteCANcoder
    private static final SteerFeedbackType STEER_FEEDBACK_TYPE = SteerFeedbackType.FusedCANcoder;

    // The stator current at which the wheels start to slip;
    // This needs to be tuned to your individual robot
    private static final Current SLIP_CURRENT = Amps.of(120.0);

    // Initial configs for the drive and steer motors and the azimuth encoder; these cannot be null.
    // Some configs will be overwritten; check the `with*InitialConfigs()` API documentation.
    private static final TalonFXConfiguration driveInitialConfigs = new TalonFXConfiguration();
    private static final TalonFXConfiguration steerInitialConfigs = new TalonFXConfiguration()
            .withCurrentLimits(
                    new CurrentLimitsConfigs()
                            // Swerve azimuth does not require much torque output, so we can set a relatively low
                            // stator current limit to help avoid brownouts without impacting performance.
                            .withStatorCurrentLimit(Amps.of(60))
                            .withStatorCurrentLimitEnable(true)
            );
    private static final CANcoderConfiguration encoderInitialConfigs = new CANcoderConfiguration();
    // Configs for the Pigeon 2; leave this null to skip applying Pigeon 2 configs
    private static final Pigeon2Configuration pigeonConfigs = null;

    // Theoretical free speed (m/s) at 12 V applied output;
    // This needs to be tuned to your individual robot
    public static final LinearVelocity SPEED_AT_12_VOLTS = MetersPerSecond.of(4.73);

    // Every 1 rotation of the azimuth results in COUPLE_RATIO drive motor turns;
    // This may need to be tuned to your individual robot
    private static final double COUPLE_RATIO = 4.1666666666666666666666666666667;

    private static final double DRIVE_GEAR_RATIO = 6.75;
    private static final double STEER_GEAR_RATIO = 25;
    private static final Distance WHEEL_RADIUS = Inches.of(2);

    private static final boolean INVERT_LEFT_SIDE = false;
    private static final boolean INVERT_RIGHT_SIDE = true;

    private static final int PIGEON_ID = 40;

    // These are only used for simulation
    private static final MomentOfInertia STEER_INERTIA = KilogramSquareMeters.of(0.01);
    private static final MomentOfInertia DRIVE_INERTIA = KilogramSquareMeters.of(0.01);
    // Simulated voltage necessary to overcome friction
    private static final Voltage STEER_FRICTION_VOLTAGE = Volts.of(0.2);
    private static final Voltage DRIVE_FRICTION_VOLTAGE = Volts.of(0.2);

    public static final SwerveDrivetrainConstants DrivetrainConstants = new SwerveDrivetrainConstants()
            .withCANBusName(Constants.canivore.getName())
            .withPigeon2Id(PIGEON_ID)
            .withPigeon2Configs(pigeonConfigs);

    private static final SwerveModuleConstantsFactory<TalonFXConfiguration, TalonFXConfiguration, CANcoderConfiguration> ConstantCreatorModule0 =
            new SwerveModuleConstantsFactory<TalonFXConfiguration, TalonFXConfiguration, CANcoderConfiguration>()
                    .withDriveMotorGearRatio(DRIVE_GEAR_RATIO)
                    .withSteerMotorGearRatio(STEER_GEAR_RATIO)
                    .withCouplingGearRatio(COUPLE_RATIO)
                    .withWheelRadius(WHEEL_RADIUS)
                    .withSteerMotorGains(module0SteerGains)
                    .withDriveMotorGains(module0DriveGains)
                    .withSteerMotorClosedLoopOutput(STEER_CLOSED_LOOP_OUTPUT)
                    .withDriveMotorClosedLoopOutput(DRIVE_CLOSED_LOOP_OUTPUT)
                    .withSlipCurrent(SLIP_CURRENT)
                    .withSpeedAt12Volts(SPEED_AT_12_VOLTS)
                    .withDriveMotorType(DRIVE_MOTOR_TYPE)
                    .withSteerMotorType(STEER_MOTOR_TYPE)
                    .withFeedbackSource(STEER_FEEDBACK_TYPE)
                    .withDriveMotorInitialConfigs(driveInitialConfigs)
                    .withSteerMotorInitialConfigs(steerInitialConfigs)
                    .withEncoderInitialConfigs(encoderInitialConfigs)
                    .withSteerInertia(STEER_INERTIA)
                    .withDriveInertia(DRIVE_INERTIA)
                    .withSteerFrictionVoltage(STEER_FRICTION_VOLTAGE)
                    .withDriveFrictionVoltage(DRIVE_FRICTION_VOLTAGE);

    private static final SwerveModuleConstantsFactory<TalonFXConfiguration, TalonFXConfiguration, CANcoderConfiguration> ConstantCreatorModule1 =
            new SwerveModuleConstantsFactory<TalonFXConfiguration, TalonFXConfiguration, CANcoderConfiguration>()
                    .withDriveMotorGearRatio(DRIVE_GEAR_RATIO)
                    .withSteerMotorGearRatio(STEER_GEAR_RATIO)
                    .withCouplingGearRatio(COUPLE_RATIO)
                    .withWheelRadius(WHEEL_RADIUS)
                    .withSteerMotorGains(module1SteerGains)
                    .withDriveMotorGains(module1DriveGains)
                    .withSteerMotorClosedLoopOutput(STEER_CLOSED_LOOP_OUTPUT)
                    .withDriveMotorClosedLoopOutput(DRIVE_CLOSED_LOOP_OUTPUT)
                    .withSlipCurrent(SLIP_CURRENT)
                    .withSpeedAt12Volts(SPEED_AT_12_VOLTS)
                    .withDriveMotorType(DRIVE_MOTOR_TYPE)
                    .withSteerMotorType(STEER_MOTOR_TYPE)
                    .withFeedbackSource(STEER_FEEDBACK_TYPE)
                    .withDriveMotorInitialConfigs(driveInitialConfigs)
                    .withSteerMotorInitialConfigs(steerInitialConfigs)
                    .withEncoderInitialConfigs(encoderInitialConfigs)
                    .withSteerInertia(STEER_INERTIA)
                    .withDriveInertia(DRIVE_INERTIA)
                    .withSteerFrictionVoltage(STEER_FRICTION_VOLTAGE)
                    .withDriveFrictionVoltage(DRIVE_FRICTION_VOLTAGE);

    private static final SwerveModuleConstantsFactory<TalonFXConfiguration, TalonFXConfiguration, CANcoderConfiguration> ConstantCreatorModule2 =
            new SwerveModuleConstantsFactory<TalonFXConfiguration, TalonFXConfiguration, CANcoderConfiguration>()
                    .withDriveMotorGearRatio(DRIVE_GEAR_RATIO)
                    .withSteerMotorGearRatio(STEER_GEAR_RATIO)
                    .withCouplingGearRatio(COUPLE_RATIO)
                    .withWheelRadius(WHEEL_RADIUS)
                    .withSteerMotorGains(module2SteerGains)
                    .withDriveMotorGains(module2DriveGains)
                    .withSteerMotorClosedLoopOutput(STEER_CLOSED_LOOP_OUTPUT)
                    .withDriveMotorClosedLoopOutput(DRIVE_CLOSED_LOOP_OUTPUT)
                    .withSlipCurrent(SLIP_CURRENT)
                    .withSpeedAt12Volts(SPEED_AT_12_VOLTS)
                    .withDriveMotorType(DRIVE_MOTOR_TYPE)
                    .withSteerMotorType(STEER_MOTOR_TYPE)
                    .withFeedbackSource(STEER_FEEDBACK_TYPE)
                    .withDriveMotorInitialConfigs(driveInitialConfigs)
                    .withSteerMotorInitialConfigs(steerInitialConfigs)
                    .withEncoderInitialConfigs(encoderInitialConfigs)
                    .withSteerInertia(STEER_INERTIA)
                    .withDriveInertia(DRIVE_INERTIA)
                    .withSteerFrictionVoltage(STEER_FRICTION_VOLTAGE)
                    .withDriveFrictionVoltage(DRIVE_FRICTION_VOLTAGE);

    private static final SwerveModuleConstantsFactory<TalonFXConfiguration, TalonFXConfiguration, CANcoderConfiguration> ConstantCreatorModule3 =
            new SwerveModuleConstantsFactory<TalonFXConfiguration, TalonFXConfiguration, CANcoderConfiguration>()
                    .withDriveMotorGearRatio(DRIVE_GEAR_RATIO)
                    .withSteerMotorGearRatio(STEER_GEAR_RATIO)
                    .withCouplingGearRatio(COUPLE_RATIO)
                    .withWheelRadius(WHEEL_RADIUS)
                    .withSteerMotorGains(module3SteerGains)
                    .withDriveMotorGains(module3DriveGains)
                    .withSteerMotorClosedLoopOutput(STEER_CLOSED_LOOP_OUTPUT)
                    .withDriveMotorClosedLoopOutput(DRIVE_CLOSED_LOOP_OUTPUT)
                    .withSlipCurrent(SLIP_CURRENT)
                    .withSpeedAt12Volts(SPEED_AT_12_VOLTS)
                    .withDriveMotorType(DRIVE_MOTOR_TYPE)
                    .withSteerMotorType(STEER_MOTOR_TYPE)
                    .withFeedbackSource(STEER_FEEDBACK_TYPE)
                    .withDriveMotorInitialConfigs(driveInitialConfigs)
                    .withSteerMotorInitialConfigs(steerInitialConfigs)
                    .withEncoderInitialConfigs(encoderInitialConfigs)
                    .withSteerInertia(STEER_INERTIA)
                    .withDriveInertia(DRIVE_INERTIA)
                    .withSteerFrictionVoltage(STEER_FRICTION_VOLTAGE)
                    .withDriveFrictionVoltage(DRIVE_FRICTION_VOLTAGE);


    // Front Left
    private static final int kFrontLeftDriveMotorId = 20;
    private static final int kFrontLeftSteerMotorId = 10;
    private static final int kFrontLeftEncoderId = 30;
    private static final Angle kFrontLeftEncoderOffset = Rotations.of(0.435546875);
    private static final boolean kFrontLeftSteerMotorInverted = false;
    private static final boolean kFrontLeftEncoderInverted = false;

    private static final Distance kFrontLeftXPos = Inches.of(10.5);
    private static final Distance kFrontLeftYPos = Inches.of(13.45);

    // Front Right
    private static final int kFrontRightDriveMotorId = 21;
    private static final int kFrontRightSteerMotorId = 11;
    private static final int kFrontRightEncoderId = 31;
    private static final Angle kFrontRightEncoderOffset = Rotations.of(-0.293701171875);
    private static final boolean kFrontRightSteerMotorInverted = false;
    private static final boolean kFrontRightEncoderInverted = false;

    private static final Distance kFrontRightXPos = Inches.of(10.5);
    private static final Distance kFrontRightYPos = Inches.of(-13.45);

    // Back Left
    private static final int kBackLeftDriveMotorId = 22;
    private static final int kBackLeftSteerMotorId = 12;
    private static final int kBackLeftEncoderId = 32;
    private static final Angle kBackLeftEncoderOffset = Rotations.of(0.226318359375);
    private static final boolean kBackLeftSteerMotorInverted = false;
    private static final boolean kBackLeftEncoderInverted = false;

    private static final Distance kBackLeftXPos = Inches.of(-10.5);
    private static final Distance kBackLeftYPos = Inches.of(13.45);

    // Back Right
    private static final int kBackRightDriveMotorId = 23;
    private static final int kBackRightSteerMotorId = 13;
    private static final int kBackRightEncoderId = 33;
    private static final Angle kBackRightEncoderOffset = Rotations.of(-0.199951171875);
    private static final boolean kBackRightSteerMotorInverted = false;
    private static final boolean kBackRightEncoderInverted = false;

    private static final Distance kBackRightXPos = Inches.of(-10.5);
    private static final Distance kBackRightYPos = Inches.of(-13.45);


    public static final SwerveModuleConstants<TalonFXConfiguration, TalonFXConfiguration, CANcoderConfiguration> FrontLeft =
            ConstantCreatorModule0.createModuleConstants(
                    kFrontLeftSteerMotorId, kFrontLeftDriveMotorId, kFrontLeftEncoderId, kFrontLeftEncoderOffset,
                    kFrontLeftXPos, kFrontLeftYPos, INVERT_LEFT_SIDE, kFrontLeftSteerMotorInverted, kFrontLeftEncoderInverted
            );
    public static final SwerveModuleConstants<TalonFXConfiguration, TalonFXConfiguration, CANcoderConfiguration> FrontRight =
            ConstantCreatorModule1.createModuleConstants(
                    kFrontRightSteerMotorId, kFrontRightDriveMotorId, kFrontRightEncoderId, kFrontRightEncoderOffset,
                    kFrontRightXPos, kFrontRightYPos, INVERT_RIGHT_SIDE, kFrontRightSteerMotorInverted, kFrontRightEncoderInverted
            );
    public static final SwerveModuleConstants<TalonFXConfiguration, TalonFXConfiguration, CANcoderConfiguration> BackLeft =
            ConstantCreatorModule2.createModuleConstants(
                    kBackLeftSteerMotorId, kBackLeftDriveMotorId, kBackLeftEncoderId, kBackLeftEncoderOffset,
                    kBackLeftXPos, kBackLeftYPos, INVERT_LEFT_SIDE, kBackLeftSteerMotorInverted, kBackLeftEncoderInverted
            );
    public static final SwerveModuleConstants<TalonFXConfiguration, TalonFXConfiguration, CANcoderConfiguration> BackRight =
            ConstantCreatorModule3.createModuleConstants(
                    kBackRightSteerMotorId, kBackRightDriveMotorId, kBackRightEncoderId, kBackRightEncoderOffset,
                    kBackRightXPos, kBackRightYPos, INVERT_RIGHT_SIDE, kBackRightSteerMotorInverted, kBackRightEncoderInverted
            );

    public static final LinearVelocity maxVelocity = CommandSwerveDriveConstants.SPEED_AT_12_VOLTS; // SPEED_AT_12_VOLTS desired top speed
    public static final AngularVelocity MaxAngularRate = RotationsPerSecond.of(0.75); // 3/4 of a rotation per second max angular velocity

    private static final PhoenixPIDController pathXController = new PhoenixPIDController(10, 0, 0);
    private static final PhoenixPIDController pathYController = new PhoenixPIDController(10, 0, 0);
    public static final PhoenixPIDController pathThetaController = new PhoenixPIDController(5.9918340044856690519902612191937, 0, 0);

    private static final SwerveDriveTelemetry swerveDriveTelemetry = new SwerveDriveTelemetry(maxVelocity);

    private static final SwerveDrivetrain<TalonFX, TalonFX, CANcoder> swerveDriveTrain = new SwerveDrivetrain<>(
            TalonFX::new,
            TalonFX::new,
            CANcoder::new,
            DrivetrainConstants, FrontLeft, FrontRight, BackLeft, BackRight);

    private static final PhotonCamera photonCamera0 = new PhotonCamera(OurCameraConstants.Camera0.name);
    private static final PhotonCamera photonCamera1 = new PhotonCamera(OurCameraConstants.Camera1.name);
    private static final PhotonCamera photonCamera2 = new PhotonCamera(OurCameraConstants.Camera2.name);
    private static final PhotonVisionCamera camera0 = new PhotonVisionCamera(OurCameraConstants.Camera0.constants, photonCamera0);
    private static final PhotonVisionCamera camera1 = new PhotonVisionCamera(OurCameraConstants.Camera1.constants, photonCamera1);
    private static final PhotonVisionCamera camera2 = new PhotonVisionCamera(OurCameraConstants.Camera2.constants, photonCamera2);


    private static final CTRESwerveDrive CTRE_SWERVE_DRIVE = new CTRESwerveDrive(
            swerveDriveTrain,
            pathXController,
            pathYController,
            pathThetaController,
            swerveDriveTelemetry,
            maxVelocity,
            MaxAngularRate,
            camera0,
            camera1,
            camera2);

    /**
     * Creates a CommandSwerveDrivetrain instance.
     * This should only be called once in your robot program,
     */
    public static CommandSwerveDrive createCommandSwerve() {
        return new CommandSwerveDrive(
                CTRE_SWERVE_DRIVE,
                simLoopPeriod);
    }

    static {
        pathThetaController.enableContinuousInput(-Math.PI, Math.PI);
    }


}