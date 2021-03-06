package org.motechproject.wa.swc.exception;

/**
 * Signals an issue with importing an SWC which already exits in database.
 */
public class SwcExistingRecordException extends Exception {

    public SwcExistingRecordException(String message) {
        super(message);
    }
}
