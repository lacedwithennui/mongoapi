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
                if(validUname(request.params("uname"))) {
                    response.status(200);
                    JSONObject json = new JSONObject(request.body());
                    String id = Mongo.putPost(json.getString("dateString"), json.getJSONArray("images"), json.getString("description"));
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
        Spark.port(8080);
        Spark.get("/db/:uname/posts/:date", routeDate);
        Spark.get("/db/:uname/posts", routeAll);
        Spark.get("/db/:uname/images/:oidString", routeImage);
        Spark.post("/db/:uname/upload/images", routeUploadImage);
        Spark.post("/db/:uname/upload/posts", routeUploadPost);
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