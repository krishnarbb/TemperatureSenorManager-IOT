
package messages.sensor;

public class RespondMetaData {

    private final long requestId;
    private final String floorId;
    private final String sensorId;

    public RespondMetaData(final long requestId, final String floorId, final String sensorId) {
        super();
        this.requestId = requestId;
        this.floorId = floorId;
        this.sensorId = sensorId;
    }

    public long getRequestId() {
        return requestId;
    }

    public String getFloorId() {
        return floorId;
    }

    public String getSensorId() {
        return sensorId;
    }

}
