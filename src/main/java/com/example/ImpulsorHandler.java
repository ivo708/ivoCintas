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
    private static final Map<UUID, PushData> pushingPlayers = new ConcurrentHashMap<>();

    public static class PushData {
        public final Vec3d pushVec;
        public final double centerCoord; // Coordenada fija (X o Z) del centro del bloque

        public PushData(Vec3d pushVec, double centerCoord) {
            this.pushVec = pushVec;
            this.centerCoord = centerCoord;
        }
    }

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            server.getPlayerManager().getPlayerList().forEach(player -> {
                UUID playerId = player.getUuid();
                if (pushingPlayers.containsKey(playerId)) {
                    PushData pushData = pushingPlayers.get(playerId);
                    World world = player.getEntityWorld();

                    // Posición actual del jugador
                    double playerX = player.getX();
                    double playerY = player.getY();
                    double playerZ = player.getZ();

                    // Nueva posición basada en el vector de empuje
                    double newX = playerX + pushData.pushVec.x;
                    double newZ = playerZ + pushData.pushVec.z;

                    // Mantener la alineación en la coordenada perpendicular
                    if (pushData.pushVec.x != 0) {
                        // Movimiento en X; mantener Z alineada al centro
                        newZ = pushData.centerCoord;
                    } else if (pushData.pushVec.z != 0) {
                        // Movimiento en Z; mantener X alineada al centro
                        newX = pushData.centerCoord;
                    }

                    // Comprobamos colisiones
                    double legHeight = playerY + 0.5;
                    double torsoHeight = playerY + 1.5;
                    BlockPos legPos = new BlockPos((int) (newX+(4*pushData.pushVec.x)), (int) legHeight, (int) (newZ+(4*pushData.pushVec.z)));
                    BlockPos torsoPos = new BlockPos((int) (newX+(4*pushData.pushVec.x)), (int) torsoHeight, (int) (newZ+(4*pushData.pushVec.z)));
                    BlockState legState = world.getBlockState(legPos);
                    BlockState torsoState = world.getBlockState(torsoPos);
                    boolean legCollision = !legState.getCollisionShape(world, legPos).isEmpty();
                    boolean torsoCollision = !torsoState.getCollisionShape(world, torsoPos).isEmpty();

                    if (legCollision || torsoCollision) {
                        // Al detectar colisión, se detiene el impulso
                        pushingPlayers.remove(playerId);
                    } else {
                        // Teletransportamos al jugador a la nueva posición
                        player.teleport(player.getServerWorld(), newX, playerY, newZ, player.getYaw(), player.getPitch());
                    }
                }
            });
        });
    }

    public static void addPlayer(PlayerEntity player, Vec3d pushVec) {
        double centerCoord;
        if (pushVec.x != 0) {
            // Movimiento en X; fijar Z al centro del bloque actual
            centerCoord = Math.floor(player.getZ()) + 0.5;
        } else {
            // Movimiento en Z; fijar X al centro del bloque actual
            centerCoord = Math.floor(player.getX()) + 0.5;
        }
        pushingPlayers.put(player.getUuid(), new PushData(pushVec, centerCoord));
    }
}

