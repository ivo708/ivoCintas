package com.example;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.block.ShapeContext;

public class Cinta extends HorizontalFacingBlock {
    private static final VoxelShape SHAPE = Block.createCuboidShape(0, 0, 0, 16, 4, 16);

    public Cinta(Settings settings) {
        super(settings);
        // Estado inicial orientado al norte
        this.setDefaultState(this.getDefaultState().with(FACING, Direction.NORTH));
    }
    
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(Properties.HORIZONTAL_FACING);
    }
    
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        // La dirección del jugador se obtiene con getPlayerFacing() y se invierte para que el bloque "mire" al jugador.
        return this.getDefaultState().with(Properties.HORIZONTAL_FACING, ctx.getHorizontalPlayerFacing());
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
            IvoCintas.LOGGER.info("PISANDO");
            boolean sameAsStart=true;
            ServerPlayerEntity player = (ServerPlayerEntity) entity;
            if (CintaHandler.pushingPlayers.containsKey(player.getUuid())) {
            	sameAsStart= CintaHandler.pushingPlayers.get(player.getUuid()).pos==pos;
                
            }
            if(!CintaHandler.pushingPlayers.containsKey(player.getUuid()) || !sameAsStart){
	            // Se obtiene la dirección en la que apunta el bloque según su propiedad FACING
	            Direction pushDirection = state.get(FACING);
	            // Magnitud base del empuje (0.5 para un empuje visible; ajústalo si es necesario)
	            double pushMagnitude = 0.2;
	            Vec3d pushVec = new Vec3d(pushDirection.getOffsetX(), 0, pushDirection.getOffsetZ())
	                    .normalize().multiply(pushMagnitude);
	            
	            MinecraftServer server = player.getServer();
	            
	            double startX = player.getX();
	            double startZ = player.getZ();
	            double targetX = pos.getX() + 0.5;
	            double targetZ = pos.getZ() + 0.5;
	            
	            int steps = 5;
	            for (int i = 1; i <= steps; i++) {
	                double progress = (double) i / steps;
	                double interpolatedX = MathHelper.lerp(progress, startX, targetX);
	                double interpolatedZ = MathHelper.lerp(progress, startZ, targetZ);
	                server.execute(() -> {
	                    if (player.isAlive()) {
	                        player.teleport(player.getServerWorld(), interpolatedX, player.getY(), interpolatedZ, player.getYaw(), player.getPitch());
	                    }
	                });
	            }
	
	            IvoCintas.LOGGER.info("PUSHVEC: x="+pushVec.x+", y="+pushVec.y+", z="+pushVec.z);
	            // Registramos al jugador para que se impulse y se alinee hacia el centro
	            CintaHandler.addPlayer(player, pushVec,pos);
	        }
        }
        super.onSteppedOn(world, pos, state, entity);
    }
    
    @Override
    protected MapCodec<? extends HorizontalFacingBlock> getCodec() {
        return null;
    }
}
