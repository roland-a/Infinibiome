package weightedgpa.infinibiome.internal.generators.utils;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.electronwill.nightconfig.toml.TomlParser;

import java.util.LinkedHashMap;

public final class ConfigHelper {
    private ConfigHelper(){}

    public static CommentedConfig getOrMakeSubConfig(CommentedConfig config, String path){
        if (config.contains(path)) return config.get(path);

        CommentedConfig result = CommentedConfig.wrap(
            new LinkedHashMap<>(),
            TomlFormat.instance()
        );

        config.set(path, result);

        return result;
    }

    public static int getInt(
        CommentedConfig config,
        String path,
        int defaultValue,
        int minValue,
        int maxValue,
        String description
    ){
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

            if (result < minValue){
                result = minValue;
            }
            if (result > maxValue){
                result = maxValue;
            }
        } catch (Throwable e){
            result = defaultValue;
        }

        config.set(path, result);

        return result;
    }


    public static double getFloat(
        CommentedConfig config,
        String path,
        double defaultValue,
        double minValue,
        double maxValue,
        String description
    ){
        return getRelativeFloat(
            config,
            path,
            defaultValue,
            minValue,
            maxValue,
            1,
            description
        );
    }

    public static double getRelativeFloat(
        CommentedConfig config,
        String path,
        double defaultValue,
        double minValue,
        double maxValue,
        String description
    ){
        return getRelativeFloat(
            config,
            path,
            defaultValue,
            minValue,
            maxValue,
            defaultValue,
            description
        );
    }

    public static double getRelativeFloat(
        CommentedConfig config,
        String path,
        double defaultValue,
        double minValue,
        double maxValue,
        double relative,
        String description
    ){
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
        }
        else {
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

            if (result < minValue / relative){
                result = minValue / relative;
            }
            if (result > maxValue / relative){
                result = maxValue / relative;
            }
        } catch (Throwable e){
            //e.printStackTrace();

            result = defaultValue / relative;
        }

        config.set(path, result);

        return result * relative;
    }


    public static boolean getBool(
        CommentedConfig config,
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
        }
        catch (Throwable e){
            //e.printStackTrace();

            result = defaultValue;
        }

        config.set(path, result);

        return result;
    }
}
