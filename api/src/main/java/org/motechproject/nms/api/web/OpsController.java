package org.motechproject.nms.api.web;

import org.motechproject.nms.api.web.contract.AddSwcRequest;
import org.motechproject.nms.api.web.service.SwcCsvService;
import org.motechproject.nms.swc.utils.SwcConstants;
import org.motechproject.nms.api.web.contract.mobileAcademy.GetBookmarkResponse;
import org.motechproject.nms.api.web.converter.WashAcademyConverter;
import org.motechproject.nms.imi.service.CdrFileService;
import org.motechproject.nms.swc.domain.DeactivationReason;
import org.motechproject.nms.swc.repository.SwcDataService;
import org.motechproject.nms.swc.service.SwcService;
import org.motechproject.nms.mcts.service.MctsWsImportService;
import org.motechproject.nms.props.service.LogHelper;

import org.motechproject.nms.washacademy.dto.WaBookmark;
import org.motechproject.nms.washacademy.service.WashAcademyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Controller to expose methods for OPS personnel
 */
@RequestMapping("/ops")
@Controller
public class OpsController extends BaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpsController.class);

    @Autowired
    private SwcDataService swcDataService;

    @Autowired
    private SwcService subscriberService;

    @Autowired
    private CdrFileService cdrFileService;

    @Autowired
    private MctsWsImportService mctsWsImportService;

    @Autowired
    private WashAcademyService washAcademyService;

    @Autowired
    private SwcCsvService swcCsvService;

    private final String contactNumber = "contactNumber";

    //only for debugging purposes and will not be returned anywhere
    public static final String NON_ASHA_TYPE = "<MctsId: %s,Contact Number: %s, Invalid Type: %s>";

    protected static boolean validatetypeASHA(StringBuilder errors, String fieldName, String mctsFlwId, Long contactNumber, String type) {
        if (!validateFieldPresent(errors, fieldName, type)) {
            return false;
        }
        String designation = type.trim();
        if (SwcConstants.ASHA_TYPE.equalsIgnoreCase(designation)) {
            return true;
        }
        errors.append(String.format(NON_ASHA_TYPE, mctsFlwId, contactNumber, type));
        return false;
    }

    /**
     * Provided for OPS as a crutch to be able to empty all MDS cache directly after modifying the database by hand
     */
    @RequestMapping("/evictAllCache")
    @ResponseStatus(HttpStatus.OK)
    public void evictAllCache() {
        LOGGER.info("/evictAllCache()");
        swcDataService.evictAllCache();
    }

    @RequestMapping("/ping")
    @ResponseStatus(HttpStatus.OK)
    public String ping() {
        LOGGER.info("/ping()");
        return "PING";
    }

    @RequestMapping("/cleanCallRecords")
    @ResponseStatus(HttpStatus.OK)
    public void clearCallRecords() {

        LOGGER.info("/cleanCdr()");
        cdrFileService.cleanOldCallRecords();
    }

    @RequestMapping("/startMctsSync")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void startMctsSync() {

        LOGGER.info("/startMctsSync");
        mctsWsImportService.startMctsImport();
    }

    @RequestMapping(value = "/createUpdateRchFlw",
            method = RequestMethod.POST,
            headers = { "Content-type=application/json" })
    @ResponseStatus(HttpStatus.OK)
    public void createUpdateRchFlw(@RequestBody AddSwcRequest addSwcRequest) {
        StringBuilder failureReasons = swcCsvService.csvUploadRch(addSwcRequest);
        if (failureReasons != null) {
            throw new IllegalArgumentException(failureReasons.toString());
        } else {
            swcCsvService.persistFlwRch(addSwcRequest);
        }
    }

    @RequestMapping("/getbookmark")
    @ResponseBody
    public GetBookmarkResponse getBookmarkWithScore(@RequestParam(required = false) Long callingNumber) {
        LOGGER.info("/getbookmark");
        WaBookmark bookmark = washAcademyService.getBookmarkOps(callingNumber);
        GetBookmarkResponse ret = WashAcademyConverter.convertBookmarkDto(bookmark);
        log("RESPONSE: /ops/getbookmark", String.format("bookmark=%s", ret.toString()));
        return ret;
    }

    @RequestMapping(value = "/deactivationRequest",
            method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public void deactivationRequest(@RequestParam(value = "msisdn") Long msisdn, @RequestParam(value = "deactivationReason") String deactivationReason) {
        log("REQUEST: /ops/deactivationRequest", String.format(
                "callingNumber=%s",
                LogHelper.obscure(msisdn)));
        StringBuilder failureReasons = new StringBuilder();
        validateField10Digits(failureReasons, contactNumber, msisdn);
        validateFieldPositiveLong(failureReasons, contactNumber, msisdn);
        validateDeactivationReason(failureReasons, "deactivationReason", deactivationReason);
        if (failureReasons.length() > 0) {
            throw new IllegalArgumentException(failureReasons.toString());
        }
        DeactivationReason reason = DeactivationReason.valueOf(deactivationReason);
        subscriberService.getRecords();
        LOGGER.info(reason.name());
    }

    @RequestMapping("/getScores")
    @ResponseBody
    public String getScoresForNumber(@RequestParam(required = true) Long callingNumber) {
        LOGGER.info("/getScores Getting scores for user");
        String scores = washAcademyService.getScoresForUser(callingNumber);
        LOGGER.info("Scores: " + scores);
        return scores;
    }


}

