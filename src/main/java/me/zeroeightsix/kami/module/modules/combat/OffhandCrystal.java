package me.zeroeightsix.kami.module.modules.combat;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.events.GuiScreenEvent;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.*;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;

import static me.zeroeightsix.kami.KamiMod.MODULE_MANAGER;
import static me.zeroeightsix.kami.module.modules.client.Colours.getItems;

/**
 * @author polymer (main listener switch function xd)
 * @author dominikaaaa (made epic and smooth and cleaned up code <3) (why did i rewrite this 4 times)
 * Created by polymer on 21/02/20
 * Updated by dominikaaaa on 07/03/20
 */
@Module.Info(
        name = "OffhandCrystal",
        category = Module.Category.COMBAT,
        description = "Holds an End Crystal for crystalling!"
)
class OffhandCrystal extends Module {
    private Setting<Double> disableHealth = register(Settings.doubleBuilder("Disable Health").withMinimum(0.0).withValue(4.0).withMaximum(20.0).build());
    private Setting<Boolean> gappleOnly = register(Settings.b("Gapple Only", false));

    int gaps = -1;
    boolean autoTotemWasEnabled = false;
    boolean cancelled = false;
    boolean isGuiOpened = false;
    Item usedItem;
    CrystalAura crystalAura;

    @EventHandler
    private Listener<PacketEvent.Send> sendListener = new Listener<>(e ->{
        if (e.getPacket() instanceof CPacketPlayerTryUseItem) {
            if (cancelled) {
                disableGaps();
                return;
            }
            if (mc.player.getHeldItemMainhand().getItem() instanceof ItemSword || mc.player.getHeldItemMainhand().getItem() instanceof ItemAxe || passItemCheck()) {
                if (MODULE_MANAGER.isModuleEnabled(AutoTotem.class)) {
                    autoTotemWasEnabled = true;
                    MODULE_MANAGER.getModule(AutoTotem.class).disable();
                }
                enableGaps(gaps);
            }
        }
        try {
            /* If you stop holding right click move totem back */
            if (!mc.gameSettings.keyBindUseItem.isKeyDown() && mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL) disableGaps();
                /* In case you didn't stop right clicking but you switched items by scrolling or something */
            else if ((usedItem != mc.player.getHeldItemMainhand().getItem()) && mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL) {
            }
            /* Force disable if under health limit */
            else if (mc.player.getHealth() + mc.player.getAbsorptionAmount() <= disableHealth.getValue()) {
                disableGaps();
            }
            /* Disable if there are crystals in the range of CrystalAura */
            crystalAura = MODULE_MANAGER.getModuleT(CrystalAura.class);
        } catch (NullPointerException ignored) { }
    });

    @Override
    public void onUpdate() {
        if (mc.player == null) return;

        /* If your health doesn't meet the cutoff then set it to true */
        cancelled = mc.player.getHealth() + mc.player.getAbsorptionAmount() <= disableHealth.getValue();
        if (cancelled) { disableGaps(); return; }

        if (mc.player.getHeldItemOffhand().getItem() != Items.END_CRYSTAL) {
            for (int i = 0; i < 45; i++) {
                if (mc.player.inventory.getStackInSlot(i).getItem() == Items.END_CRYSTAL) {
                    gaps = i;
                    break;
                }
            }
        }
    }

    /* If weaponCheck is disabled, check if they're not holding an item you'd want to use normally */
    private boolean passItemCheck() {
        if (gappleOnly.getValue()) return false;
        else {
            Item item = mc.player.getHeldItemMainhand().getItem();
            if (item instanceof ItemBow) return false;
            if (item instanceof ItemSnowball) return false;
            if (item instanceof ItemEgg) return false;
            if (item instanceof ItemPotion) return false;
            if (item instanceof ItemEnderEye) return false;
            if (item instanceof ItemEnderPearl) return false;
            if (item instanceof ItemFood) return false;
            if (item instanceof ItemShield) return false;
            if (item instanceof ItemFlintAndSteel) return false;
            if (item instanceof ItemFishingRod) return false;
            if (item instanceof ItemArmor) return false;
            if (item instanceof ItemExpBottle) return false;
        }
        return true;
    }

    private void disableGaps() {
        if (autoTotemWasEnabled != MODULE_MANAGER.isModuleEnabled(AutoTotem.class)) {
            moveGapsWaitForNoGui();
            MODULE_MANAGER.getModule(AutoTotem.class).enable();
            autoTotemWasEnabled = false;
        }
    }

    private void enableGaps(int slot) {
        if (mc.player.getHeldItemOffhand().getItem() != Items.END_CRYSTAL) {
            mc.playerController.windowClick(0, slot < 9 ? slot + 36 : slot, 0, ClickType.PICKUP, mc.player);
            mc.playerController.windowClick(0, 45, 0, ClickType.PICKUP, mc.player);
        }
    }

    private void moveGapsToInventory(int slot) {
        if (mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL) {
            mc.playerController.windowClick(0, 45, 0, ClickType.PICKUP, mc.player);
            mc.playerController.windowClick(0, slot < 9 ? slot + 36 : slot, 0, ClickType.PICKUP, mc.player);
        }
    }

    private void moveGapsWaitForNoGui() {
        if (isGuiOpened) return;
        moveGapsToInventory(gaps);
    }

    @EventHandler
    public Listener<GuiScreenEvent.Displayed> listener = new Listener<>(event -> isGuiOpened = event.getScreen() != null);

    @Override
    public String getHudInfo() {
        return String.valueOf(getItems(Items.END_CRYSTAL));
    }
}