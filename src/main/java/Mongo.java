import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import org.bson.BsonArray;
import org.bson.BsonObjectId;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONArray;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.InsertOneResult;

public class Mongo {
    private static ConnectionString connectionString = Credentials.connectionString;
    private static MongoClientSettings clientSettings = MongoClientSettings.builder().applyConnectionString(connectionString)
            .serverApi(ServerApi.builder().version(ServerApiVersion.V1).build()).build();
    private static MongoClient client = MongoClients.create(clientSettings);

    /**
     * Gets all documents from the posts collection and puts them in an ArrayList.
     * @return an ArrayList of JSON strings from MongoDB
     */
    public static ArrayList<String> getAllPosts() {
        try {
            MongoDatabase db = client.getDatabase("mightyPirates");
            FindIterable<Document> docs = db.getCollection("posts").find();
            ArrayList<String> docsJSON = new ArrayList<String>();
            docs.forEach(doc -> docsJSON.add(doc.toJson()));
            return docsJSON;
        }
        catch(Exception e) {
            System.out.println(e);
            return new ArrayList<String>();
        }
    }

    /**
     * Gets a post that was posted on the given datestring
     * @param dateString The datestring to search for formatted MMDDYY, i.e. 092123
     * @return The JSON string returned from the MongoDB posts collection
     */
    public static String getPost(String dateString) {
        try {
            MongoDatabase db = client.getDatabase("mightyPirates");
            MongoCollection<Document> posts = db.getCollection("posts");
            String doc = posts.find(Filters.eq("dateString", dateString)).first().toJson();
            return doc;
        }
        catch(Exception e) {
            System.out.println(e);
            return "";
        }
    }

    /**
     * Gets an image in base64 from the GridFS bucket stored in the MongoDB database
     * @param oid the hex string _id of the image to get
     * @return a base64 encoded string image (WITH leading data URL format, i.e. "data:image/[format];base64,")
     */
    public static String getImage(String oid) {
        try {
            MongoDatabase db = client.getDatabase("mightyPirates");
            GridFSBucket bucket = GridFSBuckets.create(db, "images");
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bucket.downloadToStream(new ObjectId(oid), outputStream);

            String imageb64 = outputStream.toString();
            return "{\"fileName\": \"" + bucket.find(Filters.eq("_id", new BsonObjectId(new ObjectId(oid)))).first().getFilename() + "\",\"data\": \"" + imageb64 + "\"}";
        }
        catch (Exception e) {
            System.out.println(e);
            return "";
        }
    }

    /**
     * Uploads an image from a base64 string with a given filename to the GridFS bucket stored in the MongoDB database
     * @param imageb64 the base64 encoded image (WITHOUT leading data URL format, i.e. "data:image/[format];base64,")
     * @param fileName the filename to associate with the file, i.e. "file.jpeg"
     * @return the hex string _id of the inserted image
     */
    public static String putImage(String imageb64, String fileName) {
        try {
            MongoDatabase db = client.getDatabase("mightyPirates");
            GridFSBucket bucket = GridFSBuckets.create(db, "images");
            InputStream baseStream = new ByteArrayInputStream(imageb64.getBytes(StandardCharsets.UTF_8));
            ObjectId id = bucket.uploadFromStream(fileName, baseStream);
            return id.toHexString();
        }
        catch(Exception e) {
            System.out.println(e);
            // TODO: better error handling
            return "Error" + e;
        }
    }

    /**
     * Adds a post to the posts collection with the given date, images, and description
     * @param dateString a datestring formatted mmddyy, i.e. 092123 for September 21, 2023
     * @param imageIDs a JSON array of hex string _ids, i.e. ["65128e7bf44ec02f9eac0f66", "65128e7bf44ec02f9eac0f67", "65128e7bf44ec02f9eac0f68"]
     * @param description a description of the day's meeting
     * @return the hex string _id of the inserted post document
     */
    public static String putPost(String dateString, JSONArray imageIDs, String description) {
        try {
            ArrayList<BsonObjectId> ids = new ArrayList<BsonObjectId>();
            for(int i = 0; i < imageIDs.length(); i++) {
                ids.add(i, new BsonObjectId(new ObjectId(imageIDs.getString(i))));
            }
            MongoDatabase db = client.getDatabase("mightyPirates");
            MongoCollection<Document> posts = db.getCollection("posts");
            InsertOneResult result = posts.insertOne(new Document("dateString", dateString).append("images", new BsonArray(ids)).append("description", description));
            return result.getInsertedId().asObjectId().getValue().toHexString();
        }
        catch(Exception e) {
            System.out.println(e);
            return "Error" + e;
        }
    }
}
