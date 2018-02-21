
package actors;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Terminated;
import messages.floor.RequestAllTemperatures;
import messages.floor.RequestTemperatureSensorIds;
import messages.floor.RespondTemperatureSensorIds;
import messages.sensor.RequestRegisterTemperatureSensor;
import scala.concurrent.duration.Duration;

public class Floor extends AbstractLoggingActor {

    private final String floorId;
    private final Map<String, ActorRef> sensorIdToActorRefMap = new HashMap<>();

    public static Props props(final String floorId) {
        return Props.create(Floor.class, floorId);
    }

    public Floor(final String floorId) {
        this.floorId = floorId;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(RequestRegisterTemperatureSensor.class, m -> m.getFloorId().equalsIgnoreCase(floorId),
                        this::onRequestRegisterTemperatureSensorMessage)
                .match(RequestTemperatureSensorIds.class, this::onRequestTemperatureSensorIdsMessage)
                .match(RequestAllTemperatures.class, this::onRequestAllTemperaturesMessage)
                .match(Terminated.class, this::onTerminatedMessage)
                .matchAny(m -> unhandled(m))
                .build();
    }

    private void onRequestRegisterTemperatureSensorMessage(final RequestRegisterTemperatureSensor m) {
        log().info("Floor received RequestRegisterTemperatureSensor");

        if (sensorIdToActorRefMap.containsKey(m.getSensorId())) {
            final ActorRef sensor = sensorIdToActorRefMap.get(m.getSensorId());
            sensor.forward(m, getContext());
        } else {
            final ActorRef newSensor =
                    getContext().actorOf(TemparatureSensor.props(m.getFloorId(), m.getSensorId()), "temperature-sensor-" + m.getSensorId());
            getContext().watch(newSensor);
            sensorIdToActorRefMap.put(m.getSensorId(), newSensor);
            newSensor.forward(m, getContext());
        }
    }

    private void onRequestTemperatureSensorIdsMessage(final RequestTemperatureSensorIds m) {
        sender().tell(new RespondTemperatureSensorIds(m.getRequestId(),
                sensorIdToActorRefMap.keySet()), self());
    }

    private void onRequestAllTemperaturesMessage(final RequestAllTemperatures m) {

        final Map<ActorRef, String> actorRefToSensorIdMap = new HashMap<ActorRef, String>();

        for (final Map.Entry<String, ActorRef> item : sensorIdToActorRefMap.entrySet()) {
            actorRefToSensorIdMap.put(item.getValue(), item.getKey());
        }
        getContext().actorOf(FloorQuery.props(actorRefToSensorIdMap,
                m.getRequestId(),
                sender(),
                Duration.create(3, SECONDS)));

    }

    private void onTerminatedMessage(final Terminated m) {
        String terminatedTemperatureSensorId = null;
        for (final Entry<String, ActorRef> entry : sensorIdToActorRefMap.entrySet()) {
            if (entry.getValue().equals(m.actor())) {
                terminatedTemperatureSensorId = entry.getKey();
            }
        }
        if (terminatedTemperatureSensorId != null) {
            sensorIdToActorRefMap.remove(terminatedTemperatureSensorId);
        }
    }
}
