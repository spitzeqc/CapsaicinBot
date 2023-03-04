package com.cornchip.capsaicin;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
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
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * The primary class for the bot.
 */
public class CapsaicinBot extends ListenerAdapter {
    private static final String CONFIG_PATH = "config.json";
    private static final String CHILIES_PATH = "chilies/";
    private static ServiceLoader<Chili> chiliServiceLoader; // contains a class object for each registered Chili
    private static MongoClient sharedMongoClient = null;
    private static Economy economyObject = null;

    /**
     * Used for getting the
     * @return Returns the shared economy object
     */
    public static Economy getEconomyObject() { return economyObject; }

    //run each jar and/or class file and have it return a CommandData object, which will then be loaded by the main bot?
    /**
     * Returns a List of commands loaded from each Chili.
     * Populates the 'chiliServiceLoader' list in the process
     * <p>
     * Current implementation requires each Chili to be hardcoded into the 'chiliServiceLoader' list.
     * This is subject to change
     *
     * @return List of commands from each Chili
     * @throws IOException
     */
    private static List<CommandData> loadChilies() throws IOException {
        //see if directory exists, if not create it
        if(!Files.isDirectory(Paths.get(CHILIES_PATH))) {
            System.out.println("Chilies directory not found. Creating...");
            if(!(new File(CHILIES_PATH)).mkdir()) {
                throw new IOException("Error creating " + CHILIES_PATH);
            }
        }

        List<CommandData> commands = new LinkedList<>();

        // Create list of JarFile using jars found in CHILIES_PATH
        File chiliDir = new File(CHILIES_PATH);
        if(!chiliDir.exists() || !chiliDir.isDirectory()) {
            throw new IOException(CHILIES_PATH + " is not a valid directory");
        }

        /*LinkedList<JarFile> jarFiles = new LinkedList<>();
        for ( File f : Objects.requireNonNull(chiliDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".jar"))) ) {
            jarFiles.add(new JarFile(f.getPath()));
        }*/

        //get list of jars
        File[] jars = chiliDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getPath().toLowerCase().endsWith(".jar");
            }
        });

        URL[] urls = new URL[jars.length];
        for(int i=0; i<jars.length; ++i) {
            urls[i] = jars[i].toURI().toURL();
        }
        URLClassLoader ucl = new URLClassLoader(urls);

        //load each Chili
        ServiceLoader<Chili> sl = ServiceLoader.load(Chili.class, ucl);
        Iterator<Chili> apit = sl.iterator();
        while(apit.hasNext()) {
            List<CommandData> c = apit.next().getCommands();
            if (c != null)
                commands.addAll(c);
        }

        //for( JarFile j : jarFiles ) {}

/*
        //temporary, remove when class loader is working
        chiliObjects.add(new com.cornchip.capsaicin.chilies.Uwu());
        //chiliObjects.add(new com.cornchip.capsaicin.chilies.Network());
        chiliObjects.add(new com.cornchip.capsaicin.chilies.UserReactions());
        chiliObjects.add(new com.cornchip.capsaicin.chilies.Typo());
        chiliObjects.add(new com.cornchip.capsaicin.chilies.BetterUnits());

        for(Chili c: chiliObjects) {
            if (c.getCommands() != null)
                commands.addAll(c.getCommands());
        }
 */
        return commands;
    }

    /**
     * Used for getting the
     * @return
     */
    public static String getChiliesPath() { return CHILIES_PATH; }

    /**
     * Reads and returns a JSONObject of the config file.
     * If no config is found, a config file will be generated at CONFIG_PATH and populated with default values and returns null
     *
     * @return JSON of our config. Returns null if config file was generated
     * @throws IOException
     */
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
                configMap.put("playingMessage", "Powered by CapsaicinBot");
                configMap.put("dbUri", "localhost:27017");
                configMap.put("economyEnabled", true);
                configMap.put("economyTableName", "Economy");
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

                System.out.println("Config file created! Make sure to add your bot token before re-running!");

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
        long testGuildId = -1;
        if (testingMode)
            testGuildId = (long) configJson.get("testGuildID");
        boolean globalUpdate = (boolean) configJson.get("globalUpdateOnStart");
        boolean economyEnabled = (boolean) configJson.get("economyEnabled");
        String dbUri = (String) configJson.get("dbUri");
        String economyTableName = (String) configJson.get("economyTableName");

        //open db connection (if necessary)
        if(economyEnabled) {
            sharedMongoClient = MongoClients.create(dbUri);
        }

        //load Economy (if necessary)
        if(economyEnabled) {
            economyObject = new Economy(sharedMongoClient, economyTableName);
        }

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
    /**
     * Executed on every Discord event, calls the 'runEvents' method of every registered Chili
     *
     * @see Chili
     * @param event The GenericEvent that just occurred
     */
    @Override
    public void onGenericEvent(@NotNull GenericEvent event) {
        for(Chili c: chiliServiceLoader)
            c.runEvents(event);
    }

    /**
     * Executed on every Discord update event, calls the 'runUpdateEvent' method of every registered Chili
     *
     * @see Chili
     * @param updateEvent The UpdateEvent that just occurred
     */
    @Override
    public void onGenericUpdate(@NotNull UpdateEvent<?,?> updateEvent){
        for(Chili c: chiliServiceLoader)
            c.runUpdateEvent(updateEvent);
    }

    /**
     *
     */
    protected class EconomyUpdate {

    }
}