package digilib.intakeWheel;

import com.revrobotics.sim.SparkMaxSim;
import com.revrobotics.spark.ClosedLoopSlot;
import com.revrobotics.spark.SparkBase;
import com.revrobotics.spark.SparkClosedLoopController;
import com.revrobotics.spark.SparkMax;
import digilib.MotorControllerType;
import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.math.filter.SlewRateLimiter;
import edu.wpi.first.math.numbers.N1;
import edu.wpi.first.math.system.LinearSystem;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.math.system.plant.LinearSystemId;
import edu.wpi.first.units.measure.*;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.simulation.FlywheelSim;

import static digilib.MotorControllerType.*;
import static edu.wpi.first.units.Units.*;

public class NEO550IntakeWheel implements IntakeWheel {
    private final IntakeWheelState lastState;
    private final IntakeWheelState state;
    private final AngularVelocity maxVelocity;
    private final IntakeWheelTelemetry telemetry;
    private IntakeWheelRequest intakeWheelRequest;
    private final SparkMax motor;
    private final SlewRateLimiter profile;
    private final SimpleMotorFeedforward feedforward;
    private final MutAngularVelocity velocity = RadiansPerSecond.mutable(0.0);
    private final MutTime timestamp = Seconds.mutable(0.0);
    private final MutAngularVelocity goalVelocity = RadiansPerSecond.mutable(0.0);
    private final MutAngularVelocity lastVelocity = RadiansPerSecond.mutable(0.0);
    private final MutVoltage voltage = Volts.mutable(0.0);
    private FlywheelSim flywheelSim = null;
    private SparkMaxSim sparkMaxSim = null;

    public NEO550IntakeWheel(IntakeWheelConstants constants, SparkMax motor) {
        lastState = new IntakeWheelState(constants.getWheelRadius());
        state = new IntakeWheelState(constants.getWheelRadius());
        this.motor = motor;
        this.maxVelocity = constants.getMaxVelocity();
        this.feedforward = new SimpleMotorFeedforward(constants.getKs().baseUnitMagnitude(), constants.getKv().baseUnitMagnitude(), constants.getKa().baseUnitMagnitude(), constants.getUpdatePeriod().baseUnitMagnitude());
        this.profile = new SlewRateLimiter(constants.getMaxAcceleration().baseUnitMagnitude());
        if (RobotBase.isSimulation()) {
            DCMotor dcMotor = DCMotor.getNeo550(1);
            sparkMaxSim = new SparkMaxSim(motor, dcMotor);
            LinearSystem<N1, N1, N1> plant = LinearSystemId.identifyVelocitySystem(
                    constants.getKv().baseUnitMagnitude(),
                    constants.getKa().baseUnitMagnitude());
            flywheelSim = new FlywheelSim(
                    plant,
                    dcMotor);
        }
        this.telemetry = new IntakeWheelTelemetry(
                constants.getName(),
                constants.getWheelRadius(),
                constants.getMaxVelocity(),
                constants.getMaxAcceleration());
    }

    @Override
    public MotorControllerType getMotorControllerType() {
        return REV_SPARK_MAX;
    }

    @Override
    public IntakeWheelState getState() {
        return state;
    }

    @Override
    public AngularVelocity getMaxVelocity() {
        return maxVelocity;
    }

    @Override
    public void setControl(IntakeWheelRequest request) {
        if (intakeWheelRequest != request) {
            intakeWheelRequest = request;
        }
        request.apply(this);
    }

    @Override
    public void setVelocity(AngularVelocity velocity) {
        goalVelocity.mut_replace(velocity);
        double nextVelocitySetpoint = profile.calculate(goalVelocity.baseUnitMagnitude());
        double lastVelocitySetpoint = lastVelocity.baseUnitMagnitude();
        double arbFeedforward = feedforward.calculateWithVelocities(lastVelocitySetpoint, nextVelocitySetpoint);
        motor.getClosedLoopController().setReference(nextVelocitySetpoint, SparkBase.ControlType.kVelocity, ClosedLoopSlot.kSlot1, arbFeedforward, SparkClosedLoopController.ArbFFUnits.kVoltage);
        lastVelocity.mut_setMagnitude(nextVelocitySetpoint);
    }

    @Override
    public void setIdle() {
        lastVelocity.mut_setMagnitude(motor.getEncoder().getVelocity());
        motor.getClosedLoopController().setReference(0.0, SparkBase.ControlType.kVoltage);
    }

    @Override
    public void setVoltage(Voltage voltage) {
        lastVelocity.mut_setMagnitude(motor.getEncoder().getVelocity());
        motor.getClosedLoopController().setReference(voltage.baseUnitMagnitude(), SparkBase.ControlType.kVoltage);
    }

    @Override
    public void update() {
        lastState.withIntakeState(state);
        updateState();
        updateTelemetry();
    }

    @Override
    public void updateState() {
        state.withVelocity(velocity.mut_setMagnitude(motor.getEncoder().getVelocity()))
                .withTimestamp(timestamp.mut_setMagnitude(Timer.getFPGATimestamp()))
                .withVoltage(voltage.mut_setMagnitude(motor.getAppliedOutput() * motor.getBusVoltage()));
    }

    @Override
    public void updateTelemetry() {
        telemetry.telemeterize(state);
    }

    @Override
    public void updateSimState(double dtSeconds, double supplyVoltage) {
        double inputVoltage = motor.getAppliedOutput() * 12.0;
        flywheelSim.setInputVoltage(inputVoltage);
        flywheelSim.update(dtSeconds);
        sparkMaxSim.iterate(flywheelSim.getOutput(0), 12.0, dtSeconds);
    }





}