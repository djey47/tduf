package fr.tduf.libunlimited.low.files.bin.cameras.helper;

import fr.tduf.libunlimited.low.files.bin.cameras.rw.CamerasParser;

import static java.util.Objects.requireNonNull;

public class CamerasHelper {

    /**
     *
     * @param sourceCameraId
     * @param targetCameraId
     * @param parser
     */
    public static void duplicateCameraSet(long sourceCameraId, long targetCameraId, CamerasParser parser) {
        requireNonNull(parser, "Parser with cameras contents is required.");

        int currentIndexEntryCount = parser.getCameraIndex().size();
        short viewCount = parser.getCameraIndex().get(sourceCameraId);
        parser.getDataStore().addRepeatedIntegerValue("index", "cameraId", currentIndexEntryCount, targetCameraId);
        parser.getDataStore().addRepeatedIntegerValue("index", "viewCount", currentIndexEntryCount, viewCount);

        int currentViewsCount = parser.getCameraViews().values().size();

        // TODO copy all sets
//        parser.getCameraViews().get(sourceCameraId).stream()
//
//                .forEach( (viewStore) -> {
//                            parser.getDataStore().
//                        }
//                );
    }
}
