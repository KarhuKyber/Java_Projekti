package simu.model;

import simu.framework.Clock;
import simu.framework.Trace;

// I didnt delete original code, im afraid that it will broke something, but anyways, i created Casino gambler model
// Stores psychological states, personality and financial condition,
// Used by simulation engine to model player behaviour


// Customer to be implemented according to the requirements of the simulation model (data!)
public class Customer {
	// money customer had when entering casinoo
	private int startMoney;
	// current amount of money available for betting
	private int money;
	// increases after losses, decreases after wins or drinks
	private int stress;
	// alcohol intoxication level (0..100)
    // increases when visiting bar
	private int alcohol;
	// personality risk multiplier (affects bet size)
	private double risk;
	// ability to control behaviour under stress/alcohol
	private double selfControl;
	// true when gambler loses control (aggressive betting)
	private boolean tilt;

	// creates new gambler entering the casino
    // assigns id, arrival time and initial personality parameters
	public Customer() {
		// Casino fields initialisation (our project)

        // Start money: simple version (later we can switch to distribution-based)
		startMoney = 100 + (int)(Math.random() * 401); // 100..500
		money = startMoney;

        // Stress/alcohol start values
		stress = 10;      // calm at start
		alcohol = 0;      // sober at start
		tilt = false;

        // Personality (risk and self-control): 0.8..1.2
		risk = 0.8 + Math.random() * 0.4;
		selfControl = 0.8 + Math.random() * 0.4;
	}


	// getters used by simulation engine to read customer state
	public int getMoney() { return money; }
	public int getStartMoney() { return startMoney; }
	public int getStress() { return stress; }
	public int getAlcohol() { return alcohol; }
	public boolean isTilt() { return tilt; }

	// setters used by engine to update gambler condition
	public void setMoney(int money) { this.money = money; }
	public void setStress(int stress) { this.stress = clamp0_100(stress); }
	public void setAlcohol(int alcohol) { this.alcohol = clamp0_100(alcohol); }
	public void setTilt(boolean tilt) { this.tilt = tilt; }

	// keeps stress and alcohol values inside valid range (0..100)
	private int clamp0_100(int x) {
		return Math.max(0, Math.min(100, x));
	}

	// calculates bet size based on:
    // available money, stress level, alcohol level and personality risk
    // tilt gamblers bet more aggressively
	public int calcBet() {

		int minBet = 5;

		//not betitng more than half of our balance
		int maxBet = Math.max(10, money / 2);

		double stressFactor = 1.0 + (stress / 100.0);
		double alcoholFactor = 1.0 + (alcohol / 200.0);

		// base bet is 5 procent of our money
		double base = money * 0.05;

		double bet = base
				* stressFactor
				* alcoholFactor
				* risk;

		int result = (int)Math.round(bet);

		result = Math.max(minBet, Math.min(maxBet, result));

		// tilt gamblers bet more aggressively
		if (tilt) {
			result = Math.min(maxBet, (int)(result * 1.3));
		}

		return result;
	}
	//if customer loses  money stress level gets higher
	public void applyLoss(int amount) {

		money -= amount;

		stress += 5;

		stress = clamp0_100(stress);

	}
	//if wins stress levels gets lower
	public void applyWin(int amount) {

		money += amount;

		stress -= 3;

		stress = clamp0_100(stress);

	}
	// visiting bar:
    // alcohol increases but stress decreases temporarily
	public void applyDrink() {

		alcohol = clamp0_100(alcohol + 15);

		stress = clamp0_100(stress - 10);

		// drunk gamblers sometimes lose control
		if (alcohol > 70 && Math.random() < 0.2) {
			tilt = true;
		}
	}
	// checks if gambler has no money left and must exit casino
	public boolean isBankrupt() {
		return money <= 0;
	}
}
