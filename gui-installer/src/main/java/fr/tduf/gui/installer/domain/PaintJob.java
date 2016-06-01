package fr.tduf.gui.installer.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Exterior color set for a vehicle slot
 */
public class PaintJob {
    private int rank;

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

    public int getRank() {
        return rank;
    }

    public static class PaintJobBuilder {
        private int rank = 1;
        private List<String> interiorPatternRefs = new ArrayList<>();
        private Resource nameResource;

        private PaintJobBuilder() {}

        public PaintJobBuilder atRank(int index) {
            rank = index;
            return this;
        }

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
            paintJob.rank = rank;

            return paintJob;
        }
    }
}
