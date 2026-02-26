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

    // Lisäsin logiikkaa jokaisellle tapahtumalle, miten toimii slotit, ruletti jne..
    // Roulette: risky game -> lower win chance, higher payout
    private void resolveRouletteRound(Customer c) {

        int bet = c.calcBet();

        // Example model:
        //  - 60% lose (stress increases)
        //  - 35% small win (stress decreases a bit)
        //  - 5% big win (jackpot feeling)
        double r = rng.nextDouble();

        if (r < 0.60) {
            // loses the bet
            c.applyLoss(bet);
        } else if (r < 0.95) {
            // small win: profit ~ 1.5x bet
            // (applyWin should add money; choose what "amount" means in your Customer)
            c.applyWin((int) Math.round(bet * 1.5));
        } else {
            // big win: rare, bigger payout
            c.applyWin(bet * 6);
        }
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

    public MyEngine(IControllerMtoV controller) { // NEW
        super(controller); // NEW

        servicePoints = new ServicePoint[4];

        // KASINON PALVELUPISTEET
        // Baari - palveluaika noin 5 min, keskihajonta 2
        servicePoints[0] = new ServicePoint(new Normal(5, 2), eventList, EventType.BAR_SERVICE_END);

        // Peliautomaatit - peliaika noin 15 min, keskihajonta 8
        servicePoints[1] = new ServicePoint(new Normal(15, 8), eventList, EventType.SLOTS_PLAY_END);

        // Blackjack - peliaika noin 20 min, keskihajonta 10
        servicePoints[2] = new ServicePoint(new Normal(20, 10), eventList, EventType.BLACKJACK_GAME_END);
        // Roulette - peliaika noin 12 min, keskihajonta 6
        servicePoints[3] = new ServicePoint(new Normal(12, 6), eventList, EventType.ROULETTE_GAME_END);


        arrivalProcess = new ArrivalProcess(new Negexp(15, 5), eventList, EventType.CASINO_ARRIVAL);
    }

    @Override
    protected void initialization() {
        arrivalProcess.generateNext();     // First arrival in the system
    }

    @Override
    protected void runEvent(Event t) {  // B phase events
        Customer a;

        switch ((EventType) t.getType()) {

            case CASINO_ARRIVAL:

                Customer newCustomer = new Customer();
                arrivalProcess.generateNext();
                controller.visualiseCustomer();

                //satunnainen palvelupisteen valinta
                int randomServicePoint = rng.nextInt(4);
                servicePoints[randomServicePoint].addQueue(newCustomer);
                break;

            case BAR_SERVICE_END:
                a = servicePoints[0].removeQueue();
                decideNextActivity(a);
                //jos lähtee baariin niin juo
                a.applyDrink();
                break;

            case SLOTS_PLAY_END:
                a = servicePoints[1].removeQueue();
                decideNextActivity(a);
                resolveSlotsRound(a);
                break;

            case BLACKJACK_GAME_END:
                a = servicePoints[2].removeQueue();
                decideNextActivity(a);
                resolveBlackjackRound(a);
                break;
            case ROULETTE_GAME_END:
                a = servicePoints[3].removeQueue();
                decideNextActivity(a);
                resolveRouletteRound(a);
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
