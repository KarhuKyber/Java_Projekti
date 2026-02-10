package simu.model;

import simu.framework.IEventType;

// TODO:
// Event types are defined by the requirements of the simulation model
public enum EventType implements IEventType {
	ARRIVAL, DECISION, SERVICE_END, EXIT;
}
