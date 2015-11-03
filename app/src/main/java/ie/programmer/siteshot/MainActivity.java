package ie.programmer.siteshot;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import de.greenrobot.event.util.AsyncExecutor;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final TextView tv = (TextView)findViewById(R.id.urlText);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = String.valueOf(tv.getText());
                queryMovieDb(url, new Response() {
                            @Override
                            public void handle(String response) {
                                try {
                                    JSONObject obj = new JSONObject(response);

                                } catch(Exception ex) {

                                }
                            }
                        },
                        new ErrorResponse() {
                            @Override
                            public void handle() {

                            }
                        });
            }
        });
    }

    public interface ErrorResponse {
        void handle();
    }

    public interface Response {
        void handle(final String response);
    }


    static final String api = "https://www.googleapis.com/pagespeedonline/v1/runPagespeed?screenshot=true&strategy=mobile&url=";


    public void queryMovieDb(final String request, final Response response, final ErrorResponse errorResponse) {
        AsyncExecutor.create().execute(
                new AsyncExecutor.RunnableEx() {
                    @Override
                    public void run() throws Exception {
                        java.net.URL url = new URL(api + request);
                        try {
                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                            conn.setRequestMethod("GET");
                            conn.setRequestProperty("Accept", "application/json");
                            if (conn.getResponseCode() != 200) {
                                showError(MainActivity.this, "Failed : HTTP error code : "
                                        + conn.getResponseCode());
                                errorResponse.handle();
                            } else {
                                BufferedReader br = new BufferedReader(new InputStreamReader(
                                        (conn.getInputStream())));
                                StringBuilder builder = new StringBuilder();
                                String output;
                                while ((output = br.readLine()) != null) {
                                    builder.append(output);
                                }
                                conn.disconnect();
                                response.handle(builder.toString());
                            }
                        } catch (Exception e) {
                            errorResponse.handle();
                        }
                    }
                }
        );
    }

    public static void showError(Context context, String errorMessage) {
        if (null != errorMessage) {
            Log.e("Error", "Unable to get web page image: " + errorMessage);
            Toast toast = Toast.makeText(context, errorMessage, Toast.LENGTH_LONG);
            TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
            if (v != null) {
                v.setTextColor(Color.RED);
            }
            toast.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
