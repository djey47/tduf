package fr.tduf.libunlimited.high.files.db.patcher.domain;

import java.util.Optional;
import java.util.Properties;

import static java.util.Objects.requireNonNull;

/**
 * Stores all information to resolve eventual placeholders in change objects
 */
public class PatchProperties extends Properties {

    private static final String PLACEHOLDER_NAME_SLOT_REFERENCE = "SLOTREF";
    private static final String PLACEHOLDER_NAME_ID_CAR = "CARID";
    private static final String PLACEHOLDER_NAME_RESOURCE_BANK = "RES_BANKNAME";
    private static final String PLACEHOLDER_NAME_RESOURCE_MODEL = "RES_MODELNAME";
    private static final String PLACEHOLDER_NAME_RESOURCE_VERSION = "RES_VERSIONNAME";
    private static final String PLACEHOLDER_NAME_FMT_RIMS_REFERENCE = "RIMREF.%d";
    private static final String PLACEHOLDER_NAME_FMT_INTERIOR_REFERENCE = "INTREF.%d";
    private static final String PLACEHOLDER_NAME_FMT_RESOURCE_RIM_BANK = "RES_BANKNAME.%s.%d";
    private static final String PLACEHOLDER_NAME_FMT_RESOURCE_INTERIOR = "RES_INTNAME.%d";
    private static final String PLACEHOLDER_NAME_FMT_RESOURCE_COLOR = "RES_COLORNAME.%d";
    private static final String SUFFIX_FRONT_RIMS = "FR";
    private static final String SUFFIX_REAR_RIMS = "RR";

    /**
     * Stores value associated to a placeholder.
     */
    public void register(String placeholder, String value) {
        setProperty(
                requireNonNull(placeholder, "Placeholder is required."),
                requireNonNull(value, "Value is required")
        );
    }

    /**
     * @return value associated tyo requested placeholder if it exists, empty otherwise.
     */
    public Optional<String> retrieve(String placeholder) {
        requireNonNull(placeholder, "Placeholder is required.");

        return Optional.ofNullable(getProperty(placeholder));
    }

    /**
     * @return a deep copy of this property store.
     */
    public PatchProperties makeCopy() {
        final PatchProperties patchProperties = new PatchProperties();

        entrySet().forEach((property) -> {
            final String placeholder = (String) property.getKey();
            final String value = (String) property.getValue();

            patchProperties.register(placeholder, value);
        });

        return patchProperties;
    }

    public void setCarIdentifierIfNotExists(String carIdentifier) {
        registerIfNotExists(PLACEHOLDER_NAME_ID_CAR, carIdentifier);
    }

    public void setVehicleSlotReferenceIfNotExists(String slotReference) {
        registerIfNotExists(PLACEHOLDER_NAME_SLOT_REFERENCE, slotReference);
    }

    public void setRimsSlotReferenceIfNotExists(String rimsSlotReference, int rimSet) {
        String placeholderName = String.format(PLACEHOLDER_NAME_FMT_RIMS_REFERENCE, rimSet);
        registerIfNotExists(placeholderName, rimsSlotReference);
    }

    public void setInteriorReferenceIfNotExists(String interiorReference, int interiorSet) {
        String placeholderName = String.format(PLACEHOLDER_NAME_FMT_INTERIOR_REFERENCE, interiorSet);
        registerIfNotExists(placeholderName, interiorReference);
    }

    public void setResourceModelBankIfNotExists(String modelBankReference) {
        registerIfNotExists(PLACEHOLDER_NAME_RESOURCE_BANK, modelBankReference);
    }

    public void setResourceFrontRimBankIfNotExists(String rimBankReference, int rimSet) {
        String placeholderName = String.format(PLACEHOLDER_NAME_FMT_RESOURCE_RIM_BANK, SUFFIX_FRONT_RIMS, rimSet);
        registerIfNotExists(placeholderName, rimBankReference);
    }

    public void setResourceRearRimBankIfNotExists(String rimBankReference, int rimSet) {
        String placeholderName = String.format(PLACEHOLDER_NAME_FMT_RESOURCE_RIM_BANK, SUFFIX_REAR_RIMS, rimSet);
        registerIfNotExists(placeholderName, rimBankReference);
    }

    public void setResourceModelNameIfNotExists(String modelNameReference) {
        registerIfNotExists(PLACEHOLDER_NAME_RESOURCE_MODEL, modelNameReference);
    }

    public void setResourceVersionNameIfNotExists(String versionNameReference) {
        registerIfNotExists(PLACEHOLDER_NAME_RESOURCE_VERSION, versionNameReference);
    }

    public void setResourceInteriorNameIfNotExists(String interiorNameReference, int interiorSet) {
        String placeholderName = String.format(PLACEHOLDER_NAME_FMT_RESOURCE_INTERIOR, interiorSet);
        registerIfNotExists(placeholderName, interiorNameReference);
    }

    public void setResourceColorNameIfNotExists(String colorNameReference, int exteriorSet) {
        String placeholderName = String.format(PLACEHOLDER_NAME_FMT_RESOURCE_COLOR, exteriorSet);
        registerIfNotExists(placeholderName, colorNameReference);
    }

    private void registerIfNotExists(String placeholder, String value) {
        if (retrieve(placeholder).isPresent()) {
            return;
        }

        register(placeholder, value);
    }

    public Optional<String> getVehicleSlotReference() {
        return retrieve(PLACEHOLDER_NAME_SLOT_REFERENCE);
    }
}
