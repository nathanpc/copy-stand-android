package com.innoveworkshop.copystand.services;

import static android.widget.Toast.LENGTH_LONG;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.innoveworkshop.copystand.R;
import com.innoveworkshop.copystand.models.Clip;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Background service responsible for monitoring the system's clipboard for changes and
 * synchronizing it with other devices on the network.
 */
public class ClipboardManagerService extends Service {
    private final ArrayList<Clip> clips = new ArrayList();
    private ClipboardUpdateListener clipboardUpdateListener = null;
    private ClipboardManager clipboard;

    private final ServerThread serverThread = new ServerThread();
    private final IBinder binder = new LocalBinder();
    private final String TAG = "CLIPBOARD_SERVICE";
    private final int SERVICE_ID = 2547;
    private final String CHANNEL_ID = "COPYSTAND";

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate()");
        super.onCreate();

        // Get the system clipboard object and add our event listener to it.
        clipboard = (ClipboardManager) getApplicationContext().getSystemService(CLIPBOARD_SERVICE);
        clipboard.addPrimaryClipChangedListener(onPrimaryClipChangedListener);

        // Start the synchronization server thread.
        serverThread.start();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
        super.onDestroy();

        // Remove our clipboard changed event handler and stop the synchronization server thread.
        clipboard.removePrimaryClipChangedListener(onPrimaryClipChangedListener);
        serverThread.interrupt();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand()");

        try {
            // Check if the system's clipboard is available.
            notifyIfClipboardUnaccessible();

            // Start the foreground service with its notification.
            Notification notification = createServiceNotification();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                int type = ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE;
                startForeground(SERVICE_ID, notification, type);
            } else {
                startForeground(SERVICE_ID, notification);
            }
            Log.i(TAG, "Foreground service started");
        } catch (IllegalStateException e) {
            // Starting the foreground service failed, most likely because of a timeout.
            Log.e(TAG, "Could not start the foreground service");
            Log.e(TAG, e.toString());
            Toast.makeText(getApplicationContext(), getString(R.string.service_failed_start),
                    LENGTH_LONG).show();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if (intent.getComponent() != null) {
            Log.d(TAG, "Bound to activity " + intent.getComponent().getClassName());
        } else {
            Log.d(TAG, "Bound to unknown activity");
        }

        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (intent.getComponent() != null) {
            Log.d(TAG, "Unbound from activity " + intent.getComponent().getClassName());
        } else {
            Log.d(TAG, "Unbound from unknown activity");
        }

        // Unbind clipboard update event listener and test if we can still access the clipboard.
        clipboardUpdateListener = null;
        notifyIfClipboardUnaccessible();

        return super.onUnbind(intent);
    }

    /**
     * Checks if the system's clipboard is accessible and if not, because of Android 10+ limitation,
     * a message to the user should be displayed.
     */
    public void notifyIfClipboardUnaccessible() {
        // Do nothing if clipboard is accessible.
        if (clipboard.getPrimaryClip() != null)
            return;

        // Display message.
        Toast.makeText(getApplicationContext(), R.string.clipboard_unaccessible, LENGTH_LONG)
                .show();

        // Show notification with more information about the issue.
        NotificationCompat.Builder notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Clipboard Unaccessible")
                .setContentText(getString(R.string.clipboard_unaccessible))
                .setAutoCancel(true)
                .setChannelId(CHANNEL_ID);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(getString(R.string.clipboard_help_url)));
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        notification.setContentIntent(pendingIntent);
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1234, notification.build());
    }

    /**
     * Adds a {@link Clip} object to the top of our clipboard history list.
     *
     * @param clip Clipboard item to be added to the history.
     */
    public void addClip(Clip clip) {
        // Add the clip object to our internal history.
        clips.add(0, clip);

        // Notify the event listener that the clipboard history has been updated.
        if (ClipboardManagerService.this.clipboardUpdateListener != null)
            ClipboardManagerService.this.clipboardUpdateListener.onClipAdded(clip);
    }

    /**
     * Creates the foreground service notification for usage in
     * {@link #startForeground(int, Notification)}.
     *
     * @return Notification used for maintaining a foreground service.
     */
    private Notification createServiceNotification() {
        // Create a notification channel for newer versions of Android.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    getString(R.string.app_name), NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Copy Stand channel for foreground service notification");
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }

        // Build the notification object and return it.
        return new NotificationCompat.Builder(this, CHANNEL_ID).build();
    }

    /**
     * Sets the clipboard history update listener event handler.
     * <b>WARNING:</b> The event listener will be called from a separate thread.
     *
     * @param listener A clipboard updated event listener.
     */
    public void setClipboardUpdateListener(ClipboardUpdateListener listener) {
        clipboardUpdateListener = listener;
    }

    /**
     * Gets the list of clipboard items in our history.
     *
     * @return List of clipboard items.
     */
    public ArrayList<Clip> getClips() {
        return clips;
    }

    /**
     * Event handler for clipboard content changed events.
     */
    ClipboardManager.OnPrimaryClipChangedListener onPrimaryClipChangedListener = () -> {
        // If we have no new clip, ignore everything.
        if (!clipboard.hasPrimaryClip())
            return;

        // Get the contents of the clipboard.
        ClipData data = clipboard.getPrimaryClip();
        if (data == null) {
            Log.e(TAG, "ClipboardManager.getPrimaryClip() returned null");
            return;
        }

        // Create a Clip object and add it to the history.
        Clip clip = Clip.fromClipboard(data);
        if (clip == null) {
            Log.d(TAG, "ClipData object could not be converted to Clip");
            return;
        }
        addClip(clip);
    };

    /**
     * Background synchronization server thread.
     */
    private class ServerThread extends Thread {
        private boolean running = false;

        public ServerThread() {
            super();
        }

        @Override
        public void run() {
            running = true;
            int i = 1;
            while (running) {
                try {
                    Clip clip = new Clip(Calendar.getInstance(), "Some item " + i++, "localhost");
                    addClip(clip);
                    sleep(1000);
                } catch (InterruptedException e) {
                    running = false;
                }
            }
        }

        @Override
        public void interrupt() {
            running = false;
            super.interrupt();
        }
    }

    /**
     * Class used to bind local activities to this service, allowing for communication between the
     * {@link Service} and an {@link android.app.Activity}.
     */
    public class LocalBinder extends Binder {
        /**
         * Gets the associated {@link ClipboardManagerService} object.
         *
         * @return Associated {@link ClipboardManagerService} object.
         */
        public ClipboardManagerService getService() {
            return ClipboardManagerService.this;
        }
    }

    /**
     * Interface used for listening to clipboard changed events, both from our own or updates from
     * other devices on the network.
     */
    public interface ClipboardUpdateListener {
        /**
         * A new clipboard item was added to the history maintained by the manager service.
         *
         * @param clip Clipboard item that was added to the history.
         */
        void onClipAdded(Clip clip);
    }
}
