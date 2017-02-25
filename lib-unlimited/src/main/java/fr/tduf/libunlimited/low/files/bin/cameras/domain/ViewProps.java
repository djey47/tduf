package fr.tduf.libunlimited.low.files.bin.cameras.domain;

import fr.tduf.libunlimited.low.files.common.domain.DataStoreProps;
import fr.tduf.libunlimited.low.files.research.domain.DataStore;
import fr.tduf.libunlimited.low.files.research.rw.GenericParser;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Stream;

/**
 * All handled view properties
 */
public enum ViewProps implements DataStoreProps {
    STEERING_WHEEL_TURN("steeringWheelTurn", "", GenericParser::getNumeric),
    STEERING_WHEEL_TILT("steeringWheelTilt", "Wheel vertical adjustment", GenericParser::getNumeric),
    CAMERA_POSITION_X("cameraPositionX", "Left/Right", GenericParser::getNumeric),
    CAMERA_POSITION_Y("cameraPositionY", "Up/Down", GenericParser::getNumeric),
    CAMERA_POSITION_Z("cameraPositionZ", "Forward/Backward", GenericParser::getNumeric),
    VIEW_POSITION_X("viewPositionX", "Left/Right", GenericParser::getNumeric),
    VIEW_POSITION_Y("viewPositionY", "Up/Down", GenericParser::getNumeric),
    VIEW_POSITION_Z("viewPositionZ", "Forward/Backward", GenericParser::getNumeric),
    BINOCULARS("binoculars", "", GenericParser::getNumeric),
    UNKNOWN_1("unk1", "", GenericParser::getNumeric),
    UNKNOWN_2("unk2", "", GenericParser::getNumeric),
    UNKNOWN_3("unk3", "", GenericParser::getNumeric),
    UNKNOWN_4("unk4", "", GenericParser::getNumeric),
    UNKNOWN_5("unk5", "", GenericParser::getNumeric);

    private String storeFieldName;
    private String description;
    private BiFunction<DataStore, DataStoreProps, Optional<?>> parsingFunction;

    ViewProps(String storeFieldName, String description, BiFunction<DataStore, DataStoreProps, Optional<?>> parsingFunction) {
        this.storeFieldName = storeFieldName;
        this.description = description;
        this.parsingFunction = parsingFunction;
    }

    @Override
    public Optional<?> retrieveFrom(DataStore viewStore) {
        return parsingFunction.apply(viewStore, this);
    }

    @Override
    public String getStoreFieldName() {
        return storeFieldName;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return storeFieldName;
    }

    public static Stream<ViewProps> valuesStream() {
        return Stream.of(values());
    }
}
