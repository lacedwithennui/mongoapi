import spark.Spark;

public class Main {
    private static Crypto crypto = new Crypto();
    private static Mongo mongo = new Mongo(crypto);
    private static Routes routes = new Routes(mongo, crypto);
    
    public static void main(String[] args) {
        Spark.port(8080);
        Spark.get("/db/auths", routes.routeAuth);
        Spark.options("/db/auths", routes.routeOptions);
        Spark.get("/db/auths/check", routes.routeAuthCheck);
        Spark.options("/db/auths/check", routes.routeOptions);
        Spark.get("/db/posts/:date", routes.routeDate);
        Spark.get("/db/posts", routes.routeAll);
        Spark.get("/db/images/:oidString", routes.routeImage);
        Spark.get("/db/note/:oidString", routes.routeNote);
        Spark.post("/db/upload/note", routes.routeUploadNote);
        Spark.post("/db/note/rename", routes.routeRenameNote);
        Spark.get("/db/notes/:uoid", routes.routeAllFileOIDs);
        Spark.post("/db/update/note", routes.routeUpdateNote);
        Spark.post("/db/delete/note", routes.routeDeleteNote);
        Spark.post("/db/upload/images", routes.routeUploadImage);
        Spark.options("/db/upload/images", routes.routeOptions);
        Spark.post("/db/upload/posts", routes.routeUploadPost);
        Spark.options("/db/upload/posts", routes.routeOptions);
    }
}