package fr.tduf.libunlimited.low.files.db.rw;

import org.junit.Test;

import java.util.ArrayList;

public class JsonGatewayTest {

    @Test(expected = NullPointerException.class)
    public void dump_whenNullTopicList_shouldThrowException() throws Exception {
        // GIVEN-WHEN
        JsonGateway.dump("", "", true, null, new ArrayList<>());

        // THEN: NPE
    }

    @Test(expected = NullPointerException.class)
    public void dump_whenNullIntegrityErrorList_shouldThrowException() throws Exception {
        // GIVEN-WHEN
        JsonGateway.dump("", "", true, new ArrayList<>(), null);

        // THEN: NPE
    }

    @Test(expected = NullPointerException.class)
    public void gen_whenNullTopicList_shouldThrowException() throws Exception {
        // GIVEN-WHEN
        JsonGateway.gen("", "", true, null);

        // THEN: NPE
    }
}
