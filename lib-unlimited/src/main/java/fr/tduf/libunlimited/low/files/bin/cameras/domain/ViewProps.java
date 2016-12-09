package fr.tduf.libunlimited.low.files.bin.cameras.domain;

import fr.tduf.libunlimited.low.files.bin.cameras.rw.CamerasParser;
import fr.tduf.libunlimited.low.files.research.domain.DataStore;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Stream;

/**
 * All handled view properties
 */
public enum ViewProps {
    TYPE("type", CamerasParser::getViewType),
    STEERING_WHEEL_TURN("steeringWheelTurn", CamerasParser::getNumeric),
    STEERING_WHEEL_TILT("steeringWheelTilt", CamerasParser::getNumeric),
    CAMERA_POSITION_X("cameraPositionX", CamerasParser::getNumeric),
    CAMERA_POSITION_Y("cameraPositionY", CamerasParser::getNumeric),
    CAMERA_POSITION_Z("cameraPositionZ", CamerasParser::getNumeric),
    VIEW_POSITION_X("viewPositionX", CamerasParser::getNumeric),
    VIEW_POSITION_Y("viewPositionY", CamerasParser::getNumeric),
    VIEW_POSITION_Z("viewPositionZ", CamerasParser::getNumeric),
    BINOCULARS("binoculars", CamerasParser::getNumeric);


    private String storeFieldName;
    private BiFunction<DataStore, ViewProps, Optional<?>> parsingFunction;

    ViewProps(String storeFieldName, BiFunction<DataStore, ViewProps, Optional<?>> parsingFunction) {
        this.storeFieldName = storeFieldName;
        this.parsingFunction = parsingFunction;
    }

    public Optional<?> parse(DataStore dataStore) {
        return parsingFunction.apply(dataStore, this);
    }

    public String getStoreFieldName() {
        return storeFieldName;
    }

    public static Stream<ViewProps> valuesStream() {
        return Stream.of(values());
    }

    @Override
    public String toString() {
        return storeFieldName;
    }
}
