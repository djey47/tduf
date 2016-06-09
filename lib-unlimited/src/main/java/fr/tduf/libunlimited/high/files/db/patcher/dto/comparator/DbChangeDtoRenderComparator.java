package fr.tduf.libunlimited.high.files.db.patcher.dto.comparator;

import fr.tduf.libunlimited.common.game.domain.Locale;
import fr.tduf.libunlimited.framework.collect.ComparisonChain;
import fr.tduf.libunlimited.high.files.db.dto.DbFieldValueDto;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;

import java.util.Comparator;
import java.util.List;

/**
 * Specifies comparison rules for change objects.
 */
public class DbChangeDtoRenderComparator implements Comparator<DbPatchDto.DbChangeDto> {
    @Override
    public int compare(DbPatchDto.DbChangeDto o1, DbPatchDto.DbChangeDto o2) {

        return ComparisonChain.start()
                .compare(o1.getTopic().name(), o2.getTopic().name())
                .compare(o1.getType().getRenderPriority(), o2.getType().getRenderPriority())
                .compare(o1.getRef(), o2.getRef(), getReferencesComparator())
                .compare(o1.getLocale(), o2.getLocale(), getLocalesComparator())
                .compare(o1.getFilterCompounds(), o2.getFilterCompounds(), getFiltersComparator())
                .compare(o1.getValues(), o2.getValues(), getValuesComparator())
                .result();
    }

    private static Comparator<List<String>> getValuesComparator() {
        return (values1, values2) -> {
            // Limits to first value
            if (values1 == null && values2 == null) {
                return 0;
            }

            if (values1 == null) {
                return -1;
            }

            if (values2 == null) {
                return 1;
            }

            if (values1.isEmpty() && values2.isEmpty()) {
                return 0;
            }

            if (values1.isEmpty()) {
                return -1;
            }

            if (values2.isEmpty()) {
                return 1;
            }

            return values1.get(0).compareTo(values2.get(0));
        };
    }

    private static Comparator<List<DbFieldValueDto>> getFiltersComparator() {
        return (filter1, filter2) -> {
            // Limits to first compound
            if (filter1 == null && filter2 == null) {
                return 0;
            }

            if (filter1 == null) {
                return -1;
            }

            if (filter2 == null) {
                return 1;
            }

            if (filter1.isEmpty() && filter2.isEmpty()) {
                return 0;
            }

            if (filter1.isEmpty()) {
                return -1;
            }

            if (filter2.isEmpty()) {
                return 1;
            }

            return filter1.get(0).getValue().compareTo(filter2.get(0).getValue());
        };
    }

    private static Comparator<Locale> getLocalesComparator() {
        return (locale1, locale2) -> {
            if (locale1 == null && locale2 == null) {
                return 0;
            }

            if (locale1 == null) {
                return -1;
            }

            if (locale2 == null) {
                return 1;
            }

            return locale1.name().compareTo(locale2.name());
        };
    }

    private static Comparator<String> getReferencesComparator() {
        return (ref1, ref2) -> {
            if (ref1 == null && ref2 == null) {
                return 0;
            }

            if (ref1 == null) {
                return -1;
            }

            if (ref2 == null) {
                return 1;
            }

            return Long.valueOf(ref1).compareTo(Long.valueOf(ref2));
        };
    }
}
