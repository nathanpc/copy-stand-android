package com.innoveworkshop.copystand;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.innoveworkshop.copystand.adapters.ClipListItemAdapter;
import com.innoveworkshop.copystand.models.Clip;
import com.innoveworkshop.copystand.services.ClipboardManagerService;
import com.innoveworkshop.copystand.utils.PermissionUtils;

/**
 * Application's main activity.
 */
public class MainActivity extends AppCompatActivity {
    private ClipboardManagerService clipboardService;
    private boolean boundService = false;
    private ClipListItemAdapter listAdapter;

    private ListView listView;
    private MenuItem syncServerMenuItem;
    private MenuItem settingsMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize the activity and it's UI components.
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setSupportActionBar(findViewById(R.id.toolbar));

        // Get UI components.
        listView = findViewById(R.id.clips_list);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate main menu.
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);

        // Synchronization server button.
        syncServerMenuItem = menu.findItem(R.id.sync_server_button);
        syncServerMenuItem.setOnMenuItemClickListener(item -> {
            // Change the running status of the server.
            if (boundService) {
                if (clipboardService.isServerRunning()) {
                    clipboardService.stopSynchronizationServer();
                } else {
                    clipboardService.startSynchronizationServer();
                }

                // Update the button.
                updateServerStatusComponents(clipboardService.isServerRunning());
            }

            return false;
        });

        // Settings button.
        settingsMenuItem = menu.findItem(R.id.settings_button);

        // Update the menu items.
        if (boundService)
            updateServerStatusComponents(clipboardService.isServerRunning());

        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Request the notification runtime permission.
        requestNotificationRuntimePermission(getApplicationContext(), () -> {
            // Start the application's foreground service.
            startClipboardService(getApplicationContext());
            bindClipboardService();
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindClipboardService();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        // Show server status.
        if (boundService)
            updateServerStatusComponents(clipboardService.isServerRunning());

        if (PermissionUtils.systemBlocksBackgroundClipboardAccess()) {
            // Manually get the clipboard contents if we are not allowed in the background.
            if (boundService)
                clipboardService.updateFromClipboard();

            // Show notification to quickly access the application and copy data.
            ClipboardManagerService.showQuickAccessNotification(this);
        }
    }

    /**
     * Requests the runtime permission for notification access.
     *
     * @param context Application's context.
     * @param success What to do when we have permission to use the notification service.
     */
    public void requestNotificationRuntimePermission(Context context, Runnable success) {
        // This runtime permission was not required until Tiramisu.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
            return;

        // Build up the permission rationale dialog in case we need it.
        AlertDialog.Builder permissionRationaleDialog = new AlertDialog.Builder(context)
                .setTitle(R.string.permission_needed)
                .setMessage(R.string.notification_permission_message)
                .setPositiveButton(R.string.grant_permission, (dialog, which) -> {
                    // Request the runtime permission again.
                    requestNotificationRuntimePermission(context, success);
                })
                .setNegativeButton(android.R.string.ok, null)
                .setIconAttribute(android.R.attr.alertDialogIcon);

        // Decide what to do based on the status of our runtime permission.
        if (PermissionUtils.hasNotificationPermission(context)) {
            success.run();
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.POST_NOTIFICATIONS)) {
            // Show informative dialog about why the user wants notifications.
            permissionRationaleDialog.show();
        } else {
            // Permission request dialog handler.
            ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(), isGranted -> {
                        if (isGranted) {
                            success.run();
                        } else {
                            permissionRationaleDialog.show();
                        }
                    });

            // Request the notification runtime permission.
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    /**
     * Starts the foreground clipboard manager service so that it runs indefinitely.
     *
     * @param context Application's context.
     */
    private void startClipboardService(Context context) {
        Intent service = new Intent(context, ClipboardManagerService.class);
        startService(service);
    }

    /**
     * Binds ourselves to the foreground clipboard manager service so that we can communicate with
     * it and call its methods.
     */
    private void bindClipboardService() {
        Intent service = new Intent(this, ClipboardManagerService.class);
        bindService(service, connection, BIND_AUTO_CREATE);
    }

    /**
     * Unbinds ourselves from the foreground clipboard manager service.
     */
    private void unbindClipboardService() {
        unbindService(connection);
        boundService = false;
        invalidateListAdapter();
    }

    /**
     * Invalidates our clipboard items list adapter.
     */
    private void invalidateListAdapter() {
        listAdapter = null;
        listView.setAdapter(null);
    }

    /**
     * Changes the UI components according to the synchronization server running status.
     *
     * @param running Is the synchronization server running?
     */
    public void updateServerStatusComponents(boolean running) {
        if (syncServerMenuItem != null) {
            syncServerMenuItem.setIcon(running ? R.drawable.stop : R.drawable.play);
            syncServerMenuItem.setTitle(running ? R.string.stop_server : R.string.start_server);
        }
    }

    /**
     * Event listener for clipboard updates.
     */
    private final ClipboardManagerService.ClipboardUpdateListener onClipboardUpdated =
            new ClipboardManagerService.ClipboardUpdateListener() {
                @Override
                public void onServerStatusChanged(boolean running) {
                    runOnUiThread(() -> {
                        updateServerStatusComponents(running);
                        Toast.makeText(getApplicationContext(), running ?
                                R.string.sync_server_started : R.string.sync_server_stopped,
                                Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onClipAdded(Clip clip) {
                    runOnUiThread(() -> {
                        if (listAdapter != null)
                            listAdapter.notifyDataSetChanged();
                    });
                }
    };

    /**
     * Foreground service connection object.
     */
    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance.
            ClipboardManagerService.LocalBinder binder =
                    (ClipboardManagerService.LocalBinder) service;
            clipboardService = binder.getService();
            boundService = true;

            // Ensure that we bind our list adapter to the service's clipboard history.
            listAdapter = new ClipListItemAdapter(getApplicationContext(), R.layout.list_item,
                    clipboardService.getClips());
            listView.setAdapter(listAdapter);

            // Show server status.
            updateServerStatusComponents(clipboardService.isServerRunning());

            // Create an event listener for clipboard list updates.
            clipboardService.setClipboardUpdateListener(onClipboardUpdated);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            boundService = false;
            invalidateListAdapter();
        }
    };
}