package org.motechproject.nms.testing.it.swcUpdate;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.mtraining.domain.ActivityRecord;
import org.motechproject.mtraining.domain.ActivityState;
import org.motechproject.mtraining.domain.Bookmark;
import org.motechproject.mtraining.repository.ActivityDataService;
import org.motechproject.mtraining.repository.BookmarkDataService;
import org.motechproject.nms.csv.domain.CsvAuditRecord;
import org.motechproject.nms.csv.exception.CsvImportDataException;
import org.motechproject.nms.csv.repository.CsvAuditRecordDataService;
import org.motechproject.nms.swc.domain.SwcJobStatus;
import org.motechproject.nms.swc.domain.Swachchagrahi;
import org.motechproject.nms.swc.repository.SwcDataService;
import org.motechproject.nms.swc.service.SwcService;
import org.motechproject.nms.swcUpdate.service.SwcUpdateImportService;
import org.motechproject.nms.washacademy.domain.CourseCompletionRecord;
import org.motechproject.nms.washacademy.repository.CourseCompletionRecordDataService;
import org.motechproject.nms.region.repository.CircleDataService;
import org.motechproject.nms.region.repository.DistrictDataService;
import org.motechproject.nms.region.repository.LanguageDataService;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.nms.region.service.DistrictService;
import org.motechproject.nms.region.service.LanguageService;
import org.motechproject.nms.testing.it.api.utils.RequestBuilder;
import org.motechproject.nms.testing.it.utils.RegionHelper;
import org.motechproject.nms.testing.service.TestingService;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.motechproject.testing.osgi.http.SimpleHttpClient;
import org.motechproject.testing.utils.TestContext;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class SwachgrahiUpdateImportServiceBundleIT extends BasePaxIT {

    @Inject
    CircleDataService circleDataService;
    @Inject
    DistrictDataService districtDataService;
    @Inject
    DistrictService districtService;
    @Inject
    StateDataService stateDataService;
    @Inject
    LanguageDataService languageDataService;
    @Inject
    LanguageService languageService;
    @Inject
    SwcDataService swcDataService;
    @Inject
    SwcService swcService;
    @Inject
    TestingService testingService;
    @Inject
    SwcUpdateImportService swcUpdateImportService;
    @Inject
    CsvAuditRecordDataService csvAuditRecordDataService;
    @Inject
    CourseCompletionRecordDataService courseCompletionRecordDataService;
    @Inject
    BookmarkDataService bookmarkDataService;
    @Inject
    ActivityDataService activityDataService;

    private RegionHelper rh;


    @Before
    public void setUp() {
        rh = new RegionHelper(languageDataService, languageService, circleDataService, stateDataService,
                districtDataService, districtService);

        testingService.clearDatabase();

        rh.hindiLanguage();
        rh.kannadaLanguage();
        rh.delhiState();
        rh.delhiCircle();
    }

    // Test when state not provided
    @Test(expected = CsvImportDataException.class)
    public void testImportWhenStateNotPresent() throws Exception {
        Reader reader = createLanguageReaderWithHeaders("72185,210302604211400029,9439986187,en,");
        swcUpdateImportService.importLanguageData(reader);
    }

    // Test when state not in database
    @Test(expected = CsvImportDataException.class)
    public void testImportWhenStateNotInDatabase() throws Exception {
        Swachchagrahi swc = new Swachchagrahi(9439986187L);
        swcService.add(swc);

        Reader reader = createLanguageReaderWithHeaders("72185,210302604211400029,9439986187,en,2");
        swcUpdateImportService.importLanguageData(reader);
    }

    // Test when language not provided
    @Test(expected = CsvImportDataException.class)
    public void testImportWhenLanguageNotPresent() throws Exception {
        Reader reader = createLanguageReaderWithHeaders("72185,210302604211400029,9439986187,,1");
        swcUpdateImportService.importLanguageData(reader);
    }

    // Test when language not in database
    @Test(expected = CsvImportDataException.class)
    public void testImportWhenLanguageNotInDatabase() throws Exception {
        Swachchagrahi swc = new Swachchagrahi(9439986187L);
        swcService.add(swc);

        Reader reader = createLanguageReaderWithHeaders("72185,210302604211400029,9439986187,en,1");
        swcUpdateImportService.importLanguageData(reader);
    }

    // Test when only NMS Id found and SWC not in database
    @Test(expected = CsvImportDataException.class)
    public void testImportWhenSWCIdProvidedButNotInDatabase() throws Exception {
        Reader reader = createLanguageReaderWithHeaders("72185,,,hi,1");
        swcUpdateImportService.importLanguageData(reader);
    }

    // NMS_FT_553
    // Test when only MCTS Id found and SWC not in database
    @Test(expected = CsvImportDataException.class)
    public void testImportWhenMCTSIdProvidedButNotInDatabase() throws Exception {
        Reader reader = createLanguageReaderWithHeaders(",210302604211400029,,hi,1");
        swcUpdateImportService.importLanguageData(reader);
    }

    // NMS_FT_554
    // Test when only MSISDN found and SWC not in database
    @Test(expected = CsvImportDataException.class)
    public void testImportWhenMSISDProvidedButNotInDatabase() throws Exception {
        Reader reader = createLanguageReaderWithHeaders(",,9439986187,hi,1");
        swcUpdateImportService.importLanguageData(reader);
    }

    // Test NMS Id takes precedence over MCTS ID

    // Test NMS Id takes precedence over MSISDN
    @Test
    public void testImportWhenNMSIdTakesPrecedenceOverMSIDN() throws Exception {
        Swachchagrahi swc = new Swachchagrahi(1000000000L);
        swc.setSwcId("72185");
        swc.setLanguage(rh.kannadaLanguage());
        swc.setJobStatus(SwcJobStatus.ACTIVE);
        swcService.add(swc);

        swc = new Swachchagrahi(2000000000L);
        swc.setLanguage(rh.kannadaLanguage());
        swc.setJobStatus(SwcJobStatus.ACTIVE);
        swcService.add(swc);

        Reader reader = createLanguageReaderWithHeaders("72185,,2000000000,hi,1");
        swcUpdateImportService.importLanguageData(reader);

        swc = swcService.getByContactNumber(1000000000L);
        assertEquals(rh.hindiLanguage(), swc.getLanguage());

        swc = swcService.getByContactNumber(2000000000L);
        assertEquals(rh.kannadaLanguage(), swc.getLanguage());
    }

    // Test MCTS Id takes precedence over MSISDN

    // Test MSISDN only
    @Test
    public void testImportWhenMSISDNOnly() throws Exception {
        Swachchagrahi swc = new Swachchagrahi(1000000000L);
        swc.setLanguage(rh.kannadaLanguage());
        swc.setJobStatus(SwcJobStatus.ACTIVE);
        swcService.add(swc);

        swc = new Swachchagrahi(2000000000L);
        swc.setLanguage(rh.kannadaLanguage());
        swc.setJobStatus(SwcJobStatus.ACTIVE);
        swcService.add(swc);

        Reader reader = createLanguageReaderWithHeaders("72185,210302604211400029,1000000000,hi,1");
        swcUpdateImportService.importLanguageData(reader);

        swc = swcService.getByContactNumber(1000000000L);
        assertEquals(rh.hindiLanguage(), swc.getLanguage());

        swc = swcService.getByContactNumber(2000000000L);
        assertEquals(rh.kannadaLanguage(), swc.getLanguage());
    }

    @Test
    public void testImportFromSampleLanguageDataFile() throws Exception {
        Swachchagrahi swc = new Swachchagrahi(1000000000L);
        swc.setLanguage(rh.kannadaLanguage());
        swc.setSwcId("72185");
        swc.setJobStatus(SwcJobStatus.ACTIVE);
        swcService.add(swc);

        swc = new Swachchagrahi(2000000000L);
        swc.setLanguage(rh.kannadaLanguage());
        swc.setJobStatus(SwcJobStatus.ACTIVE);
        swcService.add(swc);

        swcUpdateImportService.importLanguageData(read("csv/swc_language_update.csv"));

        swc = swcService.getByContactNumber(1000000000L);
        assertEquals(rh.hindiLanguage(), swc.getLanguage());

        swc = swcService.getByContactNumber(2000000000L);
        assertEquals(rh.hindiLanguage(), swc.getLanguage());
    }

    /************************************************************************************************************
     MSISDN TESTS
     ***********************************************************************************************************/
    // Test when new msisdn not provided
    @Test(expected = CsvImportDataException.class)
    public void testMsisdnImportWhenNewMsisdnNotPresent() throws Exception {
        Reader reader = createMSISDNReaderWithHeaders("72185,210302604211400029,9439986187,,1");
        swcUpdateImportService.importMSISDNData(reader);
    }

    // Test when only NMS Id found and SWC not in database
    @Test(expected = CsvImportDataException.class)
    public void testMsisdnImportWhenSWCIdProvidedButNotInDatabase() throws Exception {
        Reader reader = createMSISDNReaderWithHeaders("72185,,,9439986187,1");
        swcUpdateImportService.importMSISDNData(reader);
    }

    // Test when only MCTS Id found and SWC not in database
    @Test(expected = CsvImportDataException.class)
    public void testMsisdnImportWhenMCTSIdProvidedButNotInDatabase() throws Exception {
        Reader reader = createMSISDNReaderWithHeaders(",210302604211400029,,9439986187,1");
        swcUpdateImportService.importMSISDNData(reader);
    }

    // Test when only MSISDN found and SWC not in database
    @Test(expected = CsvImportDataException.class)
    public void testMsisdnImportWhenMSISDNProvidedButNotInDatabase() throws Exception {
        Reader reader = createMSISDNReaderWithHeaders(",,9439986187,9439986188,1");
        swcUpdateImportService.importMSISDNData(reader);
    }

    // Test NMS Id takes precedence over MCTS ID

    // Test NMS Id takes precedence over MSISDN
    @Test
    public void testMsisdnImportWhenNMSIdTakesPrecedenceOverMSIDN() throws Exception {
        Swachchagrahi swc = new Swachchagrahi(1000000000L);
        swc.setSwcId("72185");
        swc.setJobStatus(SwcJobStatus.ACTIVE);
        swcService.add(swc);

        swc = new Swachchagrahi(2000000000L);
        swc.setJobStatus(SwcJobStatus.ACTIVE);
        swcService.add(swc);

        Reader reader = createMSISDNReaderWithHeaders("72185,,2000000000,9439986187,1");
        swcUpdateImportService.importMSISDNData(reader);

        swc = swcService.getByContactNumber(9439986187L);
        assertNotNull(swc);
        assertEquals("72185", swc.getSwcId());

        swc = swcService.getByContactNumber(1000000000L);
        assertNull(swc);

        swc = swcService.getByContactNumber(2000000000L);
        assertNotNull(swc);
    }

    // Test MCTS Id takes precedence over MSISDN

    // Test MSISDN only swc Update and Bookmark, Completion and Activity Record
    @Test
    public void testMsisdnImportWhenMSISDNOnly() throws Exception {
        Swachchagrahi swc = new Swachchagrahi(1000000000L);
        swc.setJobStatus(SwcJobStatus.ACTIVE);
        swcService.add(swc);

        swc = new Swachchagrahi(2000000000L);
        swc.setJobStatus(SwcJobStatus.ACTIVE);
        swcService.add(swc);

        createMaRecords(1000000000L);

        Reader reader = createMSISDNReaderWithHeaders("72185,210302604211400029,1000000000,9439986187,1");
        swcUpdateImportService.importMSISDNData(reader);

        swc = swcService.getByContactNumber(9439986187L);
        assertNotNull(swc);

        swc = swcService.getByContactNumber(1000000000L);
        assertNull(swc);

        swc = swcService.getByContactNumber(2000000000L);
        assertNotNull(swc);

        assertMaRecords(1000000000L, 9439986187L);
    }

    @Test
    public void testMsisdnImportFromSampleDataFile() throws Exception {
        Swachchagrahi swc = new Swachchagrahi(1000000000L);
        swc.setSwcId("72185");
        swc.setJobStatus(SwcJobStatus.ACTIVE);
        swcService.add(swc);

        swc = new Swachchagrahi(2000000000L);
        swc.setJobStatus(SwcJobStatus.ACTIVE);
        swcService.add(swc);

        swcUpdateImportService.importMSISDNData(read("csv/swc_msisdn_update.csv"));

        swc = swcService.getByContactNumber(9439986187L);
        assertNotNull(swc);

        swc = swcService.getByContactNumber(9439986188L);
        assertNotNull(swc);

        swc = swcService.getByContactNumber(1000000000L);
        assertNull(swc);

        swc = swcService.getByContactNumber(2000000000L);
        assertNull(swc);
    }

    // Test new MSISDN larger than 10 digits
    @Test
    public void testMsisdnImportWhenNewMsisdnTooLong() throws Exception {
        Swachchagrahi swc = new Swachchagrahi(1000000000L);
        swc.setSwcId("72185");
        swc.setJobStatus(SwcJobStatus.ACTIVE);
        swcService.add(swc);

        Reader reader = createMSISDNReaderWithHeaders("72185,210302604211400029,1000000000,09439986187,1");
        swcUpdateImportService.importMSISDNData(reader);

        swc = swcService.getByContactNumber(9439986187L);
        assertNotNull(swc);

        swc = swcService.getByContactNumber(1000000000L);
        assertNull(swc);
    }

    // NMS_FT_557
    // Test new MSISDN not a valid number
    @Test(expected = CsvImportDataException.class)
    public void testMsisdnImportWhenMSISDNProvidedButNotValid() throws Exception {
        Reader reader = createMSISDNReaderWithHeaders(",,9439986187,AAAAAAAAAA,1");
        swcUpdateImportService.importMSISDNData(reader);
    }

    // NMS_FT_557
    // Test new MSISDN less than 10 digits
    @Test(expected = CsvImportDataException.class)
    public void testMsisdnImportWhenMSISDNProvidedButTooShort() throws Exception {
        Reader reader = createMSISDNReaderWithHeaders(",,9439986187,943998618,1");
        swcUpdateImportService.importMSISDNData(reader);
    }

    // NMS_FT_556
    // Test new MSISDN associated with existing SWC
    @Test(expected = CsvImportDataException.class)
    public void testMsisdnImportWhenMSISDNProvidedButAlreadyInUse() throws Exception {
        Swachchagrahi swc = new Swachchagrahi(1000000000L);
        swcService.add(swc);

        swc = new Swachchagrahi(9439986187L);
        swcService.add(swc);

        Reader reader = createMSISDNReaderWithHeaders(",,9439986187,1000000000,1");
        swcUpdateImportService.importMSISDNData(reader);
    }

    // Test Ma Update when new MSISDN associated with existing SWC
    @Test
    public void testMaUpdateWhenMSISDNProvidedIsAlreadyInUse() throws Exception {
        Swachchagrahi swc = new Swachchagrahi(1000000000L);
        swcService.add(swc);

        swc = new Swachchagrahi(9439986187L);
        swcService.add(swc);

        createMaRecords(9439986187L);
        createMaRecords(1000000000L);
        assertBookmark("1000000000", 1);
        assertActivity("1000000000", 2);

        Reader reader = createMSISDNReaderWithHeaders(",,9439986187,1000000000,1");

        try {
            swcUpdateImportService.importMSISDNData(reader);
        } catch(CsvImportDataException e) {

            assertBookmark("1000000000", 1);   // Records expected is 1 instead of 2 since update fails
            assertActivity("1000000000", 2);
        }
    }

    private Reader createMSISDNReaderWithHeaders(String... lines) {
        StringBuilder builder = new StringBuilder();
        builder.append("NMS SWC-ID,MCTS SWC-ID,MSISDN,NEW MSISDN,STATE").append("\n");
        for (String line : lines) {
            builder.append(line).append("\n");
        }
        return new StringReader(builder.toString());
    }

    private Reader createLanguageReaderWithHeaders(String... lines) {
        StringBuilder builder = new StringBuilder();
        builder.append("NMS SWC-ID,MCTS SWC-ID,MSISDN,LANGUAGE CODE,STATE").append("\n");
        for (String line : lines) {
            builder.append(line).append("\n");
        }
        return new StringReader(builder.toString());
    }

    private Reader read(String resource) {
        return new InputStreamReader(getClass().getClassLoader().getResourceAsStream(resource));
    }

    /**
     * Method used to import CSV File For updating SWC Data. option can be
     * "msisdn" or "language"
     */
    private HttpResponse importCsvFileForSWCUpdate(String option,
            String fileName)
            throws InterruptedException, IOException {
        HttpPost httpPost;
        if (StringUtils.isBlank(option)) {
            // update using import
            httpPost = new HttpPost(String.format(
                    "http://localhost:%d/swcUpdate/import",
                    TestContext.getJettyPort()));
        } else {
            httpPost = new HttpPost(String.format(
                    "http://localhost:%d/swcUpdate/update/%s",
                    TestContext.getJettyPort(), option));
        }
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        builder.addPart(
                "csvFile",
                new FileBody(new File(String.format(
                        "src/test/resources/csv/%s", fileName))));
        httpPost.setEntity(builder.build());

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(
                httpPost, RequestBuilder.ADMIN_USERNAME,
                RequestBuilder.ADMIN_PASSWORD);
        return response;
    }

    /**
     * To verify language is updated successfully when MCTS SWC ID is provided.
     */
    @Test
    public void verifyFT550() throws InterruptedException, IOException {
        Swachchagrahi swc = new Swachchagrahi(1000000000L);
        swc.setSwcId("72185");
        swc.setLanguage(rh.kannadaLanguage());
        swc.setState(rh.delhiState());
        swc.setDistrict(rh.newDelhiDistrict());
        swc.setJobStatus(SwcJobStatus.ACTIVE);
        swcService.add(swc);

        assertEquals(
                HttpStatus.SC_OK,
                importCsvFileForSWCUpdate("language",
                        "swc_language_update_only_swcId.csv").getStatusLine()
                        .getStatusCode());

        swc = swcService.getBySwcId("72185");
        assertEquals(rh.hindiLanguage(), swc.getLanguage());

        assertEquals(1, csvAuditRecordDataService.count());
        assertEquals("Success", csvAuditRecordDataService.retrieveAll().get(0)
                .getOutcome());
    }

    /**
     * To verify language is updated successfully when MSISDN is provided.
     */
    @Test
    public void verifyFT551() throws InterruptedException, IOException {
        Swachchagrahi swc = new Swachchagrahi(1000000000L);
        swc.setSwcId("72185");
        swc.setLanguage(rh.kannadaLanguage());
        swc.setState(rh.delhiState());
        swc.setDistrict(rh.newDelhiDistrict());
        swc.setJobStatus(SwcJobStatus.ACTIVE);
        swcService.add(swc);

        assertEquals(
                HttpStatus.SC_OK,
                importCsvFileForSWCUpdate("language",
                        "swc_language_update_only_MSISDN.csv").getStatusLine()
                        .getStatusCode());

        swc = swcService.getBySwcId("72185");
        assertEquals(rh.hindiLanguage(), swc.getLanguage());

        assertEquals(1, csvAuditRecordDataService.count());
        assertEquals("Success", csvAuditRecordDataService.retrieveAll().get(0)
                .getOutcome());
    }

    /**
     * To verify language updated is getting rejected when language provided is
     * having invalid value.
     */
    // TODO JIRA issue https://applab.atlassian.net/browse/NMS-252
    @Test
    public void verifyFT552() throws InterruptedException, IOException {
        Swachchagrahi swc = new Swachchagrahi(1000000000L);
        swc.setSwcId("72185");
        swc.setLanguage(rh.kannadaLanguage());
        swc.setState(rh.delhiState());
        swc.setDistrict(rh.newDelhiDistrict());
        swcService.add(swc);

        assertEquals(
                HttpStatus.SC_BAD_REQUEST,
                importCsvFileForSWCUpdate("language",
                        "swc_language_update_lang_error.csv").getStatusLine()
                        .getStatusCode());

        swc = swcService.getBySwcId("72185");
        assertEquals(rh.kannadaLanguage(), swc.getLanguage());

        assertEquals(1, csvAuditRecordDataService.count());
        assertTrue(csvAuditRecordDataService.retrieveAll().get(0).getOutcome()
                .contains("Failure"));
    }

    /**
     * To verify MSISDN is updated successfully when MCTS SWC ID is provided.
     */
    @Test
    public void verifyFT555() throws InterruptedException, IOException {
        Swachchagrahi swc = new Swachchagrahi(1000000000L);
        swc.setSwcId("72185");
        swc.setLanguage(rh.kannadaLanguage());
        swc.setState(rh.delhiState());
        swc.setDistrict(rh.newDelhiDistrict());
        swc.setJobStatus(SwcJobStatus.ACTIVE);
        swcService.add(swc);

        assertEquals(
                HttpStatus.SC_OK,
                importCsvFileForSWCUpdate("msisdn",
                        "swc_msisdn_update_only_swcId.csv").getStatusLine()
                        .getStatusCode());

        swc = swcService.getByContactNumber(9439986187L);
        assertNotNull(swc);

        swc = swcService.getByContactNumber(1000000000L);
        assertNull(swc);

        assertEquals(1, csvAuditRecordDataService.count());
        assertEquals("Success", csvAuditRecordDataService.retrieveAll().get(0)
                .getOutcome());
    }

    /*
     * To verify location is updated successfully when MCTS SWC ID is provided.
     */
    // TODO https://applab.atlassian.net/browse/NMS-255
    @Test
    public void verifyFT558() throws InterruptedException, IOException {
        // create SWC record having state as "Delhi" and district as "new delhi district"
        Swachchagrahi swc = new Swachchagrahi("Aisha Bibi", 1234567899L);
        swc.setState(rh.delhiState());
        swc.setDistrict(rh.newDelhiDistrict());
        swc.setLanguage(rh.hindiLanguage());
        swc.setJobStatus(SwcJobStatus.ACTIVE);
        swcService.add(swc);

        // update SWC district to "southDelhiDistrict"
        rh.southDelhiDistrict();

        HttpResponse response = importCsvFileForSWCUpdate(null,
                "swc_FT_558.txt");
        assertEquals(HttpStatus.SC_OK, response.getStatusLine()
                .getStatusCode());

        swc = swcService.getByContactNumber(1234567899L);
        assertEquals(rh.southDelhiDistrict().getCode(), swc.getDistrict()
                .getCode());
        assertEquals(rh.delhiState().getCode(), swc.getState().getCode());

        // Language should not be updated
        assertEquals(rh.hindiLanguage().getCode(), swc.getLanguage().getCode());

        // Assert audit trail log
        CsvAuditRecord csvAuditRecord = csvAuditRecordDataService.retrieveAll()
                .get(0);
        assertEquals("/swcUpdate/import", csvAuditRecord.getEndpoint());
        assertTrue(csvAuditRecord.getOutcome().contains("Success"));
        assertEquals("swc_FT_558.txt", csvAuditRecord.getFile());
    }

    /*
     * To verify location update is rejected when state provided is having
     * invalid value.
     */
    @Test
    public void verifyFT560() throws InterruptedException, IOException {
        // create SWC record
        Swachchagrahi swc = new Swachchagrahi("Aisha Bibi", 1234567899L);
        swc.setState(rh.delhiState());
        swc.setDistrict(rh.newDelhiDistrict());
        swc.setLanguage(rh.hindiLanguage());
        swcService.add(swc);

        // update state to "State 10" which doesn't exist in DB
        HttpResponse response = importCsvFileForSWCUpdate(null,
                "swc_FT_560.txt");
        assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine()
                .getStatusCode());

        // Assert audit trail log
        CsvAuditRecord csvAuditRecord = csvAuditRecordDataService.retrieveAll()
                .get(0);
        assertEquals("/swcUpdate/import", csvAuditRecord.getEndpoint());
        assertTrue(csvAuditRecord.getOutcome().contains("Failure: "));
        assertEquals("swc_FT_560.txt", csvAuditRecord.getFile());
    }

    /*
     * To verify location update is rejected when District provided is having
     * invalid value.
     */
    @Test
    public void verifyFT561() throws InterruptedException, IOException {
        // create SWC record
        Swachchagrahi swc = new Swachchagrahi("Aisha Bibi", 1234567899L);
        swc.setState(rh.delhiState());
        swc.setDistrict(rh.newDelhiDistrict());
        swc.setLanguage(rh.hindiLanguage());
        swcService.add(swc);

        // update SWC district to a value which doesn't exist in DB
        HttpResponse response = importCsvFileForSWCUpdate(null,
                "swc_FT_561.txt");
        assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine()
                .getStatusCode());

        // Assert audit trail log
        CsvAuditRecord csvAuditRecord = csvAuditRecordDataService.retrieveAll()
                .get(0);
        assertEquals("/swcUpdate/import", csvAuditRecord.getEndpoint());
        assertTrue(csvAuditRecord.getOutcome().contains("Failure: "));
        assertEquals("swc_FT_561.txt", csvAuditRecord.getFile());
    }

    /**
     * Method used to add Bookmark, Completion and Activity record with given contactNumber
     */
    private void createMaRecords(Long contactNumber) {

        bookmarkDataService.create(new Bookmark(contactNumber.toString(), "1", "1", "1", new HashMap<String, Object>()));

        CourseCompletionRecord ccr = new CourseCompletionRecord(contactNumber, 35, "score", false);
        courseCompletionRecordDataService.create(ccr);
//        String externalId, String courseName, String chapterName, String lessonName, DateTime startTime, DateTime completionTime, ActivityState.STARTED);
        ActivityRecord ar = new ActivityRecord(contactNumber.toString(), "1", "1", "1", null,null , ActivityState.STARTED);
        activityDataService.create(ar);
        ar = new ActivityRecord(contactNumber.toString(), "1", "1", "1", null,null , ActivityState.COMPLETED);
        activityDataService.create(ar);
    }

    private void assertMaRecords(Long oldContactNumber, Long newContactNumber) {

        String oldContact = oldContactNumber.toString();
        String newContact = newContactNumber.toString();

        assertBookmark(oldContact, 0);
        assertBookmark(newContact, 1);

        assertActivity(oldContact, 0);
        assertActivity(newContact, 2);
    }

    private void assertBookmark(String contactNumber, int expected) {
        List<Bookmark> bm = bookmarkDataService.findBookmarksForUser(contactNumber);
        assertTrue(bm.size() == expected);
    }

    private void assertActivity(String contactNumber, int expected) {
        List<ActivityRecord> ar = activityDataService.findRecordsForUser(contactNumber);
        assertTrue(ar.size() == expected);
    }
}