package com.cornchip.capsaicin;

import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.UpdateEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import java.util.List;

public abstract class Chili {
    protected abstract void runEvents(GenericEvent event); //used to check if an event has occurred (this includes SlashCommandEvents's)
    protected abstract void runUpdateEvent(UpdateEvent<?, ?> updateEvent);
    protected abstract List<CommandData> getCommands(); //used to load commands into discord
}
