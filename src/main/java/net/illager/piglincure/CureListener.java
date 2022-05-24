package net.illager.piglincure;

import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Piglin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class CureListener implements Listener {

  private static final int CURE_DELAY = 3600;
  private PiglinCurePlugin plugin;
  private HashSet<UUID> curing = new HashSet<UUID>();

  public CureListener(PiglinCurePlugin plugin) {
    this.plugin = plugin;
    plugin
        .getServer()
        .getPluginManager()
        .registerEvents(this, plugin);
  }

  @EventHandler
  public void onUseItem(PlayerInteractEntityEvent event) {

    // Interaction is not with a zombified piglin
    if (event.getRightClicked().getType() != EntityType.ZOMBIFIED_PIGLIN)
      return;

    final PigZombie zombifiedPiglin = (PigZombie) event.getRightClicked();
    final UUID zpUuid = zombifiedPiglin.getUniqueId();
    final Player player = event.getPlayer();
    final ItemStack usedItem = player
        .getInventory()
        .getItem(event.getHand());

    // Piglin is already being cured
    if (curing.contains(zpUuid))
      return;

    // Weakness effect is not applied to zombified piglin
    if (!zombifiedPiglin.hasPotionEffect(PotionEffectType.WEAKNESS))
      return;

    // Item used is not a golden carrot
    if (usedItem.getType() != Material.GOLDEN_CARROT)
      return;

    curing.add(zpUuid);
    usedItem.setAmount(usedItem.getAmount() - 1);

    // Produce immediate effects
    player.playSound(
        zombifiedPiglin,
        Sound.ENTITY_ZOMBIFIED_PIGLIN_ANGRY,
        SoundCategory.NEUTRAL,
        1.0f,
        1.0f);
    final PotionEffect speedEffect = new PotionEffect(
        PotionEffectType.SPEED,
        CURE_DELAY,
        2,
        true);
    zombifiedPiglin.addPotionEffect(speedEffect);

    // Schedule delayed effects
    Bukkit
        .getScheduler()
        .runTaskLater(this.plugin, new Runnable() {
          @Override
          public void run() {
            final Piglin piglin = (Piglin) zombifiedPiglin
                .getWorld()
                .spawnEntity(zombifiedPiglin.getLocation(), EntityType.PIGLIN);

            // Copy data
            piglin.setAge(zombifiedPiglin.getAge());
            final String customName = zombifiedPiglin.getCustomName();
            if (customName != null)
              piglin.setCustomName(customName);
            final EntityEquipment pEquipment = piglin.getEquipment();
            final EntityEquipment zpEquipment = zombifiedPiglin.getEquipment();
            pEquipment.clear();
            pEquipment.setItemInMainHand(zpEquipment.getItemInMainHand());
            pEquipment.setItemInOffHand(zpEquipment.getItemInOffHand());
            pEquipment.setArmorContents(zpEquipment.getArmorContents());

            zombifiedPiglin.remove();
            player.playSound(
                piglin,
                Sound.ENTITY_PIGLIN_CELEBRATE,
                SoundCategory.NEUTRAL,
                1.0f,
                1.0f);
            curing.remove(zpUuid);
          }
        }, CURE_DELAY);
  }
}
