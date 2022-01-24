package weightedgpa.infinibiome.internal.pos;

import weightedgpa.infinibiome.internal.misc.MathHelper;

import java.util.Random;
import java.util.function.BiFunction;

public final class PosHelper {
    private PosHelper(){}

    public static boolean contains(int lowestX, int lowestZ, int size, double posX, double posZ){
        int highestX = lowestX + size;
        int highestZ = lowestZ + size;

        if (posX < lowestX){
            return false;
        }
        if (posZ < lowestZ){
            return false;
        }
        if (posX >= highestX){
            return false;
        }
        if (posZ >= highestZ){
            return false;
        }
        return true;
    }

    public static <I> I random(int lowestX, int lowestZ, int size, Random random, BiFunction<Integer, Integer, I> toPos){
        int highestX = lowestX + size;
        int highestZ = lowestZ + size;

        return toPos.apply(
            MathHelper.randomInt(
                lowestX, highestX, random
            ),
            MathHelper.randomInt(
                lowestZ, highestZ, random
            )
        );
    }
}
