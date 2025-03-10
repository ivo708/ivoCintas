package com.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.block.SlabBlock;

public class IvoCintas implements ModInitializer {
	public static final Block Impulsor_Block = new Impulsor(Block.Settings.create().strength(4.0f));
	public static final Block Cinta_Block = new Cinta(Block.Settings.create().strength(4.0f));
	public static final Block Freno_Block = new Freno(Block.Settings.create().strength(4.0f));
	public static final SlabBlock BarrierSlab_Block = new BarrierSlab(Block.Settings.create().strength(Float.MAX_VALUE).nonOpaque());
	public static final Logger LOGGER = LoggerFactory.getLogger("IvoCintas");

    @Override
    public void onInitialize() {
        Registry.register(Registries.BLOCK,Identifier.of("ivocintas", "impulsor"), Impulsor_Block);
        Registry.register(Registries.ITEM,Identifier.of("ivocintas", "impulsor"),new BlockItem(Impulsor_Block, new Item.Settings()));
        Registry.register(Registries.BLOCK,Identifier.of("ivocintas", "cinta"), Cinta_Block);
        Registry.register(Registries.ITEM,Identifier.of("ivocintas", "cinta"),new BlockItem(Cinta_Block, new Item.Settings()));
        Registry.register(Registries.BLOCK,Identifier.of("ivocintas", "freno"), Freno_Block);
        Registry.register(Registries.ITEM,Identifier.of("ivocintas", "freno"),new BlockItem(Freno_Block, new Item.Settings()));    
        Registry.register(Registries.BLOCK,Identifier.of("ivocintas", "barrierslab"),BarrierSlab_Block);    
        Registry.register(Registries.ITEM,Identifier.of("ivocintas", "barrierslab"),new BlockItem(BarrierSlab_Block, new Item.Settings()));    

        ImpulsorHandler.initialize();
        CintaHandler.initialize();

    }
}
