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
import android.view.View.OnClickListener;
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
import de.greenrobot.event.util.AsyncExecutor.RunnableEx;
import ie.programmer.siteshot.R.color;
import ie.programmer.siteshot.R.id;
import ie.programmer.siteshot.R.layout;

public class MainActivity extends AppCompatActivity {

    private static final String api = "https://www.googleapis.com/pagespeedonline/v1/runPagespeed?screenshot=true&strategy=mobile&url=";
    private ShareActionProvider mShareActionProvider;

    private void showError(String errorMessage) {
        if (errorMessage != null) {
            Snackbar.make(getCurrentFocus(), errorMessage, Snackbar.LENGTH_LONG)
                    .show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(layout.activity_main);
        Toolbar toolbar = (Toolbar) this.findViewById(id.toolbar);
        this.setSupportActionBar(toolbar);

        final EditText tv = (EditText) this.findViewById(id.urlText);
        final ImageView iv = (ImageView) this.findViewById(id.imageView);

        final FloatingActionButton share = (FloatingActionButton) this.findViewById(id.share);
        share.setBackgroundTintList(this.getResources().getColorStateList(color.colorPrimary));
        share.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.doShare();
            }
        });

        FloatingActionButton fab = (FloatingActionButton) this.findViewById(id.fab);
        fab.setBackgroundTintList(this.getResources().getColorStateList(color.colorPrimary));
        fab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = String.valueOf(tv.getText());
                MainActivity.this.queryMovieDb(url, new MainActivity.Response() {
                            @Override
                            public void handle(String response) {
                                try {
                                    JSONObject obj = new JSONObject(response);
                                    JSONObject screenshot = obj.getJSONObject("screenshot");
                                    String data = new String(screenshot.get("data").toString());
                                    data = data.replace("_", "/");
                                    data = data.replace("-", "+");
                                    byte[] decoded = Base64.decode(data, Base64.DEFAULT);
                                    final String imagePath = MainActivity.this.getPictureFile();
                                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(imagePath));
                                    bos.write(decoded);
                                    bos.flush();
                                    bos.close();
                                    MainActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                FileInputStream fis = new FileInputStream(new File(imagePath));
                                                iv.setImageBitmap(BitmapFactory.decodeStream(fis));
                                                fis.close();
                                                share.setVisibility(View.VISIBLE);
                                            } catch (Exception ex) {
                                                MainActivity.this.showError(ex.getMessage());
                                            }
                                        }
                                    });
                                } catch (Exception ex) {
                                    MainActivity.this.showError(ex.getMessage());
                                }
                            }
                        },
                        new MainActivity.ErrorResponse() {
                            @Override
                            public void handle(String errorMessage) {
                                MainActivity.this.showError(errorMessage);
                            }
                        });
            }
        });
    }

    private String getPictureFile() {
        return this.getFilesDir().getAbsolutePath() + "/" + "website.jpg";
    }

    private String processRequest(String request) {
        if (!(request.startsWith("http://") || request.startsWith("https://"))) {
            return "http://" + request;
        }
        return request;
    }

    private void queryMovieDb(final String request, final MainActivity.Response response, final MainActivity.ErrorResponse errorResponse) {
        AsyncExecutor.create().execute(
                new RunnableEx() {
                    @Override
                    public void run() throws Exception {
                        URL url = new URL(MainActivity.api + MainActivity.this.processRequest(request));
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
                                MainActivity.this.showError("Failed : HTTP error code : "
                                        + conn.getResponseCode());
                                //    errorResponse.handle(parseError(conn.g));
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
        this.getMenuInflater().inflate(menu.menu_main, menu);
        MenuItem menuItem = menu.findItem(id.menu_item_share);
        this.mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
        return true;
    }

    private void doShare() {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/jpeg");
        share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///" + this.getPictureFile()));
        this.startActivity(Intent.createChooser(share, "Share Image"));
    }

    private void setShareIntent(Intent shareIntent) {
        if (this.mShareActionProvider != null) {
            this.mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == id.menu_item_share) {
            this.doShare();
        }

        return super.onOptionsItemSelected(item);
    }

    public interface ErrorResponse {
        void handle(String errorMessage);
    }

    public interface Response {
        void handle(String response);
    }
}
