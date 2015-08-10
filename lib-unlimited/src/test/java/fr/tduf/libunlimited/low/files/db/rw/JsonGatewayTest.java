package fr.tduf.libunlimited.low.files.db.rw;

import org.junit.Test;

public class JsonGatewayTest {

    @Test(expected = NullPointerException.class)
    public void dump_whenNullTopicList_shouldThrowException() throws Exception {
        // GIVEN-WHEN
        JsonGateway.dump("", "", true, null);

        // THEN: NPE
    }
}