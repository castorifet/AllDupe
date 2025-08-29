package awa.castorifet.alldupe;

import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.Location;

public class FlagListener implements Listener {
    private final DupeFlagPlugin plugin;

    private final NamespacedKey KEY_FLAGGED;
    private final NamespacedKey KEY_ITEM64;

    public FlagListener(DupeFlagPlugin plugin) {
        this.plugin = plugin;
        this.KEY_FLAGGED = new NamespacedKey(plugin, "dupe_flagged");
        this.KEY_ITEM64 = new NamespacedKey(plugin, "dupe_item_b64");
    }

    private static boolean isAllowedType(Entity entity) {
        EntityType t = entity.getType();
        return t == EntityType.COW || t == EntityType.PIG || t == EntityType.DONKEY;
    }

    private static boolean isAdult(Entity entity) {
        if (entity instanceof Ageable ageable) {
            return ageable.isAdult();
        }
        return false;
    }

    private static boolean isShulkerBox(ItemStack item) {
        return item != null && item.getType() != null && Tag.SHULKER_BOXES.isTagged(item.getType());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onRightClickEntity(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        Entity clicked = event.getRightClicked();

        if (!isAllowedType(clicked)) return;

        if (!isAdult(clicked)) {
            player.sendMessage("Only adult animals can be flagged.");
            event.setCancelled(true);
            return;
        }

        ItemStack inHand = player.getInventory().getItemInMainHand();
        if (!isShulkerBox(inHand)) {
            player.sendMessage("Only shulker boxes are supported to dupe.");
            event.setCancelled(true);
            return;
        }

        ItemStack snapshot = inHand.clone();
        snapshot.setAmount(1);

        String base64;
        try {
            base64 = ItemStacksB64.itemToBase64(snapshot);
        } catch (Exception ex) {
            player.sendMessage("Failed to mark: serialization error.");
            plugin.getLogger().warning("Serialization failed: " + ex.getMessage());
            event.setCancelled(true);
            return;
        }

        PersistentDataContainer pdc = clicked.getPersistentDataContainer();
        pdc.set(KEY_FLAGGED, PersistentDataType.BYTE, (byte)1);
        pdc.set(KEY_ITEM64, PersistentDataType.STRING, base64);

        player.sendMessage("Flagged.Kill to dupe.");
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFlaggedEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Player killer = entity.getKiller();
        if (killer == null) return; 

        PersistentDataContainer pdc = entity.getPersistentDataContainer();
        Byte flagged = pdc.get(KEY_FLAGGED, PersistentDataType.BYTE);
        String item64 = pdc.get(KEY_ITEM64, PersistentDataType.STRING);

        if (flagged == null || flagged == 0 || item64 == null || item64.isEmpty()) return;

        ItemStack dup;
        try {
            dup = ItemStacksB64.itemFromBase64(item64);
        } catch (Exception ex) {
            plugin.getLogger().warning("Deserialization failed: " + ex.getMessage());
            return;
        }

        ItemStack drop1 = dup.clone();
        drop1.setAmount(1);
        ItemStack drop2 = dup.clone();
        drop2.setAmount(1);

        Location loc = entity.getLocation();
        World world = loc.getWorld();
        if (world != null) {
            world.dropItemNaturally(loc, drop1);
            world.dropItemNaturally(loc, drop2);
        }
    }
}
