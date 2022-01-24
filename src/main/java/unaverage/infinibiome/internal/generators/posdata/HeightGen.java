package weightedgpa.infinibiome.internal.generators.posdata;

import net.minecraft.block.BlockState;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.generators.ClimateConfig;
import weightedgpa.infinibiome.api.generators.PosDataTimings;
import weightedgpa.infinibiome.api.generators.Seed;
import weightedgpa.infinibiome.api.pos.BlockPos2D;
import weightedgpa.infinibiome.api.posdata.*;
import weightedgpa.infinibiome.internal.floatfunc.FloatFunc;
import weightedgpa.infinibiome.internal.floatfunc.generators.PerlinNoise;
import weightedgpa.infinibiome.internal.floatfunc.modifiers.IntervalMapperWrapper;
import weightedgpa.infinibiome.internal.floatfunc.modifiers.SeamlessGrid;
import weightedgpa.infinibiome.internal.floatfunc.util.Interval;
import weightedgpa.infinibiome.internal.misc.Helper;
import weightedgpa.infinibiome.internal.misc.MCHelper;
import weightedgpa.infinibiome.internal.misc.MathHelper;
import weightedgpa.infinibiome.internal.misc.PregeneratedSeamlessGrid;

import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

public final class HeightGen extends DataGeneratorBase {
   private final Land land;
   private final Beach beach;
   private final Ocean ocean;

   private final ClimateConfig climateConfig;


   public HeightGen(DependencyInjector di) {
      super(di, "infinibiome:heightGen", PosDataTimings.HEIGHT);
      this.ocean = new Ocean(this.seed);
      this.beach = new Beach(this.seed);
      this.land = new Land(this.seed);

      this.climateConfig = di.get(ClimateConfig.class);
   }

   public void generateData(PosDataTable data) {
      LandmassInfo landmassType = data.get(PosDataKeys.LANDMASS_TYPE);

      if (landmassType.isLand()) {
         this.land.modifyDataOutput(data, landmassType.getTransitionToBeach());
      } else if (landmassType.isBeach()) {
         this.beach.modifyDataOutput(data, ((LandmassInfo.Beach)landmassType).getTransitionToLand());
      } else {
         assert landmassType.isOcean();
         this.ocean.modifyDataOutput(data, landmassType.getTransitionToBeach());
      }
   }

   class Land {
      private final Interval AMP = new Interval(5.0D, 100.0D);
      private final Interval BASE_HEIGHT_ABOVE_SEA = new Interval(0.0D, 20.0D);
      private final Interval TRANSITION_EASE = new Interval(2.0D, 10.0D);
      private final HeightMapProducer heightMapProducer;
      private final FloatFunc<BlockPos2D> transitionEase;

      Land(Seed seed) {
         seed = seed.newSeed("land");
         FloatFunc<BlockPos2D> ampFunc = this.initAmp(seed);
         this.heightMapProducer = (new HeightMapProducer(seed, FloatFunc.constFunc(63.0D), ampFunc)).setRelativeScale(this.initRelativeScale(seed, ampFunc))._setDebuggable("land");
         this.transitionEase = this.initEaseFunc(seed);
      }

      private FloatFunc<BlockPos2D> initBaseHeight(Seed seed) {
         seed = seed.newSeed("baseHeight");
         return Helper.initUniformNoise(seed, 2048.0D).skew(FloatFunc.constFunc(-5.0D)).mapInterval(this.BASE_HEIGHT_ABOVE_SEA.applyOp((v) -> {
            return v + (double)MCHelper.WATER_HEIGHT;
         }));
      }

      private FloatFunc<BlockPos2D> initAmp(Seed seed) {
         seed = seed.newSeed("amp");
         return Helper.initUniformNoise(seed, 4096.0D).skew(FloatFunc.constFunc(-20.0D)).mapInterval(this.AMP);
      }

      private FloatFunc<BlockPos2D> initRelativeScale(Seed seed, final FloatFunc<BlockPos2D> ampFunc) {
         seed = seed.newSeed("relativeScale");
         final FloatFunc<BlockPos2D> scalePercent = Helper.initUniformNoise(seed, 2048.0D);
         scalePercent.mapInterval(Interval.PERCENT)._setDebuggable("landScale", "scalePercent", (t, p) -> {
            SeamlessGrid var10000 = PregeneratedSeamlessGrid.TABLE_256_256;
            t.getClass();
            return var10000._debugValue(p, t::getOutput);
         });
         (new FloatFunc<BlockPos2D>() {
            public double getOutput(BlockPos2D input) {
               return Land.this.getRelativeScaleMin(ampFunc.getOutput(input));
            }
         })._setDebuggable("landScale", "min", (t, p) -> {
            SeamlessGrid var10000 = PregeneratedSeamlessGrid.TABLE_256_256;
            t.getClass();
            return var10000._debugValue(p, t::getOutput);
         });
         (new FloatFunc<BlockPos2D>() {
            public double getOutput(BlockPos2D input) {
               return Land.this.getRelativeScaleMax(ampFunc.getOutput(input));
            }
         })._setDebuggable("landScale", "max", (t, p) -> {
            SeamlessGrid var10000 = PregeneratedSeamlessGrid.TABLE_256_256;
            t.getClass();
            return var10000._debugValue(p, t::getOutput);
         });
         ampFunc._setDebuggable("landScale", "amp", (t, p) -> {
            SeamlessGrid var10000 = PregeneratedSeamlessGrid.TABLE_256_256;
            t.getClass();
            return var10000._debugValue(p, t::getOutput);
         });
         return (new FloatFunc<BlockPos2D>() {
            public double getOutput(BlockPos2D input) {
               double amp = ampFunc.getOutput(input);
               Interval interval = new Interval(Land.this.getRelativeScaleMin(amp), Land.this.getRelativeScaleMax(amp));
               return scalePercent.getOutputInterval().mapInterval(scalePercent.getOutput(input), interval);
            }

            public Interval getOutputInterval() {
               return new Interval(1.0D, 15.0D);
            }
         })._setDebuggable("landScale", "scale", (t, p) -> {
            SeamlessGrid var10000 = PregeneratedSeamlessGrid.TABLE_256_256;
            t.getClass();
            return var10000._debugValue(p, t::getOutput);
         });
      }

      private double getRelativeScaleMin(double amp) {
         return -0.042105263157894736D * amp + 5.2105263157894735D;
      }

      private double getRelativeScaleMax(double amp) {
         return -0.1368421052631579D * amp + 15.68421052631579D;
      }

      private FloatFunc<BlockPos2D> initEaseFunc(Seed seed) {
         seed = seed.newSeed("ease");
         return Helper.initUniformNoise(seed, 2048.0D).mapInterval(this.TRANSITION_EASE)._setDebuggable("landmass", "landTransitionEase", (p) -> {
            return p;
         });
      }

      void modifyDataOutput(PosDataTable dataTable, double transitionToBeach) {
         MathHelper.ease(transitionToBeach, this.transitionEase.getOutput(dataTable.getPos()));
         dataTable.set(PosDataKeys.MAPPED_HEIGHT, this.getHeight(dataTable.getPos(), transitionToBeach));
         dataTable.set(PosDataKeys.AMP, this.getAmp(dataTable.getPos(), transitionToBeach));
         dataTable.set(PosDataKeys.GROUND_BLOCKS, () -> {
            return this.getLandGroundBlocks(dataTable);
         });
      }

      private List<BlockState> getLandGroundBlocks(PosDataTable data) {
         double humidity = ((ClimateValue)data.get(PosDataKeys.HUMIDITY)).fromHeight(data.get(PosDataKeys.MAPPED_HEIGHT));
         humidity = PosDataHelper.fuzzHumidity(humidity, data.getPos(), climateConfig);
         return PosDataHelper.DRY_INTERVAL.contains(humidity) ? GroundBlocks.SAND.getSurfaceBlocks(data.getPos()) : GroundBlocks.DIRT.getSurfaceBlocks(data.getPos());
      }

      private double getHeight(BlockPos2D pos, double transitionToBeach) {
         double landHeight = this.heightMapProducer.getMappedHeight(pos);
         return transitionToBeach == 0.0D ? landHeight : MathHelper.lerp(transitionToBeach, landHeight, HeightGen.this.beach.getHeight(pos, 1.0D));
      }

      private double getAmp(BlockPos2D pos, double transitionToBeach) {
         double landAmp = this.heightMapProducer.getAmp(pos);
         return transitionToBeach == 0.0D ? landAmp : MathHelper.lerp(transitionToBeach, landAmp, HeightGen.this.beach.getAmp(pos));
      }
   }

   class Ocean {
      private final Interval BASE_HEIGHT_BELOW_SEA_LEVEl = new Interval(5.0D, 35.0D);
      private final Interval AMP = new Interval(-20.0D, -5.0D);
      private final Interval EASE = new Interval(2.0D, 10.0D);
      private final Interval TRANSITION_TO_DEEP_SEA;
      private final HeightMapProducer heightMapProducer;
      private final FloatFunc<BlockPos2D> easeFunc;

      Ocean(Seed seed) {
         this.TRANSITION_TO_DEEP_SEA = new Interval((double)(MCHelper.WATER_HEIGHT - 25), (double)(MCHelper.WATER_HEIGHT - 30));
         seed = seed.newSeed("ocean");
         this.heightMapProducer = (new HeightMapProducer(seed, this.initBaseHeight(seed), this.initAmp(seed)))._setDebuggable("ocean");
         this.easeFunc = this.initEaseFunc(seed);
      }

      private FloatFunc<BlockPos2D> initBaseHeight(Seed seed) {
         seed = seed.newSeed("baseHeight");
         return Helper.initUniformNoise(seed, 2048.0D).mapInterval(this.BASE_HEIGHT_BELOW_SEA_LEVEl.applyOp((n) -> {
            return (double)MCHelper.WATER_HEIGHT - n;
         }));
      }

      private FloatFunc<BlockPos2D> initAmp(Seed seed) {
         seed = seed.newSeed("amp");
         return Helper.initUniformNoise(seed, 2048.0D).mapInterval(this.AMP);
      }

      private FloatFunc<BlockPos2D> initEaseFunc(Seed seed) {
         seed = seed.newSeed("ease");
         return Helper.initUniformNoise(seed, 2048.0D).mapInterval(this.EASE)._setDebuggable("ocean", "transitionEase", (p) -> {
            return p;
         });
      }

      void modifyDataOutput(PosDataTable dataOutput, double transitionToBeach) {
         transitionToBeach = MathHelper.ease(transitionToBeach, this.easeFunc.getOutput(dataOutput.getPos()));
         dataOutput.set(PosDataKeys.MAPPED_HEIGHT, this.getHeight(dataOutput.getPos(), transitionToBeach));
         dataOutput.set(PosDataKeys.AMP, this.getAmp(dataOutput.getPos(), transitionToBeach));
         dataOutput.set(PosDataKeys.GROUND_BLOCKS, () -> {
            return this.getGroundBlocks(dataOutput);
         });
      }

      private double getHeight(BlockPos2D pos, double transitionToBeach) {
         double oceanHeight = this.heightMapProducer.getMappedHeight(pos);
         return transitionToBeach == 0.0D ? oceanHeight : MathHelper.lerp(transitionToBeach, oceanHeight, HeightGen.this.beach.getHeight(pos, 0.0D));
      }

      private double getAmp(BlockPos2D pos, double transitionToBeach) {
         double oceanAmp = this.heightMapProducer.getAmp(pos);
         return transitionToBeach == 0.0D ? oceanAmp : MathHelper.lerp(transitionToBeach, oceanAmp, HeightGen.this.beach.getAmp(pos));
      }

      private List<BlockState> getGroundBlocks(PosDataTable posDataTable) {
         double height = posDataTable.get(PosDataKeys.MAPPED_HEIGHT);
         if (height > this.TRANSITION_TO_DEEP_SEA.getMax()) {
            return HeightGen.this.beach.getGroundBlocks(posDataTable.getPos());
         } else if (height < this.TRANSITION_TO_DEEP_SEA.getMin()) {
            return GroundBlocks.GRAVEL.getSurfaceBlocks(posDataTable.getPos());
         } else {
            double chanceAsDeepSea = 1.0D - this.TRANSITION_TO_DEEP_SEA.mapInterval(height, Interval.PERCENT);
            Random random = HeightGen.this.randomGen.getRandom(posDataTable.getPos().getBlockX(), posDataTable.getPos().getBlockZ());
            return MathHelper.randomBool(chanceAsDeepSea, random) ? GroundBlocks.GRAVEL.getSurfaceBlocks(posDataTable.getPos()) : HeightGen.this.beach.getGroundBlocks(posDataTable.getPos());
         }
      }
   }

   class Beach {
      private final Interval BEACH_HEIGHT = new Interval(0.0D, 5.0D);
      private final Interval EASE = new Interval(2.0D, 10.0D);
      private final FloatFunc<BlockPos2D> heightAboveSeaFunc;
      private final Predicate<BlockPos2D> isGravelBeachFunc;
      private final Predicate<BlockPos2D> isStoneBeachFunc;
      private final FloatFunc<BlockPos2D> easeFunc;

      Beach(Seed seed) {
         seed = seed.newSeed("beach");
         this.heightAboveSeaFunc = this.initHeightAboveSeaFunc(seed);
         this.easeFunc = this.initEaseFunc(seed);
         this.isGravelBeachFunc = this.beachTypeTemplate(seed.newSeed("gravelBeach"));
         this.isStoneBeachFunc = this.beachTypeTemplate(seed.newSeed("stoneBeach"));
      }

      private FloatFunc<BlockPos2D> initHeightAboveSeaFunc(Seed seed) {
         seed = seed.newSeed("heightAboveSea");
         return (new PerlinNoise(seed, 2048.0D, BlockPos2D.INFO)).toUniform(PerlinNoise.PERCENTILE_TABLE).mapInterval(this.BEACH_HEIGHT)._setDebuggable("beach", "heightAboveSea", (p) -> {
            return p;
         });
      }

      private FloatFunc<BlockPos2D> initEaseFunc(Seed seed) {
         seed = seed.newSeed("ease");
         return (new PerlinNoise(seed, 2048.0D, BlockPos2D.INFO)).toUniform(PerlinNoise.PERCENTILE_TABLE).mapInterval(this.EASE)._setDebuggable("beach", "ease", (p) -> {
            return p;
         });
      }

      private Predicate<BlockPos2D> beachTypeTemplate(Seed seed) {
         seed = seed.newSeed("beachType");
         return (new IntervalMapperWrapper((new PerlinNoise(seed, 2048.0D, BlockPos2D.INFO)).toUniform(PerlinNoise.PERCENTILE_TABLE))).addBranch(new Interval(0.0D, 0.05000000074505806D), 1.0D, 1.0D).addBranch(new Interval(0.05000000074505806D, 0.05999999865889549D), 1.0D, 0.0D).randomBool(BlockPos2D.INFO, seed);
      }

      void modifyDataOutput(PosDataTable dataTable, double transitionToLand) {
         transitionToLand = MathHelper.ease(transitionToLand, this.easeFunc.getOutput(dataTable.getPos()));
         dataTable.set(PosDataKeys.MAPPED_HEIGHT, this.getHeight(dataTable.getPos(), transitionToLand));
         dataTable.set(PosDataKeys.AMP, this.getAmp(dataTable.getPos()));
         dataTable.set(PosDataKeys.GROUND_BLOCKS, () -> {
            return this.getGroundBlocks(dataTable.getPos());
         });
      }

      double getHeight(BlockPos2D pos, double percentTowardsLand) {
         return MathHelper.lerp(percentTowardsLand, (double) MCHelper.WATER_HEIGHT, this.heightAboveSeaFunc.getOutput(pos) + (double)MCHelper.WATER_HEIGHT);
      }

      double getAmp(BlockPos2D pos) {
         return this.heightAboveSeaFunc.getOutput(pos);
      }

      List<BlockState> getGroundBlocks(BlockPos2D pos) {
         boolean isStoneBeach = this.isStoneBeachFunc.test(pos);
         boolean isGravelBeach = this.isGravelBeachFunc.test(pos);
         if (!isStoneBeach && !isGravelBeach) {
            return GroundBlocks.SAND.getSurfaceBlocks(pos);
         } else if (isStoneBeach && !isGravelBeach) {
            return GroundBlocks.STONE.getSurfaceBlocks(pos);
         } else if (!isStoneBeach && isGravelBeach) {
            return GroundBlocks.GRAVEL.getSurfaceBlocks(pos);
         } else {
            assert isStoneBeach && isGravelBeach;

            Random random = HeightGen.this.randomGen.getRandom(pos.getBlockX(), pos.getBlockZ());
            return MathHelper.randomBool(0.5D, random) ? GroundBlocks.GRAVEL.getSurfaceBlocks(pos) : GroundBlocks.STONE.getSurfaceBlocks(pos);
         }
      }
   }


}
