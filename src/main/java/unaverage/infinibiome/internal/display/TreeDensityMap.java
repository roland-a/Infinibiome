package weightedgpa.infinibiome.internal.display;

import weightedgpa.infinibiome.api.pos.BlockPos2D;
import weightedgpa.infinibiome.api.pos.InterChunkPos;
import weightedgpa.infinibiome.api.posdata.PosDataHelper;
import weightedgpa.infinibiome.api.posdata.PosDataKeys;
import weightedgpa.infinibiome.internal.generators.interchunks.tree.TreeGens;

import java.awt.*;

public final class TreeDensityMap extends DataMapBase {
    private final TreeGens treeGens;

    public TreeDensityMap(){
        this(1, "67");
    }

    private TreeDensityMap(int scale, String seedBranch) {
        super(scale, seedBranch);

        this.treeGens = di.get(TreeGens.class);
    }

    @Override
    protected Color getColor(int posX, int posZ, int screenPixelX, int screenPixelZ) {
        InterChunkPos interChunkPos = new InterChunkPos(posX, posZ);

        BlockPos2D pos = interChunkPos.getLowestCenterBlockPos();

        if (posData.get(PosDataKeys.LANDMASS_TYPE, pos).isOcean()) return Color.BLUE;

        if (PosDataHelper.isUnderwaterPortionOfLakeOrRiver(pos, posData)) return Color.BLUE;

        float treeDensity = (float) treeGens.getApproxDensity(interChunkPos);

        return new Color(treeDensity, treeDensity, treeDensity);
    }
}
