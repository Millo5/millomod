package net.millo.millomod.mod.features.impl.coding.cache;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.impl.client.keybinding.KeyBindingRegistryImpl;
import net.millo.millomod.MilloMod;
import net.millo.millomod.mod.features.impl.util.Tracker;
import net.millo.millomod.mod.features.impl.util.teleport.TeleportHandler;
import net.millo.millomod.mod.util.GlobalUtil;
import net.millo.millomod.mod.util.ItemUtil;
import net.millo.millomod.system.Config;
import net.millo.millomod.mod.features.Feature;
import net.millo.millomod.mod.features.Keybound;
import net.millo.millomod.mod.features.HandlePacket;
import net.millo.millomod.mod.features.impl.util.NotificationTray;
import net.millo.millomod.mod.hypercube.template.Template;
import net.millo.millomod.mod.util.gui.GUIStyles;
import net.millo.millomod.system.FileManager;
import net.millo.millomod.system.PlayerUtil;
import net.minecraft.block.Block;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Pattern;

public class PlotCaching extends Feature implements Keybound {

    Template cachedTemplate;
    private KeyBinding key;
    private int cacheNextItem = 0;
    @Nullable CacheGUI cacheGUI;
    private Vec3d clickedLoc;

    @Override
    public String getKey() {
        return "plot_caching";
    }


    @HandlePacket
    public boolean onMessage(GameMessageS2CPacket message) { // TODO: fix this
        String content = message.content().getString();
        if (cacheNextItem > 0) {
             if (content.equals("Error: Unable to create code template! Exceeded the code data size limit.")) {
                cacheNextItem = 0;
                if (doingFullScan) {
                    scanPlotStep_OLD = ScanPlotStep.TELEPORT;
                    return true;
                }
            }
            return content.trim().equals("Note: You can view your past 5 created templates with /templatehistory!");
        }
        return false;
    }

    @HandlePacket
    public boolean slotUpdate(ScreenHandlerSlotUpdateS2CPacket slot) {
        if (cacheNextItem == 0) return false;
        cacheNextItem = 0;

        String codeTemplateData = ItemUtil.getPBVString(slot.getStack(), "hypercube:codetemplatedata");
        if (codeTemplateData == null) return false;

        cachedTemplate = Template.parseItem(codeTemplateData);
        cachedTemplate.startPos = clickedLoc;

        FileManager.writeTemplate(cachedTemplate);

        NotificationTray.pushNotification(
                Text.of("Cached"),
                Text.literal(cachedTemplate.getName()).setStyle(GUIStyles.NAME.getStyle())
        );

        if (MilloMod.MC.getNetworkHandler() != null)
            MilloMod.MC.getNetworkHandler().sendPacket(new CreativeInventoryActionC2SPacket(slot.getSlot(), ItemStack.EMPTY));
        if (cacheGUI != null) cacheGUI.loadTemplate(cachedTemplate);

        if (doingFullScan) {
            scanPlotStep_OLD = ScanPlotStep.TELEPORT;
        }

        return true;
    }

    @Override
    public void loadKeybinds() {
        key = KeyBindingRegistryImpl.registerKeyBinding(
                new KeyBinding(
                        "key.millo.display_cache",
                        InputUtil.Type.KEYSYM,
                        -1,
                        "key.category.millo"
                )
        );
    }

    @Override
    public void triggerKeybind(Config config) {}


    int cacheTries = 0;
    @Override
    public void onTick() {
        if (GlobalUtil.isKeyPressed(key)) {
            trigger();
        }

        if (cacheNextItem > 0) {
            cacheNextItem--;
            if (cacheNextItem == 0) {
                if (doingFullScan) {
                    cacheTries ++;
                    if (cacheTries > 3) {
                        scanPlotStep_OLD = ScanPlotStep.TELEPORT;
                        NotificationTray.pushNotification(Text.of("Failed to cache"),
                                Text.literal(clickedLoc.toString()).setStyle(GUIStyles.SCARY.getStyle()));
                    } else cacheMethodFromPosition(BlockPos.ofFloored(clickedLoc));
                } else if (cacheGUI != null) cacheGUI.loadTemplate(cachedTemplate);
            }
        }

        if (fullScan) {
            scanTick();
        }

        if (doingFullScan) {
            scanTickOld();
        }
    }



    public void trigger() {
        if (MilloMod.MC.currentScreen != null || doingFullScan) return;

        cacheGUI = new CacheGUI();
        cacheGUI.open();

        MinecraftClient mc = MilloMod.MC;
        ClientPlayerEntity player = mc.player;

        if (mc.getNetworkHandler() == null || player == null || mc.world == null) return;

        BlockHitResult rayHit = mc.world.raycast(new RaycastContext(player.getEyePos(),
                player.getEyePos().add(player.getRotationVector().multiply(5d)),
                RaycastContext.ShapeType.OUTLINE,
                RaycastContext.FluidHandling.NONE,
                ShapeContext.absent()
        ));

        if (rayHit.getType() == HitResult.Type.MISS) return;

        var blockPos = rayHit.getBlockPos();
        Block block = mc.world.getBlockState(blockPos).getBlock();
        if (!Pattern.compile("minecraft:(diamond|emerald|lapis|gold)_block").matcher(Registries.BLOCK.getId(block).toString()).matches()){
            blockPos = blockPos.add(1, 0, 0);
        }

        cacheMethodFromPosition(blockPos);
    }

    public void cacheMethodFromPosition(BlockPos position) {
        var interact = MilloMod.MC.interactionManager;
        var net = MilloMod.MC.getNetworkHandler();
        var player = MilloMod.MC.player;

        if (net == null || interact == null || player == null) return;

        clickedLoc = position.toCenterPos();
        cacheNextItem = 40;

        boolean sneaking = player.isSneaking();

        ItemStack item = player.getMainHandStack();

//        player.getInventory().setStack(player.getInventory().selectedSlot, ItemStack.EMPTY);
//        net.sendPacket(new CreativeInventoryActionC2SPacket(player.getInventory().selectedSlot, ItemStack.EMPTY));
//        if (!sneaking) net.sendPacket(new ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
//        interact.interactBlock(player, Hand.MAIN_HAND, new BlockHitResult(
//                clickedLoc, Direction.UP, position, false
//        ));
//        if (!sneaking) net.sendPacket(new ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
//        net.sendPacket(new CreativeInventoryActionC2SPacket(player.getInventory().selectedSlot, item));
//        player.getInventory().setStack(player.getInventory().selectedSlot, item);

        player.getInventory().setStack(player.getInventory().selectedSlot, ItemStack.EMPTY);
        PlayerUtil.sendHandItem(ItemStack.EMPTY);
        if (!sneaking) PlayerUtil.sendSneak(true);
        PlayerUtil.rightClickPos(position);
        if (!sneaking) PlayerUtil.sendSneak(false);
        PlayerUtil.sendHandItem(item);
        player.getInventory().setStack(player.getInventory().selectedSlot, item);
    }




    private Stack<BlockPos> scanStack_OLD;
    private BlockPos scanStepTarget;
    private boolean doingFullScan = false;
    private ScanPlotStep scanPlotStep_OLD = ScanPlotStep.NONE;
    private int scanPlotTicksTried_OLD = 0;


    public boolean scanPlotOld(String size) {
        if (doingFullScan) {
            doingFullScan = false;
            return false;
        }
        doingFullScan = true;

        ArrayList<BlockPos> signs = Tracker.getPlot().scanForMethods(size);

        scanStack_OLD = new Stack<>();
        scanStack_OLD.addAll(signs);

        scanPlotStep_OLD = ScanPlotStep.TELEPORT;
        return true;
    }


    private HashMap<String, Template> methodStack;
    private Iterator<Map.Entry<String, Template>> methodStackIterator;
    private Map.Entry<String, Template> methodStackEntry;
    private boolean fullScan = false;
    private ScanPlotStep scanStep;
    private int waitForShulkers = 0;
    public boolean scanPlot() {
        if (fullScan) {
            scanStep = ScanPlotStep.NONE;
            fullScan = false;
            return false;
        }
        fullScan = true;

        waitForShulkers = 0;
        scanStep = ScanPlotStep.CACHE;
        methodStack = new HashMap<>();
        PlayerUtil.sendCommand("p totemplate");

        return true;
    }

    @HandlePacket
    public boolean slotUpdateScanPlot(ScreenHandlerSlotUpdateS2CPacket slot) {
        if (!fullScan) return false;
        if (scanStep != ScanPlotStep.CACHE) return false;

        ComponentMap shulkerComponents = slot.getStack().getComponents();
        if (shulkerComponents == null) return false;

        ContainerComponent containerComponent = shulkerComponents.get(DataComponentTypes.CONTAINER);
        if (containerComponent == null) return false;

        NotificationTray.pushNotification(Text.of("Scanning"), Text.literal("Shulker Box").setStyle(GUIStyles.NAME.getStyle()));

        for (ItemStack itemStack : containerComponent.iterateNonEmpty()) {
            var bukkitValues = ItemUtil.getPBV(itemStack);
            if (bukkitValues == null || !bukkitValues.contains("hypercube:codetemplatedata", NbtElement.STRING_TYPE))
                continue;

            String codeTemplateData = bukkitValues.getString("hypercube:codetemplatedata");

            Template template = Template.parseItem(codeTemplateData);
            FileManager.writeTemplate(template);

            methodStack.put(template.getName(), template);
        }

        waitForShulkers = 1;

        return false;
    }

    private void scanTick() {
        if (!fullScan || MilloMod.MC.player == null) return;

        if (scanStep == ScanPlotStep.CACHE && waitForShulkers >= 1) {
            waitForShulkers++;
            if (waitForShulkers > 10) {
                waitForShulkers = 0;
                NotificationTray.pushNotification(Text.of("Caching"), Text.of(methodStack.size() + " methods"));

                methodStackIterator = methodStack.entrySet().iterator();
                scanStep = ScanPlotStep.TELEPORT;

                fullScan = false; // end of scan
            }
            return;
        }

        if (methodStackIterator == null) return;

        if (!methodStackIterator.hasNext() && scanStep == ScanPlotStep.TELEPORT) {
            fullScan = false;
            scanStep = ScanPlotStep.NONE;
            NotificationTray.pushNotification(Text.of("Finished full scan"));
            return;
        }

        switch (scanStep) {
            default -> {}
            case TELEPORT -> {
                scanStep = ScanPlotStep.WAIT_FOR_TP;
                methodStackEntry = methodStackIterator.next();
                String methodName = methodStackEntry.getKey();
                String methodType = methodStackEntry.getValue().getMethodName().charAt(0) + "";

                TeleportHandler.teleportToMethod(methodType + " " + methodName, () -> {
                    scanStep = ScanPlotStep.WAIT_FOR_CACHE;
                });

            }
            case WAIT_FOR_CACHE -> {
                scanStep = ScanPlotStep.TELEPORT;
                cacheTries = 0;

                Template template = methodStackEntry.getValue();
                template.startPos = TeleportHandler.getLastTeleportPosition().add(0, -1.5, 0);

                NotificationTray.pushNotification(Text.of("Caching"), Text.of(template.getName()));

                FileManager.writeTemplate(template);
            }
        }

    }




    int teleportDelay = 0;
    private void scanTickOld() {
        if (!doingFullScan || MilloMod.MC.player == null) return;

        if (scanStack_OLD.isEmpty() && scanPlotStep_OLD == ScanPlotStep.TELEPORT) {
            doingFullScan = false;
            MilloMod.MC.player.sendMessage(Text.of("Finished full scan"), false);
            return;
        }
        scanPlotTicksTried_OLD++;
        if (scanPlotTicksTried_OLD > 10 && scanStepTarget != null) {
            scanPlotStep_OLD = ScanPlotStep.CACHE;
            if (MilloMod.MC.player.getPos().distanceTo(scanStepTarget.toCenterPos()) > 4) {
                scanStack_OLD.add(scanStepTarget);
                scanPlotStep_OLD = ScanPlotStep.TELEPORT;
            }
        }


        if (scanPlotStep_OLD == ScanPlotStep.TELEPORT) {
            teleportDelay ++;
            if (teleportDelay < 60) {
                return;
            }
        }
        teleportDelay = 0;

        switch (scanPlotStep_OLD) {
            default -> {}
            case TELEPORT -> {
                scanPlotTicksTried_OLD = 0;
                scanStepTarget = scanStack_OLD.pop();
                scanPlotStep_OLD = ScanPlotStep.WAIT_FOR_TP;
                TeleportHandler.teleportTo(scanStepTarget.toCenterPos().add(0, 1.5, 0), () -> {
                    if (!TeleportHandler.lastSuccess) {
                        // Out of bounds, skip
                        scanPlotStep_OLD = ScanPlotStep.TELEPORT;
                        return;
                    }
                    scanPlotStep_OLD = ScanPlotStep.CACHE;
                });
            }
            case CACHE -> {
                scanPlotTicksTried_OLD = 0;
                scanPlotStep_OLD = ScanPlotStep.WAIT_FOR_CACHE;
                cacheTries = 0;
                cacheMethodFromPosition(scanStepTarget);
            }
        }

    }

    public ArrayList<Integer> getCachedPlots() {
        return FileManager.getCachedPlots();
    }

    private enum ScanPlotStep { NONE, TELEPORT, WAIT_FOR_TP, CACHE, WAIT_FOR_CACHE }


}
