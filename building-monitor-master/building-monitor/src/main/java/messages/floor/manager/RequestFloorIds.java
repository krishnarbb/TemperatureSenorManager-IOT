
package messages.floor.manager;

public class RequestFloorIds {
    private final long requestId;

    public RequestFloorIds(final long requestId) {
        this.requestId = requestId;
    }

    public long getRequestId() {
        return requestId;
    }

}
