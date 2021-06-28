package com.finlib.finutils.tests;

import com.finlib.finutils.FrequencyType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class FrequencyTypeTest {
    @Test
    void testFrequencyTypes(){
        assertEquals(1, FrequencyType.ANNUAL.getFrequency());
        assertEquals(12, FrequencyType.MONTHLY.getFrequency());
    }

}