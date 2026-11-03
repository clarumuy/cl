package uy.edu.isbo.control.model.agent;

/** Una orden pendiente y el tiempo que falta para ejecutarla. */
public record CommandOffer(long id, long delayMillis) {
    public String wire() { return "shutdown|" + id + "|" + delayMillis; }
}
