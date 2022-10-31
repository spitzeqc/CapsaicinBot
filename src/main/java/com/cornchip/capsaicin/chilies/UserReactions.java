package com.cornchip.capsaicin.chilies;

import com.cornchip.capsaicin.Chili;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.UpdateEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class UserReactions extends Chili {
    private static final String CHILIES_PATH = com.cornchip.capsaicin.CapsaicinBot.getChiliesPath();
    private static final String RESOURCES_PATH = "UserReactions/";
    private static final String REACTIONS_FILE = "reactions.dat";

    //HashMap<Guild, HashMap<User, LinkedList<MessageReaction.ReactionEmote>>>
    private HashMap<String, HashMap<String, LinkedList<String>>> userReactions;
    private HashMap<String, HashMap<String, String>> setReactionMessages;
    private HashMap<String, LinkedList<String>> messageReactions;

    public UserReactions() {
        this.userReactions = this.loadReactionData();
        if(this.userReactions == null)
            this.userReactions = new HashMap<>();

        this.setReactionMessages = new HashMap<>();
        this.messageReactions = new HashMap<>();
    }

    private static String convert16to32(String toConvert){
        for (int i = 0; i < toConvert.length(); ) {
            int codePoint = Character.codePointAt(toConvert, i);
            i += Character.charCount(codePoint);
            String utf32 = String.format("0x%x%n", codePoint);
            return utf32;
        }
        return null;
    }

    public void startSetUserReaction(SlashCommandEvent event) {
        Message reactionAddMessage = (new MessageBuilder()).append("Create your reaction (only use emotes from this server)").build();

        event.reply(reactionAddMessage).queue();
        if(!event.getChannel().hasLatestMessage()) {
            event.getChannel().sendMessage("An error has occurred").queue();
            return;
        }
        List<Message> history = event.getChannel().getHistoryAround(event.getChannel().getLatestMessageId(), 1).complete().retrieveFuture(10).complete();
        String messageID = null;
        for(Message m: history) {
            if(m.getAuthor().getId().equals(event.getJDA().getSelfUser().getId())) {
                messageID = m.getId();
                break;
            }
        }

        HashMap<String, String> temp = new HashMap<>();
        temp.put(event.getGuild().getId(), messageID);

        this.setReactionMessages.put(event.getUser().getId(), temp);
        this.messageReactions.put(messageID, new LinkedList<>());
    }
    public void endSetUserReaction(SlashCommandEvent event) {
        if(!setReactionMessages.containsKey(event.getUser().getId()) || !setReactionMessages.get(event.getUser().getId()).containsKey(event.getGuild().getId())){
            event.reply("You are not currently setting a message reaction!").setEphemeral(true).queue();
            return;
        }

        LinkedList<String> reactions = this.messageReactions.get(this.setReactionMessages.get(event.getUser().getId()).get(event.getGuild().getId()));

        HashMap<String, LinkedList<String>> temp;
        if(!this.userReactions.containsKey(event.getUser().getId()))
            temp = new HashMap<>();
        else
            temp = this.userReactions.get(event.getUser().getId());


        temp.put(event.getGuild().getId(), reactions);

        this.userReactions.put(event.getUser().getId(), temp);
        this.saveReactionData();

        String m = this.setReactionMessages.get(event.getUser().getId()).get(event.getGuild().getId());
        this.messageReactions.remove(m);
        this.setReactionMessages.get(event.getUser().getId()).remove(event.getGuild().getId());

        event.reply("Reaction updated").setEphemeral(true).queue();
    }
    public void removeUserReaction(SlashCommandEvent event) {
        this.userReactions.get(event.getUser().getId()).remove(event.getGuild().getId());
        this.saveReactionData();
        event.reply("Reaction removed").setEphemeral(true).queue();
    }

    private void saveReactionData() {
        String reactionDataPath = (CHILIES_PATH + RESOURCES_PATH + REACTIONS_FILE);

        File f;
        FileOutputStream fout = null;
        ObjectOutputStream oos = null;

        try {
            if(!Files.isDirectory(Paths.get((CHILIES_PATH + RESOURCES_PATH)))) {
                System.out.println(RESOURCES_PATH + " not found. Creating...");
                if(!(new File((CHILIES_PATH + RESOURCES_PATH))).mkdir()) {
                    throw new IOException("Error creating '" + RESOURCES_PATH + "' please create manually");
                }
            }

            f = new File(reactionDataPath);
            f.createNewFile();
            fout = new FileOutputStream(f);
            oos = new ObjectOutputStream(fout);

            oos.writeObject(this.userReactions);

        } catch(IOException ioe) {
            System.err.println("An error occurred when writing user reactions data!\n" + ioe.getMessage());
        } finally {
            try {
                if (oos != null) {
                    oos.close();
                }
                if(fout != null) {
                    fout.close();
                }
            } catch (IOException ignored) {}
        }
    }

    private HashMap<String, HashMap<String, LinkedList<String>>> loadReactionData() {
        String reactionDataPath = (CHILIES_PATH + RESOURCES_PATH + REACTIONS_FILE);

        HashMap<String, HashMap<String, LinkedList<String>>> data = null;

        File reactionFile = new File(reactionDataPath);
        //check if file exists
        if(reactionFile.exists() && !reactionFile.isDirectory()) {
            ObjectInputStream ois = null;
            try {
                FileInputStream streamIn = new FileInputStream(reactionDataPath);
                ois = new ObjectInputStream(streamIn);
                //load users data map
                data = (HashMap<String, HashMap<String, LinkedList<String>>>) ois.readObject();

                streamIn.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (ois != null)
                    try {
                        ois.close();
                    } catch (IOException ignored) {
                    }
            }
        } else {
            data = new HashMap<>();
        }

        return data;
    }

    private void addReactions(MessageReceivedEvent event) {
        String guild = event.getGuild().getId();
        String user = event.getAuthor().getId();
        if(!this.userReactions.containsKey(user) || !this.userReactions.get(user).containsKey(guild))
            return;

        LinkedList<String> emotes = this.userReactions.get(user).get(guild);
        if(emotes == null)
            return;

        Message message = event.getMessage();

        JDA jda = event.getJDA();
        for(String e: emotes) {
            if(e.startsWith("U+")) {
                message.addReaction(e).queue();
            } else {
                Emote emote = jda.getEmoteById(e);
                if(emote == null)
                    continue;
                message.addReaction(emote).queue();
            }


        }
    }

    @Override
    public void runEvents(GenericEvent event) {
        if (event instanceof SlashCommandEvent) {
            if (((SlashCommandEvent) event).getName().equals("reaction")) {
                String subcommand = ((SlashCommandEvent) event).getSubcommandName();
                switch (subcommand) {
                    case "set" -> startSetUserReaction((SlashCommandEvent) event);
                    case "save" -> endSetUserReaction((SlashCommandEvent) event);
                    case "clear" -> removeUserReaction((SlashCommandEvent) event);
                }
            }
        }
        if (event instanceof GuildMessageReactionAddEvent) {
            GuildMessageReactionAddEvent reactionEvent = (GuildMessageReactionAddEvent) event;

            //check if member is in setReactionMessages
            String guild = reactionEvent.getGuild().getId();
            String user = reactionEvent.getUser().getId();
            if(!this.setReactionMessages.containsKey(user) || !this.setReactionMessages.get(user).containsKey(guild))
                return;

            if (!this.setReactionMessages.get(user).get(guild).equals(reactionEvent.getMessageId()))
                return;

            String reaction;
            if (reactionEvent.getReactionEmote().isEmoji()){
                String utf32 = convert16to32(reactionEvent.getReactionEmote().getEmoji());
                if (utf32 != null) {
                    reaction = "U+" + utf32.substring(2);
                }
                else {
                    System.err.println("Error converting emoji " + reactionEvent.getReactionEmote().getEmoji() + " to utf32! Skipping...");
                    return;
                }
            } else {
                reaction = reactionEvent.getReactionEmote().getId();
            }

            this.messageReactions.get(reactionEvent.getMessageId()).add(reaction);

        }
        if (event instanceof GuildMessageReactionRemoveEvent) {
            GuildMessageReactionRemoveEvent reactionEvent = (GuildMessageReactionRemoveEvent) event;

            //check if member is in setReactionMessages
            String guild = reactionEvent.getGuild().getId();
            String user = reactionEvent.getUser().getId();
            if(!this.setReactionMessages.containsKey(user) || !this.setReactionMessages.get(user).containsKey(guild))
                return;

            if (!this.setReactionMessages.get(user).get(guild).equals(reactionEvent.getMessageId()))
                return;

            String reaction;
            if (reactionEvent.getReactionEmote().isEmoji()){
                String utf32 = convert16to32(reactionEvent.getReactionEmote().getEmoji());
                reaction = "U+" + utf32.substring(2);
            } else {
                reaction = reactionEvent.getReactionEmote().getId();
            }

            this.messageReactions.get(reactionEvent.getMessageId()).remove(reaction);

        }
        if (event instanceof MessageReceivedEvent) {
            addReactions((MessageReceivedEvent) event);
        }
    }

    @Override
    public void runUpdateEvent(UpdateEvent<?, ?> updateEvent) {

    }

    @Override
    public List<CommandData> getCommands() {
        LinkedList<CommandData> commandData = new LinkedList<>();
        CommandData reaction = new CommandData("reaction", "Manage reactions to your messages");

        SubcommandData set = new SubcommandData("set", "Set the reaction for your messages (emotes MUST be from the server you are currently on)");
        SubcommandData save = new SubcommandData("save", "Save the reaction for your messages");
        SubcommandData clear = new SubcommandData("clear", "Clear your reaction to messages");

        reaction.addSubcommands(set, save, clear);
        commandData.add(reaction);

        return commandData;
    }
}
