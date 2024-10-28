package net.bandit.many_bows.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ArcaneBow extends BowItem {

    public ArcaneBow(Properties properties) {
        super(properties);
    }
    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        if (entity instanceof Player player) {
            int charge = this.getUseDuration(stack) - timeCharged;
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SHROOMLIGHT_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);


            boolean hasInfinity = player.getAbilities().instabuild || EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, stack) > 0;

            if (charge >= 20 && (hasInfinity || consumeArrows(player, 3))) {
                fireExtraArrows(level, player, hasInfinity);
                stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(player.getUsedItemHand()));
            }
        }
    }

    private boolean consumeArrows(Player player, int count) {
        if (player.getAbilities().instabuild) {
            return true;
        }

        int arrowsRemoved = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() == Items.ARROW) {
                int removeAmount = Math.min(stack.getCount(), count - arrowsRemoved);
                stack.shrink(removeAmount);
                arrowsRemoved += removeAmount;
                if (arrowsRemoved >= count) {
                    return true;
                }
            }
        }
        return false;
    }

    private void fireExtraArrows(Level level, Player player, boolean hasInfinity) {
        float basePitch = player.getXRot();
        float baseYaw = player.getYRot();

        for (int i = -1; i <= 1; i++) {
            AbstractArrow extraArrow = new AbstractArrow(EntityType.ARROW, player, level) {
                @Override
                protected ItemStack getPickupItem() {
                    return hasInfinity ? ItemStack.EMPTY : new ItemStack(Items.ARROW); // Prevent pickup if bow has Infinity
                }
            };
            extraArrow.shootFromRotation(player, basePitch, baseYaw + i * 5.0F, 0.0F, 4.0F, 1.0F);
            extraArrow.setBaseDamage(extraArrow.getBaseDamage() + 4.0);
            if (hasInfinity) {
                extraArrow.pickup = AbstractArrow.Pickup.DISALLOWED; // Prevent pickup if bow has Infinity
            }
            level.addFreshEntity(extraArrow);
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
    }


    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int timeLeft) {
        super.onUseTick(level, entity, stack, timeLeft);
        if (entity instanceof Player) {
            Player player = (Player) entity;
            int charge = this.getUseDuration(stack) - timeLeft;
            if (charge == 10) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SHROOMLIGHT_STEP, SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.many_bows.arcane_bow.tooltip").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("item.many_bows.arcane_bow.tooltip.ability").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.many_bows.arcane_bow.tooltip.legend").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
    }
}