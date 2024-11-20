package net.millo.millomod.mixin.render.chat;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.millo.millomod.MilloMod;
import net.millo.millomod.mod.features.impl.global.sidechat.HudWithSideChat;
import net.millo.millomod.system.Config;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

@Mixin(ChatScreen.class)
public class MChatScreen extends Screen {

    protected MChatScreen(Text title) {
        super(title);
    }

    @ModifyVariable(method = "<init>", at = @At("HEAD"), argsOnly = true)
    private static String openChat(String originalChatText) {
        boolean enabled = Config.getInstance().get("auto_command.enabled");
        if (!enabled || !originalChatText.isEmpty()) return originalChatText;
        return "@";
    }

    @Inject(method = "render", at = @At("RETURN"))
    public void renderAutoCommand(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        boolean enabled = Config.getInstance().get("auto_command.enabled");
        if (!enabled) return;

        context.fill(0, height - 14, 2, height - 2, Color.PINK.hashCode());
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ChatHud;render(Lnet/minecraft/client/gui/DrawContext;IIIZ)V"))
    public void renderSideChat(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (MilloMod.MC.inGameHud instanceof HudWithSideChat) {
            ((HudWithSideChat) MilloMod.MC.inGameHud).millomod$getSideChat().render(context, MilloMod.MC.inGameHud.getTicks(), mouseX, mouseY, MilloMod.MC.currentScreen == this);
        }
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/hud/ChatHud;getIndicatorAt(DD)Lnet/minecraft/client/gui/hud/MessageIndicator;"))
    public MessageIndicator renderSideChat(ChatHud mainChat, double mouseX, double mouseY, Operation<MessageIndicator> operation) {
        var mainTag = operation.call(mainChat, mouseX, mouseY);
        if (mainTag != null) return mainTag;

        // Get message indicator from side chat
        return ((HudWithSideChat) MilloMod.MC.inGameHud).millomod$getSideChat().getIndicatorAt(mouseX, mouseY);
    }


}
