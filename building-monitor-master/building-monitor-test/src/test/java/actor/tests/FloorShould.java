
package actor.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import actors.Floor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.testkit.TestProbe;
import akka.testkit.javadsl.TestKit;
import messages.floor.RequestTemperatureSensorIds;
import messages.floor.RespondTemperatureSensorIds;
import messages.sensor.RequestRegisterTemperatureSensor;
import messages.sensor.RespondSensorRegistered;
import scala.concurrent.duration.FiniteDuration;

public class FloorShould {
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
    public void returnTemperatureSensorsIdsOnlyFromActiveActors() {
        final TestProbe probe = TestProbe.apply(system);
        final ActorRef floor = system.actorOf(Floor.props("a"));

        floor.tell(new RequestRegisterTemperatureSensor(1, "a", "42"), probe.ref());
        probe.expectMsgClass(RespondSensorRegistered.class);
        final ActorRef firstSensorAdded = probe.lastSender();

        floor.tell(new RequestRegisterTemperatureSensor(2, "a", "90"), probe.ref());
        probe.expectMsgClass(RespondSensorRegistered.class);

        // Stop one of the actors
        probe.watch(firstSensorAdded);
        firstSensorAdded.tell(PoisonPill.getInstance(), probe.ref());
        probe.expectTerminated(firstSensorAdded, FiniteDuration.apply(3, TimeUnit.SECONDS));

        floor.tell(new RequestTemperatureSensorIds(1), probe.ref());
        final RespondTemperatureSensorIds response = probe.expectMsgClass(RespondTemperatureSensorIds.class);

        assertEquals(1, response.getSensorIds().size());
        assertTrue(response.getSensorIds().contains("90"));

    }
}
