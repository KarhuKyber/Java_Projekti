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
	private final Stats stats = new Stats();

	public MyEngine(IControllerMtoV controller) {
		super(controller);

		// 4 service points: Bar, Slots, Blackjack, Roulette
		servicePoints = new ServicePoint[4];

		// 0 = Bar
		servicePoints[0] = new ServicePoint(
				new Normal(5, 2),
				eventList,
				EventType.BAR_SERVICE_END
		);

		// 1 = Slots
		servicePoints[1] = new ServicePoint(
				new Normal(8, 4),
				eventList,
				EventType.SLOTS_PLAY_END
		);

		// 2 = Blackjack
		servicePoints[2] = new ServicePoint(
				new Normal(12, 5),
				eventList,
				EventType.BLACKJACK_GAME_END
		);

		// 3 = Roulette
		servicePoints[3] = new ServicePoint(
				new Normal(9, 4),
				eventList,
				EventType.ROULETTE_GAME_END
		);

		arrivalProcess = new ArrivalProcess(
				new Negexp(25, 5),
				eventList,
				EventType.CASINO_ARRIVAL
		);
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
				double now = Clock.getInstance().getTime();

				Customer newCustomer = new Customer();
				stats.onArrival(now);

				arrivalProcess.generateNext();
				controller.visualiseCustomer();

				// First service is random
				int randomServicePoint = rng.nextInt(4);
				enqueueAndMaybeStart(randomServicePoint, newCustomer);

				refreshView();
				break;

			case BAR_SERVICE_END:
				c = servicePoints[0].removeQueue();
				servicePoints[0].beginService();   // start next one in bar queue
				c.applyDrink();
				decideNextActivity(c);

				refreshView();
				break;

			case SLOTS_PLAY_END:
				c = servicePoints[1].removeQueue();
				servicePoints[1].beginService();   // start next one in slots queue
				resolveSlotsRound(c);
				decideNextActivity(c);

				refreshView();
				break;

			case BLACKJACK_GAME_END:
				c = servicePoints[2].removeQueue();
				servicePoints[2].beginService();   // start next one in blackjack queue
				resolveBlackjackRound(c);
				decideNextActivity(c);

				refreshView();
				break;

			case ROULETTE_GAME_END:
				c = servicePoints[3].removeQueue();
				servicePoints[3].beginService();   // start next one in roulette queue
				resolveRouletteRound(c);
				decideNextActivity(c);

				refreshView();
				break;
		}
	}

	private void decideNextActivity(Customer c) {
		double now = Clock.getInstance().getTime();


		// Exit conditions we need for stats
		boolean profitLowStress =
				(c.getMoney() > c.getStartMoney()) &&
						(c.getStress() < 20);

		// 1) Bankrupt -> exit
		if (c.isBankrupt()) {
			stats.onExit(c, now, true, false);
			return;
		}

		// 2) Profit + low stress -> sometimes exit
		if (profitLowStress && rng.nextDouble() < 0.03) {
			stats.onExit(c, now, false, true);
			return;
		}


		// small generic chance to leave after each activity
		if (rng.nextDouble() < 0.01) {
			stats.onExit(c, now, false, false);
			return;
		}
		// tired customers may leave even without profit
		if (c.getStress() > 85 && rng.nextDouble() < 0.15) {
			stats.onExit(c, now, false, false);
			return;
		}
		// Time-of-day
		double hour = (now / 60.0) % 24.0;
		boolean evening = (hour >= 18.0 || hour < 3.0);

		// --- Direct bar impulse ---
		double baseBarChance = evening ? 0.10 : 0.02;      // 10% evening, 2% daytime
		double stressBoost = (c.getStress() / 100.0) * 0.10; // up to +10%
		double barChance = Math.min(0.60, baseBarChance + stressBoost);

		if (rng.nextDouble() < barChance) {
			enqueueAndMaybeStart(0, c); // Bar
			return;
		}

		double s = c.getStress() / 100.0;    // 0..1
		double alc = c.getAlcohol() / 100.0; // 0..1

		// Weights: higher weight => more likely
		double wBar = (evening ? 1.2 : 0.4) + 1.2 * s + 0.2 * alc;
		double wSlots = 1.0 + 1.2 * s + 0.6 * alc;
		double wBlackjack = Math.max(0.4, 1.2 - 0.3 * s - 0.1 * alc);
		double wRoulette = 0.8 + 1.0 * s + 0.4 * alc;

		double total = wBar + wSlots + wBlackjack + wRoulette;
		double r = rng.nextDouble() * total;

		if (r < wBar) {
			enqueueAndMaybeStart(0, c); // Bar
		} else if (r < wBar + wSlots) {
			enqueueAndMaybeStart(1, c); // Slots
		} else if (r < wBar + wSlots + wBlackjack) {
			enqueueAndMaybeStart(2, c); // Blackjack
		} else {
			enqueueAndMaybeStart(3, c); // Roulette
		}
	}

	private void enqueueAndMaybeStart(int idx, Customer c) {
		servicePoints[idx].addQueue(c);

		if (!servicePoints[idx].isReserved()) {
			servicePoints[idx].beginService();
		}
	}

	private void refreshView() {
		int inside = stats.arrived - stats.exited;

		controller.updateCasinoView(
				inside,
				servicePoints[0].queueLength(), // bar
				servicePoints[1].queueLength(), // slots
				servicePoints[2].queueLength(), // blackjack
				servicePoints[3].queueLength()  // roulette
		);
	}

	private void resolveSlotsRound(Customer c) {
		int bet = c.calcBet();

		if (rng.nextDouble() < 0.30) {
			c.applyWin((int) Math.round(bet * 1.5));
		} else {
			c.applyLoss(bet);
		}
	}

	private void resolveBlackjackRound(Customer c) {
		int bet = c.calcBet();

		if (rng.nextDouble() < 0.42) {
			c.applyWin((int) Math.round(bet * 1.1));
		} else {
			c.applyLoss(bet);
		}
	}

	private void resolveRouletteRound(Customer c) {
		int bet = c.calcBet();
		double r = rng.nextDouble();

		if (r < 0.75) {
			c.applyLoss(bet);
		} else if (r < 0.97) {
			c.applyWin((int) Math.round(bet * 1.2));
		} else {
			c.applyWin(bet * 3);
		}
	}

	@Override
	protected void results() {
		StringBuilder sb = new StringBuilder();

		sb.append("---- CASINO STATS ----\n");
		sb.append("Arrived: ").append(stats.arrived).append("\n");
		sb.append("Exited: ").append(stats.exited).append("\n");
		sb.append("Still inside: ").append(stats.arrived - stats.exited).append("\n\n");

		sb.append(String.format("Avg money change: %.1f\n", stats.avgMoneyChange()));
		sb.append(String.format("Bankrupt %%: %.1f%%\n", stats.bankruptPct()));
		sb.append(String.format("Profit+LowStress %%: %.1f%%\n", stats.profitLowStressPct()));
		sb.append(String.format("Avg stress: %.1f\n", stats.avgExitStress()));
		sb.append(String.format("Avg alcohol: %.1f\n", stats.avgExitAlcohol()));

		controller.showStats(sb.toString());
		controller.showEndTime(Clock.getInstance().getTime());
	}
}