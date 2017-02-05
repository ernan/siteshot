package ie.programmer.siteshot;

import android.content.Context;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import de.greenrobot.event.util.AsyncExecutor;

public class Util {
    public static final String API = "https://www.googleapis.com/pagespeedonline/v1/runPagespeed?screenshot=true&strategy=mobile&url=";
    public static final String LAST_URL = "LAST_URL";

    public static String writeToExternal(Context context, String filename){
        String newFileName = null;
        try {
            File file = new File(context.getExternalFilesDir(null), filename);
            newFileName = file.getAbsolutePath();
            InputStream is = new FileInputStream(context.getFilesDir() + File.separator + filename);
            OutputStream os = new FileOutputStream(file);
            byte[] toWrite = new byte[is.available()];
            L.i("Available " + is.available());
            int result = is.read(toWrite);
            L.i("Result " + result);
            os.write(toWrite);
            is.close();
            os.close();
            L.i("Copying to " + context.getExternalFilesDir(null) + File.separator + filename);
            L.i("Copying from " + context.getFilesDir() + File.separator + filename);
        } catch (Exception e) {
            Toast.makeText(context, "File write failed: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
        return newFileName;
    }

    public static void siteShot(final String request, final Response response, final Response errorResponse) {
        AsyncExecutor.create().execute(
                new AsyncExecutor.RunnableEx() {
                    @Override
                    public void run() throws Exception {
                        URL url = new URL(API + processRequest(request));
                        try {
                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                            conn.setRequestMethod("GET");
                            conn.setRequestProperty("Accept", "application/json");
                            if (conn.getResponseCode() == 200) {
                                BufferedReader br = new BufferedReader(new InputStreamReader(
                                        conn.getInputStream()));
                                StringBuilder builder = new StringBuilder();
                                String output;
                                while ((output = br.readLine()) != null) {
                                    builder.append(output);
                                }
                                conn.disconnect();
                                response.handle(builder.toString());
                            } else {
                                errorResponse.handle("Failed : HTTP error code : "
                                        + conn.getResponseCode());
                            }
                        } catch (Exception e) {
                            errorResponse.handle(e.getMessage());
                        }
                    }
                }
        );
    }


    public static String processRequest(String request) {
        if (!(request.startsWith("http://") || request.startsWith("https://"))) {
            return "http://" + request;
        }
        return request;
    }

}
