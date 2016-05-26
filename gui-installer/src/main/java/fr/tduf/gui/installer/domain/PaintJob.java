package fr.tduf.gui.installer.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Exterior color set for a vehicle slot
 */
public class PaintJob {
    private Resource name;

    private List<String> interiorPatternRefs;

    private PaintJob() {}

    public static PaintJobBuilder builder() {
        return new PaintJobBuilder();
    }

    public Resource getName() {
        return name;
    }

    public List<String> getInteriorPatternRefs() {
        return interiorPatternRefs;
    }

    public static class PaintJobBuilder {
        private List<String> interiorPatternRefs = new ArrayList<>();
        private Resource nameResource;

        private PaintJobBuilder() {}

        public PaintJobBuilder addInteriorPattern(String interiorPatternReference) {
            interiorPatternRefs.add(interiorPatternReference);
            return this;
        }

        public PaintJobBuilder withName(Resource nameResource) {
            this.nameResource = nameResource;
            return this;
        }

        public PaintJob build() {
            PaintJob paintJob = new PaintJob();

            paintJob.name = nameResource;
            paintJob.interiorPatternRefs = interiorPatternRefs;

            return paintJob;
        }
    }
}
