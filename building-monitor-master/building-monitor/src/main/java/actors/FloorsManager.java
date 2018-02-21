
package actors;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Terminated;
import messages.floor.manager.RequestFloorIds;
import messages.floor.manager.RespondFloorIds;
import messages.sensor.RequestRegisterTemperatureSensor;

public class FloorsManager extends AbstractLoggingActor {

    private final Map<String, ActorRef> floorIdToActorRefMap = new HashMap<String, ActorRef>();

    public static Props props() {
        return Props.create(FloorsManager.class);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(RequestRegisterTemperatureSensor.class, this::onRequestRegisterTemperatureSensorMessage)
                .match(RequestFloorIds.class, this::onRequestFloorIds)
                .match(Terminated.class, this::onTerminatedMessage)
                .matchAny(m -> unhandled(m))
                .build();
    }

    private void onRequestRegisterTemperatureSensorMessage(final RequestRegisterTemperatureSensor m) {
        log().info("FloorsManager received RequestRegisterTemperatureSensor");

        if (floorIdToActorRefMap.containsKey(m.getFloorId())) {
            final ActorRef floor = floorIdToActorRefMap.get(m.getFloorId());
            floor.forward(m, getContext());
        } else {
            final ActorRef newFloor =
                    getContext().actorOf(Floor.props(m.getFloorId()), "floor-" + m.getFloorId());
            getContext().watch(newFloor);
            floorIdToActorRefMap.put(m.getFloorId(), newFloor);
            newFloor.forward(m, getContext());
        }
    }

    private void onRequestFloorIds(final RequestFloorIds m) {
        sender().tell(new RespondFloorIds(m.getRequestId(), floorIdToActorRefMap.keySet()), self());
    }

    private void onTerminatedMessage(final Terminated m) {
        String terminatedFloorId = null;
        for (final Entry<String, ActorRef> entry : floorIdToActorRefMap.entrySet()) {
            if (entry.getValue().equals(m.actor())) {
                terminatedFloorId = entry.getKey();
            }
        }
        if (terminatedFloorId != null) {
            floorIdToActorRefMap.remove(terminatedFloorId);
        }
    }

}
