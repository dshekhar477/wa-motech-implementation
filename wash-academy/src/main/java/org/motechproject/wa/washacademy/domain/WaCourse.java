package org.motechproject.wa.washacademy.domain;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.domain.MdsEntity;

import javax.jdo.annotations.Unique;

/**
 * Course entity to store the contents
 */
@Entity(tableName = "wash_course")
public class WaCourse extends MdsEntity {

    @Field
    @Unique
    private String name;

    @Field(type = "text")
    private String content;

    @Field
    @Unique
    private int courseId;

    @Field
    private int passingScore;

    @Field
    private int noOfChapters;



//    @JsonManagedReference
//    @Persistent(mappedBy = "waCourse", defaultFetchGroup = "false")
//    @Cascade(delete = true)
//    @Order(extensions = @Extension(vendorName = "datanucleus", key = "list-ordering", value = "id ASC"))


    public WaCourse() {
    }

    public WaCourse(String name, String content) {
        this.name = name;
        this.content = content;
    }
    public int getPassingScore() {    return passingScore;    }

    public void setPassingScore(int passingScore) {       this.passingScore = passingScore;    }

    public int getNoOfChapters() {   return noOfChapters;  }

    public void setNoOfChapters(int noOfChapters) {     this.noOfChapters = noOfChapters;   }

    public int getCourseId() {     return courseId;    }

    public void setCourseId(int courseId) {   this.courseId = courseId;    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
