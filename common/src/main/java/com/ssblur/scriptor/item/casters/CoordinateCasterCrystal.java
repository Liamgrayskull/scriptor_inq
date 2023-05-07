package com.ssblur.scriptor.item.casters;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.ssblur.scriptor.events.TomeReloadListener;
import com.ssblur.scriptor.events.messages.TraceNetwork;
import com.ssblur.scriptor.helpers.ComponentHelper;
import com.ssblur.scriptor.helpers.DictionarySavedData;
import com.ssblur.scriptor.helpers.LimitedBookSerializer;
import com.ssblur.scriptor.helpers.targetable.EntityTargetable;
import com.ssblur.scriptor.helpers.targetable.Targetable;
import com.ssblur.scriptor.item.interfaces.ItemWithCustomRenderer;
import com.ssblur.scriptor.word.Spell;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CoordinateCasterCrystal extends CasterCrystal {
  public CoordinateCasterCrystal(Properties properties) {
    super(properties);
  }

  public static class BlockPosDirection extends Pair<BlockPos, Direction> {
    BlockPos left;
    Direction right;
    public BlockPosDirection(BlockPos left, Direction right) {
      this.left = left;
      this.right = right;
    }

    @Override
    public BlockPos getLeft() {
      return left;
    }

    @Override
    public Direction getRight() {
      return right;
    }

    @Override
    public Direction setValue(Direction value) {
      return right = value;
    }
  }

  @Override
  public List<Targetable> getTargetables(ItemStack itemStack, Level level) {
    ArrayList<Targetable> list = new ArrayList<>();
    if(itemStack.getTag() != null && itemStack.getTag().contains("coordinates")){
      var coords = itemStack.getTag().getList("coordinates", ListTag.TAG_LONG_ARRAY);
      for(var tag: coords) {
        if(tag instanceof LongArrayTag array) {
          list.add(new Targetable(level, new BlockPos(array.get(0).getAsLong(), array.get(1).getAsLong(), array.get(2).getAsLong())));
        }
      }
    }
    return list;
  }

  @Override
  public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
    super.appendHoverText(itemStack, level, list, tooltipFlag);
    var font = Minecraft.getInstance().font;

    var coordinates = getCoordinates(itemStack);
    for(var pair: coordinates) {
      var coordinate = pair.getLeft();
      ComponentHelper.updateTooltipWith(ChatFormatting.GRAY, list, "lore.scriptor.coordinate_crystal_3", coordinate.getX(), coordinate.getY(), coordinate.getZ());
    }

    if(coordinates.isEmpty())
      list.add(Component.translatable("lore.scriptor.coordinate_crystal_1").withStyle(ChatFormatting.GRAY));
    else {
      if(coordinates.size() < 4)
        list.add(Component.translatable("lore.scriptor.coordinate_crystal_2").withStyle(ChatFormatting.GRAY));
      list.add(Component.translatable("lore.scriptor.crystal_reset").withStyle(ChatFormatting.GRAY));
    }
  }

  @Override
  public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
    var result = super.use(level, player, interactionHand);

    if(!level.isClientSide) {
      ServerLevel server = (ServerLevel) level;

      ItemStack itemStack = player.getItemInHand(interactionHand);
      TraceNetwork.requestTraceData(player, target -> addCoordinate(itemStack, target.getTargetBlockPos(), target.getFacing()));
    }

    return result;
  }

  public static void addCoordinate(ItemStack itemStack, BlockPos pos, Direction direction) {
    var tag = itemStack.getOrCreateTag();

    if(!tag.contains("coordinates"))
      tag.put("coordinates", new ListTag());
    ListTag list = tag.getList("coordinates", ListTag.TAG_LONG_ARRAY);

    if(list.size() < 4)
      list.add(new LongArrayTag(new long[] {pos.getX(), pos.getY(), pos.getZ(), direction.ordinal()}));
  }

  public static List<BlockPosDirection> getCoordinates(ItemStack itemStack) {
    ArrayList<BlockPosDirection> list = new ArrayList<>();
    if(itemStack.getTag() != null && itemStack.getTag().contains("coordinates")){
      var coords = itemStack.getTag().getList("coordinates", ListTag.TAG_LONG_ARRAY);
      for(var tag: coords) {
        if(tag instanceof LongArrayTag array && array.size() == 4)
          list.add(new BlockPosDirection(
            new BlockPos(array.get(0).getAsLong(), array.get(1).getAsLong(), array.get(2).getAsLong()),
            Direction.values()[array.get(3).getAsInt()]
        ));
      }
    }
    return list;
  }
}