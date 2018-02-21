
package messages.query;

import java.util.Map;

public class RespondAllTemperatures {

    private final long requestId;
    private final Map<String, ITemperatureQueryReading> temperatureReadings;

    public RespondAllTemperatures(final long requestId, final Map<String, ITemperatureQueryReading> temperatureReadings) {
        this.requestId = requestId;
        this.temperatureReadings = temperatureReadings;
    }

    public long getRequestId() {
        return requestId;
    }

    public Map<String, ITemperatureQueryReading> getTemperatureReadings() {
        return temperatureReadings;
    }

}
