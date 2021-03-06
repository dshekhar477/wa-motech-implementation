package org.motechproject.wa.swc.service.impl;

import org.motechproject.server.config.SettingsFacade;
import org.motechproject.wa.swc.service.SwcSettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * helper service class created to facilitate ITs.
 */
@Service("swcSettingsService")
public class SwcSettingsServiceImpl implements SwcSettingsService {
    private SettingsFacade settingsFacade;

    @Autowired
    SwcSettingsServiceImpl(SettingsFacade settingsFacade) {
        this.settingsFacade = settingsFacade;
    }


    public SettingsFacade getSettingsFacade() {
        return settingsFacade;
    }
}
