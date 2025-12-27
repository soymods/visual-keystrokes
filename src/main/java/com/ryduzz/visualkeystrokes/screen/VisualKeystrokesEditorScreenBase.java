package com.ryduzz.visualkeystrokes.screen;

import com.ryduzz.visualkeystrokes.config.OverlayConfig;
import net.minecraft.client.MinecraftClient;
import com.ryduzz.visualkeystrokes.util.MatrixStackCompat;
import com.ryduzz.visualkeystrokes.util.RenderSnap;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class VisualKeystrokesEditorScreenBase extends Screen implements VisualKeystrokesEditor {
    private static final int SIDEBAR_WIDTH = 160;
    private static final int SIDEBAR_PADDING = 10;
    private static final int SIDEBAR_ENTRY_HEIGHT = 22;
    private static final int SIDEBAR_SCROLL_SPEED = 12;
    private static final int SIDEBAR_SCROLLBAR_WIDTH = 2;
    private static final int HANDLE_SIZE = 6;
    private static final int MIN_GROUP_SIZE = 12;
    private static final int TOOL_BUTTON_SIZE = 18;
    private static final int TOOL_BUTTON_PADDING = 6;
    private static final int TRASH_SIZE = 8;
    private static final int EDIT_ICON_SIZE = 8;
    private static final int EDIT_ICON_GAP = 4;
    private static final int SEARCH_HEIGHT = 18;
    private static final int SEARCH_PADDING = 8;
    private static final int HEADER_Y = 12;
    private static final int HEADER_PADDING = 10;
    private static final int RESET_PADDING_X = 8;
    private static final int RESET_PADDING_Y = 4;
    private static final int SETTINGS_BUTTON_SIZE = 20;
    private static final int SETTINGS_PANEL_WIDTH = 220;
    private static final int SETTINGS_PANEL_HEIGHT = 150;
    private static final int EDITOR_PANEL_WIDTH = 280;
    private static final int EDITOR_PANEL_HEIGHT = 230;
    private static final int COLOR_PICKER_RADIUS = 32;
    private static final int COLOR_PICKER_PADDING = 10;
    private static final int COLOR_PICKER_SLIDER_WIDTH = 8;
    private static final int COLOR_PICKER_FIELD_HEIGHT = 18;
    private static final int GUIDE_COLOR = 0xFF00B7FF;
    private static final Method REFRESH_WIDGET_POSITIONS =
        findMethod(Screen.class, "refreshWidgetPositions");
    private static Method legacyTextFieldOnClick;
    private static Method legacyTextFieldKeyPressed;
    private static Method legacyTextFieldCharTyped;

    private final OverlayConfig config;
    private final List<Group> groups;
    private final Map<String, Group> groupById = new HashMap<>();
    private final Map<String, Template> templates = new LinkedHashMap<>();

    private final List<Group> selectedGroups = new ArrayList<>();
    private DragMode dragMode = DragMode.NONE;
    private ResizeHandle resizeHandle = ResizeHandle.NONE;
    private double dragOffsetX;
    private double dragOffsetY;
    private Bounds dragStartBounds;
    private double dragStartX;
    private double dragStartY;
    private boolean draggingFromSidebar;
    private List<KeySnapshot> resizeSnapshots;
    private boolean lassoActive;
    private double lassoStartX;
    private double lassoStartY;
    private double lassoEndX;
    private double lassoEndY;
    private final List<GuideLine> guideLines = new ArrayList<>();
    private final List<DistanceLabel> distanceLabels = new ArrayList<>();

    private boolean sidebarOpen;
    private float sidebarProgress;
    private double sidebarScroll;
    private double sidebarMaxScroll;
    private String lastSearchText = "";

    protected TextFieldWidget searchField;
    private int resetX;
    private int resetY;
    private int resetWidth;
    private int resetHeight;
    private boolean infoOpen;
    private int headerX;
    private int headerY;
    private int headerWidth;
    private int headerHeight;
    private int infoCloseX;
    private int infoCloseY;
    private int infoCloseWidth;
    private int infoCloseHeight;
    private boolean settingsOpen;
    private int settingsButtonX;
    private int settingsButtonY;
    private int settingsPanelX;
    private int settingsPanelY;
    private boolean editorOpen;
    private int editorPanelX;
    private int editorPanelY;
    private int editorPanelWidth;
    private int editorPanelHeight;
    private int editorCloseX;
    private int editorCloseY;
    private int editorCloseWidth;
    private int editorCloseHeight;
    private int editorResetX;
    private int editorResetY;
    private int editorResetWidth;
    private int editorResetHeight;
    private int editorVisibilityX;
    private int editorVisibilityY;
    private int editorVisibilityWidth;
    private int editorVisibilityHeight;
    private boolean colorPickerOpen;
    private ColorTarget activeColorTarget = ColorTarget.BACKGROUND;
    private ColorPickerDragArea colorPickerDragArea = ColorPickerDragArea.NONE;
    private float pickerHue = 0.0f;
    private float pickerSaturation = 0.0f;
    private float pickerValue = 1.0f;
    private int colorPickerX;
    private int colorPickerY;
    private int colorPickerWidth;
    private int colorPickerHeight;
    private TextFieldWidget colorHexField;

    protected VisualKeystrokesEditorScreenBase(OverlayConfig config) {
        super(Text.literal("Visual Keystrokes"));
        this.config = config;
        this.groups = buildGroups(config);
        rebuildGroups();
        buildTemplates();
    }

    @Override
    protected void init() {
        super.init();
        searchField = new TextFieldWidget(textRenderer, 0, 0, SIDEBAR_WIDTH - SIDEBAR_PADDING * 2, SEARCH_HEIGHT, Text.empty());
        searchField.setMaxLength(32);
        searchField.setSuggestion("Search...");
        searchField.setVisible(false);
        addDrawableChild(searchField);
        colorHexField = new TextFieldWidget(textRenderer, 0, 0, 90, COLOR_PICKER_FIELD_HEIGHT, Text.empty());
        colorHexField.setMaxLength(7);
        colorHexField.setSuggestion("#RRGGBB");
        colorHexField.setVisible(false);
        addDrawableChild(colorHexField);
    }

    protected void refreshWidgetPositions() {
        invokeRefreshWidgetPositions();
    }

    @Override
    public void tick() {
        super.tick();
        float target = sidebarOpen ? 1.0f : 0.0f;
        if (sidebarProgress < target) {
            sidebarProgress = Math.min(target, sidebarProgress + 0.15f);
        } else if (sidebarProgress > target) {
            sidebarProgress = Math.max(target, sidebarProgress - 0.15f);
        }

        if (searchField != null) {
            searchField.setVisible(sidebarProgress > 0.01f);
        }
        if (colorHexField != null) {
            colorHexField.setVisible(colorPickerOpen);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderInGameBackground(context);

        drawHeader(context, mouseX, mouseY);

        boolean dragging = dragMode == DragMode.MOVE || dragMode == DragMode.RESIZE;
        drawOverlayBase(context, !dragging);
        drawSidebar(context, mouseX, mouseY);
        drawSidebarToggle(context, mouseX, mouseY);
        drawSettingsButton(context, mouseX, mouseY);
        if (dragging) {
            drawSelectedOverlay(context);
        }
        if (settingsOpen) {
            drawSettingsPopup(context, mouseX, mouseY);
        }
        if (editorOpen) {
            drawEditorPopup(context, mouseX, mouseY);
        }
        if (colorPickerOpen) {
            drawColorPicker(context, mouseX, mouseY);
        }
        if (infoOpen) {
            drawInfoPopup(context, mouseX, mouseY);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // Avoid the default blur pass in Screen#renderBackground.
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (handleSearchFieldClickLegacy(mouseX, mouseY, button)) {
            return true;
        }
        if (handleMouseClicked(mouseX, mouseY, button, false)) {
            return true;
        }
        return false;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (handleMouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
            return true;
        }
        return false;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (handleMouseReleased(mouseX, mouseY, button)) {
            return true;
        }
        return false;
    }

    protected boolean handleMouseClicked(double mouseX, double mouseY, int button, boolean doubleClick) {
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return false;
        }

        if (colorPickerOpen && handleColorPickerClick(mouseX, mouseY)) {
            return true;
        }

        if (editorOpen && handleEditorPopupClick(mouseX, mouseY)) {
            return true;
        }

        if (handleHeaderClick(mouseX, mouseY)) {
            return true;
        }

        if (infoOpen && handleInfoPopupClick(mouseX, mouseY)) {
            return true;
        }

        if (handleSettingsButtonClick(mouseX, mouseY)) {
            return true;
        }

        if (settingsOpen && handleSettingsPopupClick(mouseX, mouseY)) {
            return true;
        }

        if (handleSidebarToggleClick(mouseX, mouseY)) {
            return true;
        }

        if (handleResetClick(mouseX, mouseY)) {
            return true;
        }

        if (handleSidebarClick(mouseX, mouseY)) {
            return true;
        }

        double overlayX = toOverlayX(mouseX);
        double overlayY = toOverlayY(mouseY);

        Group primary = primarySelected();
        if (primary != null && primary.isVisible()) {
            Bounds selectionBounds = selectedBounds();
            if (selectionBounds != null) {
                if (hitTestEditIcon(selectionBounds, overlayX, overlayY)) {
                    openEditor();
                    return true;
                }
                if (hitTestTrashIcon(selectionBounds, overlayX, overlayY)) {
                    removeSelected();
                    return true;
                }
            }
            ResizeHandle handle = selectionBounds == null ? ResizeHandle.NONE : hitTestHandle(selectionBounds, overlayX, overlayY);
            if (handle != ResizeHandle.NONE) {
                dragMode = DragMode.RESIZE;
                resizeHandle = handle;
                dragStartBounds = selectionBounds;
                resizeSnapshots = KeySnapshot.capture(keysForSelectedGroups());
                dragStartX = overlayX;
                dragStartY = overlayY;
                return true;
            }
        }

        Group hit = hitTestGroup(overlayX, overlayY);
        if (hit != null) {
            if (!selectedGroups.contains(hit)) {
                selectedGroups.clear();
                selectedGroups.add(hit);
                closeColorPicker();
            }
            dragMode = DragMode.MOVE;
            draggingFromSidebar = false;
            Bounds bounds = selectedGroups.size() > 1 ? selectedBounds() : hit.getBounds();
            if (bounds == null) {
                bounds = hit.getBounds();
            }
            dragOffsetX = overlayX - bounds.x;
            dragOffsetY = overlayY - bounds.y;
            return true;
        }

        selectedGroups.clear();
        closeEditor();
        dragMode = DragMode.LASSO;
        lassoActive = true;
        lassoStartX = overlayX;
        lassoStartY = overlayY;
        lassoEndX = overlayX;
        lassoEndY = overlayY;
        return true;
    }

    protected boolean handleMouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (colorPickerOpen && button == GLFW.GLFW_MOUSE_BUTTON_LEFT && handleColorPickerDrag(mouseX, mouseY)) {
            return true;
        }
        if (dragMode == DragMode.NONE) {
            return false;
        }

        double overlayX = toOverlayX(mouseX);
        double overlayY = toOverlayY(mouseY);

        if (dragMode == DragMode.LASSO) {
            lassoEndX = overlayX;
            lassoEndY = overlayY;
            updateLassoSelection();
            return true;
        }

        if (selectedGroups.isEmpty()) {
            return false;
        }

        if (dragMode == DragMode.MOVE) {
            Group primary = primarySelected();
            if (primary == null) {
                return false;
            }
            Bounds bounds = selectedBounds();
            if (bounds == null) {
                bounds = primary.getBounds();
            }
            double targetX = overlayX - dragOffsetX;
            double targetY = overlayY - dragOffsetY;
            if (draggingFromSidebar) {
                targetX = overlayX - bounds.width / 2.0;
                targetY = overlayY - bounds.height / 2.0;
            }
            Bounds targetBounds = new Bounds((int) Math.round(targetX), (int) Math.round(targetY), bounds.width, bounds.height);
            Bounds snapped = applySnapping(targetBounds);
            int dx = snapped.x - bounds.x;
            int dy = snapped.y - bounds.y;
            moveSelectedBy(dx, dy);
            return true;
        }

        if (dragMode == DragMode.RESIZE) {
            resizeSelectedGroup(overlayX, overlayY);
            return true;
        }

        return false;
    }

    protected boolean handleMouseReleased(double mouseX, double mouseY, int button) {
        if (colorPickerDragArea != ColorPickerDragArea.NONE) {
            colorPickerDragArea = ColorPickerDragArea.NONE;
            return true;
        }
        if (dragMode != DragMode.NONE) {
            if (dragMode == DragMode.MOVE && !selectedGroups.isEmpty() && isInSidebarArea(mouseX, mouseY)) {
                for (Group group : selectedGroups) {
                    group.setVisible(false);
                }
                selectedGroups.clear();
            }
            if (dragMode == DragMode.LASSO) {
                selectGroupInLasso();
                lassoActive = false;
            }
            dragMode = DragMode.NONE;
            resizeHandle = ResizeHandle.NONE;
            draggingFromSidebar = false;
            dragStartBounds = null;
            resizeSnapshots = null;
            guideLines.clear();
            distanceLabels.clear();
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (sidebarProgress <= 0.01f) {
            return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        }
        int sidebarWidth = (int) (SIDEBAR_WIDTH * sidebarProgress);
        int sidebarX = width - sidebarWidth;
        int listTop = HEADER_Y + 14 + SEARCH_HEIGHT + SEARCH_PADDING;
        int listBottom = height - SIDEBAR_PADDING - SETTINGS_BUTTON_SIZE - 6;
        int listHeight = Math.max(0, listBottom - listTop);

        if (mouseX >= sidebarX && mouseY >= listTop && mouseY <= listBottom && listHeight > 0 && sidebarMaxScroll > 0) {
            double next = sidebarScroll - verticalAmount * SIDEBAR_SCROLL_SPEED;
            sidebarScroll = Math.max(0, Math.min(next, sidebarMaxScroll));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (searchField != null && searchField.isFocused()) {
            if (invokeSearchFieldKeyPressedLegacy(keyCode, scanCode, modifiers)) {
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                searchField.setFocused(false);
                setFocused(null);
                return true;
            }
        }
        if (colorHexField != null && colorHexField.isFocused()) {
            if (invokeTextFieldKeyPressedLegacy(colorHexField, keyCode, scanCode, modifiers)) {
                applyHexFieldIfValid();
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                colorHexField.setFocused(false);
                setFocused(null);
                return true;
            }
        }
        return handleKeyPressedCommon(keyCode);
    }

    public boolean charTyped(char chr, int modifiers) {
        if (searchField != null && searchField.isFocused()) {
            return invokeSearchFieldCharTypedLegacy(chr, modifiers);
        }
        if (colorHexField != null && colorHexField.isFocused()) {
            boolean handled = invokeTextFieldCharTypedLegacy(colorHexField, chr, modifiers);
            if (handled) {
                applyHexFieldIfValid();
            }
            return handled;
        }
        return false;
    }

    protected boolean handleKeyPressedCommon(int keyCode) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_DELETE || keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            removeSelected();
            return true;
        }
        return false;
    }

    @Override
    public void close() {
        OverlayConfig.save(config);
        MinecraftClient.getInstance().setScreen(null);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private Group primarySelected() {
        if (selectedGroups.isEmpty()) {
            return null;
        }
        return selectedGroups.get(selectedGroups.size() - 1);
    }

    private boolean isKeySelected(OverlayConfig.KeyDefinition key) {
        for (Group group : selectedGroups) {
            if (group.containsKey(key)) {
                return true;
            }
        }
        return false;
    }

    private void drawOverlayBase(DrawContext context, boolean includeSelected) {
        float renderScale = RenderSnap.snapScale(config.scale);
        double offsetX = RenderSnap.snapOffset(config.offsetX, renderScale);
        double offsetY = RenderSnap.snapOffset(config.offsetY, renderScale);
        MatrixStackCompat.push(context.getMatrices());
        MatrixStackCompat.translate(context.getMatrices(), offsetX, offsetY);
        MatrixStackCompat.scale(context.getMatrices(), renderScale, renderScale);

        for (OverlayConfig.KeyDefinition key : config.keys) {
            if (!key.isVisible()) {
                continue;
            }
            if (!includeSelected && isKeySelected(key)) {
                continue;
            }
            int x = key.x;
            int y = key.y;
            int width = key.width;
            int height = key.height;

            int background = resolveBackgroundColor(key);
            int border = resolveBorderColor(key);
            int textColor = resolveTextColor(key);
            context.fill(x, y, x + width, y + height, background);
            drawBorder(context, x, y, width, height, border);

            int textWidth = textRenderer.getWidth(key.label);
            int textX = x + (width - textWidth) / 2;
            int textY = y + (height - textRenderer.fontHeight) / 2;
            context.drawTextWithShadow(textRenderer, key.label, textX, textY, textColor);
        }

        if (includeSelected && !selectedGroups.isEmpty()) {
            if (selectedGroups.size() > 1) {
                for (Group group : selectedGroups) {
                    if (group.isVisible()) {
                        drawSelection(context, group.getBounds(), false);
                    }
                }
            }

            Bounds combined = selectedBounds();
            if (combined != null) {
                drawSelection(context, combined, true);
                drawTrashIcon(context, combined);
                drawEditIcon(context, combined);
            }
        }

        if (lassoActive && dragMode == DragMode.LASSO) {
            drawLasso(context);
        }

        MatrixStackCompat.pop(context.getMatrices());
    }

    private void drawSelectedOverlay(DrawContext context) {
        if (selectedGroups.isEmpty()) {
            return;
        }

        float renderScale = RenderSnap.snapScale(config.scale);
        double offsetX = RenderSnap.snapOffset(config.offsetX, renderScale);
        double offsetY = RenderSnap.snapOffset(config.offsetY, renderScale);
        MatrixStackCompat.push(context.getMatrices());
        MatrixStackCompat.translate(context.getMatrices(), offsetX, offsetY);
        MatrixStackCompat.scale(context.getMatrices(), renderScale, renderScale);

        for (Group group : selectedGroups) {
            for (OverlayConfig.KeyDefinition key : group.keys) {
                if (!key.isVisible()) {
                    continue;
                }
                int x = key.x;
                int y = key.y;
                int width = key.width;
                int height = key.height;

                int background = resolveBackgroundColor(key);
                int border = resolveBorderColor(key);
                int textColor = resolveTextColor(key);
                context.fill(x, y, x + width, y + height, background);
                drawBorder(context, x, y, width, height, border);

                int textWidth = textRenderer.getWidth(key.label);
                int textX = x + (width - textWidth) / 2;
                int textY = y + (height - textRenderer.fontHeight) / 2;
                context.drawTextWithShadow(textRenderer, key.label, textX, textY, textColor);
            }
        }

        if (selectedGroups.size() > 1) {
            for (Group group : selectedGroups) {
                drawSelection(context, group.getBounds(), false);
            }
        }

        Bounds combined = selectedBounds();
        if (combined != null) {
            drawSelection(context, combined, true);
            drawTrashIcon(context, combined);
            drawEditIcon(context, combined);
        }

        if ((dragMode == DragMode.MOVE || dragMode == DragMode.RESIZE) && config.guidesEnabled) {
            drawGuides(context);
        }
        if ((dragMode == DragMode.MOVE || dragMode == DragMode.RESIZE) && config.distanceLabelsEnabled) {
            drawDistanceLabels(context);
        }

        MatrixStackCompat.pop(context.getMatrices());
    }

    private void drawSidebar(DrawContext context, int mouseX, int mouseY) {
        if (sidebarProgress <= 0.01f) {
            resetX = 0;
            resetY = 0;
            resetWidth = 0;
            resetHeight = 0;
            settingsButtonX = 0;
            settingsButtonY = 0;
            return;
        }

        int sidebarWidth = (int) (SIDEBAR_WIDTH * sidebarProgress);
        int sidebarX = width - sidebarWidth;

        context.fill(sidebarX, 0, width, height, 0xCC111111);
        context.drawTextWithShadow(textRenderer, "Add Elements", sidebarX + HEADER_PADDING, HEADER_Y, 0xFFFFFFFF);
        drawResetButton(context, sidebarX, sidebarWidth, mouseX, mouseY);

        int searchX = sidebarX + SIDEBAR_PADDING;
        int searchY = HEADER_Y + 14;
        int searchWidth = sidebarWidth - SIDEBAR_PADDING * 2;
        searchField.setX(searchX);
        searchField.setY(searchY);
        searchField.setWidth(searchWidth);
        searchField.setVisible(true);
        if (searchField.getText().isEmpty()) {
            searchField.setSuggestion("Search...");
        } else {
            searchField.setSuggestion("");
        }

        int listTop = searchY + SEARCH_HEIGHT + SEARCH_PADDING;
        int listBottom = height - SIDEBAR_PADDING - SETTINGS_BUTTON_SIZE - 6;
        int listHeight = Math.max(0, listBottom - listTop);

        String filterText = searchField.getText();
        if (!filterText.equals(lastSearchText)) {
            sidebarScroll = 0;
            lastSearchText = filterText;
        }

        List<Template> filtered = getTemplates(filterText);
        int totalHeight = filtered.isEmpty() ? 0 : filtered.size() * (SIDEBAR_ENTRY_HEIGHT + 6) - 6;
        sidebarMaxScroll = Math.max(0, totalHeight - listHeight);
        sidebarScroll = Math.max(0, Math.min(sidebarScroll, sidebarMaxScroll));

        int scrollOffset = (int) Math.round(sidebarScroll);
        int y = listTop - scrollOffset;
        if (listHeight > 0) {
            context.enableScissor(sidebarX, listTop, width, listBottom);
        }
        for (Template template : filtered) {
            int entryX = sidebarX + SIDEBAR_PADDING;
            int entryY = y;
            int entryWidth = sidebarWidth - SIDEBAR_PADDING * 2;
            int entryHeight = SIDEBAR_ENTRY_HEIGHT;
            Group existing = groupById.get(template.id);
            boolean active = existing != null && existing.isVisible();
            int entryColor = isPointInside(mouseX, mouseY, entryX, entryY, entryWidth, entryHeight)
                ? 0xFF2B2B2B
                : (active ? 0xFF242424 : 0xFF1E1E1E);

            context.fill(entryX, entryY, entryX + entryWidth, entryY + entryHeight, entryColor);
            drawBorder(context, entryX, entryY, entryWidth, entryHeight, 0xFF000000);
            context.drawTextWithShadow(textRenderer, template.displayName, entryX + 6, entryY + 7, 0xFFFFFFFF);
            y += entryHeight + 6;
        }
        if (listHeight > 0) {
            context.disableScissor();
        }

        if (sidebarMaxScroll > 0 && listHeight > 0) {
            int trackX = width - SIDEBAR_PADDING - SIDEBAR_SCROLLBAR_WIDTH;
            context.fill(trackX, listTop, trackX + SIDEBAR_SCROLLBAR_WIDTH, listBottom, 0xFF2B2B2B);
            double ratio = listHeight / (double) totalHeight;
            int thumbHeight = Math.max(12, (int) Math.round(listHeight * ratio));
            int thumbTravel = listHeight - thumbHeight;
            int thumbY = listTop + (int) Math.round((sidebarScroll / sidebarMaxScroll) * thumbTravel);
            context.fill(trackX, thumbY, trackX + SIDEBAR_SCROLLBAR_WIDTH, thumbY + thumbHeight, 0xFFFFFFFF);
        }
    }

    private void drawSettingsButton(DrawContext context, int mouseX, int mouseY) {
        if (sidebarProgress <= 0.01f) {
            return;
        }
        int sidebarWidth = (int) (SIDEBAR_WIDTH * sidebarProgress);
        int sidebarX = width - sidebarWidth;

        settingsButtonX = sidebarX + sidebarWidth - SETTINGS_BUTTON_SIZE - SIDEBAR_PADDING;
        settingsButtonY = height - SETTINGS_BUTTON_SIZE - SIDEBAR_PADDING;

        boolean hovered = isPointInside(mouseX, mouseY, settingsButtonX, settingsButtonY, SETTINGS_BUTTON_SIZE, SETTINGS_BUTTON_SIZE);
        int bgColor = hovered ? 0xFF2B2B2B : 0xFF1E1E1E;
        context.fill(settingsButtonX, settingsButtonY, settingsButtonX + SETTINGS_BUTTON_SIZE, settingsButtonY + SETTINGS_BUTTON_SIZE, bgColor);
        drawBorder(context, settingsButtonX, settingsButtonY, SETTINGS_BUTTON_SIZE, SETTINGS_BUTTON_SIZE, 0xFF000000);

        Text gear = Text.literal("⚙");
        int gearWidth = textRenderer.getWidth(gear);
        int gearX = settingsButtonX + (SETTINGS_BUTTON_SIZE - gearWidth) / 2;
        int gearY = settingsButtonY + (SETTINGS_BUTTON_SIZE - textRenderer.fontHeight) / 2;
        context.drawTextWithShadow(textRenderer, gear, gearX, gearY, 0xFFFFFFFF);
    }

    private void drawSidebarToggle(DrawContext context, int mouseX, int mouseY) {
        int buttonSize = TOOL_BUTTON_SIZE;
        int sidebarWidth = (int) (SIDEBAR_WIDTH * sidebarProgress);
        int buttonX = width - buttonSize - TOOL_BUTTON_PADDING - sidebarWidth;
        int buttonY = 8;

        int bgColor = 0xCC111111;
        int borderColor = 0xFF000000;
        int iconColor = 0xFFFFFFFF;
        int iconSize = 10;
        int iconX = buttonX + (buttonSize - iconSize) / 2;
        int iconY = buttonY + (buttonSize - iconSize) / 2;
        boolean hovered = isPointInside(mouseX, mouseY, buttonX, buttonY, buttonSize, buttonSize);

        context.fill(buttonX, buttonY, buttonX + buttonSize, buttonY + buttonSize, hovered ? 0xDD1A1A1A : bgColor);
        drawBorder(context, buttonX, buttonY, buttonSize, buttonSize, borderColor);

        context.fill(iconX, iconY + iconSize / 2 - 1, iconX + iconSize, iconY + iconSize / 2 + 1, iconColor);
        if (!sidebarOpen) {
            context.fill(iconX + iconSize / 2 - 1, iconY, iconX + iconSize / 2 + 1, iconY + iconSize, iconColor);
        }
    }

    private void drawHeader(DrawContext context, int mouseX, int mouseY) {
        Text title = Text.literal("Visual Keystrokes");
        headerWidth = textRenderer.getWidth(title);
        headerHeight = textRenderer.fontHeight;
        headerX = (width - headerWidth) / 2;
        headerY = 8;

        context.drawTextWithShadow(textRenderer, title, headerX, headerY, 0xFFFFFFFF);
        boolean hovered = isPointInside(mouseX, mouseY, headerX, headerY, headerWidth, headerHeight);
        if (hovered || infoOpen) {
            int underlineY = headerY + headerHeight + 2;
            context.drawHorizontalLine(headerX, headerX + headerWidth, underlineY, 0xFFFFFFFF);
        }
    }

    private boolean handleHeaderClick(double mouseX, double mouseY) {
        if (isPointInside(mouseX, mouseY, headerX, headerY, headerWidth, headerHeight)) {
            infoOpen = true;
            return true;
        }
        return false;
    }

    private void drawInfoPopup(DrawContext context, int mouseX, int mouseY) {
        int panelWidth = Math.min(460, width - 80);
        int panelHeight = 230;
        int panelX = (width - panelWidth) / 2;
        int panelY = (height - panelHeight) / 2;

        context.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xCC111111);
        drawBorder(context, panelX, panelY, panelWidth, panelHeight, 0xFF000000);

        int centerX = panelX + panelWidth / 2;
        int textY = panelY + 26;
        drawCenteredText(context, "Visual Keystrokes", centerX, textY);
        textY += 34;
        drawCenteredText(context, "Created by: ryduzz", centerX, textY);
        textY += 20;
        drawCenteredText(context, "Built for Minecraft: 1.21 - 1.21.11", centerX, textY);
        textY += 20;
        drawCenteredText(context, "Running Minecraft: fabric-loader-0.18.3-1.21.11", centerX, textY);
        textY += 20;
        drawCenteredText(context, "Current Build: 1.0.0+mc1.21.11", centerX, textY);
        textY += 20;
        drawCenteredText(context, "Fabric Loader: 0.18.3", centerX, textY);

        Text closeLabel = Text.literal("Close");
        int buttonWidth = 140;
        int buttonHeight = 24;
        infoCloseWidth = buttonWidth;
        infoCloseHeight = buttonHeight;
        infoCloseX = centerX - buttonWidth / 2;
        infoCloseY = panelY + panelHeight - 36;

        int closeBg = isPointInside(mouseX, mouseY, infoCloseX, infoCloseY, buttonWidth, buttonHeight) ? 0xFF2B2B2B : 0xFF1E1E1E;
        context.fill(infoCloseX, infoCloseY, infoCloseX + buttonWidth, infoCloseY + buttonHeight, closeBg);
        drawBorder(context, infoCloseX, infoCloseY, buttonWidth, buttonHeight, 0xFF000000);
        drawCenteredText(context, closeLabel, centerX, infoCloseY + 7);
    }

    private boolean handleInfoPopupClick(double mouseX, double mouseY) {
        if (isPointInside(mouseX, mouseY, infoCloseX, infoCloseY, infoCloseWidth, infoCloseHeight)) {
            infoOpen = false;
            return true;
        }
        return isPointInside(mouseX, mouseY,
            (width - Math.min(460, width - 80)) / 2,
            (height - 230) / 2,
            Math.min(460, width - 80),
            230);
    }


    private void drawCenteredText(DrawContext context, String text, int centerX, int y) {
        int textWidth = textRenderer.getWidth(text);
        context.drawTextWithShadow(textRenderer, text, centerX - textWidth / 2, y, 0xFFFFFFFF);
    }

    private void drawCenteredText(DrawContext context, Text text, int centerX, int y) {
        int textWidth = textRenderer.getWidth(text);
        context.drawTextWithShadow(textRenderer, text, centerX - textWidth / 2, y, 0xFFFFFFFF);
    }

    protected boolean handleSearchFieldClickLegacy(double mouseX, double mouseY, int button) {
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return false;
        }
        if (!isSearchFieldHit(mouseX, mouseY)) {
            return false;
        }
        setFocused(searchField);
        searchField.setFocused(true);
        invokeSearchFieldOnClickLegacy(mouseX, mouseY);
        return true;
    }

    protected boolean isSearchFieldHit(double mouseX, double mouseY) {
        if (sidebarProgress <= 0.01f) {
            return false;
        }
        int sidebarWidth = (int) (SIDEBAR_WIDTH * sidebarProgress);
        int sidebarX = width - sidebarWidth;
        int searchX = sidebarX + SIDEBAR_PADDING;
        int searchY = HEADER_Y + 14;
        int searchWidth = sidebarWidth - SIDEBAR_PADDING * 2;
        return isPointInside(mouseX, mouseY, searchX, searchY, searchWidth, SEARCH_HEIGHT);
    }

    private boolean invokeSearchFieldOnClickLegacy(double mouseX, double mouseY) {
        return invokeTextFieldOnClickLegacy(searchField, mouseX, mouseY);
    }

    private boolean invokeSearchFieldKeyPressedLegacy(int keyCode, int scanCode, int modifiers) {
        return invokeTextFieldKeyPressedLegacy(searchField, keyCode, scanCode, modifiers);
    }

    private boolean invokeSearchFieldCharTypedLegacy(char chr, int modifiers) {
        return invokeTextFieldCharTypedLegacy(searchField, chr, modifiers);
    }

    private boolean invokeTextFieldOnClickLegacy(TextFieldWidget field, double mouseX, double mouseY) {
        if (field == null) {
            return false;
        }
        try {
            if (legacyTextFieldOnClick == null) {
                legacyTextFieldOnClick = TextFieldWidget.class.getMethod("onClick", double.class, double.class);
            }
            legacyTextFieldOnClick.invoke(field, mouseX, mouseY);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to handle legacy text field click.", e);
        }
    }

    private boolean invokeTextFieldKeyPressedLegacy(TextFieldWidget field, int keyCode, int scanCode, int modifiers) {
        if (field == null) {
            return false;
        }
        try {
            if (legacyTextFieldKeyPressed == null) {
                legacyTextFieldKeyPressed = TextFieldWidget.class.getMethod("keyPressed", int.class, int.class, int.class);
            }
            return (boolean) legacyTextFieldKeyPressed.invoke(field, keyCode, scanCode, modifiers);
        } catch (NoSuchMethodException e) {
            return false;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to handle legacy text field key press.", e);
        }
    }

    private boolean invokeTextFieldCharTypedLegacy(TextFieldWidget field, char chr, int modifiers) {
        if (field == null) {
            return false;
        }
        try {
            if (legacyTextFieldCharTyped == null) {
                legacyTextFieldCharTyped = TextFieldWidget.class.getMethod("charTyped", char.class, int.class);
            }
            return (boolean) legacyTextFieldCharTyped.invoke(field, chr, modifiers);
        } catch (NoSuchMethodException e) {
            return false;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to handle legacy text field char input.", e);
        }
    }


    private boolean handleSidebarClick(double mouseX, double mouseY) {
        if (sidebarProgress <= 0.01f) {
            return false;
        }
        int sidebarWidth = (int) (SIDEBAR_WIDTH * sidebarProgress);
        int sidebarX = width - sidebarWidth;
        if (mouseX < sidebarX) {
            return false;
        }

        int searchX = sidebarX + SIDEBAR_PADDING;
        int searchY = HEADER_Y + 14;
        int searchWidth = sidebarWidth - SIDEBAR_PADDING * 2;
        if (isPointInside(mouseX, mouseY, searchX, searchY, searchWidth, SEARCH_HEIGHT)) {
            return false;
        }

        int listTop = searchY + SEARCH_HEIGHT + SEARCH_PADDING;
        int listBottom = height - SIDEBAR_PADDING - SETTINGS_BUTTON_SIZE - 6;
        int scrollOffset = (int) Math.round(sidebarScroll);
        if (mouseY < listTop || mouseY > listBottom) {
            return false;
        }

        int y = listTop - scrollOffset;
        for (Template template : getTemplates(searchField.getText())) {
            int entryX = sidebarX + SIDEBAR_PADDING;
            int entryY = y;
            int entryWidth = sidebarWidth - SIDEBAR_PADDING * 2;
            int entryHeight = SIDEBAR_ENTRY_HEIGHT;
            if (isPointInside(mouseX, mouseY, entryX, entryY, entryWidth, entryHeight)) {
                Group group = groupById.get(template.id);
                if (group == null) {
                    addTemplate(template);
                    rebuildGroups();
                    group = groupById.get(template.id);
                }
                if (group != null) {
                    group.setVisible(true);
                    selectedGroups.clear();
                    selectedGroups.add(group);
                    dragMode = DragMode.MOVE;
                    draggingFromSidebar = true;
                    Bounds bounds = group.getBounds();
                    dragOffsetX = bounds.width / 2.0;
                    dragOffsetY = bounds.height / 2.0;
                    double overlayX = toOverlayX(mouseX);
                    double overlayY = toOverlayY(mouseY);
                    int targetX = (int) Math.round(overlayX - bounds.width / 2.0);
                    int targetY = (int) Math.round(overlayY - bounds.height / 2.0);
                    moveSelectedBy(targetX - bounds.x, targetY - bounds.y);
                    return true;
                }
                dragMode = DragMode.MOVE;
                return true;
            }
            y += entryHeight + 6;
        }
        return false;
    }

    private void invokeRefreshWidgetPositions() {
        if (REFRESH_WIDGET_POSITIONS == null) {
            return;
        }
        try {
            REFRESH_WIDGET_POSITIONS.invoke(this);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to refresh widget positions.", e);
        }
    }

    private static Method findMethod(Class<?> owner, String name, Class<?>... params) {
        try {
            Method method = owner.getDeclaredMethod(name, params);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private void drawResetButton(DrawContext context, int sidebarX, int sidebarWidth, int mouseX, int mouseY) {
        Text label = Text.literal("Reset");
        int textWidth = textRenderer.getWidth(label);
        int buttonWidth = textWidth + RESET_PADDING_X * 2;
        int buttonHeight = textRenderer.fontHeight + RESET_PADDING_Y * 2;

        resetWidth = Math.min(buttonWidth, sidebarWidth - HEADER_PADDING * 2);
        resetHeight = buttonHeight;
        resetX = sidebarX + sidebarWidth - HEADER_PADDING - resetWidth;
        resetY = HEADER_Y - RESET_PADDING_Y;

        int bg = isPointInside(mouseX, mouseY, resetX, resetY, resetWidth, resetHeight) ? 0xFF2B2B2B : 0xFF1E1E1E;
        context.fill(resetX, resetY, resetX + resetWidth, resetY + resetHeight, bg);
        drawBorder(context, resetX, resetY, resetWidth, resetHeight, 0xFF000000);

        int textX = resetX + (resetWidth - textWidth) / 2;
        int textY = resetY + (resetHeight - textRenderer.fontHeight) / 2;
        context.drawTextWithShadow(textRenderer, label, textX, textY, 0xFFFFFFFF);
    }

    private boolean handleResetClick(double mouseX, double mouseY) {
        if (sidebarProgress <= 0.01f) {
            return false;
        }
        if (isPointInside(mouseX, mouseY, resetX, resetY, resetWidth, resetHeight)) {
            resetWorkspace();
            return true;
        }
        return false;
    }

    private boolean handleSettingsButtonClick(double mouseX, double mouseY) {
        if (sidebarProgress <= 0.01f) {
            return false;
        }
        if (isPointInside(mouseX, mouseY, settingsButtonX, settingsButtonY, SETTINGS_BUTTON_SIZE, SETTINGS_BUTTON_SIZE)) {
            settingsOpen = !settingsOpen;
            if (settingsOpen) {
                closeEditor();
            }
            return true;
        }
        return false;
    }

    private void drawSettingsPopup(DrawContext context, int mouseX, int mouseY) {
        settingsPanelX = settingsButtonX - SETTINGS_PANEL_WIDTH + SETTINGS_BUTTON_SIZE;
        settingsPanelY = settingsButtonY - SETTINGS_PANEL_HEIGHT - 8;

        context.fill(settingsPanelX, settingsPanelY, settingsPanelX + SETTINGS_PANEL_WIDTH, settingsPanelY + SETTINGS_PANEL_HEIGHT, 0xCC111111);
        drawBorder(context, settingsPanelX, settingsPanelY, SETTINGS_PANEL_WIDTH, SETTINGS_PANEL_HEIGHT, 0xFF000000);

        int left = settingsPanelX + 12;
        int top = settingsPanelY + 12;
        int lineHeight = 22;

        drawSettingsRow(context, "Snapping", config.snappingEnabled, left, top);
        drawSettingsRow(context, "Guides", config.guidesEnabled, left, top + lineHeight);
        drawSettingsRow(context, "Distance Labels", config.distanceLabelsEnabled, left, top + lineHeight * 2);
        drawThresholdRow(context, left, top + lineHeight * 3);
    }

    private void drawSettingsRow(DrawContext context, String label, boolean enabled, int x, int y) {
        context.drawTextWithShadow(textRenderer, label, x, y + 4, 0xFFFFFFFF);
        String value = enabled ? "On" : "Off";
        int valueWidth = textRenderer.getWidth(value);
        int buttonWidth = 44;
        int buttonHeight = 18;
        int buttonX = settingsPanelX + SETTINGS_PANEL_WIDTH - 12 - buttonWidth;
        int buttonY = y;
        context.fill(buttonX, buttonY, buttonX + buttonWidth, buttonY + buttonHeight, 0xFF1E1E1E);
        drawBorder(context, buttonX, buttonY, buttonWidth, buttonHeight, 0xFF000000);
        context.drawTextWithShadow(textRenderer, value, buttonX + (buttonWidth - valueWidth) / 2, buttonY + 5, 0xFFFFFFFF);
    }

    private void drawThresholdRow(DrawContext context, int x, int y) {
        String label = "Snap Threshold";
        context.drawTextWithShadow(textRenderer, label, x, y + 4, 0xFFFFFFFF);
        int value = Math.max(1, config.snapThreshold);
        String valueText = Integer.toString(value);
        int buttonSize = 18;
        int gap = 4;
        int right = settingsPanelX + SETTINGS_PANEL_WIDTH - 12;
        int minusX = right - buttonSize * 2 - gap;
        int plusX = right - buttonSize;
        int buttonY = y;

        context.fill(minusX, buttonY, minusX + buttonSize, buttonY + buttonSize, 0xFF1E1E1E);
        drawBorder(context, minusX, buttonY, buttonSize, buttonSize, 0xFF000000);
        context.fill(minusX + 4, buttonY + buttonSize / 2 - 1, minusX + buttonSize - 4, buttonY + buttonSize / 2 + 1, 0xFFFFFFFF);

        context.fill(plusX, buttonY, plusX + buttonSize, buttonY + buttonSize, 0xFF1E1E1E);
        drawBorder(context, plusX, buttonY, buttonSize, buttonSize, 0xFF000000);
        context.fill(plusX + 4, buttonY + buttonSize / 2 - 1, plusX + buttonSize - 4, buttonY + buttonSize / 2 + 1, 0xFFFFFFFF);
        context.fill(plusX + buttonSize / 2 - 1, buttonY + 4, plusX + buttonSize / 2 + 1, buttonY + buttonSize - 4, 0xFFFFFFFF);

        int valueWidth = textRenderer.getWidth(valueText);
        int valueX = minusX - gap - valueWidth;
        context.drawTextWithShadow(textRenderer, valueText, valueX, buttonY + 5, 0xFFFFFFFF);
    }

    private boolean handleSettingsPopupClick(double mouseX, double mouseY) {
        if (!isPointInside(mouseX, mouseY, settingsPanelX, settingsPanelY, SETTINGS_PANEL_WIDTH, SETTINGS_PANEL_HEIGHT)) {
            settingsOpen = false;
            return false;
        }

        int left = settingsPanelX + 12;
        int top = settingsPanelY + 12;
        int lineHeight = 22;
        int buttonWidth = 44;
        int buttonHeight = 18;
        int buttonX = settingsPanelX + SETTINGS_PANEL_WIDTH - 12 - buttonWidth;

        if (isPointInside(mouseX, mouseY, buttonX, top, buttonWidth, buttonHeight)) {
            config.snappingEnabled = !config.snappingEnabled;
            OverlayConfig.save(config);
            return true;
        }
        if (isPointInside(mouseX, mouseY, buttonX, top + lineHeight, buttonWidth, buttonHeight)) {
            config.guidesEnabled = !config.guidesEnabled;
            OverlayConfig.save(config);
            return true;
        }
        if (isPointInside(mouseX, mouseY, buttonX, top + lineHeight * 2, buttonWidth, buttonHeight)) {
            config.distanceLabelsEnabled = !config.distanceLabelsEnabled;
            OverlayConfig.save(config);
            return true;
        }

        int buttonSize = 18;
        int gap = 4;
        int right = settingsPanelX + SETTINGS_PANEL_WIDTH - 12;
        int minusX = right - buttonSize * 2 - gap;
        int plusX = right - buttonSize;
        int buttonY = top + lineHeight * 3;

        if (isPointInside(mouseX, mouseY, minusX, buttonY, buttonSize, buttonSize)) {
            config.snapThreshold = Math.max(1, config.snapThreshold - 1);
            OverlayConfig.save(config);
            return true;
        }
        if (isPointInside(mouseX, mouseY, plusX, buttonY, buttonSize, buttonSize)) {
            config.snapThreshold = Math.min(20, config.snapThreshold + 1);
            OverlayConfig.save(config);
            return true;
        }

        return true;
    }

    private void drawEditorPopup(DrawContext context, int mouseX, int mouseY) {
        if (selectedGroups.isEmpty()) {
            closeEditor();
            return;
        }

        editorPanelWidth = Math.min(EDITOR_PANEL_WIDTH, width - 60);
        editorPanelHeight = Math.min(EDITOR_PANEL_HEIGHT, height - 60);
        editorPanelX = (width - editorPanelWidth) / 2;
        editorPanelY = (height - editorPanelHeight) / 2;
        editorPanelY = Math.max(20, editorPanelY);

        context.fill(editorPanelX, editorPanelY, editorPanelX + editorPanelWidth, editorPanelY + editorPanelHeight, 0xCC111111);
        drawBorder(context, editorPanelX, editorPanelY, editorPanelWidth, editorPanelHeight, 0xFF000000);

        int titleX = editorPanelX + 12;
        int titleY = editorPanelY + 12;
        String title = selectedGroups.size() == 1
            ? "Edit " + primarySelected().displayName
            : "Edit " + selectedGroups.size() + " Elements";
        context.drawTextWithShadow(textRenderer, title, titleX, titleY, 0xFFFFFFFF);

        editorCloseWidth = 14;
        editorCloseHeight = 14;
        editorCloseX = editorPanelX + editorPanelWidth - editorCloseWidth - 8;
        editorCloseY = editorPanelY + 8;
        int closeBg = isPointInside(mouseX, mouseY, editorCloseX, editorCloseY, editorCloseWidth, editorCloseHeight) ? 0xFF2B2B2B : 0xFF1E1E1E;
        context.fill(editorCloseX, editorCloseY, editorCloseX + editorCloseWidth, editorCloseY + editorCloseHeight, closeBg);
        drawBorder(context, editorCloseX, editorCloseY, editorCloseWidth, editorCloseHeight, 0xFF000000);
        context.drawTextWithShadow(textRenderer, "X", editorCloseX + 4, editorCloseY + 3, 0xFFFFFFFF);

        int rowStartY = titleY + 20;
        int rowHeight = 24;
        int boxSize = 14;
        ColorTarget[] targets = new ColorTarget[] {
            ColorTarget.BACKGROUND,
            ColorTarget.PRESSED,
            ColorTarget.BORDER,
            ColorTarget.TEXT
        };
        String[] labels = new String[] { "Fill", "Pressed", "Outline", "Text" };

        for (int i = 0; i < targets.length; i++) {
            int rowY = rowStartY + i * rowHeight;
            context.drawTextWithShadow(textRenderer, labels[i], titleX, rowY + 4, 0xFFFFFFFF);

            ColorState state = selectionColorState(targets[i]);
            int boxX = editorPanelX + editorPanelWidth - 12 - boxSize;
            int boxY = rowY + 2;
            context.fill(boxX, boxY, boxX + boxSize, boxY + boxSize, state.color);
            drawBorder(context, boxX, boxY, boxSize, boxSize, 0xFF000000);
            if (state.mixed) {
                String mixed = "Mixed";
                int mixedWidth = textRenderer.getWidth(mixed);
                context.drawTextWithShadow(textRenderer, mixed, boxX - mixedWidth - 6, rowY + 4, 0xFFA0A0A0);
            }
        }

        int controlsY = rowStartY + targets.length * rowHeight + 6;
        int buttonWidth = 80;
        int buttonHeight = 18;

        String visibilityLabel = visibilityLabelForSelection();
        editorVisibilityWidth = buttonWidth;
        editorVisibilityHeight = buttonHeight;
        editorVisibilityX = editorPanelX + editorPanelWidth - 12 - buttonWidth;
        editorVisibilityY = controlsY;
        int visibilityBg = isPointInside(mouseX, mouseY, editorVisibilityX, editorVisibilityY, editorVisibilityWidth, editorVisibilityHeight)
            ? 0xFF2B2B2B
            : 0xFF1E1E1E;
        context.fill(editorVisibilityX, editorVisibilityY, editorVisibilityX + editorVisibilityWidth, editorVisibilityY + editorVisibilityHeight, visibilityBg);
        drawBorder(context, editorVisibilityX, editorVisibilityY, editorVisibilityWidth, editorVisibilityHeight, 0xFF000000);
        int visibilityTextWidth = textRenderer.getWidth(visibilityLabel);
        context.drawTextWithShadow(
            textRenderer,
            visibilityLabel,
            editorVisibilityX + (editorVisibilityWidth - visibilityTextWidth) / 2,
            editorVisibilityY + 5,
            0xFFFFFFFF
        );
        context.drawTextWithShadow(textRenderer, "Visibility", titleX, controlsY + 4, 0xFFFFFFFF);

        editorResetWidth = 110;
        editorResetHeight = buttonHeight;
        editorResetX = editorPanelX + editorPanelWidth - 12 - editorResetWidth;
        editorResetY = controlsY + rowHeight;
        int resetBg = isPointInside(mouseX, mouseY, editorResetX, editorResetY, editorResetWidth, editorResetHeight)
            ? 0xFF2B2B2B
            : 0xFF1E1E1E;
        context.fill(editorResetX, editorResetY, editorResetX + editorResetWidth, editorResetY + editorResetHeight, resetBg);
        drawBorder(context, editorResetX, editorResetY, editorResetWidth, editorResetHeight, 0xFF000000);
        String resetLabel = "Reset Colors";
        int resetTextWidth = textRenderer.getWidth(resetLabel);
        context.drawTextWithShadow(
            textRenderer,
            resetLabel,
            editorResetX + (editorResetWidth - resetTextWidth) / 2,
            editorResetY + 5,
            0xFFFFFFFF
        );
        context.drawTextWithShadow(textRenderer, "Overrides", titleX, editorResetY + 4, 0xFFFFFFFF);
    }

    private boolean handleEditorPopupClick(double mouseX, double mouseY) {
        if (!isPointInside(mouseX, mouseY, editorPanelX, editorPanelY, editorPanelWidth, editorPanelHeight)) {
            closeEditor();
            return false;
        }

        if (isPointInside(mouseX, mouseY, editorCloseX, editorCloseY, editorCloseWidth, editorCloseHeight)) {
            closeEditor();
            return true;
        }

        int rowStartY = editorPanelY + 32;
        int rowHeight = 24;
        int boxSize = 14;
        ColorTarget[] targets = new ColorTarget[] {
            ColorTarget.BACKGROUND,
            ColorTarget.PRESSED,
            ColorTarget.BORDER,
            ColorTarget.TEXT
        };

        for (int i = 0; i < targets.length; i++) {
            int rowY = rowStartY + i * rowHeight;
            int boxX = editorPanelX + editorPanelWidth - 12 - boxSize;
            int boxY = rowY + 2;
            if (isPointInside(mouseX, mouseY, boxX, boxY, boxSize, boxSize)) {
                openColorPicker(targets[i]);
                return true;
            }
        }

        if (isPointInside(mouseX, mouseY, editorVisibilityX, editorVisibilityY, editorVisibilityWidth, editorVisibilityHeight)) {
            toggleSelectionVisibility();
            OverlayConfig.save(config);
            return true;
        }

        if (isPointInside(mouseX, mouseY, editorResetX, editorResetY, editorResetWidth, editorResetHeight)) {
            clearSelectionColorOverrides();
            OverlayConfig.save(config);
            return true;
        }

        return true;
    }

    private void drawColorPicker(DrawContext context, int mouseX, int mouseY) {
        int panelWidth = COLOR_PICKER_PADDING * 2 + COLOR_PICKER_RADIUS * 2 + COLOR_PICKER_SLIDER_WIDTH + 12;
        int panelHeight = COLOR_PICKER_PADDING * 3 + COLOR_PICKER_RADIUS * 2 + COLOR_PICKER_FIELD_HEIGHT + 18;
        int panelX = editorPanelX + editorPanelWidth + 12;
        if (panelX + panelWidth > width - 10) {
            panelX = editorPanelX - panelWidth - 12;
        }
        if (panelX < 10) {
            panelX = (width - panelWidth) / 2;
        }
        panelX = Math.max(10, Math.min(panelX, width - panelWidth - 10));
        int panelY = editorPanelY + 10;
        if (panelY + panelHeight > height - 10) {
            panelY = height - panelHeight - 10;
        }
        panelY = Math.max(10, panelY);

        colorPickerX = panelX;
        colorPickerY = panelY;
        colorPickerWidth = panelWidth;
        colorPickerHeight = panelHeight;

        context.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xCC111111);
        drawBorder(context, panelX, panelY, panelWidth, panelHeight, 0xFF000000);

        int wheelCenterX = colorWheelCenterX();
        int wheelCenterY = colorWheelCenterY();
        drawColorWheel(context, wheelCenterX, wheelCenterY);
        drawValueSlider(context, wheelCenterX, wheelCenterY);

        int indicatorX = wheelCenterX + Math.round((float) Math.cos(pickerHue * Math.PI * 2) * pickerSaturation * COLOR_PICKER_RADIUS);
        int indicatorY = wheelCenterY + Math.round((float) Math.sin(pickerHue * Math.PI * 2) * pickerSaturation * COLOR_PICKER_RADIUS);
        context.drawHorizontalLine(indicatorX - 2, indicatorX + 2, indicatorY, 0xFFFFFFFF);
        context.drawVerticalLine(indicatorX, indicatorY - 2, indicatorY + 2, 0xFFFFFFFF);

        int sliderX = colorValueSliderX();
        int sliderY = colorValueSliderY();
        int sliderHeight = colorValueSliderHeight();
        int valueY = sliderY + Math.round((1.0f - pickerValue) * (sliderHeight - 1));
        context.drawHorizontalLine(sliderX - 1, sliderX + COLOR_PICKER_SLIDER_WIDTH, valueY, 0xFFFFFFFF);

        int fieldX = panelX + COLOR_PICKER_PADDING;
        int fieldY = panelY + COLOR_PICKER_PADDING + COLOR_PICKER_RADIUS * 2 + 10;
        int fieldWidth = panelWidth - COLOR_PICKER_PADDING * 2;
        if (colorHexField != null) {
            colorHexField.setX(fieldX);
            colorHexField.setY(fieldY);
            colorHexField.setWidth(fieldWidth);
            colorHexField.setVisible(true);
        }
        context.drawTextWithShadow(textRenderer, "Hex", fieldX, fieldY - 10, 0xFFFFFFFF);
    }

    private boolean handleColorPickerClick(double mouseX, double mouseY) {
        if (!isPointInside(mouseX, mouseY, colorPickerX, colorPickerY, colorPickerWidth, colorPickerHeight)) {
            closeColorPicker();
            return false;
        }

        if (colorHexField != null && isPointInside(mouseX, mouseY, colorHexField.getX(), colorHexField.getY(), colorHexField.getWidth(), colorHexField.getHeight())) {
            setFocused(colorHexField);
            colorHexField.setFocused(true);
            invokeTextFieldOnClickLegacy(colorHexField, mouseX, mouseY);
            return true;
        }

        if (isPointInside(mouseX, mouseY, colorValueSliderX(), colorValueSliderY(), COLOR_PICKER_SLIDER_WIDTH, colorValueSliderHeight())) {
            updateValueFromSlider(mouseY);
            colorPickerDragArea = ColorPickerDragArea.VALUE;
            return true;
        }

        if (isPointInside(mouseX, mouseY, colorWheelLeft(), colorWheelTop(), COLOR_PICKER_RADIUS * 2, COLOR_PICKER_RADIUS * 2)) {
            updateColorFromWheel(mouseX, mouseY);
            colorPickerDragArea = ColorPickerDragArea.WHEEL;
            return true;
        }

        return true;
    }

    private boolean handleColorPickerDrag(double mouseX, double mouseY) {
        if (colorPickerDragArea == ColorPickerDragArea.WHEEL) {
            updateColorFromWheel(mouseX, mouseY);
            return true;
        }
        if (colorPickerDragArea == ColorPickerDragArea.VALUE) {
            updateValueFromSlider(mouseY);
            return true;
        }
        return false;
    }

    private void openEditor() {
        if (selectedGroups.isEmpty()) {
            return;
        }
        editorOpen = true;
        settingsOpen = false;
    }

    private void closeEditor() {
        editorOpen = false;
        closeColorPicker();
    }

    private void openColorPicker(ColorTarget target) {
        if (selectedGroups.isEmpty()) {
            return;
        }
        activeColorTarget = target;
        colorPickerOpen = true;
        colorPickerDragArea = ColorPickerDragArea.NONE;
        ColorState state = selectionColorState(target);
        updatePickerFromColor(state.color);
        syncHexField(state.color);
    }

    private void closeColorPicker() {
        colorPickerOpen = false;
        colorPickerDragArea = ColorPickerDragArea.NONE;
        if (colorHexField != null) {
            colorHexField.setFocused(false);
            setFocused(null);
        }
    }

    private void applyHexFieldIfValid() {
        if (!colorPickerOpen || colorHexField == null) {
            return;
        }
        String text = colorHexField.getText().trim();
        if (text.startsWith("#")) {
            text = text.substring(1);
        }
        if (text.length() != 6) {
            return;
        }
        int rgb;
        try {
            rgb = Integer.parseInt(text, 16);
        } catch (NumberFormatException ignored) {
            return;
        }
        int color = 0xFF000000 | rgb;
        applySelectionColor(activeColorTarget, color);
        updatePickerFromColor(color);
        syncHexField(color);
    }

    private void updatePickerFromColor(int color) {
        float[] hsv = rgbToHsv(color);
        pickerHue = hsv[0];
        pickerSaturation = hsv[1];
        pickerValue = hsv[2];
    }

    private void updateColorFromWheel(double mouseX, double mouseY) {
        int centerX = colorWheelCenterX();
        int centerY = colorWheelCenterY();
        double dx = mouseX - centerX;
        double dy = mouseY - centerY;
        double dist = Math.sqrt(dx * dx + dy * dy);
        double clamped = Math.min(dist, COLOR_PICKER_RADIUS);
        double angle = Math.atan2(dy, dx);
        pickerHue = (float) ((angle / (Math.PI * 2) + 1.0) % 1.0);
        pickerSaturation = (float) (clamped / COLOR_PICKER_RADIUS);
        int color = hsvToRgb(pickerHue, pickerSaturation, pickerValue);
        applySelectionColor(activeColorTarget, color);
        syncHexField(color);
    }

    private void updateValueFromSlider(double mouseY) {
        int sliderY = colorValueSliderY();
        int sliderHeight = colorValueSliderHeight();
        double t = (mouseY - sliderY) / sliderHeight;
        pickerValue = (float) (1.0 - Math.max(0.0, Math.min(1.0, t)));
        int color = hsvToRgb(pickerHue, pickerSaturation, pickerValue);
        applySelectionColor(activeColorTarget, color);
        syncHexField(color);
    }

    private void applySelectionColor(ColorTarget target, int color) {
        for (OverlayConfig.KeyDefinition key : keysForSelectedGroups()) {
            setColorOverride(key, target, color);
        }
        OverlayConfig.save(config);
    }

    private void clearSelectionColorOverrides() {
        for (OverlayConfig.KeyDefinition key : keysForSelectedGroups()) {
            setColorOverride(key, ColorTarget.BACKGROUND, null);
            setColorOverride(key, ColorTarget.PRESSED, null);
            setColorOverride(key, ColorTarget.BORDER, null);
            setColorOverride(key, ColorTarget.TEXT, null);
        }
        OverlayConfig.save(config);
    }

    private void toggleSelectionVisibility() {
        List<OverlayConfig.KeyDefinition> keys = keysForSelectedGroups();
        if (keys.isEmpty()) {
            return;
        }
        boolean allVisible = true;
        for (OverlayConfig.KeyDefinition key : keys) {
            if (!key.isVisible()) {
                allVisible = false;
                break;
            }
        }
        boolean next = !allVisible;
        for (OverlayConfig.KeyDefinition key : keys) {
            key.setVisible(next);
        }
    }

    private String visibilityLabelForSelection() {
        List<OverlayConfig.KeyDefinition> keys = keysForSelectedGroups();
        if (keys.isEmpty()) {
            return "Shown";
        }
        int visibleCount = 0;
        for (OverlayConfig.KeyDefinition key : keys) {
            if (key.isVisible()) {
                visibleCount++;
            }
        }
        if (visibleCount == 0) {
            return "Hidden";
        }
        if (visibleCount == keys.size()) {
            return "Shown";
        }
        return "Mixed";
    }

    private ColorState selectionColorState(ColorTarget target) {
        List<OverlayConfig.KeyDefinition> keys = keysForSelectedGroups();
        int fallback = resolveConfigColor(target);
        if (keys.isEmpty()) {
            return new ColorState(fallback, false);
        }
        int color = resolveColor(keys.get(0), target);
        boolean mixed = false;
        for (OverlayConfig.KeyDefinition key : keys) {
            if (resolveColor(key, target) != color) {
                mixed = true;
                break;
            }
        }
        return new ColorState(color, mixed);
    }

    private int resolveColor(OverlayConfig.KeyDefinition key, ColorTarget target) {
        return switch (target) {
            case BACKGROUND -> resolveBackgroundColor(key);
            case PRESSED -> resolvePressedColor(key);
            case BORDER -> resolveBorderColor(key);
            case TEXT -> resolveTextColor(key);
        };
    }

    private int resolveConfigColor(ColorTarget target) {
        return switch (target) {
            case BACKGROUND -> config.backgroundColor;
            case PRESSED -> config.pressedColor;
            case BORDER -> config.borderColor;
            case TEXT -> config.textColor;
        };
    }

    private void setColorOverride(OverlayConfig.KeyDefinition key, ColorTarget target, Integer color) {
        switch (target) {
            case BACKGROUND -> key.backgroundColorOverride = color;
            case PRESSED -> key.pressedColorOverride = color;
            case BORDER -> key.borderColorOverride = color;
            case TEXT -> key.textColorOverride = color;
        }
    }

    private int resolveBackgroundColor(OverlayConfig.KeyDefinition key) {
        return OverlayConfig.resolveColor(key.backgroundColorOverride, config.backgroundColor);
    }

    private int resolvePressedColor(OverlayConfig.KeyDefinition key) {
        return OverlayConfig.resolveColor(key.pressedColorOverride, config.pressedColor);
    }

    private int resolveBorderColor(OverlayConfig.KeyDefinition key) {
        return OverlayConfig.resolveColor(key.borderColorOverride, config.borderColor);
    }

    private int resolveTextColor(OverlayConfig.KeyDefinition key) {
        return OverlayConfig.resolveColor(key.textColorOverride, config.textColor);
    }

    private int colorWheelCenterX() {
        return colorPickerX + COLOR_PICKER_PADDING + COLOR_PICKER_RADIUS;
    }

    private int colorWheelCenterY() {
        return colorPickerY + COLOR_PICKER_PADDING + COLOR_PICKER_RADIUS;
    }

    private int colorWheelLeft() {
        return colorWheelCenterX() - COLOR_PICKER_RADIUS;
    }

    private int colorWheelTop() {
        return colorWheelCenterY() - COLOR_PICKER_RADIUS;
    }

    private int colorValueSliderX() {
        return colorWheelCenterX() + COLOR_PICKER_RADIUS + 8;
    }

    private int colorValueSliderY() {
        return colorWheelCenterY() - COLOR_PICKER_RADIUS;
    }

    private int colorValueSliderHeight() {
        return COLOR_PICKER_RADIUS * 2;
    }

    private void drawColorWheel(DrawContext context, int centerX, int centerY) {
        int radius = COLOR_PICKER_RADIUS;
        for (int dy = -radius; dy <= radius; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                double dist = Math.sqrt(dx * dx + dy * dy);
                if (dist > radius) {
                    continue;
                }
                float hue = (float) ((Math.atan2(dy, dx) / (Math.PI * 2) + 1.0) % 1.0);
                float sat = (float) (dist / radius);
                int color = hsvToRgb(hue, sat, pickerValue);
                context.fill(centerX + dx, centerY + dy, centerX + dx + 1, centerY + dy + 1, color);
            }
        }
    }

    private void drawValueSlider(DrawContext context, int centerX, int centerY) {
        int sliderX = colorValueSliderX();
        int sliderY = colorValueSliderY();
        int sliderHeight = colorValueSliderHeight();
        for (int i = 0; i < sliderHeight; i++) {
            float value = 1.0f - (i / (float) (sliderHeight - 1));
            int color = hsvToRgb(pickerHue, pickerSaturation, value);
            context.fill(sliderX, sliderY + i, sliderX + COLOR_PICKER_SLIDER_WIDTH, sliderY + i + 1, color);
        }
        drawBorder(context, sliderX, sliderY, COLOR_PICKER_SLIDER_WIDTH, sliderHeight, 0xFF000000);
    }

    private void syncHexField(int color) {
        if (colorHexField == null) {
            return;
        }
        String next = String.format("#%06X", color & 0xFFFFFF);
        if (!next.equalsIgnoreCase(colorHexField.getText())) {
            colorHexField.setText(next);
        }
    }

    private int hsvToRgb(float hue, float saturation, float value) {
        float h = (hue % 1.0f + 1.0f) % 1.0f;
        float c = value * saturation;
        float hPrime = h * 6.0f;
        float x = c * (1.0f - Math.abs(hPrime % 2.0f - 1.0f));
        float r1;
        float g1;
        float b1;
        if (hPrime < 1.0f) {
            r1 = c;
            g1 = x;
            b1 = 0.0f;
        } else if (hPrime < 2.0f) {
            r1 = x;
            g1 = c;
            b1 = 0.0f;
        } else if (hPrime < 3.0f) {
            r1 = 0.0f;
            g1 = c;
            b1 = x;
        } else if (hPrime < 4.0f) {
            r1 = 0.0f;
            g1 = x;
            b1 = c;
        } else if (hPrime < 5.0f) {
            r1 = x;
            g1 = 0.0f;
            b1 = c;
        } else {
            r1 = c;
            g1 = 0.0f;
            b1 = x;
        }
        float m = value - c;
        int r = clampColor((r1 + m) * 255.0f);
        int g = clampColor((g1 + m) * 255.0f);
        int b = clampColor((b1 + m) * 255.0f);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private float[] rgbToHsv(int color) {
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float delta = max - min;

        float h;
        if (delta == 0.0f) {
            h = 0.0f;
        } else if (max == r) {
            h = ((g - b) / delta) % 6.0f;
        } else if (max == g) {
            h = ((b - r) / delta) + 2.0f;
        } else {
            h = ((r - g) / delta) + 4.0f;
        }
        h /= 6.0f;
        if (h < 0.0f) {
            h += 1.0f;
        }

        float s = max == 0.0f ? 0.0f : delta / max;
        float v = max;
        return new float[] { h, s, v };
    }

    private int clampColor(float value) {
        if (value < 0.0f) {
            return 0;
        }
        if (value > 255.0f) {
            return 255;
        }
        return Math.round(value);
    }

    private boolean handleSidebarToggleClick(double mouseX, double mouseY) {
        int buttonSize = TOOL_BUTTON_SIZE;
        int sidebarWidth = (int) (SIDEBAR_WIDTH * sidebarProgress);
        int buttonX = width - buttonSize - TOOL_BUTTON_PADDING - sidebarWidth;
        int buttonY = 8;
        if (isPointInside(mouseX, mouseY, buttonX, buttonY, buttonSize, buttonSize)) {
            toggleSidebar();
            return true;
        }
        return false;
    }

    private void drawSelection(DrawContext context, Bounds bounds, boolean showHandles) {
        int x = bounds.x;
        int y = bounds.y;
        int width = bounds.width;
        int height = bounds.height;

        int outlineColor = 0xFFFFFFFF;
        context.drawHorizontalLine(x, x + width, y, outlineColor);
        context.drawHorizontalLine(x, x + width, y + height, outlineColor);
        context.drawVerticalLine(x, y, y + height, outlineColor);
        context.drawVerticalLine(x + width, y, y + height, outlineColor);

        if (showHandles) {
            drawHandle(context, x - HANDLE_SIZE / 2, y - HANDLE_SIZE / 2);
            drawHandle(context, x + width - HANDLE_SIZE / 2, y - HANDLE_SIZE / 2);
            drawHandle(context, x - HANDLE_SIZE / 2, y + height - HANDLE_SIZE / 2);
            drawHandle(context, x + width - HANDLE_SIZE / 2, y + height - HANDLE_SIZE / 2);
        }
    }

    private void drawTrashIcon(DrawContext context, Bounds bounds) {
        int trashX = trashIconX(bounds);
        int trashY = trashIconY(bounds);
        int size = TRASH_SIZE;

        int outline = 0xFFFFFFFF;
        int fill = 0xFF1A1A1A;
        int lidHeight = 2;

        int right = trashX + size - 1;
        int bottom = trashY + size - 1;
        int bodyTop = trashY + lidHeight;

        context.fill(trashX + 1, bodyTop + 1, right, bottom, fill);
        context.drawHorizontalLine(trashX, right, trashY, outline);
        context.drawHorizontalLine(trashX + 1, right - 1, trashY + 1, outline);
        context.drawHorizontalLine(trashX, right, bodyTop, outline);
        context.drawHorizontalLine(trashX, right, bottom, outline);
        context.drawVerticalLine(trashX, bodyTop, bottom, outline);
        context.drawVerticalLine(right, bodyTop, bottom, outline);

        int handleLeft = trashX + 2;
        int handleRight = right - 2;
        context.drawHorizontalLine(handleLeft, handleRight, trashY - 1, outline);
        context.drawVerticalLine(handleLeft, trashY - 1, trashY, outline);
        context.drawVerticalLine(handleRight, trashY - 1, trashY, outline);

        int slatTop = bodyTop + 2;
        int slatBottom = bottom - 2;
        int slatLeft = trashX + 2;
        int slatMid = trashX + size / 2;
        int slatRight = right - 2;
        context.drawVerticalLine(slatLeft, slatTop, slatBottom, outline);
        context.drawVerticalLine(slatMid, slatTop, slatBottom, outline);
        context.drawVerticalLine(slatRight, slatTop, slatBottom, outline);
    }

    private void drawEditIcon(DrawContext context, Bounds bounds) {
        int iconX = editIconX(bounds);
        int iconY = editIconY(bounds);
        int color = 0xFFFFFFFF;

        context.drawHorizontalLine(iconX + 1, iconX + 4, iconY + 1, color);
        context.drawHorizontalLine(iconX + 2, iconX + 5, iconY + 2, color);
        context.drawHorizontalLine(iconX + 3, iconX + 6, iconY + 3, color);
        context.drawHorizontalLine(iconX + 4, iconX + 7, iconY + 4, color);
        context.drawHorizontalLine(iconX + 5, iconX + 6, iconY + 5, color);
    }


    private void drawLasso(DrawContext context) {
        int left = (int) Math.round(Math.min(lassoStartX, lassoEndX));
        int right = (int) Math.round(Math.max(lassoStartX, lassoEndX));
        int top = (int) Math.round(Math.min(lassoStartY, lassoEndY));
        int bottom = (int) Math.round(Math.max(lassoStartY, lassoEndY));

        if (right - left < 2 || bottom - top < 2) {
            return;
        }

        int fill = 0x22FFFFFF;
        int outline = 0xFFFFFFFF;
        context.fill(left, top, right, bottom, fill);
        context.drawHorizontalLine(left, right, top, outline);
        context.drawHorizontalLine(left, right, bottom, outline);
        context.drawVerticalLine(left, top, bottom, outline);
        context.drawVerticalLine(right, top, bottom, outline);
    }

    private void drawHandle(DrawContext context, int x, int y) {
        int size = HANDLE_SIZE;
        context.fill(x, y, x + size, y + size, 0xFFFFFFFF);
        drawBorder(context, x, y, size, size, 0xFF000000);
    }

    private void resizeSelectedGroup(double overlayX, double overlayY) {
        Group primary = primarySelected();
        if (dragStartBounds == null || primary == null) {
            return;
        }

        int left = dragStartBounds.x;
        int top = dragStartBounds.y;
        int right = dragStartBounds.x + dragStartBounds.width;
        int bottom = dragStartBounds.y + dragStartBounds.height;

        int newLeft = left;
        int newTop = top;
        int newRight = right;
        int newBottom = bottom;

        int dx = (int) Math.round(overlayX - dragStartX);
        int dy = (int) Math.round(overlayY - dragStartY);

        double aspect = dragStartBounds.width / (double) dragStartBounds.height;
        int width;
        int height;
        boolean horizontalDominant = Math.abs(dx) >= Math.abs(dy);

        switch (resizeHandle) {
            case TOP_LEFT -> {
                width = Math.max(MIN_GROUP_SIZE, right - (left + dx));
                height = Math.max(MIN_GROUP_SIZE, bottom - (top + dy));
                if (horizontalDominant) {
                    height = Math.max(MIN_GROUP_SIZE, (int) Math.round(width / aspect));
                } else {
                    width = Math.max(MIN_GROUP_SIZE, (int) Math.round(height * aspect));
                }
                newLeft = right - width;
                newTop = bottom - height;
            }
            case TOP_RIGHT -> {
                width = Math.max(MIN_GROUP_SIZE, (right + dx) - left);
                height = Math.max(MIN_GROUP_SIZE, bottom - (top + dy));
                if (horizontalDominant) {
                    height = Math.max(MIN_GROUP_SIZE, (int) Math.round(width / aspect));
                } else {
                    width = Math.max(MIN_GROUP_SIZE, (int) Math.round(height * aspect));
                }
                newRight = left + width;
                newTop = bottom - height;
            }
            case BOTTOM_LEFT -> {
                width = Math.max(MIN_GROUP_SIZE, right - (left + dx));
                height = Math.max(MIN_GROUP_SIZE, (bottom + dy) - top);
                if (horizontalDominant) {
                    height = Math.max(MIN_GROUP_SIZE, (int) Math.round(width / aspect));
                } else {
                    width = Math.max(MIN_GROUP_SIZE, (int) Math.round(height * aspect));
                }
                newLeft = right - width;
                newBottom = top + height;
            }
            case BOTTOM_RIGHT -> {
                width = Math.max(MIN_GROUP_SIZE, (right + dx) - left);
                height = Math.max(MIN_GROUP_SIZE, (bottom + dy) - top);
                if (horizontalDominant) {
                    height = Math.max(MIN_GROUP_SIZE, (int) Math.round(width / aspect));
                } else {
                    width = Math.max(MIN_GROUP_SIZE, (int) Math.round(height * aspect));
                }
                newRight = left + width;
                newBottom = top + height;
            }
            case NONE -> {
                return;
            }
        }

        Bounds target = new Bounds(newLeft, newTop, newRight - newLeft, newBottom - newTop);
        Bounds snapped = applyResizeSnapping(target, resizeHandle, horizontalDominant, aspect);
        primary.resizeTo(dragStartBounds, snapped, resizeSnapshots);
    }

    private ResizeHandle hitTestHandle(Bounds bounds, double overlayX, double overlayY) {
        int x = bounds.x;
        int y = bounds.y;
        int width = bounds.width;
        int height = bounds.height;

        if (isPointInside(overlayX, overlayY, x - HANDLE_SIZE / 2.0, y - HANDLE_SIZE / 2.0, HANDLE_SIZE, HANDLE_SIZE)) {
            return ResizeHandle.TOP_LEFT;
        }
        if (isPointInside(overlayX, overlayY, x + width - HANDLE_SIZE / 2.0, y - HANDLE_SIZE / 2.0, HANDLE_SIZE, HANDLE_SIZE)) {
            return ResizeHandle.TOP_RIGHT;
        }
        if (isPointInside(overlayX, overlayY, x - HANDLE_SIZE / 2.0, y + height - HANDLE_SIZE / 2.0, HANDLE_SIZE, HANDLE_SIZE)) {
            return ResizeHandle.BOTTOM_LEFT;
        }
        if (isPointInside(overlayX, overlayY, x + width - HANDLE_SIZE / 2.0, y + height - HANDLE_SIZE / 2.0, HANDLE_SIZE, HANDLE_SIZE)) {
            return ResizeHandle.BOTTOM_RIGHT;
        }

        return ResizeHandle.NONE;
    }

    private boolean hitTestTrashIcon(Bounds bounds, double overlayX, double overlayY) {
        int trashX = trashIconX(bounds);
        int trashY = trashIconY(bounds);
        return isPointInside(overlayX, overlayY, trashX, trashY, TRASH_SIZE, TRASH_SIZE);
    }

    private boolean hitTestEditIcon(Bounds bounds, double overlayX, double overlayY) {
        int editX = editIconX(bounds);
        int editY = editIconY(bounds);
        return isPointInside(overlayX, overlayY, editX, editY, EDIT_ICON_SIZE, EDIT_ICON_SIZE);
    }

    private int trashIconX(Bounds bounds) {
        return bounds.x + bounds.width + 5;
    }

    private int trashIconY(Bounds bounds) {
        return bounds.y + bounds.height + 4;
    }

    private int editIconX(Bounds bounds) {
        return trashIconX(bounds) + TRASH_SIZE + EDIT_ICON_GAP;
    }

    private int editIconY(Bounds bounds) {
        return trashIconY(bounds);
    }


    private boolean isInSidebarArea(double mouseX, double mouseY) {
        if (sidebarProgress <= 0.01f) {
            return false;
        }
        int sidebarWidth = (int) (SIDEBAR_WIDTH * sidebarProgress);
        int sidebarX = width - sidebarWidth;
        return mouseX >= sidebarX && mouseY >= 0 && mouseY <= height;
    }

    private void selectGroupInLasso() {
        if (!lassoActive) {
            return;
        }
        int left = (int) Math.round(Math.min(lassoStartX, lassoEndX));
        int right = (int) Math.round(Math.max(lassoStartX, lassoEndX));
        int top = (int) Math.round(Math.min(lassoStartY, lassoEndY));
        int bottom = (int) Math.round(Math.max(lassoStartY, lassoEndY));

        if (right - left < 2 || bottom - top < 2) {
            return;
        }

        Bounds lasso = new Bounds(left, top, right - left, bottom - top);
        selectedGroups.clear();
        for (Group group : groups) {
            if (group.isVisible() && group.intersects(lasso)) {
                selectedGroups.add(group);
            }
        }
    }

    private void updateLassoSelection() {
        if (!lassoActive) {
            return;
        }
        int left = (int) Math.round(Math.min(lassoStartX, lassoEndX));
        int right = (int) Math.round(Math.max(lassoStartX, lassoEndX));
        int top = (int) Math.round(Math.min(lassoStartY, lassoEndY));
        int bottom = (int) Math.round(Math.max(lassoStartY, lassoEndY));

        if (right - left < 2 || bottom - top < 2) {
            selectedGroups.clear();
            return;
        }

        Bounds lasso = new Bounds(left, top, right - left, bottom - top);
        selectedGroups.clear();
        for (Group group : groups) {
            if (group.isVisible() && group.intersects(lasso)) {
                selectedGroups.add(group);
            }
        }
    }

    private void resetWorkspace() {
        config.resetLayout();
        OverlayConfig.save(config);
        selectedGroups.clear();
        closeEditor();
        dragMode = DragMode.NONE;
        resizeHandle = ResizeHandle.NONE;
        draggingFromSidebar = false;
        dragStartBounds = null;
        resizeSnapshots = null;
        lassoActive = false;
        guideLines.clear();
        distanceLabels.clear();
        if (searchField != null) {
            searchField.setText("");
        }
        rebuildGroups();
    }

    private void buildTemplates() {
        templates.clear();

        templates.put("wasd", Template.keys("wasd", "WASD", List.of(
            key("W", OverlayConfig.InputType.KEY, GLFW.GLFW_KEY_W, 28, 0, 24, 24),
            key("A", OverlayConfig.InputType.KEY, GLFW.GLFW_KEY_A, 0, 28, 24, 24),
            key("S", OverlayConfig.InputType.KEY, GLFW.GLFW_KEY_S, 28, 28, 24, 24),
            key("D", OverlayConfig.InputType.KEY, GLFW.GLFW_KEY_D, 56, 28, 24, 24)
        )));

        templates.put("space", Template.keys("space", "SPACE", List.of(
            key("SPACE", OverlayConfig.InputType.KEY, GLFW.GLFW_KEY_SPACE, 0, 0, 80, 24)
        )));

        templates.put("shift", Template.keys("shift", "SHIFT", List.of(
            key("SHIFT", OverlayConfig.InputType.KEY, GLFW.GLFW_KEY_LEFT_SHIFT, 0, 0, 52, 24)
        )));

        templates.put("ctrl", Template.keys("ctrl", "CTRL", List.of(
            key("CTRL", OverlayConfig.InputType.KEY, GLFW.GLFW_KEY_LEFT_CONTROL, 0, 0, 52, 24)
        )));

        templates.put("lmb", Template.keys("lmb", "LMB", List.of(
            key("LMB", OverlayConfig.InputType.MOUSE, GLFW.GLFW_MOUSE_BUTTON_LEFT, 0, 0, 32, 32)
        )));

        templates.put("rmb", Template.keys("rmb", "RMB", List.of(
            key("RMB", OverlayConfig.InputType.MOUSE, GLFW.GLFW_MOUSE_BUTTON_RIGHT, 0, 0, 32, 32)
        )));

        templates.put("mmb", Template.keys("mmb", "MMB", List.of(
            key("MMB", OverlayConfig.InputType.MOUSE, GLFW.GLFW_MOUSE_BUTTON_MIDDLE, 0, 0, 32, 24)
        )));

        templates.put("keyboard", Template.keys("keyboard", "Mini Keyboard", miniKeyboardKeys()));
        templates.put("numbers", Template.keys("numbers", "Number Row", numberRowKeys()));
        templates.put("function", Template.keys("function", "Function Keys", functionKeys()));
        templates.put("arrows", Template.keys("arrows", "Arrow Keys", arrowKeys()));
        templates.put("utility", Template.keys("utility", "Utility Keys", utilityKeys()));
        templates.put("full_keyboard", Template.keys("full_keyboard", "Full Keyboard", fullKeyboardKeys()));

        templates.put("cps", Template.stat("cps", "CPS Counter", statKey("CPS", "cps", 0, 0)));
        templates.put("cps_rmb", Template.stat("cps_rmb", "RMB CPS", statKey("RMB CPS", "cps_rmb", 0, 0)));
        templates.put("cps_mmb", Template.stat("cps_mmb", "MMB CPS", statKey("MMB CPS", "cps_mmb", 0, 0)));
    }

    private List<Template> getTemplates(String filterText) {
        String filter = filterText == null ? "" : filterText.trim().toLowerCase();
        List<Template> list = new ArrayList<>();
        for (Template template : templates.values()) {
            if (template.matchesFilter(filter)) {
                list.add(template);
            }
        }
        return list;
    }

    private void addTemplate(Template template) {
        for (OverlayConfig.KeyDefinition key : template.createKeys()) {
            config.keys.add(key);
        }
        OverlayConfig.save(config);
    }

    private OverlayConfig.KeyDefinition key(String label, OverlayConfig.InputType type, int code, int x, int y, int width, int height) {
        return new OverlayConfig.KeyDefinition(label, type, code, x, y, width, height, type == OverlayConfig.InputType.MOUSE ? label.toLowerCase() : "keyboard");
    }

    private OverlayConfig.KeyDefinition statKey(String label, String statId, int x, int y) {
        return new OverlayConfig.KeyDefinition(label, statId, x, y, 64, 28, "stats");
    }

    private List<OverlayConfig.KeyDefinition> miniKeyboardKeys() {
        List<OverlayConfig.KeyDefinition> keys = new ArrayList<>();
        keys.add(key("1", OverlayConfig.InputType.KEY, GLFW.GLFW_KEY_1, 0, 0, 14, 14));
        keys.add(key("2", OverlayConfig.InputType.KEY, GLFW.GLFW_KEY_2, 16, 0, 14, 14));
        keys.add(key("3", OverlayConfig.InputType.KEY, GLFW.GLFW_KEY_3, 32, 0, 14, 14));
        keys.add(key("4", OverlayConfig.InputType.KEY, GLFW.GLFW_KEY_4, 48, 0, 14, 14));
        keys.add(key("5", OverlayConfig.InputType.KEY, GLFW.GLFW_KEY_5, 64, 0, 14, 14));

        keys.add(key("Q", OverlayConfig.InputType.KEY, GLFW.GLFW_KEY_Q, 0, 16, 14, 14));
        keys.add(key("W", OverlayConfig.InputType.KEY, GLFW.GLFW_KEY_W, 16, 16, 14, 14));
        keys.add(key("E", OverlayConfig.InputType.KEY, GLFW.GLFW_KEY_E, 32, 16, 14, 14));
        keys.add(key("R", OverlayConfig.InputType.KEY, GLFW.GLFW_KEY_R, 48, 16, 14, 14));
        keys.add(key("T", OverlayConfig.InputType.KEY, GLFW.GLFW_KEY_T, 64, 16, 14, 14));

        keys.add(key("A", OverlayConfig.InputType.KEY, GLFW.GLFW_KEY_A, 0, 32, 14, 14));
        keys.add(key("S", OverlayConfig.InputType.KEY, GLFW.GLFW_KEY_S, 16, 32, 14, 14));
        keys.add(key("D", OverlayConfig.InputType.KEY, GLFW.GLFW_KEY_D, 32, 32, 14, 14));
        keys.add(key("F", OverlayConfig.InputType.KEY, GLFW.GLFW_KEY_F, 48, 32, 14, 14));
        keys.add(key("G", OverlayConfig.InputType.KEY, GLFW.GLFW_KEY_G, 64, 32, 14, 14));

        keys.add(key("Z", OverlayConfig.InputType.KEY, GLFW.GLFW_KEY_Z, 8, 48, 14, 14));
        keys.add(key("X", OverlayConfig.InputType.KEY, GLFW.GLFW_KEY_X, 24, 48, 14, 14));
        keys.add(key("C", OverlayConfig.InputType.KEY, GLFW.GLFW_KEY_C, 40, 48, 14, 14));
        keys.add(key("V", OverlayConfig.InputType.KEY, GLFW.GLFW_KEY_V, 56, 48, 14, 14));
        return keys;
    }

    private List<OverlayConfig.KeyDefinition> numberRowKeys() {
        List<OverlayConfig.KeyDefinition> keys = new ArrayList<>();
        int x = 0;
        for (int i = 1; i <= 9; i++) {
            keys.add(key(Integer.toString(i), OverlayConfig.InputType.KEY, GLFW.GLFW_KEY_0 + i, x, 0, 14, 14));
            x += 16;
        }
        keys.add(key("0", OverlayConfig.InputType.KEY, GLFW.GLFW_KEY_0, x, 0, 14, 14));
        return keys;
    }

    private List<OverlayConfig.KeyDefinition> functionKeys() {
        List<OverlayConfig.KeyDefinition> keys = new ArrayList<>();
        int x = 0;
        for (int i = 1; i <= 12; i++) {
            keys.add(key("F" + i, OverlayConfig.InputType.KEY, GLFW.GLFW_KEY_F1 + (i - 1), x, 0, 18, 14));
            x += 20;
        }
        return keys;
    }

    private List<OverlayConfig.KeyDefinition> arrowKeys() {
        List<OverlayConfig.KeyDefinition> keys = new ArrayList<>();
        keys.add(key("↑", OverlayConfig.InputType.KEY, GLFW.GLFW_KEY_UP, 18, 0, 18, 18));
        keys.add(key("←", OverlayConfig.InputType.KEY, GLFW.GLFW_KEY_LEFT, 0, 20, 18, 18));
        keys.add(key("↓", OverlayConfig.InputType.KEY, GLFW.GLFW_KEY_DOWN, 18, 20, 18, 18));
        keys.add(key("→", OverlayConfig.InputType.KEY, GLFW.GLFW_KEY_RIGHT, 36, 20, 18, 18));
        return keys;
    }

    private List<OverlayConfig.KeyDefinition> utilityKeys() {
        List<OverlayConfig.KeyDefinition> keys = new ArrayList<>();
        keys.add(key("ESC", OverlayConfig.InputType.KEY, GLFW.GLFW_KEY_ESCAPE, 0, 0, 24, 18));
        keys.add(key("TAB", OverlayConfig.InputType.KEY, GLFW.GLFW_KEY_TAB, 28, 0, 24, 18));
        keys.add(key("E", OverlayConfig.InputType.KEY, GLFW.GLFW_KEY_E, 56, 0, 18, 18));
        keys.add(key("Q", OverlayConfig.InputType.KEY, GLFW.GLFW_KEY_Q, 78, 0, 18, 18));
        keys.add(key("R", OverlayConfig.InputType.KEY, GLFW.GLFW_KEY_R, 100, 0, 18, 18));
        keys.add(key("F", OverlayConfig.InputType.KEY, GLFW.GLFW_KEY_F, 122, 0, 18, 18));
        return keys;
    }

    private List<OverlayConfig.KeyDefinition> fullKeyboardKeys() {
        List<OverlayConfig.KeyDefinition> keys = new ArrayList<>();
        int key = 18;
        int gap = 2;
        int row0 = 0;
        int row1 = key + gap;
        int row2 = row1 + key + gap;
        int row3 = row2 + key + gap;
        int row4 = row3 + key + gap;
        int row5 = row4 + key + gap;

        int x = 0;
        keys.add(key("ESC", OverlayConfig.InputType.KEY, GLFW.GLFW_KEY_ESCAPE, x, row0, 24, key));
        x += 28;
        for (int i = 1; i <= 12; i++) {
            keys.add(key("F" + i, OverlayConfig.InputType.KEY, GLFW.GLFW_KEY_F1 + (i - 1), x, row0, key, key));
            x += key + gap;
        }

        x = 0;
        keys.add(key("~", OverlayConfig.InputType.KEY, GLFW.GLFW_KEY_GRAVE_ACCENT, x, row1, key, key));
        x += key + gap;
        for (int i = 1; i <= 9; i++) {
            keys.add(key(Integer.toString(i), OverlayConfig.InputType.KEY, GLFW.GLFW_KEY_0 + i, x, row1, key, key));
            x += key + gap;
        }
        keys.add(key("0", OverlayConfig.InputType.KEY, GLFW.GLFW_KEY_0, x, row1, key, key));
        x += key + gap;
        keys.add(key("-", OverlayConfig.InputType.KEY, GLFW.GLFW_KEY_MINUS, x, row1, key, key));
        x += key + gap;
        keys.add(key("=", OverlayConfig.InputType.KEY, GLFW.GLFW_KEY_EQUAL, x, row1, key, key));
        x += key + gap;
        keys.add(key("BACK", OverlayConfig.InputType.KEY, GLFW.GLFW_KEY_BACKSPACE, x, row1, 34, key));

        x = 0;
        keys.add(key("TAB", OverlayConfig.InputType.KEY, GLFW.GLFW_KEY_TAB, x, row2, 28, key));
        x += 30;
        String[] rowQ = {"Q","W","E","R","T","Y","U","I","O","P","[","]"};
        int[] rowQCodes = {GLFW.GLFW_KEY_Q,GLFW.GLFW_KEY_W,GLFW.GLFW_KEY_E,GLFW.GLFW_KEY_R,GLFW.GLFW_KEY_T,GLFW.GLFW_KEY_Y,
            GLFW.GLFW_KEY_U,GLFW.GLFW_KEY_I,GLFW.GLFW_KEY_O,GLFW.GLFW_KEY_P,GLFW.GLFW_KEY_LEFT_BRACKET,GLFW.GLFW_KEY_RIGHT_BRACKET};
        for (int i = 0; i < rowQ.length; i++) {
            keys.add(key(rowQ[i], OverlayConfig.InputType.KEY, rowQCodes[i], x, row2, key, key));
            x += key + gap;
        }
        keys.add(key("\\", OverlayConfig.InputType.KEY, GLFW.GLFW_KEY_BACKSLASH, x, row2, 28, key));

        x = 0;
        keys.add(key("CAPS", OverlayConfig.InputType.KEY, GLFW.GLFW_KEY_CAPS_LOCK, x, row3, 32, key));
        x += 34;
        String[] rowA = {"A","S","D","F","G","H","J","K","L",";","'"};
        int[] rowACodes = {GLFW.GLFW_KEY_A,GLFW.GLFW_KEY_S,GLFW.GLFW_KEY_D,GLFW.GLFW_KEY_F,GLFW.GLFW_KEY_G,GLFW.GLFW_KEY_H,
            GLFW.GLFW_KEY_J,GLFW.GLFW_KEY_K,GLFW.GLFW_KEY_L,GLFW.GLFW_KEY_SEMICOLON,GLFW.GLFW_KEY_APOSTROPHE};
        for (int i = 0; i < rowA.length; i++) {
            keys.add(key(rowA[i], OverlayConfig.InputType.KEY, rowACodes[i], x, row3, key, key));
            x += key + gap;
        }
        keys.add(key("ENTER", OverlayConfig.InputType.KEY, GLFW.GLFW_KEY_ENTER, x, row3, 36, key));

        x = 0;
        keys.add(key("SHIFT", OverlayConfig.InputType.KEY, GLFW.GLFW_KEY_LEFT_SHIFT, x, row4, 40, key));
        x += 42;
        String[] rowZ = {"Z","X","C","V","B","N","M",",",".","/"};
        int[] rowZCodes = {GLFW.GLFW_KEY_Z,GLFW.GLFW_KEY_X,GLFW.GLFW_KEY_C,GLFW.GLFW_KEY_V,GLFW.GLFW_KEY_B,GLFW.GLFW_KEY_N,
            GLFW.GLFW_KEY_M,GLFW.GLFW_KEY_COMMA,GLFW.GLFW_KEY_PERIOD,GLFW.GLFW_KEY_SLASH};
        for (int i = 0; i < rowZ.length; i++) {
            keys.add(key(rowZ[i], OverlayConfig.InputType.KEY, rowZCodes[i], x, row4, key, key));
            x += key + gap;
        }
        keys.add(key("SHIFT", OverlayConfig.InputType.KEY, GLFW.GLFW_KEY_RIGHT_SHIFT, x, row4, 44, key));

        x = 0;
        keys.add(key("CTRL", OverlayConfig.InputType.KEY, GLFW.GLFW_KEY_LEFT_CONTROL, x, row5, 28, key));
        x += 30;
        keys.add(key("WIN", OverlayConfig.InputType.KEY, GLFW.GLFW_KEY_LEFT_SUPER, x, row5, 24, key));
        x += 26;
        keys.add(key("ALT", OverlayConfig.InputType.KEY, GLFW.GLFW_KEY_LEFT_ALT, x, row5, 24, key));
        x += 26;
        keys.add(key("SPACE", OverlayConfig.InputType.KEY, GLFW.GLFW_KEY_SPACE, x, row5, 90, key));
        x += 92;
        keys.add(key("ALT", OverlayConfig.InputType.KEY, GLFW.GLFW_KEY_RIGHT_ALT, x, row5, 24, key));
        x += 26;
        keys.add(key("WIN", OverlayConfig.InputType.KEY, GLFW.GLFW_KEY_RIGHT_SUPER, x, row5, 24, key));
        x += 26;
        keys.add(key("MENU", OverlayConfig.InputType.KEY, GLFW.GLFW_KEY_MENU, x, row5, 24, key));
        x += 26;
        keys.add(key("CTRL", OverlayConfig.InputType.KEY, GLFW.GLFW_KEY_RIGHT_CONTROL, x, row5, 28, key));

        return keys;
    }

    private Bounds selectedBounds() {
        if (selectedGroups.isEmpty()) {
            return null;
        }
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (Group group : selectedGroups) {
            Bounds bounds = group.getBounds();
            minX = Math.min(minX, bounds.x);
            minY = Math.min(minY, bounds.y);
            maxX = Math.max(maxX, bounds.x + bounds.width);
            maxY = Math.max(maxY, bounds.y + bounds.height);
        }

        return new Bounds(minX, minY, maxX - minX, maxY - minY);
    }

    private List<OverlayConfig.KeyDefinition> keysForSelectedGroups() {
        List<OverlayConfig.KeyDefinition> keys = new ArrayList<>();
        for (Group group : selectedGroups) {
            keys.addAll(group.keys);
        }
        return keys;
    }

    private Bounds applySnapping(Bounds target) {
        guideLines.clear();
        distanceLabels.clear();

        if (!config.snappingEnabled && !config.guidesEnabled && !config.distanceLabelsEnabled) {
            return target;
        }

        SnapPair snap = computeSnapResults(target, true, true, true, true, true, true);
        Bounds snapped = new Bounds(
            (int) Math.round(target.x + snap.snapDx),
            (int) Math.round(target.y + snap.snapDy),
            target.width,
            target.height
        );
        updateSnapOverlays(snap.xResult, snap.yResult, snapped);
        return snapped;
    }

    private Bounds applyResizeSnapping(Bounds target, ResizeHandle handle, boolean horizontalDominant, double aspect) {
        guideLines.clear();
        distanceLabels.clear();

        if (!config.snappingEnabled && !config.guidesEnabled && !config.distanceLabelsEnabled) {
            return target;
        }

        boolean moveLeft = handle == ResizeHandle.TOP_LEFT || handle == ResizeHandle.BOTTOM_LEFT;
        boolean moveRight = handle == ResizeHandle.TOP_RIGHT || handle == ResizeHandle.BOTTOM_RIGHT;
        boolean moveTop = handle == ResizeHandle.TOP_LEFT || handle == ResizeHandle.TOP_RIGHT;
        boolean moveBottom = handle == ResizeHandle.BOTTOM_LEFT || handle == ResizeHandle.BOTTOM_RIGHT;

        SnapPair snap = computeSnapResults(target, moveLeft, moveRight, false, moveTop, moveBottom, false);

        int left = target.x;
        int top = target.y;
        int right = target.x + target.width;
        int bottom = target.y + target.height;

        // Snap along the dominant axis to preserve the resize aspect ratio.
        if (horizontalDominant) {
            if (moveLeft && snap.snapDx != 0) {
                left = (int) Math.round(target.x + snap.snapDx);
            }
            if (moveRight && snap.snapDx != 0) {
                right = (int) Math.round(target.x + target.width + snap.snapDx);
            }
            int width = Math.max(MIN_GROUP_SIZE, right - left);
            int height = Math.max(MIN_GROUP_SIZE, (int) Math.round(width / aspect));
            if (moveTop) {
                top = bottom - height;
            } else if (moveBottom) {
                bottom = top + height;
            }
        } else {
            if (moveTop && snap.snapDy != 0) {
                top = (int) Math.round(target.y + snap.snapDy);
            }
            if (moveBottom && snap.snapDy != 0) {
                bottom = (int) Math.round(target.y + target.height + snap.snapDy);
            }
            int height = Math.max(MIN_GROUP_SIZE, bottom - top);
            int width = Math.max(MIN_GROUP_SIZE, (int) Math.round(height * aspect));
            if (moveLeft) {
                left = right - width;
            } else if (moveRight) {
                right = left + width;
            }
        }

        Bounds snapped = new Bounds(left, top, right - left, bottom - top);
        updateSnapOverlays(snap.xResult, snap.yResult, snapped);
        return snapped;
    }

    private SnapPair computeSnapResults(
        Bounds target,
        boolean includeLeft,
        boolean includeRight,
        boolean includeCenterX,
        boolean includeTop,
        boolean includeBottom,
        boolean includeCenterY
    ) {
        int threshold = Math.max(1, config.snapThreshold);
        double scale = config.scale;

        double left = target.x;
        double right = target.x + target.width;
        double top = target.y;
        double bottom = target.y + target.height;
        double centerX = left + target.width / 2.0;
        double centerY = top + target.height / 2.0;

        double leftScreen = config.offsetX + left * scale;
        double rightScreen = config.offsetX + right * scale;
        double topScreen = config.offsetY + top * scale;
        double bottomScreen = config.offsetY + bottom * scale;
        double centerXScreen = config.offsetX + centerX * scale;
        double centerYScreen = config.offsetY + centerY * scale;

        double snapDx = 0;
        double snapDy = 0;

        SnapResult xResult = new SnapResult();
        SnapResult yResult = new SnapResult();

        if (config.snappingEnabled || config.guidesEnabled) {
            double[] xGuides = new double[] {0.0, width / 2.0, width};
            double[] yGuides = new double[] {0.0, height / 2.0, height};

            for (double guide : xGuides) {
                if (includeCenterX) {
                    considerSnapScreen(centerXScreen, guide, threshold, xResult, false);
                }
                if (includeLeft) {
                    considerSnapScreen(leftScreen, guide, threshold, xResult, false);
                }
                if (includeRight) {
                    considerSnapScreen(rightScreen, guide, threshold, xResult, false);
                }
            }

            for (double guide : yGuides) {
                if (includeCenterY) {
                    considerSnapScreen(centerYScreen, guide, threshold, yResult, true);
                }
                if (includeTop) {
                    considerSnapScreen(topScreen, guide, threshold, yResult, true);
                }
                if (includeBottom) {
                    considerSnapScreen(bottomScreen, guide, threshold, yResult, true);
                }
            }

            for (Group group : groups) {
                if (!group.isVisible() || selectedGroups.contains(group)) {
                    continue;
                }
                Bounds bounds = group.getBounds();
                double gLeft = bounds.x;
                double gRight = bounds.x + bounds.width;
                double gTop = bounds.y;
                double gBottom = bounds.y + bounds.height;
                double gCenterX = gLeft + bounds.width / 2.0;
                double gCenterY = gTop + bounds.height / 2.0;

                double gLeftScreen = config.offsetX + gLeft * scale;
                double gRightScreen = config.offsetX + gRight * scale;
                double gTopScreen = config.offsetY + gTop * scale;
                double gBottomScreen = config.offsetY + gBottom * scale;
                double gCenterXScreen = config.offsetX + gCenterX * scale;
                double gCenterYScreen = config.offsetY + gCenterY * scale;

                if (includeLeft) {
                    considerSnapScreen(leftScreen, gLeftScreen, threshold, xResult, false);
                    considerSnapScreen(leftScreen, gRightScreen, threshold, xResult, false);
                }
                if (includeRight) {
                    considerSnapScreen(rightScreen, gRightScreen, threshold, xResult, false);
                    considerSnapScreen(rightScreen, gLeftScreen, threshold, xResult, false);
                }
                if (includeCenterX) {
                    considerSnapScreen(centerXScreen, gCenterXScreen, threshold, xResult, false);
                }

                if (includeTop) {
                    considerSnapScreen(topScreen, gTopScreen, threshold, yResult, true);
                    considerSnapScreen(topScreen, gBottomScreen, threshold, yResult, true);
                }
                if (includeBottom) {
                    considerSnapScreen(bottomScreen, gBottomScreen, threshold, yResult, true);
                    considerSnapScreen(bottomScreen, gTopScreen, threshold, yResult, true);
                }
                if (includeCenterY) {
                    considerSnapScreen(centerYScreen, gCenterYScreen, threshold, yResult, true);
                }
            }
        }

        if (config.snappingEnabled) {
            if (xResult.bestDeltaScreen != null) {
                snapDx = xResult.bestDeltaScreen / scale;
            }
            if (yResult.bestDeltaScreen != null) {
                snapDy = yResult.bestDeltaScreen / scale;
            }
        }

        return new SnapPair(xResult, yResult, snapDx, snapDy);
    }

    private void updateSnapOverlays(SnapResult xResult, SnapResult yResult, Bounds snapped) {
        if (config.guidesEnabled) {
            guideLines.addAll(xResult.guides);
            guideLines.addAll(yResult.guides);
        }

        if (config.distanceLabelsEnabled) {
            int threshold = Math.max(1, config.snapThreshold);
            int distanceThreshold = threshold * 2;
            double scale = config.scale;
            int snappedLeft = snapped.x;
            int snappedTop = snapped.y;
            int snappedRight = snapped.x + snapped.width;
            int snappedBottom = snapped.y + snapped.height;

            int distLeft = (int) Math.round(config.offsetX + snappedLeft * scale);
            int distTop = (int) Math.round(config.offsetY + snappedTop * scale);
            int distRight = (int) Math.round(width - (config.offsetX + snappedRight * scale));
            int distBottom = (int) Math.round(height - (config.offsetY + snappedBottom * scale));

            if (distLeft <= distanceThreshold) {
                distanceLabels.add(new DistanceLabel(snappedLeft + 2, snappedTop + snapped.height / 2, distLeft));
            }
            if (distRight <= distanceThreshold) {
                distanceLabels.add(new DistanceLabel(snappedRight - 2, snappedTop + snapped.height / 2, distRight));
            }
            if (distTop <= distanceThreshold) {
                distanceLabels.add(new DistanceLabel(snappedLeft + snapped.width / 2, snappedTop + 2, distTop));
            }
            if (distBottom <= distanceThreshold) {
                distanceLabels.add(new DistanceLabel(snappedLeft + snapped.width / 2, snappedBottom - 2, distBottom));
            }
        }
    }

    private void considerSnapScreen(double targetEdge, double guide, int threshold, SnapResult result, boolean horizontal) {
        double delta = guide - targetEdge;
        double distance = Math.abs(delta);
        if (distance <= threshold) {
            result.guides.add(new GuideLine(horizontal, (int) Math.round(guide)));
            if (result.bestDeltaScreen == null || distance < Math.abs(result.bestDeltaScreen)) {
                result.bestDeltaScreen = delta;
            }
        }
    }

    private void drawGuides(DrawContext context) {
        double minX = -config.offsetX / config.scale;
        double minY = -config.offsetY / config.scale;
        double maxX = (width - config.offsetX) / config.scale;
        double maxY = (height - config.offsetY) / config.scale;

        for (GuideLine line : guideLines) {
            if (line.horizontal) {
                double overlayY = (line.screenPosition - config.offsetY) / config.scale;
                drawDottedHorizontal(context, minX, maxX, overlayY, GUIDE_COLOR);
            } else {
                double overlayX = (line.screenPosition - config.offsetX) / config.scale;
                drawDottedVertical(context, minY, maxY, overlayX, GUIDE_COLOR);
            }
        }
    }

    private void drawDottedHorizontal(DrawContext context, double startX, double endX, double y, int color) {
        int x1 = (int) Math.round(Math.min(startX, endX));
        int x2 = (int) Math.round(Math.max(startX, endX));
        int yPos = (int) Math.round(y);
        int segment = 6;
        int gap = 4;
        for (int x = x1; x <= x2; x += segment + gap) {
            int xEnd = Math.min(x + segment, x2);
            context.drawHorizontalLine(x, xEnd, yPos, color);
        }
    }

    private void drawDottedVertical(DrawContext context, double startY, double endY, double x, int color) {
        int y1 = (int) Math.round(Math.min(startY, endY));
        int y2 = (int) Math.round(Math.max(startY, endY));
        int xPos = (int) Math.round(x);
        int segment = 6;
        int gap = 4;
        for (int y = y1; y <= y2; y += segment + gap) {
            int yEnd = Math.min(y + segment, y2);
            context.drawVerticalLine(xPos, y, yEnd, color);
        }
    }

    private void drawDistanceLabels(DrawContext context) {
        for (DistanceLabel label : distanceLabels) {
            String text = Integer.toString(label.value);
            int textWidth = textRenderer.getWidth(text);
            int x = label.x - textWidth / 2;
            int y = label.y - textRenderer.fontHeight / 2;
            context.fill(x - 2, y - 2, x + textWidth + 2, y + textRenderer.fontHeight + 2, 0xCC111111);
            context.drawTextWithShadow(textRenderer, text, x, y, 0xFFFFFFFF);
        }
    }

    private Group hitTestGroup(double overlayX, double overlayY) {
        return groups.stream()
            .filter(Group::isVisible)
            .sorted(Comparator.comparingInt(group -> -group.getBounds().area()))
            .filter(group -> group.contains(overlayX, overlayY))
            .findFirst()
            .orElse(null);
    }

    private void moveSelectedBy(int dx, int dy) {
        for (Group group : selectedGroups) {
            group.moveBy(dx, dy);
        }
    }

    private void removeSelected() {
        if (selectedGroups.isEmpty()) {
            return;
        }
        for (Group group : selectedGroups) {
            group.setVisible(false);
        }
        selectedGroups.clear();
        closeEditor();
    }

    private List<Group> getHiddenGroups(String filterText) {
        String filter = filterText == null ? "" : filterText.trim().toLowerCase();
        List<Group> hidden = new ArrayList<>();
        for (Group group : groups) {
            if (!group.isVisible() && group.matchesFilter(filter)) {
                hidden.add(group);
            }
        }
        return hidden;
    }

    private double toOverlayX(double screenX) {
        float renderScale = RenderSnap.snapScale(config.scale);
        double offsetX = RenderSnap.snapOffset(config.offsetX, renderScale);
        return (screenX - offsetX) / renderScale;
    }

    private double toOverlayY(double screenY) {
        float renderScale = RenderSnap.snapScale(config.scale);
        double offsetY = RenderSnap.snapOffset(config.offsetY, renderScale);
        return (screenY - offsetY) / renderScale;
    }

    private void toggleSidebar() {
        sidebarOpen = !sidebarOpen;
        if (!sidebarOpen) {
            settingsOpen = false;
        }
    }

    private static void drawBorder(DrawContext context, int x, int y, int width, int height, int color) {
        context.fill(x, y, x + width, y + 1, color);
        context.fill(x, y + height - 1, x + width, y + height, color);
        context.fill(x, y, x + 1, y + height, color);
        context.fill(x + width - 1, y, x + width, y + height, color);
    }

    private static boolean isPointInside(double x, double y, double rectX, double rectY, double width, double height) {
        return x >= rectX && x <= rectX + width && y >= rectY && y <= rectY + height;
    }

    private void rebuildGroups() {
        groups.clear();
        groups.addAll(buildGroups(config));
        groupById.clear();
        for (Group group : groups) {
            groupById.put(group.id, group);
        }
    }

    private static List<Group> buildGroups(OverlayConfig config) {
        Map<String, List<OverlayConfig.KeyDefinition>> grouped = new LinkedHashMap<>();
        for (OverlayConfig.KeyDefinition key : config.keys) {
            grouped.computeIfAbsent(key.group, ignored -> new ArrayList<>()).add(key);
        }

        List<Group> groups = new ArrayList<>();
        for (Map.Entry<String, List<OverlayConfig.KeyDefinition>> entry : grouped.entrySet()) {
            groups.add(new Group(entry.getKey(), displayName(entry.getKey()), entry.getValue()));
        }
        return groups;
    }

    private static String displayName(String id) {
        return switch (id) {
            case "keyboard" -> "Keyboard";
            case "wasd" -> "WASD";
            case "space" -> "SPACE";
            case "shift" -> "SHIFT";
            case "ctrl" -> "CTRL";
            case "numbers" -> "Number Row";
            case "function" -> "Function Keys";
            case "arrows" -> "Arrow Keys";
            case "utility" -> "Utility Keys";
            case "full_keyboard" -> "Full Keyboard";
            case "cps" -> "CPS Counter";
            case "cps_rmb" -> "RMB CPS";
            case "cps_mmb" -> "MMB CPS";
            case "lmb" -> "LMB";
            case "rmb" -> "RMB";
            case "mmb" -> "MMB";
            default -> id.toUpperCase();
        };
    }

    private enum ColorTarget {
        BACKGROUND,
        PRESSED,
        BORDER,
        TEXT
    }

    private enum ColorPickerDragArea {
        NONE,
        WHEEL,
        VALUE
    }

    private enum DragMode {
        NONE,
        MOVE,
        RESIZE,
        LASSO
    }

    private enum ResizeHandle {
        NONE,
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT
    }

    private static final class Group {
        private final String id;
        private final String displayName;
        private final List<OverlayConfig.KeyDefinition> keys;

        private Group(String id, String displayName, List<OverlayConfig.KeyDefinition> keys) {
            this.id = id;
            this.displayName = displayName;
            this.keys = keys;
        }

        private boolean isVisible() {
            for (OverlayConfig.KeyDefinition key : keys) {
                if (key.isVisible()) {
                    return true;
                }
            }
            return false;
        }

        private void setVisible(boolean visible) {
            for (OverlayConfig.KeyDefinition key : keys) {
                key.setVisible(visible);
            }
        }

        private Bounds getBounds() {
            int minX = Integer.MAX_VALUE;
            int minY = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            int maxY = Integer.MIN_VALUE;

            for (OverlayConfig.KeyDefinition key : keys) {
                int x = key.x;
                int y = key.y;
                int right = key.x + key.width;
                int bottom = key.y + key.height;
                minX = Math.min(minX, x);
                minY = Math.min(minY, y);
                maxX = Math.max(maxX, right);
                maxY = Math.max(maxY, bottom);
            }

            if (minX == Integer.MAX_VALUE) {
                return new Bounds(0, 0, 0, 0);
            }

            return new Bounds(minX, minY, maxX - minX, maxY - minY);
        }

        private boolean contains(double x, double y) {
            Bounds bounds = getBounds();
            return x >= bounds.x && x <= bounds.x + bounds.width && y >= bounds.y && y <= bounds.y + bounds.height;
        }

        private void moveBy(int dx, int dy) {
            for (OverlayConfig.KeyDefinition key : keys) {
                key.x += dx;
                key.y += dy;
            }
        }

        private void resizeTo(Bounds start, Bounds target) {
            resizeTo(start, target, null);
        }

        private void resizeTo(Bounds start, Bounds target, List<KeySnapshot> snapshots) {
            if (start.width <= 0 || start.height <= 0) {
                return;
            }
            double scaleX = target.width / (double) start.width;
            double scaleY = target.height / (double) start.height;

            if (snapshots == null) {
                for (OverlayConfig.KeyDefinition key : keys) {
                    int offsetX = key.x - start.x;
                    int offsetY = key.y - start.y;

                    key.x = (int) Math.round(target.x + offsetX * scaleX);
                    key.y = (int) Math.round(target.y + offsetY * scaleY);
                    key.width = Math.max(4, (int) Math.round(key.width * scaleX));
                    key.height = Math.max(4, (int) Math.round(key.height * scaleY));
                }
                return;
            }

            for (KeySnapshot snapshot : snapshots) {
                int offsetX = snapshot.x - start.x;
                int offsetY = snapshot.y - start.y;

                snapshot.key.x = (int) Math.round(target.x + offsetX * scaleX);
                snapshot.key.y = (int) Math.round(target.y + offsetY * scaleY);
                snapshot.key.width = Math.max(4, (int) Math.round(snapshot.width * scaleX));
                snapshot.key.height = Math.max(4, (int) Math.round(snapshot.height * scaleY));
            }
        }

        private boolean containsKey(OverlayConfig.KeyDefinition key) {
            return keys.contains(key);
        }

        private boolean matchesFilter(String filter) {
            if (filter == null || filter.isEmpty()) {
                return true;
            }
            return displayName.toLowerCase().contains(filter);
        }

        private boolean intersects(Bounds other) {
            Bounds bounds = getBounds();
            return bounds.x <= other.x + other.width
                && bounds.x + bounds.width >= other.x
                && bounds.y <= other.y + other.height
                && bounds.y + bounds.height >= other.y;
        }
    }

    private static final class Template {
        private final String id;
        private final String displayName;
        private final List<OverlayConfig.KeyDefinition> keys;

        private Template(String id, String displayName, List<OverlayConfig.KeyDefinition> keys) {
            this.id = id;
            this.displayName = displayName;
            this.keys = keys;
        }

        private static Template keys(String id, String displayName, List<OverlayConfig.KeyDefinition> keys) {
            return new Template(id, displayName, keys);
        }

        private static Template stat(String id, String displayName, OverlayConfig.KeyDefinition key) {
            List<OverlayConfig.KeyDefinition> keys = new ArrayList<>();
            keys.add(key);
            return new Template(id, displayName, keys);
        }

        private boolean matchesFilter(String filter) {
            if (filter == null || filter.isEmpty()) {
                return true;
            }
            return displayName.toLowerCase().contains(filter);
        }

        private List<OverlayConfig.KeyDefinition> createKeys() {
            List<OverlayConfig.KeyDefinition> copies = new ArrayList<>();
            for (OverlayConfig.KeyDefinition key : keys) {
                copies.add(copyKey(key, id));
            }
            return copies;
        }

        private OverlayConfig.KeyDefinition copyKey(OverlayConfig.KeyDefinition key, String groupId) {
            if (key.type == OverlayConfig.InputType.STAT) {
                return new OverlayConfig.KeyDefinition(key.label, key.statId, key.x, key.y, key.width, key.height, groupId);
            }
            return new OverlayConfig.KeyDefinition(key.label, key.type, key.code, key.x, key.y, key.width, key.height, groupId);
        }
    }

    private static final class Bounds {
        private final int x;
        private final int y;
        private final int width;
        private final int height;

        private Bounds(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        private int area() {
            return width * height;
        }
    }

    private static final class ColorState {
        private final int color;
        private final boolean mixed;

        private ColorState(int color, boolean mixed) {
            this.color = color;
            this.mixed = mixed;
        }
    }

    private static final class GuideLine {
        private final boolean horizontal;
        private final int screenPosition;

        private GuideLine(boolean horizontal, int screenPosition) {
            this.horizontal = horizontal;
            this.screenPosition = screenPosition;
        }
    }

    private static final class DistanceLabel {
        private final int x;
        private final int y;
        private final int value;

        private DistanceLabel(int x, int y, int value) {
            this.x = x;
            this.y = y;
            this.value = value;
        }
    }

    private static final class SnapResult {
        private final List<GuideLine> guides = new ArrayList<>();
        private Double bestDeltaScreen;
    }

    private static final class SnapPair {
        private final SnapResult xResult;
        private final SnapResult yResult;
        private final double snapDx;
        private final double snapDy;

        private SnapPair(SnapResult xResult, SnapResult yResult, double snapDx, double snapDy) {
            this.xResult = xResult;
            this.yResult = yResult;
            this.snapDx = snapDx;
            this.snapDy = snapDy;
        }
    }

    private static final class KeySnapshot {
        private final OverlayConfig.KeyDefinition key;
        private final int x;
        private final int y;
        private final int width;
        private final int height;

        private KeySnapshot(OverlayConfig.KeyDefinition key) {
            this.key = key;
            this.x = key.x;
            this.y = key.y;
            this.width = key.width;
            this.height = key.height;
        }

        private static List<KeySnapshot> capture(List<OverlayConfig.KeyDefinition> keys) {
            List<KeySnapshot> snapshots = new ArrayList<>(keys.size());
            for (OverlayConfig.KeyDefinition key : keys) {
                snapshots.add(new KeySnapshot(key));
            }
            return snapshots;
        }
    }
}
