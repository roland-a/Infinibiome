package weightedgpa.infinibiome.internal.minecraftImpl;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class ChunkDataWriter {
    private ChunkDataWriter(){}

    private static final Map<ChunkPos, CompoundNBT> dataLoaded = new HashMap<>();
    private static final Map<ChunkPos, List<Consumer<CompoundNBT>>> dataToBeSaved = new HashMap<>();

    public static CompoundNBT readChunk(ChunkPos chunkPos){
        if (!dataLoaded.containsKey(chunkPos)){
            dataLoaded.put(chunkPos, new CompoundNBT());
        }

        CompoundNBT result = dataLoaded.get(chunkPos);

        modify(chunkPos, result);

        return result;
    }

    public static void write(ChunkPos chunkPos, Consumer<CompoundNBT> func){
        dataToBeSaved.computeIfAbsent(
            chunkPos,
            __ -> new ArrayList<>()
        )
        .add(
            func
        );
    }

    private static void modify(ChunkPos chunkPos, CompoundNBT nbt){
        List<Consumer<CompoundNBT>> list = dataToBeSaved.computeIfAbsent(
            chunkPos,
            __ -> new ArrayList<>()
        );

        for (Consumer<CompoundNBT> func: list){
            func.accept(nbt);
        }
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkDataEvent.Load e){
        dataLoaded.put(
            e.getChunk().getPos(),
            e.getData()
        );
    }

    @SubscribeEvent
    public static void onChunkSave(ChunkDataEvent.Save e){
        modify(
            e.getChunk().getPos(),
            e.getData()
        );

        dataToBeSaved.remove(e.getChunk().getPos());
        dataLoaded.remove(e.getChunk().getPos());
    }
}
