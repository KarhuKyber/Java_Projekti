package simu.model;

public class Stats {

    public int arrived = 0;
    public int exited = 0;

    public int bankrupt = 0;
    public int profitLowStress = 0;

    public long sumExitStress = 0;
    public long sumExitAlcohol = 0;

    // load: how many customers were inside during each hour (simple counter)
    public final int[] exitsPerHour = new int[24];
    public final int[] arrivalsPerHour = new int[24];

    public void onArrival(double timeMinutes) {
        arrived++;
        int h = (int)((timeMinutes / 60.0) % 24);
        arrivalsPerHour[h]++;
    }

    public void onExit(Customer c, double timeMinutes, boolean wasBankrupt, boolean wasProfitLowStress) {
        exited++;

        if (wasBankrupt) bankrupt++;
        if (wasProfitLowStress) profitLowStress++;

        sumMoneyChange += (c.getMoney() - c.getStartMoney());
        sumExitStress += c.getStress();
        sumExitAlcohol += c.getAlcohol();

        int h = (int)((timeMinutes / 60.0) % 24);
        exitsPerHour[h]++;
    }
    public double avgMoneyChange() {
        return exited == 0 ? 0 : (double) sumMoneyChange / exited;
    }

    public double bankruptPct() {
        return exited == 0 ? 0 : 100.0 * bankrupt / exited;
    }
    public long sumMoneyChange = 0;

    public double profitLowStressPct() {
        return exited == 0 ? 0 : 100.0 * profitLowStress / exited;
    }

    public double avgExitStress() {
        return exited == 0 ? 0 : (double)sumExitStress / exited;
    }

    public double avgExitAlcohol() {
        return exited == 0 ? 0 : (double)sumExitAlcohol / exited;
    }
}