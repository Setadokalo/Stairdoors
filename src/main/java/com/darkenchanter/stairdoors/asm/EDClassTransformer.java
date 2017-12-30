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
		if (arg0.equals("aqa")) {
			this.log.debug("[USRENV] Inside Door transformer about to patch " + arg0);
			return patchBlockDoor(arg2, true);
		}
		else if (arg0.equals("net.minecraft.block.BlockDoor")) {
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
		String canPlaceBlockAt;
		String canPlaceBlockAt_desc;
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
		// Set up the class reader
		ClassReader cr = new ClassReader(classData);
		ClassNode cv = new ClassNode();
		cr.accept(cv, 0);

		//Loop through all methods to find the one we want
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
		//ASM specific for cleaning up and returning the final bytes for JVM processing.
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		cv.accept(writer);
		return writer.toByteArray();
	}

	/**
	 * Patches the method {@link net.minecraft.block.BlockDoor#canPlaceBlockAt} to allow for placement on stairs.
	 * @param method The MethodNode referring to canPlaceBlockAt.
	 */
	private void patchCanPlaceBlockAt(MethodNode method) {
		log.debug("Inside canPlaceBlockAt, see? " + method.name + "::" + method.desc);
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
				log.debug("At right location in canPlaceBlockAt, begin injection");

				// Begin transforming stuff!

				InsnList insert = new InsnList(); // The list of instructions to add to the method
				/*                     --- This is what needs to be added ---                             */
				/*
				/* INVOKESTATIC net/darkenchanter/stairdoors/inject/BlockDoor.canPlaceBlockAtInject ()Z;  */
				/* IFEQ L3                                                                                */
                /* IRETURN 1                                                                              */
				MethodInsnNode invokeCPBAInject =
						new MethodInsnNode(
								INVOKESTATIC,
								"com/darkenchanter/stairdoors/inject/BlockDoorI",
								"canPlaceBlockAtInject",
								"(Lnet/minecraft/world/World;" +
									"Lnet/minecraft/util/math/BlockPos;" +
									"Lnet/minecraft/block/BlockDoor;)Z",
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
				/*                     --- This is what needs to be added ---                             */
				/*
				/* INVOKESTATIC com/darkenchanter/stairdoors/inject/BlockDoor.canPlaceBlockAtInject ()Z;  */
				/* IFEQ L3                                                                                */
                /* IRETURN 1                                                                              */
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
				IOR //Compares the first and second return values of the method, if either is 1 than the result is 1

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
				INVOKESTATIC com/darkenchanter/stairdoors/inject/BlockDoorI neighborChangedInject (Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)Z
				IOR //Compares the first and second return values of the method, if either is 1 than the result is 1

				*/
				insert.add(new VarInsnNode(ALOAD, 1));
				insert.add(new VarInsnNode(ALOAD, 2));
				insert.add(new VarInsnNode(ALOAD, 3));
				insert.add(new MethodInsnNode(INVOKESTATIC, "com/darkenchanter/stairdoors/inject/BlockDoorI", "neighborChangedInject", "(Lawt;Lamu;Let;)Z", false));
				insert.add(new InsnNode(IOR));
				method.instructions.insert(currentNode, insert);
				break;
			}
		}


	}
 }