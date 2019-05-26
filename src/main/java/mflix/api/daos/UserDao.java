package mflix.api.daos;

import com.mongodb.MongoClientSettings;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import mflix.api.models.Session;
import mflix.api.models.User;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

@Configuration
public class UserDao extends AbstractMFlixDao {

    private final MongoCollection<User> usersCollection;
    private final MongoCollection<Session> sessionsCollection;

    private final Logger log;

    @Autowired
    public UserDao(
            MongoClient mongoClient, @Value("${spring.mongodb.database}") String databaseName) {
        super(mongoClient, databaseName);
        CodecRegistry pojoCodecRegistry =
                fromRegistries(
                        MongoClientSettings.getDefaultCodecRegistry(),
                        fromProviders(PojoCodecProvider.builder().automatic(true).build()));

        usersCollection = db.getCollection("users", User.class).withCodecRegistry(pojoCodecRegistry);
        log = LoggerFactory.getLogger(this.getClass());
        sessionsCollection = db.getCollection("sessions", Session.class).withCodecRegistry(pojoCodecRegistry);
    }

    /**
     * Inserts the `user` object in the `users` collection.
     *
     * @param user - User object to be added
     * @return True if successful, throw IncorrectDaoOperation otherwise
     */
    public boolean addUser(User user) {
        try {
            usersCollection.withWriteConcern(WriteConcern.MAJORITY).insertOne(user);
            return true;
        } catch (Exception e) {
            throw new IncorrectDaoOperation("insert error has error: " + e.getMessage(), e);
        }
    }

    /**
     * Creates session using userId and jwt token.
     *
     * @param userId - user string identifier
     * @param jwt    - jwt string token
     * @return true if successful
     */
    public boolean createUserSession(String userId, String jwt) {

        Session userSession = sessionsCollection.find(Filters.eq("user_id", userId)).first();

        if (userSession == null) {
            userSession = new Session();
            userSession.setUserId(userId);
            userSession.setJwt(jwt);

            sessionsCollection.withWriteConcern(WriteConcern.MAJORITY).insertOne(userSession);
        }
        return true;

    }

    /**
     * Returns the User object matching the an email string value.
     *
     * @param email - email string to be matched.
     * @return User object or null.
     */
    public User getUser(String email) {
        return usersCollection.find(Filters.eq("email", email)).first();
    }

    /**
     * Given the userId, returns a Session object.
     *
     * @param userId - user string identifier.
     * @return Session object or null.
     */
    public Session getUserSession(String userId) {
        return sessionsCollection.find(Filters.eq("user_id", userId)).first();
    }

    public boolean deleteUserSessions(String userId) {
        sessionsCollection.deleteOne(Filters.eq("user_id", userId));
        return true;
    }

    /**
     * Removes the user document that match the provided email.
     *
     * @param email - of the user to be deleted.
     * @return true if user successfully removed
     */
    public boolean deleteUser(String email) {

        try {
            User user = getUser(email);
            if (user == null) {
                return true;
            }

            deleteUserSessions(email);
            usersCollection.withWriteConcern(WriteConcern.MAJORITY).deleteOne(Filters.eq("email", email));

            return true;
        } catch (Exception e) {
            log.error("delete error", e);
            return false;
        }
    }

    /**
     * Updates the preferences of an user identified by `email` parameter.
     *
     * @param email           - user to be updated email
     * @param userPreferences - set of preferences that should be stored and replace the existing
     *                        ones. Cannot be set to null value
     * @return User object that just been updated.
     */
    public boolean updateUserPreferences(String email, Map<String, ?> userPreferences) {

        if (userPreferences == null) {
            return false;
        }

        try {

            Map<String, String> map = new HashMap<>();
            userPreferences.keySet().forEach(k -> {
                if (userPreferences.get(k) != null) {
                    map.put(k, userPreferences.get(k).toString());
                }
            });

            User user = getUser(email);
            user.setPreferences(map);

            usersCollection.updateOne(Filters.eq("email", email), new Document("$set", user));

            return true;
        } catch (Exception e) {
            log.error("update user " + email + " error", e);
            return false;
        }
    }
}
