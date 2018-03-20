package org.motechproject.wa.imi.ut;

import org.junit.Test;
import org.motechproject.wa.imi.service.impl.CdrHelper;

//import org.motechproject.wa.kilkari.dto.CallDetailRecordDto;
//import org.motechproject.wa.kilkari.exception.InvalidCallRecordDataException;

public class CdrHelperUnitTest {


//    @Test(expected=IllegalArgumentException.class)
//    public void testTooFewFields() {
//        CallDetailRecordDto cdr = CdrHelper.csvLineToCdrDto("a,b");
//        assertNotNull(cdr);
//    }
//
//    @Test(expected=IllegalArgumentException.class)
//    public void testTooManyFields() {
//        CallDetailRecordDto cdr = CdrHelper.csvLineToCdrDto("a,b,c,d,e,f,g,h,i,j,k,l,m,o,p,q,r,s,t");
//        assertNotNull(cdr);
//    }
//
//    @Test(expected=InvalidCallRecordDataException.class)
//    public void testInvalidFields() {
//        CallDetailRecordDto cdr = CdrHelper.csvLineToCdrDto("a,b,c,d,e,f,g,h,i,j,k,l,m,o,p,q,r,s");
//        assertNotNull(cdr);
//    }
//
//    @Test(expected=InvalidCallRecordDataException.class)
//    public void testInvalidTimes() {
//        CallDetailRecordDto cdr = CdrHelper.csvLineToCdrDto("20150513184533:58747ffc-6b7c-4abb-91d3-f099aa1bf5a3," +
//                "1111111111,c,d,e,123456,g,h,1001,j,k,456,123,o,p,q,3,s");
//        assertNotNull(cdr);
//    }
//
//    @Test
//    public void testCsvLineToCdr() {
//        CallDetailRecordDto expectedCdr = new CallDetailRecordDto();
//        expectedCdr.setRequestId(new RequestId("58747ffc-6b7c-4abb-91d3-f099aa1bf5a3", "20150513184533"));
//        expectedCdr.setMsisdn(1111111111L);
//        expectedCdr.setCallAnswerTime(new DateTime(123456L));
//        expectedCdr.setStatusCode(StatusCode.OBD_SUCCESS_CALL_CONNECTED);
//        expectedCdr.setLanguageLocationId("j");
//        expectedCdr.setContentFile("k");
//        expectedCdr.setMsgPlayDuration(333);
//        expectedCdr.setCircleId("o");
//        expectedCdr.setOperatorId("p");
//        expectedCdr.setCallDisconnectReason(CallDisconnectReason.CONTENT_NOT_FOUND);
//        expectedCdr.setWeekId("s");
//
//        CallDetailRecordDto cdr = CdrHelper.csvLineToCdrDto("20150513184533:58747ffc-6b7c-4abb-91d3-f099aa1bf5a3," +
//                "1111111111,c,d,e,123456,g,h,1001,j,k,123,456,o,p,q,3,s");
//        assertEquals(expectedCdr, cdr);
//    }

    @Test
    public void validateCsv() {
        CdrHelper.validateCsv("a,b,c,d,e,f,g,h,i,j,k,l,m,o,p,q,r,s");
    }

    @Test(expected=IllegalArgumentException.class)
    public void validateCsvTooFewFields() {
        CdrHelper.validateCsv("a,b");
    }

    @Test(expected=IllegalArgumentException.class)
    public void validateCsvTooManyFields() {
        CdrHelper.validateCsv("a,b,c,d,e,f,g,h,i,j,k,l,m,o,p,q,r,s,t");
    }

}
