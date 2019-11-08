package com.arvenwood.deenchantment;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static org.spongepowered.api.text.format.TextColors.*;

@Plugin(
        id = "deenchantment",
        name = "DeEnchantment",
        version = "1.0.0",
        description = "Turn enchanted items back into books!",
        url = "https://ore.spongepowered.org/Arvenwood/DeEnchantment",
        authors = {"doot"}
)
public class DeEnchantment {

    private final Random RANDOM = new Random();

    private final ConfigurationLoader<CommentedConfigurationNode> loader;
    private final Path configFile;

    private CommentedConfigurationNode node;
    private Config config;

    @Inject
    public DeEnchantment(@DefaultConfig(sharedRoot = true) ConfigurationLoader<CommentedConfigurationNode> loader,
                         @DefaultConfig(sharedRoot = true) Path configFile) {
        this.loader = loader;
        this.configFile = configFile;
    }

    @Listener
    public void onInit(GameInitializationEvent event) throws ObjectMappingException, IOException {
        if (Files.exists(this.configFile)) {
            this.node = this.loader.load();
            this.config = this.node.getValue(Config.TYPE_TOKEN);
        } else {
            this.config = new Config();
            this.node = this.loader.createEmptyNode();
            this.node.setValue(Config.TYPE_TOKEN, this.config);
            this.loader.save(this.node);
        }

        CommandSpec help = CommandSpec.builder()
                .permission("deenchantment.help")
                .executor((src, args) -> {
                    PaginationList.Builder pagination = PaginationList.builder();
                    List<Text> contents = new ArrayList<>();

                    pagination.padding(Text.of(GOLD, "=")).title(Text.of(YELLOW, "DeEnchantment"));

                    contents.add(Text.of(GREEN, "/deenchant help ", DARK_GREEN, "- Shows this help command."));
                    if (src.hasPermission("deenchantment.reload"))
                        contents.add(Text.of(GREEN, "/deenchant reload ", DARK_GREEN, "- Reloads the configuration."));
                    if (src.hasPermission("deenchantment.one"))
                        contents.add(Text.of(GREEN, "/deenchant one ", DARK_GREEN, "- Randomly transfers one enchantment from the item in hand."));
                    if (src.hasPermission("deenchantment.all"))
                        contents.add(Text.of(GREEN, "/deenchant all ", DARK_GREEN, "- Transfers all enchantments from the item in hand."));

                    pagination.contents(contents).sendTo(src);

                    return CommandResult.success();
                })
                .build();

        CommandSpec reload = CommandSpec.builder()
                .permission("deenchantment.reload")
                .executor((src, args) -> {
                    try {
                        this.node = this.loader.load();
                        this.config = this.node.getValue(Config.TYPE_TOKEN);
                    } catch (IOException | ObjectMappingException e) {
                        throw new CommandException(Text.of("Failed to reload the configuration."));
                    }

                    src.sendMessage(Text.of(GREEN, "Successfully reloaded the configuration."));

                    return CommandResult.success();
                })
                .build();

        CommandSpec one = CommandSpec.builder()
                .permission("deenchantment.one")
                .executor(this::one)
                .build();

        CommandSpec all = CommandSpec.builder()
                .permission("deenchantment.all")
                .executor(this::all)
                .build();

        CommandSpec deenchant = CommandSpec.builder()
                .permission("deenchantment.use")
                .child(help, "help", "?")
                .child(reload, "reload")
                .child(one, "one")
                .child(all, "all")
                .build();

        Sponge.getCommandManager().register(this, deenchant, "deenchant");
    }

    private CommandResult one(CommandSource src, CommandContext ctx) throws CommandException {
        if (!(src instanceof Player)) {
            throw new CommandException(Text.of("You must be a player to use that command!"));
        }

        Player player = (Player) src;

        ItemStack item = player.getItemInHand(HandTypes.MAIN_HAND)
                .filter(i -> !i.isEmpty())
                .orElseThrow(() -> new CommandException(Text.of("You must be holding an item in your main hand.")));

        List<Enchantment> enchantments = item.get(Keys.ITEM_ENCHANTMENTS)
                .orElse(Collections.emptyList())
                .stream()
                .filter(enchantment -> !this.config.blacklist.contains(enchantment.getType()))
                .collect(Collectors.toList());

        if (enchantments.isEmpty()) {
            throw new CommandException(Text.of("That item is not deenchantable."));
        }

        Enchantment enchantment = random(enchantments, RANDOM);

        ItemStack book = ItemStack.builder()
                .itemType(ItemTypes.ENCHANTED_BOOK)
                .add(Keys.STORED_ENCHANTMENTS, Lists.newArrayList(enchantment))
                .build();

        player.setItemInHand(HandTypes.MAIN_HAND, book);

        String name = enchantment.getType().getId().toLowerCase();
        if (name.startsWith("minecraft:")) name = name.substring(10);

        player.sendMessage(Text.of(GREEN, "You kept ", WHITE, name, " ", enchantment.getLevel(), GREEN, "!"));

        return CommandResult.success();
    }

    private CommandResult all(CommandSource src, CommandContext ctx) throws CommandException {
        if (!(src instanceof Player)) {
            throw new CommandException(Text.of("You must be a player to use that command!"));
        }

        Player player = (Player) src;

        ItemStack item = player.getItemInHand(HandTypes.MAIN_HAND)
                .filter(i -> !i.isEmpty())
                .orElseThrow(() -> new CommandException(Text.of("You must be holding an item in your main hand.")));
        List<Enchantment> enchantments = item.get(Keys.ITEM_ENCHANTMENTS)
                .orElse(Collections.emptyList())
                .stream()
                .filter(enchantment -> !this.config.blacklist.contains(enchantment.getType()))
                .collect(Collectors.toList());

        if (enchantments.isEmpty()) {
            throw new CommandException(Text.of("That item is not deenchantable."));
        }

        ItemStack book = ItemStack.builder()
                .itemType(ItemTypes.ENCHANTED_BOOK)
                .add(Keys.STORED_ENCHANTMENTS, enchantments)
                .build();

        player.setItemInHand(HandTypes.MAIN_HAND, book);

        player.sendMessage(Text.of(GREEN, "All allowed enchantments were transferred to the book."));

        return CommandResult.success();
    }

    private <T> T random(List<T> list, Random random) {
        int index = random.nextInt(list.size());
        return list.get(index);
    }
}
