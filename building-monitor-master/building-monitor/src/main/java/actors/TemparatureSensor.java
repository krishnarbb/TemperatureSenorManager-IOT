
package actors;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;
import messages.sensor.RequestMetaData;
import messages.sensor.RequestRegisterTemperatureSensor;
import messages.sensor.RequestTemperature;
import messages.sensor.RequestUpdateTemperature;
import messages.sensor.RespondMetaData;
import messages.sensor.RespondSensorRegistered;
import messages.sensor.RespondTemperature;
import messages.sensor.RespondTemperatureUpdated;

public class TemparatureSensor extends AbstractLoggingActor {

    private final String floorId;
    private final String sensorId;
    private double lastTemperatureRecorded;

    public static Props props(final String floorId, final String sensorId) {
        return Props.create(TemparatureSensor.class, floorId, sensorId);
    }

    public TemparatureSensor(final String floorId, final String sensorId) {
        super();
        this.floorId = floorId;
        this.sensorId = sensorId;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(RequestMetaData.class, this::onRequestMetaDataMessage)
                .match(RequestTemperature.class, this::onRequestTemperatureMessage)
                .match(RequestUpdateTemperature.class, this::onRequestUpdateTemperatureMessage)
                .match(RequestRegisterTemperatureSensor.class,
                        // predicate
                        m -> m.getFloorId().equalsIgnoreCase(floorId) && m.getSensorId().equalsIgnoreCase(sensorId),
                        // function to be executed
                        this::onRequestRegisterTemperatureSensorMessage)
                .matchAny(m -> unhandled(m))
                .build();

    }

    private void onRequestMetaDataMessage(final RequestMetaData m) {
        log().info("received RequestMetaData");
        sender().tell(new RespondMetaData(m.getRequestId(), floorId, sensorId), self());
    }

    private void onRequestTemperatureMessage(final RequestTemperature m) {
        log().info("received RequestTemperature");
        sender().tell(new RespondTemperature(m.getRequestId(), lastTemperatureRecorded), self());
    }

    private void onRequestUpdateTemperatureMessage(final RequestUpdateTemperature m) {
        log().info("received RequestUpdateTemperature");
        lastTemperatureRecorded = m.getTemperature();
        sender().tell(new RespondTemperatureUpdated(m.getRequestId()), self());
    }

    private void onRequestRegisterTemperatureSensorMessage(final RequestRegisterTemperatureSensor m) {
        log().info("sensor received RequestRegisterTemperatureSensor");
        sender().tell(new RespondSensorRegistered(m.getRequestId(), self()), self());
    }

}
