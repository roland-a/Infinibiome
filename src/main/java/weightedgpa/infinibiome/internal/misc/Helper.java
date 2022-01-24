package weightedgpa.infinibiome.internal.misc;

import com.google.common.collect.Lists;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;
import weightedgpa.infinibiome.api.generators.Seed;
import weightedgpa.infinibiome.api.pos.IntPosInfo;
import weightedgpa.infinibiome.internal.floatfunc.generators.PerlinNoise;
import weightedgpa.infinibiome.internal.floatfunc.FloatFunc;
import weightedgpa.infinibiome.api.pos.BlockPos2D;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.*;
import java.util.stream.LongStream;

public final class Helper {
    public static final double COMMON_SCALE = 2048;

    private Helper(){}

    public static FloatFunc<BlockPos2D> initUniformNoise(Seed seed, double scale){
        return new PerlinNoise<>(seed, scale, BlockPos2D.INFO)
            .toUniform(
                PerlinNoise.PERCENTILE_TABLE
            );
    }

    public static <I> boolean passesSurroundingTest(I center, int radius, Predicate<I> condition, IntPosInfo<I> posInfo){
        assert radius >= 0: radius;

        if (radius == 0) return condition.test(center);

        for (I placeToCheck: getSearchPos(center, radius, posInfo)){
            if (!condition.test(placeToCheck)){
                return false;
            }
        }
        return true;
    }

    @Nullable
    public static <I> I findSuitableSpot(I center, int radius, Predicate<I> condition, IntPosInfo<I> posInfo){
        assert radius > 0: radius;

        for (I placeToCheck: getSearchPos(center, radius, posInfo)){
            if (condition.test(placeToCheck)){
                return placeToCheck;
            }
        }
        return null;
    }

    private static final double COS_ONE_FORTH_PI = StrictMath.cos(Math.PI/4);

    private static <I> List<I> getSearchPos(I center, int radius, IntPosInfo<I> posInfo){
        assert radius >= 0;

        int diagonal = (int)Math.round(radius * COS_ONE_FORTH_PI);

        return Lists.newArrayList(
            center,
            posInfo.offset(center, radius, 0),
            posInfo.offset(center, -radius, 0),
            posInfo.offset(center, 0, radius),
            posInfo.offset(center, 0, -radius),

            posInfo.offset(center, diagonal, diagonal),
            posInfo.offset(center, diagonal, -diagonal),
            posInfo.offset(center, -diagonal, diagonal),
            posInfo.offset(center, -diagonal, -diagonal)
        );
    }
    
    @Nullable
    public static <T> T timed(int seconds, Supplier<? extends T> supplier){
        assert seconds > 0;

        ExecutorService thread = Executors.newSingleThreadExecutor();

        Future<T> result = thread.submit(supplier::get);

        try {
            return result.get(seconds, TimeUnit.SECONDS);
        }
        catch (TimeoutException e){
            return null;
        }
        catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public static DataInput getResource(String resourceName){
        return new DataInputStream(
            Helper.class.getResourceAsStream(resourceName)
        );
    }

    public static <T> boolean intersects(Collection<T> list1, Collection<T> list2){
        for (T a: list1){
            for (T b: list2){
                if (a.equals(b)) return true;
            }
        }
        return false;
    }



    public static long packXZ(int x, int z){
        return (((long)x) << 32) | (z & 0xffffffffL);
    }

    public static int getIntX(long mergedXZ){
        return (int)(mergedXZ >> 32);
    }

    public static int getIntZ(long mergedXZ){
        return (int)mergedXZ;
    }

    public static void iterXZParallel(int width, int height, BiConsumer<Integer, Integer> consumer){
        assert width > 0;
        assert height > 0;

        long total = (long)width * height;

        LongStream.range(0, total)
            .parallel()
            .forEach(
                i -> consumer.accept(
                    (int)(i / height),
                    (int)(i % height)
                )
            );
    }

    public static <T> void strictSort(List<T> list, Comparator<T> comparator){
        list.sort(comparator);

        for (int i = 0; i < list.size() - 1; i++){
            T current = list.get(i);
            T next = list.get(i + 1);

            if (comparator.compare(current, next) == 0){
                throw new IllegalStateException("objects are not allowed to have equal precedent");
            }
        }
    }

    public static <T> List<T> shuffle(List<T> list, Random random){
        List<T> result = new ArrayList<>(list);

        Collections.shuffle(result, random);

        return result;
    }

    public static void placeClusterWithUnknownHeight(BlockPos centerPos, FloatFunc<BlockPos2D> radiusNoise, IWorldReader world, Consumer<BlockPos> placeAt) {
        int centerHeight = centerPos.getY();

        BlockPos2D centerPos2D = MCHelper.to2D(centerPos);

        int radiusMax = MathHelper.ceil(radiusNoise.getOutputInterval().getMax());

        for (int x = -radiusMax; x <= radiusMax; x++){
            for (int z = -radiusMax; z <= radiusMax; z++){
                BlockPos2D currentPos2D = centerPos2D.offset(x,  z);

                MutableInt currentHeight = new MutableInt(centerHeight);

                forEachPathBetween(
                    centerPos2D,
                    currentPos2D,
                    p -> {
                        Integer newHeight = adjustHeight(
                            p,
                            currentHeight.getValue(),
                            world
                        );

                        if (newHeight == null) return false;

                        currentHeight.setValue(
                            newHeight
                        );

                        return true;
                    }
                );

                BlockPos currentPos = currentPos2D.to3D(currentHeight.getValue());

                placeAt.accept(currentPos);
            }
        }
    }

    private static void forEachPathBetween(BlockPos2D from, BlockPos2D to, Predicate<BlockPos2D> consumer){
        BlockPos2D currPos = from;

        while (!currPos.equals(to)){
            boolean continueFlag = consumer.test(currPos);

            if (!continueFlag) return;

            int xDirection = Integer.signum(to.getBlockX() - from.getBlockX());
            int zDirection = Integer.signum(to.getBlockZ() - from.getBlockZ());

            currPos = currPos.offset(xDirection, zDirection);
        }
    }

    @Nullable
    private static Integer adjustHeight(BlockPos2D currentPos, int centerHeight, IWorldReader world){
        BlockState initialBlock = world.getBlockState(currentPos.to3D(centerHeight));

        int scanDirection;

        if (MCHelper.isSolid(initialBlock)) {
            scanDirection = 1;
        } else {
            scanDirection = -1;
        }

        int scannedHeight = centerHeight;

        while (scannedHeight >= 0 && scannedHeight <= 255){
            BlockPos currPos = currentPos.to3D(scannedHeight);

            if (
                MCHelper.isMostlyAir(world.getBlockState(currPos)) &&
                MCHelper.isSolid(world.getBlockState(currPos.down()))
            ){
                return scannedHeight;
            }

            scannedHeight += scanDirection;
        }
        return null;
    }

    public static <T> void set(Object o, Field field, T newValue) {
        field.setAccessible(true);

        try {
            field.set(o, newValue);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T pickWeighted(Function<T, Double> toWeight, Random random, List<T> list){
        assert !list.isEmpty();

        List<T> possible = new ArrayList<>();

        double total = 0;

        for (T t: list){
            total += toWeight.apply(t);

            possible.add(t);
        }

        if (total == 0){
            int randomIndex = random.nextInt(possible.size());

            return possible.get(randomIndex);
        }

        double randomValue = MathHelper.lerp(
            random.nextDouble(),
            0,
            total
        );

        double cummulative = 0;

        for (T t: possible){
            cummulative += toWeight.apply(t);

            if (cummulative >= randomValue) return t;
        }
        throw new RuntimeException("should never happen");
    }
}
