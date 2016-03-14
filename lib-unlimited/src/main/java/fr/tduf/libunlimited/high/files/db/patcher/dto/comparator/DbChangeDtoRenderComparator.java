package fr.tduf.libunlimited.high.files.db.patcher.dto.comparator;

import com.google.common.collect.ComparisonChain;
import fr.tduf.libunlimited.high.files.db.patcher.dto.DbPatchDto;

import java.util.Comparator;

// TODO order by filter values and values
public class DbChangeDtoRenderComparator implements Comparator<DbPatchDto.DbChangeDto> {
    @Override
    public int compare(DbPatchDto.DbChangeDto o1, DbPatchDto.DbChangeDto o2) {

        return ComparisonChain.start()
                .compare(o1.getTopic().name(), o2.getTopic().name())
                .compare(o1.getType().getRenderPriority(), o2.getType().getRenderPriority())
                .compare(o1.getRef(), o2.getRef(), (ref1, ref2) -> {
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
                })
                .compare(o1.getLocale(), o2.getLocale(), (locale1, locale2) -> {
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
                })
                .result();
    }
}
