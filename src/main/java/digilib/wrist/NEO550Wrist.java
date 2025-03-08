package digilib.wrist;

import com.ctre.phoenix6.hardware.CANcoder;
import com.ctre.phoenix6.signals.MagnetHealthValue;
import com.ctre.phoenix6.sim.CANcoderSimState;
import com.revrobotics.sim.SparkMaxSim;
import com.revrobotics.spark.*;
import digilib.MotorControllerType;
import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.math.filter.SlewRateLimiter;
import edu.wpi.first.math.numbers.N1;
import edu.wpi.first.math.numbers.N2;
import edu.wpi.first.math.system.LinearSystem;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.math.system.plant.LinearSystemId;
import edu.wpi.first.math.trajectory.ExponentialProfile;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj.simulation.DCMotorSim;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import static digilib.wrist.NEO550Wrist.ControlState.POSITION;
import static digilib.wrist.NEO550Wrist.ControlState.VELOCITY;

public class NEO550Wrist implements Wrist {


    public enum ControlState {
        POSITION,
        VELOCITY,
        VOLTAGE
    }

    private final WristState state = new WristState();
    private final double minAngleRotations;
    private final double maxAngleRotations;
    private final double maxVelocityRPS;
    private final WristTelemetry telemetry;
    private final SparkMax motor;
    private final CANcoder cancoder;
    private ControlState controlState = null;
    private final ExponentialProfile positionProfile;
    private final SlewRateLimiter velocityProfile;
    private final ExponentialProfile.State goal = new ExponentialProfile.State();
    private ExponentialProfile.State setpoint = new ExponentialProfile.State();
    private final SimpleMotorFeedforward feedforward;
    private final PIDController positionPIDController;
    private final PIDController velocityPIDController;
    private final double maxControlVoltage;
    private final double controlPeriodSeconds;
    private DCMotorSim simWrist = null;
    private SparkMaxSim sparkMaxSim = null;
    private CANcoderSimState canCoderSimState = null;

    public NEO550Wrist(
            WristConstants constants,
            SparkMax motor,
            CANcoder cancoder,
            PIDController positionPIDController,
            PIDController velocityPIDController,
            double controlPeriodSeconds) {
        minAngleRotations = constants.minAngleDegrees() / 360.0;
        maxAngleRotations = constants.maxAngleDegrees() / 360.0;
        maxVelocityRPS = constants.maxVelocityRPS();
        this.motor = motor;
        this.cancoder = cancoder;
        this.telemetry = new WristTelemetry(
                constants.name(),
                constants.minAngleDegrees(),
                constants.maxAngleDegrees(),
                constants.maxVelocityRPS(),
                constants.maxAccelerationRPSSquared());
        this.feedforward = new SimpleMotorFeedforward(
                constants.ksVolts(),
                constants.kvVoltsPerRPS(),
                constants.kaVoltsPerRPSSquared(),
                controlPeriodSeconds);
        this.positionPIDController = positionPIDController;
        this.velocityPIDController = velocityPIDController;
        this.positionProfile = new ExponentialProfile(
                ExponentialProfile.Constraints.fromCharacteristics(
                        constants.maxControlVoltage(),
                        constants.kvVoltsPerRPS(),
                        constants.kaVoltsPerRPSSquared()));
        this.velocityProfile = new SlewRateLimiter(constants.maxAccelerationRPSSquared());
        this.maxControlVoltage = constants.maxControlVoltage();
        this.controlPeriodSeconds = controlPeriodSeconds;

        resetPosition();

        if (RobotBase.isSimulation()) {
            DCMotor dcMotor = DCMotor.getNeo550(1);
            sparkMaxSim = new SparkMaxSim(motor, dcMotor);
            canCoderSimState = cancoder.getSimState();
            LinearSystem<N2, N1, N2> plant = LinearSystemId.identifyPositionSystem(
                    constants.kvVoltsPerRPS() / 2 / Math.PI,
                    constants.kaVoltsPerRPSSquared() / 2 / Math.PI);
            simWrist = new DCMotorSim(
                    plant,
                    dcMotor);
            simWrist.setAngle(constants.startingAngleDegrees() / 360.0);
            canCoderSimState.setRawPosition(constants.startingAngleDegrees() / 360.0);
            sparkMaxSim.setPosition(constants.startingAngleDegrees() / 360.0);
        }
    }

    @Override
    public MotorControllerType getMotorControllerType() {
        return MotorControllerType.REV_SPARK_MAX;
    }

    @Override
    public double getMinAngleRotations() {
        return minAngleRotations;
    }

    @Override
    public double getMaxAngleRotations() {
        return maxAngleRotations;
    }

    @Override
    public double getMaxVelocityRPS() {
        return maxVelocityRPS;
    }

    @Override
    public WristState getState() {
        return state;
    }

    @Override
    public void setPosition(double setpointRotations) {
        if (controlState != POSITION) {
            setpoint.position = cancoder.getAbsolutePosition().getValueAsDouble();
            setpoint.velocity = cancoder.getVelocity().getValueAsDouble();
            controlState = POSITION;
        }
        double currentAngleRotations = cancoder.getAbsolutePosition().getValueAsDouble();
        goal.velocity = 0.0;
        if (currentAngleRotations >= maxAngleRotations && setpointRotations > maxVelocityRPS) {
            goal.position = maxAngleRotations;
        } else if (currentAngleRotations <= minAngleRotations && setpointRotations < minAngleRotations) {
            goal.position = minAngleRotations;
        } else {
            goal.position = setpointRotations;
        }
        ExponentialProfile.State next = positionProfile
                .calculate(controlPeriodSeconds, setpoint, goal);
        double feedforwardVolts = feedforward
                .calculateWithVelocities(setpoint.velocity, next.velocity);
        double feedbackVolts = positionPIDController
                .calculate(currentAngleRotations, next.position);
        double volts = MathUtil.clamp(feedbackVolts + feedforwardVolts, -maxControlVoltage, maxControlVoltage);
        motor.setVoltage(volts);
        setpoint = next;
    }

    @Override
    public void setVelocity(double setpointScalar) {
        if (controlState != VELOCITY) {
            velocityProfile.reset(cancoder.getVelocity().getValueAsDouble());
            setpoint.velocity = cancoder.getVelocity().getValueAsDouble();
            controlState = VELOCITY;
        }
        double currentAngleRotations = cancoder.getAbsolutePosition().getValueAsDouble();
        double currentVelocityRPS = cancoder.getVelocity().getValueAsDouble();
        if (currentAngleRotations >= maxAngleRotations && setpointScalar > 0.0){
            setPosition(maxAngleRotations);
        }else if(currentAngleRotations <= minAngleRotations && setpointScalar < 0.0){
            setPosition(minAngleRotations);
        }else{
            goal.velocity = setpointScalar * maxVelocityRPS;
            double nextVelocitySetpoint = velocityProfile.calculate(goal.velocity);
            double feedforwardVolts = feedforward
                    .calculateWithVelocities(setpoint.velocity, nextVelocitySetpoint);
            double feedbackVolts = velocityPIDController
                    .calculate(currentVelocityRPS, nextVelocitySetpoint);
            double volts = MathUtil.clamp(feedbackVolts + feedforwardVolts, -maxControlVoltage, maxControlVoltage);
            motor.setVoltage(volts);
            setpoint.velocity = nextVelocitySetpoint;
        }
    }

    @Override
    public void setVoltage(double volts) {
        if (controlState != ControlState.VOLTAGE) {
            controlState = ControlState.VOLTAGE;
        }
        motor.setVoltage(volts);
    }

    @Override
    public void resetPosition() {
        double absolutePositionRotations = cancoder.getAbsolutePosition().getValueAsDouble();
        absolutePositionRotations = MathUtil.inputModulus(absolutePositionRotations, -0.5, 0.5);
        motor.getEncoder().setPosition(absolutePositionRotations);
    }

    @Override
    public void update() {
        resetPosition();
        updateState();
        updateTelemetry();
    }

    @Override
    public void updateState() {
        state.setMotorEncoderPositionRotations(motor.getEncoder().getPosition());
        state.setAbsoluteEncoderPositionRotations(cancoder.getAbsolutePosition().getValueAsDouble());
        state.setMotorEncoderVelocityRPS(motor.getEncoder().getVelocity());
        state.setAbsoluteEncoderVelocityRPS(cancoder.getVelocity().getValueAsDouble());
        state.setVolts(motor.getAppliedOutput() * motor.getBusVoltage());
        state.setAmps(motor.getOutputCurrent());
        SmartDashboard.putString("Wrist CAN Coder", cancoder.getMagnetHealth().getValue().name());
        state.setAbsoluteEncoderStatus(cancoder.getMagnetHealth().getValue());
    }

    @Override
    public void updateTelemetry() {
        telemetry.telemeterize(state);
    }

    @Override
    public void updateSimState(double dt, double supplyVoltage) {
        var inputVoltage = motor.getAppliedOutput() * 12.0;
        simWrist.setInputVoltage(inputVoltage);
        simWrist.update(dt);

        double rotations = simWrist.getAngularPositionRotations();
        rotations = MathUtil.inputModulus(rotations, -0.5, 0.5);

        canCoderSimState.setSupplyVoltage(supplyVoltage);
        canCoderSimState.setMagnetHealth(MagnetHealthValue.Magnet_Green);
        canCoderSimState.setVelocity(simWrist.getAngularVelocityRPM() / 60.0);
        canCoderSimState.setRawPosition(rotations);

        sparkMaxSim.iterate(simWrist.getAngularVelocityRadPerSec(), 12.0, dt);
    }
}
