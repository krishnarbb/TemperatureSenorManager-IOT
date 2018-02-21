
package messages.sensor;

public class RequestRegisterTemperatureSensor {
    private final long requestId;
    private final String floorId;
    private final String sensorId;

    public RequestRegisterTemperatureSensor(final long requestId, final String floorId, final String sensorId) {
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
