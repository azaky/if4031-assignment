package if4031.mongodb.twitterclient;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import javafx.util.Pair;
import org.bson.Document;
import org.bson.types.BSONTimestamp;
import org.bson.types.ObjectId;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

/**
 * Created by Zaky on 14/11/2015.
 */
public class TwitterClient {

    private static final String DATABASE_NAME = "willyzaky";

    private String username;
    private final MongoClient mongoClient;
    private final MongoDatabase database;

    public TwitterClient(MongoClient mongoClient, MongoDatabase database) {
        this.mongoClient = mongoClient;
        this.database = database;
    }

    public static TwitterClient create(List<String> serverAddresses, String databaseName) {
        List<ServerAddress> addresses = new ArrayList<ServerAddress>();
        for (String address : serverAddresses) {
            addresses.add(new ServerAddress(address));
        }
        MongoClient mongoClient = new MongoClient(addresses);
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        System.out.println("Successfully connected to Server");
        return new TwitterClient(mongoClient, database);
    }

    public void close() {
        mongoClient.close();
    }

    public void register(String username, String password) {
        Document doc = new Document()
                .append("username", username)
                .append("password", Utils.hash(password));
        database.getCollection("users").insertOne(doc);
        this.username = username;
        System.out.println("Register successful");
    }

    public boolean login(String username, String password) {
        MongoCursor<Document> cursor = database.getCollection("users")
                .find(and(eq("username", username),
                        eq("password", Utils.hash(password)))).iterator();
        if (!cursor.hasNext()) {
            System.out.println("Login unsuccessful");
            return false;
        } else {
            System.out.println("Login successful");
            this.username = username;
            return true;
        }
    }

    public void follow(String followingUsername) {
        BSONTimestamp since = new BSONTimestamp();
        database.getCollection("friends").insertOne(new Document()
                .append("username", username)
                .append("friends", followingUsername)
                .append("since", since));
        database.getCollection("followers").insertOne(new Document()
                .append("username", followingUsername)
                .append("follower", username)
                .append("since", since));
    }

    public void tweet(String body) {
        ObjectId id = new ObjectId();
        BSONTimestamp now = new BSONTimestamp();
        database.getCollection("tweets").insertOne(new Document()
                .append("tweet_id", id)
                .append("username", username)
                .append("body", body));
        database.getCollection("userline").insertOne(new Document()
                .append("tweet_id", id)
                .append("username", username)
                .append("time", now));
        List<String> followers = getFollowers();
        for (String follower : followers) {
            database.getCollection("timeline").insertOne(new Document()
                    .append("tweet_id", id)
                    .append("username", follower)
                    .append("time", now));
        }
    }

    public List<String> getFollowers() {
        List<String> followers = new ArrayList<String>();
        MongoCursor<Document> cursor = database.getCollection("followers")
                .find(eq("username", username)).iterator();
        try {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                followers.add(doc.getString("follower"));
            }
        } finally {
            cursor.close();
        }
        return followers;
    }

    public List<String> getTweet(String username) {
        List<String> tweets = new ArrayList<String>();
        MongoCursor<Document> cursor = database.getCollection("userline")
                .find(eq("username", username)).iterator();
        MongoCollection<Document> tweetCollection = database.getCollection("tweets");
        try {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                ObjectId id = doc.getObjectId("tweet_id");
                Document tweetDoc = tweetCollection.find(eq("tweet_id", id)).first();
                tweets.add("@" + tweetDoc.getString("username") + ": " + tweetDoc.getString("body"));
            }
        } finally {
            cursor.close();
        }
        return tweets;
    }

    public List<String> getTimeline() {
        List<String> tweets = new ArrayList<String>();
        MongoCursor<Document> cursor = database.getCollection("timeline")
                .find(eq("username", username)).iterator();
        MongoCollection<Document> tweetCollection = database.getCollection("tweets");
        try {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                ObjectId id = doc.getObjectId("tweet_id");
                Document tweetDoc = tweetCollection.find(eq("tweet_id", id)).first();
                tweets.add("@" + tweetDoc.getString("username") + ": " + tweetDoc.getString("body"));
            }
        } finally {
            cursor.close();
        }
        return tweets;
    }

    public void run() {
        Scanner sc = new Scanner(System.in);
        while (true) {
            String command = sc.next();
            if (command.compareTo("login") == 0) {
                String username = sc.next();
                String password = sc.next();
                login(username, password);
            } else if (command.compareTo("register") == 0) {
                String username = sc.next();
                String password = sc.next();
                register(username, password);
            } else if (command.compareTo("follow") == 0) {
                String usernameToFollow = sc.next();
                follow(usernameToFollow);
            } else if (command.compareTo("tweet") == 0) {
                String tweetBody = sc.nextLine();
                tweet(tweetBody);
            } else if (command.compareTo("show") == 0) {
                command = sc.next();
                if (command.compareTo("tweet") == 0) {
                    String username = sc.next();
                    List<String> tweets = getTweet(username);
                    for (String tweet : tweets) {
                        System.out.println(tweet);
                    }
                } else if (command.compareTo("timeline") == 0) {
                    List<String> tweets = getTimeline();
                    for (String tweet : tweets) {
                        System.out.println(tweet);
                    }
                }
            } else if (command.compareTo("exit") == 0) {
                close();
                System.out.println("Bye!");
                break;
            }
        }
    }

    public static void main(String[] args) {
        Pair<List<String>, String> config = loadConfig();
        TwitterClient client = TwitterClient.create(config.getKey(), config.getValue());
        client.run();
    }

    private static Pair<List<String>, String> loadConfig() {
        List<String> addresses = new ArrayList<String>();
        String dbname = "willyzaky";
        FileInputStream input = null;
        try {
            input = new FileInputStream("config.properties");
            Properties properties = new Properties();
            properties.load(input);
            String[] addressesArray = properties.getProperty("mongodb_servers", "localhost").split(",");
            for (String address : addressesArray) {
                addresses.add(address);
            }
            dbname = properties.getProperty("mongodb_name", "willyzaky");
        } catch (IOException e) {
            e.printStackTrace();
            addresses.clear();
            addresses.add("localhost");
            dbname = "willyzaky";
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return new Pair<List<String>, String>(addresses, dbname);
    }
}
