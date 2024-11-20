package net.millo.millomod.mod.util.gui;

import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.tooltip.TooltipState;
import net.minecraft.text.Text;

public interface TooltipHolder {

    TooltipState tooltip = new TooltipState();

    default TooltipState getTooltip() {
        return tooltip;
    }

    default void setTooltip(Tooltip tooltip) {
        this.tooltip.setTooltip(tooltip);
    }

    default void setTooltip(Text text) {
        this.tooltip.setTooltip(Tooltip.of(text));
    }

}
