
package messages.floor;

import java.util.Set;

public class RespondTemperatureSensorIds {
    private final long requestId;
    private final Set<String> sensorIds;

    public RespondTemperatureSensorIds(final long requestId, final Set<String> sensorIds) {
        this.requestId = requestId;
        this.sensorIds = sensorIds;
    }

    public long getRequestId() {
        return requestId;
    }

    public Set<String> getSensorIds() {
        return sensorIds;
    }

}
