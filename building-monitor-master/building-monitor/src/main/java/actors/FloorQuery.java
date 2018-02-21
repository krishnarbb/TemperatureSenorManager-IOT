
package actors;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.actor.Props;
import akka.actor.Terminated;
import messages.query.ITemperatureQueryReading;
import messages.query.NoTemperatureReadingRecordedYet;
import messages.query.QueryTimeout;
import messages.query.RespondAllTemperatures;
import messages.query.TemperatureAvailable;
import messages.query.TemperatureSensorNotAvailable;
import messages.query.TemperatureSensorTimedOut;
import messages.sensor.RequestTemperature;
import messages.sensor.RespondTemperature;
import scala.concurrent.duration.FiniteDuration;

public class FloorQuery extends AbstractLoggingActor {

    public static long TemperatureRequestCorrelationId = 42;

    private final Cancellable queryTimeoutTimer;
    private final Map<ActorRef, String> actorToSensorId;
    private final long requestId;
    private final ActorRef requester;
    private final FiniteDuration timeout;

    private final Map<String, ITemperatureQueryReading> repliesReceived = new HashMap<String, ITemperatureQueryReading>();
    private final HashSet<ActorRef> stillAwaitingReply;

    public static Props props(final Map<ActorRef, String> actorToSensorId, final long requestId, final ActorRef requester,
            final FiniteDuration timeout) {
        return Props.create(FloorQuery.class, actorToSensorId, requestId, requester, timeout);
    }

    public FloorQuery(final Map<ActorRef, String> actorToSensorId, final long requestId, final ActorRef requester, final FiniteDuration timeout) {
        this.actorToSensorId = actorToSensorId;
        this.requestId = requestId;
        this.requester = requester;
        this.timeout = timeout;
        stillAwaitingReply = new HashSet<>();
        stillAwaitingReply.addAll(actorToSensorId.keySet());
        // ScheduleTellOnceCancelable

        queryTimeoutTimer = getContext().system().scheduler().scheduleOnce(
                this.timeout,
                getSelf(),
                QueryTimeout.getInstance(),
                getContext().dispatcher(), getSelf());
    }

    @Override
    public void preStart() {
        for (final ActorRef temperatureSensor : actorToSensorId.keySet()) {
            getContext().watch(temperatureSensor);
            temperatureSensor.tell(new RequestTemperature(TemperatureRequestCorrelationId), getSelf());
        }
    }

    @Override
    public void postStop() {
        queryTimeoutTimer.cancel();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(RespondTemperature.class, m -> m.getRequestId() == TemperatureRequestCorrelationId, this::onRespondTemperatureMessage)
                .match(QueryTimeout.class, this::onQueryTimeoutMessage)
                .match(Terminated.class, this::onTerminatedMessage)
                .matchAny(m -> unhandled(m))
                .build();

    }

    private void onRespondTemperatureMessage(final RespondTemperature m) {
        log().info("FloorQuery received RespondTemperature");
        ITemperatureQueryReading reading = null;
        if (m.getTemperature() != 0) {
            reading = new TemperatureAvailable(m.getTemperature());
        } else {
            reading = NoTemperatureReadingRecordedYet.getInstance();
        }
        recordSensorResponse(sender(), reading);
    }

    private void recordSensorResponse(final ActorRef sensorActor, final ITemperatureQueryReading reading) {
        getContext().unwatch(sensorActor);
        final String sensorId = actorToSensorId.get(sensorActor);
        stillAwaitingReply.remove(sensorActor);
        repliesReceived.put(sensorId, reading);

        final boolean allRepliesHaveBeenReceived = stillAwaitingReply.size() == 0 ? true : false;
        if (allRepliesHaveBeenReceived) {
            requester.tell(new RespondAllTemperatures(requestId, repliesReceived), getSelf());
            getContext().stop(getSelf());
        }
    }

    private void onQueryTimeoutMessage(final QueryTimeout m) {
        for (final ActorRef sensor : stillAwaitingReply) {
            final String sensorId = actorToSensorId.get(sensor);
            repliesReceived.put(sensorId, TemperatureSensorTimedOut.getInstance());
        }
        requester.tell(new RespondAllTemperatures(requestId, repliesReceived), getSelf());
        getContext().stop(getSelf());
    }

    private void onTerminatedMessage(final Terminated m) {
        recordSensorResponse(m.actor(), TemperatureSensorNotAvailable.getInstance());
    }

}
