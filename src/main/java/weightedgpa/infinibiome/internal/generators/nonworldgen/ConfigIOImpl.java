package weightedgpa.infinibiome.internal.generators.nonworldgen;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.electronwill.nightconfig.toml.TomlParser;
import com.electronwill.nightconfig.toml.TomlWriter;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.loading.FMLPaths;
import weightedgpa.infinibiome.api.dependency.*;
import weightedgpa.infinibiome.api.generators.nonworldgen.ConfigIO;
import weightedgpa.infinibiome.api.generators.nonworldgen.DefaultConfig;
import weightedgpa.infinibiome.internal.dependency.DependencyInjectorImpl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public final class ConfigIOImpl implements ConfigIO {
    private final CommentedConfig config;

    public ConfigIOImpl(DependencyInjector di) {
        try {
            config = new TomlParser().parse(
                new FileReader(
                    getReadFile(di.get(ServerWorld.class))
                )
            );
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public ConfigIOImpl(List<DependencyModule> modules){
        modules = new ArrayList<>(modules);

        config = CommentedConfig.wrap(new LinkedHashMap<>(), TomlFormat.instance());

        modules.add(
            t -> t.addItem(ConfigIOImpl.class, __ -> this)
        );

        DependencyInjector di = new DependencyInjectorImpl(modules).initInjector();

        di.getAll(DefaultConfig.class);
    }

    private ConfigIOImpl(CommentedConfig config) {
        this.config = config;
    }

    public static void refreshConfig(List<DependencyModule> depModules){
        CommentedConfig defaultConfig = new ConfigIOImpl(depModules).getRawConfig();

        new TomlWriter().write(
            defaultConfig,
            getPreConfigFile(),
            WritingMode.REPLACE
        );
    }

    public static void saveConfig(DependencyInjector di){
        di.getAll(DefaultConfig.class);

        CommentedConfig config = di.get(ConfigIO.class).getRawConfig();

        ServerWorld world = di.get(ServerWorld.class);

        new TomlWriter().write(
            config,
            getWorldConfigFile(world),
            WritingMode.REPLACE
        );
    }

    private static File getReadFile(ServerWorld serverWorld){
        File worldConfigFile = getWorldConfigFile(serverWorld);

        if (worldConfigFile.exists()){
            return worldConfigFile;
        }

        return getPreConfigFile();
    }

    private static File getWorldConfigFile(ServerWorld world){
        return new File(
            world.getSaveHandler().getWorldDirectory().getAbsolutePath() +
                File.separator +
                "serverconfig" +
                File.separator +
                "infinibiome_config.toml"
        );
    }

    private static File getPreConfigFile(){
        return new File(
            FMLPaths.CONFIGDIR.get().toAbsolutePath() +
            File.separator +
            "infinibiome_preconfig.toml"
        );
    }

    @Override
    public ConfigIOImpl subConfig(String path) {
        if (!config.contains(path)) {
            config.add(
                path,
                CommentedConfig.wrap(new LinkedHashMap<>(), TomlFormat.instance())
            );
        }

        return new ConfigIOImpl(config.<CommentedConfig>get(path));
    }

    @Override
    public int getInt(
        String path,
        int defaultValue,
        int minValue,
        int maxValue,
        String description
    ) {
        assert minValue <= maxValue;
        assert minValue <= defaultValue && defaultValue <= maxValue;

        if (!description.isEmpty()) {
            config.setComment(
                path,
                "\n" +
                    description + "\n" +
                    String.format("Default: %s", defaultValue) + "\n" +
                    String.format("Range: %s ~ %s", minValue, maxValue)
            );
        } else {
            config.setComment(
                path,
                "\n" +
                    String.format("Default: %s", defaultValue) + "\n" +
                    String.format("Range: %s ~ %s", minValue, maxValue)
            );
        }

        int result;

        try {
            result = config.getInt(path);

            if (result < minValue) {
                result = minValue;
            }
            if (result > maxValue) {
                result = maxValue;
            }
        } catch (Throwable e) {
            result = defaultValue;
        }

        config.set(path, result);

        return result;
    }


    @Override
    public double getFloat(
        String path,
        double defaultValue,
        double minValue,
        double maxValue,
        String description
    ) {
        return getRelativeFloat(
            path,
            defaultValue,
            minValue,
            maxValue,
            1,
            description
        );
    }

    @Override
    public double getRelativeFloat(
        String path,
        double defaultValue,
        double minValue,
        double maxValue,
        String description
    ) {
        return getRelativeFloat(
            path,
            defaultValue,
            minValue,
            maxValue,
            defaultValue,
            description
        );
    }

    @Override
    public double getRelativeFloat(
        String path,
        double defaultValue,
        double minValue,
        double maxValue,
        double relative,
        String description
    ) {
        assert minValue <= maxValue;
        assert minValue <= defaultValue && defaultValue <= maxValue;
        assert relative > 0;

        if (!description.isEmpty()) {
            config.setComment(
                path,
                "\n" +
                    description + "\n" +
                    String.format("Default: %s", defaultValue/relative) + "\n" +
                    String.format("Range: %s ~ %s", minValue/relative, maxValue/relative)
            );
        } else {
            config.setComment(
                path,
                "\n" +
                    String.format("Default: %s", defaultValue/relative) + "\n" +
                    String.format("Range: %s ~ %s", minValue/relative, maxValue/relative)
            );
        }

        double result;

        try {
            result = config.<Number>get(path).doubleValue();

            if (result < minValue/relative) {
                result = minValue/relative;
            }
            if (result > maxValue/relative) {
                result = maxValue/relative;
            }
        } catch (Throwable e) {
            //e.printStackTrace();

            result = defaultValue/relative;
        }

        config.set(path, result);

        return result*relative;
    }


    @Override
    public boolean getBool(

        String path,
        boolean defaultValue,
        String description
    ) {
        if (!description.isEmpty()) {
            config.setComment(
                path,
                "\n" +
                description + "\n" +
                String.format("Default: %s", defaultValue)
            );
        } else {
            config.setComment(
                path,
                "\n" +
                String.format("Default: %s", defaultValue)
            );
        }

        boolean result;

        try {
            result = config.get(path);
        } catch (Throwable e) {
            //e.printStackTrace();

            result = defaultValue;
        }

        config.set(path, result);

        return result;
    }

    @Override
    public CommentedConfig getRawConfig() {
        return config;
    }
}
