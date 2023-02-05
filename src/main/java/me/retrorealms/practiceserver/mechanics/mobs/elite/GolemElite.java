package me.retrorealms.practiceserver.mechanics.mobs.elite;

import me.retrorealms.practiceserver.PracticeServer;
import me.retrorealms.practiceserver.mechanics.damage.Damage;
import me.retrorealms.practiceserver.mechanics.mobs.Mobs;
import me.retrorealms.practiceserver.mechanics.mobs.Spawners;
import me.retrorealms.practiceserver.mechanics.player.Listeners;
import me.retrorealms.practiceserver.utils.Particles;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Random;

public class GolemElite implements Listener {

    BukkitRunnable task;
    int timer = 1;
    public static HashMap<LivingEntity, Integer> golems;
    public void onEnable(){
        Bukkit.getServer().getPluginManager().registerEvents(this, PracticeServer.plugin);
        golems  = new HashMap<>();
        new BukkitRunnable() {
            public void run(){
                try{
                Spawners.mobs.keySet().forEach(livingEntity -> {
                    if(!Mobs.isGolemBoss(livingEntity)) return;
                    double health = livingEntity.getHealth();
                    int stage = golems.get(livingEntity);
                    if(health < 100000 * (3-stage)){
                        if(stage == 0){
                            Spawners.spawnMob(livingEntity.getLocation(), "zombie", 6, true);
                            playSound(livingEntity);
                        }if(stage == 1){
                            Spawners.spawnMob(livingEntity.getLocation(), "zombie", 6, true);
                            playSound(livingEntity);
                        }if(stage == 2){
                            livingEntity.getNearbyEntities(50.0, 50.0,50.0).forEach(player -> {
                                if(player instanceof Player){
                                    ((Player) player).playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0F, 1.0F);
                                    livingEntity.setHealth(200000);
                                    timer = 0;
                                }
                            });
                        }
                        golems.put(livingEntity, ++stage);
                    }
                    if(stage == 3){
                        if(timer == 0){
                            Vector v = new Vector(0, 1.5, 0);
                            timer = 5;
                        }else if(timer == 4){
                            Creature c = (Creature) livingEntity;
                            Vector v = c.getTarget().getLocation().toVector().subtract(livingEntity.getLocation().toVector());
                            timer--;

                            task = new BukkitRunnable() {
                                @Override
                                public void run() {
                                    if(livingEntity.isOnGround()){
                                        crit(livingEntity, task);
                                        livingEntity.setVelocity(new Vector(0, 0, 0));
                                    }
                                }
                            };
                            task.runTaskTimer(PracticeServer.plugin, 5, 2);
                        }else if(timer == 3){
                            timer--;
                        }else{
                            timer--;
                        }
                    }
                });
                }catch (Exception e) {

                }

            }
        }.runTaskTimer(PracticeServer.plugin, 20, 20);
    }

    public void playSound(LivingEntity livingEntity){

        livingEntity.getNearbyEntities(50.0, 50.0,50.0).forEach(player -> {
            if(player instanceof Player){
                ((Player) player).playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0F, 1.0F);
            }
        });
    }

    public void crit(LivingEntity s, BukkitRunnable task){
        if(task != null) task.cancel();
        int dmg = 1;
        if (s.getEquipment().getItemInMainHand() != null && s.getEquipment().getItemInMainHand().getType() != Material.AIR) {
            int min = Damage.getDamageRange(s.getEquipment().getItemInMainHand()).get(0);
            int max = Damage.getDamageRange(s.getEquipment().getItemInMainHand()).get(1);
            dmg = new Random().nextInt(max - min + 1) + min + 1;
        }
        dmg *= 2.4;
        for (Entity e : s.getNearbyEntities(8.0, 8.0, 8.0)) {
            if (!(e instanceof Player)) continue;
            if (Listeners.mobd.containsKey(s.getUniqueId())) {
                Listeners.mobd.remove(s.getUniqueId());
            }
            Player p = (Player) e;
            p.damage(s.getLastDamage(), s);
            Vector v = p.getLocation().toVector().subtract(s.getLocation().toVector());
            if (v.getX() != 0.0 || v.getY() != 0.0 || v.getZ() != 0.0) v.normalize();
            p.setVelocity(v.multiply(3));

        }

        s.getWorld().playSound(s.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.5f);
        Particles.EXPLOSION_HUGE.display(0.0f, 0.0f, 0.0f, 1.0f, 40, s.getLocation().clone().add(0.0, 1.0, 0.0), 20.0);

        s.setCustomName(Mobs.generateOverheadBar(s, s.getHealth(), s.getMaxHealth(), Mobs.getMobTier(s), true));
        s.setCustomNameVisible(true);

    }

    @EventHandler
    public void stopDespawn(ChunkUnloadEvent e){
        Entity[] entities = e.getChunk().getEntities();
        for(Entity entity : entities){
            if(entity instanceof Creature && Mobs.isGolemBoss(entity)){
                e.setCancelled(true);
            }
        }
    }
}
