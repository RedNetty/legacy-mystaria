package me.retrorealms.practiceserver.mechanics.mobs.boss.abilties;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

public class AbilityHandler {

    public static List<BossAbility> createAbilities(List<String> abilityNames) {
        List<BossAbility> abilityList = new ArrayList<>();
        for (String abilityName : abilityNames) {
            try {
                // Dynamically load the class based on the ability name
                Class<?> abilityClass = Class.forName(abilityName);

                if (BossAbility.class.isAssignableFrom(abilityClass)) {
                    // Get the constructor of the class that takes no arguments
                    Constructor<?> constructor = abilityClass.getConstructor();

                    // Create an instance of the ability class
                    BossAbility ability = (BossAbility) constructor.newInstance();
                    abilityList.add(ability);
                    // Call the execute method on the ability
                } else {
                    System.err.println("Invalid ability class: " + abilityName);
                    // Handle the case where the class does not extend BossAbility
                }
            } catch (Exception e) {
                e.printStackTrace();
                // Handle any exceptions that occur during the reflection process
            }
        }
        return abilityList;
    }

}
