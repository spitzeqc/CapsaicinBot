package com.cornchip.capsaicin;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.InsertOneResult;
import org.bson.Document;

import static com.mongodb.client.model.Filters.eq;

public class Economy {
    public static final int DEFAULT_START_BALANCE = 10;
    private MongoClient dbClient;
    private String dbName;

    /**
     *
     * @param dbClient MongoClient for database connection
     * @param dbName Name of table to work with
     */
    public Economy(MongoClient dbClient, String dbName) {
        this.dbClient = dbClient;
        this.dbName = dbName;
    }

    /**
     * Check if specified amount of currency is present for user
     *
     * @param guildId ID of guild (string)
     * @param userId ID of user (string)
     * @param amount Minimum amount of currency user should have
     * @return Return true of amount of currency is greater or equal to the amount provided
     */
    protected boolean verifyFunds(String guildId, String userId, int amount) {
        MongoDatabase database = dbClient.getDatabase(dbName);
        MongoCollection<Document> collection = database.getCollection(guildId);

        Document doc = collection.find(eq("user", userId)).first();
        //insert missing user
        if(doc == null) {
            doc = new Document()
                    .append("user", userId)
                    .append("balance", DEFAULT_START_BALANCE);

            try {
                InsertOneResult res = collection.insertOne(doc);
            }
            catch (MongoException me) {
                System.err.println("Unable to insert user due to error: " + me);
                return false;
            }
        }

        return doc.getInteger("user") >= amount;
    }

    /**
     * Add currency to a user
     * @param guildId ID of guild (string)
     * @param userId ID of user (string)
     * @param amount Amount of currency to add
     */
    protected void addFunds(String guildId, String userId, int amount) {
        MongoDatabase db = dbClient.getDatabase(dbName);
        BasicDBObject newDoc = new BasicDBObject()
                .append("$inc", new BasicDBObject()
                        .append("balance", amount));

        db.getCollection(guildId).updateOne(new BasicDBObject().append("user", userId), newDoc);
    }

    /**
     * Remove currency from a user. Does NOT verify user has enough funds
     * @param guildId ID of guild (string)
     * @param userId ID of user (string)
     * @param amount Amount of currency to remove
     */
    protected void removeFunds(String guildId, String userId, int amount) {
        this.addFunds(guildId, userId, -amount);
    }
}
