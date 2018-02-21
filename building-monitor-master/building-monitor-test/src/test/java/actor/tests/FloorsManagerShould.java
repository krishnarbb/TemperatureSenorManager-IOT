
package actor.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import actors.FloorsManager;
import akka.actor.ActorIdentity;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Identify;
import akka.actor.PoisonPill;
import akka.pattern.AskableActorSelection;
import akka.testkit.TestProbe;
import akka.testkit.javadsl.TestKit;
import akka.util.Timeout;
import messages.floor.manager.RequestFloorIds;
import messages.floor.manager.RespondFloorIds;
import messages.sensor.RequestRegisterTemperatureSensor;
import messages.sensor.RespondSensorRegistered;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.FiniteDuration;

public class FloorsManagerShould {

    public static final int RETRIEVE_ACTOR_WAIT_TIMEOUT = 2;
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
    public void returnFloorIdsOnlyFromActiveActors() {
        final TestProbe probe = TestProbe.apply(system);
        final ActorRef floorManager = system.actorOf(FloorsManager.props(), "FloorsManager");

        floorManager.tell(new RequestRegisterTemperatureSensor(1, "a", "42"), probe.ref());
        probe.expectMsgClass(RespondSensorRegistered.class);

        floorManager.tell(new RequestRegisterTemperatureSensor(2, "b", "90"), probe.ref());
        probe.expectMsgClass(RespondSensorRegistered.class);

        // Stop one of the floor actors
        final ActorSelection actorSelection = system.actorSelection("akka://default/user/FloorsManager/floor-a");
        final ActorRef firstFloor = retrieveActor(actorSelection);
        probe.watch(firstFloor);
        firstFloor.tell(PoisonPill.getInstance(), probe.ref());
        probe.expectTerminated(firstFloor, FiniteDuration.apply(3, TimeUnit.SECONDS));

        floorManager.tell(new RequestFloorIds(1), probe.ref());
        final RespondFloorIds received = probe.expectMsgClass(RespondFloorIds.class);

        assertEquals(1, received.getRequestId());
        assertEquals(1, received.getFloorIds().size());
        assertTrue(received.getFloorIds().contains("b"));
    }

    private ActorRef retrieveActor(final ActorSelection actorSelection) {

        final Timeout timeout = new Timeout(RETRIEVE_ACTOR_WAIT_TIMEOUT, TimeUnit.SECONDS);
        final AskableActorSelection askableActorSelection = new AskableActorSelection(actorSelection);
        final Future<Object> future = askableActorSelection.ask(new Identify(null), timeout);
        ActorIdentity actorIdentity;
        try {
            actorIdentity = (ActorIdentity) Await.result(future, timeout.duration());
        } catch (final Exception e) {
            throw new IllegalStateException("Failed to retrieve the requested actor.", e);
        }

        return actorIdentity.getActorRef().get();
    }
}
