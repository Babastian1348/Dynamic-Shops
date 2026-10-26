package com.babastian.dynamicshops.items;

import com.babastian.dynamicshops.config.ModConfig;
import com.babastian.dynamicshops.money.MoneyComponent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.math.Fraction;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class WalletItem extends Item {

    public WalletItem(Properties properties) {
        super(properties.stacksTo(1).component(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY));
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        updateWalletLore(stack);
        return stack;
    }

    private int getMaxCapacity() {
        return ModConfig.getInstance().MAX_CAPACITY;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            int currentPocketMoney = MoneyComponent.getBalance(player);
            NumberFormat format = NumberFormat.getNumberInstance(Locale.US);
            String formattedMoney = format.format(currentPocketMoney);

            Component message = Component.translatable("actionbar.dynamicshops.pocket_money", formattedMoney)
                    .withStyle(Style.EMPTY.withColor(0x00AA00).withBold(Boolean.TRUE));

            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.connection.send(new net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket(message));
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        TooltipDisplay display = stack.getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT);
        if (!display.shows(DataComponents.BUNDLE_CONTENTS)) {
            return Optional.empty();
        }
        BundleContents contents = stack.get(DataComponents.BUNDLE_CONTENTS);
        if (contents == null || contents.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new net.minecraft.world.inventory.tooltip.BundleTooltip(contents));
    }

    // --- INSERCIÓN Y EXTRACCIÓN SEGURA ---

    @Override
    public boolean overrideStackedOnOther(ItemStack self, Slot slot, ClickAction clickAction, Player player) {
        if (clickAction == ClickAction.PRIMARY) {
            ItemStack slotItem = slot.getItem();
            if (!slotItem.isEmpty() && slotItem.getItem() instanceof CurrencyItem) {
                int inserted = tryInsertCurrency(self, slotItem, player, slot);
                if (inserted > 0) {
                    playInsertSound(player);
                    updateWalletLore(self);
                    this.broadcastChangesOnContainerMenu(player);
                    return true;
                } else {
                    playInsertFailSound(player);
                }
            }
        } else if (clickAction == ClickAction.SECONDARY) {
            ItemStack slotItem = slot.getItem();
            if (slotItem.isEmpty()) {
                BundleContents initialContents = self.get(DataComponents.BUNDLE_CONTENTS);
                if (initialContents != null && !initialContents.isEmpty()) {
                    BundleContents.Mutable contents = new BundleContents.Mutable(initialContents);
                    ItemStack removed = contents.removeOne();
                    if (removed != null) {
                        ItemStack remainder = slot.safeInsert(removed);
                        if (remainder.getCount() > 0) {
                            contents.tryInsert(remainder);
                        } else {
                            playRemoveOneSound(player);
                        }
                        self.set(DataComponents.BUNDLE_CONTENTS, contents.toImmutable());
                        updateWalletLore(self);
                        this.broadcastChangesOnContainerMenu(player);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack self, ItemStack other, Slot slot, ClickAction clickAction, Player player, SlotAccess carriedItem) {
        if (clickAction == ClickAction.PRIMARY) {
            if (!other.isEmpty() && other.getItem() instanceof CurrencyItem) {
                if (slot.allowModification(player)) {
                    int inserted = tryInsertCurrencyDirect(self, other);
                    if (inserted > 0) {
                        playInsertSound(player);
                        updateWalletLore(self);
                        this.broadcastChangesOnContainerMenu(player);
                        return true;
                    } else {
                        playInsertFailSound(player);
                    }
                }
            }
        } else if (clickAction == ClickAction.SECONDARY) {
            if (other.isEmpty() && slot.allowModification(player)) {
                BundleContents initialContents = self.get(DataComponents.BUNDLE_CONTENTS);
                if (initialContents != null && !initialContents.isEmpty()) {
                    BundleContents.Mutable contents = new BundleContents.Mutable(initialContents);
                    ItemStack removed = contents.removeOne();
                    if (removed != null) {
                        playRemoveOneSound(player);
                        carriedItem.set(removed);
                        self.set(DataComponents.BUNDLE_CONTENTS, contents.toImmutable());
                        updateWalletLore(self);
                        this.broadcastChangesOnContainerMenu(player);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private int tryInsertCurrency(ItemStack walletStack, ItemStack slotItem, Player player, Slot slot) {
        CurrencyItem currencyItem = (CurrencyItem) slotItem.getItem();
        int valuePerItem = currencyItem.getValue(slotItem);
        if (valuePerItem <= 0) valuePerItem = 1;

        int currentTotal = getTotalMoney(walletStack);
        int maxCap = getMaxCapacity();
        int availableMoney = maxCap - currentTotal;
        if (availableMoney <= 0) return 0;

        int maxItemsByMoney = availableMoney / valuePerItem;
        int itemsToTake = Math.min(slotItem.getCount(), maxItemsByMoney);
        if (itemsToTake <= 0) return 0;

        ItemStack taken = slot.safeTake(slotItem.getCount(), itemsToTake, player);
        if (taken.isEmpty()) return 0;

        addItemsToWallet(walletStack, taken);
        return taken.getCount();
    }

    private int tryInsertCurrencyDirect(ItemStack walletStack, ItemStack otherItem) {
        CurrencyItem currencyItem = (CurrencyItem) otherItem.getItem();
        int valuePerItem = currencyItem.getValue(otherItem);
        if (valuePerItem <= 0) valuePerItem = 1;

        int currentTotal = getTotalMoney(walletStack);
        int maxCap = getMaxCapacity();
        int availableMoney = maxCap - currentTotal;
        if (availableMoney <= 0) return 0;

        int maxItemsByMoney = availableMoney / valuePerItem;
        int itemsToTake = Math.min(otherItem.getCount(), maxItemsByMoney);
        if (itemsToTake <= 0) return 0;

        ItemStack taken = otherItem.split(itemsToTake);
        if (taken.isEmpty()) return 0;

        addItemsToWallet(walletStack, taken);
        return taken.getCount();
    }

    private static void addItemsToWallet(ItemStack walletStack, ItemStack newItems) {
        List<ItemStack> allItems = new ArrayList<>();
        BundleContents contents = walletStack.get(DataComponents.BUNDLE_CONTENTS);
        if (contents != null) {
            allItems.addAll(contents.itemCopyStream().toList());
        }
        allItems.add(newItems);

        List<ItemStack> packed = normalizeAndPack(allItems);
        List<ItemStackTemplate> templates = packed.stream()
                .map(ItemStackTemplate::fromNonEmptyStack)
                .toList();

        walletStack.set(DataComponents.BUNDLE_CONTENTS, new BundleContents(templates));
    }

    private static List<ItemStack> normalizeAndPack(List<ItemStack> rawItems) {
        List<ItemStack> packed = new ArrayList<>();
        for (ItemStack raw : rawItems) {
            if (raw.isEmpty()) continue;
            int remaining = raw.getCount();
            int maxStack = raw.getMaxStackSize();

            for (ItemStack existing : packed) {
                if (remaining <= 0) break;
                if (ItemStack.isSameItemSameComponents(existing, raw)) {
                    int space = maxStack - existing.getCount();
                    if (space > 0) {
                        int toAdd = Math.min(remaining, space);
                        existing.grow(toAdd);
                        remaining -= toAdd;
                    }
                }
            }

            while (remaining > 0) {
                int toAdd = Math.min(remaining, maxStack);
                ItemStack newStack = raw.copyWithCount(toAdd);
                packed.add(newStack);
                remaining -= toAdd;
            }
        }
        return packed;
    }

    public static void cycleSelectedItem(ItemStack stack, boolean scrollDown) {
        BundleContents initialContents = stack.get(DataComponents.BUNDLE_CONTENTS);
        if (initialContents == null || initialContents.isEmpty()) return;

        int size = initialContents.size();
        int currentIndex = initialContents.getSelectedItemIndex();

        if (currentIndex == -1) {
            currentIndex = scrollDown ? 0 : size - 1;
        } else {
            if (scrollDown) {
                currentIndex = (currentIndex + 1) % size;
            } else {
                currentIndex = (currentIndex - 1 + size) % size;
            }
        }

        BundleContents.Mutable contents = new BundleContents.Mutable(initialContents);
        contents.toggleSelectedItem(currentIndex);
        stack.set(DataComponents.BUNDLE_CONTENTS, contents.toImmutable());
    }

    // --- BARRA DE PROGRESO ---

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getTotalMoney(stack) > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        BundleContents contents = stack.get(DataComponents.BUNDLE_CONTENTS);
        if (contents == null || contents.isEmpty()) return 0;
        Fraction weight = contents.weight().result().orElse(Fraction.ZERO);
        float percentage = weight.floatValue();
        return Math.min(Math.round(percentage * 13.0F), 13);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        int total = getTotalMoney(stack);
        int maxCap = getMaxCapacity();
        return total >= maxCap ? 0xFF5555 : 0x55FF55;
    }

    // --- MÉTODOS AUXILIARES ---

    private int getTotalMoney(ItemStack stack) {
        BundleContents contents = stack.get(DataComponents.BUNDLE_CONTENTS);
        if (contents == null) {
            contents = BundleContents.EMPTY;
            stack.set(DataComponents.BUNDLE_CONTENTS, contents);
        }

        if (!stack.has(DataComponents.LORE)) {
            updateWalletLore(stack);
        }

        int total = 0;
        for (ItemStack item : contents.itemCopyStream().toList()) {
            if (item.getItem() instanceof CurrencyItem currency) {
                total += currency.getValue(item) * item.getCount();
            }
        }
        return total;
    }

    private static void updateWalletLore(ItemStack stack) {
        int total = 0;
        BundleContents contents = stack.get(DataComponents.BUNDLE_CONTENTS);
        if (contents != null) {
            for (ItemStack item : contents.itemCopyStream().toList()) {
                if (item.getItem() instanceof CurrencyItem currency) {
                    total += currency.getValue(item) * item.getCount();
                }
            }
        }

        int maxCap = ModConfig.getInstance().MAX_CAPACITY;
        NumberFormat format = NumberFormat.getNumberInstance(Locale.US);

        List<Component> loreLines;
        if (total == 0) {
            loreLines = List.of(
                    Component.translatable("tooltip.dynamicshops.wallet.capacity", format.format(maxCap))
                            .withStyle(Style.EMPTY.withColor(0x00AA00).withBold(Boolean.FALSE))
            );
        } else {
            loreLines = List.of(
                    Component.translatable("tooltip.dynamicshops.wallet.content", format.format(total), format.format(maxCap))
                            .withStyle(Style.EMPTY.withColor(0x00AA00).withBold(Boolean.FALSE))
            );
        }
        stack.set(DataComponents.LORE, new ItemLore(loreLines));
    }

    private static void playRemoveOneSound(Entity entity) {
        entity.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
    }

    private static void playInsertSound(Entity entity) {
        entity.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
    }

    private static void playInsertFailSound(Entity entity) {
        entity.playSound(SoundEvents.BUNDLE_INSERT_FAIL, 1.0F, 1.0F);
    }

    private void broadcastChangesOnContainerMenu(Player player) {
        AbstractContainerMenu containerMenu = player.containerMenu;
        if (containerMenu != null) {
            containerMenu.slotsChanged(player.getInventory());
        }
    }
}