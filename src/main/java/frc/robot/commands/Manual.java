package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.button.Trigger;
import frc.robot.Manipulator;

import java.util.function.DoubleSupplier;

import static edu.wpi.first.wpilibj2.command.Commands.either;

public class Manual {

    private final Manipulator manipulator;
    public final Trigger hasCoral;

    public Manual(Manipulator manipulator) {
        this.manipulator = manipulator;
        hasCoral = manipulator.hasCoral();
    }

    public Command manualArm(DoubleSupplier setpointScalar) {
        return manipulator.arm().toVelocity(setpointScalar);
    }

    public Command manualElevator(DoubleSupplier setpointScalar) {
        return manipulator.elevator().toVelocity(setpointScalar);
    }

    public Command manualElevatorDangerous(DoubleSupplier setpointScalar){
        return manipulator.elevator().toVoltage(setpointScalar);
    }

    public Command manualWristToggle() {
        return either(
                manipulator.wrist().toAngle(90.0),
                manipulator.wrist().toAngle(0.0),
                manipulator.wrist().inRange(-10.0, 45.0))
                .withName("Manual Wrist Toggle");
    }

    public Command manualWrist(DoubleSupplier setpointScalar) {
        return manipulator.wrist().toVelocity(setpointScalar);
    }

    public Command manualCoralIntake(DoubleSupplier setpointScalar) {
        return manipulator.coralIntakeWheel().toVelocity(setpointScalar);
    }

    public Command manualAlgaeClawToggle() {
        return manipulator.algaeClaw().toggle();
    }

    public Command manualCoralClawToggle() {
        return manipulator.coralClaw().toggle();
    }
}
