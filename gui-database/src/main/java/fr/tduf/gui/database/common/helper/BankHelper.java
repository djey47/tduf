package fr.tduf.gui.database.common.helper;

import java.nio.file.Files;
import java.nio.file.Path;

public class BankHelper {
    /**
     * @return true if database from specified path is in packed state, false otherwise (including case of non existing).
     */
    public static boolean isPackedDatabase(Path realDatabasePath) {
        return Files.exists(realDatabasePath.resolve("DB.bnk"))
                && Files.exists(realDatabasePath.resolve("DB_US.bnk"));
    }
}
