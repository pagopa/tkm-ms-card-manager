package it.gov.pagopa.tkm.ms.cardmanager.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NumberUtils {

    public static List<NumberRange> splitNumberToRange(long min, long max, int wantedPartition) throws IllegalArgumentException {
        checkInputSplitNumberToRange(min, max, wantedPartition);

        List<NumberRange> numberRanges = new ArrayList<>();
        long diff = max - min;
        long interval = (long) Math.ceil(diff / (double) wantedPartition);
        long newMin = min;
        long newMax;
        do {
            newMax = newMin + interval;
            if (newMax >= max)
                newMax = max + 1;

            NumberRange numberRange = NumberRange.builder().minIncluded(newMin).maxExcluded(newMax).build();
            numberRanges.add(numberRange);

            newMin = newMax;
        } while (newMin < max);

        return numberRanges;
    }

    private static void checkInputSplitNumberToRange(long min, long max, int wantedPartition) {
        if (min > max) {
            throw new IllegalArgumentException("Min must be less than max");
        } else if (wantedPartition <= 0) {
            throw new IllegalArgumentException("wantedPartition must be grater than 0");
        }
    }
}
