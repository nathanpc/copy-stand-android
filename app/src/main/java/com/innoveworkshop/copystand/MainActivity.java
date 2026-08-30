package com.innoveworkshop.copystand;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.innoveworkshop.copystand.adapters.ClipListItemAdapter;
import com.innoveworkshop.copystand.models.Clip;
import com.innoveworkshop.copystand.services.ClipboardManagerService;

/**
 * Application's main activity.
 */
public class MainActivity extends AppCompatActivity {
    private ClipboardManagerService clipboardService;
    private boolean boundService = false;
    private ClipListItemAdapter listAdapter;

    private ListView listView;

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
    protected void onStart() {
        super.onStart();

        // Start the application's foreground service.
        startClipboardService(getApplicationContext());
        bindClipboardService();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindClipboardService();
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
     * Event listener for clipboard updates.
     */
    private final ClipboardManagerService.ClipboardUpdateListener onClipboardUpdated =
            new ClipboardManagerService.ClipboardUpdateListener() {
        @Override
        public void onClipAdded(Clip clip) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (listAdapter != null)
                        listAdapter.notifyDataSetChanged();
                }
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