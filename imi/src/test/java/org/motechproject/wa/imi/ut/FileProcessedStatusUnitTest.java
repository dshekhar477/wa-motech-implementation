package org.motechproject.wa.imi.ut;

import org.junit.Test;
import org.motechproject.wa.imi.domain.FileProcessedStatus;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FileProcessedStatusUnitTest {
    @Test
    public void testValid() {
        assertFalse(FileProcessedStatus.isValidEnumValue(0));
        assertTrue(FileProcessedStatus.isValidEnumValue(8000));
    }

    @Test
    public void testValue() {
        assertEquals(FileProcessedStatus.FILE_ERROR_IN_FILE_FORMAT.getValue(), 8005);
    }

    @Test
    public void testFromInt() {
        assertEquals(FileProcessedStatus.fromInt(8000), FileProcessedStatus.FILE_PROCESSED_SUCCESSFULLY);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testFromIntFailure() {
        FileProcessedStatus.fromInt(0);
    }
}
