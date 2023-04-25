package com.ssblur.scriptor.word.action.teleport;

import com.ssblur.scriptor.helpers.targetable.EntityTargetable;
import com.ssblur.scriptor.helpers.targetable.InventoryTargetable;
import com.ssblur.scriptor.helpers.targetable.ItemTargetable;
import com.ssblur.scriptor.helpers.targetable.Targetable;
import com.ssblur.scriptor.word.action.Action;
import com.ssblur.scriptor.word.descriptor.Descriptor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.core.jmx.Server;

public class SwapAction extends Action {
  @Override
  public void apply(Targetable caster, Targetable targetable, Descriptor[] descriptors) {
    if(targetable.getLevel().isClientSide
        || caster == null
        || caster.getLevel() == null
        || targetable.getLevel() == null) return;

    teleport(caster, targetable);
    teleport(targetable, caster);
  }

  protected void teleport(Targetable from, Targetable to) {
    if(
      from instanceof InventoryTargetable fromInventory
        && to instanceof InventoryTargetable toInventory
        && fromInventory.getContainer() != null
        && toInventory.getContainer() != null
    ) {
      var itemStack = fromInventory.getContainer().getItem(fromInventory.getTargetedSlot());
      if(toInventory.getContainer().canPlaceItem(toInventory.getTargetedSlot(), itemStack)) {
        var itemStack2 = toInventory.getContainer().getItem(toInventory.getTargetedSlot());
        var newItemStack = itemStack.copy();
        newItemStack.setCount(1);
        if(itemStack2.isEmpty()) {

          itemStack.shrink(1);
          toInventory.getContainer().setItem(toInventory.getTargetedSlot(), newItemStack);
        } else if(itemStack2.sameItemStackIgnoreDurability(itemStack)) {
          itemStack.shrink(1);
          itemStack2.grow(1);
        }
      }
    } else if(
      from instanceof ItemTargetable fromItem
        && to instanceof InventoryTargetable toInventory
        && toInventory.getContainer() != null
    ) {
      var itemStack = fromItem.getTargetItem();
      if(toInventory.getContainer().canPlaceItem(toInventory.getTargetedSlot(), itemStack)) {
        var itemStack2 = toInventory.getContainer().getItem(toInventory.getTargetedSlot());
        var newItemStack = itemStack.copy();
        newItemStack.setCount(1);
        if(itemStack2.isEmpty()) {
          itemStack.shrink(1);
          toInventory.getContainer().setItem(toInventory.getTargetedSlot(), newItemStack);
        } else if(itemStack2.sameItemStackIgnoreDurability(itemStack)) {
          itemStack.shrink(1);
          itemStack2.grow(1);
        }
      }
    } else if(from instanceof ItemTargetable fromItem && fromItem.shouldTargetItem()) {
      var itemStack = fromItem.getTargetItem();
      itemStack.shrink(1);
      var newItemStack = itemStack.copy();
      newItemStack.setCount(1);
      ItemEntity entity = new ItemEntity(to.getLevel(), to.getTargetPos().x(), to.getTargetPos().y() + 1, to.getTargetPos().z(), newItemStack);
      to.getLevel().addFreshEntity(entity);
    } else if(from instanceof EntityTargetable fromEntity && fromEntity.getTargetEntity() instanceof LivingEntity living) {
      if(living.level != to.getLevel())
        living.changeDimension((ServerLevel) to.getLevel());
      living.teleportTo(to.getTargetPos().x, to.getTargetPos().y, to.getTargetPos().z);
      living.setDeltaMovement(0, 0, 0);
      living.resetFallDistance();
    }
  }

  @Override
  public Cost cost() { return new Cost(6, COSTTYPE.ADDITIVE); }

}