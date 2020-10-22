package fr.tduf.libunlimited.low.files.gfx.materials.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fr.tduf.libunlimited.low.files.research.domain.DataStore;

import java.util.List;

/**
 * Represents all material definitions
 */
public class MaterialDefs {
    @JsonIgnore
    protected DataStore originalDataStore;

    protected long fileSize;

    protected List<Material> materials;
    protected List<AdditionalSetting> additionalSettings;
    private MaterialDefs() {}

    public static MaterialDefsBuilder builder() {
        return new MaterialDefsBuilder();
    }

    public long getFileSize() {
        return fileSize;
    }

    public DataStore getOriginalDataStore() {
        return originalDataStore;
    }

    public List<Material> getMaterials() {
        return materials;
    }

    public List<AdditionalSetting> getAdditionalSettings() {
        return additionalSettings;
    }

    public static class MaterialDefsBuilder extends MaterialDefs {
        public MaterialDefsBuilder fromDatastore(DataStore originalDataStore) {
            this.originalDataStore = originalDataStore;
            return this;
        }

        public MaterialDefsBuilder withMaterials(List<Material> materials) {
            this.materials = materials;
            return this;
        }

        public MaterialDefsBuilder withAdditionalSettings(List<AdditionalSetting> additionalSettings) {
            this.additionalSettings = additionalSettings;
            return this;
        }

        public  MaterialDefsBuilder withFileSize(long fileSize) {
            this.fileSize = fileSize;
            return this;
        }


        public MaterialDefs build() {
            MaterialDefs materialDefs = new MaterialDefs();
            materialDefs.originalDataStore = originalDataStore;
            materialDefs.materials = materials;
            materialDefs.additionalSettings = additionalSettings;
            materialDefs.fileSize = fileSize;
            return materialDefs;
        }
    }
}
