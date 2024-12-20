package net.millo.millomod.mixin.render;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.millo.millomod.mod.features.FeatureHandler;
import net.millo.millomod.mod.features.impl.coding.SoundPreview;
import net.millo.millomod.mod.features.impl.coding.argumentinsert.ArgumentInsert;
import net.millo.millomod.mod.util.gui.elements.ButtonElement;
import net.millo.millomod.mod.util.gui.elements.TextFieldElement;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(HandledScreen.class)
public abstract class MContainerScreen<T extends ScreenHandler> extends Screen {

    protected MContainerScreen(Text title) {
        super(title);
    }

    @Shadow public abstract T getScreenHandler();

    @Shadow @Final protected T handler;
    @Shadow protected int y;
    @Shadow protected int x;
    @Shadow protected int backgroundWidth;
    @Shadow protected int playerInventoryTitleX;

    @Shadow public abstract boolean keyPressed(int keyCode, int scanCode, int modifiers);

    @Unique
    ArgumentInsert insertFeature;
    @Unique
    SoundPreview soundPreviewFeature;
    @Unique
    private boolean textFieldShown = false;

    @Inject(method = "init", at = @At("RETURN"))
    private void init(CallbackInfo ci) {
        insertFeature = (ArgumentInsert) FeatureHandler.getFeature(ArgumentInsert.class);
        insertFeature.setHandlerPosRef(x, y);

        soundPreviewFeature = (SoundPreview) FeatureHandler.getFeature(SoundPreview.class);

        if (soundPreviewFeature.isEnabled()) {
            initSoundPreview();
        }
    }

    @Unique
    private static TextFieldWidget argumentTextField;

    @Inject(method = "tick", at = @At("HEAD"))
    private void tick(CallbackInfo ci) {
        if (!textFieldShown && insertFeature.showTextField()) {
            textFieldShown = true;
            // add it
            int BOX_WIDTH = 200;
            argumentTextField = new TextFieldElement(MinecraftClient.getInstance().textRenderer,
                    x + handler.slots.get(insertFeature.getSlot()).x + 24,
                    y + handler.slots.get(insertFeature.getSlot()).y,
                    BOX_WIDTH,
                    16,
                    Text.literal(""));
            argumentTextField.setPlaceholder(Text.literal("Enter Value.."));
            argumentTextField.setChangedListener((s) -> insertFeature.setValue(s));
            argumentTextField.setMaxLength(10000);
            addSelectableChild(argumentTextField);

            setFocused(null);
            argumentTextField.setFocused(false);
            setFocused(argumentTextField);
            argumentTextField.setFocused(true);
//            focusOn(argumentTextField);

        }
        if (textFieldShown && !insertFeature.showTextField()) {
            // remove it
            remove(argumentTextField);
            argumentTextField = null;
            textFieldShown = false;
        }
    }


//    @Inject(method = "drawMouseoverTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTooltip(Lnet/minecraft/client/font/TextRenderer;Ljava/util/List;Ljava/util/Optional;II)V"), locals = LocalCapture.CAPTURE_FAILHARD)
//    private void renderTooltip(DrawContext context, int x, int y, CallbackInfo ci, ItemStack stack) {
//        boolean enabled = Config.getInstance().get("preview_skin.enabled");
//        if (!enabled) return;
//
//        Item item = stack.getItem();
//        if (item instanceof BlockItem) {
//            Block block = ((BlockItem) item).getBlock();
//            if (block instanceof AbstractSkullBlock) {
//                previewHeadSkin(context, stack);
//            }
//        }
//    }
    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        insertFeature.mouseClicked(mouseX, mouseY, button, cir);
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        insertFeature.keyPressed(keyCode, scanCode, modifiers, cir);
    }

    @Inject(method = "close", at = @At("HEAD"))
    private void close(CallbackInfo ci) {
        insertFeature.onClose();
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        renderTextField(context, mouseX, mouseY, delta);

        if (insertFeature.getSlot() == -1) return;
        if (insertFeature.getSlot() > this.handler.slots.size()) return;

        Slot slot = this.handler.slots.get(insertFeature.getSlot());

        context.getMatrices().push();
        context.getMatrices().translate(this.x, this.y, 0.0F);
        insertFeature.render(context, mouseX, mouseY, delta, slot, ci);
        context.getMatrices().pop();
    }

    @Unique
    private void renderTextField(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!textFieldShown) return;

        context.getMatrices().push();
        context.getMatrices().translate(0f, 0f, 500f);
        argumentTextField.render(context, mouseX, mouseY, delta);
        context.getMatrices().pop();
    }


    @Unique
    private void initSoundPreview() {
        if (!(handler instanceof GenericContainerScreenHandler chestHandler)) return;

        ButtonElement button = new ButtonElement(x + backgroundWidth + 5, y + 10, 20, 20, Text.literal("P"), (b) -> {
            Inventory inv = chestHandler.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                ItemStack stack = inv.getStack(i);
                if (stack.isEmpty()) continue;
                soundPreviewFeature.previewSound(stack);
            }
        }, textRenderer);

        addDrawableChild(button);

    }


    @Unique
    private void previewHeadSkin(DrawContext context, ItemStack stack) {
//        MinecraftClient mc = MinecraftClient.getInstance();
//
//        NbtCompound nbt = stack.getNbt();
//        if (nbt == null) return;
//
//        GameProfile profile = readGameProfile(nbt.getCompound("SkullOwner"));
//        if (profile == null) return;
//
//        var skin = mc.getSkinProvider().getSkinTextures(profile);
//        var entity = new StaticSkinRenderer(mc.world, skin);
//        var x = Objects.requireNonNull(mc.currentScreen).width / 5;
//        var y = mc.currentScreen.height / 2 + 20;
//
//        InventoryScreen.drawEntity(context, x - 35, y - 50, x + 35, y + 50, 40, 0.0625f, x - 20, y - 20, entity);


//        var entity = new Skin
    }

    @Unique
    private GameProfile readGameProfile(NbtCompound skullOwner) {
        UUID uuid = skullOwner.getUuid("Id");
        String name = skullOwner.getString("Name");

        try {
            GameProfile gameProfile = new GameProfile(uuid, name);
            if (skullOwner.contains("Properties", 10)) {
                NbtCompound properties = skullOwner.getCompound("Properties");

                for (String property : properties.getKeys()) {
                    NbtList list = properties.getList(property, 2);

                    for (int i = 0; i < list.size(); ++i) {
                        NbtCompound tag = list.getCompound(i);
                        String value = tag.getString("Value");
                        if (tag.contains("Signature", 8)) {
                            gameProfile.getProperties().put(property, new Property(property, value, tag.getString("Signature")));
                        } else {
                            gameProfile.getProperties().put(property, new Property(property, value));
                        }
                    }
                }
            }

            return gameProfile;
        } catch (Throwable t) {
            return null;
        }
    }

}














