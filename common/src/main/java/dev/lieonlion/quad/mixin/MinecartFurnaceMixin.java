package dev.lieonlion.quad.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.MinecartFurnace;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.CookingFuel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(value = MinecartFurnace.class, priority = 1004)
public abstract class MinecartFurnaceMixin extends AbstractMinecart {
    @Shadow private int fuel;
    @Shadow public Vec3 push;

    public MinecartFurnaceMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @ModifyExpressionValue(method = "interact", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/vehicle/minecart/MinecartFurnace;addFuel(Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/item/ItemStack;)Z"))
    private boolean applyAbstractFurnaceFuelItems(boolean original, Player player, InteractionHand hand) {
        return quad$addFuel(player.position(), player.getItemInHand(hand), player);
    }

    @Unique
    private boolean quad$addFuel(Vec3 interactingPos, ItemStack itemStack, LivingEntity entity) {
        if (itemStack.isEmpty()) return false;
        if (!level().isClientSide() && level() instanceof ServerLevel serverLevel) {
            int itemFuelTime = quad$getBurnDuration(serverLevel, itemStack, entity);
            if (itemFuelTime > 0 && this.fuel + itemFuelTime <= 32000) {
              this.fuel += itemFuelTime;

              if (this.fuel > 0) {
                this.push = this.position().subtract(interactingPos).horizontal();
              }
              return true;
            }
            return false;
        }
        return false;
    }

    @Unique
    protected int quad$getBurnDuration(ServerLevel level, ItemStack stack, LivingEntity entity) {
        return ResolvableInt.getFromItem(
            stack,
            DataComponents.COOKING_FUEL,
            CookingFuel::burnTime, quad$getLootContext(level, entity, stack),
            0
        );
    }

    @Unique
    protected LootContext quad$getLootContext(final ServerLevel level, LivingEntity entity, ItemStack stack) {
        return (new LootContext.Builder((new LootParams.Builder(level))
            .withParameter(LootContextParams.TARGET_ENTITY, this)
            .withParameter(LootContextParams.INTERACTING_ENTITY, entity)
            .withParameter(LootContextParams.TOOL, stack)
            .create(LootContextParamSets.ENTITY_INTERACT))).create(Optional.empty());
    }
}
