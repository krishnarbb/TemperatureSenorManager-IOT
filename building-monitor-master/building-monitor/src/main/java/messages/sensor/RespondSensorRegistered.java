
package messages.sensor;

import akka.actor.ActorRef;

public class RespondSensorRegistered {
    private final long requestId;
    private final ActorRef sensorReference;

    public RespondSensorRegistered(final long requestId, final ActorRef sensorReference) {
        this.requestId = requestId;
        this.sensorReference = sensorReference;
    }

    public long getRequestId() {
        return requestId;
    }

    public ActorRef getSensorReference() {
        return sensorReference;
    }

}
