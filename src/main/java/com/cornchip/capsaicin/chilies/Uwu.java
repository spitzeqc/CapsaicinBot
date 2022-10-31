package com.cornchip.capsaicin.chilies;

import club.minnced.discord.webhook.external.JDAWebhookClient;
import club.minnced.discord.webhook.receive.ReadonlyMessage;
import club.minnced.discord.webhook.send.WebhookMessage;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.cornchip.capsaicin.Chili;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.UpdateEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import net.dv8tion.jda.api.requests.restaction.WebhookAction;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Uwu extends Chili {
    private static final String CHILIES_PATH = com.cornchip.capsaicin.CapsaicinBot.getChiliesPath();
    private static final String RESOURCES_PATH = "Uwu/";
    private static final String USERS_FILE = "registeredUsers.dat";
    private static final String CHANNELS_FILE = "enabledChannels.dat";

    //Member, list of enabled guilds
    private HashMap<String, ArrayList<String>> registeredUsers;
    //Guild, list of enabled channels
    private HashMap<String, ArrayList<String>> allowedChannels;

    public Uwu() {
        this.registeredUsers = loadUsersData();
        if(this.registeredUsers == null)
            this.registeredUsers = new HashMap<>();

        this.allowedChannels = loadChannelData();
        if(this.allowedChannels == null)
            this.allowedChannels = new HashMap<>();
    }

    private HashMap<String, ArrayList<String>> loadUsersData() {
        String dataPath = CHILIES_PATH + RESOURCES_PATH + USERS_FILE;
        HashMap<String, ArrayList<String>> data = null;

        File dataFile = new File(dataPath);
        if(dataFile.exists() && !dataFile.isDirectory()) {
            ObjectInputStream ois = null;
            try {
                FileInputStream streamIn = new FileInputStream(dataPath);
                ois = new ObjectInputStream(streamIn);
                data = (HashMap<String, ArrayList<String>>) ois.readObject();

                streamIn.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if(ois != null) {
                    try {
                        ois.close();
                    } catch (Exception ignored) {}
                }
            }
        } else {
            data = new HashMap<>();
        }

        return data;
    }
    private HashMap<String, ArrayList<String>> loadChannelData() {
        String dataPath = CHILIES_PATH + RESOURCES_PATH + CHANNELS_FILE;
        HashMap<String, ArrayList<String>> data = null;

        File dataFile = new File(dataPath);
        if(dataFile.exists() && !dataFile.isDirectory()) {
            ObjectInputStream ois = null;
            try {
                FileInputStream streamIn = new FileInputStream(dataPath);
                ois = new ObjectInputStream(streamIn);
                data = (HashMap<String, ArrayList<String>>) ois.readObject();

                streamIn.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if(ois != null) {
                    try {
                        ois.close();
                    } catch (Exception ignored) {}
                }
            }
        } else {
            data = new HashMap<>();
        }

        return data;
    }

    private void writeUwuChanges() {
        String dataPath = CHILIES_PATH + RESOURCES_PATH + USERS_FILE;

        File f;
        FileOutputStream fout = null;
        ObjectOutputStream oos = null;

        try {
            if(!Files.isDirectory(Paths.get(CHILIES_PATH+RESOURCES_PATH))) {
                System.out.println(RESOURCES_PATH + " not found. Creating...");
                if(!(new File(CHILIES_PATH+RESOURCES_PATH)).mkdir()) {
                    throw new IOException("Error creating '" + RESOURCES_PATH + "' please create manually");
                }
            }

            f = new File(dataPath);
            f.createNewFile();
            fout = new FileOutputStream(f);
            oos = new ObjectOutputStream(fout);

            oos.writeObject(this.registeredUsers);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (oos != null)
                    oos.close();
                if (fout != null)
                    fout.close();
            } catch (Exception ignored) {}
        }
    }

    private void writeChannelChanges() {
        String dataPath = CHILIES_PATH + RESOURCES_PATH + CHANNELS_FILE;

        File f;
        FileOutputStream fout = null;
        ObjectOutputStream oos = null;

        try {
            if(!Files.isDirectory(Paths.get(CHILIES_PATH+RESOURCES_PATH))) {
                System.out.println(RESOURCES_PATH + " not found. Creating...");
                if(!(new File(CHILIES_PATH+RESOURCES_PATH)).mkdir()) {
                    throw new IOException("Error creating '" + RESOURCES_PATH + "' please create manually");
                }
            }

            f = new File(dataPath);
            f.createNewFile();
            fout = new FileOutputStream(f);
            oos = new ObjectOutputStream(fout);

            oos.writeObject(this.allowedChannels);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (oos != null)
                    oos.close();
                if (fout != null)
                    fout.close();
            } catch (Exception ignored) {}
        }
    }


    private static String uwuChangeString(String baseString, int index, char replacementChar) throws IndexOutOfBoundsException {
        if (index<0)
            return replacementChar + baseString;

        if(index >= baseString.length())
            return baseString + replacementChar;

        return baseString.substring(0, index) + replacementChar + baseString.substring(index+1);
    }

    private String convertToUwu(String message) {
        HashMap<Character, Character> characterMap = new HashMap<>(); //Probably should make this a constant instead of recreating every time
        characterMap.put('l', 'w');
        characterMap.put('r', 'w');
        HashMap<String, String> wordMap = new HashMap<>();
        wordMap.put("the", "teh");

        String newMessage = message.toLowerCase();
        //iterate over every character replacement
        for(Map.Entry<Character, Character> set : characterMap.entrySet())
            newMessage = newMessage.replaceAll(set.getKey().toString(), set.getValue().toString());

        //replace words
        for(Map.Entry<String, String> set: wordMap.entrySet())
            newMessage = newMessage.replaceAll(set.getKey(), set.getValue());

        //capitalize
        for(int i=0; i<newMessage.length(); ++i) {
            if(Character.isUpperCase(message.charAt(i))) {
                newMessage = uwuChangeString(newMessage, i, Character.toUpperCase(newMessage.charAt(i)));
            }
        }
        newMessage = newMessage.replaceAll("!", ":3");

        return newMessage;
    }

    private boolean enableUwu(Member member) {
        String memberId = member.getId();
        String guildId = member.getGuild().getId();
        if( !this.registeredUsers.containsKey(memberId) ) {
            this.registeredUsers.put(memberId, new ArrayList<>(Collections.singleton(guildId)));
            this.writeUwuChanges();
            return true;
        } else if ( !this.registeredUsers.get(memberId).contains(guildId) ) {
            this.registeredUsers.get(memberId).add(guildId);
            this.writeUwuChanges();
            return true;
        }
        return false;
    }

    private boolean disableUwu(Member member) {
        String memberId = member.getId();
        String guildId = member.getGuild().getId();
        if(this.registeredUsers.containsKey(memberId) && this.registeredUsers.get(memberId).contains(guildId)) {

            this.registeredUsers.get(memberId).remove(guildId);
            if(this.registeredUsers.get(memberId).isEmpty())
                this.registeredUsers.remove(memberId);

            this.writeUwuChanges();
            return true;
        }
        return false;
    }

    //need to create method for running automatic translation
    public void automaticUwu(MessageReceivedEvent event) throws ExecutionException, InterruptedException, IOException {
        Object lock = new Object();

        Member member = event.getMember();
        if(member==null)
            return;
        String guildID = member.getGuild().getId();
        if(event.getAuthor().isBot() || !this.registeredUsers.containsKey(member.getId()) || !this.registeredUsers.get(member.getId()).contains(guildID))
            return;

        if(!this.allowedChannels.containsKey(guildID) || !this.allowedChannels.get(guildID).contains(event.getChannel().getId()))
            return;


        String translated = convertToUwu(event.getMessage().getContentRaw());
        if(translated.equals(event.getMessage().getContentRaw()))
            return;

        MessageChannel channelObject = event.getChannel();

        /*
            regex: <@:\d{20}>
            java string: "<@:\\d{20}>"
            parse the 20 digits
            create mention
        */

        Pattern pattern = Pattern.compile("<@:3\\d{18}>");
        Matcher matcher = pattern.matcher(translated);
        while(matcher.find()) {
            String group = matcher.group();
            String userID = group.substring(4,22);
            //String mentionedMemberName = event.getJDA().getUserById(group.substring(4,22)).getAsMention();

            AtomicReference<User> u = new AtomicReference<>();
            event.getJDA().retrieveUserById(userID).queue(u::set);
            synchronized (lock) {
                while(u.get() == null) {
                    try {
                        lock.wait(100);
                    } catch(Exception ignored){}
                }
                lock.notify();
            }
            String mentionedMemberName = u.get().getAsMention();
            translated = translated.replace(group, mentionedMemberName);
        }

        //create webhook
        WebhookAction webhookBuilder;
        try {
            webhookBuilder = ((TextChannel) channelObject).createWebhook(member.getEffectiveName());
        } catch (PermissionException pe) {
            event.getMessage().reply("Lacking permission to create webhooks").queue();
            return;
        }
        try {
            InputStream imageStream = new URL( member.getEffectiveAvatarUrl() ).openStream();
            webhookBuilder = webhookBuilder.setAvatar( Icon.from( imageStream ) );
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }

        //execute create and get webhook
        AtomicReference<Webhook> webhookRef = new AtomicReference<>();
        webhookBuilder.queue(webhookRef::set)  ; //execute webhook creation and store webhook in webhookRef

        synchronized (lock) {
            while(webhookRef.get() == null) {
                try {
                    lock.wait(100);
                } catch (Exception ignored) {}
            }
            lock.notify();
        }

        Webhook webhook = webhookRef.get();

        //make message to send
        HashMap<String, Message.Attachment> fileMap = new HashMap<>();

        List<Message.Attachment> attachments = event.getMessage().getAttachments();

        WebhookMessageBuilder webhookMessageBuilder = new WebhookMessageBuilder().setContent(translated);
        for(Message.Attachment a: attachments) {
            InputStream s = a.retrieveInputStream().get();
            webhookMessageBuilder.addFile(a.getFileName(), s);
            s.close();
        }
        WebhookMessage message = webhookMessageBuilder.build();

        synchronized (lock) {
            //send message and destroy webhook
            try (JDAWebhookClient client = JDAWebhookClient.from(webhook)) {
                CompletableFuture<ReadonlyMessage> cf = client.send(message);

                while (!cf.isDone()) {
                    try {
                        lock.wait(100);
                    } catch (Exception ignored) {}
                }
                lock.notify();
            }


            webhook.delete().queue();
            event.getMessage().delete().queue();
        }
    }

    public boolean uwuEnableChannel(SlashCommandEvent event) {
        String channelId = event.getChannel().getId();
        String guildId = event.getGuild().getId();
        if( !this.allowedChannels.containsKey(guildId) ) {
            this.allowedChannels.put(guildId, new ArrayList<>(Collections.singleton(channelId)));
            this.writeChannelChanges();
            return true;
        } else if ( !this.allowedChannels.get(guildId).contains(channelId) ) {
            this.allowedChannels.get(guildId).add(channelId);
            this.writeChannelChanges();
            return true;
        }
        return false;
    }
    public boolean uwuDisableChannel(SlashCommandEvent event) {
        String channelId = event.getChannel().getId();
        String guildId = event.getGuild().getId();
        if(this.allowedChannels.containsKey(guildId) && this.allowedChannels.get(guildId).contains(channelId)) {

            this.allowedChannels.get(guildId).remove(channelId);
            if(this.allowedChannels.get(guildId).isEmpty())
                this.allowedChannels.remove(guildId);

            this.writeChannelChanges();
            return true;
        }
        return false;
    }

    public void uwuReaction(MessageReceivedEvent event) {
        if ( event.getMessage().getContentDisplay().toLowerCase().contains("uwu") ) {
            event.getMessage().addReaction("U+1F1FA").queue();
            event.getMessage().addReaction("U+1F1FC").queue();
            event.getMessage().addReaction("U+26CE").queue();
        } else if ( event.getMessage().getContentDisplay().toLowerCase().contains("owo") ) {
            //event.getMessage().addReaction("U+1F17E,U+FE0F").queue();
            event.getMessage().addReaction("U+1F17E").queue();
            event.getMessage().addReaction("U+1F1FC").queue();
            event.getMessage().addReaction("U+1F1F4").queue();
        }
    }

    public void runCommands(SlashCommandEvent event) {
        String commandName = event.getName();
        if(commandName.equals("uwu")) {
            String subcommand = event.getSubcommandName();
            String subcommandGroup = event.getSubcommandGroup();

            if(subcommand != null && subcommandGroup == null) {
                switch (subcommand) {
                    case "translate":
                        event.reply(this.convertToUwu(event.getOption("phrase").getAsString())).queue();
                        break;
                    case "enable":
                        if (this.enableUwu(event.getMember()))
                            event.reply("Automatic translation enabled").setEphemeral(true).queue();
                        else
                            event.reply("Automatic translation is already enabled").setEphemeral(true).queue();
                        break;
                    case "disable":
                        if (this.disableUwu(event.getMember()))
                            event.reply("Automatic translation disabled").setEphemeral(true).queue();
                        else
                            event.reply("Automatic translation is already disabled").setEphemeral(true).queue();
                        break;
                    default:
                        event.reply("An error has occurred").setEphemeral(true).queue();
                }
            }

            if(subcommandGroup != null) {
                if(subcommandGroup.equals("channel") && subcommand!=null) {

                    if(event.getGuild()==null) {
                        event.reply("**Error**: This command can only be used within a guild!").setEphemeral(true).queue();
                    } else if (!event.getUser().getId().equals(event.getGuild().getOwnerId())) {
                        event.reply("**Error**: Only the guild owner can use this command!").setEphemeral(true).queue();
                    } else {
                        switch (subcommand) {
                            case "enable":
                                if(this.uwuEnableChannel(event))
                                    event.reply("Automatic translation enabled for this channel").setEphemeral(true).queue();
                                else
                                    event.reply("Automatic translation is already enabled for this channel").setEphemeral(true).queue();
                                break;
                            case "disable":
                                if(this.uwuDisableChannel(event))
                                    event.reply("Automatic translation disabled for this channel").setEphemeral(true).queue();
                                else
                                    event.reply("Automatic translation is already disabled for this channel").setEphemeral(true).queue();
                                break;
                            default:
                                event.reply("An error has occurred").setEphemeral(true).queue();
                                break;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void runEvents(GenericEvent event) {
        if(event instanceof SlashCommandEvent)
            runCommands((SlashCommandEvent) event);

        if(event instanceof MessageReceivedEvent) {
            this.uwuReaction((MessageReceivedEvent) event);

            try {
                this.automaticUwu((MessageReceivedEvent) event);
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }

        }

    }

    @Override
    public void runUpdateEvent(UpdateEvent<?, ?> updateEvent) {

    }

    @Override
    public List<CommandData> getCommands() {
        List<CommandData> commands = new LinkedList<CommandData>(); //where we will store all our commands. This is what we return
        CommandData uwuCommandData = new CommandData("uwu", "Like 1337 sp34k, but for modern gamers"); //setup parent command

        SubcommandData translate = new SubcommandData("translate", "Translate a phrase"); //add the subcommand to parent command
        translate.addOption(OptionType.STRING, "phrase", "The phrase to translate", true); //add an option to the subcommand

        SubcommandData enableAuto = new SubcommandData("enable", "Enable automatic translation");
        SubcommandData disableAuto = new SubcommandData("disable", "Disable automatic translation");

        SubcommandGroupData channelToggle = new SubcommandGroupData("channel", "Allow automatic translation in a channel (disabled by default)");
        SubcommandData enableChannel = new SubcommandData("enable", "Allow for automatic translation in the current channel");
        SubcommandData disableChannel = new SubcommandData("disable", "Do not allow for automatic translation in the current channel");

        channelToggle.addSubcommands(enableChannel, disableChannel);

        uwuCommandData.addSubcommands(translate, enableAuto, disableAuto);
        uwuCommandData.addSubcommandGroups(channelToggle);

        commands.add(uwuCommandData);
        return commands;
    }

}
