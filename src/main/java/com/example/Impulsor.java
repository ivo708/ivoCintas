package com.example;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
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
    
    // Es importante añadir la propiedad FACING al estado del bloque.
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
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
            // Se obtiene la dirección en la que apunta el bloque (la propiedad FACING)
            Direction pushDirection = state.get(FACING);
            Vec3d pushVec = new Vec3d(pushDirection.getOffsetX(), 0, pushDirection.getOffsetZ())
                    .normalize().multiply(0.5);
            ImpulsorHandler.addPlayer((PlayerEntity) entity, pushVec);
        }
        super.onSteppedOn(world, pos, state, entity);
    }
    
    @Override
    protected MapCodec<? extends HorizontalFacingBlock> getCodec() {
        return null;
    }
}
