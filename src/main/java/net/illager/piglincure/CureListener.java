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
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
  }

  @EventHandler
  public void onUseItem(PlayerInteractEntityEvent event) {

    // Interaction is not with a zombified piglin
    if (event.getRightClicked().getType() != EntityType.ZOMBIFIED_PIGLIN)
      return;

    final PigZombie zombifiedPiglin = (PigZombie) event.getRightClicked();
    final ItemStack usedItem = event.getPlayer().getInventory().getItem(event.getHand());

    // Piglin is already being cured
    if (curing.contains(zombifiedPiglin.getUniqueId()))
      return;

    // Weakness effect is not applied to zombified piglin
    if (!zombifiedPiglin.hasPotionEffect(PotionEffectType.WEAKNESS))
      return;

    // Item used is not a golden carrot
    if (usedItem.getType() != Material.GOLDEN_CARROT)
      return;

    usedItem.setAmount(usedItem.getAmount() - 1);
    curePiglin(zombifiedPiglin, event.getPlayer());
  }

  private void curePiglin(PigZombie patient, Player doctor) {
    curing.add(patient.getUniqueId());

    // Immediate effects
    doctor.playSound(patient, Sound.ENTITY_ZOMBIFIED_PIGLIN_ANGRY, 1.0f, 1.0f);
    patient.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, CURE_DELAY, 2, true));

    // Schedule delayed effects
    Bukkit.getScheduler().runTaskLater(this.plugin, new Runnable() {
      @Override
      public void run() {
        final Piglin convert = (Piglin) patient.getWorld().spawnEntity(patient.getLocation(), EntityType.PIGLIN);

        // Copy data from "patient" entity to "convert" entity
        convert.setAge(patient.getAge());
        final String customName = patient.getCustomName();
        if (customName != null)
          convert.setCustomName(customName);
        final EntityEquipment convertEquipment = convert.getEquipment();
        final EntityEquipment patientEquipment = patient.getEquipment();
        convertEquipment.clear();
        convertEquipment.setItemInMainHand(patientEquipment.getItemInMainHand());
        convertEquipment.setItemInOffHand(patientEquipment.getItemInOffHand());
        convertEquipment.setArmorContents(patientEquipment.getArmorContents());

        curing.remove(patient.getUniqueId());
        patient.remove();
        doctor.playSound(convert, Sound.ENTITY_PIGLIN_CELEBRATE, SoundCategory.NEUTRAL, 1.0f, 1.0f);
      }
    }, CURE_DELAY);
  }
}
