package simu.model;

/**
 * Casino gambler model.
 * Stores financial state and psychological state, used by the engine to model behaviour.
 */
public class Customer {

	// Optional: if we want to mark exit time (not required for stats, but ok to keep)
	private double removalTime;

	// Money at entry
	private final int startMoney;

	// Current money
	private int money;

	// 0..100 (rises after losses, lowers after wins / drinks)
	private int stress;

	// 0..100
	private int alcohol;

	// Personality risk multiplier (affects bet size)
	private final double risk;

	// Individual preference to go to bar (0.8..1.2). Helps create variation.
	private final double barPreference;

	// Aggressive betting mode
	private boolean tilt;

	public Customer() {
		startMoney = 100 + (int) (Math.random() * 401); // 100..500
		money = startMoney;

		stress = 10;
		alcohol = 0;
		tilt = false;

		risk = 0.8 + Math.random() * 0.4;          // 0.8..1.2
		barPreference = 0.8 + Math.random() * 0.4; // 0.8..1.2
	}

	// --- Getters ---
	public int getMoney() { return money; }
	public int getStartMoney() { return startMoney; }
	public int getStress() { return stress; }
	public int getAlcohol() { return alcohol; }
	public boolean isTilt() { return tilt; }
	public double getBarPreference() { return barPreference; }

	public double getRemovalTime() { return removalTime; }
	public void setRemovalTime(double removalTime) { this.removalTime = removalTime; }

	private int clamp0_100(int x) {
		return Math.max(0, Math.min(100, x));
	}

	/**
	 * Calculates bet size based on money, stress, alcohol and risk.
	 */
	public int calcBet() {
		int minBet = 5;
		int maxBet = Math.max(10, money / 2); // never bet more than half of balance

		double stressFactor = 1.0 + (stress / 100.0);
		double alcoholFactor = 1.0 + (alcohol / 200.0);

		double base = money * 0.05; // 5% of money
		double bet = base * stressFactor * alcoholFactor * risk;

		int result = (int) Math.round(bet);
		result = Math.max(minBet, Math.min(maxBet, result));

		if (tilt) {
			result = Math.min(maxBet, (int) (result * 1.3));
		}

		return result;
	}

	/**
	 * Losing money increases stress.
	 */
	public void applyLoss(int amount) {
		money -= amount;
		stress = clamp0_100(stress + 7);

		// if money goes too low, stress jumps a bit (optional, makes bankrupt possible faster)
		if (money < 50) {
			stress = clamp0_100(stress + 15);
		}
	}

	/**
	 * Winning money decreases stress slightly.
	 */
	public void applyWin(int amount) {
		money += amount;
		stress = clamp0_100(stress - 3);
	}

	/**
	 * Visiting bar: alcohol increases, stress decreases temporarily.
	 */
	public void applyDrink() {
		alcohol = clamp0_100(alcohol + 15);
		stress = clamp0_100(stress - 10);

		// drunk gamblers sometimes lose control
		if (alcohol > 70 && Math.random() < 0.2) {
			tilt = true;
		}
	}

	public boolean isBankrupt() {
		return money <= 0;
	}
}