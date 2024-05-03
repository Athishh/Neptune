package dev.lrxh.neptune.utils;

import lombok.experimental.UtilityClass;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

@UtilityClass
public class ItemUtils {

    public static ItemStack[] fixInventoryOrder(ItemStack[] source) {
        ItemStack[] fixed = new ItemStack[36];

        System.arraycopy(source, 0, fixed, 27, 9);
        System.arraycopy(source, 9, fixed, 0, 27);

        return fixed;
    }

    public static List<ItemStack> color(List<ItemStack> itemStackList, Color color) {
        List<ItemStack> items = new ArrayList<>();
        for (ItemStack itemStack : itemStackList) {
            if (itemStack == null) {
                continue;
            } else {
                itemStack.getType();
            }
            if (itemStack.getType() == Material.LEATHER_BOOTS || itemStack.getType() == Material.LEATHER_CHESTPLATE || itemStack.getType() == Material.LEATHER_HELMET
                    || itemStack.getType() == Material.LEATHER_LEGGINGS) {
                LeatherArmorMeta meta = (LeatherArmorMeta) itemStack.getItemMeta();
                meta.setColor(color);
                itemStack.setItemMeta(meta);
            } else if (itemStack.getType() == Material.WHITE_WOOL) {
                if (color.equals(Color.BLUE)) {
                    itemStack.setType(Material.BLUE_WOOL);
                } else {
                    itemStack.setType(Material.RED_WOOL);
                }
            }
            items.add(itemStack);
        }
        return items;
    }

    public String serialize(List<ItemStack> items) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        if (items == null) {
            items = new ArrayList<>();
        }

        try {
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(new GZIPOutputStream(outputStream));

            dataOutput.writeInt(items.size());

            for (ItemStack item : items) {
                if (item == null) {
                    item = new ItemStack(Material.AIR);
                }
                dataOutput.writeObject(item);
            }

            dataOutput.close();
        } catch (IOException e) {
            Console.error("Occurred while saving items " + e.getMessage());
            return null;
        }

        return Base64.getEncoder().encodeToString(outputStream.toByteArray());
    }

    public String serialize(ItemStack item) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(new GZIPOutputStream(outputStream));
            dataOutput.writeObject(item);

            dataOutput.close();
        } catch (IOException e) {
            Console.error("Occurred while saving item " + e.getMessage());
            return null;
        }

        return Base64.getEncoder().encodeToString(outputStream.toByteArray());
    }

    public List<ItemStack> deserialize(String base64) {
        byte[] data = Base64.getDecoder().decode(base64);
        List<ItemStack> items = new ArrayList<>();
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(new GZIPInputStream(inputStream));

            int size = dataInput.readInt();

            for (int i = 0; i < size; i++) {
                ItemStack item = (ItemStack) dataInput.readObject();
                items.add(item);
            }

            dataInput.close();
        } catch (IOException | ClassNotFoundException e) {
            Console.error("Occurred while loading items " + e.getMessage());
            return null;
        }
        return items;
    }

    public ItemStack deserializeItem(String base64) {
        byte[] data = Base64.getDecoder().decode(base64);
        ItemStack item = null;
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(new GZIPInputStream(inputStream));
            item = (ItemStack) dataInput.readObject();
            dataInput.close();
        } catch (IOException | ClassNotFoundException e) {
            Console.error("Occurred while loading item!");
        }
        return item;
    }
}
