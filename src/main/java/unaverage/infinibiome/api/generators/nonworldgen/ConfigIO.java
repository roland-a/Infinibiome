package weightedgpa.infinibiome.api.generators.nonworldgen;

import com.electronwill.nightconfig.core.CommentedConfig;

/**
 * Used for reading and writing configuration values on the config file generated at minecraft menu screen
 */
public interface ConfigIO {
    ConfigIO subConfig(String path);

    int getInt(String path, int defaultValue, int minValue, int maxValue, String description);

    double getFloat(String path, double defaultValue, double minValue, double maxValue, String description);

    double getRelativeFloat(String path, double defaultValue, double minValue, double maxValue, String description);

    double getRelativeFloat(String path, double defaultValue, double minValue, double maxValue, double relative, String description);

    boolean getBool(String path, boolean defaultValue, String description);

    CommentedConfig getRawConfig();
}
