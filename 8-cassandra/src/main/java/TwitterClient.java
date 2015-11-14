import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Created by Willy on 11/11/2015.
 */
public class TwitterClient {

    private static final String[] CONTACT_POINTS = {"167.205.35.19", "167.205.35.20", "167.205.35.21", "167.205.35.22"};
    private static final int CONTACT_PORT = 9160;

    private String username;
    private final Cluster cluster;
    private final Session session;

    public void register(String username, String password) {
        session.execute("INSERT INTO users (username, password) VALUES ('" + username + "', '" + password + "')");
        this.username = username;
        System.out.println("Register successful");
    }

    public boolean login(String username, String password) {
        ResultSet result = session.execute("SELECT * FROM users WHERE username = ? AND password = ?", username, password);
        if (result.getAvailableWithoutFetching() == 1) {
            System.out.println("Login successful");
            this.username = username;
            return true;
        } else {
            System.out.println("Login unsuccessful");
            return false;
        }
    }

    public void follow(String followingUsername) {
        session.execute("INSERT INTO followers (username, follower, since) VALUES (?, ?, 'now')", followingUsername, username);
    }

    public void tweet(String body) {
        UUID uuid = UUID.randomUUID();
        session.execute("INSERT INTO tweets (tweet_id, username, body) VALUES (?, ?, ?)", uuid, username, body);
        session.execute("INSERT INTO userline (username, time, tweet_id) VALUES (?, now(), ?)", username, uuid);
        List<String> followers = getFollowers();
        for (String follower : followers) {
            session.execute("INSERT INTO timeline (username, time, tweet_id) VALUES (?, now(), ?)", follower, uuid);
        }
    }

    public List<String> getFollowers() {
        ResultSet result = session.execute("SELECT * FROM followers WHERE username = ?", username);
        final List<String> followers = new ArrayList<String>();
        result.forEach(new Consumer<Row>() {
            public void accept(Row row) {
                followers.add(row.getString("follower"));
            }
        });
        return followers;
    }

    public List<String> getTweet(String username) {
        ResultSet result = session.execute("SELECT * FROM userline WHERE username = ?", username);
        final List<String> tweets = new ArrayList<String>();
        result.forEach(new Consumer<Row>() {
            public void accept(Row row) {
                UUID tweetId = row.getUUID("tweet_id");
                final ResultSet localResult = session.execute("SELECT * FROM tweets WHERE tweet_id = ?", tweetId);
                localResult.forEach(new Consumer<Row>() {
                    public void accept(Row row) {
                        tweets.add(row.getString("body"));
                    }
                });
            }
        });
        return tweets;
    }

    public List<String> getTimeline() {
        ResultSet result = session.execute("SELECT * FROM timeline WHERE username = ?", username);
        final List<String> tweets = new ArrayList<String>();
        result.forEach(new Consumer<Row>() {
            public void accept(Row row) {
                UUID tweetId = row.getUUID("tweet_id");
                final ResultSet localResult = session.execute("SELECT * FROM tweets WHERE tweet_id = ?", tweetId);
                localResult.forEach(new Consumer<Row>() {
                    public void accept(Row row) {
                        tweets.add("@" + row.getString("username") + ": " + row.getString("body"));
                    }
                });
            }
        });
        return tweets;
    }

    public TwitterClient() {
        cluster = Cluster.builder().addContactPoints(CONTACT_POINTS)
                .build();
        session = cluster.connect("willyzaky");
    }

    public static void main(String[] args) {
        TwitterClient client = new TwitterClient();
        System.out.println("Successfully connect to Cassandra");

        Scanner sc = new Scanner(System.in);
        while (true) {
            String command = sc.next();
            if (command.compareTo("login") == 0) {
                String username = sc.next();
                String password = sc.next();
                client.login(username, password);
            } else if (command.compareTo("register") == 0) {
                String username = sc.next();
                String password = sc.next();
                client.register(username, password);
            } else if (command.compareTo("follow") == 0) {
                String usernameToFollow = sc.next();
                client.follow(usernameToFollow);
            } else if (command.compareTo("tweet") == 0) {
                String tweet = sc.nextLine();
                client.tweet(tweet);
            } else if (command.compareTo("show") == 0) {
                command = sc.next();
                if (command.compareTo("tweet") == 0) {
                    String username = sc.next();
                    List<String> tweets = client.getTweet(username);
                    for (String tweet : tweets) {
                        System.out.println(tweet);
                    }
                } else if (command.compareTo("timeline") == 0) {
                    List<String> tweets = client.getTimeline();
                    for (String tweet : tweets) {
                        System.out.println(tweet);
                    }
                }
            } else if (command.compareTo("exit") == 0) {
                client.cluster.close();
                System.out.println("Bye!");
                break;
            }
        }
    }
}
