package digilib.cameras;

import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.math.MatBuilder;
import edu.wpi.first.math.Matrix;
import edu.wpi.first.math.Nat;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.math.numbers.N1;
import edu.wpi.first.math.numbers.N3;
import edu.wpi.first.units.measure.MutTime;
import edu.wpi.first.wpilibj.Timer;
import org.photonvision.EstimatedRobotPose;
import org.photonvision.PhotonCamera;
import org.photonvision.PhotonPoseEstimator;
import org.photonvision.targeting.PhotonTrackedTarget;

import java.util.List;
import java.util.Optional;

import static digilib.cameras.CameraState.*;
import static digilib.cameras.CameraState.CameraMode.*;
import static edu.wpi.first.units.Units.Seconds;

public class PhotonVisionCamera implements Camera {

    private final CameraState state = new CameraState();
    private final Transform3d robotToCamera;
    private final Transform3d cameraToRobot;
    private final AprilTagFieldLayout fieldTags;
    private CameraRequest cameraRequest;
    private final CameraTelemetry telemetry;
    private final PhotonCamera photonCamera;
    private final PhotonPoseEstimator photonPoseEstimator;
    private final Matrix<N3, N1> poseStdDev = MatBuilder.fill(Nat.N3(), Nat.N1(), Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);


    public PhotonVisionCamera(CameraConstants cameraConstants, PhotonCamera photonCamera) {
        this.photonCamera = photonCamera;
        this.robotToCamera = cameraConstants.robotToCamera();
        this.cameraToRobot = robotToCamera.inverse();
        this.fieldTags = cameraConstants.aprilTagFieldLayout();
        photonPoseEstimator = new PhotonPoseEstimator(
                fieldTags,
                cameraConstants.primaryStrategy(),
                robotToCamera);
        photonPoseEstimator.setMultiTagFallbackStrategy(cameraConstants.fallBackPoseStrategy());
        telemetry = new CameraTelemetry(
                cameraConstants.name(),
                cameraConstants.robotToCamera(),
                cameraConstants.primaryStrategy(),
                cameraConstants.fallBackPoseStrategy());
    }

    @Override
    public CameraState getState() {
        return state;
    }

    @Override
    public void setControl(CameraRequest request) {
        if (cameraRequest != request) {
            cameraRequest = request;
        }
        request.apply(this);
    }

    @Override
    public void setRobotPoseMode() {
        photonCamera.setPipelineIndex(ROBOT_POSE.getPipelineIndex());
    }

    @Override
    public void update() {
        updateState();
        updateTelemetry();
    }

    public void updateState() {
        Pose3d pose3d = null;
        MutTime timestamp = Seconds.mutable(Double.NaN);
        for (var change : photonCamera.getAllUnreadResults()) {
            Optional<EstimatedRobotPose> estimatedRobotPose = photonPoseEstimator.update(change);
            pose3d = estimatedRobotPose.map(x -> x.estimatedPose).orElse(null);
            updateEstimationStdDevs(
                    pose3d,
                    change.getTargets());
            estimatedRobotPose.ifPresentOrElse(
                    est -> timestamp.mut_setMagnitude(est.timestampSeconds),
                    () -> timestamp.mut_setMagnitude(Double.NaN));
        }
        state.withCameraMode(getCameraMode())
                .withRobotPose(null)
                .withTimestamp(Timer.getFPGATimestamp());
    }


    @Override
    public void updateTelemetry() {
        telemetry.telemeterize(state);
    }

    private CameraMode getCameraMode() {
        return CameraMode.get(photonCamera.getPipelineIndex());
    }

    private void updateEstimationStdDevs(
            Pose3d estimatedRobotPose,
            List<PhotonTrackedTarget> targets) {
        if (estimatedRobotPose == null || targets.isEmpty()) {
            poseStdDev.set(0, 0, Double.MAX_VALUE);
            poseStdDev.set(1, 0, Double.MAX_VALUE);
            poseStdDev.set(2, 0, Double.MAX_VALUE);
        }

        Pose2d estRobotPose2d = estimatedRobotPose.toPose2d();
        List<Pose2d> targetPose2ds = targets.stream()
                .map(target -> {
                    Transform3d cameraToTarget = target.bestCameraToTarget;
                    return new Pose3d()
                            .plus(cameraToTarget)
                            .relativeTo(fieldTags.getOrigin())
                            .plus(cameraToRobot)
                            .toPose2d();
                }).toList();
        double estRobotPoseX = estRobotPose2d.getX();
        double estRobotPoseY = estRobotPose2d.getY();
        double estRobotPose2dThetaRad = estRobotPose2d.getRotation().getRadians();
        double numberOfTargets = targetPose2ds.size();
        double xStdDev = targetPose2ds.stream()
                .map(targetPose -> Math.pow(targetPose.getX() - estRobotPoseX, 2))
                .reduce(0.0, Double::sum)
                / (numberOfTargets - 1);
        xStdDev = Math.sqrt(xStdDev);
        double yStdDev = targetPose2ds.stream()
                .map(targetPose -> Math.pow(targetPose.getY() - estRobotPoseY, 2))
                .reduce(0.0, Double::sum)
                / (numberOfTargets - 1);
        yStdDev = Math.sqrt(yStdDev);
        double thetaStdDev = targetPose2ds.stream()
                .map(targetPose -> Math.pow(targetPose.getRotation().getRadians() - estRobotPose2dThetaRad, 2))
                .reduce(0.0, Double::sum)
                / (numberOfTargets - 1);
        thetaStdDev = Math.sqrt(thetaStdDev);
        poseStdDev.set(0, 0, xStdDev);
        poseStdDev.set(1, 0, yStdDev);
        poseStdDev.set(2, 0, thetaStdDev);
    }
}
