import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class TotemClickLimit extends JavaPlugin implements Listener {
    private final Map<Player, Integer> clickCounts = new HashMap<>();
    
    @Override  
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @EventHandler  
    public void onRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        
        if (event.getMaterial() == Material.TOTEM_OF_UNDYING && event.getAction().toString().contains("RIGHT_CLICK")) {
            // Prevent auto-clicking  
            clickCounts.putIfAbsent(player, 0);
            int clicks = clickCounts.get(player) + 1;

            if (clicks > 15) {
                player.kickPlayer("§bPvPClub §7You clicked onto the totem much too quickly, try to slow it down!");
                clickCounts.remove(player);
                return;
            }

            clickCounts.put(player, clicks);
            moveTotemToOffhand(player);
            
            // Reset clicks after a delay  
            new BukkitRunnable() {
                @Override  
                public void run() {
                    clickCounts.put(player, 0);
                }
            }.runTaskLater(this, 20); // Reset after 1 second
            
            // Check for temporary ban  
            if (clicks >= 3) {
                player.kickPlayer("You have been clicking on the totem 3 times too quickly!");
                Bukkit.getScheduler().runTaskLater(this, () -> {
                    // You can implement a more sophisticated ban system here  
                    // For example, store the player UUID and ban for one day  
                    Bukkit.getBanList(org.bukkit.BanList.Type.NAME).addBan(player.getName(), "Banned for excessive clicking", null, null);
                }, 1L);
                clickCounts.remove(player);
            }
        }
    }

    private void moveTotemToOffhand(Player player) {
        ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
        if (itemInMainHand.getType() == Material.TOTEM_OF_UNDYING) {
            ItemStack totem = itemInMainHand.clone();
            player.getInventory().setItemInMainHand(new ItemStack(Material.AIR)); // Remove from main hand  
            player.getInventory().setItemInOffHand(totem); // Move to offhand  
        }
    }
}
