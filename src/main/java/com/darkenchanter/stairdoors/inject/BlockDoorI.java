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
	/** The injected method into canPlaceBlockAt, which checks if the block below is stairs (and makes the method return true immediately if true)
	 *
	 * @param worldIn The world the block is getting placed in.
	 * @param pos     The block position that this is getting placed at.
	 * @param door    Unnecessary, but I don't want to rewrite the inject code.
	 * @return Whether or not the block below is stairs, which allows for placement (Sometimes gets dropped immediately by neighborChanged)
	 */
	public static boolean canPlaceBlockAtInject(World worldIn, BlockPos pos, BlockDoor door) {
		BlockPos base = pos.down(); // Gets the block below this one
		return (worldIn.getBlockState(base).getBlock().getClass() == BlockStairs.class); //Is block below stairs
	}

	/**
	 * The injected method into neighborChanged. This is hooked into the "Is block below solid?" check in
	 * BlockDoor#neighborChanged, and adds a special case for stairs ("Does block below face the opposite direction as this block?")
	 * @param state The BlockState for the BlockDoor, used to calculate if the stairs are facing opposite the door
	 * @param worldIn The world the blocks exist in
	 * @param pos The position of this block
	 * @return Whether or not to delete the door
	 */
	public static boolean neighborChangedInject(IBlockState state, World worldIn, BlockPos pos ) {
		BlockPos base = pos.down(); // Gets the block below this one
		boolean isBlockStairs = (worldIn.getBlockState(base).getBlock().getClass() == BlockStairs.class); //Is block below stairs
		IBlockState stateToCompare = state.withRotation(Rotation.CLOCKWISE_180); // Reverses the door
		return isBlockStairs && stateToCompare.getProperties().get(BlockDoor.FACING) //gets the orientation of the reversed door
				.equals(worldIn.getBlockState(pos.down()).getProperties().get(BlockStairs.FACING)); // Compares the reversed orientation to the stairs orientation
	}
}
