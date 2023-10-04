import java.util.Base64;

import org.json.JSONObject;

import spark.Request;
import spark.Response;
import spark.Route;

public class Routes {
    public final Route routeDate, routeAll, routeImage, routeUploadImage, routeUploadPost, routeOptions, routeAuth, routeAuthCheck;
    public Routes(Mongo mongo) {
        this.routeDate = new Route() {
            @Override
            public Object handle(Request request, Response response) {
                response.type("json");
                response.status(200);
                response.body(mongo.getPost(request.params("date")));
                response.header("Access-Control-Allow-Origin", "*");
                response.header("Access-Control-Allow-Methods", "GET");
                return response.body();
            }
        };
        this.routeAll = new Route() {
            @Override
            public Object handle(Request request, Response response) {
                response.type("json");
                response.status(200);
                response.body("{\"posts\": " + mongo.getAllPosts().toString() + "}");
                response.header("Access-Control-Allow-Origin", "*");
                response.header("Access-Control-Allow-Methods", "GET");
                return response.body();
            }
        };
        this.routeImage = new Route() {
            @Override
            public Object handle(Request request, Response response) {
                response.type("json");
                response.status(200);
                response.body(mongo.getImage(request.params("oidString")));
                response.header("Access-Control-Allow-Origin", "*");
                response.header("Access-Control-Allow-Methods", "GET");
                return response.body();
            }
        };
        this.routeUploadImage = new Route() {
            @Override
            public Object handle(Request request, Response response) {
                response.status(200);
                JSONObject json = new JSONObject(request.body());
                String id = mongo.putImage(json.get("data").toString(), json.get("fileName").toString(), json.getBoolean("featured"));
                response.body("{\"uploadedID\": \"" + id + "\"}");
                response.header("Access-Control-Allow-Origin", "*");
                response.header("Access-Control-Allow-Methods", "GET");
                return response.body();
            }
        };
        this.routeUploadPost = new Route() {
            @Override
            public Object handle(Request request, Response response) {
                response.header("Access-Control-Allow-Origin", "*");
                response.header("Access-Control-Allow-Methods", "POST");
                response.header("Access-Control-Allow-Headers", "Authorization");
                response.header("Access-Control-Allow-Credentials", "true");
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
        this.routeAuth = new Route() {
            @Override
            public Object handle(Request request, Response response) {
                response.status(200);
                response.header("Access-Control-Allow-Origin", "*");
                response.header("Access-Control-Allow-Methods", "GET");
                response.header("Access-Control-Allow-Headers", "authorization");
                response.header("Access-Control-Allow-Credentials", "true");
                mongo.deleteExpired();
                try {
                    String creds = new String(Base64.getDecoder().decode(request.headers("authorization").substring("Basic ".length())));
                    String uname = creds.split(":")[0];
                    String pword = creds.split(":")[1];
                    if(mongo.checkCredentials(uname, pword)) {
                        return "{\"token\": \"" + mongo.createToken() + "\"}";
                    }
                    else {
                        response.status(401);
                        return "{\"error\": \"401: Wrong username or password.\"}";
                    }
                }
                catch(Exception e) {
                    e.printStackTrace();
                    return "{\"error\": \"" + e.getMessage() + "\"}";
                }
            }
        };
        this.routeAuthCheck = new Route() {
            @Override
            public Object handle(Request request, Response response) {
                mongo.deleteExpired();
                response.header("Access-Control-Allow-Origin", "*");
                response.header("Access-Control-Allow-Methods", "GET");
                response.header("Access-Control-Allow-Headers", "authorization");
                response.header("Access-Control-Allow-Credentials", "true");
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
