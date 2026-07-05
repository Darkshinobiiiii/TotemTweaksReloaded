package net.pathdos.totemtweaks;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import me.shedaniel.autoconfig.AutoConfigClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.pathdos.totemtweaks.config.Gui;

public class TotemTweaksClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        registerCommands();
    }

    private void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(ClientCommands.literal("totemTweaks")
                    .executes(this::openConfigScreen)
            );

            dispatcher.register(ClientCommands.literal("simulatePop")
                    .executes(context -> {
                        simulatePop();
                        return Command.SINGLE_SUCCESS;
                    })
            );
        });
    }

    private int openConfigScreen(CommandContext<?> context) {
        Minecraft client = Minecraft.getInstance();

        client.execute(() -> {
            client.setScreenAndShow(
                    AutoConfigClient.getConfigScreen(Gui.class, null).get()
            );
        });

        return Command.SINGLE_SUCCESS;
    }

    public static void simulatePop() {
        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().gameRenderer.displayItemActivation(new ItemStack(Items.TOTEM_OF_UNDYING));
        }
    }
}
