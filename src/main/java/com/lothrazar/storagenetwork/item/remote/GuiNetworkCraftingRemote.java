package com.lothrazar.storagenetwork.item.remote;

import com.lothrazar.storagenetwork.StorageNetwork;
import com.lothrazar.storagenetwork.api.EnumSortType;
import com.lothrazar.storagenetwork.api.IGuiNetwork;
import com.lothrazar.storagenetwork.gui.NetworkWidget;
import com.lothrazar.storagenetwork.jei.JeiHooks;
import com.lothrazar.storagenetwork.network.ClearRecipeMessage;
import com.lothrazar.storagenetwork.network.RequestMessage;
import com.lothrazar.storagenetwork.network.SettingsSyncMessage;
import com.lothrazar.storagenetwork.registry.PacketRegistry;
import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.List;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GuiNetworkCraftingRemote extends ContainerScreen<ContainerNetworkCraftingRemote> implements IGuiNetwork {

  private static final int KEY_BACKSPACE = 259;
  private static final int KEY_ESC = 256;
  private static final int HEIGHT = 256;
  private static final int WIDTH = 176;
  private final ResourceLocation textureCraft = new ResourceLocation(StorageNetwork.MODID, "textures/gui/request.png");
  private final NetworkWidget network;
  private final ItemStack remote;

  public GuiNetworkCraftingRemote(ContainerNetworkCraftingRemote screenContainer, PlayerInventory inv, ITextComponent titleIn) {
    super(screenContainer, inv, titleIn);
    //since the rightclick action forces only MAIN_HAND openings, is ok
    this.remote = inv.player.getHeldItem(Hand.MAIN_HAND);
    network = new NetworkWidget(this);
    network.setLines(4);
    this.xSize = WIDTH;
    this.ySize = HEIGHT;
    network.fieldHeight = 90;
  }

  @Override
  public void drawGradient(MatrixStack ms, int x, int y, int x2, int y2, int u, int v) {
    super.fillGradient(ms, x, y, x2, y2, u, v);
  }

  @Override
  public void renderStackTooltip(MatrixStack ms, ItemStack stack, int mousex, int mousey) {
    super.renderTooltip(ms, stack, mousex, mousey);
  }

  @Override
  public void setStacks(List<ItemStack> stacks) {
    network.stacks = stacks;
  }

  @Override
  public boolean isJeiSearchSynced() {
    return ItemStorageCraftingRemote.isJeiSearchSynced(remote);
  }

  @Override
  public void setJeiSearchSynced(boolean val) {
    ItemStorageCraftingRemote.setJeiSearchSynced(remote, val);
  }

  @Override
  public boolean getDownwards() {
    return ItemStorageCraftingRemote.getDownwards(remote);
  }

  @Override
  public void setDownwards(boolean val) {
    ItemStorageCraftingRemote.setDownwards(remote, val);
  }

  @Override
  public EnumSortType getSort() {
    return ItemStorageCraftingRemote.getSort(remote);
  }

  @Override
  public void setSort(EnumSortType val) {
    ItemStorageCraftingRemote.setSort(remote, val);
  }

  @Override
  public void init() {
    super.init();
    int searchLeft = guiLeft + 81, searchTop = guiTop + 160, width = 85;
    searchTop = guiTop + 96;
    network.searchBar = new TextFieldWidget(font,
        searchLeft, searchTop,
        width, font.FONT_HEIGHT, null);
    network.searchBar.setMaxStringLength(30);
    network.initSearchbar();
    network.initButtons();
    this.addButton(network.directionBtn);
    this.addButton(network.sortBtn);
    if (JeiHooks.isJeiLoaded()) {
      addButton(network.jeiBtn);
    }
  }

  @Override
  public void render(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
    this.renderBackground(ms);
    super.render(ms, mouseX, mouseY, partialTicks);
    this.renderHoveredTooltip(ms, mouseX, mouseY); //      this.renderHoveredToolTip(mouseX, mouseY);
    network.searchBar.render(ms, mouseX, mouseY, partialTicks);
    network.render();
  }

  @Override
  protected void drawGuiContainerBackgroundLayer(MatrixStack ms, float partialTicks, int mouseX, int mouseY) {
    this.minecraft.getTextureManager().bindTexture(textureCraft);
    int k = (this.width - this.xSize) / 2;
    int l = (this.height - this.ySize) / 2;
    this.blit(ms, k, l, 0, 0, this.xSize, this.ySize);
    network.applySearchTextToSlots();
    network.renderItemSlots(ms, mouseX, mouseY, font);
  }

  @Override
  public void drawGuiContainerForegroundLayer(MatrixStack ms, int mouseX, int mouseY) {
    network.drawGuiContainerForegroundLayer(ms, mouseX, mouseY, font);
  }

  boolean isScrollable(double x, double y) {
    int scrollHeight = 135;
    return isPointInRegion(0, 0,
        this.width - 8, scrollHeight,
        x, y);
  }

  @Override
  public boolean mouseScrolled(double x, double y, double mouseButton) {
    super.mouseScrolled(x, y, mouseButton);
    //<0 going down
    // >0 going up
    if (isScrollable(x, y) && mouseButton != 0) {
      network.mouseScrolled(mouseButton);
    }
    return true;
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
    super.mouseClicked(mouseX, mouseY, mouseButton);
    network.mouseClicked(mouseX, mouseY, mouseButton);
    //recipe clear thingy
    //TODO: network needs isCrafting and isPointInRegion access to refactor
    // OR make real button lol
    int rectX = 63;
    int rectY = 110;
    if (isPointInRegion(rectX, rectY, 7, 7, mouseX, mouseY)) {
      PacketRegistry.INSTANCE.sendToServer(new ClearRecipeMessage());
      PacketRegistry.INSTANCE.sendToServer(new RequestMessage(0, ItemStack.EMPTY, false, false));
      return true;
    }
    return true;
  }

  @Override
  public boolean keyPressed(int keyCode, int scanCode, int b) {
    InputMappings.Input mouseKey = InputMappings.getInputByCode(keyCode, scanCode);
    if (keyCode == KEY_ESC) {
      minecraft.player.closeScreen();
      return true; // Forge MC-146650: Needs to return true when the key is handled.
    }
    if (network.searchBar.isFocused()) {
      if (keyCode == KEY_BACKSPACE) { // BACKSPACE
        network.syncTextToJei();
      }
      network.searchBar.keyPressed(keyCode, scanCode, b);
      return true;
    }
    else if (network.stackUnderMouse.isEmpty()) {
      try {
        JeiHooks.testJeiKeybind(mouseKey, network.stackUnderMouse);
      }
      catch (Throwable e) {
        StorageNetwork.LOGGER.error("JEI compat issue ", e);
      }
    }
    //Regardless of above branch, also check this
    if (minecraft.gameSettings.keyBindInventory.isActiveAndMatches(mouseKey)) {
      minecraft.player.closeScreen();
      return true; // Forge MC-146650: Needs to return true when the key is handled.
    }
    return super.keyPressed(keyCode, scanCode, b);
  }

  @Override
  public boolean charTyped(char typedChar, int keyCode) {
    if (network.charTyped(typedChar, keyCode)) {
      return true;
    }
    return false;
  }

  @Override
  public boolean isInRegion(int x, int y, int width, int height, double mouseX, double mouseY) {
    return super.isPointInRegion(x, y, width, height, mouseX, mouseY);
  }

  @Override
  public void syncDataToServer() {
    PacketRegistry.INSTANCE.sendToServer(new SettingsSyncMessage(null, getDownwards(), getSort(), this.isJeiSearchSynced()));
  }
}
