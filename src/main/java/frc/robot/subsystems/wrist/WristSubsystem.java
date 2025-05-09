package frc.robot.subsystems.wrist;

import com.ctre.phoenix6.Utils;
import edu.wpi.first.units.measure.MutAngle;
import edu.wpi.first.units.measure.Time;
import edu.wpi.first.wpilibj.Notifier;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Subsystem;
import digilib.wrist.Wrist;
import edu.wpi.first.wpilibj2.command.button.Trigger;

import java.util.function.DoubleSupplier;

import static edu.wpi.first.units.Units.Rotations;
import static edu.wpi.first.wpilibj2.command.Commands.sequence;

public class WristSubsystem implements Subsystem {
    private final Wrist wrist;
    private double lastSimTime;
    private final Time simLoopPeriod;

    public WristSubsystem(Wrist wrist, Time simLoopPeriod) {
        this.wrist = wrist;
        this.simLoopPeriod = simLoopPeriod;
        if (Utils.isSimulation()) {
            startSimThread();
        }
    }

    public Trigger gte(double angleDegrees) {
        return new Trigger(() -> wrist
                .getMotorEncoderPositionDegrees() >= angleDegrees);
    }

    public Trigger lte(double angleDegrees) {
        return new Trigger(() -> wrist
                .getMotorEncoderPositionDegrees() <= angleDegrees);
    }

    public Trigger inRange(double minAngleDegrees, double maxAngleDegrees) {
        return gte(minAngleDegrees).and(lte(maxAngleDegrees));
    }

    public Command toAngle(double degrees) {
        return run(() -> wrist.applyPositionRotations(degrees / 360.0))
                .withName(String.format("%s: %.2f deg", getName(), degrees));
    }

    public Command toVelocity(DoubleSupplier scalarSetpoint) {
        return run(() -> wrist.applyVelocity(scalarSetpoint.getAsDouble()))
                .withName(String.format("%s: VELOCITY", getName()));
    }

    public Command hold() {
        MutAngle holdPosition = Rotations.mutable(0.0);
        return sequence(
                runOnce(() -> holdPosition.mut_setMagnitude(wrist.getMotorEncoderPositionRotations())),
                run(() -> wrist.applyPositionRotations(holdPosition.in(Rotations))))
                .withName(String.format("%s: HOLD", getName()));
    }

    @Override
    public void periodic() {
        wrist.update();
    }

    @SuppressWarnings("resource")
    private void startSimThread() {
        lastSimTime = Utils.getCurrentTimeSeconds();

        new Notifier(() -> {
            final double currentTime = Utils.getCurrentTimeSeconds();
            double deltaTime = currentTime - lastSimTime;
            lastSimTime = currentTime;

            wrist.updateSimState(deltaTime, RobotController.getBatteryVoltage());
        }).startPeriodic(simLoopPeriod.baseUnitMagnitude());
    }
}
