package fr.tduf.libunlimited.low.files.bin.cameras.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonIgnore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;

/**
 * Brings all information about a single camera (view set)
 */
public class CameraSetInfo {
    private int cameraIdentifier;
    private List<CameraView> views;

    private CameraSetInfo() {}

    public static CameraInfoBuilder builder() {
        return new CameraInfoBuilder();
    }

    public int getCameraIdentifier() {
        return cameraIdentifier;
    }

    public List<CameraView> getViews() {
        return views;
    }

    @JsonIgnore
    public Map<ViewKind, CameraView> getViewsByKind() {
        return getViews().stream()
                .collect(toMap(CameraView::getKind, v -> v));
    }

    @Override
    public boolean equals(Object o) {
        return reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("cameraIdentifier", cameraIdentifier)
                .toString();
    }

    public static class CameraInfoBuilder {
        private int cameraIdentifier;
        private List<CameraView> views = new ArrayList<>();

        public CameraInfoBuilder forIdentifier(int cameraIdentifier) {
            this.cameraIdentifier = cameraIdentifier;
            return this;
        }

        public CameraInfoBuilder addView(CameraView view) {
            views.add(view);
            return this;
        }

        public CameraInfoBuilder withUsedViews(List<CameraView> allViews, List<CameraView> usedViews) {
            views.addAll(allViews);
            views
                    .forEach(view -> usedViews.stream()
                            .filter(usedView -> usedView.getKind() == view.getKind())
                            .filter(usedView -> usedView.getUsedCameraSetId() != 0)
                            .findAny()
                            .ifPresent(view::setUsedSettings));
            return this;
        }

        public CameraSetInfo build() {
            final CameraSetInfo cameraSetInfo = new CameraSetInfo();

            cameraSetInfo.cameraIdentifier = cameraIdentifier;
            cameraSetInfo.views = views;

            return cameraSetInfo;
        }
    }
}
