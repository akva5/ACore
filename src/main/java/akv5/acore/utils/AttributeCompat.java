package akv5.acore.utils;

import akv5.acore.ACore;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.UUID;

public class AttributeCompat {

    @SuppressWarnings("deprecation")
    public static void addAttributeModifier(ItemMeta itemMeta, String attributeName, String itemName,
                                            double value, EquipmentSlot slot) {
        if (itemMeta == null) {
            ACore.getInstance().getLogger().warning("ItemMeta null при добавлении атрибута " + attributeName);
            return;
        }
        try {
            Attribute attribute = getCompatibleAttribute(attributeName);
            if (attribute == null) {
                ACore.getInstance().getLogger().warning("Неизвестный атрибут: " + attributeName);
                return;
            }

            UUID modifierUUID = UUID.nameUUIDFromBytes((itemName + attributeName).getBytes());
            AttributeModifier modifier = new AttributeModifier(
                    modifierUUID,
                    itemName + "_" + attributeName,
                    value,
                    AttributeModifier.Operation.ADD_NUMBER,
                    slot
            );

            boolean modifierExists = false;
            if (itemMeta.hasAttributeModifiers()) {
                var existingModifiers = itemMeta.getAttributeModifiers(attribute);
                if (existingModifiers != null) {
                    for (AttributeModifier existingMod : existingModifiers) {
                        if (existingMod.getUniqueId().equals(modifierUUID)) {
                            modifierExists = true;
                            break;
                        }
                    }
                }
            }

            if (!modifierExists) {
                itemMeta.addAttributeModifier(attribute, modifier);
            }

        } catch (Exception e) {
            ACore.getInstance().getLogger().warning("Ошибка при добавлении атрибута " + attributeName + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void addAttributeModifierSafe(ItemMeta itemMeta, String attributeName, String itemName,
                                                double value, EquipmentSlot slot) {
        if (itemMeta == null) {
            ACore.getInstance().getLogger().warning("ItemMeta null при добавлении атрибута " + attributeName);
            return;
        }
        try {
            Attribute attribute = getCompatibleAttribute(attributeName);
            if (attribute == null) {
                ACore.getInstance().getLogger().warning("Неизвестный атрибут: " + attributeName);
                return;
            }

            UUID modifierUUID = UUID.nameUUIDFromBytes((itemName + attributeName).getBytes());
            AttributeModifier modifier = new AttributeModifier(
                    modifierUUID,
                    itemName + "_" + attributeName,
                    value,
                    AttributeModifier.Operation.ADD_NUMBER,
                    slot
            );

            if (itemMeta.hasAttributeModifiers()) {
                var existingModifiers = itemMeta.getAttributeModifiers(attribute);
                if (existingModifiers != null) {
                    for (AttributeModifier existingMod : existingModifiers) {
                        if (existingMod.getUniqueId().equals(modifierUUID)) {
                            itemMeta.removeAttributeModifier(attribute, existingMod);
                            break;
                        }
                    }
                }
            }

            itemMeta.addAttributeModifier(attribute, modifier);

        } catch (Exception e) {
            ACore.getInstance().getLogger().warning("Ошибка при добавлении атрибута " + attributeName + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static Attribute getCompatibleAttribute(String attributeName) {
        String normalizedName = attributeName.toUpperCase().replace(" ", "_");

        try {
            return Attribute.valueOf(normalizedName);
        } catch (IllegalArgumentException e) {
            return getAttributeByAlternativeName(normalizedName);
        }
    }

    private static Attribute getAttributeByAlternativeName(String name) {
        switch (name) {
            default:
                return null;
        }
    }
}