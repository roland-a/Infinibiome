package weightedgpa.infinibiome.api.posdata;

import net.minecraft.block.BlockState;
import net.minecraft.world.biome.Biome;

import java.util.List;
import java.util.Optional;

/**
 * List of default keys
 *
 * More keys can be created as long as theyre initialized before any PosDataProvider instances are created
 */
public final class PosDataKeys {
    private PosDataKeys(){}

    public static final PosDataKey<ClimateValue>
        TEMPERATURE = new PosDataKey<>();

    public static final PosDataKey<ClimateValue>
        HUMIDITY = new PosDataKey<>();

    public static final PosDataKeyFloat
        MAPPED_HEIGHT = new PosDataKeyFloat();

    public static final PosDataKeyFloat
        AMP = new PosDataKeyFloat();

    public static final PosDataKey<LandmassInfo>
        LANDMASS_TYPE = new PosDataKey<>();

    public static final PosDataKeyDefered<List<BlockState>>
        GROUND_BLOCKS = new PosDataKeyDefered<>();

    public static final PosDataKeyDefered<Optional<Biome>>
        OVERRIDE_BIOME = new PosDataKeyDefered<Optional<Biome>>(__ -> Optional::empty);

    public static final PosDataKey<Boolean>
        HEIGHT_MODIFIED_BY_LAKE = new PosDataKey<>(__ -> false);

    public static final PosDataKey<Boolean>
        HEIGHT_MODIFIED_BY_RIVER = new PosDataKey<>(__ -> false);

    public static final PosDataKey<Boolean>
        IS_MUSHROOM_ISLAND = new PosDataKey<>(__ -> false);

    public static final PosDataKey<Boolean>
        PRE_CLAMP_APPLIED = new PosDataKey<>(__ -> false);

    public static final PosDataKey<Boolean>
        AllOW_INFINIBIOME_GEN = new PosDataKey<>(__ -> true);

    public static void init() {}
}
