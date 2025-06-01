package net.millo.millomod.mod.features.impl.coding.cache;

import net.millo.millomod.SoundHandler;
import net.millo.millomod.mod.features.impl.util.Tracker;
import net.millo.millomod.mod.hypercube.template.Template;
import net.millo.millomod.mod.hypercube.template.TemplateBlock;
import net.millo.millomod.mod.util.MathUtil;
import net.millo.millomod.mod.util.gui.GUI;
import net.millo.millomod.mod.util.gui.GUIStyles;
import net.millo.millomod.mod.util.gui.elements.*;
import net.millo.millomod.system.FileManager;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.text.Text;

import java.util.*;
import java.util.regex.Pattern;

public class CacheGUI extends GUI {
    public static CacheGUI lastOpenedGUI;
    private static Template template;
    private boolean hierarchyOpen = true;
    private double hierarchyX = paddingX;

    private ScrollableElement lineContainer;
    private ButtonElement hierarchyButton;
    private TextElement plotIdText;
    private TextFieldElement forcePlotIdField;
    private ScrollableElement templates;
    private TextFieldElement searchBar;

    private ArrayList<String> methodNames = new ArrayList<>();
    private ArrayList<LineElement> lines;

    private int plotId;
    private static int forcedPlotId = -1;
    int toolbarSize = 20;


    // TODO: Search for actions
    // TODO: ^ Search for usages of method
    // TODO: Folders.
    //      -   Make this a setting in the cache menu, enabled will split on .'s

    // TODO: Fix scrollbar when searching
    // TODO: Add a "help" segment (how to tp, and the keybinding)
    // TODO: In the context menu on methods, add a "get string" to get the method name
    // TODO: In the context menu on methods, add a "get template" to get the template item

    // TODO: (SOMEHOW) Allow the opening of different PLOTS in the cache menu

    public CacheGUI() {
        super(Text.of("Cache"));
        lastOpenedGUI = this;
    }

    private boolean pendingTemplateListUpdate = false;
    public void loadTemplate(Template template){
        if (template == null) return;
        if (CacheGUI.template != null) {
//            if (template.getFileName().equals(CacheGUI.template.getFileName())) return; // uncomment maybe idk never?
            historyStack.push(CacheGUI.template.getFileName());
            futureStack.clear();
        }
        loadTemplateLines(template);
        pendingTemplateListUpdate = true;   // pending to prevent concurrency error
    }

    private void loadTemplateLines(Template template) {
        boolean reload = CacheGUI.template == null;
        CacheGUI.template = template;
        if (reload) {
            clearChildren();
            init();
            return;
        }
        addLinesFromTemplate();
        lineContainer.setFade(getFade());
    }

    public TextRenderer getTextRenderer() {
        return textRenderer;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        double desiredHierarchyX = hierarchyOpen ? width / 5d : paddingX;
        hierarchyX = MathUtil.clampLerp(hierarchyX, desiredHierarchyX, delta);

        if (lineContainer != null) {
            lineContainer.setX((int) hierarchyX);
            lineContainer.setWidth(backgroundWidth - (int) hierarchyX + paddingX);
        }

        hierarchyButton.setX((int) hierarchyX);
        hierarchyButton.setRealX((int) hierarchyX);

        plotIdText.visible = hierarchyOpen;
        plotIdText.setWidth((int) (hierarchyX - paddingX));


        templates.setWidth((int) (hierarchyX - paddingX));
        templates.getDrawables().forEach(i -> {
            if (i instanceof ButtonElement) {
                ((ButtonElement) i).setWidth((int) (hierarchyX - paddingX) - 8);
            }
        });

        searchBar.setVisible(hierarchyOpen);
        searchBar.setWidth((int) (hierarchyX - paddingX - 8));

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void tick() {
        if (pendingTemplateListUpdate) {
            pendingTemplateListUpdate = false;
            updateTemplateList();
        }
    }

    protected void init() {
        super.init();
        plotId = Tracker.getPlot().getPlotId();
        if (forcedPlotId > 0) plotId = forcedPlotId;

        searchMenuOpen = false;

        // Toolbar
        hierarchyButton = new ButtonElement(
                paddingX, paddingY, toolbarSize, toolbarSize, Text.of("<"),
                (button) -> {
                    SoundHandler.playSound("");
                    if (hierarchyOpen) button.setText(Text.of(">"));
                    else button.setText(Text.of("<"));
                    hierarchyOpen = !hierarchyOpen;
                },
                textRenderer);
        addDrawableChild(hierarchyButton);


        // template exists
        if (template == null) {
            addEmpty();
        }
        plotIdText = new TextElement(paddingX, paddingY, 0, 20,
                Text.literal("Plot: "+plotId).setStyle(GUIStyles.HEADER.getStyle()),
                textRenderer);
        addDrawableChild(plotIdText);

        // list of plot templates (hierarchy)
        searchBar = new TextFieldElement(textRenderer, paddingX + 4, paddingY + toolbarSize, 50, 16, Text.of(""));
        searchBar.setPlaceholder(Text.literal("Search...").setStyle(GUIStyles.COMMENT.getStyle()));
        searchBar.setChangedListener(s -> updateTemplateList());
        addDrawableChild(searchBar);

        templates = new ScrollableElement(paddingX, paddingY + toolbarSize + 16, 50, backgroundHeight - toolbarSize - 20, Text.literal(""));
        updateMethodNamesList();
        updateTemplateList();

        addDrawableChild(templates);

        forcePlotIdField = new TextFieldElement(textRenderer, 0, 0, 100, 16, Text.of(""));
        if (forcedPlotId > 0) forcePlotIdField.setText(String.valueOf(forcedPlotId));

        forcePlotIdField.setPlaceholder(Text.literal("Force Plot ID").setStyle(GUIStyles.COMMENT.getStyle()));
        forcePlotIdField.setChangedListener(s -> {
            if (s.isEmpty()) {
                forcedPlotId = -1;
                return;
            }
            if (!FileManager.isPlotCached(s)) return;
            int id;
            try {
                id = Integer.parseInt(s);
            } catch (NumberFormatException e) {
                return;
            }
            if (forcedPlotId == id) return;

            forcedPlotId = id;

            // Forceful reload
            template = null;
            loadTemplateLines(null);

        });

        addDrawableChild(forcePlotIdField);

        if (template == null) {
            return;
        }

        addLinesFromTemplate();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 3) historyBack();
        if (button == 4) historyForward();
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void historyBack() {
        Template template = null;
        while (template == null && !historyStack.isEmpty()) {
            template = FileManager.readTemplate(plotId, historyStack.pop());
        }
        if (template == null) return;
        if (CacheGUI.template != null) futureStack.push(CacheGUI.template.getFileName());
        SoundHandler.playSound("minecraft:block.azalea_leaves.hit", 2, 2);
        loadTemplateLines(template);
    }
    private void historyForward() {
        Template template = null;
        while (template == null && !futureStack.isEmpty()) {
            template = FileManager.readTemplate(plotId, futureStack.pop());
        }
        if (template == null) return;
        if (CacheGUI.template != null) historyStack.push(CacheGUI.template.getFileName());
        SoundHandler.playSound("minecraft:block.azalea_leaves.hit", 2, 2);
        loadTemplateLines(template);
    }

    private static final Stack<String> historyStack = new Stack<>();
    private static final Stack<String> futureStack = new Stack<>();

    private void addLinesFromTemplate() {

        if (lineContainer != null) remove(lineContainer);
        lineContainer = new ScrollableElement(paddingX, paddingY + toolbarSize, backgroundWidth, backgroundHeight - toolbarSize, Text.literal(""));

        if (template == null) return;

        lines = new ArrayList<>();

        int worldProgress = 0; // keeps track of how many in world blocks have gone by
        int lineNum = 0;
        int indentation = 0;
        for (TemplateBlock i : template.blocks) {
            lineNum++;
            if (Objects.equals(i.id, "bracket"))
                if (Objects.equals(i.direct, "close")) {
                    indentation--;
                    if (indentation < 0) indentation = 0;
                } else worldProgress -= 2;


            LineElement line = i.toLine();
            line.setIndent(indentation);

            if (template.startPos == null) line.setLineNum(lineNum,  template.getMethodName().charAt(0) + " " + template.getName(), worldProgress);
            else line.setLineNum(lineNum, template.startPos.add(-1, 0, worldProgress));

            line.init(backgroundWidth, 12);
            if (searchMenuOpen) {
                line.highlight(searchMenu.getSearchText());
            }

            lineContainer.addDrawableChild(line);
            lines.add(line);

            if (Objects.equals(i.id, "bracket") && Objects.equals(i.direct, "open") ||
                    Objects.equals(i.block, "func") || Objects.equals(i.block, "process") ||
                    Objects.equals(i.block, "event") || Objects.equals(i.block, "entity_event")) {
                indentation++;
            }
            worldProgress += 2;
        }

        addDrawableChild(lineContainer);
    }


    private void updateMethodNamesList() {
        methodNames = (ArrayList<String>) FileManager.getTemplatesFromPlot(plotId);
    }
    private void updateTemplateList() {
        // Updates the hierarchy

        // Clear all existing templates shown in hierarchy
        templates.clear();

        // Initialize map of folders
        HashMap<String, MethodFolder> rootFolders = new HashMap<>();
        ArrayList<ButtonElement> rootMethods = new ArrayList<>();

        var pattern = Pattern.compile(".+(?=\\.\\w)");

        // Extract search filter
        String[] match = searchBar.getText().trim().toLowerCase().split(" ");
        for (String methodName : methodNames) {
            // Exclude methods that do not fit filter
            String parsedFileName = Template.reverseFileName(methodName);
            if (!Arrays.stream(match).allMatch(i -> parsedFileName.toLowerCase().contains(i)) && !searchBar.getText().trim().isEmpty()) {
                continue;
            }

            // Create the element button
            ButtonElement elementToAdd = new MethodElement(16, plotId, methodName, (button) -> {
                SoundHandler.playClick();
                Template template = FileManager.readTemplate(plotId, methodName);
                loadTemplateLines(template);
            }, textRenderer);
            elementToAdd.setFade(getFade());


            // Find folder tree for current method
            var matcher = pattern.matcher(parsedFileName.replaceAll("\\.(event|func|process|entity_event)$", ""));
            if (matcher.find()) {
                String folderNamespace = matcher.group();
                List<String> folderNames = new ArrayList<>(List.of(folderNamespace.split("\\.")));
//                Collections.reverse(folderNames);

                String rootFolderName = folderNames.get(0);
                MethodFolder folder = null;
                if (rootFolders.containsKey(rootFolderName)) {
                    folder = rootFolders.get(folderNames.get(0));
                } else {
                    folder = new MethodFolder(16, rootFolderName, (button) -> {}, textRenderer);
                    rootFolders.put(rootFolderName, folder);
                }


                // Recursively find the rest of the path
                folderNames.remove(0);
                for (String folderName : folderNames) {
                    folder = folder.subFolder(folderName);
                }

                folder.add(elementToAdd);


//                MethodFolder folder = null;
//                for (String folderName : folderNames) {
//                    if (rootFolders.containsKey(folderName)) {
//                        folder = rootFolders.get(folderName);
//                    } else {
//                        folder = new MethodFolder(16, folderName, (button) -> {}, textRenderer);
//                        if (!searchBar.getText().isEmpty()) folder.open();
//                        rootFolders.put(folderName, folder);
//                    }
//                    folder.add(elementToAdd);
//                    elementToAdd = folder;
//                }
            } else {
                rootMethods.add(elementToAdd);
            }

            // Append the method as a button to the hierarchy

//            if (elementToAdd instanceof MethodElement) {
//                elementToAdd.setFade(getFade());
//                templates.addDrawableChild(elementToAdd);
//            }
        }

        rootFolders.forEach((folderName, folder) -> folder.addTo(templates, getFade()));
        rootMethods.forEach(templates::addDrawableChild);

    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {

        if (keyCode == 70) {
            if (modifiers == 3) {
                searchBar.setEditable(true);
                searchBar.setSelectionStart(0);
                searchBar.setSelectionEnd(searchBar.getText().length());
                this.setFocused(searchBar);
            } else if (modifiers == 2) {
                openSearchMenu();
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public void findMethod(String methodName) {
        methodName = methodName.replaceAll("(?<=\\.)(start|call)_(?=(func|process))", "");
        Template template = FileManager.readTemplate(plotId, methodName);
        if (template != null) loadTemplate(template);
    }

    public void reload() {
        updateMethodNamesList();
        updateTemplateList();
        remove(lineContainer);
        lineContainer = null;
        template = null;
        addEmpty();
    }

    private void addEmpty() {
        addDrawableChild(new TextElement(paddingX, paddingY + toolbarSize, backgroundWidth, backgroundHeight - toolbarSize,
                Text.literal("Empty").setStyle(GUIStyles.COMMENT.getStyle()),
                textRenderer));
    }


    public int getBackgroundHeight() {
        return backgroundHeight;
    }

    CacheSearchMenu searchMenu;
    private boolean searchMenuOpen = false;
    private void openSearchMenu() {
        if (searchMenuOpen) {
            searchMenu.focus();
            return;
        }
        searchMenuOpen = true;

        searchMenu = new CacheSearchMenu(this, width - paddingX, paddingY);
        addDrawableChild(searchMenu);

        searchMenu.focus();
    }

    @Override
    public <T extends Element & Drawable & Selectable> T addDrawableChild(T drawableElement) {
        return super.addDrawableChild(drawableElement);
    }

    @Override
    public <T extends Element & Selectable> T addSelectableChild(T child) {
        return super.addSelectableChild(child);
    }

    public ArrayList<LineElement> getLines() {
        return lines;
    }

    public String getSearchText() {
        if (!searchMenuOpen) return "";
        return searchMenu.getSearchText();
    }

    public ArrayList<String> getAllMethods() {
        return methodNames;
    }

    public int getPlotId() {
        return plotId;
    }
}
