package me.retrorealms.practiceserver.manager;

/**
 * Created by Khalid on 8/3/2017.
 */
public class SampleManager extends Manager {

    @Override
    public void onEnable() {
        System.out.println("Red is gay");
    }

    @Override
    public void onDisable() {
        System.out.println("Red is really gay");
    }
}
