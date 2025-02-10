package frc.robot.subsystems.arm;

import com.ctre.phoenix6.Utils;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.Time;
import edu.wpi.first.wpilibj.Notifier;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Subsystem;
import digilib.arm.Arm;
import digilib.arm.ArmRequest;
import edu.wpi.first.wpilibj2.command.button.Trigger;

import java.util.function.Supplier;

public class CommandArm implements Subsystem {
    private final Arm arm;
    private double lastSimTime;
    private final Time simLoopPeriod;

    public CommandArm(Arm arm, Time simLoopPeriod) {
        this.arm = arm;
        this.simLoopPeriod = simLoopPeriod;
        if (Utils.isSimulation()) {
            startSimThread();
        }
    }

    public Trigger atPosition(Angle position, Angle tolerance) {
        return new Trigger(() -> arm.getState().getPosition().isNear(position, tolerance));
    }

    public Trigger lessThanPosition(Angle position, Angle tolerance) {
        return new Trigger(() -> {
            double currentPositionValue = arm.getState().getPosition().baseUnitMagnitude();
            double positionValue = position.baseUnitMagnitude();
            double diff = Math.abs(currentPositionValue - positionValue);
            if (currentPositionValue < positionValue && diff >= tolerance.magnitude()) {
                return true;
            }
            return false;
        });
    }

    public Command applyRequest(Supplier<ArmRequest> requestSupplier) {
        return run(() -> arm.setControl(requestSupplier.get()))
                .handleInterrupt(arm::disableHold);
    }

    @Override
    public void periodic() {
        arm.update();
    }

    private void startSimThread() {
        lastSimTime = Utils.getCurrentTimeSeconds();

        /* Run simulation at a faster rate so PID gains behave more reasonably */
        /* use the measured time delta, get battery voltage from WPILib */
        Notifier m_simNotifier = new Notifier(() -> {
            final double currentTime = Utils.getCurrentTimeSeconds();
            double deltaTime = currentTime - lastSimTime;
            lastSimTime = currentTime;

            /* use the measured time delta, get battery voltage from WPILib */
            arm.updateSimState(deltaTime, RobotController.getBatteryVoltage());
        });
        m_simNotifier.startPeriodic(simLoopPeriod.baseUnitMagnitude());
    }


}



