package com.innoveworkshop.copystand.models;

import android.content.ClipData;
import android.net.Uri;

import androidx.annotation.Nullable;

import java.util.Calendar;

/**
 * Data object containing an instance of a clipboard data copied event.
 */
public class Clip {
    private final Calendar creationDate;
    private final String data;
    private final String device;

    /**
     * Constructs a brand new, fully populated, clipboard item object.
     *
     * @param creationDate Timestamp when this item was copied into the clipboard.
     * @param data         Contents of the clipboard item.
     * @param device       Device where the item originated.
     */
    public Clip(Calendar creationDate, String data, String device) {
        this.creationDate = creationDate;
        this.data = data;
        this.device = device;
    }

    /**
     * Constructs a new clipboard item that was copied right now and in our own device.
     *
     * @param data Contents of the clipboard item.
     */
    public Clip(String data) {
        this(Calendar.getInstance(), data, "localhost");
    }

    /**
     * Constructs a new clipboard item from a system's clipboard data object.
     *
     * @param data System's clipboard data object.
     *
     * @return Clip object or {@code null} if it cannot be converted.
     */
    public static Clip fromClipboard(ClipData data) {
        for (int i = 0; i < data.getItemCount(); i++) {
            ClipData.Item item = data.getItemAt(i);

            // Try to get the contents of the clipboard.
            if (item.getText() != null) {
                // Was it plain text?
                return new Clip(item.getText().toString());
            } else if (item.getUri() != null) {
                // Was it an URI?
                return new Clip(item.getUri().toString());
            }
        }

        return null;
    }

    /**
     * Gets the timestamp when this item was copied into the clipboard.
     *
     * @return Timestamp when this item was copied into the clipboard.
     */
    public Calendar getCreationDate() {
        return this.creationDate;
    }

    /**
     * Gets the contents of what was copied into the clipboard.
     *
     * @return Contents of the clipboard item.
     */
    public String getData() {
        return this.data;
    }

    /**
     * Gets the name of the device where the item was copied into the clipboard.
     *
     * @return Name of the device where the clipboard item originated.
     */
    public String getDevice() {
        return this.device;
    }

    /**
     * Checks if a {@see Clip} object is the same as this one, looking only at the contents, not at
     * the rest of the metadata.
     *
     * @param obj Object to be checked for equality.
     *
     * @return {@code true} if the contents of the clipboard item are the same, {@code false} if
     *         they are not the same or the object is not of {@see Clip} type.
     */
    @Override
    public boolean equals(@Nullable Object obj) {
        // Null object or non-Clip objects are automatically not equal.
        if (!(obj instanceof Clip))
            return false;

        // Compare the items data field.
        return data.equals(((Clip) obj).data);
    }
}
