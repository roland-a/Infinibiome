package weightedgpa.infinibiome.internal.generators.interchunks.mob;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.pos.InterChunkPos;
import weightedgpa.infinibiome.internal.generators.utils.GenHelper;

import java.lang.reflect.Field;

import static weightedgpa.infinibiome.api.posdata.PosDataHelper.*;
import static weightedgpa.infinibiome.internal.generators.utils.condition.ConditionHelper.*;

//spawns in cold climates
public final class FoxGen extends MobGenBase {
    private static final Field FOX_TYPE_PARAM = ObfuscationReflectionHelper.findField(
        FoxEntity.class,
        "field_213523_bz"
    );
    private DataParameter<Integer> key;

    public FoxGen(DependencyInjector di) {
        super(di, Infinibiome.MOD_ID + ":fox");

        config = initConfig()
            .getEntity(
                this::getEntity
            )
            .setGroupCount(1)
            .setBabyChance(MobHelper.COMMON_BABY_RATE)
            .alwaysAboveWater()
            .neverInMushroomIsland()
            .anyNonHighSlope()
            .onlyOnNonBeachLand()
            .setTemperature(
                GenHelper.COLDISH
            )
            .anyHumidityIncludingDesert()
            .setChancePerChunk(1/200d)
            .addExtraConditions(
                onlyIfNotNear(
                    di,
                    2,
                    MobGenBase::canSpawnAtInterChunk,
                    WolfGen.class,
                    PolarBearGen.class
                )
            );
    }

    private AnimalEntity getEntity(BlockPos mobPos, InterChunkPos interChunkPos, IWorld world) {
        FoxEntity fox = new FoxEntity(EntityType.FOX, world.getWorld());

        if (key == null){
            try{
                key = (DataParameter<Integer>)FOX_TYPE_PARAM.get(fox);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        double temp = getTemperature(mobPos, posData);

        if (FREEZE_INTERVAL.contains(temp)) {
            fox.getDataManager()
                .set(
                    key,
                    1
                );
        }

        return fox;
    }

}
