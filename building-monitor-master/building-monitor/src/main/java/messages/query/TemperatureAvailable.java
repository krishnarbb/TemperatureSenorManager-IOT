
package messages.query;

public class TemperatureAvailable implements ITemperatureQueryReading {

    private final double temperature;

    public TemperatureAvailable(final double temperature) {
        this.temperature = temperature;
    }

    public double getTemperature() {
        return temperature;
    }

}
