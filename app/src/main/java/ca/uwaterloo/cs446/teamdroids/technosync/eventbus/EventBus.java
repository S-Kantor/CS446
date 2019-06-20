package ca.uwaterloo.cs446.teamdroids.technosync.eventbus;

import java.util.ArrayList;
import java.util.List;

public class EventBus {

    private List<Subscriber> subscribers;

    public void newEvent(EventPackage eventPackage){
        for (Subscriber subscriber : subscribers){
            subscriber.notify(eventPackage);
        }
    }

    public void register(Subscriber subscriber){
        subscribers.add(subscriber);
    }

    public EventBus (){
        subscribers = new ArrayList<>();
    }

}
