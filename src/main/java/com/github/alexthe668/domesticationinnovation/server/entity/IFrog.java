package com.github.alexthe668.domesticationinnovation.server.entity;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

public interface IFrog {

    boolean onFrogInteract(Player player, InteractionHand hand);
}
