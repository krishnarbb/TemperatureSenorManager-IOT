
package messages.sensor;

public class RespondTemperatureUpdated {
    private final long requestId;

    public RespondTemperatureUpdated(final long requestId) {
        this.requestId = requestId;
    }

    public long getRequestId() {
        return requestId;
    }

}
