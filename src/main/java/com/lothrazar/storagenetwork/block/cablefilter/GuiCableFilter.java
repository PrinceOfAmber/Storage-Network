package com.lothrazar.storagenetwork.block.cablefilter;
import com.google.common.collect.Lists;
import com.lothrazar.storagenetwork.StorageNetwork;
import com.lothrazar.storagenetwork.block.request.GuiButtonRequest;
import com.lothrazar.storagenetwork.gui.IGuiPrivate;
import com.lothrazar.storagenetwork.gui.ItemSlotNetwork;
import com.lothrazar.storagenetwork.network.CableDataMessage;
import com.lothrazar.storagenetwork.registry.PacketRegistry;
import com.lothrazar.storagenetwork.util.inventory.FilterItemStackHandler;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Foods;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.List;

public class GuiCableFilter extends ContainerScreen<ContainerCableFilter> implements IGuiPrivate {

  private final ResourceLocation texture = new ResourceLocation(StorageNetwork.MODID, "textures/gui/cable.png");
  //  protected GuiCableButton btnInputOutputStorage;
  ContainerCableFilter containerCableLink;
  private GuiButtonRequest btnMinus;
  private GuiButtonRequest btnPlus;
  private GuiButtonRequest btnWhite;
  private GuiButtonRequest btnImport;
  private boolean isWhitelist;
  private List<ItemSlotNetwork> itemSlotsGhost;

  public GuiCableFilter(ContainerCableFilter containerCableFilter, PlayerInventory inv, ITextComponent name) {
    super(containerCableFilter, inv, name);
    this.containerCableLink = containerCableFilter;
  }

  @Override
  public void init() {
    super.init();
    this.isWhitelist = containerCableLink.link.getFilter().isWhitelist;
    int x = guiLeft + 7, y = guiTop + 8;
    btnMinus = addButton(new GuiButtonRequest(x, y, "-", (p) -> {
      this.syncData(-1);
    }));
    x += 30;
    btnPlus = addButton(new GuiButtonRequest(x, y, "+", (p) -> {
      this.syncData(+1);
    }));//.setTexture(new ResourceLocation(StorageNetwork.MODID, "textures/gui/button.png"));
    x +=20;
    btnWhite = addButton(new GuiButtonRequest(x, y, "", (p) -> {
      this.isWhitelist = !this.isWhitelist;
      this.syncData(0);
    }));
    x += 20;
    btnImport = addButton(new GuiButtonRequest(x, y, "", (p) -> {
      importFilterSlots();
    }));//.setTexture(new ResourceLocation(StorageNetwork.MODID, "textures/gui/button_full.png"));
    btnImport.setMessage("I");
  }

  private void importFilterSlots() {
    PacketRegistry.INSTANCE.sendToServer(new CableDataMessage(CableDataMessage.CableMessageType.IMPORT_FILTER.ordinal()));
  }

  private void sendStackSlot(int value, ItemStack stack) {
    PacketRegistry.INSTANCE.sendToServer(new CableDataMessage(CableDataMessage.CableMessageType.SAVE_FITLER.ordinal(), value, stack));
  }

  private void syncData(int priority) {
    //    containerCableLink.link.setPriority(priority);
    containerCableLink.link.getFilter().isWhitelist = this.isWhitelist;
    PacketRegistry.INSTANCE.sendToServer(new CableDataMessage(CableDataMessage.CableMessageType.SYNC_DATA.ordinal(),
        priority, isWhitelist));
  }

  @Override
  public void render(int mouseX, int mouseY, float partialTicks) {
    renderBackground();
    super.render(mouseX, mouseY, partialTicks);
    renderHoveredToolTip(mouseX, mouseY);
    if (containerCableLink == null || containerCableLink.link == null) {
      return;
    }
    btnWhite.setMessage(this.isWhitelist ? "W" : "B");
  }

  @Override
  public void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
    super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    int priority = containerCableLink.link.getPriority();
    font.drawString(String.valueOf(priority),
        30 - font.getStringWidth(String.valueOf(priority)) / 2,
        5,// btnMinus.y,
        4210752);
    this.drawTooltips(mouseX, mouseY);
  }

  private void drawTooltips(final int mouseX, final int mouseY) {
    if (btnImport != null && btnImport.isMouseOver(mouseX, mouseY)) {
      renderTooltip(Lists.newArrayList(I18n.format("gui.storagenetwork.import")), mouseX - guiLeft, mouseY - guiTop);
    }
    if (btnWhite != null && btnWhite.isMouseOver(mouseX, mouseY)) {
      renderTooltip(Lists.newArrayList(I18n.format(this.isWhitelist ? "gui.storagenetwork.gui.whitelist" : "gui.storagenetwork.gui.blacklist")), mouseX - guiLeft, mouseY - guiTop);
    }
    if (btnMinus != null && btnMinus.isMouseOver(mouseX, mouseY)) {
      renderTooltip(Lists.newArrayList(I18n.format("gui.storagenetwork.priority.down")), mouseX - guiLeft, mouseY - guiTop);
    }
    if (btnPlus != null && btnPlus.isMouseOver(mouseX, mouseY)) {
      renderTooltip(Lists.newArrayList(I18n.format("gui.storagenetwork.priority.up")), mouseX - guiLeft, mouseY - guiTop);
    }
  }

  public static final int SLOT_SIZE = 18;

  @Override protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
    GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
    minecraft.getTextureManager().bindTexture(texture);
    int xCenter = (width - xSize) / 2;
    int yCenter = (height - ySize) / 2;
    blit(xCenter, yCenter, 0, 0, xSize, ySize);
    // TODO CHECK BOXES
    //    checkOreBtn.setIsChecked(containerCableLink.link.filters.ores);
    //    checkNbtBtn.setIsChecked(containerCableLink.link.filters.nbt);
    itemSlotsGhost = Lists.newArrayList();
    //TODO: shared with GuiCableIO
    int rows = 2;
    int cols = 9;
    int index = 0;
    int y = 35;
    for (int row = 0; row < rows; row++) {
      for (int col = 0; col < cols; col++) {
        ItemStack stack = containerCableLink.link.getFilter().getStackInSlot(index);
        int x = 8 + col * SLOT_SIZE;
        itemSlotsGhost.add(new ItemSlotNetwork(this, stack, guiLeft + x, guiTop + y, stack.getCount(), guiLeft, guiTop, true));
        index++;
      }
      //move down to second row
      y += SLOT_SIZE;
    }
    for (ItemSlotNetwork s : itemSlotsGhost) {
      s.drawSlot(font, mouseX, mouseY);
    }
  }

  public void setFilterItems(List<ItemStack> stacks) {
    FilterItemStackHandler filter = this.containerCableLink.link.getFilter();
    for (int i = 0; i < stacks.size(); i++) {
      ItemStack s = stacks.get(i);
      filter.setStackInSlot(i, s);
    }
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
    ItemStack mouse = minecraft.player.inventory.getItemStack();
    for (int i = 0; i < this.itemSlotsGhost.size(); i++) {
      ItemSlotNetwork slot = itemSlotsGhost.get(i);
      if (slot.isMouseOverSlot((int) mouseX, (int) mouseY)) {
        //
        StorageNetwork.log(mouseButton + " over filter " + slot.getStack() + " MOUSE + " + mouse);
        if (slot.getStack().isEmpty() == false) {
          //i hit non-empty slot, clear it no matter what
          if (mouseButton == 1) {
            slot.getStack().setCount(1);
          }
          else {
            slot.setStack(ItemStack.EMPTY);
          }
          this.sendStackSlot(i, slot.getStack());
          return true;
        }
        else {
          //i hit an empty slot, save what im holding
          slot.setStack(mouse.copy());
          this.sendStackSlot(i, mouse.copy());
          return true;
        }
      }
    }
    return super.mouseClicked(mouseX, mouseY, mouseButton);
  }

  public boolean isInRegion(int rectX, int rectY, int rectWidth, int rectHeight, int pointX, int pointY) {
    return super.isPointInRegion(rectX, rectY, rectWidth, rectHeight, pointX, pointY);
  }

  public void renderStackToolTip(ItemStack stack, int x, int y) {
    super.renderTooltip(stack, x, y);
  }

  public void drawGradientRect(int left, int top, int right, int bottom, int startColor, int endColor) {
    super.fillGradient(left, top, right, bottom, startColor, endColor);
  }
}