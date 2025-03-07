package com.example;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ImpulsorHandler {
    // Mapa para almacenar (por UUID) los jugadores que están siendo empujados y su vector de empuje
    private static final Map<UUID, Vec3d> pushingPlayers = new ConcurrentHashMap<>();

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            server.getPlayerManager().getPlayerList().forEach(player -> {
                UUID playerId = player.getUuid();
                if (pushingPlayers.containsKey(playerId)) {
                    Vec3d pushVec = pushingPlayers.get(playerId);
                    World world = player.getEntityWorld();
                    // Se calcula la posición horizontal siguiente basada en el vector de empuje
                    int nextX = (int) Math.floor(player.getX() + pushVec.x);
                    int nextY = (int) Math.floor(player.getY());
                    int nextZ = (int) Math.floor(player.getZ() + pushVec.z);                   
                    
                    BlockPos posFeet = new BlockPos(nextX, nextY, nextZ);
                    BlockPos posHead = new BlockPos(nextX, nextY + 1, nextZ);
                    BlockState stateFeet = world.getBlockState(posFeet);
                    BlockState stateHead = world.getBlockState(posHead);
                    
                    // Si en cualquiera de las posiciones (pies o cabeza) se detecta una colisión, se asume que hay obstáculo
                    if (!stateFeet.getCollisionShape(world, posFeet).isEmpty() ||
                        !stateHead.getCollisionShape(world, posHead).isEmpty()) {
                        pushingPlayers.remove(playerId);
                    } else {
                        // Si no hay obstáculo, se aplica el empuje
                        player.setVelocity(pushVec);
                    }
                }
            });
        });
    }

    public static void addPlayer(PlayerEntity player, Vec3d pushVec) {
        pushingPlayers.put(player.getUuid(), pushVec);
    }
}
