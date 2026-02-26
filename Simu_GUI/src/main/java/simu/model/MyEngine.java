package simu.model;

import controller.IControllerMtoV;
import eduni.distributions.Negexp;
import eduni.distributions.Normal;
import simu.framework.ArrivalProcess;
import simu.framework.Clock;
import simu.framework.Engine;
import simu.framework.Event;
import java.util.Random;


public class MyEngine extends Engine {
	private ArrivalProcess arrivalProcess;

	public MyEngine(IControllerMtoV controller){ // NEW
		super(controller); // NEW
		
		servicePoints = new ServicePoint[3];

		// KASINON PALVELUPISTEET
		// Baari - palveluaika noin 5 min, keskihajonta 2
		servicePoints[3]=new ServicePoint(new Normal(5,2), eventList, EventType.BAR_SERVICE_END);

		// Peliautomaatit - peliaika noin 15 min, keskihajonta 8
		servicePoints[4]=new ServicePoint(new Normal(15,8), eventList, EventType.SLOTS_PLAY_END);

		// Blackjack - peliaika noin 20 min, keskihajonta 10
		servicePoints[5]=new ServicePoint(new Normal(20,10), eventList, EventType.BLACKJACK_GAME_END);


		

		arrivalProcess = new ArrivalProcess(new Negexp(15,5), eventList, EventType.CASINO_ARRIVAL);
	}

	@Override
	protected void initialization() {
		arrivalProcess.generateNext();	 // First arrival in the system
	}

	@Override
	protected void runEvent(Event t) {  // B phase events
		Customer a;

		switch ((EventType)t.getType()){

			case CASINO_ARRIVAL:

			Customer newCustomer = new Customer();
			arrivalProcess.generateNext();
			controller.visualiseCustomer();

			//satunnainen palvelupisteen valinta
			int randomServicePoint = new Random().nextInt(3);
			servicePoints[randomServicePoint].addQueue(newCustomer);
			break;

		case BAR_SERVICE_END:
			a = servicePoints[0].removeQueue();
			 decideNextActivity(a);
			break;

		case SLOTS_PLAY_END:
			a = servicePoints[1].removeQueue();
			decideNextActivity(a);
			break;

		case BLACKJACK_GAME_END:
			a = servicePoints[2].removeQueue();
			decideNextActivity(a);
			break;
		}


	}

	private void decideNextActivity(Customer customer) {
		//logiikka tähän
	}

	@Override
	protected void results() {
		// OLD text UI
		//System.out.println("Simulation ended at " + Clock.getInstance().getClock());
		//System.out.println("Results ... are currently missing");

		// NEW GUI
		controller.showEndTime(Clock.getInstance().getTime());
	}
}
