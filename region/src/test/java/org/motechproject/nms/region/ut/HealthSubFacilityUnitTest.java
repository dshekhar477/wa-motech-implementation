package org.motechproject.nms.region.ut;

import org.junit.Before;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * Created by rob on 5/4/15.
 */
public class HealthSubFacilityUnitTest {
    Validator validator;

    @Before
    public void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testNameNull() {
        HealthSubFacility healthSubFacility = new HealthSubFacility();

        Set<ConstraintViolation<HealthSubFacility>> constraintViolations = validator
                .validateProperty(healthSubFacility, "name");

        assertEquals(1, constraintViolations.size());
        assertEquals("may not be null", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void testNameSize() {
        HealthSubFacility healthSubFacility = new HealthSubFacility();
        healthSubFacility
                .setName("AAAAAAAAAABBBBBBBBBBAAAAAAAAAABBBBBBBBBBAAAAAAAAAABBBBBBBBBBAAAAAAAAAABBBBBBBBBBAAAAAAAAAABBBBBBBBBBXAAAAAAAAAABBBBBBBBBBAAAAAAAAAABBBBBBBBBBAAAAAAAAAABBBBBBBBBBAAAAAAAAAABBBBBBBBBBAAAAAAAAAABBBBBBBBBBXJGDJFGEJWGFWEJHGFJWEHGFJWEGFJHEGfdewfwefwefeweeee");

        Set<ConstraintViolation<HealthSubFacility>> constraintViolations = validator
                .validateProperty(healthSubFacility, "name");

        assertEquals(1, constraintViolations.size());
        assertEquals("size must be between 1 and 250", constraintViolations.iterator().next().getMessage());

        healthSubFacility.setName("");

        constraintViolations = validator.validateProperty(healthSubFacility, "name");

        assertEquals(1, constraintViolations.size());
        assertEquals("size must be between 1 and 250", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void testRegionalNameSize() {
        HealthSubFacility healthSubFacility = new HealthSubFacility();
        healthSubFacility
                .setRegionalName("AAAAAAAAAABBBBBBBBBBAAAAAAAAAABBBBBBBBBBAAAAAAAAAABBBBBBBBBBAAAAAAAAAABBBBBBBBBBAAAAAAAAAABBBBBBBBBBXAAAAAAAAAABBBBBBBBBBAAAAAAAAAABBBBBBBBBBAAAAAAAAAABBBBBBBBBBAAAAAAAAAABBBBBBBBBBAAAAAAAAAABBBBBBBBBBXJGDJFGEJWGFWEJHGFJWEHGFJWEGFJHEGfdewfwefwefeweeee");

        Set<ConstraintViolation<HealthSubFacility>> constraintViolations = validator
                .validateProperty(healthSubFacility, "regionalName");

        assertEquals(1, constraintViolations.size());
        assertEquals("size must be between 1 and 250", constraintViolations.iterator().next().getMessage());

        healthSubFacility.setRegionalName("");

        constraintViolations = validator.validateProperty(healthSubFacility, "regionalName");

        assertEquals(1, constraintViolations.size());
        assertEquals("size must be between 1 and 250", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void testCodeNull() {
        HealthSubFacility healthSubFacility = new HealthSubFacility();

        Set<ConstraintViolation<HealthSubFacility>> constraintViolations = validator
                .validateProperty(healthSubFacility, "code");

        assertEquals(1, constraintViolations.size());
        assertEquals("may not be null", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void testHealthFacilityNull() {
        HealthSubFacility healthSubFacility = new HealthSubFacility();

        Set<ConstraintViolation<HealthSubFacility>> constraintViolations = validator
                .validateProperty(healthSubFacility, "healthFacility");

        assertEquals(1, constraintViolations.size());
        assertEquals("may not be null", constraintViolations.iterator().next().getMessage());
    }}
