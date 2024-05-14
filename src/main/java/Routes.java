import org.json.JSONObject;

import spark.Request;
import spark.Response;
import spark.Route;

public class Routes {
    public final Route routeDate, routeAll, routeImage, routeUploadImage, routeUploadPost, routeUploadNote,
            routeAllFileOIDs, routeNote, routeRenameNote, routeUpdateNote, routeDeleteNote, routeOptions, routeAuth, routeAuthCheck;

    public Routes(Mongo mongo, Crypto auth) {
        // Returns the post that corresponds with the given date.
        this.routeDate = new Route() {
            @Override
            public Object handle(Request request, Response response) {
                response.status(200);
                response.body(mongo.getPost(request.params("date")));
                response.header("Access-Control-Allow-Origin", "*");
                response.header("Access-Control-Allow-Methods", "GET");
                return response.body();
            }
        };
        // Returns all posts.
        this.routeAll = new Route() {
            @Override
            public Object handle(Request request, Response response) {
                response.status(200);
                response.body("{\"posts\": " + mongo.getAllPosts().toString() + "}");
                response.header("Access-Control-Allow-Origin", "*");
                response.header("Access-Control-Allow-Methods", "GET");
                return response.body();
            }
        };
        // Returns the image that corresponds with the given oid.
        this.routeImage = new Route() {
            @Override
            public Object handle(Request request, Response response) {
                response.status(200);
                response.body(mongo.getImage(request.params("oidString")));
                response.header("Access-Control-Allow-Origin", "*");
                response.header("Access-Control-Allow-Methods", "GET");
                return response.body();
            }
        };
        // Posts the given image to the database and returns the oid that it was uploaded at
        this.routeUploadImage = new Route() {
            @Override
            public Object handle(Request request, Response response) {
                if(mongo.checkToken(request.headers("Authorization").substring("Bearer ".length()))) {
                    JSONObject json = new JSONObject(request.body());
                    String id = mongo.putImage(json.get("data").toString(), json.get("fileName").toString(), json.getBoolean("featured"));
                    response.status(200);
                    response.body("{\"uploadedID\": \"" + id + "\"}");
                }
                else {
                    response.status(401);
                    response.body("{\"error\": \"401: You do not have authorization to view this information.\"}");
                }
                response.header("Access-Control-Allow-Origin", "*");
                response.header("Access-Control-Allow-Methods", "GET");
                return response.body();
            }
        };
        // Uploads a post to the database and returns the oid that it was uploaded at
        this.routeUploadPost = new Route() {
            @Override
            public Object handle(Request request, Response response) {
                response.header("Access-Control-Allow-Origin", "*");
                mongo.deleteExpired();
                try {
                    if(mongo.checkToken(request.headers("Authorization").substring("Bearer ".length()))) {
                        response.status(200);
                        
                        JSONObject json = new JSONObject(request.body());
                        String id = mongo.putPost(json.getString("dateString"), json.getJSONArray("images"), json.getString("description"));
                        response.body("{\"uploadedID\": \"" + id + "\"}");
                    }
                    else {
                        response.status(401);
                        response.body("{\"error\": \"401: You do not have authorization to view this information.\"}");
                    }
                }
                catch(Exception e) {
                    e.printStackTrace();
                    response.body("{\"error\": \"" + e.getMessage() + "\"}");
                }
                return response.body();
            }
        };
        this.routeUploadNote = new Route() {
            @Override
            public Object handle(Request request, Response response) {
                response.header("Access-Control-Allow-Origin", "*");
                try {
                    response.status(200);
                    JSONObject json = new JSONObject(request.body());
                    String id = mongo.putNote(json.getString("dateString"), json.getString("fileName"), json.getString("contentHTML"), json.getString("owner"));
                    response.body("{\"uploadedID\": \"" + id + "\"}");
                }
                catch(Exception e) {
                    e.printStackTrace();
                    response.body("{\"error\": \"" + e.getMessage() + "\"}");
                }
                return response.body();
            }
        };
        this.routeAllFileOIDs = new Route() {
            @Override
            public Object handle(Request request, Response response) {
                response.status(200);
                response.body(mongo.getAllNoteIDs(request.params("uoid")));
                response.header("Access-Control-Allow-Origin", "*");
                response.header("Access-Control-Allow-Methods", "GET");
                return response.body();
            }
        };
        this.routeNote = new Route() {
            @Override
            public Object handle(Request request, Response response) {
                response.status(200);
                response.body(mongo.getNote(request.params("oidString")));
                response.header("Access-Control-Allow-Origin", "*");
                response.header("Access-Control-Allow-Methods", "GET");
                return response.body();
            }
        };
        this.routeRenameNote = new Route() {
            @Override
            public Object handle(Request request, Response response) {
                response.status(200);
                JSONObject json = new JSONObject(request.body());
                response.body(mongo.renameNote(json.getString("oidString"), json.getString("newName")));
                response.header("Access-Control-Allow-Origin", "*");
                response.header("Access-Control-Allow-Methods", "GET");
                return response.body();
            }
        };
        this.routeUpdateNote = new Route() {
            @Override
            public Object handle(Request request, Response response) {
                response.header("Access-Control-Allow-Origin", "*");
                try {
                    response.status(200);
                    JSONObject json = new JSONObject(request.body());
                    String id = mongo.updateNote(json.getString("contentHTML"), json.getString("oidString"));
                    response.body("{\"uploadedID\": \"" + id + "\"}");
                }
                catch(Exception e) {
                    e.printStackTrace();
                    response.body("{\"error\": \"" + e.getMessage() + "\"}");
                }
                return response.body();
            }
        };
        this.routeDeleteNote = new Route() {
            public Object handle(Request request, Response response) {
                response.header("Access-Control-Allow-Origin", "*");
                response.status(200);
                try {
                    JSONObject json = new JSONObject(request.body());
                    mongo.deleteNote(json.getString("oidString"));
                }
                catch(Exception e) {
                    e.printStackTrace();
                    response.body("{\"error\": \"" + e.getMessage() + "\"}");
                }
                return response.body();
            };
        };
        // Returns the preflight options for any route
        this.routeOptions = new Route() {
            @Override
            public Object handle(Request request, Response response) {
                response.header("Access-Control-Allow-Origin", "*");
                response.header("Access-Control-Allow-Methods", "*");
                response.header("Access-Control-Allow-Headers", "Authorization");
                response.header("Access-Control-Allow-Credentials", "true");
                response.status(200);
                return "";
            }
        };
        // Checks whether the given encrypted username and password correspond with a user in the database.
        // Returns a randomized access token to be stored in the browser cookies to authenticate.
        this.routeAuth = new Route() {
            @Override
            public Object handle(Request request, Response response) {
                mongo.deleteExpired();
                response.header("Access-Control-Allow-Origin", "*");
                try {
                    String creds = auth.decrypt(request.headers("authorization").substring("Basic ".length()));
                    String uname = creds.split(":")[0];
                    String pword = creds.split(":")[1];
                    if(mongo.checkCredentials(uname, pword) || mongo.checkCredentials(uname, pword)) {
                        response.status(200);
                        return "{\"token\": \"" + mongo.createToken(uname) + "\"}";
                    }
                    else {
                        response.status(401);
                        return "{\"error\": \"401: Wrong username or password.\"}";
                    }
                }
                catch(Exception e) {
                    e.printStackTrace();
                    response.status(500);
                    return "{\"error\": \"" + e.getMessage() + "\"}";
                }
            }
        };
        // Checks whether the given access token is valid.
        this.routeAuthCheck = new Route() {
            @Override
            public Object handle(Request request, Response response) {
                mongo.deleteExpired();
                response.header("Access-Control-Allow-Origin", "*");
                try {
                    if(mongo.checkToken(request.headers("Authorization").substring("Bearer ".length()))) {
                        response.status(200);
                        return "{\"status\": \"ok\"}";
                    }
                    else {
                        response.status(401);
                        return "{\"error\": \"401: Wrong username or password.\"}";
                    }
                }
                catch(Exception e) {
                    e.printStackTrace();
                    response.status(500);
                    return "{\"error\": \"" + e.getMessage() + "\"}";
                }
            }
        };
    }
}
