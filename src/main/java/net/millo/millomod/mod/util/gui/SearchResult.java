package net.millo.millomod.mod.util.gui;

import net.millo.millomod.mod.features.impl.coding.cache.LineElement;

public class SearchResult {

    private final String methodName;
    private final LineElement line;
    private final int segment;

    public SearchResult(String methodName, LineElement lineElement, int segment) {
        this.methodName = methodName;
        this.line = lineElement;
        this.segment = segment;
    }

    public LineElement getLine() {
        return line;
    }

    public int getSegment() {
        return segment;
    }

    @Override
    public String toString() {
        return "SearchResult{" +
                "methodName='" + methodName + '\'' +
                ", line=" + line +
                ", segment=" + segment +
                '}';
    }
}
