package com.innoveworkshop.copystand.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.innoveworkshop.copystand.R;
import com.innoveworkshop.copystand.models.Clip;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * ListView adapter for an array of {@link Clip} clipboard item objects.
 */
public class ClipListItemAdapter extends ArrayAdapter<Clip> {
    private @LayoutRes int layoutResource;

    /**
     * Constructs a new clipboard items ListView adapter.
     *
     * @param context  Current application's context.
     * @param resource Resource ID for a layout file containing view of each list item.
     * @param clips    List of clipboard item objects.
     */
    public ClipListItemAdapter(@NonNull Context context, @LayoutRes int resource,
                               List<Clip> clips) {
        super(context, resource, clips);
        this.layoutResource = resource;
    }

    @SuppressLint("SimpleDateFormat")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // Reuse the View if possible.
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(layoutResource, parent, false);
        }

        // Populate the new View with the item contents.
        Clip clip = getItem(position);
        if (clip != null) {
            // Get view components.
            TextView contentLabel = convertView.findViewById(R.id.content_label);
            TextView deviceLabel = convertView.findViewById(R.id.device_label);
            TextView timestampLabel = convertView.findViewById(R.id.timestamp_label);

            // Populates the view contents.
            contentLabel.setText(clip.getData());
            deviceLabel.setText(clip.getDevice());

            // Create time label string.
            SimpleDateFormat formatter;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                formatter = new SimpleDateFormat("HH:mm:ss",  Locale.getDefault(
                        Locale.Category.FORMAT));
            } else {
                formatter = new SimpleDateFormat("HH:mm:ss");
            }
            timestampLabel.setText(formatter.format(clip.getCreationDate().getTime()));
        }

        return convertView;
    }
}
