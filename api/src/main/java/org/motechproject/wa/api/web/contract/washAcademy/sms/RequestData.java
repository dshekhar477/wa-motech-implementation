package org.motechproject.wa.api.web.contract.washAcademy.sms;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Sms delivery status container
 */
public class RequestData {

    @Valid
    @NotNull
    private DeliveryInfoNotification deliveryInfoNotification;

    public RequestData() {
    }

    public DeliveryInfoNotification getDeliveryInfoNotification() {
        return deliveryInfoNotification;
    }

    public void setDeliveryInfoNotification(DeliveryInfoNotification deliveryInfoNotification) {
        this.deliveryInfoNotification = deliveryInfoNotification;
    }

    @Override
    public String toString() {
        return "RequestData{" +
                "deliveryInfoNotification=" + deliveryInfoNotification +
                '}';
    }
}
