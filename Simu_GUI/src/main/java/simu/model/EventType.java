package simu.model;

import simu.framework.IEventType;

// TODO:
// Event types are defined by the requirements of the simulation model
public enum EventType implements IEventType {

	// Kasino eventit

	CASINO_ARRIVAL, CASINO_EXIT,

	//-Baari
	BAR_ARRIVAL, BAR_SERVICE_END,

	//-Slotit
	SLOTS_ARRIVAL, SLOTS_PLAY_END,

	//-Blackjack
	BLACKJACK_ARRIVAL, BLACKJACK_GAME_END,
	//-Blackjack
	ROULETTE_ARRIVAL, ROULETTE_GAME_END,
}
