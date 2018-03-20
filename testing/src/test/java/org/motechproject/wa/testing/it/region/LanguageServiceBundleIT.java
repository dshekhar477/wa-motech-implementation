package org.motechproject.wa.testing.it.region;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.motechproject.wa.region.domain.Circle;
import org.motechproject.wa.region.domain.District;
import org.motechproject.wa.region.domain.Language;
import org.motechproject.wa.region.domain.State;
import org.motechproject.wa.region.repository.CircleDataService;
import org.motechproject.wa.region.repository.DistrictDataService;
import org.motechproject.wa.region.repository.LanguageDataService;
import org.motechproject.wa.region.repository.StateDataService;
import org.motechproject.wa.region.service.LanguageService;
import org.motechproject.wa.testing.service.TestingService;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import static org.junit.Assert.*;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class LanguageServiceBundleIT extends BasePaxIT {

    @Inject
    TestingService testingService;

    @Inject
    LanguageService languageService;

    @Inject
    StateDataService stateDataService;

    @Inject
    DistrictDataService districtDataService;

    @Inject
    CircleDataService circleDataService;

    @Inject
    LanguageDataService languageDataService;

    @Inject
    PlatformTransactionManager transactionManager;

    @Before
    public void setUp() {
        testingService.clearDatabase();

        Language ta = languageDataService.create(new Language("50", "tamil"));
        Language hi = languageDataService.create(new Language("99", "hindi"));

        State state = new State();
        state.setName("State 1");
        state.setCode(1L);

        stateDataService.create(state);

        Circle circle = new Circle("AA");
        circle.setDefaultLanguage(ta);
        circleDataService.create(circle);

        District district = new District();
        district.setName("District 1");
        district.setRegionalName("District 1");
        district.setCode(1L);
        district.setLanguage(ta);
        district.setState(state);
        district.setCircle(circle);

        District district2 = new District();
        district2.setName("District 2");
        district2.setRegionalName("District 2");
        district2.setCode(2L);
        district2.setLanguage(ta);
        district2.setState(state);
        district2.setCircle(circle);

        District district3 = new District();
        district3.setName("District 3");
        district3.setRegionalName("District 3");
        district3.setCode(3L);
        district3.setLanguage(hi);
        district3.setState(state);
        district3.setCircle(circle);

        state.getDistricts().addAll(new ArrayList<>(Arrays.asList(district, district2, district3)));
        stateDataService.update(state);
    }

    @Test
    public void testValidGetAll() {
        Language tamil = languageService.getForCode("50");
        Language hindi = languageService.getForCode("99");

        // Call get all and verify there are two
        Set<Language> languages = languageService.getAll();

        assertNotNull(languages);
        assertEquals(2, languages.size());
        assertTrue(languages.contains(tamil));
        assertTrue(languages.contains(hindi));
    }

    @Test
    public void testValidGetAllForCircle() {

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
            Language tamil = languageService.getForCode("50");
            Language hindi = languageService.getForCode("99");

            Circle circle = circleDataService.findByName("AA");
            assertNotNull(circle);

            Set<Language> languages = languageService.getAllForCircle(circle);

            assertNotNull(languages);
            assertEquals(2, languages.size());
            assertTrue(languages.contains(tamil));
            assertTrue(languages.contains(hindi));
        transactionManager.commit(status);
    }

    @Test
    public void testValidGetAllStatesForLanguage() {

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
            Language tamil = languageService.getForCode("50");
            assertNotNull(tamil);

            Language hindi = languageService.getForCode("99");
            assertNotNull(hindi);

            Set<State> states = languageService.getAllStatesForLanguage(tamil);
            assertNotNull(states);
            assertEquals(1, states.size());
            assertEquals(1l, (long) states.iterator().next().getCode());

            states = languageService.getAllStatesForLanguage(hindi);
            assertNotNull(states);
            assertEquals(1, states.size());
            assertEquals(1l, (long) states.iterator().next().getCode());
        transactionManager.commit(status);
    }
}
