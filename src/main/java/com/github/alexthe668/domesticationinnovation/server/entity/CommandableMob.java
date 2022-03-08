package com.github.alexthe668.domesticationinnovation.server.entity;

import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;

public interface CommandableMob {

    int getCommand();

    void setCommand(int command);

    default InteractionResult playerSetCommand(Player owner, Animal ourselves) {
        if (!owner.level.isClientSide) {
            int command = (getCommand() + 1) % 3;
            this.setCommand(command);
            owner.displayClientMessage(new TranslatableComponent("message.domesticationinnovation.command_" + command, ourselves.getName()), true);
            if(ourselves instanceof TamableAnimal){
                ((TamableAnimal)(ourselves)).setOrderedToSit(command == 1);
            }
        }
        return InteractionResult.PASS;
    }
}
