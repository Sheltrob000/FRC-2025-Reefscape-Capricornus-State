package digilib.objectDetectors;

import edu.wpi.first.units.measure.*;

import static edu.wpi.first.units.Units.*;

public class ObjectDetectorState {

    private boolean inRange = false;
    private final MutTime timestamp = Seconds.mutable(0.0);

    public boolean isInRange() {
        return inRange;
    }

    public Time getTimestamp() {
        return timestamp;
    }

    public ObjectDetectorState withInRange(boolean inRange) {
        this.inRange = inRange;
        return this;
    }

    public ObjectDetectorState withTimestamp(Time timestamp) {
        this.timestamp.mut_replace(timestamp);
        return this;
    }

    public ObjectDetectorState withState(ObjectDetectorState state) {
        this.inRange = state.inRange;
        this.timestamp.mut_replace(state.timestamp);
        return this;
    }
}
