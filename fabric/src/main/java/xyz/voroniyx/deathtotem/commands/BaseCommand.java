package xyz.voroniyx.deathtotem.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;

public abstract class BaseCommand {
    public abstract int execute(CommandContext<CommandSourceStack> ctx) throws Exception;
    public abstract void register(CommandDispatcher<CommandSourceStack> dispatcher);
}
