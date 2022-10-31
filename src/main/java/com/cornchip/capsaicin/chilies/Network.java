package com.cornchip.capsaicin.chilies;

import com.cornchip.capsaicin.Chili;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.UpdateEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Network extends Chili {

    private static final String CHILIES_PATH = com.cornchip.capsaicin.CapsaicinBot.getChiliesPath();
    private static final String RESOURCES_PATH = "Network/";
    private static final String CONFIG_FILE = "config.json";
    private static final String CONFIG_PATH = CHILIES_PATH  + RESOURCES_PATH + CONFIG_FILE;

    private String ztNetworkId;
    private String ztApiToken;
    private String jellyfinApiToken;
    private String jellyfinUrl;
    private JSONArray jellyfinInclude;

    public Network() {
        try {
            JSONObject config = loadConfig();
            if (config == null) {
                System.out.println("Please check " + CONFIG_PATH);
                return;
            }
            this.jellyfinApiToken = (String) config.get("jellyfinApiToken");
            this.jellyfinUrl = (String) config.get("jellyfinURL");
            this.ztNetworkId = (String) config.get("zerotierNetworkId");
            this.ztApiToken = (String) config.get("zerotierApiToken");

            Object[] foldersArrayObject = ((JSONArray) config.get("jellyfinInclude")).toArray();
            String[] foldersArray = new String[foldersArrayObject.length];
            for(int i=0; i<foldersArrayObject.length; ++i){
                    foldersArray[i] = (String) foldersArrayObject[i];
            }

            this.jellyfinInclude = this.findJellyfinFolders(foldersArray);


        } catch (IOException ioe) {
            System.err.println("Error has occurred reading " + CONFIG_PATH + '\n' + ioe.getMessage());
        }
    }

    private static JSONObject loadConfig() throws IOException{
        //check if config file exists. If not, create it
        File f = new File(CONFIG_PATH);
        if(!f.exists()) {
            System.out.println(CONFIG_PATH + " not found! Generating...");

            if(!Files.isDirectory(Paths.get((CHILIES_PATH + RESOURCES_PATH)))) {
                System.out.println(RESOURCES_PATH + " not found. Creating...");
                if(!(new File((CHILIES_PATH + RESOURCES_PATH))).mkdir()) {
                    throw new IOException("Error creating '" + RESOURCES_PATH + "' please create manually");
                }
            }

            try {
                if (!f.createNewFile()) {
                    throw new IOException("Error creating " + CONFIG_PATH);
                }

                //write our json fields to our new file (Using linked hashmap, this SHOULD give the keys a consistent order)
                LinkedHashMap<String, Object> configMap = new LinkedHashMap<>();
                configMap.put("jellyfinURL", "http://localhost:8096");
                configMap.put("jellyfinApiToken", "Put token here");
                configMap.put("jellyfinInclude", new JSONArray());
                configMap.put("zerotierNetworkId", "Put network id here");
                configMap.put("zerotierApiToken", "Put token here");


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

    private JSONArray findJellyfinFolders(String[] names) throws IOException {
        String urlString = this.jellyfinUrl + "/Library/VirtualFolders";
        HttpClient httpClient = HttpClientBuilder.create().build();

        HttpGet request = new HttpGet(urlString);
        request.addHeader("X-MediaBrowser-Token", this.jellyfinApiToken);
        request.addHeader("Content-Type", "application/json");

        //get list of folders
        HttpResponse response = httpClient.execute(request);
        if(response.getStatusLine().getStatusCode() != 200) {
            System.err.println(response.getStatusLine().getStatusCode());
            return null;
        }

        //convert response to json
        String responseJsonString = EntityUtils.toString(response.getEntity());
        JSONParser parser = new JSONParser();
        JSONArray responseJsonArray = null;
        try {
            Object obj = parser.parse(responseJsonString);
            responseJsonArray = (JSONArray) obj;
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(responseJsonArray == null)
            return null;

        //create hashmap of
        //HashMap<String, String> returnData = new HashMap<>();
        JSONArray returnData = new JSONArray();
        for(Object f: responseJsonArray) {
            JSONObject json = (JSONObject) f;
            String folderName = (String) json.get("Name");
            for(String n: names) {
                if(n.equals(folderName)) {
                    returnData.add(json.get("ItemId"));
                    break;
                }
            }
        }

        return returnData;
    }

    private boolean addJellyfinUser(String username) {
        Map<String, Object> data = new HashMap<>();
        data.put("Name", username);
        data.put("Password", "password");
        String json = new JSONObject(data).toJSONString();

        String urlString = this.jellyfinUrl + "/Users/New";

        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpResponse response = null;
        int responseCode = -1;
        try {
            HttpPost request = new HttpPost(urlString);
            StringEntity params = new StringEntity(json);

            //add data to post request object
            request.addHeader("X-MediaBrowser-Token", this.jellyfinApiToken);
            request.addHeader("Content-Type", "application/json");
            request.setEntity(params);

            //execute request
            response = httpClient.execute(request);
            responseCode = response.getStatusLine().getStatusCode();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (responseCode != 200)
            return false;

        JSONObject responseJson = null;
        try {
            String responseJsonString = EntityUtils.toString(response.getEntity());
            JSONParser parser = new JSONParser();
            try {
                Object obj = parser.parse(responseJsonString);
                responseJson = (JSONObject) obj;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        if (responseJson != null) {
            String jellyfinId = (String) responseJson.get("Id");
            JSONObject policy = (JSONObject) responseJson.get("Policy");

            urlString = this.jellyfinUrl + "/Users/" + jellyfinId + "/Policy";

            try {
                HttpPost request = new HttpPost(urlString);
                policy.put("EnableAllFolders", false);
                policy.put("EnabledFolders", this.jellyfinInclude);

                //add data to post request object
                request.addHeader("X-MediaBrowser-Token", this.jellyfinApiToken);
                request.addHeader("Content-Type", "application/json");
                //StringEntity policyString = new StringEntity(responseJson.toJSONString());
                StringEntity policyString = new StringEntity(policy.toJSONString());
                request.setEntity(policyString);


                response = httpClient.execute(request);
                responseCode = response.getStatusLine().getStatusCode();

                if(responseCode == 204)
                    return true;
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        return false;
    }

    private boolean authorizeNode(String nodeId, String name) {
        Map<String, Object> config = new HashMap<>();
        config.put("authorized", true);

        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("config", new JSONObject(config));

        String json = new JSONObject(data).toJSONString();

        String urlString = String.format("https://my.zerotier.com/api/v1/network/%s/member/%s", this.ztNetworkId, nodeId);

        //String postData = String.format("\"name\"=\"%s\"&config={\"authorized\": true}", name);

        HttpClient httpClient = HttpClientBuilder.create().build();
        try {
            //create post and post data request objects
            HttpPost request = new HttpPost(urlString);
            StringEntity params = new StringEntity(json);

            //add data to post request object
            request.addHeader("Authorization", "bearer " + this.ztApiToken);
            request.addHeader("Content-Type", "application/json");
            request.setEntity(params);

            //execute request
            HttpResponse response = httpClient.execute(request);
            int responseCode = response.getStatusLine().getStatusCode();
            return responseCode==200;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void runCommands(SlashCommandEvent event) {
        if (event.getName().equals("network")) {
            String subcommand = event.getSubcommandName();
            switch(subcommand) {
                case "register":
                    String username = event.getUser().getName();
                    String nodeId = event.getOption("node_id").getAsString(); //required option, cannot be null
                    if(nodeId.length() != 10){
                        event.reply("Error: Node ID `" + nodeId + "` is invalid!").setEphemeral(true).queue();
                        break;
                    }

                    if(this.authorizeNode(nodeId, username)) {
                        if(this.addJellyfinUser(username))
                            event.reply("Successfully registered on to network").setEphemeral(true).queue();
                        else
                            event.reply("Successfully connected to Zerotier, but an error occurred while creating jellyfin account. Please try again later").setEphemeral(true).queue();
                    } else {
                        event.reply("An error occurred while registering").setEphemeral(true).queue();
                    }
                    break;
                case "download":
                    event.reply("https://www.zerotier.com/download/").setEphemeral(true).queue();
                    break;
                case "network":
                    event.reply("The ZeroTier network ID is `" + this.ztNetworkId + '`').setEphemeral(true).queue();
                    break;
                case "instructions":
                    MessageEmbed embed = new EmbedBuilder().setTitle("Connecting to the network")
                            .addField("First", "Go to https://www.zerotier.com/download/ and install the program", false)
                            .addField("Second", "Open ZeroTier and connect to the network `" + this.ztNetworkId + '`', false)
                            .addField("Third", "Run the command `/network register <node_id>`", false)
                            .addField("Fourth", "Open `10.241.0.2:8001` in your browser", false)
                            .addField("Fifth", "Login using your discord username (**not** your nickname) and the password `password`", false)
                            .build();
                    //event.getChannel().sendMessageEmbeds(embed).queue();
                    event.replyEmbeds(embed).setEphemeral(true).queue();
                    break;
                default:
                    event.reply("Unknown subcommand entered").setEphemeral(true).queue();
            }
        }
    }

    @Override
    public void runEvents(GenericEvent event) {
        if (event instanceof SlashCommandEvent)
            runCommands((SlashCommandEvent) event);

    }

    @Override
    public void runUpdateEvent(UpdateEvent<?, ?> updateEvent) {

    }

    @Override
    public List<CommandData> getCommands() {
        List<CommandData> commands = new LinkedList<>();
        CommandData networkCommand = new CommandData("network", "Access the private network");

        SubcommandData registerSubcommand = new SubcommandData("register", "Register yourself on the private network");
        registerSubcommand.addOption(OptionType.STRING, "node_id", "Your ZeroTier node id", true);
        SubcommandData instructionsSubcommand = new SubcommandData("instructions", "Instructions on how to access the network");

        networkCommand.addSubcommands(registerSubcommand, instructionsSubcommand);

        commands.add(networkCommand);

        return commands;
    }
}
