
package actor.tests;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.TimeUnit;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import actors.TemparatureSensor;
import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.UnhandledMessage;
import akka.testkit.TestActorRef;
import akka.testkit.TestProbe;
import akka.testkit.javadsl.TestKit;
import messages.sensor.RequestMetaData;
import messages.sensor.RequestRegisterTemperatureSensor;
import messages.sensor.RequestTemperature;
import messages.sensor.RequestUpdateTemperature;
import messages.sensor.RespondMetaData;
import messages.sensor.RespondSensorRegistered;
import messages.sensor.RespondTemperature;
import messages.sensor.RespondTemperatureUpdated;
import scala.concurrent.duration.FiniteDuration;

public class TemparatureSensorShould {

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
    public void initializeSensorMetaData() {
        new TestKit(system) {
            {
                final ActorRef sensor = system.actorOf(TemparatureSensor.props("a", "1"));

                sensor.tell(new RequestMetaData(1), getRef());

                final RespondMetaData received = expectMsgClass(RespondMetaData.class);
                assertEquals(1, received.getRequestId());
                assertEquals("a", received.getFloorId());
                assertEquals("1", received.getSensorId());
            }
        };
    }

    @Test
    public void startWithNoTemperature() {
        new TestKit(system) {
            {
                final ActorRef sensor = system.actorOf(TemparatureSensor.props("a", "1"));

                sensor.tell(new RequestTemperature(1), getRef());

                final RespondTemperature received = expectMsgClass(RespondTemperature.class);
                assertEquals(0.0, received.getTemperature(), 0);
            }
        };
    }

    @Test
    public void confirmTemperatureUpdate() {
        new TestKit(system) {
            {
                final ActorRef sensor = system.actorOf(TemparatureSensor.props("a", "1"));

                sensor.tell(new RequestUpdateTemperature(42, 100), getRef());

                final RespondTemperatureUpdated received = expectMsgClass(RespondTemperatureUpdated.class);
                assertEquals(42, received.getRequestId());
            }
        };
    }

    @Test
    public void updateNewTemperature() {
        new TestKit(system) {
            {
                final ActorRef sensor = system.actorOf(TemparatureSensor.props("a", "1"));

                sensor.tell(new RequestUpdateTemperature(42, 100), getRef());
                final RespondTemperatureUpdated respondTemperatureUpdated = expectMsgClass(RespondTemperatureUpdated.class);

                sensor.tell(new RequestTemperature(1), getRef());

                final RespondTemperature received = expectMsgClass(RespondTemperature.class);
                assertEquals(100, received.getTemperature(), 0);
                assertEquals(1, received.getRequestId());
            }
        };
    }

    @Test
    public void registerSensor() {
        new TestKit(system) {
            {
                final ActorRef sensor = system.actorOf(TemparatureSensor.props("a", "1"));

                sensor.tell(new RequestRegisterTemperatureSensor(1, "a", "1"), getRef());

                final RespondSensorRegistered received = expectMsgClass(RespondSensorRegistered.class);

                assertEquals(1, received.getRequestId());
                assertEquals(sensor, received.getSensorReference());
            }
        };
    }

    @Test
    public void notRegisterSensorWhenIncorrectFloor() {
        new TestKit(system) {
            {
                final ActorRef sensor = system.actorOf(TemparatureSensor.props("a", "1"));

                sensor.tell(new RequestRegisterTemperatureSensor(1, "b", "1"), getRef());

                expectNoMsg();
            }
        };
    }

    @Test
    public void notRegisterSensorWhenIncorrectSensorId() {
        final TestProbe probe = TestProbe.apply(system);
        final TestProbe eventStreamProbe = TestProbe.apply(system);

        system.eventStream().subscribe(eventStreamProbe.ref(), UnhandledMessage.class);

        final TestActorRef<Actor> sensor = TestActorRef.create(system, TemparatureSensor.props("a", "1"));
        sensor.tell(new RequestRegisterTemperatureSensor(1, "a", "2"), probe.ref());

        probe.expectNoMsg();
        eventStreamProbe.expectMsgClass(FiniteDuration.apply(3, TimeUnit.SECONDS), UnhandledMessage.class);
    }

    @Test
    public void handleInValidMessage() {
        final TestProbe probe = TestProbe.apply(system);
        final TestActorRef<Actor> ref = TestActorRef.create(system, TemparatureSensor.props("a", "1"));
        system.eventStream().subscribe(probe.ref(), UnhandledMessage.class);
        ref.tell("notAValidMessage", ActorRef.noSender());
        probe.expectMsgClass(FiniteDuration.apply(3, TimeUnit.SECONDS), UnhandledMessage.class);
    }

}
