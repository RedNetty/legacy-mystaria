package me.retrorealms.practiceserver.mechanics.mobs.boss.abilties;

import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

public class AbilityTask extends BukkitRunnable {
    private final BossAbility ability;
    private final LivingEntity bossEntity;

    public AbilityTask(BossAbility ability, LivingEntity bossEntity) {
        this.ability = ability;
        this.bossEntity = bossEntity;
    }

    @Override
    public void run() {
        ability.use(bossEntity.getLocation());
    }
}