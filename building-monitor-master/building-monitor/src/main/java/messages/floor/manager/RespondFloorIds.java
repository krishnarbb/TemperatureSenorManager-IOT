
package messages.floor.manager;

import java.util.Set;

public class RespondFloorIds {
    private final long requestId;
    private final Set<String> floorIds;

    public RespondFloorIds(final long requestId, final Set<String> floorIds) {
        this.requestId = requestId;
        this.floorIds = floorIds;
    }

    public long getRequestId() {
        return requestId;
    }

    public Set<String> getFloorIds() {
        return floorIds;
    }

}
