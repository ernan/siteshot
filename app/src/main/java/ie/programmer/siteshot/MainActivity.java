package ie.programmer.siteshot;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import de.greenrobot.event.util.AsyncExecutor;

public class MainActivity extends AppCompatActivity {

    static final String api = "https://www.googleapis.com/pagespeedonline/v1/runPagespeed?screenshot=true&strategy=mobile&url=";
    private ShareActionProvider mShareActionProvider;

    public void showError(String errorMessage) {
        if (null != errorMessage) {
            Snackbar.make(this.getCurrentFocus(), errorMessage, Snackbar.LENGTH_LONG)
                    .show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final EditText tv = (EditText) findViewById(R.id.urlText);
        final ImageView iv = (ImageView) findViewById(R.id.imageView);

        final FloatingActionButton share = (FloatingActionButton) findViewById(R.id.share);
        share.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimary));
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doShare();
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimary));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = String.valueOf(tv.getText());
                queryMovieDb(url, new Response() {
                            @Override
                            public void handle(String response) {
                                try {
                                    JSONObject obj = new JSONObject(response);
                                    JSONObject screenshot = obj.getJSONObject("screenshot");
                                    String data = new String(screenshot.get("data").toString());
                                    data = data.replace("_", "/");
                                    data = data.replace("-", "+");
                                    byte[] decoded = Base64.decode(data, Base64.DEFAULT);
                                    final String imagePath = getPictureFile();
                                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(imagePath));
                                    bos.write(decoded);
                                    bos.flush();
                                    bos.close();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                FileInputStream fis = new FileInputStream(new File(imagePath));
                                                iv.setImageBitmap(BitmapFactory.decodeStream(fis));
                                                fis.close();
                                                share.setVisibility(View.VISIBLE);
                                            } catch (Exception ex) {
                                                showError(ex.getMessage());
                                            }
                                        }
                                    });
                                } catch (Exception ex) {
                                    showError(ex.getMessage());
                                }
                            }
                        },
                        new ErrorResponse() {
                            @Override
                            public void handle(final String errorMesage) {
                                showError(errorMesage);
                            }
                        });
            }
        });
    }

    String getPictureFile() {
        return getFilesDir().getAbsolutePath() + "/" + "website.jpg";
    }

    public String processRequest(String request) {
        if (!(request.startsWith("http://") || request.startsWith("https://"))) {
            request = "http://" + request;
        }
        return request;
    }

    public void queryMovieDb(final String request, final Response response, final ErrorResponse errorResponse) {
        AsyncExecutor.create().execute(
                new AsyncExecutor.RunnableEx() {
                    @Override
                    public void run() throws Exception {
                        java.net.URL url = new URL(api + processRequest(request));
                        try {
                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                            conn.setRequestMethod("GET");
                            conn.setRequestProperty("Accept", "application/json");
                            if (conn.getResponseCode() != 200) {
                                showError("Failed : HTTP error code : "
                                        + conn.getResponseCode());
                                //    errorResponse.handle(parseError(conn.g));
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
                            errorResponse.handle(e.getMessage());
                        }
                    }
                }
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem menuItem = menu.findItem(R.id.menu_item_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
        return true;
    }

    void doShare() {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/jpeg");
        share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///" + getPictureFile()));
        startActivity(Intent.createChooser(share, "Share Image"));
    }

    private void setShareIntent(Intent shareIntent) {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_item_share) {
            doShare();
        }

        return super.onOptionsItemSelected(item);
    }

    public interface ErrorResponse {
        void handle(final String errorMessage);
    }

    public interface Response {
        void handle(final String response);
    }
}
