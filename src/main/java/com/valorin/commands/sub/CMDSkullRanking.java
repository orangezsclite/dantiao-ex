package com.valorin.commands.sub;

import com.valorin.caches.AreaCache;
import com.valorin.commands.SubCommand;
import com.valorin.commands.way.AdminCommand;
import com.valorin.data.encapsulation.RankingSkull;
import com.valorin.ranking.skull.SkullManager;
import com.valorin.util.ViaVersion;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.valorin.Main.getInstance;
import static com.valorin.configuration.languagefile.MessageSender.sm;
import static com.valorin.configuration.languagefile.MessageSender.sml;

public class CMDSkullRanking extends SubCommand implements AdminCommand {

    public CMDSkullRanking() {
        super("rankingskull", "rsk");
    }

    public void sendHelp(Player player) {
        sm("", player);
        sm("&3&lDan&b&l&oTiao &f&l>> &a管理员帮助：头颅排行榜操作", player, false);
        sm("&b/dt rankingskull(rsk) create <编辑名> <类型(win或kd)> <排名> &f- &a将指针所指的头颅设置为某种排行榜的排行头颅", player, false);
        sm("&b/dt rankingskull(rsk) remove <编辑名> &f- &a删除某个排行头颅",
                player, false);
        sm("&b/dt rankingskull(rsk) list &f- &a查看所有排行头颅及其坐标信息", player, false);
        sm("&b/dt rankingskull(rsk) refresh &f- &a手动刷新所有排行头颅", player, false);
        sm("", player);
    }

    public static boolean isInt(String... nl) {
        boolean result = true;
        for (String n : nl) {
            Pattern pattern = Pattern.compile("[0-9]+");
            Matcher matcher = pattern.matcher(n);
            if (!matcher.matches()) {
                result = false;
            }
        }
        return result;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label,
                             String[] args) {
        Player player = null;
        if (sender instanceof Player) {
            player = (Player) sender;
        }
        if (args.length == 1) {
            sendHelp(player);
            return true;
        }
        AreaCache cache = getInstance().getCacheHandler().getArea();
        if (args[1].equalsIgnoreCase("remove")) {
            if (args.length != 3) {
                sm("&7正确用法：/dt rsk remove <编辑名>", player);
                return true;
            }
            String editName = args[2];
            List<RankingSkull> rankingSkullList = cache.getRankingSkullList();
            boolean isIncluded = false;
            for (RankingSkull rankingSkull : rankingSkullList) {
                if (rankingSkull.getEditName().equals(editName)) {
                    isIncluded = true;
                    break;
                }
            }
            if (!isIncluded) {
                sm("&c[x]不存在的排行头颅，请检查输入", player);
                return true;
            }
            cache.removeRankingSkull(editName);
            sm("&a[v]排行头颅删除完毕！", player);
            return true;
        }
        if (args[1].equalsIgnoreCase("list")) {
            List<RankingSkull> rankingSkullList = cache.getRankingSkullList();
            if (rankingSkullList.size() == 0) {
                sm("&7没有创建任何排行头颅", player);
                return true;
            }
            sm("&6排行头颅如下 [right]", player);
            for (RankingSkull rankingSkull : rankingSkullList) {
                String editName = rankingSkull.getEditName();
                String rankingType = rankingSkull.getRankingType();
                int ranking = rankingSkull.getRanking();
                Location location = rankingSkull.getLocation();
                if (location != null) {
                    Block skullBlock = location.getBlock();
                    if (ViaVersion.isPlayerSkull(skullBlock.getType())) {
                        sm("&e编辑名:{editname} &f- &b对应第{ranking}名 &f- &d类型:{type} &f- &e头颅方块坐标({world},{x},{y},{z})",
                                player,
                                "editname ranking type world x y z",
                                new String[]{editName, "" + ranking, "" + rankingType, location.getWorld().getName(), "" + location.getBlockX(), "" + location.getBlockY(), "" + location.getBlockZ()}, false);
                    } else {
                        sm("&e编辑名:{editname} &f- &b对应第{ranking}名 &f- &d类型:{type} &f- &e头颅方块坐标({world},{x},{y},{z}) &c[此头颅已被其他方块代替或已被其他方式破坏，无法更新]",
                                player,
                                "editname ranking type world x y z",
                                new String[]{editName, "" + ranking, "" + rankingType, location.getWorld().getName(), "" + location.getBlockX(), "" + location.getBlockY(), "" + location.getBlockZ()}, false);
                    }
                } else {
                    sm("&e编辑名:{editname} &f- &b对应第{ranking}名 &f- &d类型:{type} &f- &c头颅方块坐标无效(可能是其所在的世界已被删除)",
                            player,
                            "editname ranking type",
                            new String[]{editName, "" + ranking, "" + rankingType}, false);
                }
            }
            sm("&6共计 &f&l{amount} &6个排行头颅", player, "amount", new String[]{""
                    + rankingSkullList.size()});
            return true;
        }
        if (args[1].equalsIgnoreCase("refresh")) {
            SkullManager.refresh(false);
            sm("&a[v]排行头颅刷新成功", player);
            return true;
        }
        if (player == null) {
            sm("&c[x]这条指令只能由服务器内的玩家执行！后台无法使用！", null);
            return true;
        }
        if (args[1].equalsIgnoreCase("create")) {
            if (args.length != 5) {
                sml("&7正确用法：/dt rsk create <编辑名> <类型> <排名>|&7类型输入win代表胜场数排行榜，输入kd代表KD值排行榜(字母不分大小写)", player);
                return true;
            }
            HashSet<Material> transparentMaterial = new HashSet<>();
            transparentMaterial.add(Material.AIR);
            transparentMaterial.add(Material.SNOW);
            transparentMaterial.add(Material.GRASS);
            transparentMaterial.add(Material.GLASS);
            Block block = player.getTargetBlock(transparentMaterial, 5);

            if (!ViaVersion.isPlayerSkull(block.getType())) {
                sm("&c[x]请将指针对准玩家头颅方块", player);
                return true;
            }
            String rankingType = args[3];
            if (!rankingType.equalsIgnoreCase("win") && !rankingType.equalsIgnoreCase("kd")) {
                sml("&c[x]类型请输入英文 win 或 kd|&7输入win代表胜场数排行榜，输入kd代表KD值排行榜(字母不分大小写)", player);
                return true;
            }
            String rankingString = args[4];
            if (!isInt(rankingString)) {
                sm("&c[x]请输入有效的阿拉伯数字！", player);
                return true;
            }
            int ranking = Integer.parseInt(rankingString);
            if (ranking <= 0) {
                sm("&c[x]请输入有效的阿拉伯数字！", player);
                return true;
            }
            String editName = args[2];
            Location location = block.getLocation();
            List<RankingSkull> rankingSkullList = cache.getRankingSkullList();
            boolean isEditNameRepeat = false;
            boolean isLocationRepeat = false;
            for (RankingSkull rankingSkull : rankingSkullList) {
                if (rankingSkull.getEditName().equals(editName)) {
                    isEditNameRepeat = true;
                }
                if (rankingSkull.getLocation() != null) {
                    if (rankingSkull.getLocation().equals(location)) {
                        isLocationRepeat = true;
                    }
                }
            }
            if (isEditNameRepeat || isLocationRepeat) {
                sm("&c[x]排行头颅创建失败，原因如下：", player);
                if (isEditNameRepeat) {
                    sm("&7排行头颅的编辑名 {editname} 已存在", player, "editname", new String[]{editName}, false);
                }
                if (isLocationRepeat) {
                    sm("&7这个位置设置了其他排行头颅，你可以通过点击它来查看其编辑名", player, false);
                }
                return true;
            }
            cache.addRankingSkull(editName, rankingType, ranking, location);
            SkullManager.refresh(false);
            sm("&a[v]排行头颅创建完毕！", player);
            return true;
        }
        sendHelp(player);
        return true;
    }
}