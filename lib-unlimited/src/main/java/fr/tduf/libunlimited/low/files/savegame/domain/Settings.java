package fr.tduf.libunlimited.low.files.savegame.domain;

import fr.tduf.libunlimited.low.files.db.dto.DbResourceDto;

import java.util.List;

public class Settings {
    private DbResourceDto.Locale language;
    private List<KeyMapping> keyMappings;
}
