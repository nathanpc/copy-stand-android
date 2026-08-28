package com.innoveworkshop.copystand;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.innoveworkshop.copystand.adapters.ClipListItemAdapter;
import com.innoveworkshop.copystand.models.Clip;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Application's main activity.
 */
public class MainActivity extends AppCompatActivity {

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

        // Populate the ListView with a sample list for testing.
        ArrayList<Clip> arr = new ArrayList<>();
        for (int i = 0; i < 20; i++)
            arr.add(new Clip(Calendar.getInstance(), "Some item " + i, "localhost"));
        ClipListItemAdapter adapter = new ClipListItemAdapter(getApplicationContext(),
                R.layout.list_item, arr);
        ListView listView = findViewById(R.id.clips_list);
        listView.setAdapter(adapter);
    }
}