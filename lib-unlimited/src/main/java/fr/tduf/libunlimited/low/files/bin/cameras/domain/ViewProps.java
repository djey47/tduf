package fr.tduf.libunlimited.low.files.bin.cameras.domain;

import fr.tduf.libunlimited.framework.function.TriConsumer;
import fr.tduf.libunlimited.low.files.bin.cameras.rw.CamerasParser;
import fr.tduf.libunlimited.low.files.research.domain.DataStore;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Stream;

/**
 * All handled view properties
 */
public enum ViewProps {
    // TODO introduce field for class and use generics
    TYPE("type", CamerasParser::getViewType, CamerasParser::setViewType),
    STEERING_WHEEL_TURN("steeringWheelTurn", CamerasParser::getNumeric, CamerasParser::setNumeric),
    STEERING_WHEEL_TILT("steeringWheelTilt", CamerasParser::getNumeric, CamerasParser::setNumeric),
    CAMERA_POSITION_X("cameraPositionX", CamerasParser::getNumeric, CamerasParser::setNumeric),
    CAMERA_POSITION_Y("cameraPositionY", CamerasParser::getNumeric, CamerasParser::setNumeric),
    CAMERA_POSITION_Z("cameraPositionZ", CamerasParser::getNumeric, CamerasParser::setNumeric),
    VIEW_POSITION_X("viewPositionX", CamerasParser::getNumeric, CamerasParser::setNumeric),
    VIEW_POSITION_Y("viewPositionY", CamerasParser::getNumeric, CamerasParser::setNumeric),
    VIEW_POSITION_Z("viewPositionZ", CamerasParser::getNumeric, CamerasParser::setNumeric),
    BINOCULARS("binoculars", CamerasParser::getNumeric, CamerasParser::setNumeric);

    private String storeFieldName;
    private BiFunction<DataStore, ViewProps, Optional<?>> parsingFunction;
    private TriConsumer<Object, DataStore, ViewProps> updatingFunction;

    ViewProps(String storeFieldName, BiFunction<DataStore, ViewProps, Optional<?>> parsingFunction, TriConsumer<Object, DataStore, ViewProps> updatingFunction) {
        this.storeFieldName = storeFieldName;
        this.parsingFunction = parsingFunction;
        this.updatingFunction = updatingFunction;
    }

    // TODO rename to retrieveIn()
    public Optional<?> parse(DataStore viewStore) {
        return parsingFunction.apply(viewStore, this);
    }

    public void updateIn(DataStore viewStore, Object value) {
        updatingFunction.accept(value, viewStore, this);
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
