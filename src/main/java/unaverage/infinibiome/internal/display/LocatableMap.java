package weightedgpa.infinibiome.internal.display;

/*
import weightedgpa.infinibiome.internal.misc.Pair;
import weightedgpa.infinibiome.internal.misc.InfinibiomeDepIDs;
import weightedgpa.infinibiome.api.startup.DefaultDepIDs;
import weightedgpa.infinibiome.api.startup.DependencyManager;
import weightedgpa.infinibiome.api.startup.DependencyManager.Injector;
import weightedgpa.infinibiome.api.startup.Locatable;
import weightedgpa.infinibiome.api.startup.DepID;
import weightedgpa.infinibiome.api.generators.Seed;
import weightedgpa.infinibiome.api.pointsprovider.PointsProvider;
import weightedgpa.infinibiome.api.pos.BlockPos2D;
import weightedgpa.infinibiome.api.posdata.DefaultPosDataKeys;
import weightedgpa.infinibiome.api.posdata.LandmassType;
import weightedgpa.infinibiome.api.posdata.PosDataProvider;
import weightedgpa.infinibiome.internal.generators.DefaultModule;
import weightedgpa.infinibiome.internal.misc.ChunkGenWorld;
import weightedgpa.infinibiome.internal.generators.misc.DefaultConfigGen;

import java.awt.*;
import java.util.Collections;

public final class LocatableMap extends DisplayMapBase {
    private final PointsProvider<BlockPos2D> allLocations;
    private final float radius;
    private final PosDataProvider data;

    public LocatableMap(){
        this(
            Seed.ROOT.newSeed("98"),
            InfinibiomeDepIDs.OAK_BIG,
            20,
            5
        );
    }

    private LocatableMap(Seed seed, DepID resource, int scale, int radius) {
        super(scale);

        DependencyManager depManager = DependencyManager.init();

        depManager.getTable().add(
            DefaultDepIDs.SEED, __ -> seed
        );

        depManager.getTable().add(
            DefaultConfigGen.ID, __ -> new DefaultConfigGen(null)
        );

        DefaultModule.getDependencies(depManager.getTable());

        depManager.getTable().remove(
            InfinibiomeDepIDs.RAVINE
        );
        depManager.getTable().remove(
            InfinibiomeDepIDs.CAVE
        );

        this.data = depManager.getInjector().get(DefaultDepIDs.DATA);

        this.allLocations = ((Locatable)depManager.getInjector().get(resource))
            .getAllLocations(
                Collections.emptyList(),
                new ChunkGenWorld(depManager.getInjector())
            );

        this.radius = radius;
    }

    @Override
    protected Color getColor(int posX, int posZ, int screenPixelX, int screenPixelZ) {
        if (posX % 1000 == 0 || posZ % 1000 == 0){
            return Color.BLACK;
        }

        BlockPos2D pos = BlockPos2D.init(posX, posZ);

        if (data.get(DefaultPosDataKeys.LANDMASS_TYPE, pos) instanceof LandmassType.Land){
            if (isHere(pos)){
                return new Color(0,128,0);
            }
            return new Color(0, 255, 0);
        }
        if (isHere(pos)){
            return new Color(0, 0, 128);
        }
        return new Color(0, 0, 255);
    }

    private boolean isHere(BlockPos2D pos){
        for (BlockPos2D __ : allLocations.getBoundedPoints(pos, radius)){
            return true;
        }
        return false;
    }
}

 */
