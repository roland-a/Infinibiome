package weightedgpa.infinibiome.internal.minecraftImpl;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.BiomeManager;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilder;

final class PosEmbeddedBiomes{
    static final BiomeManager MANAGER = new PosEmbeddedBiomes.Manager();

    private static final class Manager extends BiomeManager{
        private Manager() {
            super(
                (x, y, z) -> null,
                0,
                (seed, x, y, z, biomeReader) -> null
            );
        }

        @Override
        public net.minecraft.world.biome.Biome getBiome(BlockPos posIn) {
            return new Biome(
                new ChunkPos(posIn)
            );
        }

        @Override
        public BiomeManager copyWithProvider(BiomeProvider newProvider) {
            return this;
        }
    }

    @SuppressWarnings("ClassNameSameAsAncestorName")
    static class Biome extends net.minecraft.world.biome.Biome {
        private static final Builder BUILDER = new Builder()
            .category(Category.BEACH)
            .temperature(0)
            .downfall(0)
            .precipitation(RainType.RAIN)
            .surfaceBuilder(
                SurfaceBuilder.NOPE,
                SurfaceBuilder.SAND_SAND_GRAVEL_CONFIG
            )
            .depth(1)
            .scale(1)
            .waterColor(4159204)
            .waterFogColor(329011)
            .parent(null);

        private final ChunkPos pos;

        Biome(ChunkPos pos) {
            super(
                BUILDER
            );
            this.pos = pos;
        }

        public ChunkPos getPos() {
            return pos;
        }
    }
}
