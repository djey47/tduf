package fr.tduf.libunlimited.low.files.bin.cameras.domain;

import fr.tduf.libunlimited.framework.function.TriConsumer;
import fr.tduf.libunlimited.low.files.bin.cameras.rw.CamerasParser;
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
    TYPE("type", CamerasParser::getViewType, CamerasParser::setViewType),
    STEERING_WHEEL_TURN("steeringWheelTurn", GenericParser::getNumeric, GenericParser::setNumeric),
    STEERING_WHEEL_TILT("steeringWheelTilt", GenericParser::getNumeric, GenericParser::setNumeric),
    CAMERA_POSITION_X("cameraPositionX", GenericParser::getNumeric, GenericParser::setNumeric),
    CAMERA_POSITION_Y("cameraPositionY", GenericParser::getNumeric, GenericParser::setNumeric),
    CAMERA_POSITION_Z("cameraPositionZ", GenericParser::getNumeric, GenericParser::setNumeric),
    VIEW_POSITION_X("viewPositionX", GenericParser::getNumeric, GenericParser::setNumeric),
    VIEW_POSITION_Y("viewPositionY", GenericParser::getNumeric, GenericParser::setNumeric),
    VIEW_POSITION_Z("viewPositionZ", GenericParser::getNumeric, GenericParser::setNumeric),
    BINOCULARS("binoculars", GenericParser::getNumeric, GenericParser::setNumeric),
    UNKNOWN_1("unk1", GenericParser::getNumeric, GenericParser::setNumeric),
    UNKNOWN_2("unk2", GenericParser::getNumeric, GenericParser::setNumeric),
    UNKNOWN_3("unk3", GenericParser::getNumeric, GenericParser::setNumeric),
    UNKNOWN_4("unk4", GenericParser::getNumeric, GenericParser::setNumeric),
    UNKNOWN_5("unk5", GenericParser::getNumeric, GenericParser::setNumeric);

    private String storeFieldName;
    private BiFunction<DataStore, DataStoreProps, Optional<?>> parsingFunction;
    private TriConsumer<Object, DataStore, DataStoreProps> updatingFunction;

    ViewProps(String storeFieldName, BiFunction<DataStore, DataStoreProps, Optional<?>> parsingFunction, TriConsumer<Object, DataStore, DataStoreProps> updatingFunction) {
        this.storeFieldName = storeFieldName;
        this.parsingFunction = parsingFunction;
        this.updatingFunction = updatingFunction;
    }

    @Override
    public Optional<?> retrieveFrom(DataStore viewStore) {
        return parsingFunction.apply(viewStore, this);
    }

    @Override
    public void updateIn(DataStore viewStore, Object value) {
        updatingFunction.accept(value, viewStore, this);
    }

    @Override
    public String getStoreFieldName() {
        return storeFieldName;
    }

    @Override
    public String toString() {
        return storeFieldName;
    }

    public static Stream<ViewProps> valuesStream() {
        return Stream.of(values());
    }
}
