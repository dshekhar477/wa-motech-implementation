package org.motechproject.nms.swcUpdate.service;

import org.motechproject.nms.region.domain.Panchayat;
import org.motechproject.nms.swc.exception.SwcExistingRecordException;
import org.motechproject.nms.swc.domain.SubscriptionOrigin;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.exception.InvalidLocationException;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;

public interface SwcImportService {

    void importData(Reader reader, SubscriptionOrigin importOrigin) throws IOException;

    void importMctsFrontLineWorker(Map<String, Object> record, State state) throws InvalidLocationException, SwcExistingRecordException;

    void importRchFrontLineWorker(Map<String, Object> record, Panchayat panchayat) throws InvalidLocationException, SwcExistingRecordException;
    /**
     * Used to create or update an FLW from mcts or other sync services
     * @param flwRecord key-value pair of properties for swc
     */
    boolean createUpdate(Map<String, Object> flwRecord, SubscriptionOrigin importOrigin);
}
