package com.example.saasfile.common.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BooleanValidatorTest {

    @Test
    void acceptsTrueAndFalseIgnoringCase() {
        assertTrue(BooleanValidator.isBooleanString("true"));
        assertTrue(BooleanValidator.isBooleanString("false"));
        assertTrue(BooleanValidator.isBooleanString("TRUE"));
        assertTrue(BooleanValidator.isBooleanString("False"));
    }

    @Test
    void rejectsNullEmptyAndNonBooleanValues() {
        assertFalse(BooleanValidator.isBooleanString(null));
        assertFalse(BooleanValidator.isBooleanString(""));
        assertFalse(BooleanValidator.isBooleanString("yes"));
        assertFalse(BooleanValidator.isBooleanString(" true "));
    }
}
