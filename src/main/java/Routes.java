import org.json.JSONArray;
import org.json.JSONObject;

import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;
import spark.routematch.RouteMatch;

public class Routes {
    public final Route routeDate, routeAll, routeImage, routeUploadImage, routeUploadPost, routeOptions, routeAuth, routeAuthCheck, routeEndpoints;
    public Routes(Mongo mongo, Crypto auth) {
        // Returns the post that corresponds with the given date.
        this.routeDate = new Route() {
            @Override
            public Object handle(Request request, Response response) {
                response = mongo.getPost(request.params("date")).asSparkResponse(response);
                return response.body();
            }
        };
        // Returns all posts.
        this.routeAll = new Route() {
            @Override
            public Object handle(Request request, Response response) {
                response = mongo.getAllPosts().asSparkResponse(response);
                return response.body();
            }
        };
        // Returns the image that corresponds with the given oid.
        this.routeImage = new Route() {
            @Override
            public Object handle(Request request, Response response) {
                response = mongo.getImage(request.params("oidString")).asSparkResponse(response);
                return response.body();
            }
        };
        // Posts the given image to the database and returns the oid that it was uploaded at
        this.routeUploadImage = new Route() {
            @Override
            public Object handle(Request request, Response response) {
                if(mongo.checkToken(request.headers("Authorization").substring("Bearer ".length()))) {
                    JSONObject json = new JSONObject(request.body());
                    response = mongo.postImage(json.get("data").toString(), json.get("fileName").toString(), json.getBoolean("featured")).asSparkResponse(response);
                }
                else {
                    response = httputils.Response.unauthorizedError().asSparkResponse(response);
                }
                return response.body();
            }
        };
        // Uploads a post to the database and returns the oid that it was uploaded at
        this.routeUploadPost = new Route() {
            @Override
            public Object handle(Request request, Response response) {
                mongo.deleteExpired();
                try {
                    if(mongo.checkToken(request.headers("Authorization").substring("Bearer ".length()))) {
                        JSONObject json = new JSONObject(request.body());
                        response = mongo.putPost(json.getString("dateString"), json.getJSONArray("images"), json.getString("description")).asSparkResponse(response);
                    }
                    else {
                        response = httputils.Response.unauthorizedError().asSparkResponse(response);
                    }
                }
                catch(Exception e) {
                    e.printStackTrace();
                    httputils.Response.defaultServerError(e);
                }
                return response.body();
            }
        };
        // Returns the preflight options for any route
        this.routeOptions = new Route() {
            @Override
            public Object handle(Request request, Response response) {
                response = new httputils.Response().withCode(200)
                    .withAllowAllMethodsHeader()
                    .withHeader("Access-Control-Allow-Headers", "Authorization")
                    .withHeader("Access-Control-Allow-Credentials", "true").asSparkResponse(response);
                return response.body();
            }
        };
        // Checks whether the given encrypted username and password correspond with a user in the database.
        // Returns a randomized access token to be stored in the browser cookies to authenticate.
        this.routeAuth = new Route() {
            @Override
            public Object handle(Request request, Response response) {
                mongo.deleteExpired();
                try {
                    String creds = auth.decrypt(request.headers("authorization").substring("Basic ".length()));
                    String uname = creds.split(":")[0];
                    String pword = creds.split(":")[1];
                    if(mongo.checkCredentials(uname, pword) || mongo.checkCredentials(uname, pword)) {
                        response = mongo.createToken(uname).asSparkResponse(response);
                    }
                    else {
                        response = httputils.Response.unauthorizedError()
                                .withBody("{\"error\": \"Username and/or password are incorrect.\"}")
                                .asSparkResponse(response);
                    }
                    return response.body();
                }
                catch(Exception e) {
                    e.printStackTrace();
                    response = httputils.Response.defaultServerError(e).asSparkResponse(response);
                    return response.body();
                }
            }
        };
        // Checks whether the given access token is valid.
        this.routeAuthCheck = new Route() {
            @Override
            public Object handle(Request request, Response response) {
                mongo.deleteExpired();
                try {
                    if(mongo.checkToken(request.headers("Authorization").substring("Bearer ".length()))) {
                        response = new httputils.Response().withCode(200).asSparkResponse(response);
                        return response.body();
                    }
                    else {
                        response = httputils.Response.unauthorizedError()
                                .withBody("{\"error\": \"Your session is invalid. Please log in again.\"}")
                                .asSparkResponse(response);
                        return response.body();
                    }
                }
                catch(Exception e) {
                    e.printStackTrace();
                    response = httputils.Response.defaultServerError(e).asSparkResponse(response);
                    return response.body();
                }
            }
        };
        this.routeEndpoints = new Route() {
            public Object handle(Request request, Response response) {
                JSONArray json = new JSONArray();
                for(RouteMatch route : Spark.routes()) {
                    json.put(new JSONObject().put("url", route.getMatchUri()).put("method", route.getHttpMethod()));
                }
                response = new httputils.Response().withCode(200).withAllowGetMethodHeader().withBody(json.toString()).asSparkResponse(response);
                return response.body();
            }
        };
    }
}
