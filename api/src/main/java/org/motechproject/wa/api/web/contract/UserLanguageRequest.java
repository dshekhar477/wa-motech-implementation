package org.motechproject.wa.api.web.contract;

import org.motechproject.wa.props.service.LogHelper;

/**
 * Request body
 *
 * 2.2.7 Set User Language Location Code API
 * IVR shall invoke this API to provide user languageLocation preference to MoTech.
 * /api/washacademy/languageLocationCode
 *
 * 3.2.3 Set User Language Location Code API
 * IVR shall invoke this API to set the language location code of the user in wa database.
 * /api/mobilekunji/languageLocationCode
 *
 */
public class UserLanguageRequest {
    private Long callingNumber;
    private String callId;
    private String languageLocationCode;

    // Necessary for Jackson
    public UserLanguageRequest() { }

    // Used in ITs only
    public UserLanguageRequest(Long callingNumber, String callId, String languageLocationCode) {
        this.callingNumber = callingNumber;
        this.callId = callId;
        this.languageLocationCode = languageLocationCode;
    }

    public Long getCallingNumber() {
        return callingNumber;
    }

    public void setCallingNumber(Long callingNumber) {
        this.callingNumber = callingNumber;
    }

    public String getCallId() {
        return callId;
    }

    public void setCallId(String callId) {
        this.callId = callId;
    }

    public String getLanguageLocationCode() {
        return languageLocationCode;
    }

    public void setLanguageLocationCode(String languageLocationCode) {
        this.languageLocationCode = languageLocationCode;
    }

    @Override
    public String toString() {
        return "UserLanguageRequest{" +
                "callingNumber=" + LogHelper.obscure(callingNumber) +
                ", callId=" + callId +
                ", languageLocationCode='" + languageLocationCode + '\'' +
                '}';
    }
}
