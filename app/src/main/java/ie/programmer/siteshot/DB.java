package ie.programmer.siteshot;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class DB {
    private static final String SEP = "‚‗‚";
    private static final String IMAGE_DATA_DIRECTORY = "ImageCache";
    private static final String LAST_SEARCH_TIME = "LAST_SEARCH_TIME";

    Context mContext;
    SharedPreferences mSharedPreferences;
    File mFolder = null;

    public DB(Context context) {
        mContext = context;
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    public int getInt(String key) {
        return getInt(key, 0);
    }

    public int getInt(String key, int defaultValue) {
        return mSharedPreferences.getInt(key, defaultValue);
    }

    public long getLong(String key) {
        return getLong(key, 0L);
    }

    public long getLong(String key, long defaultValue) {
        return mSharedPreferences.getLong(key, defaultValue);
    }

    public String getString(String key) {
        return getString(key, "");
    }

    public String getString(String key, String defaultValue) {
        return mSharedPreferences.getString(key, defaultValue);
    }

    public double getDouble(String key) {
        return getDouble(key, 0d);
    }

    public double getDouble(String key, double defaultValue) {
        String number = getString(key);
        try {
            return Double.parseDouble(number);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public float getFloat(String key) {
        return getFloat(key, 0f);
    }

    public float getFloat(String key, float defaultValue) {
        return mSharedPreferences.getFloat(key, defaultValue);
    }

    public int getLastSearchTime() {
        return getInt(LAST_SEARCH_TIME, 1000);
    }

    public void putLastSearchTime(int t) {
        putInt(LAST_SEARCH_TIME, t);
    }

    public void putInt(String key, int value) {
        mSharedPreferences.edit().putInt(key, value).apply();
    }

    public void putLong(String key, long value) {
        mSharedPreferences.edit().putLong(key, value).apply();
    }

    public void putDouble(String key, double value) {
        putString(key, String.valueOf(value));
    }

    public void putString(String key, String value) {
        mSharedPreferences.edit().putString(key, value).apply();
    }

    public void putList(String key, ArrayList<String> arr) {
        putString(key, TextUtils.join(SEP, arr));
    }

    public String putBitmap(String imageName, Bitmap bitmap) {
        String fullPath = setupImageFolder(imageName);
        saveBitmap(fullPath, bitmap);
        return fullPath;
    }

    public ArrayList<String> getList(String key) {
        String[] list = TextUtils
                .split(mSharedPreferences.getString(key, ""), SEP);
        return new ArrayList<>(
                Arrays.asList(list));
    }

    public void putListInt(String key, ArrayList<Integer> arr) {
        String data = TextUtils.join(SEP, arr);
        mSharedPreferences.edit().putString(key, data).apply();
    }

    public void putListLong(String key, ArrayList<Long> arr) {
        String data = TextUtils.join(SEP, arr);
        mSharedPreferences.edit().putString(key, data).apply();
    }

    private String setupImageFolder(String imageName) {
        mFolder = new File(Environment.getExternalStorageDirectory(), IMAGE_DATA_DIRECTORY);
        if (!mFolder.exists()) {
            if (!mFolder.mkdirs()) {
                L.e("While creating save path",
                        "Default Save Path Creation Error");
            }
        }
        return mFolder.getPath() + '/' + imageName;
    }

    private boolean saveBitmap(String filename, Bitmap bitmap) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(filename);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public ArrayList<Integer> getListInt(String key) {
        String[] list = TextUtils
                .split(mSharedPreferences.getString(key, ""), SEP);
        ArrayList<String> slist = new ArrayList<>(
                Arrays.asList(list));
        ArrayList<Integer> result = new ArrayList<>();
        for (int i = 0; i < slist.size(); i++) {
            result.add(Integer.parseInt(slist.get(i)));
        }
        return result;
    }

    public ArrayList<Long> getListLong(String key) {
        String[] list = TextUtils
                .split(mSharedPreferences.getString(key, ""), SEP);
        ArrayList<String> slist = new ArrayList<>(
                Arrays.asList(list));
        ArrayList<Long> result = new ArrayList<>();
        for (int i = 0; i < slist.size(); i++) {
            result.add(Long.parseLong(slist.get(i)));
        }
        return result;
    }

    public void putListBoolean(String key, ArrayList<Boolean> arr) {
        ArrayList<String> list = new ArrayList<>();
        for (Boolean b : arr) {
            if (b) {
                list.add("true");
            } else {
                list.add("false");
            }
        }
        putList(key, list);
    }

    public ArrayList<Boolean> getListBoolean(String key) {
        ArrayList<String> list = getList(key);
        ArrayList<Boolean> result = new ArrayList<>();
        for (String b : list) {
            if (b.equals("true")) {
                result.add(true);
            } else {
                result.add(false);
            }
        }
        return result;
    }

    public void putBoolean(String key, boolean value) {
        mSharedPreferences.edit().putBoolean(key, value).apply();
    }

    public boolean getBoolean(String key) {
        return mSharedPreferences.getBoolean(key, false);
    }

    public void putFloat(String key, float value) {
        mSharedPreferences.edit().putFloat(key, value).apply();
    }

    public void remove(String key) {
        mSharedPreferences.edit().remove(key).apply();
    }

    public boolean deleteImage(String path) {
        return new File(path).delete();
    }

    public void clear() {
        mSharedPreferences.edit().clear().apply();
    }

    public Map<String, ?> getAll() {
        return mSharedPreferences.getAll();
    }

    public void registerOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        mSharedPreferences.registerOnSharedPreferenceChangeListener(listener);
    }

    public void unregisterOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        mSharedPreferences.unregisterOnSharedPreferenceChangeListener(listener);
    }
}

