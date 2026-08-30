package xyz.voroniyx.deathtotem.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import org.apache.commons.lang3.NotImplementedException;
import xyz.voroniyx.deathtotem.DeathTotemMod;
import xyz.voroniyx.deathtotem.config.ModConfig;


public class ConfigCommand extends BaseCommand {

    private static final String OPTION_ENABLE_TOTEM_CONSUME = "EnableTotemConsume";
    private static final String OPTION_CONSUME_ONLY_WHEN_LAST = "TotemConsumeOnlyWhenLastTotemUsed";
    private static final String OPTION_TRIGGERING_TOTEM_NAME = "NameOfTriggeringTotem";

    @Override
    public int execute(CommandContext<CommandSourceStack> ctx) {
        throw new NotImplementedException("Use one of the private execute* methods!");
    }

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("tiab")
                        .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_OWNER))
                        .then(
                                Commands.literal("config")
                                        .then(Commands.literal(OPTION_ENABLE_TOTEM_CONSUME)
                                                .then(Commands.argument("value", BoolArgumentType.bool())
                                                        .executes(this::executeUpdateTotemConsume)
                                                )
                                        )
                                        .then(Commands.literal(OPTION_CONSUME_ONLY_WHEN_LAST)
                                                .then(Commands.argument("value", BoolArgumentType.bool())
                                                        .executes(this::executeUpdateConsumeOnlyWhenLastTotemUsed)
                                                )
                                        )
                                        .then(Commands.literal("Get")
                                                .executes(this::executeGetConfig)
                                        )
                        )
                        .then(
                                Commands.literal("override")
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .then(Commands.literal(OPTION_ENABLE_TOTEM_CONSUME)
                                                        .then(Commands.argument("value", BoolArgumentType.bool())
                                                                .executes(ctx -> executeBoolPlayerOverride(ctx, OPTION_ENABLE_TOTEM_CONSUME))
                                                        )
                                                )
                                                .then(Commands.literal(OPTION_CONSUME_ONLY_WHEN_LAST)
                                                        .then(Commands.argument("value", BoolArgumentType.bool())
                                                                .executes(ctx -> executeBoolPlayerOverride(ctx, OPTION_CONSUME_ONLY_WHEN_LAST))
                                                        )
                                                )
                                                .then(Commands.literal(OPTION_TRIGGERING_TOTEM_NAME)
                                                        .then(Commands.argument("value", StringArgumentType.greedyString())
                                                                .executes(this::executeTriggeringTotemNameOverride)
                                                        )
                                                )
                                        )
                        )
        );
    }

    private int executeUpdateTotemConsume(CommandContext<CommandSourceStack> ctx) {
        try {
            CommandSourceStack source = ctx.getSource();
            boolean newValue = BoolArgumentType.getBool(ctx, "value");

            var config = DeathTotemMod.CONFIG.getData();
            config.EnableTotemConsume = newValue;

            if (!persist(source)) {
                return 1;
            }

            source.sendSystemMessage(Component.literal(
                    "Successfully set \"" + OPTION_ENABLE_TOTEM_CONSUME + "\" to " + newValue).withColor(TextColor.GRAY));

            return 1;
        } catch (Exception ex) {
            DeathTotemMod.LOGGER.error(ex);
            return 1;
        }
    }

    private int executeUpdateConsumeOnlyWhenLastTotemUsed(CommandContext<CommandSourceStack> ctx) {
        try {
            CommandSourceStack source = ctx.getSource();
            boolean newValue = BoolArgumentType.getBool(ctx, "value");

            var config = DeathTotemMod.CONFIG.getData();
            config.TotemConsumeOnlyWhenLastTotemUsed = newValue;

            if (!persist(source)) {
                return 1;
            }

            source.sendSystemMessage(Component.literal(
                    "Successfully set \"" + OPTION_CONSUME_ONLY_WHEN_LAST + "\" to " + newValue).withColor(TextColor.GRAY));

            return 1;
        } catch (Exception ex) {
            DeathTotemMod.LOGGER.error(ex);
            return 1;
        }
    }

    private int executeGetConfig(CommandContext<CommandSourceStack> ctx) {
        try {
            CommandSourceStack source = ctx.getSource();

            var config = DeathTotemMod.CONFIG.getData();

            source.sendSystemMessage(Component.empty()
                    .append(Component.literal("Totem in a Barrel Config").withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD))
                    .append(Component.literal("\n").withStyle(ChatFormatting.RESET))
                    .append(Component.literal(OPTION_ENABLE_TOTEM_CONSUME + ": ").withStyle(ChatFormatting.WHITE))
                    .append(Component.literal(String.valueOf(config.EnableTotemConsume)).withStyle(ChatFormatting.GRAY))
                    .append(Component.literal("\n").withStyle(ChatFormatting.RESET))
                    .append(Component.literal(OPTION_CONSUME_ONLY_WHEN_LAST + ": ").withStyle(ChatFormatting.WHITE))
                    .append(Component.literal(String.valueOf(config.TotemConsumeOnlyWhenLastTotemUsed)).withStyle(ChatFormatting.GRAY))
            );

            var configs = config.PlayerOverrides;
            var playerList = ctx.getSource().getServer().getPlayerList();

            for (ModConfig.PlayerOverrides override : configs) {
                if (override.TotemConsumeOnlyWhenLastTotemUsed == null
                        && override.EnableTotemConsume == null
                        && override.NameOfTriggeringTotem == null) {
                    continue;
                }

                var targetPlayer = playerList.getPlayer(override.PlayerUUID);

                String displayName = (targetPlayer != null)
                        ? targetPlayer.getGameProfile().name()
                        : override.PlayerUUID.toString();

                var component = Component.empty()
                        .append(Component.literal(displayName + " override's:").withStyle(ChatFormatting.AQUA))
                        .append(Component.literal("\n").withStyle(ChatFormatting.RESET));

                if (override.EnableTotemConsume != null) {
                    component.append(Component.literal(OPTION_ENABLE_TOTEM_CONSUME + ": " + override.EnableTotemConsume).withStyle(ChatFormatting.GRAY))
                            .append(Component.literal("\n").withStyle(ChatFormatting.RESET));
                }

                if (override.TotemConsumeOnlyWhenLastTotemUsed != null) {
                    component.append(Component.literal(OPTION_CONSUME_ONLY_WHEN_LAST + ": " + override.TotemConsumeOnlyWhenLastTotemUsed).withStyle(ChatFormatting.GRAY))
                            .append(Component.literal("\n").withStyle(ChatFormatting.RESET));
                }

                if (override.NameOfTriggeringTotem != null) {
                    component.append(Component.literal(OPTION_TRIGGERING_TOTEM_NAME + ": " + override.NameOfTriggeringTotem).withStyle(ChatFormatting.GRAY))
                            .append(Component.literal("\n").withStyle(ChatFormatting.RESET));
                }

                source.sendSystemMessage(component);
            }

            return 1;
        } catch (Exception ex) {
            DeathTotemMod.LOGGER.error("Error executing get config command", ex);
            return 1;
        }
    }

    private int executeBoolPlayerOverride(CommandContext<CommandSourceStack> ctx, String option) {
        try {
            CommandSourceStack source = ctx.getSource();
            ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
            boolean newValue = BoolArgumentType.getBool(ctx, "value");

            var config = DeathTotemMod.CONFIG.getData();

            if (!config.AddOrUpdatePlayerOverride(player.getUUID(), option, newValue)) {
                source.sendSystemMessage(Component.literal("Could not add player override. Please try again.").withColor(TextColor.RED));
                return 1;
            }

            if (!persist(source)) {
                return 1;
            }

            source.sendSystemMessage(Component.literal(
                    "Successfully set \"" + option + "\" to " + newValue
                            + " for player: " + player.getPlainTextName()).withColor(TextColor.GRAY));

            return 1;
        } catch (Exception ex) {
            DeathTotemMod.LOGGER.error(ex);
            return 1;
        }
    }

    private int executeTriggeringTotemNameOverride(CommandContext<CommandSourceStack> ctx) {
        try {
            CommandSourceStack source = ctx.getSource();
            ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
            String newValue = StringArgumentType.getString(ctx, "value");

            var config = DeathTotemMod.CONFIG.getData();

            if (!config.AddOrUpdatePlayerOverride(player.getUUID(), OPTION_TRIGGERING_TOTEM_NAME, newValue)) {
                source.sendSystemMessage(Component.literal("Could not add player override. Please try again.").withColor(TextColor.RED));
                return 1;
            }

            if (!persist(source)) {
                return 1;
            }

            source.sendSystemMessage(Component.literal(
                    "Successfully set \"" + OPTION_TRIGGERING_TOTEM_NAME + "\" to \"" + newValue
                            + "\" for player: " + player.getPlainTextName()).withColor(TextColor.GRAY));

            return 1;
        } catch (Exception ex) {
            DeathTotemMod.LOGGER.error(ex);
            return 1;
        }
    }

    private boolean persist(CommandSourceStack source) {
        if (DeathTotemMod.CONFIG.saveSave()) {
            return true;
        }
        source.sendSystemMessage(Component.literal(
                "Could not persist new value. After next restart, it might be gone :(").withColor(TextColor.RED));
        return false;
    }
}