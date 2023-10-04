import spark.Spark;

public class Main {
    private static String[] validUNames = {"lacedwithennui", "site", "owenburden", "aidanmuldoon"};
    private static Mongo mongo = new Mongo();
    private static Routes routes = new Routes(mongo);
    
    public static void main(String[] args) {
        Spark.port(8080);
        Spark.get("/db/:uname/posts/:date", routes.routeDate);
        Spark.get("/db/:uname/posts", routes.routeAll);
        Spark.get("/db/:uname/images/:oidString", routes.routeImage);
        Spark.post("/db/:uname/upload/images", routes.routeUploadImage);
        Spark.post("/db/:uname/upload/posts", routes.routeUploadPost);
        Spark.options("/db/:uname/upload/posts", routes.routeOptions);
        Spark.get("/db/auths", routes.routeAuth);
        Spark.options("/db/auths", routes.routeOptions);
        Spark.get("/db/auths/check", routes.routeAuthCheck);
        Spark.options("/db/auths/check", routes.routeOptions);
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