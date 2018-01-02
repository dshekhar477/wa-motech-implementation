package org.motechproject.nms.api.web.contract.washAcademy.sms;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Contains sms delivery information
 */
public class DeliveryInfoNotification {

    @NotNull
    private String clientCorrelator;

    private String callbackData;

    @Valid
    @NotNull
    private DeliveryInfo deliveryInfo;

    public DeliveryInfoNotification() {
    }

    public String getClientCorrelator() {
        return clientCorrelator;
    }

    public void setClientCorrelator(String clientCorrelator) {
        this.clientCorrelator = clientCorrelator;
    }

    public String getCallbackData() {
        return callbackData;
    }

    public void setCallbackData(String callbackData) {
        this.callbackData = callbackData;
    }

    public DeliveryInfo getDeliveryInfo() {
        return deliveryInfo;
    }

    public void setDeliveryInfo(DeliveryInfo deliveryInfo) {
        this.deliveryInfo = deliveryInfo;
    }

    @Override
    public String toString() {
        return "DeliveryInfoNotification{" +
                "clientCorrelator='" + clientCorrelator + '\'' +
                ", callbackData='" + callbackData + '\'' +
                ", deliveryInfo=" + deliveryInfo +
                '}';
    }
}
