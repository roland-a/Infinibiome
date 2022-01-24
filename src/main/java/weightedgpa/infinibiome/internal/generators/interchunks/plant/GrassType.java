package weightedgpa.infinibiome.internal.generators.interchunks.plant;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;

import java.util.List;
import java.util.function.Supplier;

public enum GrassType {
    SHORT_CLUSTERED(true, true),
    SHORT_SCATTERED(false, true),
    TALL_CLUSTERED(true, false),
    TALL_SCATTERED(false, false),;

    final boolean isClustered;
    final boolean isShort;

    GrassType(boolean isClustered, boolean isShort) {
        this.isClustered = isClustered;
        this.isShort = isShort;
    }

    public <T> T clusteredOrScattered(
        Supplier<T> setClustered,
        Supplier<T> setScattered
    ){
        if (isClustered){
            return setClustered.get();
        }
        return setScattered.get();
    }

    public void getShortOrTall(
        Runnable getShort,
        Runnable getTall
    ){
        if (isShort){
            getShort.run();
        }
        else {
            getTall.run();
        }
    }

    public <T> T getShortOrTall(
        Supplier<T> getShort,
        Supplier<T> getTall
    ){
        if (isShort){
            return getShort.get();
        }
        return getTall.get();
    }

    public List<BlockState> shortOrTallPlantBlocks(
        Block shortPlant,
        Block tallPlant
    ){
        if (isShort){
            return ImmutableList.of(shortPlant.getDefaultState());
        }
        return PlantHelper.initDouble(tallPlant);
    }
}
