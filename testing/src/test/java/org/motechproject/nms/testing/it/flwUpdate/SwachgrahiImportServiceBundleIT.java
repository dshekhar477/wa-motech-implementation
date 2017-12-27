package org.motechproject.nms.testing.it.flwUpdate;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.mtraining.domain.ActivityState;
import org.motechproject.mtraining.repository.ActivityDataService;
import org.motechproject.nms.csv.domain.CsvAuditRecord;
import org.motechproject.nms.csv.exception.CsvImportDataException;
import org.motechproject.nms.csv.repository.CsvAuditRecordDataService;
import org.motechproject.nms.swc.domain.ContactNumberAudit;
import org.motechproject.nms.swc.domain.SwachchagrahiStatus;
import org.motechproject.nms.swc.domain.SwcJobStatus;
import org.motechproject.nms.swc.domain.Swachchagrahi;
import org.motechproject.nms.swc.repository.ContactNumberAuditDataService;
import org.motechproject.nms.swc.repository.SwcDataService;
import org.motechproject.nms.swcUpdate.service.SwcImportService;
import org.motechproject.nms.swc.service.SwcService;
import org.motechproject.nms.swc.domain.SubscriptionOrigin;
import org.motechproject.nms.mobileacademy.dto.MaBookmark;
import org.motechproject.nms.mobileacademy.repository.CourseCompletionRecordDataService;
import org.motechproject.nms.mobileacademy.service.MobileAcademyService;
import org.motechproject.nms.region.domain.*;
import org.motechproject.nms.region.domain.Block;
import org.motechproject.nms.region.repository.CircleDataService;
import org.motechproject.nms.region.repository.LanguageDataService;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.nms.region.service.BlockService;
import org.motechproject.nms.region.service.DistrictService;
import org.motechproject.nms.region.service.LanguageService;
import org.motechproject.nms.region.service.PanchayatService;
import org.motechproject.nms.testing.it.api.utils.RequestBuilder;
import org.motechproject.nms.testing.service.TestingService;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.motechproject.testing.osgi.http.SimpleHttpClient;
import org.motechproject.testing.utils.TestContext;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.motechproject.nms.testing.it.utils.RegionHelper.createCircle;
import static org.motechproject.nms.testing.it.utils.RegionHelper.createDistrict;
import static org.motechproject.nms.testing.it.utils.RegionHelper.createLanguage;
import static org.motechproject.nms.testing.it.utils.RegionHelper.createState;
import static org.motechproject.nms.testing.it.utils.RegionHelper.createTaluka;
import static org.motechproject.nms.testing.it.utils.RegionHelper.createVillage;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class SwachgrahiImportServiceBundleIT extends BasePaxIT {

    @Inject
    LanguageDataService languageDataService;
    @Inject
    LanguageService languageService;
    @Inject
    StateDataService stateDataService;
    @Inject
    DistrictService districtService;
    @Inject
    CircleDataService circleDataService;
    @Inject
    SwcDataService swcDataService;
    @Inject
    TestingService testingService;
    @Inject
    SwcService swcService;
    @Inject
    BlockService talukaDataService;
    @Inject
    PanchayatService panchayatService;
    @Inject
    SwcImportService swcImportService;
    @Inject
    ContactNumberAuditDataService contactNumberAuditDataService;
    @Inject
    ActivityDataService activityDataService;
    @Inject
    MobileAcademyService maService;
    @Inject
    CourseCompletionRecordDataService courseCompletionRecordDataService;


    @Inject
    private CsvAuditRecordDataService csvAuditRecordDataService;

    @Inject
    PlatformTransactionManager transactionManager;

    public static final String SUCCESS = "Success";
    private static final String VALID_CALL_ID = "1234567890123456789012345";

    @Before
    public void setUp() {
        testingService.clearDatabase();

        Language lang1 = createLanguage("L1", "Lang 1");
        languageDataService.create(lang1);

        Circle circle1 = createCircle("Circle 1");
        circleDataService.create(circle1);

        State state1 = createState(1L, "State 1");
        District district11 = createDistrict(state1, 11L, "District 11", lang1, circle1);
        District district12 = createDistrict(state1, 12L, "District 12");

        District kuraput = createDistrict(state1, 29L, "Koraput");
        District kandhamal = createDistrict(state1, 21L, "Kandhamal");
        District ganjam = createDistrict(state1, 19L, "Ganjam");
        District jharsuguda = createDistrict(state1, 2L, "Jharsuguda");
        District bargarh = createDistrict(state1, 1L, "Bargarh");
        District puri = createDistrict(state1, 18L, "Puri");

        Block similiguda = createTaluka(kuraput, (long)0463, "Similiguda", 1);
        Block Phulabani = createTaluka(kandhamal, (long)0360, "Phulabani Town", 1);
        Block kotagarh = createTaluka(kandhamal, (long)0371, "Kotagarh", 1);
        Block digapahandi = createTaluka(ganjam, (long)0343, "DIGAPAHANDI", 1);
        Block lakhanpur = createTaluka(jharsuguda, (long)0017, "Lakhanpur P.S.", 1);
        Block baliguda = createTaluka(kandhamal, (long)0367, "Baliguda", 1);
        Block bhatli = createTaluka(bargarh, (long)0013, "Bhatli", 1);
        Block pipili = createTaluka(puri, (long)0304, "Pipili", 1);



        Panchayat kunduli = createVillage(pipili, 0L, 28981L, "Bharatipur(28981)");
        Panchayat Phulbani = createVillage(pipili, 0L, 10005284L, "Nuasahi *");
        Panchayat Kotagarh = createVillage(pipili, 0L, 28981L, "Bharatipur(28981)");
        Panchayat DIGAPAHANDI = createVillage(pipili, 0L, 10005284L, "Nuasahi *");
        Panchayat Lakhanpur = createVillage(pipili, 0L, 28981L, "Bharatipur(28981)");
        Panchayat Baliguda = createVillage(pipili, 0L, 10005284L, "Nuasahi *");
        Panchayat BHATLI = createVillage(pipili, 0L, 28981L, "Bharatipur(28981)");
        Panchayat Pipli = createVillage(pipili, 0L, 10005284L, "Nuasahi *");

        similiguda.getPanchayats().add(kunduli);
        Phulabani.getPanchayats().add(Phulbani);
        kotagarh.getPanchayats().add(Kotagarh);
        digapahandi.getPanchayats().add(DIGAPAHANDI);
        lakhanpur.getPanchayats().add(Lakhanpur);
        baliguda.getPanchayats().add(Baliguda);
        bhatli.getPanchayats().add(BHATLI);
        pipili.getPanchayats().add(Pipli);


        kuraput.getBlocks().add(similiguda);
        kandhamal.getBlocks().addAll(Arrays.asList(Phulabani, kotagarh, baliguda));
        ganjam.getBlocks().add(digapahandi);
        jharsuguda.getBlocks().add(lakhanpur);
        bargarh.getBlocks().add(bhatli);
        puri.getBlocks().add(pipili);

        state1.getDistricts().addAll(Arrays.asList(district11, district12,
                kuraput, kandhamal, ganjam, jharsuguda, bargarh, puri));
        stateDataService.create(state1);
    }

    // This test should load the FLW with MCTS id '#1' and attempt to update their MSISDN to a number already
    // in use.  This should result in a unique constraint exception
    @Test(expected = CsvImportDataException.class)
    public void testImportMSISDNConflict() throws Exception {
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        State state = stateDataService.findByName("State 1");
        District district = state.getDistricts().iterator().next();

        Swachchagrahi flw = new Swachchagrahi("Existing With MSISDN", 1234567890L);
        flw.setState(state);
        flw.setDistrict(district);
        flw.setJobStatus(SwcJobStatus.ACTIVE);
        swcService.add(flw);

        flw = new Swachchagrahi("Will Update Conflict MSISDN", 1111111111L);
        flw.setState(state);
        flw.setDistrict(district);
        flw.setJobStatus(SwcJobStatus.ACTIVE);
        swcService.add(flw);
        transactionManager.commit(status);

        Reader reader = createReaderWithHeaders("#1\t1234567890\tFLW 0\t11\t18-08-2016\tASHA\tActive");
        swcImportService.importData(reader, SubscriptionOrigin.MCTS_IMPORT);
    }

    // This test should load the FLW with MSISDN 1234567890 however that FLW already has a different MCTS ID
    // assigned to them.  This should result in an exception
    //NMS_FT_538
    @Test(expected = CsvImportDataException.class)
    public void testImportByMSISDNConflictWithMCTSId() throws Exception {
        Swachchagrahi flw = new Swachchagrahi("Frank Lloyd Wright", 1234567890L);
        flw.setJobStatus(SwcJobStatus.ACTIVE);
        swcService.add(flw);

        Reader reader = createReaderWithHeaders("#1\t1234567890\tFLW 0\t11\t18-08-2016\tASHA\tActive");
        swcImportService.importData(reader, SubscriptionOrigin.MCTS_IMPORT);
    }

    @Test
    public void testASHAvalidation() throws Exception {
        swcImportService.importData(read("csv/anm-asha.txt"), SubscriptionOrigin.MCTS_IMPORT);
        List<Swachchagrahi> flws = swcDataService.retrieveAll();
        assertEquals(9,flws.size());
    }

    /**
     * VerifyFT513  verify that status of flw must be set to "inactive" when the flw data is imported into
     * the NMS DB and the user has not yet called
     */
    @Test
    public void testImportWhenDistrictLanguageLocationPresent() throws Exception {
        Reader reader = createReaderWithHeaders("#0\t1234567890\tFLW 0\t11\t18-08-2016\tASHA\tActive");
        swcImportService.importData(reader, SubscriptionOrigin.MCTS_IMPORT);

        Swachchagrahi flw = swcService.getByContactNumber(1234567890L);
        assertFLW(flw, "#0", 1234567890L, "FLW 0", "District 11", "L1");
        assertEquals(SwachchagrahiStatus.INACTIVE, flw.getCourseStatus());
    }

    @Test
    public void testImportWhenDistrictLanguageLocationNotPresent() throws Exception {
        Reader reader = createReaderWithHeaders("#0\t1234567890\tFLW 0\t12\t18-08-2016\tASHA\tActive");
        swcImportService.importData(reader, SubscriptionOrigin.MCTS_IMPORT);

        Swachchagrahi flw = swcService.getByContactNumber(1234567890L);
        assertFLW(flw, "#0", 1234567890L, "FLW 0", "District 12", null);
    }

    /**
     * NMS_FT_541: To verify FLW upload is rejected when mandatory parameter district is missing.
     */
    @Test(expected = CsvImportDataException.class)
    public void testImportWhenDistrictNotPresent() throws Exception {
        Reader reader = createReaderWithHeaders("#0\t1234567890\tFLW 0\t\t18-08-2016\tASHA\tActive");
        swcImportService.importData(reader, SubscriptionOrigin.MCTS_IMPORT);
    }


    @Test
    public void testImportFromSampleDataFile() throws Exception {
        swcImportService.importData(read("csv/anm-asha.txt"), SubscriptionOrigin.MCTS_IMPORT);

        Swachchagrahi flw1 = swcService.getByContactNumber(9999999996L);
        assertFLW(flw1, "72185", 9999999996L, "Bishnu Priya Behera", "Koraput", null);

        // verify location data was created on the fly
        State state = stateDataService.findByCode(1L);
        District district = districtService.findByStateAndCode(state, 18L);
        Block block = talukaDataService.findByDistrictAndCode(district, "111");
        assertEquals("Block", block.getName());

        Panchayat panchayat = panchayatService.findByBlockAndVcodeAndSvid(block, 555L, 0L);
        assertEquals("Panchayat", panchayat.getName());
    }

    /**
     * To verify FLW record is uploaded successfully when all mandatory parameters are present.
     */
    @Test
    public void verifyFT535() throws Exception {
        importCsvFileForFLW("flw.txt");
        Swachchagrahi flw1 = swcService.getByContactNumber(1234567899L);
        assertFLW(flw1, "1", 1234567899L, "Aisha Bibi", "District 11", "L1");
        assertEquals("State{name='State 1', code=1}", flw1.getState().toString());
        assertEquals(SwachchagrahiStatus.INACTIVE, flw1.getCourseStatus());
        // Assert audit trail log
        CsvAuditRecord csvAuditRecord = csvAuditRecordDataService.retrieveAll()
                .get(0);
        assertEquals("/flwUpdate/import", csvAuditRecord.getEndpoint());
        assertEquals(SUCCESS, csvAuditRecord.getOutcome());
        assertEquals("flw.txt", csvAuditRecord.getFile());
    }

    /**
     * To verify FLW status must be updated successfully from Anonymous to Active.
     */
    @Test
    public void verifyFT536() throws Exception {
        Swachchagrahi flw = new Swachchagrahi("Frank Lloyd Wright", 1234567890L);
        flw.setJobStatus(SwcJobStatus.ACTIVE);
        swcService.add(flw);
        Reader reader = createReaderWithHeaders("#0\t1234567890\tFLW 0\t11\t18-08-2016\tASHA\tActive");
        swcImportService.importData(reader, SubscriptionOrigin.MCTS_IMPORT);
        Swachchagrahi flw1 = swcService.getByContactNumber(1234567890L);
        assertFLW(flw1, "#0", 1234567890L, "FLW 0", "District 11", "L1");
        assertEquals("State{name='State 1', code=1}", flw1.getState().toString());
        assertEquals(SwachchagrahiStatus.ACTIVE, flw1.getCourseStatus());
    }

    /**
     * To verify FLW upload is rejected when mandatory parameter MSISDN is missing.
     */
    @Test(expected = CsvImportDataException.class)
    public void verifyFT537() throws Exception {
        Reader reader = createReaderWithHeaders("#0\t\tFLW 0\t11\t18-08-2016\tASHA\tActive");
        swcImportService.importData(reader, SubscriptionOrigin.MCTS_IMPORT);
    }

    /**
     * To verify FLW upload is rejected when mandatory parameter state is missing.
     */
    @Test(expected = IllegalArgumentException.class)
    public void verifyFT540() throws Exception {
        Reader reader = createReaderWithHeadersWithNoState("#1\t1234567890\tFLW 0\t11\t18-08-2016\tASHA\tActive");
        swcImportService.importData(reader, SubscriptionOrigin.MCTS_IMPORT);
    }

    /**
     * To verify FLW upload is rejected when mandatory parameter name is missing.
     */
    @Test
    public void verifyFT542() throws Exception {
            importCsvFileForFLW("flw_name_missing.txt");
            // Assert audit trail log
            CsvAuditRecord csvAuditRecord = csvAuditRecordDataService.retrieveAll()
                    .get(0);
            assertEquals("/flwUpdate/import", csvAuditRecord.getEndpoint());
            assertEquals("Failure: The number of columns to be processed (4) must match the number of CellProcessors (5): check that the number of CellProcessors you have defined matches the expected number of columns being read/written", csvAuditRecord.getOutcome());
            assertEquals("flw_name_missing.txt", csvAuditRecord.getFile());
    }

    /**
     * To verify FLW upload is rejected when mandatory parameter MSISDN is having invalid value
     */
    @Test(expected = CsvImportDataException.class)
    public void verifyFT543() throws Exception {
        Reader reader = createReaderWithHeaders("#1\t123456789\tFLW 1\t11\t18-08-2016\tASHA\tActive");
        swcImportService.importData(reader, SubscriptionOrigin.MCTS_IMPORT);
    }

    /**
     * To verify FLW upload is rejected when mandatory parameter MSISDN is having invalid value
     */
    @Test(expected = CsvImportDataException.class)
    public void verifyFT544() throws Exception {
        Reader reader = createReaderWithHeadersWithInvalidState("#1\t1234567890\tFLW 1\t11\t18-08-2016\tASHA\tActive");
        swcImportService.importData(reader, SubscriptionOrigin.MCTS_IMPORT);
    }

    /**
     * To verify FLW upload is rejected when mandatory parameter District is having invalid value
     */
    @Test(expected = CsvImportDataException.class)
    public void verifyFT545() throws Exception {
        Reader reader = createReaderWithHeaders("#1\t1234567890\tFLW 1\t111\t18-08-2016\tASHA\tActive");
        swcImportService.importData(reader, SubscriptionOrigin.MCTS_IMPORT);
    }

    /**
     * To verify FLW upload is rejected when combination of state and District is invalid.
     */
    @Test(expected = CsvImportDataException.class)
    public void verifyFT546() throws Exception {
        State state2 = createState(2L, "State 2");
        createDistrict(state2, 22L, "District 22");
        Reader reader = createReaderWithHeaders("#1\t1234567890\tFLW 1\t22\t18-08-2016\tASHA\tActive");
        swcImportService.importData(reader, SubscriptionOrigin.MCTS_IMPORT);
    }

    private void assertFLW(Swachchagrahi flw, String mctsFlwId, Long contactNumber, String name, String districtName, String languageLocationCode) {
        assertNotNull(flw);
        assertEquals(contactNumber, null != flw.getContactNumber() ? flw.getContactNumber() : null);
        assertEquals(name, flw.getName());
        assertEquals(districtName, null != flw.getDistrict() ? flw.getDistrict().getName() : null);
        assertEquals(languageLocationCode, null != flw.getLanguage() ? flw.getLanguage().getCode() : null);
    }

    private Reader createReaderWithHeaders(String... lines) {
        StringBuilder builder = new StringBuilder();
        builder.append("\n");
        builder.append("State Name : State 1").append("\n");
        builder.append("\n");
        builder.append("ID\tContact_No\tName\tDistrict_ID\tUpdated_On\tType\tGF_Status").append("\n");
        for (String line : lines) {
            builder.append(line).append("\n");
        }
        return new StringReader(builder.toString());
    }
    
    private Reader createReaderWithHeadersWithNoState(String... lines) {
        StringBuilder builder = new StringBuilder();
        builder.append("\n");
        builder.append("State Name :").append("\n");
        builder.append("\n");
        builder.append("ID\tContact_No\tName\tDistrict_ID\tUpdated_On\tType\tGF_Status").append("\n");
        for (String line : lines) {
            builder.append(line).append("\n");
        }
        return new StringReader(builder.toString());
    }

    private Reader createReaderWithHeadersWithInvalidState(String... lines) {
        StringBuilder builder = new StringBuilder();
        builder.append("\n");
        builder.append("State Name : State 2").append("\n");
        builder.append("\n");
        builder.append("ID\tContact_No\tName\tDistrict_ID\tUpdated_On\tType\tGF_Status").append("\n");
        for (String line : lines) {
            builder.append(line).append("\n");
        }
        return new StringReader(builder.toString());
    }

    private Reader read(String resource) {
        return new InputStreamReader(getClass().getClassLoader().getResourceAsStream(resource));
    }

    /**
     * Method used to import CSV File For FLW Data
     */
    private void importCsvFileForFLW(String fileName) throws InterruptedException, IOException {
        HttpPost httpPost = new HttpPost(String.format("http://localhost:%d/flwUpdate/import", TestContext.getJettyPort()));
        FileBody fileBody = new FileBody(new File(String.format("src/test/resources/csv/%s", fileName)));
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        builder.addPart("csvFile", fileBody);
        httpPost.setEntity(builder.build());
        SimpleHttpClient.httpRequestAndResponse(httpPost, RequestBuilder.ADMIN_USERNAME,
                RequestBuilder.ADMIN_PASSWORD);
    }

    /**
     * To verify location is updated successfully when MSISDN is provided.
     */
    // TODO JIRA issue: https://applab.atlassian.net/browse/NMS-253
    @Test
    public void verifyFT559() throws InterruptedException, IOException {
        State state = stateDataService.findByName("State 1");
        District district1 = districtService.findByStateAndName(state, "District 11");
        Language language1 = languageService.getForCode("L1");

        Swachchagrahi flw = new Swachchagrahi("Test MSISDN", 1234567890L);
        flw.setState(state);
        flw.setDistrict(district1);
        flw.setLanguage(language1);
        flw.setJobStatus(SwcJobStatus.ACTIVE);
        swcService.add(flw);

        importCsvFileForFLW("flw_location_update_msisdn.txt");

        flw = swcService.getByContactNumber(1234567890L);

        // deleting the FLW to avoid conflicts at later stage
        flw.setCourseStatus(SwachchagrahiStatus.INVALID);
        flw.setInvalidationDate(DateTime.now().minusYears(1));
        swcService.update(flw);
        swcService.delete(flw);

        assertFLW(flw, "#0", null, "Test MSISDN", "District 12", language1.getCode());

        List<CsvAuditRecord> auditRecords = csvAuditRecordDataService.retrieveAll();
        assertNotNull(auditRecords);
        assertEquals(1, auditRecords.size());

        CsvAuditRecord auditRecord = auditRecords.get(0);
        assertEquals("Success", auditRecord.getOutcome());
        assertEquals("flw_location_update_msisdn.txt", auditRecord.getFile());
    }

    /**
     * Verify that an FLWs state can be updated
     */
    @Test
    public void verifyNIP166() throws InterruptedException, IOException {
        State state = stateDataService.findByName("State 1");
        District district1 = districtService.findByStateAndName(state, "District 11");
        Language language1 = languageService.getForCode("L1");

        State state2 = createState(2L, "State 2");
        District district22 = createDistrict(state2, 22L, "District 22");
        state2.getDistricts().add(district22);
        stateDataService.create(state2);

        Swachchagrahi flw = new Swachchagrahi("Test MSISDN", 1234567890L);
        flw.setState(state);
        flw.setDistrict(district1);
        flw.setLanguage(language1);
        flw.setJobStatus(SwcJobStatus.ACTIVE);
        swcService.add(flw);

        importCsvFileForFLW("flw_update_state_by_msisdn.txt");

        flw = swcService.getByContactNumber(1234567890L);

        assertFLW(flw, "#0", 1234567890L, "Test MSISDN", "District 22", language1.getCode());

        List<CsvAuditRecord> auditRecords = csvAuditRecordDataService.retrieveAll();
        assertNotNull(auditRecords);
        assertEquals(1, auditRecords.size());

        CsvAuditRecord auditRecord = auditRecords.get(0);
        assertEquals("Success", auditRecord.getOutcome());
        assertEquals("flw_update_state_by_msisdn.txt", auditRecord.getFile());

        // deleting the FLW to avoid conflicts at later stage
        flw.setCourseStatus(SwachchagrahiStatus.INVALID);
        flw.setInvalidationDate(DateTime.now().minusYears(1));
        swcService.update(flw);
        swcService.delete(flw);
    }

    // Test whether MSISDN is updated in Bookmark, Activity and Course Completion Records along with Flw
    @Test
    public void testMsisdnUpdateInMa() throws Exception {
        Reader reader = createReaderWithHeaders("#0\t1234567890\tFLW 0\t11\t18-08-2016\tASHA\tActive");
        swcImportService.importData(reader, SubscriptionOrigin.MCTS_IMPORT);
        Long oldMsisdn = 1234567890L;

        Swachchagrahi flw = swcService.getByContactNumber(oldMsisdn);
        assertFLW(flw, "#0", oldMsisdn, "FLW 0", "District 11", "L1");

        Long flwId = flw.getId();
        MaBookmark bookmark = new MaBookmark(flwId, VALID_CALL_ID, null, null);
        maService.setBookmark(bookmark);
        assertNotNull(maService.getBookmark(oldMsisdn, VALID_CALL_ID));
        assertEquals(1, activityDataService.findRecordsForUserByState(oldMsisdn.toString(), ActivityState.STARTED).size());

        bookmark.setBookmark("COURSE_COMPLETED");
        Map<String, Integer> scores = new HashMap<>();
        for (int i = 1; i < 12; i++) {
            scores.put(String.valueOf(i), 3);
        }
        bookmark.setScoresByChapter(scores);
        maService.setBookmark(bookmark);

        // Update Msisdn
        reader = createReaderWithHeaders("#0\t9876543210\tFLW 0\t11\t18-08-2016\tASHA\tActive");
        swcImportService.importData(reader, SubscriptionOrigin.MCTS_IMPORT);
        Long newMsisdn = 9876543210L;

        flw = swcService.getByContactNumber(newMsisdn);
        assertFLW(flw, "#0", newMsisdn, "FLW 0", "District 11", "L1");

        assertNull(maService.getBookmark(oldMsisdn, VALID_CALL_ID));
        assertNotNull(maService.getBookmark(newMsisdn, VALID_CALL_ID));

        assertEquals(0, activityDataService.findRecordsForUserByState(oldMsisdn.toString(), ActivityState.STARTED).size());
        assertEquals(1, activityDataService.findRecordsForUserByState(newMsisdn.toString(), ActivityState.STARTED).size());

        List<ContactNumberAudit> contactNumberAudits = contactNumberAuditDataService.retrieveAll();
        assertEquals(1, contactNumberAudits.size());
        assertEquals(flwId, contactNumberAudits.get(0).getSwcId());
        assertEquals(oldMsisdn, contactNumberAudits.get(0).getOldCallingNumber());
        assertEquals(newMsisdn, contactNumberAudits.get(0).getNewCallingNumber());
    }
}
