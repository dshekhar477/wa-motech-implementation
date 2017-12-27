package org.motechproject.nms.swc.service;

import org.motechproject.event.MotechEvent;
import org.motechproject.mds.annotations.InstanceLifecycleListener;
import org.motechproject.mds.annotations.InstanceLifecycleListenerType;
import org.motechproject.nms.region.domain.Panchayat;
import org.motechproject.nms.swc.domain.Swachchagrahi;
import org.motechproject.nms.region.domain.State;

import java.util.List;

/**
 * Simple example of a service interface.
 */
public interface SwcService {

    State getState(Swachchagrahi swachchagrahi);

    void add(Swachchagrahi swachchagrahi);

    Swachchagrahi getByContactNumber(Long contactNumber);

    Swachchagrahi getInctiveByContactNumber(Long contactNumber);

    Swachchagrahi getBySwcId(String swcId);

    Swachchagrahi getByMctsFlwIdAndPanchayat(String mctsFlwId, Panchayat state);

    Swachchagrahi getById(Long id);

    List<Swachchagrahi> getRecords();

    void update(Swachchagrahi record);

    void delete(Swachchagrahi record);

    /**
     * MotechEvent handler that responds to scheduler events.  Purges FLW records that are in invalid state
     * and have been for more than flw.weeks_to_keep_invalid_flws weeks
     *
     * @param event
     */
    void purgeOldInvalidSWCs(MotechEvent event);

    /**
     * Lifecycle listener that verifies a Front Line Worker can only be deleted if it is invalid
     * and has been in that state for 6 weeks
     *
     * @param swachchagrahi
     */
    @InstanceLifecycleListener(InstanceLifecycleListenerType.PRE_DELETE)
    void deletePreconditionCheck(Swachchagrahi swachchagrahi);
}
