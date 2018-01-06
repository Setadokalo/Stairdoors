package com.darkenchanter.stairdoors.asm;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import javax.annotation.Nullable;
import java.io.*;
import java.util.Iterator;

public class EDClassTransformer implements IClassTransformer, Opcodes
{
	public EDClassTransformer() {
		log.debug("---BEGIN TRANSFORM---");
	}
	private Logger log = LogManager.getLogger("Class Transformer");
	@Override
	public byte[] transform(String arg0, String arg1, byte[] arg2) {
		if (arg0.equals("aqa")) { // The obfuscated BlockDoor class
			this.log.debug("[USRENV] Inside Door transformer about to patch " + arg0);
			return patchBlockDoor(arg2, true);
		}
		else if (arg0.equals("net.minecraft.block.BlockDoor")) { // The development environment BlockDoor class
			this.log.debug("[DEVENV] Inside Door transformer about to patch " + arg0);
			return patchBlockDoor(arg2, false);
		}
		return arg2;
	}

	/**
	 * Patches the BlockDoor class and it's obfuscated cousin.
	 * @param classData The class, in byte form.
	 * @param obf Whether or not the environment is obfuscated.
	 * @return The patched class data.
	 */
	private byte[] patchBlockDoor(byte[] classData, boolean obf) {
		// Handle the obfuscation
		String canPlaceBlockAt; // These are the names and descriptions of the methods, and need to be changed based on
		String canPlaceBlockAt_desc; // whether or not the environment is obfuscated.
		String neighborChanged;
		String neighborChanged_desc;
		if (obf) { //Set all the strings to their obfuscated names
			canPlaceBlockAt = "a";
			canPlaceBlockAt_desc = "(Lamu;Let;)Z";
			neighborChanged = "a";
			neighborChanged_desc = "(Lawt;Lamu;Let;Laow;Let;)V";
		}
		else { //Set all the strings to their deobfuscated names
			canPlaceBlockAt = "canPlaceBlockAt";
			canPlaceBlockAt_desc = "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)Z";
			neighborChanged = "neighborChanged";
			neighborChanged_desc = "(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;Lnet/minecraft/util/math/BlockPos;)V";
		}
		// Set up the class reader, which allows the class data to be accessed in a useful way
		ClassReader cr = new ClassReader(classData); // This just reads the data.
		ClassNode cv = new ClassNode(); // This can manipulate the data.
		cr.accept(cv, 0); // This allows the ClassNode to access the data from the ClassReader.

		//Loops through all the methods in the ClassNode and finds the two methods we want, then runs the associated method.
		for (MethodNode method : cv.methods) {
			/* -------- canPlaceBlockAt -------- */
			if (method.name.equals(canPlaceBlockAt) && method.desc.equals(canPlaceBlockAt_desc))
				if (obf) {
					patchObfCanPlaceBlockAt(method);
				} else {
					patchCanPlaceBlockAt(method);
				}
			/* -------- neighborChanged -------- */
			if (method.name.equals(neighborChanged) && method.desc.equals(neighborChanged_desc))
				if (obf) {
					patchObfNeighborChanged(method);
				} else {
					patchNeighborChanged(method);
				}
		}
		//This is actually capable of making those changes back into a byte array.
		//I am unsure of what the combined ints do.
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		// Allows the ClassWriter to access the modified data
		cv.accept(writer);
		// Returns the modified data.
		return writer.toByteArray();
	}

	/**
	 * Patches the method {@link net.minecraft.block.BlockDoor#canPlaceBlockAt} to allow for placement on stairs.
	 * @param method The MethodNode referring to canPlaceBlockAt.
	 */
	private void patchCanPlaceBlockAt(MethodNode method) {
		log.debug("Inside canPlaceBlockAt, see? " + method.name + "::" + method.desc); // Tool to make sure injection worked right.
		AbstractInsnNode currentNode; // The node the iterator is currently looking at.

		Iterator<AbstractInsnNode> iter = method.instructions.iterator();

		//Loops across all instructions in the method and finds the instruction of ASTORE 3, which occurs only once and is where my injection starts.
		while (iter.hasNext())
		{
			currentNode = iter.next(); //Move the node 'pointer' forward one.

			// Determines if the instruction is ASTORE, which is needed
			if ((currentNode.getOpcode() == Opcodes.ASTORE)    //     Casts the value of currentNode to be a VarInsnNode
					&& (((VarInsnNode) currentNode).var == 3)) // if it is an ASTORE instruction and gets it's variable.
			{
				log.debug("At right location in canPlaceBlockAt, begin injection");

				// Begin transforming stuff!

				InsnList insert = new InsnList(); // The list of instructions to add to the method
				/*                         --- This is what needs to be added ---                         *|
				|* This stuff calls the inject method, then either returns that (if true) or continues.   *|
				|*                                                                                        *|
				|* ALOAD 1                                                                                *|
				|* ALOAD 2                                                                                *|
				|* ALOAD 0                                                                                *|
				|* INVOKESTATIC net/darkenchanter/stairdoors/inject/BlockDoor.canPlaceBlockAtInject ()Z;  *|
				|*                                                                                        *|
				|* IFEQ L3_1                                                                              *|
				|* ICONST_1                                                                               *|
                |* IRETURN                                                                                *|
                |* LABEL L3_1                                                                             */
				MethodInsnNode invokeCPBAInject = // Creates the INVOKESTATIC above
						new MethodInsnNode(
								INVOKESTATIC,
								"com/darkenchanter/stairdoors/inject/BlockDoorI",
								"canPlaceBlockAtInject",
								"(Lnet/minecraft/world/World;" +
									"Lnet/minecraft/util/math/BlockPos;" +
									"Lnet/minecraft/block/BlockDoor;)Z",
								false);
				insert.add(new VarInsnNode(ALOAD, 1)); // Adds the first argument from canPlaceBlockAtInject to the stack.
				insert.add(new VarInsnNode(ALOAD, 2)); // The second
				insert.add(new VarInsnNode(ALOAD, 0)); // The third
				insert.add(invokeCPBAInject);          // Calls the method.

				LabelNode L3_1 = new LabelNode(); // The label put after the RETURN statement so that the method can continue if the value is 0.

				JumpInsnNode ifeq = new JumpInsnNode(IFEQ, L3_1);
				insert.add(ifeq); //Jumps past the next line if the value is 0 so that the method doesn't always return true.
				insert.add(new InsnNode(ICONST_1)); // Pushes 1 (TRUE) to the stack.
				insert.add(new InsnNode(IRETURN)); // Returns that 1 (TRUE).
				insert.add(L3_1);
				method.instructions.insert(currentNode, insert);
				break;
			}
		}

	}

	/**
	 * Patches the obfuscated method {@link net.minecraft.block.BlockDoor#canPlaceBlockAt} to allow for placement on stairs.
	 * @param method The MethodNode referring to canPlaceBlockAt.
	 */
	private void patchObfCanPlaceBlockAt(MethodNode method) {
		log.debug("Inside obfuscated canPlaceBlockAt, see? " + method.name + "::" + method.desc);
		AbstractInsnNode currentNode;

		Iterator<AbstractInsnNode> iter = method.instructions.iterator();

		//Loop over the instruction set and find the instruction FDIV which does the division of 1/explosionSize
		while (iter.hasNext())
		{
			currentNode = iter.next();

			// Determines if the instruction is ASTORE, which is needed
			if ((currentNode.getOpcode() == Opcodes.ASTORE)    // Casts the value of currentNode to be a VarInsnNode
					&& (((VarInsnNode) currentNode).var == 3)) // if it is an ASTORE instruction and gets it's variable.
			{
				log.debug("At right location, begin injection");

				// Begin transforming stuff!

				InsnList insert = new InsnList(); // The list of instructions to add to the method
				/*                         --- This is what needs to be added ---                         *|
				|* This stuff calls the inject method, then either returns that (if true) or continues.   *|
				|*                                                                                        *|
				|* ALOAD 1                                                                                *|
				|* ALOAD 2                                                                                *|
				|* ALOAD 0                                                                                *|
				|* INVOKESTATIC net/darkenchanter/stairdoors/inject/BlockDoor.canPlaceBlockAtInject ()Z;  *|
				|*                                                                                        *|
				|* IFEQ L3_1                                                                              *|
				|* ICONST_1                                                                               *|
                |* IRETURN                                                                                *|
                |* LABEL L3_1                                                                             */
				MethodInsnNode invokeCPBAInject =
						new MethodInsnNode(
								INVOKESTATIC,
								"com/darkenchanter/stairdoors/inject/BlockDoorI",
								"canPlaceBlockAtInject",
								"(Lamu;" +
										"Let;" +
										"Laqa;)Z",
								false);
				insert.add(new VarInsnNode(ALOAD, 1));
				insert.add(new VarInsnNode(ALOAD, 2));
				insert.add(new VarInsnNode(ALOAD, 0));
				insert.add(invokeCPBAInject);

				LabelNode L3_1 = new LabelNode();

				JumpInsnNode ifeq = new JumpInsnNode(IFEQ, L3_1);
				insert.add(ifeq);
				insert.add(new InsnNode(ICONST_1));
				insert.add(new InsnNode(IRETURN));
				insert.add(L3_1);
				method.instructions.insert(currentNode, insert);
				break;
			}
		}

	}
	/**
	 * Patches the method {@link net.minecraft.block.BlockDoor#neighborChanged(IBlockState, World, BlockPos, Block, BlockPos)} to allow for placement on stairs.
	 * @param method The MethodNode referring to canPlaceBlockAt.
	 */
	private void patchNeighborChanged(MethodNode method) {
		log.debug("Inside neighborChanged, see? " + method.name + "::" + method.desc);
		AbstractInsnNode currentNode;

		Iterator<AbstractInsnNode> iter = method.instructions.iterator();


		while (iter.hasNext())
		{
			currentNode = iter.next();

			// Determines if the instruction is ASTORE, which is needed
			if ((currentNode.getOpcode() == Opcodes.INVOKEINTERFACE)                    // Casts the value of currentNode to be a VarInsnNode
					&& (((MethodInsnNode) currentNode).name.equals("isSideSolid"))
					&& (((MethodInsnNode) currentNode).owner.equals("net/minecraft/block/state/IBlockState"))
					&& (((MethodInsnNode) currentNode).desc.equals("(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;)Z"))) // if it is an ASTORE instruction and gets it's variable.
			{
				log.debug("At right location in neighborChanged, begin injection");

				// Begin transforming stuff!

				InsnList insert = new InsnList(); // The list of instructions to add to the method
				/*                     --- This is what needs to be added ---                             */
				/*
				/*
				ALOAD 1 //Loads IBlockState from method input
				ALOAD 2 //Loads World from method input
				ALOAD 3 //Loads BlockPos from method input
				INVOKESTATIC com/darkenchanter/stairdoors/inject/BlockDoorI neighborChangedInject (Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)Z
				IOR //Compares the original and new return values of the method, if either is 1 than the result is 1

				*/
				insert.add(new VarInsnNode(ALOAD, 1));
				insert.add(new VarInsnNode(ALOAD, 2));
				insert.add(new VarInsnNode(ALOAD, 3));
				insert.add(new MethodInsnNode(INVOKESTATIC, "com/darkenchanter/stairdoors/inject/BlockDoorI", "neighborChangedInject", "(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)Z", false));
				insert.add(new InsnNode(IOR));
				method.instructions.insert(currentNode, insert);
				break;
			}
		}


	}
	/**
	 * Patches the obfuscated method {@link net.minecraft.block.BlockDoor#neighborChanged(IBlockState, World, BlockPos, Block, BlockPos)} to allow for placement on stairs.
	 * @param method The MethodNode referring to canPlaceBlockAt.
	 */
	private void patchObfNeighborChanged(MethodNode method) {
		log.debug("Inside obfuscated neighborChanged, see? " + method.name + "::" + method.desc);
		AbstractInsnNode currentNode;

		Iterator<AbstractInsnNode> iter = method.instructions.iterator();


		while (iter.hasNext())
		{
			currentNode = iter.next();

			// Determines if the instruction is ASTORE, which is needed
			if ((currentNode.getOpcode() == Opcodes.INVOKEINTERFACE)                    // Casts the value of currentNode to be a MethodInsnNode
					&& (((MethodInsnNode) currentNode).name.equals("isSideSolid"))
					&& (((MethodInsnNode) currentNode).owner.equals("awt"))
					&& (((MethodInsnNode) currentNode).desc.equals("(Lamy;Let;Lfa;)Z"))) // if it is an INVOKEINTERFACE instruction and tests if it is the right one.
			{
				log.debug("At right location in neighborChanged, begin injection");

				// Begin transforming stuff!

				InsnList insert = new InsnList(); // The list of instructions to add to the method
				/*                     --- This is what needs to be added ---                             */
				/*
				/*
				ALOAD 1 //Loads IBlockState from method input
				ALOAD 2 //Loads World from method input
				ALOAD 3 //Loads BlockPos from method input
				INVOKESTATIC com/darkenchanter/stairdoors/inject/BlockDoorI neighborChangedInject
				[(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)Z
				IOR //Compares the first and second return values of the method, if either is 1 than the result is 1

				*/
				insert.add(new VarInsnNode(ALOAD, 1));
				insert.add(new VarInsnNode(ALOAD, 2));
				insert.add(new VarInsnNode(ALOAD, 3));
				insert.add(new MethodInsnNode(INVOKESTATIC, "com/darkenchanter/stairdoors/inject/BlockDoorI",
						"neighborChangedInject", "(Lawt;Lamu;Let;)Z", false));
				insert.add(new InsnNode(IOR));
				method.instructions.insert(currentNode, insert);
				break;
			}
		}


	}
 }