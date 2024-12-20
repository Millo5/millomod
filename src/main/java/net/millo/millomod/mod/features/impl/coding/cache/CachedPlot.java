package net.millo.millomod.mod.features.impl.coding.cache;

import net.millo.millomod.mod.hypercube.template.Template;
import net.millo.millomod.mod.hypercube.template.TemplateBlock;
import net.millo.millomod.system.FileManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CachedPlot {

    private final int plotId;
    private final ArrayList<String> methodNames = new ArrayList<>();

    public CachedPlot(int plotId) {
        this(plotId, FileManager.getTemplatesFromPlot(plotId));
    }

    public CachedPlot(int plotId, List<String> methodNames) {
        this.plotId = plotId;
        this.methodNames.addAll(methodNames);
    }

    public ArrayList<String> getMethodNames() {
        return methodNames;
    }

    public String getMethodData(String name) {

        Template template = FileManager.readTemplate(plotId, name);
        if (template == null) return null;

        StringBuilder sb = new StringBuilder();

        int indent = 0;
        for (TemplateBlock block : template.blocks) {
            if (Objects.equals(block.id, "bracket"))
                if (Objects.equals(block.direct, "close")) {
                    indent--;
                    if (indent < 0) indent = 0;
                }

            LineElement lineE = block.toLine();
            lineE.setIndent(indent);
            String line = lineE.getString();
            if (line != null) sb.append(line).append("\n");

            if (Objects.equals(block.id, "bracket") && Objects.equals(block.direct, "open") ||
                    Objects.equals(block.block, "func") || Objects.equals(block.block, "process") ||
                    Objects.equals(block.block, "event") || Objects.equals(block.block, "entity_event")) {
                indent++;
            }
        }

        return sb.toString();

    }



}
