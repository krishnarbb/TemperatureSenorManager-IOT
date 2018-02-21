
package messages.query;

public class NoTemperatureReadingRecordedYet implements ITemperatureQueryReading {
    private static NoTemperatureReadingRecordedYet Instance = null;

    private NoTemperatureReadingRecordedYet() {}

    public static NoTemperatureReadingRecordedYet getInstance() {
        if (Instance == null) {
            Instance = new NoTemperatureReadingRecordedYet();
        }
        return Instance;
    }
}
