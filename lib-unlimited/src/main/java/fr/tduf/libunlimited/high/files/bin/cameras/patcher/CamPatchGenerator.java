package fr.tduf.libunlimited.high.files.bin.cameras.patcher;

import fr.tduf.libunlimited.high.files.bin.cameras.patcher.dto.CamPatchDto;
import fr.tduf.libunlimited.high.files.bin.cameras.patcher.dto.SetChangeDto;
import fr.tduf.libunlimited.high.files.bin.cameras.patcher.dto.ViewChangeDto;
import fr.tduf.libunlimited.high.files.db.patcher.domain.ItemRange;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.CameraInfo;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.CameraViewEnhanced;
import fr.tduf.libunlimited.low.files.bin.cameras.domain.ViewProps;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.*;

/**
 * Creates cameras patches from loaded cameras
 */
public class CamPatchGenerator {

    private final List<CameraInfo> camerasInformation;

    // TODO use enhanced object
    public CamPatchGenerator(List<CameraInfo> camerasInformation) {
        this.camerasInformation = requireNonNull(camerasInformation, "Loaded cameras information is required");
    }

    /**
     * Generates a patch based on current cameras info.
     * Will export all view properties.
     *
     * @param identifierRange   : range of camera set identifier values
     * @param viewRange         : range of views to be exported
     * @return a patch object with all necessary instructions.
     */
    public CamPatchDto makePatch(ItemRange identifierRange, ItemRange viewRange) {
        requireNonNull(identifierRange, "An identifier range is required.");
        requireNonNull(viewRange, "An view range is required.");

        String currentDateTime = LocalDateTime.now().format(ISO_LOCAL_DATE_TIME);
        return CamPatchDto.builder()
                .withComment("Camera patch built on " + currentDateTime)
                .addChanges(makeChangesObjectsForSetsWithIdentifiers(identifierRange, viewRange))
                .build();
    }

    private Collection<SetChangeDto> makeChangesObjectsForSetsWithIdentifiers(ItemRange identifierRange, ItemRange viewRange) {
        return camerasInformation.stream()
                .filter(setEntry -> isInIdRange(setEntry, identifierRange))
                .map(cameraInfo -> makeChangeObjectForEntry(cameraInfo, viewRange))
                .collect(toList());
    }

    private SetChangeDto makeChangeObjectForEntry(CameraInfo cameraInfo, ItemRange viewRange) {
        return SetChangeDto.builder()
                .withSetIdentifier(cameraInfo.getCameraIdentifier())
                .addChanges(makeViewChangeObjectsForSet(cameraInfo, viewRange))
                .build();
    }

    private Collection<ViewChangeDto> makeViewChangeObjectsForSet(CameraInfo cameraInfo, ItemRange viewRange) {
        return cameraInfo.getViews().stream()
                .filter(view -> isInViewRange(view, viewRange))
                .map(this::makeViewChangeObject)
                .collect(toList());
    }

    private ViewChangeDto makeViewChangeObject(CameraViewEnhanced cameraView) {
        return ViewChangeDto.builder()
                .forViewKind(cameraView.getKind())
                .withProps(getAllViewProperties(cameraView))
                .build();
    }

    private EnumMap<ViewProps, String> getAllViewProperties(CameraViewEnhanced cameraView) {
        return cameraView.getSettings().entrySet().stream()
                .collect(
                        collectingAndThen(
                                toMap(Map.Entry::getKey, entry -> entry.getValue().toString()),
                                EnumMap::new));
    }

    private boolean isInIdRange(CameraInfo setEntry, ItemRange identifierRange) {
        return identifierRange.accepts(Long.toString(setEntry.getCameraIdentifier()));
    }

    private boolean isInViewRange(CameraViewEnhanced view, ItemRange viewNameRange) {
        return viewNameRange.accepts(view.getKind().name());
    }
}
