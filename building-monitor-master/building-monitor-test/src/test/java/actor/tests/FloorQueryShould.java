
package actor.tests;

import static java.util.concurrent.TimeUnit.SECONDS;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import actors.FloorQuery;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.TestProbe;
import akka.testkit.javadsl.TestKit;
import messages.query.ITemperatureQueryReading;
import messages.query.RespondAllTemperatures;
import messages.query.TemperatureAvailable;
import messages.sensor.RequestTemperature;
import messages.sensor.RespondTemperature;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

public class FloorQueryShould {

    static ActorSystem system;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create();
    }

    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void returnTemperatures() {
        final TestProbe queryRequester = TestProbe.apply(system);
        final TestProbe temperatureSensor1 = TestProbe.apply(system);
        final TestProbe temperatureSensor2 = TestProbe.apply(system);

        final Map<ActorRef, String> actorToSensorId = new HashMap<>();
        actorToSensorId.put(temperatureSensor1.ref(), "sensor1");
        actorToSensorId.put(temperatureSensor2.ref(), "sensor2");
        final long requestId = 1;
        final ActorRef requester = queryRequester.ref();
        final FiniteDuration timeout = Duration.create(5, SECONDS);
        final ActorRef floorQuery = system.actorOf(FloorQuery.props(actorToSensorId, requestId, requester, timeout));

        final RequestTemperature m1 = temperatureSensor1.expectMsgClass(FiniteDuration.apply(3, TimeUnit.SECONDS), RequestTemperature.class);
        assertEquals(FloorQuery.TemperatureRequestCorrelationId, m1.getRequestId());
        assertEquals(floorQuery, temperatureSensor1.sender());

        final RequestTemperature m2 = temperatureSensor2.expectMsgClass(FiniteDuration.apply(3, TimeUnit.SECONDS), RequestTemperature.class);
        assertEquals(FloorQuery.TemperatureRequestCorrelationId, m2.getRequestId());
        assertEquals(floorQuery, temperatureSensor2.sender());

        // when
        floorQuery.tell(new RespondTemperature(
                FloorQuery.TemperatureRequestCorrelationId, 23.9), temperatureSensor1.ref());

        floorQuery.tell(new RespondTemperature(
                FloorQuery.TemperatureRequestCorrelationId, 32.4), temperatureSensor2.ref());

        // then

        final RespondAllTemperatures response =
                queryRequester.expectMsgClass(FiniteDuration.apply(5, TimeUnit.SECONDS), RespondAllTemperatures.class);

        assertEquals(1, response.getRequestId());
        assertEquals(2, response.getTemperatureReadings().size());

        final Map<String, ITemperatureQueryReading> readings = response.getTemperatureReadings();

        final TemperatureAvailable temperatureReading1 = (TemperatureAvailable) readings.get("sensor1");
        assertEquals(23.9, temperatureReading1.getTemperature(), 0);

        final TemperatureAvailable temperatureReading2 = (TemperatureAvailable) readings.get("sensor2");
        assertEquals(32.4, temperatureReading2.getTemperature(), 0);

    }

}
