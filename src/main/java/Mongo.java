import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.Set;

import org.bson.BsonArray;
import org.bson.BsonDateTime;
import org.bson.BsonObjectId;
import org.bson.Document;
import org.bson.conversions.Bson;
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
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;

public class Mongo {
    private ConnectionString connectionString = Credentials.connectionString;
    private MongoClientSettings clientSettings = MongoClientSettings.builder().applyConnectionString(connectionString)
            .serverApi(ServerApi.builder().version(ServerApiVersion.V1).build()).build();
    private MongoClient client = MongoClients.create(clientSettings);
    private MongoDatabase db;
    private MongoDatabase notesDB;
    private Crypto crypto;

    public Mongo(Crypto crypto) {
        this.crypto = crypto;
        try {
            this.db = client.getDatabase("mightyPirates");
            this.notesDB = client.getDatabase("notes");
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets all documents from the posts collection and puts them in an ArrayList.
     * @return an ArrayList of JSON strings from MongoDB
     */
    public ArrayList<String> getAllPosts() {
        try {
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
    public String getPost(String dateString) {
        try {
            
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
    public String getImage(String oid) {
        try {
            
            GridFSBucket bucket = GridFSBuckets.create(db, "images");
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            GridFSFile fileToDownload = bucket.find(Filters.eq("_id", new BsonObjectId(new ObjectId(oid)))).first();
            bucket.downloadToStream(new ObjectId(oid), outputStream);

            String imageb64 = outputStream.toString();
            return "{\"fileName\": \"" + fileToDownload.getFilename() + "\", \"featured\":" + fileToDownload.getMetadata().get("featured") + ", \"data\": \"" + imageb64 + "\"}";
        }
        catch (Exception e) {
            e.printStackTrace();
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
    public String putImage(String imageb64, String fileName, Boolean featured) {
        try {
            
            GridFSBucket bucket = GridFSBuckets.create(db, "images");
            MongoCollection<Document> imagesCollection = db.getCollection("images.files");
            GridFSUploadOptions options = new GridFSUploadOptions().metadata(new Document("featured", featured));
            InputStream baseStream = new ByteArrayInputStream(imageb64.getBytes(StandardCharsets.UTF_8));
            ObjectId id = bucket.uploadFromStream(fileName, baseStream, options);
            GridFSFile inserted = bucket.find(Filters.eq("_id", new BsonObjectId(id))).first();
            Bson query = Filters.and(
                    Filters.eq("filename", fileName),
                    Filters.eq("length", inserted.getLength()),
                    Filters.eq("metadata.featured", featured)
            );
            long count = imagesCollection.countDocuments(query);
            if(count > 1) {
                bucket.delete(id);
            }
            id = bucket.find(query).first().getObjectId();
            return id.toHexString();
        }
        catch(Exception e) {
            System.out.println(e);
            // TODO: better error handling
            return "{\"error\": \"" + e + "\"}";
        }
    }

    /**
     * Adds a post to the posts collection with the given date, images, and description
     * @param dateString a datestring formatted mmddyy, i.e. 092123 for September 21, 2023
     * @param imageIDs a JSON array of hex string _ids, i.e. ["65128e7bf44ec02f9eac0f66", "65128e7bf44ec02f9eac0f67", "65128e7bf44ec02f9eac0f68"]
     * @param description a description of the day's meeting
     * @return the hex string _id of the inserted post document
     */
    public String putPost(String dateString, JSONArray imageIDs, String description) {
        try {
            ArrayList<BsonObjectId> ids = new ArrayList<BsonObjectId>();
            for(int i = 0; i < imageIDs.length(); i++) {
                ids.add(i, new BsonObjectId(new ObjectId(imageIDs.getString(i))));
            }
            
            MongoCollection<Document> posts = db.getCollection("posts");
            InsertOneResult result = posts.insertOne(new Document("dateString", dateString).append("images", new BsonArray(ids)).append("description", description));
            return result.getInsertedId().asObjectId().getValue().toHexString();
        }
        catch(Exception e) {
            System.out.println(e);
            return "{\"error\": \"" + e + "\"}";
        }
    }

    public String putNote(String dateString, String fileName, String contentHTML, String uoid) {
        try {
            MongoCollection<Document> notes = notesDB.getCollection("notes");
            MongoCollection<Document> users = notesDB.getCollection("users");
            InsertOneResult result = notes.insertOne(
                    new Document("dateString", dateString)
                    .append("fileName", fileName)
                    .append("contentHTML", contentHTML)
                    .append("owner", new BsonObjectId(new ObjectId(uoid))));
            String insertedID = result.getInsertedId().asObjectId().getValue().toHexString();
            UpdateResult userResult = users.updateOne(Filters.eq(new BsonObjectId(new ObjectId(uoid))), Updates.addToSet("notes", new BsonObjectId(new ObjectId(insertedID))));
            System.out.println(userResult.getMatchedCount());
            return result.getInsertedId().asObjectId().getValue().toHexString();
        }
        catch(Exception e) {
            System.out.println(e);
            return "{\"error\": \"" + e + "\"}";
        }
    }

    public String updateNote(String newContent, String oid) {
        try {
            MongoCollection<Document> notes = notesDB.getCollection("notes");
            notes.updateOne(Filters.eq(new BsonObjectId(new ObjectId(oid))), Updates.set("contentHTML", newContent));
            return "{\"oid\": \"" + oid + "\"}";
        }
        catch(Exception e) {
            System.out.println(e);
            return "{\"error\": \"" + e + "\"}";
        }
    }

    public String getNote(String oid) {
        try {
            MongoCollection<Document> notes = notesDB.getCollection("notes");
            String doc = notes.find(Filters.eq(new BsonObjectId(new ObjectId(oid)))).first().toJson();
            return doc;
        }
        catch(Exception e) {
            System.out.println(e);
            return "";
        }
    }

    public String renameNote(String oid, String newName) {
        try {
            MongoCollection<Document> posts = notesDB.getCollection("notes");
            posts.updateOne(Filters.eq(new BsonObjectId(new ObjectId(oid))), Updates.set("fileName", newName));
            return "{\"status\": \"success\"}";
        }
        catch(Exception e) {
            System.out.println(e);
            return "";
        }
    }

    public String getAllNoteIDs(String uoid) {
        try {
            // Document doc = notesDB.getCollection("users").find(Filters.eq(new BsonObjectId(new ObjectId(uoid)))).first();
            FindIterable<Document> allNotes = notesDB.getCollection("notes").find(Filters.eq("owner", new BsonObjectId(new ObjectId(uoid)))).projection(Projections.include("fileName"));
            ArrayList<String> content = new ArrayList<String>();
            allNotes.forEach((doc) -> {
                content.add(doc.toJson());
            });
            String finalJSON = "{\"files\": [";
            for (String object : content) {
                finalJSON += object + ",";
            }
            finalJSON = finalJSON.substring(0, finalJSON.length() - 1); // remove trailing comma for valid json
            finalJSON += "]}";
            System.out.println(finalJSON);
            return finalJSON;
        }
        catch(Exception e) {
            System.out.println(e);
            return "{\"error\": \"" + e + "\"}";
        }
    }

    public String deleteNote(String oid) {
        try {
            MongoCollection<Document> notes = notesDB.getCollection("notes");
            MongoCollection<Document> users = notesDB.getCollection("users");
            notes.deleteOne(Filters.eq(new BsonObjectId(new ObjectId(oid))));
            users.updateOne(Filters.eq("notes", new BsonObjectId(new ObjectId(oid))), Updates.pull("notes", new BsonObjectId(new ObjectId(oid))));
            return "{\"status\": \"success\"}";
        }
        catch(Exception e) {
            e.printStackTrace();
            return "{\"error\": \"" + e + "\"}";
        }
    }

    /**
     * Checks whether the given credentials are stored in the database and if they match each other.
     * @param uname the plaintext username string
     * @param pword the plaintext password string
     * @return true if the credentials are in the database and match with each other.
     */
    public boolean checkCredentials(String uname, String pword) {
        boolean valid = false;
        try {
            
            MongoCollection<Document> auth = db.getCollection("auth");
            Document creds = auth.find().first();
            Set<String> keys = creds.keySet();
            keys.remove("_id");
            keys.remove("activeSessions");
            String[] keysArray = keys.toArray(new String[1]);
            for(int i = 0; i < keysArray.length; i++) {
                String person = keysArray[i];
                if(!person.equals("_id")) {
                    if(((Document) creds.get(person)).get("username").toString().equals(uname)) {
                        if(this.crypto.decrypt(((Document) creds.get(person)).get("password").toString()).equals(this.crypto.saltedHash(pword, uname))) {
                            valid = true;
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return valid;
    }

    /**
     * Creates a random eight-character string that will serve as an access token for verified users.
     * @return the random eight-character access token.
     */
    public String createToken(String username) {
        Random generator = new Random();
        long seed = generator.nextInt(10000) * username.hashCode();
        generator.setSeed(seed);
        String generatedString = generator.ints(97, 122 + 1)
            .limit(16)
            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
            .toString();
        try {
            
            MongoCollection<Document> auth = db.getCollection("auth");
            auth.findOneAndUpdate(Filters.empty(), Updates.push("activeSessions",
                    new Document("token", generatedString).append("dateTime", new BsonDateTime(new Date().getTime())).append("username", username)));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return generatedString;
    }

    /**
     * Checks whether the given token is associated with a valid active session from the database.
     * @param token the token to check
     * @return true if the token is stored in the database in an active session.
     */
    @SuppressWarnings("unchecked")
    public boolean checkToken(String token) {
        boolean valid = false;
        try {
            
            MongoCollection<Document> auth = db.getCollection("auth");
            Document creds = auth.find().first();
            ArrayList<Document> active = (ArrayList<Document>) creds.get("activeSessions");
            for(Document session : active) {
                if(session.get("token").equals(token)) {
                    valid = true;
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return valid;
    }

    /**
     * Deletes any tokens from the active sessions array that are over 3 hours old.
     */
    public void deleteExpired() {
        try {
            
            MongoCollection<Document> auth = db.getCollection("auth");
            auth.findOneAndUpdate(Filters.empty(), Updates.pull("activeSessions",
                    Filters.lt("dateTime", new BsonDateTime(new Date().getTime() - 3 * 60 * 60 * 1000)))); // 3 * 60 * 60 * 1000 = 3 hours
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
}
