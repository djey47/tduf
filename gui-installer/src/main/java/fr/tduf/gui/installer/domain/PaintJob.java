package fr.tduf.gui.installer.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Exterior color set for a vehicle slot
 */
public class PaintJob {
    private int rank;

    private Resource name;
    private Resource mainColor;
    private Resource secondaryColor;
    private Resource calipersColor;

    private long priceDollar;

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

    public Resource getMainColor() {
        return mainColor;
    }

    public Resource getSecondaryColor() {
        return secondaryColor;
    }

    public Resource getCalipersColor() {
        return calipersColor;
    }

    public long getPriceDollar() {
        return priceDollar;
    }

    public static class PaintJobBuilder {
        private int rank = 1;
        private List<String> interiorPatternRefs = new ArrayList<>(15);
        private Resource nameResource;
        private Resource mainColorResource;
        private Resource secondaryColorResource;
        private Resource calipersColorResource;
        private long priceDollar;

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

        public PaintJobBuilder withColors(Resource mainResource, Resource secondaryResource, Resource calipersResource) {
            this.mainColorResource = mainResource;
            this.secondaryColorResource = secondaryResource;
            this.calipersColorResource = calipersResource;
            return this;
        }

        public PaintJobBuilder withPrice(long priceDollar) {
            this.priceDollar = priceDollar;
            return this;
        }

        public PaintJob build() {
            PaintJob paintJob = new PaintJob();

            paintJob.name = nameResource;
            paintJob.interiorPatternRefs = interiorPatternRefs;
            paintJob.rank = rank;
            paintJob.mainColor = mainColorResource;
            paintJob.secondaryColor = secondaryColorResource;
            paintJob.calipersColor = calipersColorResource;
            paintJob.priceDollar = priceDollar;

            return paintJob;
        }
    }
}
