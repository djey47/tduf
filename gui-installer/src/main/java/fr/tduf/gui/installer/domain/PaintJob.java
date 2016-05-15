package fr.tduf.gui.installer.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Exterior color set for a vehicle slot
 */
public class PaintJob {
    private List<String> interiorPatternRefs;

    private PaintJob() {}

    public static PaintJobBuilder builder() {
        return new PaintJobBuilder();
    }

    public List<String> getInteriorPatternRefs() {
        return interiorPatternRefs;
    }

    public static class PaintJobBuilder {
        private List<String> interiorPatternRefs = new ArrayList<>();

        private PaintJobBuilder() {}

        public PaintJobBuilder addInteriorPattern(String interiorPatternReference) {
            interiorPatternRefs.add(interiorPatternReference);
            return this;
        }

        public PaintJob build() {
            PaintJob paintJob = new PaintJob();

            paintJob.interiorPatternRefs = interiorPatternRefs;

            return paintJob;
        }
    }
}
