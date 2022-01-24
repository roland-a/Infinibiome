package weightedgpa.infinibiome.api;

import com.google.common.collect.Lists;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import weightedgpa.infinibiome.api.dependency.DependencyModule;
import weightedgpa.infinibiome.api.posdata.PosDataKeys;
import weightedgpa.infinibiome.internal.generators.DefaultModule;
import weightedgpa.infinibiome.internal.generators.nonworldgen.*;
import weightedgpa.infinibiome.internal.generators.nonworldgen.controllers.GroundBoneMealControllers;
import weightedgpa.infinibiome.internal.generators.nonworldgen.controllers.PlantGrowthControllers;
import weightedgpa.infinibiome.internal.generators.nonworldgen.controllers.SaplingControllers;
import weightedgpa.infinibiome.internal.minecraftImpl.ChunkDataWriter;
import weightedgpa.infinibiome.internal.minecraftImpl.IBBiomes;
import weightedgpa.infinibiome.internal.minecraftImpl.IBWorldType;
import weightedgpa.infinibiome.internal.minecraftImpl.commands.IBCommand;

import java.util.List;


@SuppressWarnings("WeakerAccess")
@Mod(Infinibiome.MOD_ID)
@Mod.EventBusSubscriber(modid = Infinibiome.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class Infinibiome{
	public static final String MOD_ID = "infinibiome";

	public static final List<DependencyModule> depModules = Lists.newArrayList(
		DefaultModule.INSTANCE
	);

	public Infinibiome(){
		//MinecraftForge.EVENT_BUS.register(IBBiomes.class);
		MinecraftForge.EVENT_BUS.register(IBCommand.class);
		MinecraftForge.EVENT_BUS.register(GroundBoneMealControllers.class);
		MinecraftForge.EVENT_BUS.register(PlantGrowthControllers.class);
		MinecraftForge.EVENT_BUS.register(SaplingControllers.class);
		MinecraftForge.EVENT_BUS.register(ChunkDataWriter.class);

		ConfigIOImpl.refreshConfig(depModules);

		PosDataKeys.init();
		IBWorldType.init();
	}

	public static void addModule(DependencyModule module) {
		depModules.add(module);
	}

	@SubscribeEvent
	public static void onRegisterBiomes(RegistryEvent.Register<Biome> e) {
		IBBiomes.onRegisterBiomes(e);
	}
}