package org.motechproject.nms.api.web.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.motechproject.nms.api.web.contract.AddSwcRequest;
import org.motechproject.nms.api.web.service.SwcCsvService;
import org.motechproject.nms.region.repository.PanchayatDataService;
import org.motechproject.nms.swc.domain.SwcJobStatus;
import org.motechproject.nms.swc.service.SwcService;
import org.motechproject.nms.swcUpdate.service.SwcImportService;
import org.motechproject.nms.swc.domain.RejectionReasons;
import org.motechproject.nms.swc.domain.SubscriptionOrigin;
import org.motechproject.nms.swc.utils.SwcConstants;
import org.motechproject.nms.props.service.LogHelper;
import org.motechproject.nms.rejectionhandler.domain.SwcImportRejection;
import org.motechproject.nms.rejectionhandler.service.SwcRejectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by vishnu on 25/9/17.
 */
@Service("swcRejectionCsvService")
public class SwcCsvServiceImpl implements SwcCsvService {

    private static final String NOT_PRESENT = "<%s: Not Present>";
    private static final String INVALID = "<%s: Invalid>";
    private static final String IVR_INTERACTION_LOG = "IVR INTERACTION: %s";

    private static final long SMALLEST_10_DIGIT_NUMBER = 1000000000L;
    private static final long LARGEST_10_DIGIT_NUMBER  = 9999999999L;
    private final String contactNumber = "contactNumber";
    private final String gfStatus = "gfStatus";


    private static final Logger LOGGER = LoggerFactory.getLogger(SwcCsvServiceImpl.class);

    @Autowired
    private SwcRejectionService swcRejectionService;

    @Autowired
    private SwcService swcService;

    @Autowired
    private PanchayatDataService panchayatDataService;

    @Autowired
    private SwcImportService swcImportService;

    @Override
    @Transactional
    public StringBuilder csvUploadRch(AddSwcRequest addSwcRequest) {
        log("REQUEST: /ops/createUpdateRchSwc", String.format(
                "callingNumber=%s, rchId=%s, name=%s, state=%d, district=%d",
                LogHelper.obscure(addSwcRequest.getMsisdn()),
                addSwcRequest.getSwcId(),
                addSwcRequest.getName(),
                addSwcRequest.getStateId(),
                addSwcRequest.getDistrictId()));

        StringBuilder failureReasons = new StringBuilder();
        validateField10Digits(failureReasons, contactNumber, addSwcRequest.getMsisdn());
        validateFieldPositiveLong(failureReasons, contactNumber, addSwcRequest.getMsisdn());
        validateFieldPresent(failureReasons, "swcId", addSwcRequest.getSwcId());
        validateFieldPresent(failureReasons, "stateId", addSwcRequest.getStateId());
        validateFieldPresent(failureReasons, "districtId", addSwcRequest.getDistrictId());
        validateFieldString(failureReasons, "name", addSwcRequest.getName());
        validateFieldPresent(failureReasons, "blockId", addSwcRequest.getBlockId());
        validateFieldPresent(failureReasons, "panchayatId", addSwcRequest.getPanchayatId());
        if (failureReasons.length() > 0) {
            String fieldName = failureReasons.toString().split("[\\W]")[1];
            csvRejectionsRch(fieldName, addSwcRequest);
            return failureReasons;
        }
        return null;
    }

    @Override
    @Transactional
    public void persistSwcRch(AddSwcRequest addSwcRequest) {
        Map<String, Object> swcProperties = new HashMap<>();
        swcProperties.put(SwcConstants.NAME, addSwcRequest.getName());
        swcProperties.put(SwcConstants.ID, addSwcRequest.getSwcId());
        swcProperties.put(SwcConstants.MOBILE_NO, addSwcRequest.getMsisdn());
        swcProperties.put(SwcConstants.STATE_ID, addSwcRequest.getStateId());
        swcProperties.put(SwcConstants.DISTRICT_ID, addSwcRequest.getDistrictId());
        swcProperties.put(SwcConstants.GF_STATUS, SwcJobStatus.ACTIVE.toString());
        swcProperties.put(SwcConstants.BLOCK_ID, addSwcRequest.getBlockId());
        swcProperties.put(SwcConstants.PANCHAYAT_ID, addSwcRequest.getPanchayatId());

        swcImportService.createUpdate(swcProperties, SubscriptionOrigin.RCH_IMPORT);
    }

    @Override
    @Transactional
    public void csvRejectionsRch(String fieldName, AddSwcRequest addSwcRequest) {
        String action = this.rchSwcActionFinder(addSwcRequest);
        if ("contactNumber".equals(fieldName)) {
            swcRejectionService.createUpdate(swcRejectionRch(addSwcRequest, false, RejectionReasons.MOBILE_NUMBER_EMPTY_OR_WRONG_FORMAT.toString(), action));
        } else if (gfStatus.equals(fieldName)) {
            swcRejectionService.createUpdate(swcRejectionRch(addSwcRequest, false, RejectionReasons.GF_STATUS_EMPTY_OR_WRONG_FORMAT.toString(), action));
        } else {
            swcRejectionService.createUpdate(swcRejectionRch(addSwcRequest, false, RejectionReasons.FIELD_NOT_PRESENT.toString(), action));
        }
    }


    private String rchSwcActionFinder(AddSwcRequest record) {
        if (swcService.getBySwcIdAndPanchayat(record.getSwcId(), panchayatDataService.findByCode(record.getPanchayatId())) == null) {
            return "CREATE";
        } else {
            return "UPDATE";
        }
    }

    public static SwcImportRejection swcRejectionRch(AddSwcRequest record, Boolean accepted, String rejectionReason, String action) {
        SwcImportRejection swcImportRejection = new SwcImportRejection();
        swcImportRejection.setSwcName(record.getName());
        swcImportRejection.setSwcID(Long.parseLong(record.getSwcId()));
        swcImportRejection.setMsisdn(record.getMsisdn().toString());
        swcImportRejection.setStateId(record.getStateId());
        swcImportRejection.setDistrictId(record.getDistrictId());
        swcImportRejection.setBlockId(record.getBlockId());
        swcImportRejection.setPanchayatId(record.getPanchayatId());
        swcImportRejection.setSwcStatus(SwcJobStatus.ACTIVE.toString());
        swcImportRejection.setSource("RCH-Import");
        swcImportRejection.setAccepted(accepted);
        swcImportRejection.setRejectionReason(rejectionReason);
        swcImportRejection.setAction(action);

        return swcImportRejection;
    }

    private static boolean validateField10Digits(StringBuilder errors, String fieldName, Long value) {
        if (!validateFieldPresent(errors, fieldName, value)) {
            return false;
        }
        if (value >= SMALLEST_10_DIGIT_NUMBER && value <= LARGEST_10_DIGIT_NUMBER) {
            return true;
        }
        errors.append(String.format(INVALID, fieldName));
        return false;
    }

    private static boolean validateFieldPositiveLong(StringBuilder errors, String fieldName, Long value) {
        if (!validateFieldPresent(errors, fieldName, value)) {
            return false;
        }
        if (value >= 0) {
            return true;
        }
        errors.append(String.format(INVALID, fieldName));
        return false;
    }

    private static boolean validateFieldPresent(StringBuilder errors, String fieldName, Object value) {
        if (value != null) {
            return true;
        }
        errors.append(String.format(NOT_PRESENT, fieldName));
        return false;
    }

    private static boolean validateFieldString(StringBuilder errors, String fieldName, String value) {
        if (!validateFieldPresent(errors, fieldName, value)) {
            return false;
        }
        if (value.length() > 0) {
            return true;
        }
        errors.append(String.format(INVALID, fieldName));
        return false;
    }

    protected static void log(final String endpoint, final String s) {
        LOGGER.info(IVR_INTERACTION_LOG.format(endpoint) + (StringUtils.isBlank(s) ? "" : " : " + s));
    }
}
