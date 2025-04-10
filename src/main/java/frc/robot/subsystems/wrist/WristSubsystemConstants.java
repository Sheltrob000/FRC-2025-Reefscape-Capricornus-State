package frc.robot.subsystems.wrist;

import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.config.*;
import digilib.wrist.NEO550Wrist;
import digilib.wrist.Wrist;
import edu.wpi.first.units.measure.Time;
import edu.wpi.first.wpilibj.smartdashboard.MechanismLigament2d;

import static com.revrobotics.spark.ClosedLoopSlot.kSlot1;
import static com.revrobotics.spark.SparkBase.PersistMode.kPersistParameters;
import static com.revrobotics.spark.SparkBase.ResetMode.kResetSafeParameters;
import static com.revrobotics.spark.SparkLowLevel.MotorType.kBrushless;
import static com.revrobotics.spark.config.ClosedLoopConfig.FeedbackSensor.kPrimaryEncoder;
import static com.revrobotics.spark.config.SparkBaseConfig.IdleMode.kBrake;
import static digilib.wrist.Wrist.*;
import static edu.wpi.first.units.Units.Seconds;
import static frc.robot.subsystems.wrist.WristSubsystemConstants.Control.*;
import static frc.robot.subsystems.wrist.WristSubsystemConstants.Mechanism.constants;
import static frc.robot.subsystems.wrist.WristSubsystemConstants.Mechanism.reduction;
import static frc.robot.subsystems.wrist.WristSubsystemConstants.Motor.motor;
import static frc.robot.subsystems.wrist.WristSubsystemConstants.Simulation.simLoopPeriod;
import static frc.robot.subsystems.wrist.WristSubsystemConstants.Simulation.startingAngleDegrees;

public class WristSubsystemConstants {

    static final class Control {
        static final double ksVolts = 0.17044;
        static final double kvVoltsPerRPS = 4.9058;
        static final double kaVoltsPerRPSSquared = 0.14377;
        static final double positionKpVoltsPerRotation = 6.5235;
        static final double positionKdVoltsPerRPS = 0.0;

        static final double velocityKpVoltsPerRPS = 5.4797E-07;
        static final double maxControlVoltage = 12.0 - ksVolts;
        static final double maxVelocityRPS = maxControlVoltage / kvVoltsPerRPS;
        static final double maxAccelerationRPSS = maxControlVoltage / kaVoltsPerRPSSquared;
        static final double controlPeriodSeconds = 0.020;
    }

    static final class Simulation {
        static final double startingAngleDegrees = 0.0;
        static final Time simLoopPeriod = Seconds.of(0.001);
    }

    static final class Mechanism {
        static final String name = "Wrist";
        static final double reduction = 4.0 * 3.0 * 5.0 * 32.0 / 24.0;   // 10 7 32.0 / 18
        static final double minAngleDegrees = -95.0;
        static final double maxAngleDegrees = 100.0;
        static final Config constants = new Config(
                name,
                reduction,
                startingAngleDegrees,
                minAngleDegrees,
                maxAngleDegrees,
                maxControlVoltage,
                ksVolts,
                kvVoltsPerRPS,
                kaVoltsPerRPSSquared,
                maxVelocityRPS,
                maxAccelerationRPSS);
    }

    static final class Motor {
        static final int deviceId = 17;
        static final SparkBaseConfig.IdleMode idleMode = kBrake;
        static final boolean inverted = true;
        static final int smartCurrentLimit = 20;
        static final int depth = 2;
        static final int periodMs = 16;
        static final double positionFactor = 1.0 / reduction;
        static final double velocityFactor = 1.0 / reduction / 60.0;
        static final SignalsConfig signalsConfig = new SignalsConfig()
                .absoluteEncoderPositionAlwaysOn(false)
                .absoluteEncoderVelocityAlwaysOn(false)
                .analogPositionAlwaysOn(false)
                .analogVelocityAlwaysOn(false)
                .externalOrAltEncoderPositionAlwaysOn(false)
                .externalOrAltEncoderVelocityAlwaysOn(false)
                .primaryEncoderPositionAlwaysOn(false)
                .primaryEncoderVelocityAlwaysOn(true)
                .primaryEncoderVelocityPeriodMs(20)
                .primaryEncoderVelocityAlwaysOn(true)
                .primaryEncoderVelocityPeriodMs(20);
        static final EncoderConfig encoderConfig = new EncoderConfig()
                .positionConversionFactor(positionFactor)
                .velocityConversionFactor(velocityFactor)
                .quadratureAverageDepth(depth)
                .quadratureMeasurementPeriod(periodMs);
        static final ClosedLoopConfig closedLoopConfig = new ClosedLoopConfig()
                .p(positionKpVoltsPerRotation)
                .d(positionKdVoltsPerRPS)
                .p(velocityKpVoltsPerRPS, kSlot1)
                .feedbackSensor(kPrimaryEncoder);
        static final SparkBaseConfig config = new SparkFlexConfig()
                .idleMode(idleMode)
                .inverted(inverted)
                .smartCurrentLimit(smartCurrentLimit)
                .apply(signalsConfig)
                .apply(encoderConfig)
                .apply(closedLoopConfig);
        static final SparkMax motor = new SparkMax(deviceId, kBrushless);
    }

    public static WristSubsystem create(MechanismLigament2d top, MechanismLigament2d bottom) {
        motor.configure(Motor.config, kResetSafeParameters, kPersistParameters);
        Wrist wrist = new NEO550Wrist(
                constants,
                motor,
                controlPeriodSeconds,
                top,
                bottom);
        WristSubsystem wristSubsystem = new WristSubsystem(wrist, simLoopPeriod);
        wristSubsystem.setDefaultCommand(wristSubsystem.hold());
        return wristSubsystem;
    }
}
