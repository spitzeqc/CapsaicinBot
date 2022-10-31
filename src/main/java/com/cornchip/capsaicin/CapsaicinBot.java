package com.cornchip.capsaicin;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.UpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.security.auth.login.LoginException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

public class CapsaicinBot extends ListenerAdapter {
    private static final String CONFIG_PATH = "config.json";
    private static final String CHILIES_PATH = "chilies/";
    private static List<Chili> chiliObjects = new LinkedList<>();

    //run each jar and/or class file and have it return a CommandData object, which will then be loaded by the main bot?

    private static List<CommandData> loadChilies() throws IOException {
        //see if directory exists, if not create it
        if(!Files.isDirectory(Paths.get(CHILIES_PATH))) {
            System.out.println("Chilies directory not found. Creating...");
            if(!(new File(CHILIES_PATH)).mkdir()) {
                throw new IOException("Error creating ");
            }
        }


        List<CommandData> commands = new LinkedList<>();

        //temporary, remove when class loader is working
        chiliObjects.add(new com.cornchip.capsaicin.chilies.Uwu());
        chiliObjects.add(new com.cornchip.capsaicin.chilies.Network());
        chiliObjects.add(new com.cornchip.capsaicin.chilies.UserReactions());
        chiliObjects.add(new com.cornchip.capsaicin.chilies.Typo());
        chiliObjects.add(new com.cornchip.capsaicin.chilies.BetterUnits());

        for(Chili c: chiliObjects) {
            if (c.getCommands() != null)
                commands.addAll(c.getCommands());
        }

        return commands;
    }

    public static String getChiliesPath() {
        return CHILIES_PATH;
    }

    private static JSONObject loadConfig() throws IOException{
        //check if config file exists. If not, create it
        File f = new File(CONFIG_PATH);
        if(!f.exists()) {
            System.out.println("config.json not found! Generating...");
            try {
                if (!f.createNewFile()) {
                    throw new IOException("Error creating config.json!");
                }

                //write our json fields to our new file (Using linked hashmap, this SHOULD give the keys a consistent order)
                LinkedHashMap<String, Object> configMap = new LinkedHashMap<>();
                configMap.put("globalUpdateOnStart", true);
                configMap.put("testingMode", false);
                configMap.put("testGuildID", null);
                configMap.put("playingMessage", "Created by spitzeqc");
                configMap.put("token", "Put token here");


                String config = new JSONObject(configMap).toJSONString();

                //add newlines to help separate each option
                config = config.replace("{", "{\n");
                config = config.replace(",", ",\n");
                config = config.replace("}", "\n}");

                FileWriter fw = new FileWriter(f);
                fw.write(config);
                fw.flush();
                fw.close();

                return null;
            } catch (IOException ioe) {
                throw new IOException(ioe.getMessage());
            }
        }

        //read json from "config.json"
        JSONObject configJson = null;
        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(new FileReader(CONFIG_PATH));
            configJson = (JSONObject) obj;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return configJson;
    }

    public static void main(String[] args) throws LoginException, InterruptedException {

        //load config from json
        JSONObject configJson = null;
        try {
            configJson = loadConfig();
            if (configJson == null) {
                System.out.println("Please check config.json");
                return;
            }
        } catch (IOException ioe) {
            System.err.println("Error has occurred reading config.json\n" + ioe.getMessage());
        }

        //load json to variables
        String token = (String) configJson.get("token");
        String playingMessage = (String) configJson.get("playingMessage");
        boolean testingMode = (boolean) configJson.get("testingMode");
        long testGuildId = (long) configJson.get("testGuildID");
        boolean globalUpdate = (boolean) configJson.get("globalUpdateOnStart");


        //build the bot
        JDABuilder builder = JDABuilder.createDefault(token);
        builder.setActivity(Activity.playing(playingMessage));
        builder.addEventListeners(new CapsaicinBot());
        JDA bot = builder.build();
        bot.awaitReady(); //Dont want things to run before bot is ready

        if(globalUpdate)
            bot.updateCommands().queue(); //delete all old commands

        Guild testGuild = null;
        if(testingMode)
            testGuild = bot.getGuildById(testGuildId); //get the object for the test server

        List<CommandData> commands;
        try {
            commands = loadChilies();
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
            return;
        }

        if(testGuild != null) { //make sure i haven't destroyed the test server
            testGuild.updateCommands().queue(); //reset commands list
            for(CommandData cd: commands) {
                testGuild.upsertCommand(cd).queue();
            }
        }

        if(!testingMode)
            for (CommandData cd: commands)
                bot.upsertCommand(cd).queue();

        System.out.printf("Loaded %d commands%n", commands.size());
    }

    //Discord JDA functions
    @Override
    public void onGenericEvent(@NotNull GenericEvent event) {
        for(Chili c: chiliObjects)
            c.runEvents(event);
    }
    @Override
    public void onGenericUpdate(@NotNull UpdateEvent<?,?> updateEvent){
        for(Chili c: chiliObjects)
            c.runUpdateEvent(updateEvent);
    }

}