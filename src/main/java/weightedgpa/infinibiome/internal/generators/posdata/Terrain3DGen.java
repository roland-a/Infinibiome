package weightedgpa.infinibiome.internal.generators.posdata;

/*
import weightedgpa.infinibiome.internal.MiscHelper;
import weightedgpa.infinibiome.api.data.DefaultKeys;
import weightedgpa.infinibiome.api.data.BlockPosDataOutput;
import weightedgpa.infinibiome.api.noise.base.Const;
import weightedgpa.infinibiome.api.noise.base.SimplexNoise3D;
import weightedgpa.infinibiome.api.noise.util.InputWithScale;
import weightedgpa.infinibiome.api.resource.Timing;
import weightedgpa.infinibiome.api.resource.container.PreBlockPosData;
import weightedgpa.infinibiome.api.resource.container.BlockPosDataPostEntry;
import weightedgpa.infinibiome.api.noise.Noise;
import weightedgpa.infinibiome.api.noise.util.Range;
import weightedgpa.infinibiome.api.pos.BlockPos2D;
import net.minecraft.util.math.BlockPos;
import weightedgpa.infinibiome.api.generators.Seed;
import weightedgpa.infinibiome.internal.command.IBDebugCommand;

final class Terrain3DGen implements BlockPosDataPostEntry {
    private static final Range HEIGHT_FADING_RANGE = Range.init(60, 70);
    private static final Range SLOPE_FADING_RANGE = Range.init(1.25f, 1.50f);
    
    private final Noise<BlockPos> base;
    private final Noise<BlockPos2D> baseThreshold;
    
    private final PreBlockPosData preBlockPosData;

    Terrain3DGen(Seed seed, PreBlockPosData preBlockPosData) {
        seed = seed.newBranch("terrain3D");

        this.preBlockPosData = preBlockPosData;
        this.base = SimplexNoise3D.init(seed, 20, 10).mapInput(p -> InputWithScale.init(p, 1));
        this.baseThreshold = Const.init(0.35f);

        IBDebugCommand.INSTANCE.addSubcommand(
            SLOPE,
            p -> MiscHelper.getAverageSlope(p.toBlockPos2D(), 10, preBlockPosData)
        );
    }

    @Override
    public Timing getDataOutputModifierTiming() {
        return DefaultTiming.Terrain3D;
    }

    @Override
    public void modifyDataOutput(BlockPos2D pos, BlockPosDataOutput previousDataOutput) {
        final int mappedHeight = Math.round(previousDataOutput.getItem(DefaultKeys.MAPPED_HEIGHT));

        if (mappedHeight < HEIGHT_FADING_RANGE.getMin()){
            return;
        }

        float slope = MiscHelper.getAverageSlope(pos, 10, this.preBlockPosData);

        if (slope < SLOPE_FADING_RANGE.getMin()){
            return;
        }
        
        if (pos.getX() % 256 == 0 && pos.getZ() % 256 == 0) {
            System.out.println(pos);
        }

        final float threshold = this.getThreshold(pos, slope);
        
        for (int y = (int)HEIGHT_FADING_RANGE.getMin(); y <= mappedHeight; y++){
            final BlockPos pos3D = pos.toBlockPos3D(y);
            
            if (this.gapAtHeight(pos3D, threshold)){
                previousDataOutput.setGap(y);
            }
        }


    }
    
    private float getThreshold(BlockPos2D pos, float slope){
        final float result = this.baseThreshold.getOutput(pos);
        
        if (SLOPE_FADING_RANGE.hasValue(slope)){
            return result * SLOPE_FADING_RANGE.toPercent(slope);
        }
        return result;
    }
    
    private boolean gapAtHeight(BlockPos pos, float threshold){
        if (HEIGHT_FADING_RANGE.hasValue(pos.getY())){
            threshold *= HEIGHT_FADING_RANGE.toPercent(pos.getY());
        }
        
        final float base = this.base.getOutputAsPercent(pos);
        
        return base < threshold;
    }
}*/