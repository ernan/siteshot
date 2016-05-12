package ie.programmer.siteshot;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import de.greenrobot.event.EventBus;
import ie.programmer.siteshot.R.color;
import ie.programmer.siteshot.R.id;
import ie.programmer.siteshot.R.layout;

public class MainActivity extends AppCompatActivity {
    private static final String LAST_URL = "LAST_URL";

    ImageView iv;
    FloatingActionButton share;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(id.toolbar);
        setSupportActionBar(toolbar);

        final EditText tv = (EditText) findViewById(id.urlText);
        final DB db = new DB(getApplicationContext());
        tv.setText(db.getString(LAST_URL, "http://programmer.ie"));
        tv.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event != null) {
                    if ((actionId == EditorInfo.IME_ACTION_GO
                            && event.getAction() == KeyEvent.ACTION_DOWN) ||
                            (actionId == EditorInfo.IME_ACTION_DONE
                                    && event.getAction() == KeyEvent.ACTION_DOWN)) {
                        String url = String.valueOf(tv.getText());
                        db.putString(LAST_URL, url);
                        EventBus.getDefault().post(url);
                    }
                }
                return true;
            }
        });

        iv = (ImageView) findViewById(id.imageView);

        share = (FloatingActionButton) findViewById(id.share);
        share.setBackgroundTintList(getResources().getColorStateList(color.colorPrimary));
        share.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("image/jpeg");
                String fileName = Util.writeToExternal(MainActivity.this, "website.jpg");
                share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(fileName)));
                startActivity(Intent.createChooser(share, "Share Image"));
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(id.fab);
        fab.setBackgroundTintList(getResources().getColorStateList(color.colorPrimary));
        fab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = String.valueOf(tv.getText());
                db.putString(LAST_URL, url);
                EventBus.getDefault().post(url);
            }
        });
        EventBus.getDefault().register(this);
    }

    public void onEvent(String url) {
        Util.siteShot(url, new Response() {
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
                new Response() {
                    @Override
                    public void handle(String errorMessage) {
                        showError(errorMessage);
                    }
                });
    }


    private String getPictureFile() {
        return getFilesDir().getAbsolutePath() + File.separator  + "website.jpg";
    }

    private void showError(String errorMessage) {
        if (errorMessage != null) {
            L.e(errorMessage);
            Snackbar.make(getCurrentFocus(), errorMessage, Snackbar.LENGTH_LONG)
                    .show();
        }
    }
}
