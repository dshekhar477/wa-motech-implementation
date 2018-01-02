package org.motechproject.nms.api.web.contract.washAcademy;

import org.motechproject.nms.props.service.LogHelper;

import java.util.Map;

/**
 * Bookmark request object to save a bookmark 2.2.5
 */
public class SaveBookmarkRequest {

    private Long callingNumber;

    private String callId;

    private String bookmark;

    private Map<String, Integer> scoresByChapter;

    public SaveBookmarkRequest() {
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

    public String getBookmark() {
        return bookmark;
    }

    public void setBookmark(String bookmark) {
        this.bookmark = bookmark;
    }

    public Map<String, Integer> getScoresByChapter() {
        return scoresByChapter;
    }

    public void setScoresByChapter(Map<String, Integer> scoresByChapter) {
        this.scoresByChapter = scoresByChapter;
    }

    @Override
    public String toString() {
        return "SaveBookmarkRequest{" +
                "callingNumber=" + LogHelper.obscure(callingNumber) +
                ", callId=" + callId +
                ", bookmark='" + bookmark + '\'' +
                ", scoresByChapter=" + scoresByChapter +
                '}';
    }
}
