package fr.tduf.libunlimited.low.files.gfx.materials.domain;

public class AdditionalSetting {
    protected String name;
    protected byte[] data;

    public static AdditionalSettingBuilder builder() {
        return new AdditionalSettingBuilder();
    }

    public String getName() {
        return name;
    }

    public byte[] getData() {
        return data;
    }

    public static class AdditionalSettingBuilder extends AdditionalSetting {
        public AdditionalSettingBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public AdditionalSettingBuilder withData(byte[] data) {
            this.data = data;
            return this;
        }

        public AdditionalSetting build() {
            AdditionalSetting additionalSetting = new AdditionalSetting();
            additionalSetting.name = name;
            additionalSetting.data = data;
            return additionalSetting;
        }
    }
}
