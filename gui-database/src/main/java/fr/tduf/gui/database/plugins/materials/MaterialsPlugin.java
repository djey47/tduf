package fr.tduf.gui.database.plugins.materials;

import com.esotericsoftware.minlog.Log;
import fr.tduf.gui.database.plugins.common.AbstractDatabasePlugin;
import fr.tduf.gui.database.plugins.common.contexts.EditorContext;
import fr.tduf.gui.database.plugins.common.contexts.OnTheFlyContext;
import fr.tduf.gui.database.plugins.common.contexts.PluginContext;
import fr.tduf.libunlimited.common.game.FileConstants;
import fr.tduf.libunlimited.framework.io.XByteArrayInputStream;
import fr.tduf.libunlimited.low.files.gfx.materials.domain.MaterialDefs;
import fr.tduf.libunlimited.low.files.gfx.materials.rw.MaterialsParser;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import static java.nio.file.Files.readAllBytes;

public class MaterialsPlugin extends AbstractDatabasePlugin {
    private static final Class<MaterialsPlugin> thisClass = MaterialsPlugin.class;
    private static final String THIS_CLASS_NAME = thisClass.getSimpleName();

    private static final String FILE_COLORS_BANK = "colors.bnk";

    private final PluginContext materialsContext = new PluginContext();
    private final Property<MaterialDefs> materialsInfoEnhancedProperty = new SimpleObjectProperty<>();


    /**
     * Required contextual information:
     * - gameLocation
     * - materialsContext
     *
     * @param pluginName : name of plugin to initialize
     * @param context : all required information about Database Editor
     * @throws IOException when cameras file can't be parsed for some reason
     */
    @Override
    public void onInit(String pluginName, EditorContext context) throws IOException {
        super.onInit(pluginName, context);

        materialsContext.reset();

        // Loads colors.2dm in colors.bnk
        String gameLocation = context.getGameLocation();

        // Extracts all files from colors.bnk
        Path bankFilePath = resolveBankFilePath(gameLocation, FILE_COLORS_BANK);
        String extractedDirectory = createTempDirectory();
        Log.debug(THIS_CLASS_NAME, String.format("Extracting materials info from %s to %s...", bankFilePath, extractedDirectory));
        context.getMainStageController().getBankSupport().extractAll(bankFilePath.toString(), extractedDirectory);

        // Resolves material info binary file
        String binaryFileLocation = resolveColorsFileLocation(extractedDirectory).toString();
        Log.debug(THIS_CLASS_NAME, String.format("Materials binary location: %s", binaryFileLocation));
        materialsContext.setBinaryFileLocation(binaryFileLocation);

        // Loads binary file and create domain objects
        Log.info(THIS_CLASS_NAME, String.format("Loading materials info from %s...", binaryFileLocation));
        MaterialsParser materialsParser = MaterialsParser.load(getMaterialsInputStream(binaryFileLocation));
        MaterialDefs materialDefs = materialsParser.parse();

        materialsInfoEnhancedProperty.setValue(materialDefs);

        Log.info(THIS_CLASS_NAME, String.format("Material definitions loaded, %d materials available", materialDefs.getMaterials().size()));

        materialsContext.setPluginLoaded(true);
    }

    @Override
    public void onSave() throws IOException {

    }

    @Override
    public Node renderControls(OnTheFlyContext onTheFlyContext) {
        return null;
    }

    @Override
    public Set<String> getCss() {
        return null;
    }

    private static Path resolveBankFilePath(String gameLocation, String filePath) {
        return Paths.get(gameLocation, FileConstants.DIRECTORY_EURO, FileConstants.DIRECTORY_BANKS, FileConstants.DIRECTORY_VEHICLES, filePath);
    }

    private static String createTempDirectory() throws IOException {
        return Files.createTempDirectory("tduf-materials-colorsBank").toString();
    }

    private static Path resolveColorsFileLocation(String extractedDirectory) {
        return Paths.get(extractedDirectory, "4Build", "PC", "EURO", "Vehicules", "common", "common_mesh", "shader", "Colors.2DM");
    }

    private static XByteArrayInputStream getMaterialsInputStream(String sourceMaterialsFile) throws IOException {
        return new XByteArrayInputStream(readAllBytes(Paths.get(sourceMaterialsFile)));
    }
}
