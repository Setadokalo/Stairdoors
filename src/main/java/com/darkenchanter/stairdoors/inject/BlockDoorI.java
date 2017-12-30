package com.darkenchanter.stairdoors.inject;

import com.darkenchanter.stairdoors.main.Main;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockDoorI {
	private static void log(String in) {
		Main.logger.info(in);
	}
	public static boolean canPlaceBlockAtInject(World worldIn, BlockPos pos, BlockDoor door) {
		BlockPos base = pos.down();
		boolean isBlockStairs = (worldIn.getBlockState(base).getBlock().getClass() == BlockStairs.class);
		return isBlockStairs;
	}
	public static boolean neighborChangedInject(IBlockState state, World worldIn, BlockPos pos ) {
		BlockPos base = pos.down();
		boolean isBlockStairs = (worldIn.getBlockState(base).getBlock().getClass() == BlockStairs.class);
		IBlockState stateToCompare = state.withRotation(Rotation.CLOCKWISE_180);
		return isBlockStairs && stateToCompare.getProperties().get(BlockDoor.FACING).equals(worldIn.getBlockState(pos.down()).getProperties().get(BlockStairs.FACING));
	}
}
