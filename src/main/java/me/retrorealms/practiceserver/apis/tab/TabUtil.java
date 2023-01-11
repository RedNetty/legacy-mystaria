package me.retrorealms.practiceserver.apis.tab;

import me.retrorealms.practiceserver.mechanics.damage.Damage;
import me.retrorealms.practiceserver.mechanics.money.Money;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by Giovanni on 23-7-2017.
 */
public class TabUtil {

    static int getGems(Player player) {
        return Money.getGems(player);
    }

    public static String format(int number) {
        if (number == 0) {
            return "0";
        }
        return NumberFormat.getNumberInstance(Locale.US).format(number);
    }

    static int getDPS(Player player) {
        int total = 0;
        int dex = 0;

        for (ItemStack itemStack : player.getEquipment().getArmorContents()) {
            if (itemStack == null) continue;
            dex += Damage.getElem(itemStack, "DEX");
            total += Damage.getDps(itemStack);
        }

        dex = (int) Math.round(dex * 0.012);
        return total + dex;
    }

    static int getArmor(Player player) {
        int total = 0;
        int str = 0;
        for (ItemStack itemStack : player.getEquipment().getArmorContents()) {
            if (itemStack == null) continue;
            str += Damage.getElem(itemStack, "STR");
            total += Damage.getArmor(itemStack);
        }

        str = (int) Math.round(str * 0.012);
        return total + str;
    }


    static int getHPS(Player player) {
        int total = 0;
        int vit = 0;
        for (ItemStack itemStack : player.getEquipment().getArmorContents()) {
            if (itemStack == null) continue;
            vit += Damage.getElem(itemStack, "VIT");
            total += Damage.getHps(itemStack);
        }
        vit = (int) Math.round((double) vit * 0.3);
        return total + vit;
    }

    static int getEnergy(Player player) {
        int total = 0;
        int nrg = 0;
        for (ItemStack itemStack : player.getEquipment().getArmorContents()) {
            if (itemStack == null) continue;
            nrg += Damage.getElem(itemStack, "INT");
            total += Damage.getEnergy(itemStack);
        }

        nrg = Math.round(nrg / 125);
        return total + nrg;
    }
}
