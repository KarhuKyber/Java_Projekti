package view;

public interface IVisualisation {
	public void clearDisplay();
	public void newCustomer();

	// NEW: show current casino state
	public void updateCasinoView(int inside, int barQ, int slotsQ, int bjQ, int rouletteQ);
}

