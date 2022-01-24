package weightedgpa.infinibiome.internal.display;

import net.minecraft.world.biome.Biomes;

import weightedgpa.infinibiome.api.posdata.PosDataKeys;
import weightedgpa.infinibiome.api.posdata.LandmassInfo;
import weightedgpa.infinibiome.api.generators.PosDataGen;
import weightedgpa.infinibiome.api.generators.Timing;
import weightedgpa.infinibiome.api.posdata.PosDataTable;

import java.util.Optional;

public final class PosDataFiller implements PosDataGen {
    @Override
    public Timing getTiming() {
        return Timing.getDefault();
    }

    @Override
    public void generateData(PosDataTable data) {
        data.set(
            PosDataKeys.MAPPED_HEIGHT,
            64
        );

        data.set(
            PosDataKeys.LANDMASS_TYPE,
            new LandmassInfo.Land(0)
        );

        data.set(
            PosDataKeys.OVERRIDE_BIOME,
            () -> Optional.of(Biomes.PLAINS)
        );
    }
}
