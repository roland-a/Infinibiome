package weightedgpa.infinibiome.internal.generators.utils.condition;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import weightedgpa.infinibiome.api.pos.BlockPos2D;
import weightedgpa.infinibiome.api.pos.InterChunkPos;
import weightedgpa.infinibiome.internal.misc.MCHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public final class ConditionList {
    private final List<Condition> all = new ArrayList<>();

    public ConditionList(List<Condition> conditions){
        conditions.forEach(this::addCondition);

        checkOverlap();
    }

    public ConditionList(Condition... conditions){
        this(Arrays.asList(conditions));
    }

    private void addCondition(Condition c){
        if (c.isSlow()){
            all.add(c);
        }
        else {
            all.add(0, c);
        }
    }

    private void checkOverlap(){
        for (int i= 0; i < all.size(); i++){
            for (int j = 0; j < all.size(); j++){
                if (i == j) break;

                Condition c1 = all.get(i);
                Condition c2 = all.get(j);

                if (c1.likelyConflicts(c2) || c2.likelyConflicts(c1)){
                    throw new RuntimeException(
                        c1 + " "  + c2 + " conflicts"
                    );
                }
            }
        }
    }

    public ConditionList add(
        Condition... conditions
    ){
        return add(Arrays.asList(conditions));
    }

    public ConditionList add(
        Collection<Condition> conditions
    ){
        List<Condition> result = new ArrayList<>();

        result.addAll(all);
        result.addAll(conditions);

        return new ConditionList(
            result
        );
    }

    public double getAllProbability(InterChunkPos pos, StrictOption strictOption){
        return getAllProbability(
            pos.getLowestChunkPos(),
            strictOption
        );
    }

    public double getAllProbability(ChunkPos pos, StrictOption strictOption){
        double probability = 1;

        for (Condition c: all){
            probability *= strictOption.getProbability(c, pos);

            if (probability == 0) return 0;
        }

        return probability;
    }

    public double getAllProbability(BlockPos2D pos, StrictOption strictOption){
        double probability = 1;

        for (Condition c: all){
            probability *= strictOption.getProbability(c, pos);

            if (probability == 0) return 0;
        }

        return probability;
    }

    public String _debug(BlockPos2D pos){
        String result = "";

        double probability = 1;

        for (Condition c: all){
            result += "{\n";

            double multiplier = c.getProbability(pos);

            probability *= multiplier;

            result += c.toString() + "\n";
            result += "multiplier: " + multiplier + "\n";
            result += "isSlow: " + c.isSlow() + "\n";

            result += "}\n";

            //if (probability == 0) return 0;
        }

        result += probability;

        return result;
    }

    public boolean canBeHere(BlockPos2D p) {
        return getAllProbability(p, StrictOption.USE_LIKE_NON_STRICT) > 0;
    }

    public boolean canBeHere(InterChunkPos p) {
        return getAllProbability(p, StrictOption.USE_LIKE_NON_STRICT) > 0;
    }


    public enum StrictOption {
        USE_LIKE_NON_STRICT {
            @Override
            double getProbability(Condition c, BlockPos2D pos) {
                return c.getProbability(pos);
            }

            @Override
            double getProbability(Condition c, ChunkPos pos) {
                return c.getProbability(pos);
            }
        },
        FOUR_CORNER_CHECK {
            @Override
            double getProbability(Condition c, BlockPos2D pos) {
                if (!c.isStrict()) return c.getProbability(pos);

                double result = 0;

                for (int x: new int[]{0,16}){
                    for (int z: new int[]{0,16}){
                        double multiplier = c.getProbability(pos.offset(x, z));

                        if (multiplier == 0) return 0;

                        result += multiplier;
                    }
                }

                return result / 4;
            }

            @Override
            double getProbability(Condition c, ChunkPos pos) {
                if (!c.isStrict()) return c.getProbability(pos);

                double result = 0;

                for (int x: new int[]{0,1}){
                    for (int z: new int[]{0,1}){
                        double multiplier = c.getProbability(
                            new ChunkPos(
                                pos.x + x,
                                pos.z + z
                            )
                        );

                        if (multiplier == 0) return 0;

                        result += multiplier;
                    }
                }

                return result / 4;
            }
        },
        EXCLUDE{
            @Override
            double getProbability(Condition c, BlockPos2D pos) {
                if (!c.isStrict()) return 1;

                return c.getProbability(pos);
            }

            @Override
            double getProbability(Condition c, ChunkPos pos) {
                if (!c.isStrict()) return 1;

                return c.getProbability(pos);
            }
        },
        ONLY{
            @Override
            double getProbability(Condition c, BlockPos2D pos) {
                if (c.isStrict()) return 1;

                return c.getProbability(pos);
            }

            @Override
            double getProbability(Condition c, ChunkPos pos) {
                if (c.isStrict()) return 1;

                return c.getProbability(pos);
            }
        };


        abstract double getProbability(Condition c, BlockPos2D pos);

        abstract double getProbability(Condition c, ChunkPos pos);
    }
}
