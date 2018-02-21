
package messages.sensor;

public class RequestUpdateTemperature {
    long requestId;
    double temperature;

    public RequestUpdateTemperature(final long requestId, final double temperature) {
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
