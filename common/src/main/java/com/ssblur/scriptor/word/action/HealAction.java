package com.ssblur.scriptor.word.action;

import com.ssblur.scriptor.helpers.targetable.EntityTargetable;
import com.ssblur.scriptor.helpers.targetable.InventoryTargetable;
import com.ssblur.scriptor.helpers.targetable.ItemTargetable;
import com.ssblur.scriptor.helpers.targetable.Targetable;
import com.ssblur.scriptor.word.descriptor.Descriptor;
import com.ssblur.scriptor.word.descriptor.power.StrengthDescriptor;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;

public class HealAction extends Action {
  @Override
  public void apply(Targetable caster, Targetable targetable, Descriptor[] descriptors) {
    double strength = 2;
    for(var d: descriptors) {
      if(d instanceof StrengthDescriptor strengthDescriptor)
        strength += strengthDescriptor.strengthModifier();
    }

    strength = Math.max(strength, 0);

    if(targetable instanceof InventoryTargetable inventoryTargetable && inventoryTargetable.getContainer() != null) {
      int slot;
      if(inventoryTargetable.shouldIgnoreTargetedSlot())
        slot = inventoryTargetable.getFirstMatchingSlot(itemStack -> itemStack.isDamageableItem() && itemStack.getDamageValue() > 0);
      else
        slot = inventoryTargetable.getTargetedSlot();
      if(slot > 0) {
        var item = inventoryTargetable.getContainer().getItem(slot);
        if (item != null && !item.isEmpty()) {
          if (item.isDamageableItem()) {
            item.setDamageValue(item.getDamageValue() - (int) Math.round(strength));
            return;
          }
        }
      }
    }

    if(targetable instanceof ItemTargetable itemTargetable && itemTargetable.shouldTargetItem()) {
      var item = itemTargetable.getTargetItem();
      if(item != null && !item.isEmpty()) {
        if(item.isDamageableItem()) {
          item.setDamageValue(item.getDamageValue() - (int) Math.round(strength));
          return;
        }
      }
    }

    if(targetable instanceof EntityTargetable entityTargetable) {
      Entity entity = entityTargetable.getTargetEntity();
      Entity source = caster instanceof EntityTargetable casterEntity ? casterEntity.getTargetEntity() : entity;
      if(entity instanceof LivingEntity target)
        if(target.getMobType() == MobType.UNDEAD)
          target.hurt(DamageSource.indirectMagic(source, source), (float) strength);
        else
          target.heal((float) strength);
    }
  }
  @Override
  public Cost cost() { return new Cost(4, COSTTYPE.ADDITIVE); }
}
