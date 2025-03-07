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
    private static final Map<UUID, Vec3d> pushingPlayers = new ConcurrentHashMap<>();

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            server.getPlayerManager().getPlayerList().forEach(player -> {
                UUID playerId = player.getUuid();
                if (pushingPlayers.containsKey(playerId)) {
                    Vec3d pushVec = pushingPlayers.get(playerId);
                    World world = player.getEntityWorld();

                    // Posición actual del jugador
                    double playerX = player.getX();
                    double playerY = player.getY();
                    double playerZ = player.getZ();

                    // Coordenadas del siguiente paso
                    double nextX = playerX + pushVec.x;
                    double nextZ = playerZ + pushVec.z;

                    // Definir las alturas de las piernas y el torso
                    double legHeight = playerY + 0.5; // Altura de las piernas
                    double torsoHeight = playerY + 1.5; // Altura del torso

                    // Crear posiciones de bloque para las piernas y el torso
                    BlockPos legPos = new BlockPos((int) nextX, (int) legHeight, (int) nextZ);
                    BlockPos torsoPos = new BlockPos((int) nextX, (int) torsoHeight, (int) nextZ);

                    // Obtener los estados de bloque en las posiciones de las piernas y el torso
                    BlockState legState = world.getBlockState(legPos);
                    BlockState torsoState = world.getBlockState(torsoPos);

                    // Verificar si hay colisiones en las posiciones de las piernas o el torso
                    boolean legCollision = !legState.getCollisionShape(world, legPos).isEmpty();
                    boolean torsoCollision = !torsoState.getCollisionShape(world, torsoPos).isEmpty();

                    // Si hay colisión en las piernas o el torso, detener el empuje
                    if (legCollision || torsoCollision) {
                        pushingPlayers.remove(playerId);
                    } else {
                        // Aplicar la velocidad de empuje al jugador
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
