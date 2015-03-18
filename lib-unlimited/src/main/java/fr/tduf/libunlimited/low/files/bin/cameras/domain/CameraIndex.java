package fr.tduf.libunlimited.low.files.bin.cameras.domain;

/**
 *
 */
public class CameraIndex {

    private long cameraId;

    private short viewCount;

    public CameraIndex(long cameraId, short viewCount) {
        this.cameraId = cameraId;
        this.viewCount = viewCount;
    }
}
