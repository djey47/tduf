package fr.tduf.libunlimited.high.files.db.patcher.domain;

import java.util.Optional;
import java.util.Properties;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * Stores all information to resolve eventual placeholders in change objects
 */
public class PatchProperties extends Properties {

    private static final String PLACEHOLDER_NAME_SLOT_REFERENCE = "SLOTREF";
    private static final String PLACEHOLDER_NAME_ID_CAR = "CARID";
    private static final String PLACEHOLDER_NAME_BANK = "BANKNAME";
    private static final String PLACEHOLDER_NAME_RESOURCE_BANK = "RES_BANKNAME";
    private static final String PLACEHOLDER_NAME_RESOURCE_MODEL = "RES_MODELNAME";
    private static final String PLACEHOLDER_NAME_RESOURCE_VERSION = "RES_VERSIONNAME";
    private static final String PLACEHOLDER_NAME_DEALER_REFERENCE = "DEALERREF";
    private static final String PLACEHOLDER_NAME_DEALER_SLOT = "DEALERSLOT";

    private static final String PLACEHOLDER_NAME_FMT_RIMS_REFERENCE = "RIMREF.%d";
    private static final String PLACEHOLDER_NAME_FMT_RESOURCE_RIMS_BRAND = "RIMBRANDREF.%d";
    private static final String PLACEHOLDER_NAME_FMT_INTERIOR_REFERENCE = "INTREF.%d";
    private static final String PLACEHOLDER_NAME_FMT_RIMS_BANK = "BANKNAME.%s.%d";
    private static final String PLACEHOLDER_NAME_FMT_RESOURCE_RIM_BANK = "RES_BANKNAME.%s.%d";
    private static final String PLACEHOLDER_NAME_FMT_RESOURCE_COLOR = "RES_COLORNAME.%d";

    private static final String SUFFIX_FRONT_RIMS = "FR";
    private static final String SUFFIX_REAR_RIMS = "RR";

    /**
     * @return true if provided placeholder matches one used for ID_CAR
     */
    public static boolean isPlaceholderForCarIdentifier(String placeholderName) {
        return PLACEHOLDER_NAME_ID_CAR.equals(placeholderName);
    }

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
     * @return value associated to requested placeholder if it exists, empty otherwise.
     */
    public Optional<String> retrieve(String placeholder) {
        requireNonNull(placeholder, "Placeholder is required.");

        return Optional.ofNullable(getProperty(placeholder))
                .map(String::trim);
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

    public void setBankNameIfNotExists(String bankName) {
        registerIfNotExists(PLACEHOLDER_NAME_BANK, bankName);
    }


    public void setResourceBankNameIfNotExists(String modelBankReference) {
        registerIfNotExists(PLACEHOLDER_NAME_RESOURCE_BANK, modelBankReference);
    }

    public void setRimsSlotReferenceIfNotExists(String rimsSlotReference, int rimSet) {
        String placeholderName = format(PLACEHOLDER_NAME_FMT_RIMS_REFERENCE, rimSet);
        registerIfNotExists(placeholderName, rimsSlotReference);
    }

    public void setResourceRimsBrandIfNotExists(String rimBrandReference, int rimSet) {
        registerIfNotExists(format(PLACEHOLDER_NAME_FMT_RESOURCE_RIMS_BRAND, rimSet), rimBrandReference);
    }

    public void setInteriorReferenceIfNotExists(String interiorReference, int interiorSet) {
        String placeholderName = format(PLACEHOLDER_NAME_FMT_INTERIOR_REFERENCE, interiorSet);
        registerIfNotExists(placeholderName, interiorReference);
    }

    public void setFrontRimBankNameIfNotExists(String rimBankName, int rimSet) {
        String placeholderName = format(PLACEHOLDER_NAME_FMT_RIMS_BANK, SUFFIX_FRONT_RIMS, rimSet);
        registerIfNotExists(placeholderName, rimBankName);
    }
    public void setResourceFrontRimBankIfNotExists(String rimBankReference, int rimSet) {
        String placeholderName = format(PLACEHOLDER_NAME_FMT_RESOURCE_RIM_BANK, SUFFIX_FRONT_RIMS, rimSet);
        registerIfNotExists(placeholderName, rimBankReference);
    }

    public void setRearRimBankNameIfNotExists(String rimBankName, int rimSet) {
        String placeholderName = format(PLACEHOLDER_NAME_FMT_RIMS_BANK, SUFFIX_REAR_RIMS, rimSet);
        registerIfNotExists(placeholderName, rimBankName);
    }

    public void setResourceRearRimBankIfNotExists(String rimBankReference, int rimSet) {
        String placeholderName = format(PLACEHOLDER_NAME_FMT_RESOURCE_RIM_BANK, SUFFIX_REAR_RIMS, rimSet);
        registerIfNotExists(placeholderName, rimBankReference);
    }

    public void setResourceModelNameIfNotExists(String modelNameReference) {
        registerIfNotExists(PLACEHOLDER_NAME_RESOURCE_MODEL, modelNameReference);
    }

    public void setResourceVersionNameIfNotExists(String versionNameReference) {
        registerIfNotExists(PLACEHOLDER_NAME_RESOURCE_VERSION, versionNameReference);
    }

    public void setResourceColorNameIfNotExists(String colorNameReference, int exteriorSet) {
        String placeholderName = format(PLACEHOLDER_NAME_FMT_RESOURCE_COLOR, exteriorSet);
        registerIfNotExists(placeholderName, colorNameReference);
    }

    public void setDealerReferenceIfNotExists(String dealerReference) {
        registerIfNotExists(PLACEHOLDER_NAME_DEALER_REFERENCE, dealerReference);
    }

    public void setDealerSlotIfNotExists(int slotRank) {
        registerIfNotExists(PLACEHOLDER_NAME_DEALER_SLOT, Integer.valueOf(slotRank).toString());
    }

    public Optional<String> getVehicleSlotReference() {
        return retrieve(PLACEHOLDER_NAME_SLOT_REFERENCE);
    }

    public Optional<String> getCarIdentifier() {
        return retrieve(PLACEHOLDER_NAME_ID_CAR);
    }

    public Optional<String> getBankFileName() {
        return retrieve(PLACEHOLDER_NAME_BANK);
    }

    public Optional<String> getBankFileNameResource() {
        return retrieve(PLACEHOLDER_NAME_RESOURCE_BANK);
    }

    public Optional<String> getFrontRimBankFileName(int rimSet) {
        return retrieve(format(PLACEHOLDER_NAME_FMT_RIMS_BANK, SUFFIX_FRONT_RIMS, rimSet));
    }

    public Optional<String> getRimSlotReference(int rimSet) {
        return retrieve(format(PLACEHOLDER_NAME_FMT_RIMS_REFERENCE, rimSet));
    }

    public Optional<String> getRearRimBankFileName(int rimSet) {
        return retrieve(format(PLACEHOLDER_NAME_FMT_RIMS_BANK, SUFFIX_REAR_RIMS, rimSet));
    }

    public Optional<String> getFrontRimBankFileNameResource(int rimSet) {
        return retrieve(format(PLACEHOLDER_NAME_FMT_RESOURCE_RIM_BANK, SUFFIX_FRONT_RIMS, rimSet));
    }

    public Optional<String> getRearRimBankFileNameResource(int rimSet) {
        return retrieve(format(PLACEHOLDER_NAME_FMT_RESOURCE_RIM_BANK, SUFFIX_REAR_RIMS, rimSet));
    }

    public Optional<String> getDealerReference() {
        return retrieve(PLACEHOLDER_NAME_DEALER_REFERENCE);
    }

    public Optional<Integer> getDealerSlot() {
        return retrieve(PLACEHOLDER_NAME_DEALER_SLOT).map(Integer::valueOf);
    }

    public Optional<String> getRimBrandNameResource(int rimSet) {
        return retrieve(format(PLACEHOLDER_NAME_FMT_RESOURCE_RIMS_BRAND, rimSet));
    }

    private void registerIfNotExists(String placeholderName, String value) {
        if (retrieve(placeholderName).isPresent()) {
            return;
        }

        register(placeholderName, value);
    }
}
