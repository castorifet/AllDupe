package awa.castorifet.alldupe;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

public final class ItemStacksB64 {
    private ItemStacksB64() {}

    public static String itemToBase64(ItemStack item) throws Exception {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream)) {
            dataOutput.writeObject(item);
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        }
    }

    public static ItemStack itemFromBase64(String data) throws Exception {
        byte[] bytes = Base64.getDecoder().decode(data);
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
             BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)) {
            Object obj = dataInput.readObject();
            if (!(obj instanceof ItemStack stack)) {
                throw new IllegalStateException("Decoded object is not an ItemStack");
            }
            return stack;
        }
    }
}
