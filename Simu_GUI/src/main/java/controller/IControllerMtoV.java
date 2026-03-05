package controller;

/* interface for the engine */
public interface IControllerMtoV {
	public void showEndTime(double time);
	public void visualiseCustomer();
	public void updateCasinoView(int inside, int barQ, int slotsQ, int bjQ, int rouletteQ);

	public void showStats(String text);   // NEW
}
