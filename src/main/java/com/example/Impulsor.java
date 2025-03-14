package com.example;

import com.example.ImpulsorHandler.PushData;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
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

public class Impulsor extends HorizontalFacingBlock {
    private static final VoxelShape SHAPE = Block.createCuboidShape(0, 0, 0, 16, 4, 16);

    public Impulsor(Settings settings) {
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
            boolean sameAsStart=true;
            ServerPlayerEntity player = (ServerPlayerEntity) entity;
            if (ImpulsorHandler.pushingPlayers.containsKey(player.getUuid())) {
            	IvoCintas.LOGGER.info("PISANDO CONTIENE    X:"+ImpulsorHandler.pushingPlayers.get(player.getUuid()).pos.getX()+" "+pos.getX()+"    Z:"+ImpulsorHandler.pushingPlayers.get(player.getUuid()).pos.getZ()+" "+pos.getZ());
            	
            	sameAsStart= (ImpulsorHandler.pushingPlayers.get(player.getUuid()).pos.getX()==pos.getX() && ImpulsorHandler.pushingPlayers.get(player.getUuid()).pos.getY()==pos.getY() && ImpulsorHandler.pushingPlayers.get(player.getUuid()).pos.getZ()==pos.getZ());
                
            }
        	IvoCintas.LOGGER.info("contains: "+ImpulsorHandler.pushingPlayers.containsKey(player.getUuid())+" same:"+sameAsStart);
            if(!ImpulsorHandler.pushingPlayers.containsKey(player.getUuid()) || !sameAsStart){
            	ImpulsorHandler.pushingPlayers.remove(player.getUuid());
            	IvoCintas.LOGGER.info("PISANDO NO CONTIENE");
	            Direction pushDirection = state.get(FACING);
	            double pushMagnitude = 0.2;
	            Vec3d pushVec = new Vec3d(pushDirection.getOffsetX(), 0, pushDirection.getOffsetZ()).normalize().multiply(pushMagnitude);
	            ImpulsorHandler.addPlayer(player, pushVec,pos);
	        }
        }
        super.onSteppedOn(world, pos, state, entity);
    }
    
    @Override
    protected MapCodec<? extends HorizontalFacingBlock> getCodec() {
        return null;
    }
}
