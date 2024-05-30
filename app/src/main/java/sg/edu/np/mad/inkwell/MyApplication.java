package sg.edu.np.mad.inkwell;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        resetSharedPreferencesToDefault();
    }

    private void resetSharedPreferencesToDefault() {
        SharedPreferences sharedPreferences = getSharedPreferences("userPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("email", "user@gmail.com");
        editor.putString("password", "Pa$$w0rd");
        editor.apply();
    }
}

