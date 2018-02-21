
package messages.sensor;

public class RequestMetaData {

    private final long requestId;

    public RequestMetaData(final long requestId) {
        super();
        this.requestId = requestId;
    }

    public long getRequestId() {
        return requestId;
    }

}
