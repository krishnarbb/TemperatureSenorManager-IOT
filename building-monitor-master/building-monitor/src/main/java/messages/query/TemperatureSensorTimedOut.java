
package messages.query;

public class TemperatureSensorTimedOut implements ITemperatureQueryReading {
    private static TemperatureSensorTimedOut Instance = null;

    private TemperatureSensorTimedOut() {}

    public static TemperatureSensorTimedOut getInstance() {
        if (Instance == null) {
            Instance = new TemperatureSensorTimedOut();
        }
        return Instance;
    }
}
