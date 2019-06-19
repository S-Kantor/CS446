package ca.uwaterloo.cs446.teamdroids.technosync.eventbus;

public abstract class Subscriber {
    public abstract void notify(EventPackage eventPackage);
}
