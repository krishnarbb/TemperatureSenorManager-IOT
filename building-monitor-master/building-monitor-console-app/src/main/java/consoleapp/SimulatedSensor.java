
package consoleapp;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import akka.actor.ActorRef;
import akka.pattern.Patterns;
import akka.util.Timeout;
import messages.sensor.RequestRegisterTemperatureSensor;
import messages.sensor.RequestUpdateTemperature;
import messages.sensor.RespondSensorRegistered;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

public class SimulatedSensor {
    private final ActorRef floorManager;
    private final Random randomTemperatureGenerator;
    private final String floorId;
    private final String sensorId;
    private ActorRef sensorReference = null;

    public SimulatedSensor(final String floorId, final String sensorId, final ActorRef floorManager) {

        this.floorManager = floorManager;
        this.floorId = floorId;
        this.sensorId = sensorId;
        randomTemperatureGenerator = new Random(Integer.parseInt(sensorId));
    }

    public void connect() throws Exception {
        final Timeout timeout = new Timeout(Duration.create(2, TimeUnit.SECONDS));
        final Future<Object> fresponse =
                Patterns.ask(floorManager, new RequestRegisterTemperatureSensor(1, floorId, sensorId), timeout);
        final RespondSensorRegistered response = (RespondSensorRegistered) Await.result(fresponse, timeout.duration());
        sensorReference = response.getSensorReference();
    }

    public void startSendingSimulatedReadings() {
        double randomTemperature = randomTemperatureGenerator.nextDouble();
        randomTemperature *= 100;

        final Timeout timeout = new Timeout(Duration.create(1, TimeUnit.SECONDS));

        final Future<Object> fresponse =
                Patterns.ask(sensorReference, new RequestUpdateTemperature(0, randomTemperature), timeout);
    }

}
