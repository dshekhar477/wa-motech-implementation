package org.motechproject.nms.flw.service;

import org.motechproject.nms.region.domain.State;

public interface WhitelistService {
    boolean numberWhitelistedForState(State state, Long contactNumber);
}
