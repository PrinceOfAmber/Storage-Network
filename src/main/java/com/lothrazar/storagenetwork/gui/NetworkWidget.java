package com.lothrazar.storagenetwork.gui;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.lothrazar.storagenetwork.StorageNetwork;
import com.lothrazar.storagenetwork.api.IGuiNetwork;
import com.lothrazar.storagenetwork.api.util.UtilTileEntity;
import com.lothrazar.storagenetwork.block.request.GuiNetworkTable;
import com.lothrazar.storagenetwork.gui.GuiButtonRequest.TextureEnum;
import com.lothrazar.storagenetwork.gui.inventory.ItemSlotNetwork;
import com.lothrazar.storagenetwork.jei.JeiHooks;
import com.lothrazar.storagenetwork.jei.JeiSettings;
import com.lothrazar.storagenetwork.network.InsertMessage;
import com.lothrazar.storagenetwork.network.RequestMessage;
import com.lothrazar.storagenetwork.registry.PacketRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

public class NetworkWidget {

  private final IGuiNetwork gui;
  public TextFieldWidget searchBar;
  long lastClick;
  int page = 1, maxPage = 1;
  public List<ItemStack> stacks;
  List<ItemSlotNetwork> slots;
  private int lines = 4;
  private int columns = 9;
  public ItemStack stackUnderMouse = ItemStack.EMPTY;
  public int fieldHeight = 90;
  public GuiButtonRequest directionBtn;
  public GuiButtonRequest sortBtn;
  public GuiButtonRequest jeiBtn;
  public GuiButtonRequest clearTextBtn;

  public NetworkWidget(IGuiNetwork gui) {
    this.gui = gui;
    stacks = Lists.newArrayList();
    slots = Lists.newArrayList();
    PacketRegistry.INSTANCE.sendToServer(new RequestMessage());
    lastClick = System.currentTimeMillis();
  }

  public void applySearchTextToSlots() {
    String searchText = searchBar.getText();
    List<ItemStack> stacksToDisplay = searchText.equals("") ? Lists.newArrayList(stacks) : Lists.newArrayList();
    if (!searchText.equals("")) {
      for (ItemStack stack : stacks) {
        if (doesStackMatchSearch(stack)) {
          stacksToDisplay.add(stack);
        }
      }
    }
    this.sortStackWrappers(stacksToDisplay);
    this.applyScrollPaging(stacksToDisplay);
    this.rebuildItemSlots(stacksToDisplay);
  }

  public void clearSearch() {
    if (searchBar == null) {
      return;
    }
    searchBar.setText("");
    if (JeiSettings.isJeiSearchSynced()) {
      JeiHooks.setFilterText("");
    }
  }

  private boolean doesStackMatchSearch(ItemStack stack) {
    String searchText = searchBar.getText();
    if (searchText.startsWith("@")) {
      String name = UtilTileEntity.getModNameForItem(stack.getItem());
      return name.toLowerCase().contains(searchText.toLowerCase().substring(1));
    }
    else if (searchText.startsWith("#")) {
      String tooltipString;
      Minecraft mc = Minecraft.getInstance();
      List<ITextComponent> tooltip = stack.getTooltip(mc.player, ITooltipFlag.TooltipFlags.NORMAL);
      tooltipString = Joiner.on(' ').join(tooltip).toLowerCase();
      //      tooltipString = ChatFormatting.stripFormatting(tooltipString);
      return tooltipString.toLowerCase().contains(searchText.toLowerCase().substring(1));
    }
    //TODO : tag search?
    //    else if (searchText.startsWith("$")) {
    //      StringBuilder oreDictStringBuilder = new StringBuilder();
    //      for (int oreId : OreDictionary.getOreIDs(stack)) {
    //        String oreName = OreDictionary.getOreName(oreId);
    //        oreDictStringBuilder.append(oreName).append(' ');
    //      }
    //      return oreDictStringBuilder.toString().toLowerCase().contains(searchText.toLowerCase().substring(1));
    //    }
    //      return creativeTabStringBuilder.toString().toLowerCase().contains(searchText.toLowerCase().substring(1));
    //    }
    else {
      return stack.getDisplayName().toString().toLowerCase().contains(searchText.toLowerCase());
    }
  }

  public boolean canClick() {
    return System.currentTimeMillis() > lastClick + 100L;
  }

  int getLines() {
    return lines;
  }

  int getColumns() {
    return columns;
  }

  public void setLines(int v) {
    lines = v;
  }

  void setColumns(int v) {
    columns = v;
  }

  public void applyScrollPaging(List<ItemStack> stacksToDisplay) {
    maxPage = stacksToDisplay.size() / (getColumns());
    if (stacksToDisplay.size() % (getColumns()) != 0) {
      maxPage++;
    }
    maxPage -= (getLines() - 1);
    if (maxPage < 1) {
      maxPage = 1;
    }
    if (page < 1) {
      page = 1;
    }
    if (page > maxPage) {
      page = maxPage;
    }
  }

  public void mouseScrolled(double mouseButton) {
    //<0 going down
    // >0 going up
    if (mouseButton > 0 && page > 1) {
      page--;
    }
    if (mouseButton < 0 && page < maxPage) {
      page++;
    }
  }

  public void rebuildItemSlots(List<ItemStack> stacksToDisplay) {
    slots = Lists.newArrayList();
    int index = (page - 1) * (getColumns());
    for (int row = 0; row < getLines(); row++) {
      for (int col = 0; col < getColumns(); col++) {
        if (index >= stacksToDisplay.size()) {
          break;
        }
        int in = index;
        //        StorageNetwork.LOGGER.info(in + "GUI STORAGE rebuildItemSlots "+stacksToDisplay.get(in));
        slots.add(new ItemSlotNetwork(gui, stacksToDisplay.get(in),
            gui.getGuiLeft() + 8 + col * 18,
            gui.getGuiTop() + 10 + row * 18,
            stacksToDisplay.get(in).getCount(),
            gui.getGuiLeft(), gui.getGuiTop(), true));
        index++;
      }
    }
  }

  public boolean inSearchBar(double mouseX, double mouseY) {
    return gui.isInRegion(
      searchBar.x - gui.getGuiLeft(), searchBar.y - gui.getGuiTop(), // x, y
      searchBar.getWidth(), searchBar.getHeight(), // width, height
      mouseX, mouseY
    );
  }

  public void initSearchbar() {
    searchBar.setEnableBackgroundDrawing(false);
    searchBar.setVisible(true);
    searchBar.setTextColor(16777215);
    searchBar.setFocused2(true);
    if (JeiSettings.isJeiLoaded() && JeiSettings.isJeiSearchSynced()) {
      searchBar.setText(JeiHooks.getFilterText());
    }
  }

  public void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
    for (ItemSlotNetwork s : slots) {
      if (s != null && s.isMouseOverSlot(mouseX, mouseY)) {
        s.drawTooltip(mouseX, mouseY);
      }
    }
    if (directionBtn != null && directionBtn.isMouseOver(mouseX, mouseY)) {
      gui.renderTooltip(Lists.newArrayList(I18n.format("gui.storagenetwork.sort")),
          mouseX - gui.getGuiLeft(), mouseY - gui.getGuiTop());
    }
    if (sortBtn != null && sortBtn.isMouseOver(mouseX, mouseY)) {
      gui.renderTooltip(Lists.newArrayList(
          I18n.format("gui.storagenetwork.req.tooltip_" + gui.getSort())),
          mouseX - gui.getGuiLeft(), mouseY - gui.getGuiTop());
    }
    if (clearTextBtn != null && clearTextBtn.isMouseOver(mouseX, mouseY)) {
      gui.renderTooltip(Lists.newArrayList(
          I18n.format("gui.storagenetwork.tooltip_clear")),
          mouseX - gui.getGuiLeft(), mouseY - gui.getGuiTop());
    }
    if (JeiSettings.isJeiLoaded() && jeiBtn != null && jeiBtn.isMouseOver(mouseX, mouseY)) {
      String s = I18n.format(JeiSettings.isJeiSearchSynced() ? "gui.storagenetwork.fil.tooltip_jei_on" : "gui.storagenetwork.fil.tooltip_jei_off");
      gui.renderTooltip(Lists.newArrayList(s),
          mouseX - gui.getGuiLeft(), mouseY - gui.getGuiTop());
    }
    if (this.inSearchBar(mouseX, mouseY)) {
      List<String> lis = Lists.newArrayList();
      if (!Screen.hasShiftDown()) {
        lis.add(I18n.format("gui.storagenetwork.shift"));
      }
      else {
        lis.add(I18n.format("gui.storagenetwork.fil.tooltip_0"));//@
        lis.add(I18n.format("gui.storagenetwork.fil.tooltip_1"));//#
        //TODO: tag search
        //        lis.add(I18n.format("gui.storagenetwork.fil.tooltip_2"));//$
        lis.add(I18n.format("gui.storagenetwork.fil.tooltip_3"));//clear
      }
      gui.renderTooltip(lis, mouseX - gui.getGuiLeft(), mouseY - gui.getGuiTop());
    }
  }

  public void renderItemSlots(int mouseX, int mouseY, FontRenderer font) {
    stackUnderMouse = ItemStack.EMPTY;
    for (ItemSlotNetwork slot : slots) {
      slot.drawSlot(font, mouseX, mouseY);
      if (slot.isMouseOverSlot(mouseX, mouseY)) {
        stackUnderMouse = slot.getStack();
      }
    }
    if (slots.isEmpty()) {
      stackUnderMouse = ItemStack.EMPTY;
    }
  }

  public boolean charTyped(char typedChar, int keyCode) {
    if (searchBar.isFocused() && searchBar.charTyped(typedChar, keyCode)) {
      PacketRegistry.INSTANCE.sendToServer(new RequestMessage(0, ItemStack.EMPTY, false, false));
      if (JeiSettings.isJeiLoaded() && JeiSettings.isJeiSearchSynced()) {
        JeiHooks.setFilterText(searchBar.getText());
      }
      return true;
    }
    return false;
  }

  public void mouseClicked(double mouseX, double mouseY, int mouseButton) {
    searchBar.setFocused2(false);
    if (inSearchBar(mouseX, mouseY)) {
      searchBar.setFocused2(true);
      if (mouseButton == UtilTileEntity.MOUSE_BTN_RIGHT) {
        clearSearch();
        return;
      }
    }
    ItemStack stackCarriedByMouse = StorageNetwork.proxy.getClientPlayer().inventory.getItemStack();
    if (!stackUnderMouse.isEmpty()
        && (mouseButton == UtilTileEntity.MOUSE_BTN_LEFT || mouseButton == UtilTileEntity.MOUSE_BTN_RIGHT)
        && stackCarriedByMouse.isEmpty() &&
        this.canClick()) {
      PacketRegistry.INSTANCE.sendToServer(new RequestMessage(mouseButton, this.stackUnderMouse.copy(), Screen.hasShiftDown(),
          Screen.hasAltDown() || Screen.hasControlDown()));
      this.lastClick = System.currentTimeMillis();
    }
    else if (!stackCarriedByMouse.isEmpty() && inField((int) mouseX, (int) mouseY) &&
        this.canClick()) {
          //0 isd getDim()
          PacketRegistry.INSTANCE.sendToServer(new InsertMessage(0, mouseButton));
          this.lastClick = System.currentTimeMillis();
        }
  }

  private boolean inField(int mouseX, int mouseY) {
    return mouseX > (gui.getGuiLeft() + 7) && mouseX < (gui.getGuiLeft() + GuiNetworkTable.WIDTH - 7)
        && mouseY > (gui.getGuiTop() + 7) && mouseY < (gui.getGuiTop() + fieldHeight);
  }

  public void initButtons() {
    int y = this.searchBar.y - 4;
    this.directionBtn = new GuiButtonRequest(
        gui.getGuiLeft() + 6, y, "", (p) -> {
          gui.setDownwards(!gui.getDownwards());
          gui.syncDataToServer();
        });
    directionBtn.setHeight(16);
    this.sortBtn = new GuiButtonRequest(gui.getGuiLeft() + 22, y, "", (p) -> {
      gui.setSort(gui.getSort().next());
      gui.syncDataToServer();
    });
    sortBtn.setHeight(16);
    jeiBtn = new GuiButtonRequest(gui.getGuiLeft() + 38, y, "", (p) -> {
      JeiSettings.setJeiSearchSync(!JeiSettings.isJeiSearchSynced());
    });
    jeiBtn.setHeight(16);
    clearTextBtn = new GuiButtonRequest(gui.getGuiLeft() + 63, y, "X", (p) -> {
      this.clearSearch();
    });
    clearTextBtn.setHeight(16);
  }

  public void sortStackWrappers(List<ItemStack> stacksToDisplay) {
    Collections.sort(stacksToDisplay, new Comparator<ItemStack>() {

      final int mul = gui.getDownwards() ? -1 : 1;

      @Override
      public int compare(ItemStack o2, ItemStack o1) {
        switch (gui.getSort()) {
          case AMOUNT:
            return Integer.compare(o1.getCount(), o2.getCount()) * mul;
          case NAME:
            return o2.getDisplayName().toString().compareToIgnoreCase(o1.getDisplayName().toString()) * mul;
          case MOD:
            return UtilTileEntity.getModNameForItem(o2.getItem()).compareToIgnoreCase(UtilTileEntity.getModNameForItem(o1.getItem())) * mul;
        }
        return 0;
      }
    });
  }

  public void render() {
    switch (gui.getSort()) {
      case AMOUNT:
        sortBtn.setTextureId(TextureEnum.SORT_AMT);
      break;
      case MOD:
        sortBtn.setTextureId(TextureEnum.SORT_MOD);
      break;
      case NAME:
        sortBtn.setTextureId(TextureEnum.SORT_NAME);
      break;
    }
    directionBtn.setTextureId(gui.getDownwards() ? TextureEnum.SORT_DOWN : TextureEnum.SORT_UP);
    jeiBtn.setTextureId(JeiSettings.isJeiSearchSynced() ? TextureEnum.JEI_GREEN : TextureEnum.JEI_RED);
  }
}
