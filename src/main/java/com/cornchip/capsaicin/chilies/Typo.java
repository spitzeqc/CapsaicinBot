package com.cornchip.capsaicin.chilies;

import com.cornchip.capsaicin.Chili;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.UpdateEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

public class Typo extends Chili {

    private void sendImage(SlashCommandEvent event) {
        User mention = event.getOption("user").getAsUser(); //required option, cannot produce null
        
        InputStream file = getClass().getResourceAsStream("/typo.jpeg");

        byte[] f;
        try {
            f = new BufferedInputStream(file).readAllBytes(); //part of jar package
        } catch (IOException ioe) {
            return;
        }

        event.reply(mention.getAsMention() + " has made a typo").addFile(f, "typo.jpeg").queue();
    }

    @Override
    public void runEvents(GenericEvent event) {
        if (event instanceof SlashCommandEvent) {
            if ( ((SlashCommandEvent) event).getName().equals("typo") ) {
                this.sendImage((SlashCommandEvent) event);
            }
        }
    }

    @Override
    public void runUpdateEvent(UpdateEvent<?, ?> updateEvent) {

    }

    @Override
    public List<CommandData> getCommands() {
        List<CommandData> commandData = new LinkedList<>();
        CommandData typoCommand = new CommandData("typo", "Politely inform of a typo in the group chat");
        typoCommand.addOption(OptionType.USER, "user", "User who made the typo", true);

        commandData.add(typoCommand);
        return commandData;
    }
}
