package weightedgpa.infinibiome.internal.generators.interchunks.tree;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.server.ServerWorld;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.generators.*;
import weightedgpa.infinibiome.api.pos.BlockPos2D;
import weightedgpa.infinibiome.api.pos.InterChunkPos;
import weightedgpa.infinibiome.api.posdata.PosDataProvider;
import weightedgpa.infinibiome.internal.floatfunc.FloatFunc;
import weightedgpa.infinibiome.internal.floatfunc.generators.RandomGen;
import weightedgpa.infinibiome.internal.floatfunc.util.Interval;
import weightedgpa.infinibiome.internal.minecraftImpl.commands.DebugCommand;
import weightedgpa.infinibiome.internal.minecraftImpl.world.ChangeHoldingWorld;
import weightedgpa.infinibiome.internal.minecraftImpl.world.SimulatedWorld;
import weightedgpa.infinibiome.internal.misc.MCHelper;
import weightedgpa.infinibiome.internal.misc.MathHelper;

import java.util.*;

public final class TreeGens implements InterChunkGen {
    private static final int MAX_RETRIES = 16;

    private final RandomGen randomProducer;

    private final List<TreeGen> entries;

    public TreeGens(DependencyInjector di){
        Seed seed = di.get(Seed.class).newSeed(Infinibiome.MOD_ID + ":treeGens");

        this.randomProducer = new RandomGen(seed);

        this.entries = di.getAll(TreeGen.class);

        DebugCommand.registerDebugFunc(
            "tree",
            "density",
            p -> {
                InterChunkPos interChunkPos = new InterChunkPos(p);

                return String.valueOf(
                    new TreeDensityCounter(
                        new SimulatedWorld(di).simulateInterchunks(interChunkPos),
                        interChunkPos
                    ).getCurrentDensity()
                );
            }
        );

        DebugCommand.registerDebugFunc(
            "treeDensityCounter",
            "debug",
            p -> {
                InterChunkPos interChunkPos = new InterChunkPos(p);

                return new TreeDensityCounter(
                    new SimulatedWorld(di).simulateInterchunks(interChunkPos),
                    interChunkPos
                ).debugInner();
            }
        );

        DebugCommand.registerDebugFunc(
            "tree",
            "funcDensity",
            p -> String.valueOf(
                this.getApproxDensity(new InterChunkPos(p))
            )
        );
    }

    @Override
    public Timing getInterChunkTiming() {
        return InterChunkGenTimings.TREES;
    }

    public double getApproxDensity(InterChunkPos pos){
        double result = 0;

        for (TreeGen treeGen : entries){
            result += treeGen.getDensity(pos);
        }

        if (result > 0.9f){
            return 0.9f;
        }

        return result;
    }

    @Override
    public void generate(InterChunkPos interChunkPos, IWorld world){
        Random random = randomProducer.getRandom(interChunkPos.getX(), interChunkPos.getZ());

        Map<TreeGen, DensityInfo> treesRemaining = getTreesToGenerate(interChunkPos);

        TreeDensityCounter wrapper1 = new TreeDensityCounter(world, interChunkPos);

        double currentTreeDensity = wrapper1.getCurrentDensity();

        while (!treesRemaining.isEmpty()){
            boolean continuePlacingTree = false;

            TreeGen tree = pickRandomRemaining(treesRemaining, random);

            DensityInfo treeDensityInfo = treesRemaining.get(tree);

            //tries to generate one tree
            for (int i = 0; i < MAX_RETRIES; i++){
                BlockPos treePos = getRandomTreePos(interChunkPos, world, random);

                ChangeHoldingWorld wrapper2 = new ChangeHoldingWorld(wrapper1);

                tree.generate(treePos, wrapper2);

                if (!wrapper2.anyChange()) {
                    //if maximum attempts exceeded while it cant place any trees, then should no longer place any more trees
                    continuePlacingTree = false;

                    //try again if the tree couldn't generate
                    continue;
                }

                double newTreeDensity = wrapper1.getCurrentDensity();

                double gain = newTreeDensity - currentTreeDensity;

                treeDensityInfo.addGain(gain);

                //wont load the tree if theres already too many trees
                if (treeDensityInfo.tooManyTrees(random)) {
                    continuePlacingTree = false;
                    break;
                }

                wrapper2.loadChange();

                currentTreeDensity = newTreeDensity;

                continuePlacingTree = true;

                break;
            }

            if (!continuePlacingTree){
                treesRemaining.remove(tree);
            }
        }
    }

    private TreeGen pickRandomRemaining(Map<TreeGen, ?> remainingTrees, Random random){
        List<TreeGen> list = new ArrayList<>(remainingTrees.keySet());

        return list.get(random.nextInt(list.size()));
    }

    private BlockPos getRandomTreePos(InterChunkPos pos, IWorld world, Random random){
        BlockPos2D treePos2D = pos.getRandomCenterPos(random);

        int treeHeight = MCHelper.getHighestTerrainHeight(treePos2D, world) + 1;

        return treePos2D.to3D(treeHeight);
    }

    private Map<TreeGen, DensityInfo> getTreesToGenerate(InterChunkPos pos){
        Map<TreeGen, DensityInfo> result = new HashMap<>();

        double cumulative = 0;

        for (TreeGen tree: entries){
            double density = tree.getDensity(pos);

            if (density == 0) continue;

            cumulative += density;

            result.put(
                tree,
                new DensityInfo(density)
            );
        }

        if (cumulative > 0.95){
            for (Map.Entry<TreeGen, DensityInfo> entry : new HashSet<>(result.entrySet())) {
                result.put(
                    entry.getKey(),
                    entry.getValue().fix(cumulative, 0.95)
                );
            }
        }

        return result;
    }

    private static class DensityInfo {
        final double maxDensity;

        double currDensity = 0;
        int count = 0;

        DensityInfo(double density){
            maxDensity = density;
        }

        DensityInfo fix(double cumulative, double maxCummulative){
            return new DensityInfo(
                (maxDensity / cumulative) * maxCummulative
            );
        }

        void addGain(double gain){
            currDensity += gain;
            count++;
        }

        boolean tooManyTrees(Random random){
            if (currDensity == 0) return false;

            double remaining = maxDensity - currDensity;

            double chance = remaining / getAvgTreeDensity();

            chance = Interval.PERCENT.clamp(chance);

            return !MathHelper.randomBool(chance, random);
        }

        private double getAvgTreeDensity(){
            return currDensity / count;
        }

        @Override
        public String toString() {
            return "Density{" +
                "currDensity=" + currDensity +
                ", maxDensity=" + maxDensity +
                '}';
        }
    }


    class Config{
        private FloatFunc<BlockPos2D> height;


    }
}

