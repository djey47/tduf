package fr.tduf.libunlimited.low.files.db.rw;


import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertThrows;

class JsonGatewayTest {

    @Test
    void dump_whenNullTopicList_shouldThrowException() throws Exception {
        // GIVEN-WHEN-THEN
        assertThrows(NullPointerException.class,
                () -> JsonGateway.dump("", "", null, new HashSet<>()));
    }

    @Test
    void dump_whenNullIntegrityErrorList_shouldThrowException() throws Exception {
        // GIVEN-WHEN-THEN
        assertThrows(NullPointerException.class,
                () -> JsonGateway.dump("", "", new ArrayList<>(), null));
    }

    @Test
    void gen_whenNullTopicList_shouldThrowException() throws Exception {
        // GIVEN-WHEN-THEN
        assertThrows(NullPointerException.class,
                () -> JsonGateway.gen("", "", null));
    }
}
