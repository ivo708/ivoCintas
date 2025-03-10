package com.example;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

public class BarrierSlab extends SlabBlock {

    private static final VoxelShape BOTTOM_SHAPE = Block.createCuboidShape(0, 0, 0, 16, 8, 16);
    private static final VoxelShape TOP_SHAPE = Block.createCuboidShape(0, 8, 0, 16, 16, 16);

    public BarrierSlab(Settings settings) {
        super(settings);
    }
    
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockPos pos = ctx.getBlockPos();
        BlockState stateAtPos = ctx.getWorld().getBlockState(pos);
        // Si ya hay una slab en esta posición y no es doble, la convierte en doble.
        if (stateAtPos.getBlock() == this) {
            SlabType type = stateAtPos.get(TYPE);
            if (type != SlabType.DOUBLE) {
                return stateAtPos.with(TYPE, SlabType.DOUBLE);
            }
        }
        // Determinar si se coloca como slab superior o inferior según el clic del jugador
        double hitY = ctx.getHitPos().y - pos.getY();
        return this.getDefaultState().with(TYPE, hitY > 0.5 ? SlabType.TOP : SlabType.BOTTOM);
    }
    
    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, net.minecraft.block.ShapeContext context) {
        SlabType type = state.get(TYPE);
        if (type == SlabType.BOTTOM) {
            return BOTTOM_SHAPE;
        } else if (type == SlabType.TOP) {
            return TOP_SHAPE;
        } else {
            return VoxelShapes.fullCube();
        }
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, net.minecraft.block.ShapeContext context) {
        return getOutlineShape(state, world, pos, context);
    }

    @Override
    public MapCodec<? extends SlabBlock> getCodec() {
        return null;
    }
}
