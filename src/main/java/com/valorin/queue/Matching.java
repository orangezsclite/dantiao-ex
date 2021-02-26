package com.valorin.queue;

import com.valorin.Main;
import com.valorin.inventory.special.INVStart;
import com.valorin.itemstack.GUIItems;
import com.valorin.util.ViaVersion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import static com.valorin.configuration.languagefile.MessageSender.gm;
import static com.valorin.configuration.languagefile.MessageSender.sm;

public class Matching {
    private String arenaEditName; // 若为null，则说明为随机
    private String arenaDisplayName;
    private boolean enable; // 是否正在等待中
    private String waiter;
    private BukkitTask timer;
    private int time;

    private Inventory inventory;

    public Matching(String arenaEditName, String arenaDisplayName) {
        this.arenaEditName = arenaEditName;
        if (arenaDisplayName != null) {
            this.arenaDisplayName = arenaDisplayName.replace("&0", "")
                    .replace("&1", "").replace("&2", "").replace("&3", "")
                    .replace("&4", "").replace("&5", "").replace("&6", "")
                    .replace("&7", "").replace("&8", "").replace("&9", "")
                    .replace("&a", "").replace("&b", "").replace("&c", "")
                    .replace("&d", "").replace("&e", "").replace("&f", "")
                    .replace("&o", "").replace("&m", "").replace("&n", "")
                    .replace("&k", "").replace("&l", "");
        } else {
            this.arenaDisplayName = arenaDisplayName;
        }
        enable = false;
    }

    public String getArenaEditName() {
        return arenaEditName;
    }

    public String getArenaDisplayName() {
        return arenaDisplayName;
    }

    public boolean isEnable() {
        return enable;
    }

    public void refreshItem() {
        inventory.setItem(13, GUIItems.updataStart(waiter, time, arenaEditName,
                arenaDisplayName));
    }

    public void enable(String waiter) {
        this.waiter = waiter;
        enable = true;
        time = -1;
        timer = new BukkitRunnable() {
            public void run() {
                time++;
                if (inventory != null) {
                    refreshItem();
                } else {
                    Player waiterPlayer = Bukkit.getPlayerExact(waiter);
                    ViaVersion.sendActionBar(
                            waiterPlayer,
                            gm("&b正在为您搜寻对手... &6{second} &b秒", waiterPlayer,
                                    "second", new String[]{time + ""}), 0,
                            20, 60);
                }
            }
        }.runTaskTimer(Main.getInstance(), 0, 20);
    }

    public void disable(boolean isActive) {
        enable = false;
        timer.cancel();
        timer = null;
        if (isActive) {
            sm("&7已中断匹配...", Bukkit.getPlayerExact(waiter));
        }
        if (INVStart.arenaSelects.containsKey(waiter)) {
            INVStart.arenaSelects.remove(waiter);
        }
        Main.getInstance().getMatchingHandler().removeBusyPlayer(waiter);

        this.waiter = null;
    }

    public String getWaiter() {
        return waiter;
    }

    public int getTime() {
        return time;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }
}