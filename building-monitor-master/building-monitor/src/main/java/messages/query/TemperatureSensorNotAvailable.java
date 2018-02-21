
package messages.query;

public class TemperatureSensorNotAvailable implements ITemperatureQueryReading {
    private static TemperatureSensorNotAvailable Instance = null;

    private TemperatureSensorNotAvailable() {}

    public static TemperatureSensorNotAvailable getInstance() {
        if (Instance == null) {
            Instance = new TemperatureSensorNotAvailable();
        }
        return Instance;
    }
}
