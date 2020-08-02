/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.items.misc;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import appeng.api.implementations.items.IGrowableCrystal;
import appeng.core.localization.ButtonToolTips;
import appeng.items.AEBaseItem;

/**
 * This item reprents one of the seeds used to grow various forms of quartz by
 * throwing them into water (for that behavior, see the linked entity)
 */
public class CrystalSeedItem extends AEBaseItem implements IGrowableCrystal {

    /**
     * Name of NBT tag used to store the growth progress value.
     */
    private static final String TAG_GROWTH_TICKS = "p";

    /**
     * The number of growth ticks required to finish growing.
     */
    public static final int GROWTH_TICKS_REQUIRED = 600;

    /**
     * The item to convert to, when growth finishes.
     */
    private final ItemConvertible grownItem;

    public CrystalSeedItem(Settings properties, ItemConvertible grownItem) {
        super(properties);
        this.grownItem = Preconditions.checkNotNull(grownItem);
    }

    @Nullable
    @Override
    public ItemStack triggerGrowth(final ItemStack is) {
        final int growthTicks = getGrowthTicks(is) + 1;
        if (growthTicks >= GROWTH_TICKS_REQUIRED) {
            return new ItemStack(grownItem, is.getCount());
        } else {
            this.setGrowthTicks(is, growthTicks);
            return is;
        }
    }

    public static int getGrowthTicks(final ItemStack is) {
        CompoundTag tag = is.getTag();
        return tag != null ? tag.getInt(TAG_GROWTH_TICKS) : 0;
    }

    private void setGrowthTicks(final ItemStack is, int ticks) {
        ticks = MathHelper.clamp(ticks, 0, GROWTH_TICKS_REQUIRED);
        is.getOrCreateTag().putInt(TAG_GROWTH_TICKS, ticks);
    }

    @Override
    public float getMultiplier(final Block blk, final Material mat) {
        return 0.5f;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void appendTooltip(final ItemStack stack, final World world, final List<Text> lines,
            final TooltipContext advancedTooltips) {
        lines.add(ButtonToolTips.DoesntDespawn.text());
        lines.add(getGrowthTooltipItem(stack));

        super.appendTooltip(stack, world, lines, advancedTooltips);
    }

    public Text getGrowthTooltipItem(ItemStack stack) {
        final int progress = getGrowthTicks(stack);
        return new LiteralText(Math.round(100 * progress / (float) GROWTH_TICKS_REQUIRED) + "%");
    }

// FIXME FABRIC Needs custom mixin
// FIXME FABRIC    @Override
// FIXME FABRIC    public boolean hasCustomEntity(final ItemStack stack) {
// FIXME FABRIC        return true;
// FIXME FABRIC    }
// FIXME FABRIC
// FIXME FABRIC    @Override
// FIXME FABRIC    public Entity createEntity(final World world, final Entity location, final ItemStack itemstack) {
// FIXME FABRIC        final GrowingCrystalEntity egc = new GrowingCrystalEntity(world, location.getX(), location.getY(),
// FIXME FABRIC                location.getZ(), itemstack);
// FIXME FABRIC
// FIXME FABRIC        egc.setVelocity(location.getVelocity());
// FIXME FABRIC
// FIXME FABRIC        // Cannot read the pickup delay of the original item, so we
// FIXME FABRIC        // use the pickup delay used for items dropped by a player instead
// FIXME FABRIC        egc.setPickupDelay(40);
// FIXME FABRIC
// FIXME FABRIC        return egc;
// FIXME FABRIC    }

    @Override
    public void appendStacks(ItemGroup group, DefaultedList<ItemStack> items) {
        if (this.isIn(group)) {
            // lvl 0
            items.add(new ItemStack(this, 1));
            // one tick before maturity
            ItemStack almostFullGrown = new ItemStack(this, 1);
            setGrowthTicks(almostFullGrown, GROWTH_TICKS_REQUIRED - 1);
            items.add(almostFullGrown);
        }
    }

}
