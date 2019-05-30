package mflix;

import com.mongodb.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.WriteModel;
import com.mongodb.connection.SslSettings;
import mflix.api.daos.TicketTest;
import org.bson.Document;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.List;

@SpringBootTest()
@RunWith(SpringJUnit4ClassRunner.class)
public class FinalExample extends TicketTest {

    @Value("${spring.mongodb.uri}")
    private String URI;

    @Test
    public void question3() {
        MongoClientSettings settings = MongoClientSettings.builder().applyConnectionString(new ConnectionString(URI)).build();
        MongoClient mongoClient = MongoClients.create(settings);


        SslSettings sslSettings = settings.getSslSettings();
        ReadPreference readPreference = settings.getReadPreference();
        ReadConcern readConcern = settings.getReadConcern();
        WriteConcern writeConcern = settings.getWriteConcern();

        System.out.println(readPreference.toString());
        System.out.println(sslSettings.isInvalidHostNameAllowed());
        System.out.println(sslSettings.isEnabled());
        System.out.println(readConcern.asDocument().toString());
        System.out.println(writeConcern.asDocument().toString());
    }

    @Test
    public void question5() {
        MongoClient mongoClient = MongoClients.create(URI);
        MongoDatabase db = mongoClient.getDatabase("mflix");
        MongoCollection employeesCollection =
                db.getCollection("employees");

        Document doc1 = new Document("_id", 11)
                .append("name", "Edgar Martinez")
                .append("salary", "8.5M");
        Document doc2 = new Document("_id", 3)
                .append("name", "Alex Rodriguez")
                .append("salary", "18.3M");
        Document doc3 = new Document("_id", 24)
                .append("name", "Ken Griffey Jr.")
                .append("salary", "12.4M");
        Document doc4 = new Document("_id", 11)
                .append("name", "David Bell")
                .append("salary", "2.5M");
        Document doc5 = new Document("_id", 19)
                .append("name", "Jay Buhner")
                .append("salary", "5.1M");

        List<WriteModel> requests = Arrays.asList(
                new InsertOneModel<>(doc1),
                new InsertOneModel<>(doc2),
                new InsertOneModel<>(doc3),
                new InsertOneModel<>(doc4),
                new InsertOneModel<>(doc5));
        try {
            employeesCollection.bulkWrite(requests);
        } catch (Exception e) {
            System.out.println("ERROR: " + e.toString());
        }
    }
}
