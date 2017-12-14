package org.motechproject.nms.testing.it.flw;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.nms.swc.domain.CallDetailRecord;
import org.motechproject.nms.swc.domain.Swachchagrahi;
import org.motechproject.nms.swc.domain.SwachchagrahiStatus;
import org.motechproject.nms.swc.domain.ServiceUsage;
import org.motechproject.nms.swc.repository.CallDetailRecordDataService;
import org.motechproject.nms.swc.repository.SwcDataService;
import org.motechproject.nms.swc.service.SwcService;
import org.motechproject.nms.swc.service.ServiceUsageService;
import org.motechproject.nms.props.domain.Service;
import org.motechproject.nms.testing.service.TestingService;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Verify that HelloWorldService present, functional.
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class ServiceUsageServiceBundleIT extends BasePaxIT {

    @Inject
    SwcDataService swcDataService;

    @Inject
    SwcService swcService;

    @Inject
    ServiceUsageService serviceUsageService;

    @Inject
    TestingService testingService;

    @Inject
    CallDetailRecordDataService callDetailRecordDataService;

    private void setupData() {
        testingService.clearDatabase();

        for (Swachchagrahi flw: swcDataService.retrieveAll()) {
            flw.setCourseStatus(SwachchagrahiStatus.INVALID);
            flw.setInvalidationDate(new DateTime().withDate(2011, 8, 1));

            swcDataService.update(flw);
        }

        callDetailRecordDataService.deleteAll();
        swcDataService.deleteAll();
    }

    @Test
    public void testServiceUsageServicePresent() throws Exception {
        assertNotNull(serviceUsageService);
    }

    @Test
    public void testGetCurrentMonthlyUsageForFLWAndService() throws Exception {
        setupData();
        Swachchagrahi flw = new Swachchagrahi("Valid Worker", 1111111111L);
        swcService.add(flw);

        Swachchagrahi flwIgnored = new Swachchagrahi("Ignored Worker", 2222222222L);
        swcService.add(flwIgnored);

        CallDetailRecord lastMonth = new CallDetailRecord();
        lastMonth.setSwachchagrahi(flw);
        lastMonth.setCallingNumber(1111111111l);
        lastMonth.setService(Service.MOBILE_ACADEMY);
        lastMonth.setCallDurationInPulses(1);
        lastMonth.setEndOfUsagePromptCounter(1);
        lastMonth.setWelcomePrompt(true);
        lastMonth.setCallStartTime(DateTime.now().minusMonths(2));
        callDetailRecordDataService.create(lastMonth);

        // A usage record for a different service that should be ignored
        CallDetailRecord differentService = new CallDetailRecord();
        differentService.setSwachchagrahi(flw);
        differentService.setCallingNumber(1111111111l);
        differentService.setService(Service.MOBILE_KUNJI);
        differentService.setCallDurationInPulses(1);
        differentService.setEndOfUsagePromptCounter(1);
        differentService.setWelcomePrompt(true);
        differentService.setCallStartTime(DateTime.now());
        callDetailRecordDataService.create(differentService);

        // A usage record for a different FLW that should be ignored
        CallDetailRecord differentFLW = new CallDetailRecord();
        differentFLW.setSwachchagrahi(flwIgnored);
        differentFLW.setCallingNumber(1111111111l);
        differentFLW.setService(Service.MOBILE_KUNJI);
        differentFLW.setCallDurationInPulses(1);
        differentFLW.setEndOfUsagePromptCounter(1);
        differentFLW.setWelcomePrompt(true);
        differentFLW.setCallStartTime(DateTime.now());
        callDetailRecordDataService.create(differentFLW);

        // Two valid records that should get aggregated
        CallDetailRecord recordOne = new CallDetailRecord();
        recordOne.setSwachchagrahi(flw);
        recordOne.setCallingNumber(1111111111l);
        recordOne.setService(Service.MOBILE_ACADEMY);
        recordOne.setCallDurationInPulses(1);
        recordOne.setEndOfUsagePromptCounter(0);
        recordOne.setWelcomePrompt(true);
        recordOne.setCallStartTime(DateTime.now());
        callDetailRecordDataService.create(recordOne);

        CallDetailRecord recordTwo = new CallDetailRecord();
        recordTwo.setSwachchagrahi(flw);
        recordTwo.setCallingNumber(1111111111l);
        recordTwo.setService(Service.MOBILE_ACADEMY);
        recordTwo.setCallDurationInPulses(1);
        recordTwo.setEndOfUsagePromptCounter(1);
        recordTwo.setWelcomePrompt(false);
        recordTwo.setCallStartTime(DateTime.now());
        callDetailRecordDataService.create(recordTwo);

        ServiceUsage serviceUsage = serviceUsageService.getCurrentMonthlyUsageForFLWAndService(flw, Service.MOBILE_ACADEMY);

        assertEquals(flw, serviceUsage.getSwachchagrahi());
        assertEquals(Service.MOBILE_ACADEMY, serviceUsage.getService());
        assertEquals(2, serviceUsage.getUsageInPulses());
        assertEquals(1, serviceUsage.getEndOfUsage());
        assertEquals(true, serviceUsage.getWelcomePrompt());

        callDetailRecordDataService.delete(lastMonth);
        callDetailRecordDataService.delete(differentService);
        callDetailRecordDataService.delete(differentFLW);
        callDetailRecordDataService.delete(recordOne);
        callDetailRecordDataService.delete(recordTwo);

        flw.setCourseStatus(SwachchagrahiStatus.INVALID);
        flw.setInvalidationDate(new DateTime().withDate(2011, 8, 1));
        swcService.update(flw);
        swcService.delete(flw);

        flwIgnored.setCourseStatus(SwachchagrahiStatus.INVALID);
        flwIgnored.setInvalidationDate(new DateTime().withDate(2011, 8, 1));
        swcService.update(flwIgnored);
        swcService.delete(flwIgnored);
    }

    @Test
    public void testGetCurrentMonthlyUsageForFLWAndServiceWithNoService() throws Exception {
        setupData();
        Swachchagrahi flw = new Swachchagrahi("Valid Worker", 1111111111L);
        swcService.add(flw);

        ServiceUsage serviceUsage = serviceUsageService.getCurrentMonthlyUsageForFLWAndService(flw, Service.MOBILE_ACADEMY);

        assertEquals(flw, serviceUsage.getSwachchagrahi());
        assertEquals(Service.MOBILE_ACADEMY, serviceUsage.getService());
        assertEquals(0, serviceUsage.getUsageInPulses());
        assertEquals(0, serviceUsage.getEndOfUsage());
        assertEquals(false, serviceUsage.getWelcomePrompt());

        callDetailRecordDataService.deleteAll();

        flw.setCourseStatus(SwachchagrahiStatus.INVALID);
        flw.setInvalidationDate(new DateTime().withDate(2011, 8, 1));
        swcService.update(flw);
        swcService.delete(flw);
    }

}
