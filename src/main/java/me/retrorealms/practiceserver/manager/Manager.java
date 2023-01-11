package me.retrorealms.practiceserver.manager;

import me.retrorealms.practiceserver.PracticeServer;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

/**
 * Created by Khalid on 8/3/2017.
 */
public abstract class Manager {

    public abstract void onEnable();

    public abstract void onDisable();

    protected void listener(Listener listener) {
        Bukkit.getServer().getPluginManager().registerEvents(listener, PracticeServer.getInstance());
    }

}
