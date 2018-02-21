
package messages.floor;

public class RequestTemperatureSensorIds {
    private final long requestId;

    public RequestTemperatureSensorIds(final long requestId) {
        this.requestId = requestId;
    }

    public long getRequestId() {
        return requestId;
    }

}
