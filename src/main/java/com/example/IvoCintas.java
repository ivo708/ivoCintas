package com.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.block.Block;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class IvoCintas implements ModInitializer {
	public static final Block Impulsor_Block = new Impulsor(Block.Settings.create().strength(4.0f));
	public static final Block Cinta_Block = new Cinta(Block.Settings.create().strength(4.0f));
	public static final Logger LOGGER = LoggerFactory.getLogger("IvoCintas");

    @Override
    public void onInitialize() {
        Registry.register(Registries.BLOCK,Identifier.of("ivocintas", "impulsor"), Impulsor_Block);
        Registry.register(Registries.ITEM,Identifier.of("ivocintas", "impulsor"),new BlockItem(Impulsor_Block, new Item.Settings()));
        Registry.register(Registries.BLOCK,Identifier.of("ivocintas", "cinta"), Cinta_Block);
        Registry.register(Registries.ITEM,Identifier.of("ivocintas", "cinta"),new BlockItem(Cinta_Block, new Item.Settings()));        
        ImpulsorHandler.initialize();
        CintaHandler.initialize();

    }
}
