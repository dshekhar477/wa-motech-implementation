package org.motechproject.nms.testing.it.ma;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.event.MotechEvent;
import org.motechproject.mtraining.domain.ActivityState;
import org.motechproject.mtraining.domain.Bookmark;
import org.motechproject.mtraining.repository.ActivityDataService;
import org.motechproject.mtraining.repository.BookmarkDataService;
import org.motechproject.nms.flw.domain.SwachchagrahiStatus;
import org.motechproject.nms.flw.domain.SwcJobStatus;
import org.motechproject.nms.flw.domain.Swachchagrahi;
import org.motechproject.nms.flw.repository.SwcDataService;
import org.motechproject.nms.flw.service.SwcService;
import org.motechproject.nms.mobileacademy.domain.CourseCompletionRecord;
import org.motechproject.nms.mobileacademy.domain.NmsCourse;
import org.motechproject.nms.mobileacademy.dto.MaBookmark;
import org.motechproject.nms.mobileacademy.dto.MaCourse;
import org.motechproject.nms.mobileacademy.exception.CourseNotCompletedException;
import org.motechproject.nms.mobileacademy.repository.CourseCompletionRecordDataService;
import org.motechproject.nms.mobileacademy.repository.NmsCourseDataService;
import org.motechproject.nms.mobileacademy.service.CourseNotificationService;
import org.motechproject.nms.mobileacademy.service.MobileAcademyService;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.Language;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.repository.CircleDataService;
import org.motechproject.nms.region.repository.LanguageDataService;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.nms.region.service.DistrictService;
import org.motechproject.nms.region.service.LanguageService;
import org.motechproject.nms.testing.service.TestingService;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Verify that MobileAcademyService present, functional.
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class MobileAcademyServiceBundleIT extends BasePaxIT {

    @Inject
    MobileAcademyService maService;

    @Inject
    BookmarkDataService bookmarkDataService;

    @Inject
    ActivityDataService activityDataService;

    @Inject
    CourseCompletionRecordDataService courseCompletionRecordDataService;

    @Inject
    SwcDataService swcDataService;

    @Inject
    NmsCourseDataService nmsCourseDataService;

    @Inject
    CourseNotificationService courseNotificationService;

    @Inject
    LanguageDataService languageDataService;

    @Inject
    LanguageService languageService;

    @Inject
    StateDataService stateDataService;

    @Inject
    CircleDataService circleDataService;

    @Inject
    DistrictService districtService;

    @Inject
    SwcService swcService;

    @Inject
    TestingService testingService;

    @Inject
    PlatformTransactionManager transactionManager;

    private static final String VALID_COURSE_NAME = "MobileAcademyCourse";

    private static final String FINAL_BOOKMARK = "COURSE_COMPLETED";

    private static final String VALID_CALL_ID = "1234567890123456789012345";

    @Before
    public void setupMobileAcademy() {

        courseCompletionRecordDataService.deleteAll();
        activityDataService.deleteAll();
        bookmarkDataService.deleteAll();
        nmsCourseDataService.deleteAll();
        testingService.clearDatabase();
    }

    @Test
    public void testSetCourseNoUpdate() throws IOException {
        setupMaCourse();

        NmsCourse originalCourse = nmsCourseDataService.getCourseByName(VALID_COURSE_NAME);
        MaCourse copyCourse = new MaCourse(originalCourse.getName(), originalCourse.getModificationDate().getMillis(), originalCourse.getContent());
        maService.setCourse(copyCourse);

        // verify that modified time (version) didn't change
        assertEquals(nmsCourseDataService.getCourseByName(VALID_COURSE_NAME).getModificationDate(),
                originalCourse.getModificationDate());
    }

    @Test
    public void testSetCourseUpdate() throws IOException {
        setupMaCourse();
        NmsCourse originalCourse = nmsCourseDataService.getCourseByName(VALID_COURSE_NAME);
        String courseContent = originalCourse.getContent();
        MaCourse copyCourse = new MaCourse(originalCourse.getName(), originalCourse.getModificationDate().getMillis(), originalCourse.getContent() + "foo");
        maService.setCourse(copyCourse);

        // verify that modified time (version) did change
        assertNotEquals(nmsCourseDataService.getCourseByName(VALID_COURSE_NAME).getModificationDate(),
                originalCourse.getModificationDate());
        originalCourse.setContent(courseContent);
        nmsCourseDataService.update(originalCourse);
    }

    @Test
    public void testNoCoursePresent() throws IOException {
        setupMaCourse();
        NmsCourse originalCourse = nmsCourseDataService.getCourseByName(VALID_COURSE_NAME);
        nmsCourseDataService.delete(originalCourse);
        assertNull(nmsCourseDataService.getCourseByName(VALID_COURSE_NAME));

        try {
            maService.getCourse();
        } catch (IllegalStateException is) {
            assertTrue(is.toString().contains("No course bootstrapped. Check deployment"));
        }

        nmsCourseDataService.create(new NmsCourse(originalCourse.getName(), originalCourse.getContent()));
    }

    @Test
    public void testMobileAcademyServicePresent() throws Exception {
        assertNotNull(maService);
    }

    @Test
    public void testGetCourse() throws IOException {
        setupMaCourse();
        assertNotNull(maService.getCourse());
    }

    @Test
    public void testGetCourseVersion() throws IOException {
        setupMaCourse();
        assertNotNull(maService.getCourseVersion());
        assertTrue(maService.getCourseVersion() > 0);
    }

    @Test
    public void testGetBookmark() {

        Swachchagrahi flw = new Swachchagrahi(1234567890L);
        flw.setJobStatus(SwcJobStatus.ACTIVE);
        swcService.add(flw);
        String flwId = swcService.getByContactNumber(1234567890L).getId().toString();
        bookmarkDataService.create(new Bookmark(flwId, "1", "1", "1", new HashMap<String, Object>()));
        assertNotNull(maService.getBookmark(1234567890L, VALID_CALL_ID));
    }

    @Test
    public void testGetEmptyBookmark() {

        assertNull(maService.getBookmark(123L, VALID_CALL_ID));
    }

    @Test
    public void testSetNullBookmark() {
        try {
            maService.setBookmark(null);
            throw new IllegalStateException("This test expected an IllegalArgumentException");
        } catch (IllegalArgumentException ia) {
            assertTrue(ia.toString().contains("cannot be null"));
        }
    }

    @Test
    public void testSetNewBookmark() {
        Swachchagrahi flw = new Swachchagrahi(1234567890L);
        flw.setJobStatus(SwcJobStatus.ACTIVE);
        swcService.add(flw);
        flw = swcService.getByContactNumber(1234567890L);
        List<Bookmark> existing = bookmarkDataService.findBookmarksForUser(flw.getId().toString());
        MaBookmark bookmark = new MaBookmark(flw.getId(), VALID_CALL_ID, null, null);
        maService.setBookmark(bookmark);
        List<Bookmark> added = bookmarkDataService.findBookmarksForUser(flw.getId().toString());
        assertTrue(added.size() == (existing.size() + 1));
    }

    @Test
    public void testStartedActivity() {
        Swachchagrahi flw = new Swachchagrahi(1234567890L);
        flw.setJobStatus(SwcJobStatus.ACTIVE);
        swcService.add(flw);
        flw = swcService.getByContactNumber(1234567890L);
        List<Bookmark> existing = bookmarkDataService.findBookmarksForUser(flw.getId().toString());
        MaBookmark bookmark = new MaBookmark(flw.getId(), VALID_CALL_ID, null, null);
        maService.setBookmark(bookmark);
        List<Bookmark> added = bookmarkDataService.findBookmarksForUser(flw.getId().toString());
        assertTrue(added.size() == (existing.size() + 1));
        assertEquals(1, activityDataService.findRecordsForUserByState(flw.getContactNumber().toString(), ActivityState.STARTED).size());
    }

    @Test
    public void testSetExistingBookmark() {

        Swachchagrahi flw = new Swachchagrahi(1234567890L);
        flw.setJobStatus(SwcJobStatus.ACTIVE);
        swcService.add(flw);
        flw = swcService.getByContactNumber(1234567890L);
        MaBookmark bookmark = new MaBookmark(flw.getId(), VALID_CALL_ID, null, null);
        maService.setBookmark(bookmark);
        List<Bookmark> added = bookmarkDataService.findBookmarksForUser(flw.getId().toString());
        assertTrue(added.size() == 1);

        bookmark.setBookmark("Chapter3_Lesson2");
        Map<String, Integer> scores = new HashMap<>();
        scores.put("Quiz1", 4);
        bookmark.setScoresByChapter(scores);
        maService.setBookmark(bookmark);

        MaBookmark retrieved = maService.getBookmark(1234567890L, VALID_CALL_ID);
        assertNotNull(retrieved.getBookmark());
        assertTrue(retrieved.getBookmark().equals("Chapter3_Lesson2"));
        assertNotNull(retrieved.getScoresByChapter());
        assertTrue(retrieved.getScoresByChapter().get("Quiz1") == 4);
    }

    @Test
    public void testSetLastBookmark() {

        long callingNumber = 9876543210L;
        Swachchagrahi flw = new Swachchagrahi(callingNumber);
        flw.setJobStatus(SwcJobStatus.ACTIVE);
        swcService.add(flw);
        flw = swcService.getByContactNumber(callingNumber);
        MaBookmark bookmark = new MaBookmark(flw.getId(), VALID_CALL_ID, null, null);
        maService.setBookmark(bookmark);
        List<Bookmark> added = bookmarkDataService.findBookmarksForUser(flw.getId().toString());
        assertTrue(added.size() == 1);

        bookmark.setBookmark(FINAL_BOOKMARK);
        Map<String, Integer> scores = new HashMap<>();
        for (int i = 1; i < 12; i++) {
            scores.put(String.valueOf(i), ((int) (Math.random() * 100)) % 5);
        }
        bookmark.setScoresByChapter(scores);
        maService.setBookmark(bookmark);
    }

    @Test
    public void testCompletionCount() {

        long callingNumber = 9876543210L;
        Swachchagrahi flw = new Swachchagrahi(callingNumber);
        flw.setJobStatus(SwcJobStatus.ACTIVE);
        swcService.add(flw);
        flw = swcService.getByContactNumber(callingNumber);
        int completionCountBefore = activityDataService.findRecordsForUser(String.valueOf(callingNumber)).size();
        MaBookmark bookmark = new MaBookmark(flw.getId(), VALID_CALL_ID, null, null);
        maService.setBookmark(bookmark);
        List<Bookmark> added = bookmarkDataService.findBookmarksForUser(flw.getId().toString());
        assertTrue(added.size() == 1);

        bookmark.setBookmark(FINAL_BOOKMARK);
        Map<String, Integer> scores = new HashMap<>();
        for (int i = 1; i < 12; i++) {
            scores.put(String.valueOf(i), ((int) (Math.random() * 100)) % 5);
        }
        bookmark.setScoresByChapter(scores);
        maService.setBookmark(bookmark);
        int completionCountAfter = activityDataService.findRecordsForUserByState(String.valueOf(callingNumber), ActivityState.COMPLETED).size();

        assertEquals(completionCountBefore + 1, completionCountAfter);
    }

    @Test
    public void testSetGetLastBookmark() {

        long callingNumber = 9987654321L;
        Swachchagrahi flw = new Swachchagrahi(callingNumber);
        flw.setJobStatus(SwcJobStatus.ACTIVE);
        swcService.add(flw);
        flw = swcService.getByContactNumber(callingNumber);
        MaBookmark bookmark = new MaBookmark(flw.getId(), VALID_CALL_ID, null, null);
        maService.setBookmark(bookmark);
        List<Bookmark> added = bookmarkDataService.findBookmarksForUser(flw.getId().toString());
        assertTrue(added.size() == 1);

        bookmark.setBookmark(FINAL_BOOKMARK);
        Map<String, Integer> scores = new HashMap<>();
        for (int i = 1; i < 12; i++) {
            scores.put(String.valueOf(i), 1);
        }
        bookmark.setScoresByChapter(scores);
        maService.setBookmark(bookmark);

        MaBookmark retrieved = maService.getBookmark(callingNumber, VALID_CALL_ID);
        assertNotNull(retrieved.getFlwId());
        assertNotNull(retrieved.getCallId());
        assertNull(retrieved.getBookmark());
        assertNull(retrieved.getScoresByChapter());
    }

    @Test
    public void testSetGetResetBookmark() {

        long callingNumber = 9987654321L;
        Swachchagrahi flw = new Swachchagrahi(callingNumber);
        flw.setJobStatus(SwcJobStatus.ACTIVE);
        swcService.add(flw);
        flw = swcService.getByContactNumber(callingNumber);
        MaBookmark bookmark = new MaBookmark(flw.getId(), VALID_CALL_ID, null, null);
        maService.setBookmark(bookmark);
        List<Bookmark> added = bookmarkDataService.findBookmarksForUser(flw.getId().toString());
        assertTrue(added.size() == 1);

        bookmark.setBookmark(FINAL_BOOKMARK);
        Map<String, Integer> scores = new HashMap<>();
        for (int i = 1; i < 12; i++) {
            scores.put(String.valueOf(i), 4);
        }
        bookmark.setScoresByChapter(scores);
        maService.setBookmark(bookmark);

        MaBookmark retrieved = maService.getBookmark(callingNumber, VALID_CALL_ID);
        assertNotNull(retrieved.getFlwId());
        assertNotNull(retrieved.getCallId());
        assertNull(retrieved.getBookmark());
        assertNull(retrieved.getScoresByChapter());
    }

    @Test
    public void testResetBookmarkNewStartActivity() {

        long callingNumber = 9987654321L;
        Swachchagrahi flw = new Swachchagrahi(callingNumber);
        flw.setJobStatus(SwcJobStatus.ACTIVE);
        swcService.add(flw);
        flw = swcService.getByContactNumber(callingNumber);
        MaBookmark bookmark = new MaBookmark(flw.getId(), VALID_CALL_ID, null, null);
        maService.setBookmark(bookmark);
        List<Bookmark> added = bookmarkDataService.findBookmarksForUser(flw.getId().toString());
        assertTrue(added.size() == 1);

        // set final bookmark and trigger completed activity record
        bookmark.setBookmark(FINAL_BOOKMARK);
        Map<String, Integer> scores = new HashMap<>();
        for (int i = 1; i < 12; i++) {
            scores.put(String.valueOf(i), 4);
        }
        bookmark.setScoresByChapter(scores);
        maService.setBookmark(bookmark);

        // this beforeCount includes completed activity now
        int beforeCount = activityDataService.findRecordsForUser(String.valueOf(callingNumber)).size();

        // verify that the bookmark is reset on the following get call
        MaBookmark retrieved = maService.getBookmark(callingNumber, VALID_CALL_ID);
        assertNotNull(retrieved.getFlwId());
        assertNotNull(retrieved.getCallId());
        assertNull(retrieved.getBookmark());
        assertNull(retrieved.getScoresByChapter());

        // set new bookmark to trigger started activity
        bookmark.setBookmark("Chapter01_Lesson01");
        scores.clear();
        scores.put("1", 3);
        bookmark.setScoresByChapter(scores);
        maService.setBookmark(bookmark);

        // this will now include the new start activity
        int afterCount = activityDataService.findRecordsForUser(String.valueOf(callingNumber)).size();

        // verify that we added a new activity since the last completion
        assertEquals(beforeCount + 1, afterCount);
    }

    @Test
    public void testTriggerNotificationSent() {

        long callingNumber = 9876543210L;
        Swachchagrahi flw = new Swachchagrahi(callingNumber);
        flw.setJobStatus(SwcJobStatus.ACTIVE);
        swcDataService.create(flw);
        flw = swcService.getByContactNumber(callingNumber);

        MaBookmark bookmark = new MaBookmark(flw.getId(), VALID_CALL_ID, null, null);
        maService.setBookmark(bookmark);
        List<Bookmark> added = bookmarkDataService.findBookmarksForUser(String.valueOf(flw.getId()));
        assertTrue(added.size() == 1);

        bookmark.setBookmark(FINAL_BOOKMARK);
        Map<String, Integer> scores = new HashMap<>();
        for (int i = 1; i < 12; i++) {
            scores.put(String.valueOf(i), 3);
        }
        bookmark.setScoresByChapter(scores);
        maService.setBookmark(bookmark);
        Long flwId = swcService.getByContactNumber(callingNumber).getId();
        CourseCompletionRecord ccr = courseCompletionRecordDataService.findByFlwId(flwId).get(0);
        assertNotNull(ccr);
        assertEquals(ccr.getFlwId(), flwId);
        assertEquals(ccr.getScore(), 33);
    }

    @Test
    public void testTriggerNotificationNotSent() {

        long callingNumber = 9876543211L;
        Swachchagrahi flw = new Swachchagrahi(callingNumber);
        flw.setJobStatus(SwcJobStatus.ACTIVE);
        swcDataService.create(flw);
        flw = swcService.getByContactNumber(callingNumber);
        MaBookmark bookmark = new MaBookmark(flw.getId(), VALID_CALL_ID, null, null);
        maService.setBookmark(bookmark);
        List<Bookmark> added = bookmarkDataService.findBookmarksForUser(String.valueOf(flw.getId()));
        assertTrue(added.size() == 1);

        bookmark.setBookmark(FINAL_BOOKMARK);
        Map<String, Integer> scores = new HashMap<>();
        for (int i = 1; i < 12; i++) {
            scores.put(String.valueOf(i), 1);
        }
        bookmark.setScoresByChapter(scores);
        maService.setBookmark(bookmark);
        assertEquals(1, courseCompletionRecordDataService.findByFlwId(flw.getId()).size());
        assertFalse(courseCompletionRecordDataService.findByFlwId(flw.getId()).get(0).isPassed());
    }

    @Test
    public void testRetriggerNotification() {

        long callingNumber = 9876543211L;
        Swachchagrahi flw = new Swachchagrahi(callingNumber);
        flw.setJobStatus(SwcJobStatus.ACTIVE);
        swcService.add(flw);
        Long flwId = swcService.getByContactNumber(callingNumber).getId();

        CourseCompletionRecord ccr = new CourseCompletionRecord(flwId, 44, "score", true);
        courseCompletionRecordDataService.create(ccr);

        maService.triggerCompletionNotification(flwId);
        List<CourseCompletionRecord> ccrs = courseCompletionRecordDataService.findByFlwId(flwId);
        ccr = ccrs.get(ccrs.size()-1);
        assertTrue(ccr.isSentNotification());
    }

    @Test(expected = CourseNotCompletedException.class)
    public void testRetriggerNotificationException() {

        long callingNumber = 9876543222L;
        maService.triggerCompletionNotification(callingNumber);
    }

    @Test
    public void testNotification() {
        long callingNumber = 2111113333L;

        // Setup language/location and flw for notification
        Swachchagrahi flw = swcService.getByContactNumber(callingNumber);
        if (flw != null) {
            flw.setStatus(SwachchagrahiStatus.INVALID);
            swcService.update(flw);
            swcService.delete(flw);
        }

        createLanguageLocationData();
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        State sampleState = stateDataService.findByCode(1L);
        Language language = languageService.getForCode("50");
        flw = new Swachchagrahi("Test Worker", callingNumber);
        flw.setLanguage(language);
        flw.setState(sampleState);
        flw.setDistrict(sampleState.getDistricts().iterator().next());
        flw.setJobStatus(SwcJobStatus.ACTIVE);
        swcService.add(flw);
        flw = swcService.getByContactNumber(callingNumber);
        assertNotNull(flw);
        transactionManager.commit(status);

        MotechEvent event = new MotechEvent();
        event.getParameters().put("callingNumber", callingNumber);
        event.getParameters().put("smsContent", "FooBar");
        CourseCompletionRecord ccr = new CourseCompletionRecord(callingNumber, 35, "score", false);
        courseCompletionRecordDataService.create(ccr);
        courseNotificationService.sendSmsNotification(event);
        // TODO: cannot check the notification status yet since we don't have a real IMI url to hit
    }

    @Test
    public void testNotificationNoLocation() {
        long callingNumber = 2111113333L;

        // Setup flw for notification (without language/location)
        Swachchagrahi flw = swcService.getByContactNumber(callingNumber);
        if (flw != null) {
            flw.setStatus(SwachchagrahiStatus.INVALID);
            swcService.update(flw);
            swcService.delete(flw);
        }

        flw = new Swachchagrahi("Test Worker", callingNumber);
        flw.setJobStatus(SwcJobStatus.ACTIVE);
        swcService.add(flw);
        flw = swcService.getByContactNumber(callingNumber);
        assertNotNull(flw);

        MotechEvent event = new MotechEvent();
        event.getParameters().put("callingNumber", callingNumber);
        event.getParameters().put("smsContent", "FooBar");
        CourseCompletionRecord ccr = new CourseCompletionRecord(callingNumber, 35, "score", false);
        courseCompletionRecordDataService.create(ccr);
        courseNotificationService.sendSmsNotification(event);
        // TODO: cannot check the notification status yet since we don't have a real IMI url to hit
    }

    @Test
    public void testSmsReference() {
        long callingNumber = 2111113333L;

        // Setup language/location and flw for notification
        Swachchagrahi flw = swcService.getByContactNumber(callingNumber);
        if (flw != null) {
            flw.setStatus(SwachchagrahiStatus.INVALID);
            swcService.update(flw);
            swcService.delete(flw);
        }

        createLanguageLocationData();
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        State sampleState = stateDataService.findByCode(1L);
        flw = new Swachchagrahi("Test Worker", callingNumber);
        flw.setState(sampleState);
        flw.setDistrict(sampleState.getDistricts().iterator().next());
        flw.setJobStatus(SwcJobStatus.ACTIVE);
        swcService.add(flw);
        flw = swcService.getByContactNumber(callingNumber);
        assertNotNull(flw);
        Long flwId = flw.getId();
        transactionManager.commit(status);

        MotechEvent event = new MotechEvent();
        event.getParameters().put("flwId", flwId);
        event.getParameters().put("smsContent", "FooBar");
        CourseCompletionRecord ccr = new CourseCompletionRecord(flwId, 35, "score", false);
        courseCompletionRecordDataService.create(ccr);
        assertNull(ccr.getSmsReferenceNumber());

        courseNotificationService.sendSmsNotification(event);
        CourseCompletionRecord smsCcr = courseCompletionRecordDataService.findByFlwId(flwId).get(0);
        assertNotNull(smsCcr.getSmsReferenceNumber());
        String expectedCode = "" + flw.getState().getCode() + flw.getDistrict().getCode() + callingNumber + 0; // location code + callingNumber + tries
        assertEquals(expectedCode, smsCcr.getSmsReferenceNumber());
    }

    @Test
    public void testMultipleCompletions() {
        long callingNumber = 9876543210L;
        Swachchagrahi flw = new Swachchagrahi(callingNumber);
        flw.setJobStatus(SwcJobStatus.ACTIVE);
        swcService.add(flw);
        flw = swcService.getByContactNumber(callingNumber);
        MaBookmark bookmark = new MaBookmark(flw.getId(), VALID_CALL_ID, null, null);
        maService.setBookmark(bookmark);
        List<Bookmark> added = bookmarkDataService.findBookmarksForUser(flw.getId().toString());
        assertTrue(added.size() == 1);

        bookmark.setBookmark(FINAL_BOOKMARK);
        Map<String, Integer> scores = new HashMap<>();
        for (int i = 1; i < 12; i++) {
            scores.put(String.valueOf(i), ((int) (Math.random() * 100)) % 5);
        }
        String chapterwiseScore = scores.toString();
        bookmark.setScoresByChapter(scores);
        maService.setBookmark(bookmark);

        bookmark.setBookmark(FINAL_BOOKMARK);
        Map<String, Integer> scores1 = new HashMap<>();
        for (int i = 1; i < 12; i++) {
            scores1.put(String.valueOf(i), 3);
        }
        String chapterwiseScore1 = scores1.toString();
        bookmark.setScoresByChapter(scores1);
        maService.setBookmark(bookmark);

        bookmark.setBookmark(FINAL_BOOKMARK);
        Map<String, Integer> scores2 = new HashMap<>();
        for (int i = 1; i < 12; i++) {
            scores2.put(String.valueOf(i), 1);
        }
        String chapterwiseScore2 = scores2.toString();
        bookmark.setScoresByChapter(scores2);
        maService.setBookmark(bookmark);

        Long flwId = swcService.getByContactNumber(callingNumber).getId();
        List<CourseCompletionRecord> ccrs = courseCompletionRecordDataService.findByFlwId(flwId);
        assertEquals(3, ccrs.size());
        assertEquals(4, activityDataService.findRecordsForUser((String.valueOf(callingNumber))).size());
        assertEquals(ActivityState.STARTED, activityDataService.findRecordsForUser((String.valueOf(callingNumber))).get(0).getState());
        assertEquals(ActivityState.COMPLETED, activityDataService.findRecordsForUser((String.valueOf(callingNumber))).get(1).getState());
        assertEquals(ActivityState.COMPLETED, activityDataService.findRecordsForUser((String.valueOf(callingNumber))).get(2).getState());
        assertEquals(ActivityState.COMPLETED, activityDataService.findRecordsForUser((String.valueOf(callingNumber))).get(3).getState());
        assertEquals(33, ccrs.get(1).getScore());
        assertEquals(11, ccrs.get(2).getScore());
        assertTrue(ccrs.get(1).isPassed());
        assertFalse(ccrs.get(2).isPassed());
        assertEquals(chapterwiseScore, ccrs.get(0).getChapterWiseScores());
        assertEquals(chapterwiseScore1, ccrs.get(1).getChapterWiseScores());
        assertEquals(chapterwiseScore2, ccrs.get(2).getChapterWiseScores());
    }

    private void createLanguageLocationData() {
        Language ta = languageService.getForCode("50");
        if (ta == null) {
            ta = languageDataService.create(new Language("50", "hin"));
        }

        State state = stateDataService.findByCode(1L);

        if (state == null) {
            District district = new District();
            district.setName("District 1");
            district.setRegionalName("District 1");
            district.setLanguage(ta);
            district.setCode(1L);

            state = new State();
            state.setName("State 1");
            state.setCode(1L);
            state.getDistricts().add(district);
            stateDataService.create(state);
        }
    }

    /**
     * setup MA course structure from nmsCourse.json file.
     */
    private JSONObject setupMaCourse() throws IOException {
        MaCourse course = new MaCourse();
        InputStream fileStream = getFileInputStream("nmsCourse.json");
        String jsonText = IOUtils.toString(fileStream);
        JSONObject jo = new JSONObject(jsonText);
        course.setName(jo.get("name").toString());
        course.setContent(jo.get("chapters").toString());
        nmsCourseDataService.create(new NmsCourse(course.getName(), course
                .getContent()));
        fileStream.close();
        return jo;
    }

    private InputStream getFileInputStream(String fileName) {
        return getClass().getClassLoader().getResourceAsStream(fileName);
    }

}