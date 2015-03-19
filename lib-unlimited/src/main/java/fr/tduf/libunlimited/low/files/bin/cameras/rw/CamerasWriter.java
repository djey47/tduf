package fr.tduf.libunlimited.low.files.bin.cameras.rw;

import fr.tduf.libunlimited.low.files.research.rw.GenericWriter;

import java.io.IOException;

public class CamerasWriter extends GenericWriter<String> {
    protected CamerasWriter(String data) throws IOException {
        super(data);
    }

    @Override
    protected void fillStore() {

    }

    @Override
    public String getStructureResource() {
        return "/files/structures/BIN-cameras-map.json";
    }

}
