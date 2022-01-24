package weightedgpa.infinibiome.internal.generators.interchunks.struct;

import net.minecraft.world.gen.feature.Feature;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.generators.nonworldgen.DefaultConfig;
import weightedgpa.infinibiome.api.generators.nonworldgen.ConfigIO;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.internal.generators.interchunks.SmallObjectGenBase;

public final class DungeonGen extends SmallObjectGenBase {
    public DungeonGen(DependencyInjector di){
        super(di, Infinibiome.MOD_ID + ":dungeon");

        config = initConfig()
            .setWithFeature(
                Feature.MONSTER_ROOM
            )
            .setCount(
                di.getAll(Config.class).get(0).dungeonCount
            )
            .setAttemptsPerCount(1)
            .randomUnderHeight(128)
            .noChancePerChunk()
            .noExtraConditions();
    }
    
    public static final class Config implements DefaultConfig {
        private final int dungeonCount;

        public Config(DependencyInjector di){
            dungeonCount = di.get(ConfigIO.class).subConfig("STRUCT").getInt(
                "dungeon_count",
                8,
                0,
                256,
                "This value is how many times dungeons will attempt to generate per chunk."
            );
        }
    }
}
