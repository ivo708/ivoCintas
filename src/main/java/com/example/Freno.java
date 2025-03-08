package com.example;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.block.ShapeContext;

public class Freno extends Block {
    private static final VoxelShape SHAPE = Block.createCuboidShape(0, 0, 0, 16, 4, 16);

    public Freno(Settings settings) {
        super(settings);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }
    
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }
    
    @Override
    public void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity) {
        if (!world.isClient && entity instanceof PlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) entity;
            if (ImpulsorHandler.pushingPlayers.containsKey(player.getUuid())) {
                ImpulsorHandler.pushingPlayers.remove(player.getUuid());

                // Calcula el centro del bloque
                final double targetX = pos.getX() + 0.5;
                final double targetY = player.getY(); // Puedes ajustar la Y si lo deseas
                final double targetZ = pos.getZ() + 0.5;

                // Define el tiempo total de interpolación (en ticks)
                final int totalTicks = 60; // 20 ticks = 1 segundo (ajusta según lo necesites)
                // Guarda la posición de partida
                final double startX = player.getX();
                final double startZ = player.getZ();
                // Diferencias totales
                final double deltaX = targetX - startX;
                final double deltaZ = targetZ - startZ;

                // Objeto anónimo para realizar la interpolación por ticks
                new Object() {
                    int ticksElapsed = 0;

                    void smoothTeleport() {
                        if (ticksElapsed >= totalTicks) {
                            // Último ajuste para asegurar que llegue exactamente al centro
                            player.teleport(player.getServerWorld(), targetX, targetY, targetZ, player.getYaw(), player.getPitch());
                            return;
                        }
                        // Calcula la fracción de interpolación (entre 0 y 1)
                        double fraction = (ticksElapsed + 1) / (double) totalTicks;
                        double newX = startX + deltaX * fraction;
                        double newZ = startZ + deltaZ * fraction;
                        player.teleport(player.getServerWorld(), newX, targetY, newZ, player.getYaw(), player.getPitch());
                        ticksElapsed++;
                        // Programa la siguiente iteración para el siguiente tick
                        world.getServer().execute(() -> smoothTeleport());
                    }
                }.smoothTeleport();
            }
            super.onSteppedOn(world, pos, state, entity);
        }
    }


    
    @Override
    protected MapCodec<? extends Block> getCodec() {
        return null;
    }
}
