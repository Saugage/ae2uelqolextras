package com.ae2qolextras.part;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.config.RedstoneMode;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.config.YesNo;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartModel;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.IConfigManager;
import appeng.core.settings.TickRates;
import appeng.core.sync.GuiBridge;
import appeng.helpers.MultiCraftingTracker;
import appeng.me.GridAccessException;
import appeng.me.helpers.MachineSource;
import appeng.parts.automation.PartFormationPlane;
import appeng.tile.inventory.AppEngInternalAEInventory;
import appeng.util.Platform;
import appeng.util.prioritylist.FuzzyPriorityList;
import appeng.util.prioritylist.IPartitionList;
import appeng.util.prioritylist.PrecisePriorityList;
import com.ae2qolextras.AE2QOLExtras;
import com.ae2qolextras.GuiHandler;
import com.ae2qolextras.item.ModItems;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;


import java.util.Collections;
import java.util.List;

/**
 * A formation plane that pulls items out of network storage like an export bus, instead of acting as storage.
 */
public class PartActiveFormationPlane extends PartFormationPlane implements IGridTickable, ICraftingRequester {

    private final MultiCraftingTracker craftingTracker = new MultiCraftingTracker(this, 63);
    private final IActionSource mySrc = new MachineSource(this);
    private boolean lastRedstone = false;

    public PartActiveFormationPlane(final ItemStack is) {
        super(is);
        this.getConfigManager().registerSetting(Settings.REDSTONE_CONTROLLED, RedstoneMode.IGNORE);
        this.getConfigManager().registerSetting(Settings.CRAFT_ONLY, YesNo.NO);
    }

    @Override
    public List<IMEInventoryHandler> getCellArray(final IStorageChannel channel) {
        return Collections.emptyList();
    }

    @Override
    public IPartModel getStaticModels() {
        return ActivePlaneModels.getModel(this.getConnections(), this.isPowered(), this.isActive());
    }

    @Override
    public ItemStack getItemStackRepresentation() {
        return new ItemStack(ModItems.activeFormationPlane);
    }

    @Override
    public GuiBridge getGuiBridge() {
        return null;
    }

    @Override
    public boolean onPartActivate(final EntityPlayer player, final EnumHand hand, final Vec3d pos) {
        if (Platform.isServer()) {
            final BlockPos tilePos = this.getHost().getTile().getPos();
            player.openGui(AE2QOLExtras.instance, GuiHandler.partGuiId(GuiHandler.ACTIVE_FORMATION_PLANE, this.getSide()),
                    this.getHost().getTile().getWorld(), tilePos.getX(), tilePos.getY(), tilePos.getZ());
        }
        return true;
    }

    @Override
    public RedstoneMode getRSMode() {
        return (RedstoneMode) this.getConfigManager().getSetting(Settings.REDSTONE_CONTROLLED);
    }

    @Override
    public void upgradesChanged() {
        super.upgradesChanged();
        this.updateState();
    }

    @Override
    public void updateSetting(final IConfigManager manager, final Enum settingName, final Enum newValue) {
        super.updateSetting(manager, settingName, newValue);
        this.updateState();
    }

    @Override
    public void onNeighborChanged(final IBlockAccess w, final BlockPos pos, final BlockPos neighbor) {
        super.onNeighborChanged(w, pos, neighbor);
        this.updateState();
        if (this.lastRedstone != this.getHost().hasRedstone(this.getSide())) {
            this.lastRedstone = !this.lastRedstone;
            if (this.lastRedstone && this.getRSMode() == RedstoneMode.SIGNAL_PULSE) {
                this.doWork();
            }
        }
    }

    private void updateState() {
        try {
            if (!this.isSleeping()) {
                this.getProxy().getTick().wakeDevice(this.getProxy().getNode());
            } else {
                this.getProxy().getTick().sleepDevice(this.getProxy().getNode());
            }
        } catch (final GridAccessException e) {
            // :P
        }
    }

    @Override
    public void readFromNBT(final NBTTagCompound data) {
        super.readFromNBT(data);
        this.craftingTracker.readFromNBT(data);
    }

    @Override
    public void writeToNBT(final NBTTagCompound data) {
        super.writeToNBT(data);
        this.craftingTracker.writeToNBT(data);
    }

    @Override
    public TickingRequest getTickingRequest(final IGridNode node) {
        return new TickingRequest(TickRates.ExportBus.getMin(), TickRates.ExportBus.getMax(), this.isSleeping(), false);
    }

    @Override
    public TickRateModulation tickingRequest(final IGridNode node, final int ticksSinceLastCall) {
        return this.doWork();
    }

    private TickRateModulation doWork() {
        if (!this.getProxy().isActive() || !this.canWork()) {
            return TickRateModulation.IDLE;
        }

        try {
            final IMEMonitor<IAEItemStack> inv = this.getProxy().getStorage().getInventory(this.getChannel());
            final IEnergyGrid energy = this.getProxy().getEnergy();
            final ICraftingGrid cg = this.getProxy().getCrafting();
            final AppEngInternalAEInventory config = (AppEngInternalAEInventory) this.getInventoryByName("config");
            final FuzzyMode fzMode = (FuzzyMode) this.getConfigManager().getSetting(Settings.FUZZY_MODE);
            final boolean fuzzy = this.getInstalledUpgrades(Upgrades.FUZZY) > 0;
            boolean didSomething = false;

            if (this.getInstalledUpgrades(Upgrades.INVERTER) > 0) {
                final IPartitionList<IAEItemStack> filter = this.createFilter(config, fuzzy, fzMode);
                for (final IAEItemStack stored : ImmutableList.copyOf(inv.getStorageList())) {
                    if (!filter.isListed(stored) && this.pushIntoWorld(energy, inv, stored)) {
                        return TickRateModulation.FASTER;
                    }
                }
                return TickRateModulation.SLOWER;
            }

            for (int x = 0; x < this.availableSlots(); x++) {
                final IAEItemStack ais = config.getAEStackInSlot(x);
                if (ais == null) {
                    continue;
                }

                if (this.craftOnly()) {
                    didSomething = this.requestCrafting(x, ais, cg) || didSomething;
                    continue;
                }

                if (fuzzy) {
                    for (final IAEItemStack o : ImmutableList.copyOf(inv.getStorageList().findFuzzy(ais, fzMode))) {
                        if (o.getStackSize() > 0 && this.pushIntoWorld(energy, inv, o)) {
                            return TickRateModulation.FASTER;
                        }
                    }
                } else {
                    final IAEItemStack o = inv.getStorageList().findPrecise(ais);
                    if (o != null && o.getStackSize() > 0 && this.pushIntoWorld(energy, inv, o)) {
                        return TickRateModulation.FASTER;
                    }
                }

                if (this.isCraftingEnabled()) {
                    didSomething = this.requestCrafting(x, ais, cg) || didSomething;
                }
            }

            return didSomething ? TickRateModulation.FASTER : TickRateModulation.SLOWER;
        } catch (final GridAccessException e) {
            return TickRateModulation.IDLE;
        }
    }

    private boolean canWork() {
        if (this.blocked) {
            return false;
        }
        final TileEntity self = this.getHost().getTile();
        final World world = self.getWorld();
        final BlockPos target = self.getPos().offset(this.getSide().getFacing());
        return world != null && world.getChunkProvider().getLoadedChunk(target.getX() >> 4, target.getZ() >> 4) != null;
    }

    private boolean pushIntoWorld(final IEnergyGrid energy, final IMEMonitor<IAEItemStack> inv, final IAEItemStack stored) {
        final long wanted = Math.min(stored.getStackSize(), this.itemsPerOperation());
        final IAEItemStack request = stored.copy();
        request.setStackSize(wanted);

        final long accepted = wanted - stackSize(this.injectItems(request.copy(), Actionable.SIMULATE, this.mySrc));
        if (accepted <= 0) {
            return false;
        }

        request.setStackSize(accepted);
        final IAEItemStack extracted = Platform.poweredExtraction(energy, inv, request, this.mySrc);
        if (extracted == null) {
            return false;
        }

        final IAEItemStack leftover = this.injectItems(extracted.copy(), Actionable.MODULATE, this.mySrc);
        if (leftover != null && leftover.getStackSize() > 0) {
            inv.injectItems(leftover, Actionable.MODULATE, this.mySrc);
        }
        return stackSize(leftover) < extracted.getStackSize();
    }

    private static long stackSize(final IAEItemStack stack) {
        return stack == null ? 0 : stack.getStackSize();
    }

    private long itemsPerOperation() {
        if (this.getConfigManager().getSetting(Settings.PLACE_BLOCK) == YesNo.YES) {
            return 1;
        }
        switch (this.getInstalledUpgrades(Upgrades.SPEED)) {
            case 1:
                return 8;
            case 2:
                return 32;
            case 3:
                return 64;
            case 4:
                return 96;
            default:
                return 1;
        }
    }

    private int availableSlots() {
        return Math.min(18 + this.getInstalledUpgrades(Upgrades.CAPACITY) * 9, 63);
    }

    private IPartitionList<IAEItemStack> createFilter(final AppEngInternalAEInventory config, final boolean fuzzy, final FuzzyMode fzMode) {
        final IItemList<IAEItemStack> list = this.getChannel().createList();
        for (int x = 0; x < this.availableSlots(); x++) {
            final IAEItemStack ais = config.getAEStackInSlot(x);
            if (ais != null) {
                list.add(ais);
            }
        }
        return fuzzy ? new FuzzyPriorityList<>(list, fzMode) : new PrecisePriorityList<>(list);
    }

    private boolean requestCrafting(final int slot, final IAEItemStack ais, final ICraftingGrid cg) throws GridAccessException {
        return this.craftingTracker.handleCrafting(slot, this.itemsPerOperation(), ais, new PlaneInventoryAdaptor(this, this.mySrc), this.getTile().getWorld(),
                this.getProxy().getGrid(), cg, this.mySrc);
    }

    private boolean craftOnly() {
        return this.isCraftingEnabled() && this.getConfigManager().getSetting(Settings.CRAFT_ONLY) == YesNo.YES;
    }

    private boolean isCraftingEnabled() {
        return this.getInstalledUpgrades(Upgrades.CRAFTING) > 0;
    }

    @Override
    public ImmutableSet<ICraftingLink> getRequestedJobs() {
        return this.craftingTracker.getRequestedJobs();
    }

    @Override
    public IAEItemStack injectCraftedItems(final ICraftingLink link, final IAEItemStack items, final Actionable mode) {
        if (!this.getProxy().isActive()) {
            return items;
        }
        return this.injectItems(items, mode, this.mySrc);
    }

    @Override
    public void jobStateChange(final ICraftingLink link) {
        this.craftingTracker.jobStateChange(link);
    }
}
