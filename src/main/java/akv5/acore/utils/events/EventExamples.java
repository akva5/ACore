package akv5.acore.utils.events;

import akv5.acore.ACore;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class EventExamples {

    private final ACore plugin;

    public EventExamples(ACore plugin) {
        this.plugin = plugin;
        createEventsFile();
    }

    public void createEventsFile() {
        File eventsFile = new File(plugin.getDataFolder(), "customs/events.yml");

        if (!eventsFile.exists()) {
            writeEventsFile(eventsFile);
        }
    }

    private void writeEventsFile(File eventsFile) {
        try (FileWriter writer = new FileWriter(eventsFile)) {
            writer.write(getEventsFileContent());
        } catch (IOException e) {
            plugin.getLogger().warning("Не удалось создать events.yml: " + e.getMessage());
        }
    }

    private String getEventsFileContent() {
        return "#\n" +
                "# Кастом ивенты к кастом предметам\n" +
                "#\n\n" +
                "# Взаимодействие\n" +
                "events:\n" +
                "  right_click_air:\n" +
                "    type: \"PlayerInteractEvent\"\n" +
                "    slot: \"HAND\"\n" +
                "    conditions:\n" +
                "      - \"action:RIGHT_CLICK_AIR\"\n" +
                "    actions:\n" +
                "      - \"execute_command:give_diamond\"\n" +
                "    actionParams:\n" +
                "      give_diamond:\n" +
                "        command: \"give {player} diamond 1\"\n" +
                "\n" +
                "  right_click_block:\n" +
                "    type: \"PlayerInteractEvent\"\n" +
                "    slot: \"HAND\"\n" +
                "    conditions:\n" +
                "      - \"action:RIGHT_CLICK_BLOCK\"\n" +
                "    actions:\n" +
                "      - \"teleport:location\"\n" +
                "    actionParams:\n" +
                "      location:\n" +
                "        location: \"100,64,100\"\n" +
                "\n" +
                "  right_click_block_2:\n" +
                "    type: \"PlayerInteractEvent\"\n" +
                "    slot: \"OFF_HAND\"\n" +
                "    conditions:\n" +
                "      - \"action:RIGHT_CLICK_BLOCK\"\n" +
                "    actions:\n" +
                "      - \"spawn_particles:runa_particles\"\n" +
                "    actionParams:\n" +
                "      runa_particles:\n" +
                "        type: \"CRIT\"\n" +
                "        x: 0\n" +
                "        y: 3\n" +
                "        z: 0\n" +
                "        offsetX: 1\n" +
                "        offsetY: 1\n" +
                "        offsetZ: 1\n" +
                "        speed: 1\n" +
                "        count: 30\n" +
                "\n" +
                "  left_click_air:\n" +
                "    type: \"PlayerInteractEvent\"\n" +
                "    slot: \"HAND\"\n" +
                "    conditions:\n" +
                "      - \"action:LEFT_CLICK_AIR\"\n" +
                "    actions:\n" +
                "      - \"spawn_particles:flame_particles\"\n" +
                "    actionParams:\n" +
                "      flame_particles:\n" +
                "        type: \"FLAME\"\n" +
                "        count: 20\n" +
                "\n" +
                "  left_click_block:\n" +
                "    type: \"PlayerInteractEvent\"\n" +
                "    slot: \"HAND\"\n" +
                "    conditions:\n" +
                "      - \"action:LEFT_CLICK_BLOCK\"\n" +
                "    actions:\n" +
                "      - \"break_blocks:break_area\"\n" +
                "    actionParams:\n" +
                "      break_area:\n" +
                "        radius: 3\n" +
                "\n\n" +
                "  # События времени (автоматически вызываются)\n" +
                "  on_day:\n" +
                "    type: \"TimeChangeEvent\"\n" +
                "    slot: \"ANY\"\n" +
                "    conditions:\n" +
                "      - \"is_day:true\"\n" +
                "    actions:\n" +
                "      - \"sun_power:day_power\"\n" +
                "    actionParams:\n" +
                "      day_power:\n" +
                "        strength_boost: 2\n" +
                "\n" +
                "  on_night:\n" +
                "    type: \"TimeChangeEvent\"\n" +
                "    slot: \"ANY\"\n" +
                "    conditions:\n" +
                "      - \"is_night:true\"\n" +
                "    actions:\n" +
                "      - \"moon_blessing:night_blessing\"\n" +
                "    actionParams:\n" +
                "      night_blessing:\n" +
                "        night_vision: true\n" +
                "        speed: 1\n" +
                "\n\n" +
                "  # События эффектов (автоматически вызываются при наличии эффектов)\n" +
                "  on_positive_effect:\n" +
                "    type: \"PotionEffectAddEvent\"\n" +
                "    slot: \"ANY\"\n" +
                "    conditions:\n" +
                "      - \"effect_type:POSITIVE\"\n" +
                "    actions:\n" +
                "      - \"effect_amplifier:boost_positive\"\n" +
                "    actionParams:\n" +
                "      boost_positive:\n" +
                "        amplifier_boost: 1\n" +
                "\n" +
                "  on_negative_effect:\n" +
                "    type: \"PotionEffectAddEvent\"\n" +
                "    slot: \"ANY\"\n" +
                "    conditions:\n" +
                "      - \"effect_type:NEGATIVE\"\n" +
                "    actions:\n" +
                "      - \"effect_cleanse:cleanse_negative\"\n" +
                "    actionParams:\n" +
                "      cleanse_negative:\n" +
                "        chance_to_cleanse: 0.5\n" +
                "\n\n" +
                "  # Боевые ивенты\n" +
                "  on_attack:\n" +
                "    type: \"EntityDamageByEntityEvent\"\n" +
                "    slot: \"HAND\"\n" +
                "    actions:\n" +
                "      - \"lightning_strike:lightning\"\n" +
                "    actionParams:\n" +
                "      lightning:\n" +
                "        damage: 5.0\n" +
                "\n" +
                "  on_critical_hit:\n" +
                "    type: \"EntityDamageByEntityEvent\"\n" +
                "    slot: \"HAND\"\n" +
                "    conditions:\n" +
                "      - \"is_critical:true\"\n" +
                "    actions:\n" +
                "      - \"explosion:small_explosion\"\n" +
                "    actionParams:\n" +
                "      small_explosion:\n" +
                "        power: 2.0\n" +
                "        break_blocks: false\n" +
                "\n" +
                "  on_kill:\n" +
                "    type: \"EntityDeathEvent\"\n" +
                "    slot: \"HAND\"\n" +
                "    conditions:\n" +
                "      - \"killer:{player}\"\n" +
                "    actions:\n" +
                "      - \"summon_mob:zombies\"\n" +
                "    actionParams:\n" +
                "      zombies:\n" +
                "        mob_type: \"ZOMBIE\"\n" +
                "        count: 3\n" +
                "\n" +
                "  on_low_health:\n" +
                "    type: \"EntityDamageByEntityEvent\"\n" +
                "    slot: \"ANY\"\n" +
                "    conditions:\n" +
                "      - \"player_health:<5\"\n" +
                "    actions:\n" +
                "      - \"apply_effects:low_health_effects\"\n" +
                "    actionParams:\n" +
                "      low_health_effects:\n" +
                "        effects:\n" +
                "          REGENERATION: 2\n" +
                "          ABSORPTION: 1\n" +
                "\n\n" +
                "  # Ивенты передвижения\n" +
                "  on_sneak:\n" +
                "    type: \"PlayerToggleSneakEvent\"\n" +
                "    slot: \"ANY\"\n" +
                "    conditions:\n" +
                "      - \"sneaking:true\"\n" +
                "    actions:\n" +
                "      - \"invisibility:sneak_invis\"\n" +
                "    actionParams:\n" +
                "      sneak_invis:\n" +
                "        duration: 100\n" +
                "\n" +
                "  on_sprint:\n" +
                "    type: \"PlayerToggleSprintEvent\"\n" +
                "    slot: \"ANY\"\n" +
                "    conditions:\n" +
                "      - \"sprinting:true\"\n" +
                "    actions:\n" +
                "      - \"speed_boost:sprint_boost\"\n" +
                "    actionParams:\n" +
                "      sprint_boost:\n" +
                "        amplifier: 2\n" +
                "\n" +
                "  on_jump:\n" +
                "    type: \"PlayerMoveEvent\"\n" +
                "    slot: \"ANY\"\n" +
                "    conditions:\n" +
                "      - \"velocity_y:>0\"\n" +
                "    actions:\n" +
                "      - \"launch:jump_boost\"\n" +
                "    actionParams:\n" +
                "      jump_boost:\n" +
                "        power: 1.5\n" +
                "\n" +
                "  on_fall:\n" +
                "    type: \"EntityDamageEvent\"\n" +
                "    slot: \"ANY\"\n" +
                "    conditions:\n" +
                "      - \"cause:FALL\"\n" +
                "    actions:\n" +
                "      - \"slow_fall:fall_protection\"\n" +
                "    actionParams:\n" +
                "      fall_protection:\n" +
                "        duration: 100\n" +
                "\n\n" +
                "  # Ивенты инвентаря и экипировки\n" +
                "  on_equip:\n" +
                "    type: \"PlayerItemHeldEvent\"\n" +
                "    slot: \"HAND\"\n" +
                "    actions:\n" +
                "      - \"apply_attributes:equip_attributes\"\n" +
                "    actionParams:\n" +
                "      equip_attributes:\n" +
                "        attributes:\n" +
                "          GENERIC_MAX_HEALTH: 10\n" +
                "          GENERIC_ATTACK_DAMAGE: 5\n" +
                "\n" +
                "  on_unequip:\n" +
                "    type: \"PlayerDropItemEvent\"\n" +
                "    slot: \"HAND\"\n" +
                "    actions:\n" +
                "      - \"remove_attributes:remove_attrs\"\n" +
                "    actionParams:\n" +
                "      remove_attrs:\n" +
                "        attributes:\n" +
                "          - \"GENERIC_MAX_HEALTH\"\n" +
                "          - \"GENERIC_ATTACK_DAMAGE\"\n" +
                "\n" +
                "  on_hold:\n" +
                "    type: \"PlayerItemHeldEvent\"\n" +
                "    slot: \"HAND\"\n" +
                "    actions:\n" +
                "      - \"night_vision:hold_night_vision\"\n" +
                "    actionParams:\n" +
                "      hold_night_vision:\n" +
                "        duration: 200\n" +
                "\n" +
                "  on_drop:\n" +
                "    type: \"PlayerDropItemEvent\"\n" +
                "    slot: \"HAND\"\n" +
                "    actions:\n" +
                "      - \"explosion:drop_explosion\"\n" +
                "    actionParams:\n" +
                "      drop_explosion:\n" +
                "        power: 3.0\n" +
                "\n\n" +
                "  # Ивенты блоков\n" +
                "  on_block_place:\n" +
                "    type: \"BlockPlaceEvent\"\n" +
                "    slot: \"HAND\"\n" +
                "    actions:\n" +
                "      - \"fill_area:obsidian_area\"\n" +
                "    actionParams:\n" +
                "      obsidian_area:\n" +
                "        material: \"OBSIDIAN\"\n" +
                "        radius: 2\n" +
                "\n" +
                "  on_block_break:\n" +
                "    type: \"BlockBreakEvent\"\n" +
                "    slot: \"HAND\"\n" +
                "    actions:\n" +
                "      - \"drop_experience:break_exp\"\n" +
                "    actionParams:\n" +
                "      break_exp:\n" +
                "        experience: 10\n" +
                "        multiply_drops: true\n" +
                "\n" +
                "  on_farmland_trample:\n" +
                "    type: \"PlayerInteractEvent\"\n" +
                "    slot: \"ANY\"\n" +
                "    conditions:\n" +
                "      - \"block:FARMLAND\"\n" +
                "    actions:\n" +
                "      - \"restore_farmland:restore_farm\"\n" +
                "    actionParams:\n" +
                "      restore_farm:\n" +
                "        restore_speed: 5\n" +
                "\n\n" +
                "  # Ивенты окружающей среды\n" +
                "  on_enter_water:\n" +
                "    type: \"PlayerMoveEvent\"\n" +
                "    slot: \"ANY\"\n" +
                "    conditions:\n" +
                "      - \"in_water:true\"\n" +
                "    actions:\n" +
                "      - \"water_breathing:water_breath\"\n" +
                "    actionParams:\n" +
                "      water_breath:\n" +
                "        duration: 100\n" +
                "\n" +
                "  on_enter_lava:\n" +
                "    type: \"PlayerMoveEvent\"\n" +
                "    slot: \"ANY\"\n" +
                "    conditions:\n" +
                "      - \"in_lava:true\"\n" +
                "    actions:\n" +
                "      - \"fire_resistance:lava_resist\"\n" +
                "    actionParams:\n" +
                "      lava_resist:\n" +
                "        duration: 200\n" +
                "\n" +
                "  on_rain:\n" +
                "    type: \"WeatherChangeEvent\"\n" +
                "    slot: \"ANY\"\n" +
                "    conditions:\n" +
                "      - \"raining:true\"\n" +
                "    actions:\n" +
                "      - \"lightning_immunity:rain_immunity\"\n" +
                "    actionParams:\n" +
                "      rain_immunity:\n" +
                "        duration: 300\n" +
                "\n" +
                "  on_thunder:\n" +
                "    type: \"LightningStrikeEvent\"\n" +
                "    slot: \"ANY\"\n" +
                "    actions:\n" +
                "      - \"charge_item:thunder_charge\"\n" +
                "    actionParams:\n" +
                "      thunder_charge:\n" +
                "        enchantments:\n" +
                "          SHARPNESS: 3\n" +
                "\n\n" +
                "  # Ивенты крафта и зельек\n" +
                "  on_craft:\n" +
                "    type: \"CraftItemEvent\"\n" +
                "    slot: \"ANY\"\n" +
                "    actions:\n" +
                "      - \"bonus_craft:craft_bonus\"\n" +
                "    actionParams:\n" +
                "      craft_bonus:\n" +
                "        multiplier: 2\n" +
                "        extra_items:\n" +
                "          DIAMOND: 1\n" +
                "\n" +
                "  on_smith:\n" +
                "    type: \"PrepareAnvilEvent\"\n" +
                "    slot: \"ANY\"\n" +
                "    actions:\n" +
                "      - \"upgrade_item:smith_upgrade\"\n" +
                "    actionParams:\n" +
                "      smith_upgrade:\n" +
                "        preserve_enchantments: true\n" +
                "        discount: 0.5\n" +
                "\n" +
                "  on_brew:\n" +
                "    type: \"BrewEvent\"\n" +
                "    slot: \"ANY\"\n" +
                "    actions:\n" +
                "      - \"enhance_potion:brew_enhance\"\n" +
                "    actionParams:\n" +
                "      brew_enhance:\n" +
                "        amplifier_boost: 1\n" +
                "        duration_boost: 2.0\n" +
                "\n\n" +
                "  # Рыбалка\n" +
                "  on_fish:\n" +
                "    type: \"PlayerFishEvent\"\n" +
                "    slot: \"HAND\"\n" +
                "    actions:\n" +
                "      - \"treasure_fishing:fish_treasure\"\n" +
                "    actionParams:\n" +
                "      fish_treasure:\n" +
                "        treasure_chance: 0.3\n" +
                "        junk_chance: 0.1\n" +
                "\n" +
                "  on_fish_bite:\n" +
                "    type: \"PlayerFishEvent\"\n" +
                "    slot: \"HAND\"\n" +
                "    conditions:\n" +
                "      - \"state:CAUGHT_FISH\"\n" +
                "    actions:\n" +
                "      - \"auto_reel:fish_auto_reel\"\n" +
                "    actionParams:\n" +
                "      fish_auto_reel:\n" +
                "        instant_catch: true\n" +
                "\n\n" +
                "  # Еда и голод\n" +
                "  on_eat:\n" +
                "    type: \"PlayerItemConsumeEvent\"\n" +
                "    slot: \"HAND\"\n" +
                "    actions:\n" +
                "      - \"bonus_effects:eat_effects\"\n" +
                "    actionParams:\n" +
                "      eat_effects:\n" +
                "        effects:\n" +
                "          SATURATION: 2\n" +
                "          HEALTH_BOOST: 1\n" +
                "\n" +
                "  on_starve:\n" +
                "    type: \"FoodLevelChangeEvent\"\n" +
                "    slot: \"ANY\"\n" +
                "    conditions:\n" +
                "      - \"food_level:<6\"\n" +
                "    actions:\n" +
                "      - \"auto_feed:starve_feed\"\n" +
                "    actionParams:\n" +
                "      starve_feed:\n" +
                "        restore_amount: 10\n" +
                "\n\n" +
                "  # Опыт\n" +
                "  on_exp_gain:\n" +
                "    type: \"PlayerExpChangeEvent\"\n" +
                "    slot: \"ANY\"\n" +
                "    actions:\n" +
                "      - \"exp_multiplier:exp_boost\"\n" +
                "    actionParams:\n" +
                "      exp_boost:\n" +
                "        multiplier: 1.5\n" +
                "\n" +
                "  on_level_up:\n" +
                "    type: \"PlayerLevelChangeEvent\"\n" +
                "    slot: \"ANY\"\n" +
                "    conditions:\n" +
                "      - \"level_change:>0\"\n" +
                "    actions:\n" +
                "      - \"reward_level:level_rewards\"\n" +
                "    actionParams:\n" +
                "      level_rewards:\n" +
                "        rewards:\n" +
                "          - \"give {player} diamond 1\"\n" +
                "          - \"effect {player} REGENERATION 30 1\"\n" +
                "\n\n" +
                "  # Порталы\n" +
                "  on_nether_portal:\n" +
                "    type: \"PlayerPortalEvent\"\n" +
                "    slot: \"ANY\"\n" +
                "    actions:\n" +
                "      - \"safe_teleport:nether_safe\"\n" +
                "    actionParams:\n" +
                "      nether_safe:\n" +
                "        prevent_ghast_spawn: true\n" +
                "        fire_resistance: 60\n" +
                "\n" +
                "  on_end_portal:\n" +
                "    type: \"PlayerPortalEvent\"\n" +
                "    slot: \"ANY\"\n" +
                "    conditions:\n" +
                "      - \"destination:THE_END\"\n" +
                "    actions:\n" +
                "      - \"dragon_buff:end_buff\"\n" +
                "    actionParams:\n" +
                "      end_buff:\n" +
                "        strength: 2\n" +
                "        resistance: 1\n" +
                "\n\n" +
                "  # Редстоун\n" +
                "  on_redstone_activate:\n" +
                "    type: \"BlockRedstoneEvent\"\n" +
                "    slot: \"ANY\"\n" +
                "    actions:\n" +
                "      - \"power_beam:redstone_beam\"\n" +
                "    actionParams:\n" +
                "      redstone_beam:\n" +
                "        range: 10\n" +
                "        damage: 3.0\n" +
                "\n" +
                "  on_redstone_pulse:\n" +
                "    type: \"BlockRedstoneEvent\"\n" +
                "    slot: \"ANY\"\n" +
                "    conditions:\n" +
                "      - \"power:15\"\n" +
                "    actions:\n" +
                "      - \"chain_lightning:redstone_lightning\"\n" +
                "    actionParams:\n" +
                "      redstone_lightning:\n" +
                "        chain_count: 5\n" +
                "\n\n" +
                "  # Чары\n" +
                "  on_enchant:\n" +
                "    type: \"EnchantItemEvent\"\n" +
                "    slot: \"ANY\"\n" +
                "    actions:\n" +
                "      - \"bonus_enchant:enchant_bonus\"\n" +
                "    actionParams:\n" +
                "      enchant_bonus:\n" +
                "        bonus_levels: 5\n" +
                "\n" +
                "  on_combine:\n" +
                "    type: \"PrepareAnvilEvent\"\n" +
                "    slot: \"ANY\"\n" +
                "    actions:\n" +
                "      - \"preserve_enchant:combine_preserve\"\n" +
                "    actionParams:\n" +
                "      combine_preserve:\n" +
                "        no_penalty: true\n" +
                "        max_cost: 10\n" +
                "\n\n" +
                "  # Монстры\n" +
                "  on_zombie_kill:\n" +
                "    type: \"EntityDeathEvent\"\n" +
                "    slot: \"HAND\"\n" +
                "    conditions:\n" +
                "      - \"entity_type:ZOMBIE\"\n" +
                "    actions:\n" +
                "      - \"zombie_army:zombie_spawn\"\n" +
                "    actionParams:\n" +
                "      zombie_spawn:\n" +
                "        spawn_count: 3\n" +
                "        duration: 60\n" +
                "\n" +
                "  on_creeper_explode:\n" +
                "    type: \"ExplosionPrimeEvent\"\n" +
                "    slot: \"ANY\"\n" +
                "    conditions:\n" +
                "      - \"entity_type:CREEPER\"\n" +
                "    actions:\n" +
                "      - \"absorb_explosion:creeper_absorb\"\n" +
                "    actionParams:\n" +
                "      creeper_absorb:\n" +
                "        convert_to_health: true\n" +
                "\n\n" +
                "  # Животные\n" +
                "  on_animal_breed:\n" +
                "    type: \"EntityBreedEvent\"\n" +
                "    slot: \"ANY\"\n" +
                "    actions:\n" +
                "      - \"instant_growth:breed_growth\"\n" +
                "    actionParams:\n" +
                "      breed_growth:\n" +
                "        instant_adult: true\n" +
                "        extra_breeding: true\n" +
                "\n" +
                "  on_animal_tame:\n" +
                "    type: \"EntityTameEvent\"\n" +
                "    slot: \"ANY\"\n" +
                "    actions:\n" +
                "      - \"super_pet:tame_pet\"\n" +
                "    actionParams:\n" +
                "      tame_pet:\n" +
                "        max_health: 100\n" +
                "        attack_damage: 10\n" +
                "\n\n" +
                "  # Полет\n" +
                "  on_elytra_boost:\n" +
                "    type: \"PlayerToggleFlightEvent\"\n" +
                "    slot: \"ANY\"\n" +
                "    actions:\n" +
                "      - \"rocket_boost:elytra_rocket\"\n" +
                "    actionParams:\n" +
                "      elytra_rocket:\n" +
                "        power: 3.0\n" +
                "        duration: 40\n" +
                "\n" +
                "  on_fall_flying:\n" +
                "    type: \"EntityToggleGlideEvent\"\n" +
                "    slot: \"ANY\"\n" +
                "    conditions:\n" +
                "      - \"is_gliding:true\"\n" +
                "    actions:\n" +
                "      - \"air_strike:glide_strike\"\n" +
                "    actionParams:\n" +
                "      glide_strike:\n" +
                "        damage: 5.0\n" +
                "        radius: 3.0\n" +
                "\n\n" +
                "  # Команды\n" +
                "  on_command:\n" +
                "    type: \"PlayerCommandPreprocessEvent\"\n" +
                "    slot: \"ANY\"\n" +
                "    conditions:\n" +
                "      - \"command:/home\"\n" +
                "    actions:\n" +
                "      - \"command_boost:home_boost\"\n" +
                "    actionParams:\n" +
                "      home_boost:\n" +
                "        instant_teleport: true\n" +
                "        no_cooldown: true\n" +
                "\n" +
                "  on_special_command:\n" +
                "    type: \"PlayerCommandPreprocessEvent\"\n" +
                "    slot: \"ANY\"\n" +
                "    conditions:\n" +
                "      - \"command:/powerup\"\n" +
                "    actions:\n" +
                "      - \"custom_effect:powerup_effects\"\n" +
                "    actionParams:\n" +
                "      powerup_effects:\n" +
                "        effects:\n" +
                "          JUMP_BOOST: 3\n" +
                "          SPEED: 2\n" +
                "\n\n" +
                "  # Чат\n" +
                "  on_chat_message:\n" +
                "    type: \"AsyncPlayerChatEvent\"\n" +
                "    slot: \"ANY\"\n" +
                "    conditions:\n" +
                "      - \"message:abracadabra\"\n" +
                "    actions:\n" +
                "      - \"magic_words:magic_effects\"\n" +
                "    actionParams:\n" +
                "      magic_effects:\n" +
                "        effects:\n" +
                "          LEVITATION: 1\n" +
                "          GLOWING: 1\n" +
                "\n" +
                "  on_emote:\n" +
                "    type: \"PlayerAnimationEvent\"\n" +
                "    slot: \"ANY\"\n" +
                "    actions:\n" +
                "      - \"visual_effects:emote_effects\"\n" +
                "    actionParams:\n" +
                "      emote_effects:\n" +
                "        particles: true\n" +
                "        sounds: true\n" +
                "\n\n" +
                "  # Пвп\n" +
                "  on_pvp_kill:\n" +
                "    type: \"PlayerDeathEvent\"\n" +
                "    slot: \"HAND\"\n" +
                "    conditions:\n" +
                "      - \"killer_type:PLAYER\"\n" +
                "    actions:\n" +
                "      - \"bounty_hunter:pvp_reward\"\n" +
                "    actionParams:\n" +
                "      pvp_reward:\n" +
                "        reward:\n" +
                "          - \"give {killer} diamond 5\"\n" +
                "          - \"broadcast &c{killer} победил {player}!\"\n" +
                "\n" +
                "  on_pvp_death:\n" +
                "    type: \"PlayerRespawnEvent\"\n" +
                "    slot: \"ANY\"\n" +
                "    conditions:\n" +
                "      - \"last_damage_cause:ENTITY_ATTACK\"\n" +
                "    actions:\n" +
                "      - \"revenge:pvp_revenge\"\n" +
                "    actionParams:\n" +
                "      pvp_revenge:\n" +
                "        effects:\n" +
                "          STRENGTH: 1\n" +
                "          SPEED: 1\n" +
                "\n\n" +
                "  # Телепортация\n" +
                "  on_teleport:\n" +
                "    type: \"PlayerTeleportEvent\"\n" +
                "    slot: \"ANY\"\n" +
                "    actions:\n" +
                "      - \"dimensional_anchor:teleport_anchor\"\n" +
                "    actionParams:\n" +
                "      teleport_anchor:\n" +
                "        prevent_void_death: true\n" +
                "        safe_location: true\n" +
                "\n" +
                "  on_ender_pearl:\n" +
                "    type: \"PlayerTeleportEvent\"\n" +
                "    slot: \"ANY\"\n" +
                "    conditions:\n" +
                "      - \"cause:ENDER_PEARL\"\n" +
                "    actions:\n" +
                "      - \"no_damage:pearl_no_damage\"\n" +
                "    actionParams:\n" +
                "      pearl_no_damage:\n" +
                "        no_fall_damage: true\n" +
                "\n\n" +
                "  # Ресурсные пакеты\n" +
                "  on_custom_sound:\n" +
                "    type: \"PlayerInteractEvent\"\n" +
                "    slot: \"HAND\"\n" +
                "    actions:\n" +
                "      - \"play_custom_sound:magic_sound\"\n" +
                "    actionParams:\n" +
                "      magic_sound:\n" +
                "        sound_file: \"custom_magic.ogg\"\n" +
                "        volume: 1.0\n" +
                "\n" +
                "  on_custom_model:\n" +
                "    type: \"PlayerItemHeldEvent\"\n" +
                "    slot: \"HAND\"\n" +
                "    actions:\n" +
                "      - \"change_model:custom_model\"\n" +
                "    actionParams:\n" +
                "      custom_model:\n" +
                "        model_data: 12345\n" +
                "\n\n" +
                "  # Комбо\n" +
                "  on_combo_3:\n" +
                "    type: \"EntityDamageByEntityEvent\"\n" +
                "    slot: \"HAND\"\n" +
                "    conditions:\n" +
                "      - \"combo_count:3\"\n" +
                "    actions:\n" +
                "      - \"combo_attack:combo_3_attack\"\n" +
                "    actionParams:\n" +
                "      combo_3_attack:\n" +
                "        damage_multiplier: 1.5\n" +
                "\n" +
                "  on_combo_5:\n" +
                "    type: \"EntityDamageByEntityEvent\"\n" +
                "    slot: \"HAND\"\n" +
                "    conditions:\n" +
                "      - \"combo_count:5\"\n" +
                "    actions:\n" +
                "      - \"ultimate_attack:combo_5_attack\"\n" +
                "    actionParams:\n" +
                "      combo_5_attack:\n" +
                "        area_damage: true\n" +
                "        radius: 5.0\n" +
                "\n\n" +
                "  # Праздники\n" +
                "  on_christmas:\n" +
                "    type: \"PlayerJoinEvent\"\n" +
                "    slot: \"ANY\"\n" +
                "    conditions:\n" +
                "      - \"month:12\"\n" +
                "    actions:\n" +
                "      - \"gift_drop:christmas_gift\"\n" +
                "    actionParams:\n" +
                "      christmas_gift:\n" +
                "        gift_chance: 0.1\n" +
                "\n" +
                "  on_halloween:\n" +
                "    type: \"EntitySpawnEvent\"\n" +
                "    slot: \"ANY\"\n" +
                "    conditions:\n" +
                "      - \"month:10\"\n" +
                "    actions:\n" +
                "      - \"scary_mobs:halloween_scary\"\n" +
                "    actionParams:\n" +
                "      halloween_scary:\n" +
                "        special_mobs: true";
    }
}