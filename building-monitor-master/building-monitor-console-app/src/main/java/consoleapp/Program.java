
package consoleapp;

import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import actors.FloorsManager;
import akka.actor.ActorIdentity;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Identify;
import akka.pattern.AskableActorSelection;
import akka.pattern.Patterns;
import akka.util.Timeout;
import messages.floor.RequestAllTemperatures;
import messages.query.ITemperatureQueryReading;
import messages.query.RespondAllTemperatures;
import messages.query.TemperatureAvailable;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

public class Program {

    public static void main(final String args[]) throws Exception {
        final Scanner scanner = new Scanner(System.in);
        final ActorSystem system = ActorSystem.create("building-iot-system");
        final ActorRef floorManager = system.actorOf(FloorsManager.props(), "floors-manager");

        createSimulatedSensors(floorManager);

        while (true) {
            final String command = scanner.nextLine();

            switch (command) {
                case "q": {
                    system.terminate();
                    System.out.println("actor system shutdown");
                    System.exit(0);
                }
                    break;
                default:
                    System.out.println("To display temperature reading, press enter else q to quit");
                    displayTemperatures(system);
            }
        }

    }

    private static void createSimulatedSensors(final ActorRef floorManager) {
        for (int simulatedSensorId = 0; simulatedSensorId < 10; simulatedSensorId++) {
            final SimulatedSensor newSimulatedSensor = new SimulatedSensor("basement", Integer.toString(simulatedSensorId), floorManager);

            try {
                newSimulatedSensor.connect();
            } catch (final Exception e) {
                e.printStackTrace();
            }

            final boolean simulateNoReadingYet = simulatedSensorId == 3 ? true : false;

            if (!simulateNoReadingYet) {
                newSimulatedSensor.startSendingSimulatedReadings();
            }
        }
    }

    private static void displayTemperatures(final ActorSystem system) throws Exception {
        final ActorRef basementFloor = getService(system);
        final Timeout timeout = new Timeout(Duration.create(2, TimeUnit.SECONDS));
        final Future<Object> fTemperatures = Patterns.ask(basementFloor, new RequestAllTemperatures(0), timeout);
        final RespondAllTemperatures temeratures = (RespondAllTemperatures) Await.result(fTemperatures, timeout.duration());

        for (final Map.Entry<String, ITemperatureQueryReading> item : temeratures.getTemperatureReadings().entrySet()) {
            if (item.getValue() instanceof TemperatureAvailable) {
                final TemperatureAvailable t = (TemperatureAvailable) item.getValue();
                System.out.println("Sensor " + item.getKey() + " temparatur = " + t.getTemperature());
            }
        }

    }

    private static ActorRef getService(final ActorSystem system) {
        return retrieveActor(system.actorSelection("akka://building-iot-system/user/floors-manager/floor-basement"));
    }

    private static ActorRef retrieveActor(final ActorSelection actorSelection) {

        final Timeout timeout = new Timeout(2, TimeUnit.SECONDS);
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
