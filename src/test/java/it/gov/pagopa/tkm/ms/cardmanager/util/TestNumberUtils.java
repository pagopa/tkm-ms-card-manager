package it.gov.pagopa.tkm.ms.cardmanager.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestNumberUtils {

    @Test
    void splitNumberToRange_fullRange() {
        int partition = 5;
        List<NumberRange> numberRanges = NumberUtils.splitNumberToRange(0, 100, partition);
        assertEquals(partition, numberRanges.size());
        assertEquals(NumberRange.builder().minIncluded(0).maxExcluded(20).build(), numberRanges.get(0));
        assertEquals(NumberRange.builder().minIncluded(20).maxExcluded(40).build(), numberRanges.get(1));
        assertEquals(NumberRange.builder().minIncluded(40).maxExcluded(60).build(), numberRanges.get(2));
        assertEquals(NumberRange.builder().minIncluded(60).maxExcluded(80).build(), numberRanges.get(3));
        assertEquals(NumberRange.builder().minIncluded(80).maxExcluded(101).build(), numberRanges.get(4));
    }

    @Test
    void splitNumberToRange_incompleteRange() {
        int partition = 3;
        List<NumberRange> numberRanges = NumberUtils.splitNumberToRange(10, 50, partition);
        assertEquals(partition, numberRanges.size());
        assertEquals(NumberRange.builder().minIncluded(10).maxExcluded(24).build(), numberRanges.get(0));
        assertEquals(NumberRange.builder().minIncluded(24).maxExcluded(38).build(), numberRanges.get(1));
        assertEquals(NumberRange.builder().minIncluded(38).maxExcluded(51).build(), numberRanges.get(2));
    }

    @Test
    void splitNumberToRange_single() {
        List<NumberRange> numberRanges = NumberUtils.splitNumberToRange(10, 10, 2);
        assertEquals(1, numberRanges.size());
        assertEquals(NumberRange.builder().minIncluded(10).maxExcluded(11).build(), numberRanges.get(0));
    }

    @Test
    void splitNumberToRange_invalidRange() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> NumberUtils.splitNumberToRange(11, 10, 2));
    }

    @Test
    void splitNumberToRange_invalidPartitionValue() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> NumberUtils.splitNumberToRange(11, 20, 0));
        Assertions.assertThrows(IllegalArgumentException.class, () -> NumberUtils.splitNumberToRange(11, 20, -1));
    }
}