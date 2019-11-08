package com.arvenwood.deenchantment;

import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.item.enchantment.EnchantmentType;
import org.spongepowered.api.item.enchantment.EnchantmentTypes;

import java.util.Set;

@ConfigSerializable
public class Config {

    static final TypeToken<Config> TYPE_TOKEN = TypeToken.of(Config.class);

    @Setting(comment = "Enchantments that can't be deenchanted.")
    public Set<EnchantmentType> blacklist = Sets.newHashSet(EnchantmentTypes.MENDING);
}