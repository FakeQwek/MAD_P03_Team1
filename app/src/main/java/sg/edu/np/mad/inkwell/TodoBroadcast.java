package sg.edu.np.mad.inkwell;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TodoBroadcast extends BroadcastReceiver {
    // Setup for notification to be sent
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "Todo")
                .setSmallIcon(R.drawable.eye_outline)
                .setContentTitle("TODO")
                .setContentText("todo")
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);

        Date date = new Date();
        int id = Integer.parseInt(new SimpleDateFormat("ddHHmmss",  Locale.US).format(date));

        notificationManagerCompat.notify(id, builder.build());
    }
}
