package digilib.lidarSensor;

import au.grapplerobotics.LaserCan;
import au.grapplerobotics.interfaces.LaserCanInterface;
import au.grapplerobotics.interfaces.LaserCanInterface.Measurement;
import au.grapplerobotics.simulation.MockLaserCan;
import edu.wpi.first.wpilibj.Alert;
import edu.wpi.first.wpilibj.RobotBase;

import java.util.function.IntSupplier;

import static au.grapplerobotics.interfaces.LaserCanInterface.LASERCAN_STATUS_VALID_MEASUREMENT;
import static edu.wpi.first.wpilibj.Alert.AlertType.*;

public class LaserCanLidarSensor implements LidarSensor {
    private final LidarSensorState state = new LidarSensorState();
    private final LidarSensorTelemetry telemetry;
    private final LaserCanInterface laserCan;
    private final Alert alert = new Alert("Error", kError);
    private MockLaserCan laserCanSim = null;
    private IntSupplier simDistanceMeters = null;

    public LaserCanLidarSensor(
            LidarSensorConstants constants,
            LaserCan laserCan) {
        this.laserCan = laserCan;
        this.telemetry = new LidarSensorTelemetry(constants.name());

        if (RobotBase.isSimulation()) {
            laserCanSim = new MockLaserCan();
            simDistanceMeters = constants.simDistanceSupplier();
        }
    }

    @Override
    public LidarSensorState getState() {
        return state;
    }

    @Override
    public void update() {
        updateState();
        updateTelemetry();
    }

    @Override
    public void updateState() {
        if (RobotBase.isReal()) {
            Measurement measurement = laserCan.getMeasurement();
            if (measurement != null && measurement.status == LaserCanInterface.LASERCAN_STATUS_VALID_MEASUREMENT) {
                double meters = measurement.distance_mm / 1000.0;
                state.setDistanceMeters(meters);
                alert.set(false);
            } else if (measurement == null) {
                state.setDistanceMeters(Double.NaN);
                alert.setText("LaserCan measurement is null");
                alert.set(true);
            } else if (measurement.status != LaserCanInterface.LASERCAN_STATUS_VALID_MEASUREMENT) {
                state.setDistanceMeters(Double.NaN);
                alert.setText("LaserCan measurement is not valid");
                alert.set(true);
            }
        } else {
            double meters = laserCan.getMeasurement().distance_mm / 1000.0;
            state.setDistanceMeters(meters);
            alert.set(false);
        }
    }

    @Override
    public void updateTelemetry() {
        telemetry.telemeterize(state);
    }

    @Override
    public void updateSimState() {
        laserCanSim.setMeasurementFullSim(new Measurement(
                LASERCAN_STATUS_VALID_MEASUREMENT,
                simDistanceMeters.getAsInt(),
                0,
                true,
                100,
                new LaserCanInterface.RegionOfInterest(0, 0, 16, 16)));
    }
}
