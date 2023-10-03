import java.util.Base64;

import org.json.JSONObject;

import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

public class Main {
    private static String[] validUNames = {"lacedwithennui", "site", "owenburden", "aidanmuldoon"};
    
    public static void main(String[] args) {
        Route routeDate = new Route() {
            @Override
            public Object handle(Request request, Response response) {
                response.type("json");
                if(validUname(request.params("uname"))) {
                    response.status(200);
                    response.body(Mongo.getPost(request.params("date")));
                }
                else {
                    response.status(401);
                    response.body("{'error': '401: You do not have authorization to view this information.'}");
                }
                response.header("Access-Control-Allow-Origin", "*");
                response.header("Access-Control-Allow-Methods", "GET");
                return response.body();
            }
        };
        Route routeAll = new Route() {
            @Override
            public Object handle(Request request, Response response) {
                response.type("json");
                if(validUname(request.params("uname"))) {
                    response.status(200);
                    response.body("{\"posts\": " + Mongo.getAllPosts().toString() + "}");
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
        Route routeImage = new Route() {
            @Override
            public Object handle(Request request, Response response) {
                response.type("json");
                if(validUname(request.params("uname"))) {
                    response.status(200);
                    response.body(Mongo.getImage(request.params("oidString")));
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
        Route routeUploadImage = new Route() {
            @Override
            public Object handle(Request request, Response response) {
                if(validUname(request.params("uname"))) {
                    response.status(200);
                    JSONObject json = new JSONObject(request.body());
                    String id = Mongo.putImage(json.get("data").toString(), json.get("fileName").toString(), json.getBoolean("featured"));
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
        Route routeUploadPost = new Route() {
            @Override
            public Object handle(Request request, Response response) {
                response.header("Access-Control-Allow-Origin", "*");
                response.header("Access-Control-Allow-Methods", "POST");
                response.header("Access-Control-Allow-Headers", "Authorization");
                response.header("Access-Control-Allow-Credentials", "true");
                try {
                    if(Mongo.checkToken(request.headers("Authorization").substring("Bearer ".length()))) {
                        response.status(200);
                        
                        JSONObject json = new JSONObject(request.body());
                        String id = Mongo.putPost(json.getString("dateString"), json.getJSONArray("images"), json.getString("description"));
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
        Route routeOptions = new Route() {
            @Override
            public Object handle(Request request, Response response) {
                response.header("Access-Control-Allow-Origin", "*");
                response.header("Access-Control-Allow-Methods", "POST");
                response.header("Access-Control-Allow-Headers", "Authorization");
                response.header("Access-Control-Allow-Credentials", "true");
                response.status(200);
                return "";
            }
        };
        Route routeAuth = new Route() {
            @Override
            public Object handle(Request request, Response response) {
                response.status(200);
                response.header("Access-Control-Allow-Origin", "*");
                response.header("Access-Control-Allow-Methods", "GET");
                response.header("Access-Control-Allow-Headers", "authorization");
                response.header("Access-Control-Allow-Credentials", "true");
                try {
                    String creds = new String(Base64.getDecoder().decode(request.headers("authorization").substring("Basic ".length())));
                    String uname = creds.split(":")[0];
                    String pword = creds.split(":")[1];
                    return Mongo.checkCredentials(uname, pword) ? "{\"token\": \"" + Mongo.createToken() + "\"}" : "{\"error\": \"401: Wrong username or password.\"}";
                }
                catch(Exception e) {
                    e.printStackTrace();
                    return "{\"error\": \"" + e.getMessage() + "\"}";
                }
            }
        };
        Spark.port(8080);
        Spark.get("/db/:uname/posts/:date", routeDate);
        Spark.get("/db/:uname/posts", routeAll);
        Spark.get("/db/:uname/images/:oidString", routeImage);
        Spark.post("/db/:uname/upload/images", routeUploadImage);
        Spark.post("/db/:uname/upload/posts", routeUploadPost);
        Spark.options("/db/:uname/upload/posts", routeOptions);
        Spark.get("/db/auths", routeAuth);
        Spark.options("/db/auths", routeAuth);
    }

    public static boolean validUname(String uname) {
        boolean valid = false;
        for (String name : validUNames) {
            if(name.toString().equals(uname.toString())) {
                valid = true;
            }
        }
        return valid;
    }
}