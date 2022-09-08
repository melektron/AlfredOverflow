/*
 * This file is part of the AlfredOverflow distribution (https://github.com/melektron/AlfredOverflow).
 * Copyright (c) ELEKTRON.
 */

package meteordevelopment.meteorclient.systems.modules.movement;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.mixin.PlayerMoveC2SPacketAccessor;
import meteordevelopment.meteorclient.mixin.VehicleMoveC2SPacketAccessor;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;

public class BotMovement extends Module {

    public BotMovement() {
        super(Categories.Movement, "bot-movement", "Reduces accuracy of x and z in position packets to 0.01m");
    }

    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        if (!isActive()) return;
        if (event.packet instanceof PlayerMoveC2SPacket packet) {
            double currentX = packet.getX(0.0);
            double currentZ = packet.getZ(0.0);
            double newX = ((double) ((int)(currentX * 100.0))) / 100.0;
            double newZ = ((double) ((int)(currentZ * 100.0))) / 100.0;
            ((PlayerMoveC2SPacketAccessor)packet).setX(newX);
            ((PlayerMoveC2SPacketAccessor)packet).setZ(newZ);
        }
        else if (event.packet instanceof VehicleMoveC2SPacket packet) {
            double currentX = packet.getX();
            double currentZ = packet.getZ();
            double newX = ((double) ((int)(currentX * 100.0))) / 100.0;
            double newZ = ((double) ((int)(currentZ * 100.0))) / 100.0;
            ((VehicleMoveC2SPacketAccessor)packet).setX(newX);
            ((VehicleMoveC2SPacketAccessor)packet).setZ(newZ);
        }
    }
}
