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
	private final Random rng = new Random(1);

	public MyEngine(IControllerMtoV controller) {
		super(controller);

		// 4 service points: Bar, Slots, Blackjack, Roulette
		servicePoints = new ServicePoint[4];

		// 0 = Bar
		servicePoints[0] =
				new ServicePoint(new Normal(5, 2),
						eventList,
						EventType.BAR_SERVICE_END);

		// 1 = Slots
		servicePoints[1] =
				new ServicePoint(new Normal(15, 8),
						eventList,
						EventType.SLOTS_PLAY_END);

		// 2 = Blackjack
		servicePoints[2] =
				new ServicePoint(new Normal(20, 10),
						eventList,
						EventType.BLACKJACK_GAME_END);

		// 3 = Roulette
		servicePoints[3] =
				new ServicePoint(new Normal(12, 6),
						eventList,
						EventType.ROULETTE_GAME_END);

		arrivalProcess =
				new ArrivalProcess(new Negexp(15, 5),
						eventList,
						EventType.CASINO_ARRIVAL);
	}

	@Override
	protected void initialization() {
		arrivalProcess.generateNext();
	}

	@Override
	protected void runEvent(Event t) {

		Customer c;

		switch ((EventType) t.getType()) {

			case CASINO_ARRIVAL:

				Customer newCustomer = new Customer();
				arrivalProcess.generateNext();
				controller.visualiseCustomer();

				int randomServicePoint = rng.nextInt(4);
				servicePoints[randomServicePoint].addQueue(newCustomer);
				break;

			case BAR_SERVICE_END:

				c = servicePoints[0].removeQueue();
				c.applyDrink();
				decideNextActivity(c);
				break;

			case SLOTS_PLAY_END:

				c = servicePoints[1].removeQueue();
				resolveSlotsRound(c);
				decideNextActivity(c);
				break;

			case BLACKJACK_GAME_END:

				c = servicePoints[2].removeQueue();
				resolveBlackjackRound(c);
				decideNextActivity(c);
				break;

			case ROULETTE_GAME_END:

				c = servicePoints[3].removeQueue();
				resolveRouletteRound(c);
				decideNextActivity(c);
				break;
		}
	}

	private void decideNextActivity(Customer c) {

		// If bankrupt -> leaves casino (stop scheduling)
		if (c.isBankrupt()) {
			c.setRemovalTime(Clock.getInstance().getTime());
			return;
		}

		// Optional: sometimes leave when calm + profitable
		if (c.getStress() < 20 && c.getMoney() > c.getStartMoney() && rng.nextDouble() < 0.10) {
			c.setRemovalTime(Clock.getInstance().getTime());
			return;
		}

		// Time-of-day based drinking chance
		double hour = (Clock.getInstance().getTime() / 60.0) % 24.0;
		boolean evening = (hour >= 18.0 || hour < 3.0);

		double s = c.getStress() / 100.0;    // 0..1
		double alc = c.getAlcohol() / 100.0; // 0..1

		// Weights: higher weight => more likely
		double wBar = (evening ? 1.0 : 0.3) + 1.5 * s;           // stress + evening -> bar
		double wSlots = 1.0 + 1.2 * s + 0.6 * alc;               // stress/alcohol -> slots
		double wBlackjack = Math.max(0.2, 1.0 - 0.4 * s - 0.2 * alc); // calmer -> blackjack
		double wRoulette = 1.2 + 1.5 * s + 0.8 * alc;            // risky -> roulette

		double total = wBar + wSlots + wBlackjack + wRoulette;
		double r = rng.nextDouble() * total;

		if (r < wBar) {
			servicePoints[0].addQueue(c);
		} else if (r < wBar + wSlots) {
			servicePoints[1].addQueue(c);
		} else if (r < wBar + wSlots + wBlackjack) {
			servicePoints[2].addQueue(c);
		} else {
			servicePoints[3].addQueue(c);
		}
	}


	private void resolveSlotsRound(Customer c) {

		int bet = c.calcBet();

		if (rng.nextDouble() < 0.45) {
			c.applyWin((int)Math.round(bet * 1.8));
		} else {
			c.applyLoss(bet);
		}
	}

	private void resolveBlackjackRound(Customer c) {

		int bet = c.calcBet();

		if (rng.nextDouble() < 0.49) {
			c.applyWin((int)Math.round(bet * 1.2));
		} else {
			c.applyLoss(bet);
		}
	}

	private void resolveRouletteRound(Customer c) {

		int bet = c.calcBet();

		double r = rng.nextDouble();

		if (r < 0.60) {
			c.applyLoss(bet);
		} else if (r < 0.95) {
			c.applyWin((int)Math.round(bet * 1.5));
		} else {
			c.applyWin(bet * 6);
		}
	}

	protected void results() {

	}
}