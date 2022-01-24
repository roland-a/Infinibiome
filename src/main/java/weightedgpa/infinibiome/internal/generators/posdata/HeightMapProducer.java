package weightedgpa.infinibiome.internal.generators.posdata;

import org.apache.commons.lang3.Validate;
import weightedgpa.infinibiome.api.generators.Seed;
import weightedgpa.infinibiome.api.pos.BlockPos2D;
import weightedgpa.infinibiome.internal.floatfunc.FloatFunc;
import weightedgpa.infinibiome.internal.floatfunc.generators.SimplexNoise;
import weightedgpa.infinibiome.internal.floatfunc.modifiers.SeamlessGrid;
import weightedgpa.infinibiome.internal.floatfunc.util.Interval;
import weightedgpa.infinibiome.internal.misc.Helper;
import weightedgpa.infinibiome.internal.misc.MCHelper;
import weightedgpa.infinibiome.internal.misc.MathHelper;
import weightedgpa.infinibiome.internal.misc.PosModCache;
import weightedgpa.infinibiome.internal.misc.PregeneratedSeamlessGrid;

public final class HeightMapProducer {
    private static final Interval CLAMP = new Interval(-0.2D, 0.25D);
    private static final double FOLD_CLAMP_EASE_DIST = 0.02D;
    private static final Interval DEFAULT_RELATIVE_SCALE = new Interval(4.0D, 12.0D);
    private static final Interval DEFAULT_PERSISTENCE = new Interval(0.5D, 0.65D);
    private static final Interval DEFAULT_LACUNARITY = new Interval(0.5D, 0.65D);
    private static final Interval DEFAULT_SCALE_LIMIT = new Interval(10.0D, 20.0D);
    private static final Interval DEFAULT_SKEW = new Interval(-2.0D, 2.0D);
    private static final SeamlessGrid SEAMLESS_GRID;
    private final SimplexNoise<BlockPos2D> base;
    private final FloatFunc<BlockPos2D> baseHeightFunc;
    private final FloatFunc<BlockPos2D> ampFunc;
    private final FloatFunc<BlockPos2D> persistenceFunc;
    private final FloatFunc<BlockPos2D> relativeScaleFunc;
    private final FloatFunc<BlockPos2D> scaleLimitFunc;
    private final FloatFunc<BlockPos2D> lacunarityFunc;
    private final FloatFunc<BlockPos2D> skewFunc;
    private final PosModCache<BlockPos2D, HeightMapProducer.GridData> cache;

    public HeightMapProducer(Seed seed, FloatFunc<BlockPos2D> baseHeightFunc, FloatFunc<BlockPos2D> ampFunc) {
        this.cache = new PosModCache<BlockPos2D, GridData>(8, (x$0) -> {
            return new HeightMapProducer.GridData(x$0);
        }, BlockPos2D.INFO);
        seed = seed.newSeed("heightMapProducer");
        this.baseHeightFunc = baseHeightFunc;
        this.ampFunc = ampFunc;
        this.base = new SimplexNoise(seed.newSeed("base"), 1.0D, BlockPos2D.INFO);
        this.relativeScaleFunc = Helper.initUniformNoise(seed.newSeed("relativeScale"), 2048.0D).mapInterval(DEFAULT_RELATIVE_SCALE);
        this.persistenceFunc = Helper.initUniformNoise(seed.newSeed("persistence"), 2048.0D).mapInterval(DEFAULT_PERSISTENCE);
        this.lacunarityFunc = Helper.initUniformNoise(seed.newSeed("lacunarity"), 2048.0D).mapInterval(DEFAULT_LACUNARITY);
        this.scaleLimitFunc = Helper.initUniformNoise(seed.newSeed("scaleLimit"), 2048.0D).mapInterval(DEFAULT_SCALE_LIMIT);
        this.skewFunc = Helper.initUniformNoise(seed.newSeed("skew"), 2048.0D).mapInterval(DEFAULT_SKEW);
        this.validate();
    }

    private HeightMapProducer(SimplexNoise<BlockPos2D> base, FloatFunc<BlockPos2D> baseHeightFunc, FloatFunc<BlockPos2D> ampFunc, FloatFunc<BlockPos2D> relativeScaleFunc, FloatFunc<BlockPos2D> persistancesFunc, FloatFunc<BlockPos2D> lacunarityFunc, FloatFunc<BlockPos2D> scaleLimitFunc, FloatFunc<BlockPos2D> skewFunc) {
        this.cache = new PosModCache<BlockPos2D, GridData>(8, (x$0) -> {
            return new HeightMapProducer.GridData(x$0);
        }, BlockPos2D.INFO);
        this.base = base;
        this.baseHeightFunc = baseHeightFunc;
        this.ampFunc = ampFunc;
        this.persistenceFunc = persistancesFunc;
        this.relativeScaleFunc = relativeScaleFunc;
        this.lacunarityFunc = lacunarityFunc;
        this.scaleLimitFunc = scaleLimitFunc;
        this.skewFunc = skewFunc;
        this.validate();
    }

    private void validate() {
        Validate.isTrue(MCHelper.VALID_WORLD_HEIGHT.containsAll(FloatFunc.sum(this.baseHeightFunc, this.ampFunc).getOutputInterval()), "%s %s %s", this.baseHeightFunc.getOutputInterval(), this.ampFunc.getOutputInterval(), FloatFunc.sum(this.baseHeightFunc, this.ampFunc).getOutputInterval().toString());
        Validate.isTrue(MathHelper.VALID_SCALE.containsAll(this.relativeScaleFunc.getOutputInterval()), "%s", this.relativeScaleFunc.getOutputInterval());
        Validate.isTrue(MathHelper.VALID_SCALE.containsAll(this.scaleLimitFunc.getOutputInterval()), "%s", this.scaleLimitFunc.getOutputInterval());
        Validate.isTrue(MathHelper.VALID_FRACTAL_VALUE.containsAll(this.persistenceFunc.getOutputInterval()), "%s", this.persistenceFunc.getOutputInterval());
        Validate.isTrue(MathHelper.VALID_FRACTAL_VALUE.containsAll(this.lacunarityFunc.getOutputInterval()), "%s", this.lacunarityFunc.getOutputInterval());
    }

    public HeightMapProducer setRelativeScale(FloatFunc<BlockPos2D> relativeScale_) {
        return new HeightMapProducer(this.base, this.baseHeightFunc, this.ampFunc, relativeScale_, this.persistenceFunc, this.lacunarityFunc, this.scaleLimitFunc, this.skewFunc);
    }

    public HeightMapProducer setPersistenceFunc(FloatFunc<BlockPos2D> persitenceFunc_) {
        return new HeightMapProducer(this.base, this.baseHeightFunc, this.ampFunc, this.relativeScaleFunc, persitenceFunc_, this.lacunarityFunc, this.scaleLimitFunc, this.skewFunc);
    }

    public HeightMapProducer setLacunarityFunc(FloatFunc<BlockPos2D> lacunarityFunc_) {
        return new HeightMapProducer(this.base, this.baseHeightFunc, this.ampFunc, this.relativeScaleFunc, this.persistenceFunc, lacunarityFunc_, this.scaleLimitFunc, this.skewFunc);
    }

    public HeightMapProducer setScaleLimitFunc(FloatFunc<BlockPos2D> scaleLimitFunc_) {
        return new HeightMapProducer(this.base, this.baseHeightFunc, this.ampFunc, this.relativeScaleFunc, this.persistenceFunc, this.lacunarityFunc, scaleLimitFunc_, this.skewFunc);
    }

    public HeightMapProducer setSkewFunc(FloatFunc<BlockPos2D> skewFunc_) {
        return new HeightMapProducer(this.base, this.baseHeightFunc, this.ampFunc, this.relativeScaleFunc, this.persistenceFunc, this.lacunarityFunc, this.scaleLimitFunc, skewFunc_);
    }

    public double getMappedHeight(BlockPos2D pos) {
        double result = this.applyFractal(pos);
        result = this.applyPreClamp(result);
        result = this.applyClamp(result);
        result = this.applySkew(result, pos);
        result = this.applyAmp(result, pos);
        result = this.applyBaseHeight(result, pos);
        result = MCHelper.VALID_WORLD_HEIGHT.clamp(result);
        return result;
    }

    public double getAmp(BlockPos2D pos) {
        return this.ampFunc.getOutput(pos);
    }

    public HeightMapProducer _setDebuggable(String group) {
        this.baseHeightFunc._setDebuggable(group, "baseHeight", (t, p) -> {
            SeamlessGrid var10000 = SEAMLESS_GRID;
            t.getClass();
            return var10000._debugValue(p, t::getOutput);
        });
        this.ampFunc._setDebuggable(group, "amp", (t, p) -> {
            SeamlessGrid var10000 = SEAMLESS_GRID;
            t.getClass();
            return var10000._debugValue(p, t::getOutput);
        });
        this.relativeScaleFunc._setDebuggable(group, "relativeScale", (t, p) -> {
            SeamlessGrid var10000 = SEAMLESS_GRID;
            t.getClass();
            return var10000._debugValue(p, t::getOutput);
        });
        this.persistenceFunc._setDebuggable(group, "persitence", (t, p) -> {
            SeamlessGrid var10000 = SEAMLESS_GRID;
            t.getClass();
            return var10000._debugValue(p, t::getOutput);
        });
        this.lacunarityFunc._setDebuggable(group, "lacunarity", (t, p) -> {
            SeamlessGrid var10000 = SEAMLESS_GRID;
            t.getClass();
            return var10000._debugValue(p, t::getOutput);
        });
        this.scaleLimitFunc._setDebuggable(group, "scaleLimit", (t, p) -> {
            SeamlessGrid var10000 = SEAMLESS_GRID;
            t.getClass();
            return var10000._debugValue(p, t::getOutput);
        });
        this.skewFunc._setDebuggable(group, "skew", (t, p) -> {
            SeamlessGrid var10000 = SEAMLESS_GRID;
            t.getClass();
            return var10000._debugValue(p, t::getOutput);
        });
        (new FloatFunc<BlockPos2D>() {
            public double getOutput(BlockPos2D input) {
                return MathHelper.scaleLimitToOctaves(Math.max(HeightMapProducer.this.relativeScaleFunc.getOutput(input) * Math.abs(HeightMapProducer.this.ampFunc.getOutput(input)), HeightMapProducer.this.scaleLimitFunc.getOutput(input)), HeightMapProducer.this.scaleLimitFunc.getOutput(input), HeightMapProducer.this.lacunarityFunc.getOutput(input));
            }
        })._setDebuggable(group, "octaves", (t, p) -> {
            SeamlessGrid var10000 = SEAMLESS_GRID;
            t.getClass();
            return var10000._debugValue(p, t::getOutput);
        });
        (new FloatFunc<BlockPos2D>() {
            public double getOutput(BlockPos2D input) {
                return HeightMapProducer.this.relativeScaleFunc.getOutput(input) * Math.abs(HeightMapProducer.this.ampFunc.getOutput(input));
            }
        })._setDebuggable(group, "trueScale", (t, p) -> {
            SeamlessGrid var10000 = SEAMLESS_GRID;
            t.getClass();
            return var10000._debugValue(p, t::getOutput);
        });
        return this;
    }

    private double applyFractal(BlockPos2D pos) {
        return SEAMLESS_GRID.getValue(pos, (gridPos) -> {
            HeightMapProducer.GridData data = (HeightMapProducer.GridData)this.cache.get(gridPos);
            return MathHelper.fractal((i) -> {
                return this.base.getOutput(pos, i * data.scale);
            }, this.base.getOutputInterval(), data.octaves, data.persistence, data.lacunarity);
        }, BlockPos2D.INFO);
    }

    private double applyPreClamp(double prev) {
        Interval foldClampEase = new Interval(CLAMP.getMin() - 0.02D, CLAMP.getMin() + 0.02D);
        if (!foldClampEase.contains(prev)) {
            return prev;
        } else {
            double percent = foldClampEase.mapInterval(prev, Interval.PERCENT);
            percent = MathHelper.ease(percent, -3.0D);
            return Interval.PERCENT.mapInterval(percent, foldClampEase);
        }
    }

    private double applyClamp(double prev) {
        double result = (new Interval(CLAMP.getMin(), Double.POSITIVE_INFINITY)).foldClamp(prev);
        result = CLAMP.mapInterval(result, Interval.PERCENT);
        return result;
    }

    private double applySkew(double prev, BlockPos2D pos) {
        return SEAMLESS_GRID.getValue(pos, (gridPos) -> {
            HeightMapProducer.GridData data = (HeightMapProducer.GridData)this.cache.get(gridPos);
            return prev >= 1.0D ? prev : MathHelper.skew(prev, data.skew);
        }, BlockPos2D.INFO);
    }

    private double applyAmp(double prev, BlockPos2D pos) {
        return SEAMLESS_GRID.getValue(pos, (gridPos) -> {
            HeightMapProducer.GridData data = (HeightMapProducer.GridData)this.cache.get(gridPos);
            double result = prev;
            if (data.amp < 0.0D) {
                result = 1.0D - prev;
            }

            result *= data.amp;
            return result;
        }, BlockPos2D.INFO);
    }

    private double applyBaseHeight(double prev, BlockPos2D pos) {
        return SEAMLESS_GRID.getValue(pos, (gridPos) -> {
            HeightMapProducer.GridData data = (HeightMapProducer.GridData)this.cache.get(gridPos);
            return prev + data.baseHeight;
        }, BlockPos2D.INFO);
    }

    static {
        SEAMLESS_GRID = PregeneratedSeamlessGrid.TABLE_256_256;
    }

    private class GridData {
        final double scale;
        final double lacunarity;
        final double scaleLimit;
        final double amp;
        final double persistence;
        final double baseHeight;
        final double octaves;
        final double skew;

        GridData(BlockPos2D gridPos) {
            this.amp = HeightMapProducer.this.ampFunc.getOutput(gridPos);
            this.persistence = HeightMapProducer.this.persistenceFunc.getOutput(gridPos);
            this.baseHeight = HeightMapProducer.this.baseHeightFunc.getOutput(gridPos);
            this.scale = HeightMapProducer.this.relativeScaleFunc.getOutput(gridPos) * Math.abs(this.amp);
            this.scaleLimit = Math.min(HeightMapProducer.this.scaleLimitFunc.getOutput(gridPos), this.scale);
            this.lacunarity = HeightMapProducer.this.lacunarityFunc.getOutput(gridPos);
            this.skew = HeightMapProducer.this.skewFunc.getOutput(gridPos);
            this.octaves = MathHelper.scaleLimitToOctaves(this.scale, this.scaleLimit, this.lacunarity);
        }
    }
}