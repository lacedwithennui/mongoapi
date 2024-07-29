import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;
import java.lang.StringBuilder;

import org.json.JSONObject;

public class UploadThing {
    public UploadThing() {

    }

    private HTTPHelper.Response uploadOneImage(String fileName, String imageData) {
        try {
            Random generator = new Random();
            long seed = generator.nextInt(10000) * fileName.hashCode();
            generator.setSeed(seed);
            String generatedString = generator.ints(97, 122 + 1)
                .limit(8)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

            String mostLikelyUniqueName = generatedString + "-" + fileName;

            JSONObject json = new JSONObject();
            json.put(fileName, mostLikelyUniqueName);
            json.put("length", ((4 * imageData.length() / 3) + 3) & ~3);
            json.put("type", fileName.split(".")[-1]);

            URL url = new URL("https://api.uploadthing.com/v6/uploadFiles");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("x-uploadthing-api-key", Credentials.UPLOADTHING_SECRET);
            connection.setFixedLengthStreamingMode(json.toString().getBytes().length);
            connection.connect();
            connection.getOutputStream().write(json.toString().getBytes());

            return new HTTPHelper.Response();
        }
        catch(Exception e) {
            return new HTTPHelper.Response().withHeader("error", e);
        }
    }
}

