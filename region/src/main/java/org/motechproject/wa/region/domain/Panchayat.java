package org.motechproject.wa.region.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.annotations.Ignore;
import org.motechproject.mds.annotations.InstanceLifecycleListeners;
import org.motechproject.mds.domain.MdsEntity;
import org.motechproject.wa.region.domain.validation.ValidPanchayat;
import org.motechproject.wa.tracking.annotation.TrackClass;
import org.motechproject.wa.tracking.annotation.TrackFields;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Unique;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * This class has to hold both census and non-census villages.
 *
 * A Census village will have the following schema:
 * VCode int M Not null
 * Name_E varchar 50 M Not null English Name
 * Name_G nvarchar 50 M Not null Regional language name
 *
 * A Non-Census village will have the following schema:
 * SVID int M Not null Codes greater than 100000
 * VCode int O NULL If associated with any census village then code of that village
 * Name_E varchar 50 M Not null English Name
 * Name_G nvarchar 50 M Not null Regional language name
 *
 */
@ValidPanchayat
@Entity(tableName = "wash_panchayats")
@Unique(name = "UNIQUE_BLOCK_VCODE_SVID", members = { "block", "vcode", "svid" })
@TrackClass
@TrackFields
@InstanceLifecycleListeners
public class Panchayat extends MdsEntity {

    @Field
    @Column(allowsNull = "false", length = 250)
    @NotNull
    @Size(min = 1, max = 250)
    private String name;

    @Field
    @Column(length = 250)
    @Size(min = 1, max = 250)
    private String regionalName;

    @Field
    @NotNull
    @Min(0)
    // This is the village code for a census village.  One may be present for a non-census village if it is
    // associated with a census village
    private long vcode;

    @Field
    @NotNull
    @Min(0)
    // This is the village code for a non-census village.
    private long svid;

    @Field
    @Column(allowsNull = "false")
    @NotNull
    @JsonBackReference
    private Block block;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRegionalName() {
        return regionalName;
    }

    public void setRegionalName(String regionalName) {
        this.regionalName = regionalName;
    }

    /**
     * Returns the village code.  For a census village this will be the vcode provided by the MoH.
     * For a non-census village it will be the vcode if the village is associated with a census village.  If
     * it isn't then it will be the svid provided by the MoH
     *
     * @return vcode for a census village.  vcode or svid for a non-census village
     */
    @Ignore
    public long getPanchayatCode() {
        if (vcode != 0) {
            return vcode;
        }

        if (svid != 0) {
            return svid;
        }

        return 0;
    }

    public long getVcode() {
        return vcode;
    }

    public void setVcode(long vcode) {
        this.vcode = vcode;
    }

    public long getSvid() {
        return svid;
    }

    public void setSvid(long svid) {
        this.svid = svid;
    }

    public Block getBlock() {
        return block;
    }

    public void setBlock(Block block) {
        this.block = block;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Panchayat panchayat = (Panchayat) o;

        if (name != null ? !name.equals(panchayat.name) : panchayat.name != null) {
            return false;
        }
        if (vcode != 0 ? vcode != panchayat.vcode : panchayat.vcode != 0) {
            return false;
        }
        return !(svid != 0 ? svid != panchayat.svid : panchayat.svid != 0);

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + new Long(vcode).hashCode();
        result = 31 * result + new Long(svid).hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Panchayat{" +
                "name='" + name + '\'' +
                ", regionalName='" + regionalName + '\'' +
                ", vcode=" + vcode +
                ", svid=" + svid +
                '}';
    }
}
