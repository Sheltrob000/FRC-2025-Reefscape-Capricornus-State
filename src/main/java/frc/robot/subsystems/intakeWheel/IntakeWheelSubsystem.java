package frc.robot.subsystems.intakeWheel;

import com.ctre.phoenix6.Utils;
import edu.wpi.first.units.measure.Time;
import edu.wpi.first.wpilibj.Notifier;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Subsystem;
import digilib.intakeWheel.IntakeWheel;

import java.util.function.DoubleSupplier;

public class IntakeWheelSubsystem implements Subsystem {
    private final IntakeWheel intakeWheel;
    private double lastSimTime;
    private final Time simLoopPeriod;

    public IntakeWheelSubsystem(
            IntakeWheel intakeWheel,
            Time simLoopPeriod) {
        this.intakeWheel = intakeWheel;
        this.simLoopPeriod = simLoopPeriod;
        if (Utils.isSimulation()) {
            startSimThread();
        }
    }

    public Command toVelocity(DoubleSupplier scalarSetpoint) {
        return run(() -> intakeWheel.applyMotorEncoderVelocity(scalarSetpoint.getAsDouble()))
                .withName(String.format("%s: VELOCITY", getName()));
    }

    public Command toVoltage(double volts) {
        return run(() -> intakeWheel.applyVolts(volts))
                .withName(String.format("%s: VOLTAGE", getName()));
    }

    @Override
    public void periodic() {
        intakeWheel.update();
    }

    @SuppressWarnings("resource")
    private void startSimThread() {
        lastSimTime = Utils.getCurrentTimeSeconds();

        new Notifier(() -> {
            final double currentTime = Utils.getCurrentTimeSeconds();
            double deltaTime = currentTime - lastSimTime;
            lastSimTime = currentTime;
            intakeWheel.updateSimState(deltaTime, RobotController.getBatteryVoltage());
        }).startPeriodic(simLoopPeriod.baseUnitMagnitude());
    }
}
