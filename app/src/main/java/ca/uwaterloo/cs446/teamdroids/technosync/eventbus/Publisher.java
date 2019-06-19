package ca.uwaterloo.cs446.teamdroids.technosync.eventbus;

public abstract class Publisher {
    private EventBus eventBus;

    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public abstract void publish();
}
