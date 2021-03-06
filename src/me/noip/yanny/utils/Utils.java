package me.noip.yanny.utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.server.v1_11_R1.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_11_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

public class Utils {

    private Utils() {
    }

    public static String locationToString(Location location) {
        return String.format("%d %d %d %f %f %s",
                location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getYaw(), location.getPitch(), location.getWorld().getName());
    }

    public static Location parseLocation(String location, Plugin plugin) {
        String[] tokens = location.split(" ");

        if (tokens.length != 6) {
            plugin.getLogger().warning("ResidenceConfiguration.parseLocation: Invalid location data: " + location);
            return null;
        }

        World world = plugin.getServer().getWorld(tokens[5]);
        double x, y, z;
        float yaw, pitch;

        try {
            x = Double.parseDouble(tokens[0]);
            y = Double.parseDouble(tokens[1]);
            z = Double.parseDouble(tokens[2]);
            yaw = Float.parseFloat(tokens[3]);
            pitch = Float.parseFloat(tokens[4]);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }

        if (world == null) {
            plugin.getLogger().warning("ResidenceConfiguration.parseLocation: Invalid world name: " + tokens[5]);
            return null;
        }

        return new Location(world, x, y, z, yaw, pitch);
    }

    public static ItemStack book(String title, String author, String... pages) {
        ItemStack is = new ItemStack(Material.WRITTEN_BOOK, 1);
        net.minecraft.server.v1_11_R1.ItemStack nmsis = CraftItemStack.asNMSCopy(is);
        NBTTagCompound bd = new NBTTagCompound();
        bd.setString("title", title);
        bd.setString("author", author);
        NBTTagList bp = new NBTTagList();

        for(String text : pages) {
            bp.add(new NBTTagString(text));
        }

        bd.set("pages", bp);
        nmsis.setTag(bd);
        is = CraftItemStack.asBukkitCopy(nmsis);
        return is;
    }

    public static void openBook(ItemStack book, Player p) {
        int slot = p.getInventory().getHeldItemSlot();
        ItemStack old = p.getInventory().getItem(slot);
        p.getInventory().setItem(slot, book);

        ByteBuf buf = Unpooled.buffer(256);
        buf.setByte(0, (byte)0);
        buf.writerIndex(1);

        PacketPlayOutCustomPayload packet = new PacketPlayOutCustomPayload("MC|BOpen", new PacketDataSerializer(buf));
        ((CraftPlayer)p).getHandle().playerConnection.sendPacket(packet);
        p.getInventory().setItem(slot, old);
    }

    public static Vector computeThrow(Location from, Location to) {
        Vector fromVector = from.toVector();
        Vector toVector = to.toVector();
        toVector.subtract(fromVector);
        Vector up = new Vector(0, Math.sqrt(Math.sqrt(toVector.getX() * toVector.getX() + toVector.getY() * toVector.getY() + toVector.getZ() * toVector.getZ())) * 0.08, 0);
        toVector.multiply(0.1).add(up);
        return toVector;
    }

    public static double sumProbabilities(double p1, double p2) {
        return (p1 + p2) / (1 + p1 * p2);
    }

    public static <T extends Enum<T>> Map<T, Integer> convertToEnumMap(Map<String, Object> map, Class<T> enumType) {
        Map<T, Integer> result = new HashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue() instanceof Integer) {
                T type = Enum.valueOf(enumType, entry.getKey());
                result.put(type, (Integer) entry.getValue());
            }
        }
        return result;
    }
}
