package com.trungpro200.ultiminetuning;

import dev.ftb.mods.ftbultimine.api.rightclick.RegisterRightClickHandlerEvent;
import dev.ftb.mods.ftbultimine.api.rightclick.RightClickHandler;
import dev.ftb.mods.ftbultimine.api.shape.ShapeContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.Direction;
import java.util.Collection;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

// Tuning Wrench item class path
import net.caden.tuningwrench.item.custom.TunersWrenchItem;
import com.finchy.pipeorgans.content.pipes.generic.GenericPipeBlock;

public enum UltimineTuningHandler implements RightClickHandler {
    INSTANCE;

    public static final Logger LOGGER = LogUtils.getLogger();

    // Registers this event listener to FTB Ultimine's processing queue
    public static void register() {
        RegisterRightClickHandlerEvent.REGISTER.register(dispatcher -> dispatcher.registerHandler(INSTANCE));
    }

    @Override
    public int handleRightClickBlock(ShapeContext shapeContext, InteractionHand hand, Collection<BlockPos> positions) {
        Player player = shapeContext.player();
        Level level = player.level();
        ItemStack heldItem = player.getItemInHand(hand);

        // 1. Ignore if the player isn't using the Tuning Wrench
        if (!(heldItem.getItem() instanceof TunersWrenchItem) && !(heldItem.getItem().getDescriptionId().contains("block.pipeorgans"))) {
            return 0; // Return 0 blocks handled
        }

        if (level.isClientSide) {
            return 0;
        }

        boolean isWrench = heldItem.getItem() instanceof TunersWrenchItem;

        int blocksTuned = 0;
        // 2. Loop through every block highlighted in the Ultimine selection shape
        for (BlockPos pos : positions) {
            BlockState state = level.getBlockState(pos);

            // 3. Check if the block is a Sound of Steam pipe block
            if (state.getBlock() instanceof GenericPipeBlock pipeBlock) {

                // Create a clean "fake" click hit directly on the center-top of the block
                BlockHitResult fakeHit = new BlockHitResult(
                        new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5),
                        Direction.UP,
                        pos,
                        false);

                // Run the default Tuning Wrench logic on this pipe instance
                if (isWrench) {
                    heldItem.useOn(
                        new net.minecraft.world.item.context.UseOnContext(
                            player,
                            hand,
                            fakeHit
                        )
                    );
                    blocksTuned++;
                // If player holding organpipes
                } else {
                    ItemInteractionResult result = pipeBlock.useItemOn(heldItem, state, level, pos, player, hand, fakeHit);
                    if (result.consumesAction()) {
                        blocksTuned++;
                    }
                }
                
            }
        }
        //Mainly used for pipe consumption
        return blocksTuned;
    }
}