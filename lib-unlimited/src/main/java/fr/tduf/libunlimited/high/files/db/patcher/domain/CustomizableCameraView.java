package fr.tduf.libunlimited.high.files.db.patcher.domain;

import fr.tduf.libunlimited.high.files.bin.cameras.interop.dto.GenuineCamViewsDto;

import java.util.stream.Stream;

import static fr.tduf.libunlimited.high.files.bin.cameras.interop.dto.GenuineCamViewsDto.GenuineCamViewDto.Type.*;

/** All customizable view kinds **/
public enum CustomizableCameraView {
    BUMPER("BUMPER", Bumper),
    BUMPER_BACK("BUMPERBACK", Bumper_Back),
    HOOD("HOOD", Hood),
    HOOD_BACK("HOODBACK", Hood_Back),
    COCKPIT("COCKPIT", Cockpit),
    COCKPIT_BACK("COCKPITBACK", Cockpit_Back),
    FOLLOW_LARGE("FOLLOWLARGE", Follow_Large),
    FOLLOW_LARGE_BACK("FOLLOWLARGEBACK", Follow_Large_Back),
    FOLLOW_NEAR("FOLLOWNEAR", Follow_Near),
    FOLLOW_NEAR_BACK("FOLLOWNEARBACK", Follow_Near_Back),
    FOLLOW_FAR("FOLLOWFAR", Follow_Far),
    FOLLOW_FAR_BACK("FOLLOWFARBACK", Follow_Far_Back);

    private final String propertySuffix;
    private final GenuineCamViewsDto.GenuineCamViewDto.Type genuineViewType;

    CustomizableCameraView(String propertySuffix, GenuineCamViewsDto.GenuineCamViewDto.Type genuineViewType) {
        this.propertySuffix = propertySuffix;
        this.genuineViewType = genuineViewType;
    }

    public static CustomizableCameraView fromSuffix(String code) {
        try ( Stream<CustomizableCameraView> customizableViewStream = Stream.of(CustomizableCameraView.values()) ) {
            return customizableViewStream
                    .filter(view -> view.propertySuffix.equalsIgnoreCase(code))
                    .findAny()
                    .orElseThrow(() -> new IllegalArgumentException("Unknown view code: " + code));
        }
    }

    public GenuineCamViewsDto.GenuineCamViewDto.Type getGenuineViewType() {
        return genuineViewType;
    }

    public String getPropertySuffix() {
        return propertySuffix;
    }
}
