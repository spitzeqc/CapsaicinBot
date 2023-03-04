package com.cornchip.capsaicin;

import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.UpdateEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import java.util.List;

/**
 * Used to make bot semi-modular. Any command(s) or events need to implement the Chili class
 */
public abstract class Chili {
    /**
     * Executed whenever the base bot receives an event from Discord.
     * Any events, including SlashCommandEvent's, should be checked and executed here
     *
     * @param event A GenericEvent of the received event. Check and cast to proper event for use
     */
    protected void runEvents(GenericEvent event) {};

    /**
     * Executed whenever the base bot receives an UpdateEvent from Discord.
     * Any UpdateEvents should be checked and executed here
     *
     * @param updateEvent The UpdateEvent of our received event
     */
    protected void runUpdateEvent(UpdateEvent<?, ?> updateEvent) {};

    /**
     * Optional method that returns a List of commands for the bot to load.
     *
     * @return List of commands to register
     */
    protected List<CommandData> getCommands() {
        return null;
    };

    /**
     * Optional method that will be executed at a predetermined interval
     */
    protected void timedEvent() {}
}
