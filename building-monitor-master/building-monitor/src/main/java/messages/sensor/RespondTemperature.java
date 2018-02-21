
package messages.sensor;

public class RespondTemperature {
    private final long requestId;
    private final double temperature;

    public RespondTemperature(final long requestId, final double temperature) {
        this.requestId = requestId;
        this.temperature = temperature;
    }

    public long getRequestId() {
        return requestId;
    }

    public double getTemperature() {
        return temperature;
    }

}
