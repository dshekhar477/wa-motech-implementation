package org.motechproject.nms.api.utils;


import org.motechproject.nms.api.web.contract.mobileAcademy.CourseResponse;
import org.motechproject.nms.washacademy.dto.WaCourse;

/**
 * Helper to generate a course response
 */
public final class CourseBuilder {

    public static CourseResponse generateValidCourseResponse() {
        CourseResponse response = new CourseResponse();
        response.setName("MobileAcademyCourse");
        response.setCourseVersion(20150526L);
        response.setChapters("[]");
        return response;
    }

    public static WaCourse generateValidCourseDto() {
        WaCourse course = new WaCourse();
        course.setName("MobileAcademyCourse");
        course.setVersion(20150526L); // random, supposed to be millis eventually
        course.setContent("[{}]");
        return course;
    }
}
