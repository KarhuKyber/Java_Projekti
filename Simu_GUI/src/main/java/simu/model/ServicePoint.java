package simu.model;

import simu.framework.Clock;
import simu.framework.Event;
import simu.framework.EventList;
import simu.framework.Trace;
import eduni.distributions.ContinuousGenerator;
import java.util.LinkedList;

public class ServicePoint {
	private LinkedList<Customer> queue = new LinkedList<>(); // Asiakkaat jonossa
	private ContinuousGenerator generator;
	private EventList eventList;
	private EventType scheduledEventType;

	private boolean reserved = false; // Onko palvelupiste varattu (peli käynnissä)

	public int queueLength() {
		return queue.size();
	}
	public ServicePoint(ContinuousGenerator g, EventList tl, EventType type) {
		this.generator = g;
		this.eventList = tl;
		this.scheduledEventType = type;
	}

	// Lisää asiakas jonoon (esim. kun asiakas saapuu Blackjack-pöydälle)
	public void addQueue(Customer a) {
		queue.add(a);
	}

	// Poistaa ja palauttaa asiakkaan, kun peli/palvelu päättyy
	public Customer removeQueue() {
		reserved = false;
		return queue.poll();
	}

	// Aloittaa palvelun jonon ensimmäiselle asiakkaalle
	public void beginService() {
		if (queue.isEmpty()) return;

		Trace.out(Trace.Level.INFO, "Aloitetaan palvelu pisteessä: " + scheduledEventType);

		reserved = true;
		double serviceTime = generator.sample(); // Arvotaan kesto (esim. pelikierros)

		// Luodaan päättymistapahtuma tapahtumalistalle
		// Nykyinen aika + arvottu kesto
		eventList.add(new Event(scheduledEventType, Clock.getInstance().getTime() + serviceTime));
	}

	// Tarkistetaan onko piste vapaa ja onko jonoa
	public boolean isReserved() {
		return reserved;
	}

	public boolean isOnQueue() {
		return !queue.isEmpty();
	}

	// Apumetodi: Palauttaa jonossa olevan asiakkaan ilman poistamista
	public Customer getFirst() {
		return queue.peek();
	}
}