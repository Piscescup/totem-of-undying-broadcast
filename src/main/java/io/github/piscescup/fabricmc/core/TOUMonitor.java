package io.github.piscescup.fabricmc.core;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;


/**
 *
 * @author REN YuanTong
 * @since
 */
public class TOUMonitor {
    private final LocalPlayer player;
    private Integer previousTotemCount;
    private final CheckSchedule schedule;

    private TOUMonitor(LocalPlayer player) {
        this.player = player;
        this.schedule = new CheckSchedule();
    }

    public static TOUMonitor create(LocalPlayer player) {
        return new TOUMonitor(player);
    }

    public LocalPlayer player() {
        return this.player;
    }

    public boolean checkTotemNumber(int threshold) {
        int currentCount = countTotems();
        Integer previousCount = previousTotemCount;
        previousTotemCount = currentCount;

        boolean firstLow = previousCount == null && currentCount < threshold;
        boolean droppedLow = previousCount != null
            && currentCount < previousCount
            && currentCount < threshold;

        return firstLow || droppedLow;
    }

    public boolean checkPeriod(boolean enable, int tickPeriod) {
        return schedule.tick(enable, tickPeriod);
    }

    public int countTotems() {
        int count = 0;

        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            if (stack.getItem() == Items.TOTEM_OF_UNDYING) {
                count += stack.getCount();
            }
        }

        ItemStack offhandStack = player.getOffhandItem();
        if (offhandStack.getItem() == Items.TOTEM_OF_UNDYING) {
            count += offhandStack.getCount();
        }

        return count;
    }

    public boolean hasTotemInOffhand() {
        return player.getOffhandItem().getItem() == Items.TOTEM_OF_UNDYING;
    }

    public ManualCheckResult runManualCheck(int threshold) {
        int currentCount = countTotems();
        boolean lowCount   = currentCount < threshold;
        boolean hasOffhand = hasTotemInOffhand();

        if (lowCount && !hasOffhand) return ManualCheckResult.ALL;
        if (lowCount)                return ManualCheckResult.LOW_COUNT;
        if (!hasOffhand)             return ManualCheckResult.MISSING;
        return ManualCheckResult.PASS;
    }

    public void reset() {
        previousTotemCount = null;
        schedule.reset();
    }

    public enum ManualCheckResult {
        UNAVAILABLE,
        PASS,
        LOW_COUNT,
        MISSING,
        ALL
    }

}

final class CheckSchedule {
    private int interval;
    private int elapsedTicks;

    boolean tick(boolean enable, int checkTick) {
        if (!enable) {
            elapsedTicks = 0;
            return false;
        }

        if (interval != checkTick) {
            interval = checkTick;
            elapsedTicks = 0;
        }

        if (++elapsedTicks < interval) {
            return false;
        }

        elapsedTicks = 0;
        return true;
    }

    void reset() {
        interval = 0;
        elapsedTicks = 0;
    }
}
