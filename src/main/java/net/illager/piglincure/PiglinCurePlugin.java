package net.illager.piglincure;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.plugin.*;
import org.bukkit.plugin.java.annotation.plugin.author.Author;

@Plugin(name = "PiglinCure", version = "1.0.0")
@ApiVersion(ApiVersion.Target.v1_18)
@Description("Cure zombified piglins")
@Author("Benjamin Herman <benjamin@metanomial.com>")
@Website("https://github.com/illagernet/piglin-cure")
public class PiglinCurePlugin extends JavaPlugin {

  @Override
  public void onEnable() {
    new CureListener(this);
  }
}
