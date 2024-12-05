import spark.Spark;

public class Main {
    private static Crypto crypto = new Crypto();
    private static Mongo mongo = new Mongo(crypto);
    private static Routes routes = new Routes(mongo, crypto);
    
    public static void main(String[] args) {
        Spark.port(5002);
        Spark.get("/auths", routes.routeAuth);
        Spark.options("/auths", routes.routeOptions);
        Spark.get("/auths/check", routes.routeAuthCheck);
        Spark.options("/auths/check", routes.routeOptions);
        Spark.get("/posts/:date", routes.routeDate);
        Spark.get("/posts", routes.routeAll);
        Spark.get("/images/:oidString", routes.routeImage);
        Spark.post("/images", routes.routeUploadImage);
        Spark.options("/images", routes.routeOptions);
        Spark.post("/posts", routes.routeUploadPost);
        Spark.options("/posts", routes.routeOptions);
        Spark.get("/endpoints", routes.routeEndpoints);
    }
}