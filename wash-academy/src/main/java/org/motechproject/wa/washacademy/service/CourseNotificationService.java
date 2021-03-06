package org.motechproject.wa.washacademy.service;

import org.motechproject.event.MotechEvent;

/**
 * Service to initiate sms notifications via IMI
 */
public interface CourseNotificationService {

    // This is defined as an empty service to work around the OSGI limitations to inject a @Component
    void sendSmsNotification(MotechEvent event);

    void updateSmsStatus(MotechEvent event);
}
