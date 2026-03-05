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
		servicePoints[0] = new ServicePoint(new Normal(5, 2), eventList, EventType.BAR_SERVICE_END);

		// 1 = Slots
		servicePoints[1] = new ServicePoint(new Normal(15, 8), eventList, EventType.SLOTS_PLAY_END);

		// 2 = Blackjack
		servicePoints[2] = new ServicePoint(new Normal(20, 10), eventList, EventType.BLACKJACK_GAME_END);

		// 3 = Roulette
		servicePoints[3] = new ServicePoint(new Normal(12, 6), eventList, EventType.ROULETTE_GAME_END);

		arrivalProcess = new ArrivalProcess(new Negexp(15, 5), eventList, EventType.CASINO_ARRIVAL);
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

				// first service is random
				int randomServicePoint = rng.nextInt(4);
				servicePoints[randomServicePoint].addQueue(newCustomer);
				refreshView();
				break;

			case BAR_SERVICE_END:
				c = servicePoints[0].removeQueue();
				c.applyDrink();
				decideNextActivity(c);
				refreshView();
				break;

			case SLOTS_PLAY_END:
				c = servicePoints[1].removeQueue();
				resolveSlotsRound(c);
				decideNextActivity(c);
				refreshView();
				break;

			case BLACKJACK_GAME_END:
				c = servicePoints[2].removeQueue();
				resolveBlackjackRound(c);
				decideNextActivity(c);
				refreshView();
				break;

			case ROULETTE_GAME_END:
				c = servicePoints[3].removeQueue();
				resolveRouletteRound(c);
				decideNextActivity(c);
				refreshView();
				break;
		}
	}

	private void decideNextActivity(Customer c) {

		double now = Clock.getInstance().getTime();

		// Exit conditions we need for stats
		boolean profitLowStress = (c.getMoney() > c.getStartMoney()) && (c.getStress() < 20);

		// 1) Bankrupt -> exit (stop scheduling)
		if (c.isBankrupt()) {
			stats.onExit(c, now, true, false);
			return;
		}

		// 2) Profit + low stress -> sometimes exit
		if (profitLowStress && rng.nextDouble() < 0.10) {
			stats.onExit(c, now, false, true);
			return;
		}

		// Time-of-day based drinking chance
		double hour = (now / 60.0) % 24.0;
		boolean evening = (hour >= 18.0 || hour < 3.0);

		double s = c.getStress() / 100.0;    // 0..1
		double alc = c.getAlcohol() / 100.0; // 0..1

		// Weights: higher weight => more likely
		double wBar = (evening ? 2.0 : 0.7) + 2.5 * s + 0.3 * alc;                 // stress + evening -> bar
		double wSlots = 1.0 + 1.2 * s + 0.6 * alc;                     // stress/alcohol -> slots
		double wBlackjack = Math.max(0.2, 1.0 - 0.4 * s - 0.2 * alc);  // calmer -> blackjack
		double wRoulette = 1.2 + 1.5 * s + 0.8 * alc;                  // risky -> roulette

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

		if (rng.nextDouble() < 0.45) {
			c.applyWin((int) Math.round(bet * 1.8));
		} else {
			c.applyLoss(bet);
		}
	}

	private void resolveBlackjackRound(Customer c) {
		int bet = c.calcBet();

		if (rng.nextDouble() < 0.49) {
			c.applyWin((int) Math.round(bet * 1.2));
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
			c.applyWin((int) Math.round(bet * 1.5));
		} else {
			c.applyWin(bet * 6);
		}
	}

	@Override
	protected void results() {

		StringBuilder sb = new StringBuilder();

		sb.append("---- CASINO STATS ----\n");
		sb.append("Arrived: ").append(stats.arrived).append("\n");
		sb.append("Exited: ").append(stats.exited).append("\n\n");
		sb.append("Still inside: ").append(stats.arrived - stats.exited).append("\n\n");

		sb.append(String.format("Avg money change: %.1f\n", stats.avgMoneyChange()));
		sb.append("Still inside: " + (stats.arrived - stats.exited) + "\n");
		sb.append(String.format("Bankrupt %%: %.1f%%\n", stats.bankruptPct()));
		sb.append(String.format("Profit+LowStress %%: %.1f%%\n", stats.profitLowStressPct()));

		sb.append(String.format("Avg stress: %.1f\n", stats.avgExitStress()));
		sb.append(String.format("Avg alcohol: %.1f\n\n", stats.avgExitAlcohol()));

		controller.showStats(sb.toString());

		controller.showEndTime(Clock.getInstance().getTime());
	}
}