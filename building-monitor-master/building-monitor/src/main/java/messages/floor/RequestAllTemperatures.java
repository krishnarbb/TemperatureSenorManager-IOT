
package messages.floor;

public class RequestAllTemperatures {
    private final long requestId;

    public RequestAllTemperatures(final long requestId) {
        this.requestId = requestId;
    }

    public long getRequestId() {
        return requestId;
    }
}
