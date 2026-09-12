package com.innoveworkshop.copystand.models;

import android.content.ClipData;
import android.net.Uri;

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
}
