package com.innoveworkshop.copystand.models;

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
