package org.motechproject.wa.swc.service.impl;

import org.motechproject.wa.swc.domain.CallDetailRecord;
import org.motechproject.wa.swc.repository.CallDetailRecordDataService;
import org.motechproject.wa.swc.service.CallDetailRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("callDetailRecordService")
public class CallDetailRecordServiceImpl implements CallDetailRecordService {
    private CallDetailRecordDataService callDetailRecordDataService;

    @Autowired
    public CallDetailRecordServiceImpl(CallDetailRecordDataService callDetailRecordDataService) {
        this.callDetailRecordDataService = callDetailRecordDataService;
    }

    @Override
    public void add(CallDetailRecord record) {
        callDetailRecordDataService.create(record);
    }

    @Override
    public CallDetailRecord getByCallingNumber(long callingNumber) {
        return callDetailRecordDataService.findByCallingNumber(callingNumber);
    }

    @Override
    public void update(CallDetailRecord record) {
        callDetailRecordDataService.update(record);
    }

    @Override
    public void delete(CallDetailRecord record) {
        callDetailRecordDataService.delete(record);
    }
}
