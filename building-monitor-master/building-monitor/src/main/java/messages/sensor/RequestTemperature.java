
package messages.sensor;

public class RequestTemperature {
    private final long requestId;

    public RequestTemperature(final long requestId) {
        this.requestId = requestId;
    }

    public long getRequestId() {
        return requestId;
    }
}
