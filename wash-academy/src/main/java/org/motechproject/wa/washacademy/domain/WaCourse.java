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

    public WaCourse() {
    }

    public WaCourse(String name, String content) {
        this.name = name;
        this.content = content;
    }

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
