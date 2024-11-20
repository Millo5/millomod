package net.millo.millomod.mixin.render;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.millo.millomod.MilloMod;
import net.millo.millomod.mod.features.FeatureHandler;
import net.millo.millomod.mod.features.impl.global.sidechat.HudWithSideChat;
import net.millo.millomod.mod.features.impl.global.sidechat.SideChat;
import net.millo.millomod.mod.features.impl.global.sidechat.SideChatFeature;
import net.millo.millomod.mod.util.GlobalUtil;
import net.millo.millomod.mod.util.RenderInfo;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.LayeredDrawer;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.Window;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(net.minecraft.client.gui.hud.InGameHud.class)
public abstract class MInGameHUD implements HudWithSideChat {


    @Shadow public abstract TextRenderer getTextRenderer();

    @Shadow private MinecraftClient client;

    @Unique
    private final SideChat sideChat = new SideChat();

    @Inject(method = "render", at = @At("RETURN"))
    private void render(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        Window window = client.getWindow();
        FeatureHandler.renderHUD(new RenderInfo(context,
                tickCounter.getTickDelta(false),
                ci,
                window.getScaledWidth(),
                window.getScaledHeight(),
                tickCounter.getLastDuration(),
                getTextRenderer()));
    }

    @WrapOperation(method = "renderChat", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ChatHud;render(Lnet/minecraft/client/gui/DrawContext;IIIZ)V"))
    private void renderSideChat(ChatHud instance, DrawContext context, int currentTick, int mouseX, int mouseY, boolean focused, Operation<Void> original) {
        original.call(instance, context, currentTick, mouseX, mouseY, focused);
        if (FeatureHandler.getFeature("side_chat").isEnabled()) sideChat.render(context, currentTick, mouseX, mouseY, focused);
    }


    @NotNull
    public SideChat millomod$getSideChat() {
        return sideChat;
    }
}
