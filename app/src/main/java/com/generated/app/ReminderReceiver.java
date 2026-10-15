package com.generated.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class ReminderReceiver extends BroadcastReceiver {

    public static final String EXTRA_NOTE_ID = "extra_note_id";
    public static final String EXTRA_NOTE_TITLE = "extra_note_title";
    public static final String EXTRA_NOTE_CONTENT = "extra_note_content";
    public static final String CHANNEL_ID = "notes_reminders_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        long noteId = intent.getLongExtra(EXTRA_NOTE_ID, 0);
        String title = intent.getStringExtra(EXTRA_NOTE_TITLE);
        String content = intent.getStringExtra(EXTRA_NOTE_CONTENT);

        if (title == null || title.isEmpty()) {
            title = context.getString(R.string.app_name);
        }

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.reminder_channel_name);
            String description = context.getString(R.string.reminder_channel_desc);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.enableVibration(true);
            notificationManager.createNotificationChannel(channel);
        }

        Intent openIntent = new Intent(context, NoteEditActivity.class);
        openIntent.putExtra(NoteEditActivity.EXTRA_NOTE_ID, noteId);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, (int) noteId, openIntent, flags);

        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(context, CHANNEL_ID);
        } else {
            builder = new Notification.Builder(context);
        }

        builder.setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(context.getString(R.string.reminder_notification_title, title))
                .setContentText(content != null && !content.isEmpty() ? content : "יש לך תזכורת עבור פתק זה")
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_ALL);

        notificationManager.notify((int) (noteId > 0 ? noteId : System.currentTimeMillis()), builder.build());
    }
}
