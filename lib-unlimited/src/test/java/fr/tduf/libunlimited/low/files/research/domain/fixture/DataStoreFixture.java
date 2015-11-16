package fr.tduf.libunlimited.low.files.research.domain.fixture;

import fr.tduf.libunlimited.low.files.research.common.helper.StructureHelper;
import fr.tduf.libunlimited.low.files.research.common.helper.TypeHelper;
import fr.tduf.libunlimited.low.files.research.domain.DataStore;
import fr.tduf.libunlimited.low.files.research.dto.FileStructureDto;

import java.io.IOException;
import java.nio.ByteBuffer;

import static fr.tduf.libunlimited.low.files.research.dto.FileStructureDto.Type.*;

/**
 * Provides data for data store tests.
 */
public class DataStoreFixture {

    public static void createStoreEntries(DataStore dataStore) {
        dataStore.clearAll();

        putLongInStore("entry_list[0].my_field", 10L, false, dataStore);
        putFloatInStore("entry_list[0].my_fp_field", 235.666667f, dataStore);
        putStringInStore("entry_list[0].a_field", "az", dataStore);
        putRawValueInStore("entry_list[0].another_field", new byte[]{0x1, 0x2, 0x3, 0x4}, dataStore);
        putLongInStore("entry_list[1].my_field", 20L, false, dataStore);
        putFloatInStore("entry_list[1].my_fp_field", 335.666667f, dataStore);
        putStringInStore("entry_list[1].a_field", "bz", dataStore);
        putRawValueInStore("entry_list[1].another_field", new byte[]{0x5, 0x6, 0x7, 0x8}, dataStore);
        putLongInStore("entry_list[2].my_field", 30L, false, dataStore);
        putFloatInStore("entry_list[2].my_fp_field", 435.666667f, dataStore);
        putStringInStore("entry_list[2].a_field", "cz", dataStore);
        putRawValueInStore("entry_list[2].another_field", new byte [] {0x9, 0xA, 0xB, 0xC}, dataStore);
    }

    public static void putRawValueInStore(String key, byte[] bytes, DataStore dataStore) {
        dataStore.addValue(key, UNKNOWN, bytes);
    }

    public static void putStringInStore(String key, String value, DataStore dataStore) {
        dataStore.addValue(key, TEXT, value.getBytes());
    }

    public static void putLongInStore(String key, long value, boolean signed, DataStore dataStore) {
        byte[] bytes = ByteBuffer
                .allocate(8)
                .putLong(value)
                .array();
        dataStore.addValue(key, INTEGER, signed, bytes);
    }

    public static void putFloatInStore(String key, float value, DataStore dataStore) {
        byte[] bytes = ByteBuffer
                .allocate(4)
                .putFloat(value)
                .array();
        dataStore.addValue(key, FPOINT, bytes);
    }

    public static void putHalfFloatInStore(String key, float value, DataStore dataStore) {
        byte[] bytes = TypeHelper.floatingPoint16ToRaw(value);
        dataStore.addValue(key, FPOINT, bytes);
    }

    public static FileStructureDto getFileStructure(String resourcePath) throws IOException {
        return StructureHelper.retrieveStructureFromLocation(resourcePath);
    }
}