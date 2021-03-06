package org.motechproject.wa.testing.it.props;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.motechproject.wa.props.domain.DeployedService;
import org.motechproject.wa.props.domain.Service;
import org.motechproject.wa.props.repository.DeployedServiceDataService;
import org.motechproject.wa.props.service.PropertyService;
import org.motechproject.wa.region.domain.State;
import org.motechproject.wa.region.repository.StateDataService;
import org.motechproject.wa.testing.service.TestingService;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;

import javax.inject.Inject;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class PropertyServiceBundleIT extends BasePaxIT {

    @Inject
    StateDataService stateDataService;

    @Inject
    DeployedServiceDataService deployedServiceDataService;

    @Inject
    PropertyService propertyService;

    @Inject
    TestingService testingService;

    private void cleanUp() {
        testingService.clearDatabase();
    }

    private State makeState(Long code, String name) {
        State state = stateDataService.findByCode(code);
        if (state != null) {
            return state;
        }

        state = new State();
        state.setName(name);
        state.setCode(code);

        return stateDataService.create(state);
    }

    // Test a state/service that is deployed
    @Test
    public void testDeployedStateAndDeployedService() {
        cleanUp();

        State state = makeState(1L, "State 1");
        deployedServiceDataService.create(new DeployedService(state, Service.WASH_ACADEMY));

        assertTrue(propertyService.isServiceDeployedInState(Service.WASH_ACADEMY, state));
    }

    // Test a state that has a deployed service but not the one being queried
    @Test
    public void testDeployedStateNotDeployedService() {
        cleanUp();

        State state = makeState(1L, "State 1");
        deployedServiceDataService.create(new DeployedService(state, Service.WASH_ACADEMY));

        assertFalse(propertyService.isServiceDeployedInState(Service.KILKARI, state));
    }

    // Test a state with no deployments but a service that is in a different state
    @Test
    public void testNotDeployedStateDeployedService() {
        cleanUp();

        State deployed = makeState(1L, "State 1");
        deployedServiceDataService.create(new DeployedService(deployed, Service.WASH_ACADEMY));

        State notDeployed = makeState(2L, "State 2");

        assertFalse(propertyService.isServiceDeployedInState(Service.WASH_ACADEMY, notDeployed));
    }

    // Test with neither state or service deployed
    @Test
    public void testNotDeployedStateNotDeployedService() {
        cleanUp();

        State notDeployed = makeState(2L, "State 2");

        assertFalse(propertyService.isServiceDeployedInState(Service.WASH_ACADEMY, notDeployed));
    }
}
