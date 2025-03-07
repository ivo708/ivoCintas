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
    public static final Map<UUID, PushData> pushingPlayers = new ConcurrentHashMap<>();

    public static class PushData {
        public final Vec3d pushVec;
        public final double centerCoord; // Coordenada fija (X o Z) del centro del bloque
        public final BlockPos pos;

        public PushData(Vec3d pushVec, double centerCoord,BlockPos pos) {
            this.pushVec = pushVec;
            this.centerCoord = centerCoord;
			this.pos = pos;
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
                    
                    int offsetX = (int) Math.signum(pushData.pushVec.x);
                    int offsetZ = (int) Math.signum(pushData.pushVec.z);
                    BlockPos legPos = new BlockPos((int)Math.floor(playerX) + offsetX, (int)Math.floor(playerY), (int)Math.floor(playerZ) + offsetZ);
                    BlockPos torsoPos = new BlockPos((int)Math.floor(playerX) + offsetX, (int)Math.floor(playerY) + 1, (int)Math.floor(playerZ) + offsetZ);
                    BlockState legState = world.getBlockState(legPos);
                    BlockState torsoState = world.getBlockState(torsoPos);
                    boolean legCollision = !legState.getCollisionShape(world, legPos).isEmpty();
                    boolean torsoCollision = !torsoState.getCollisionShape(world, torsoPos).isEmpty();

                    if (legCollision && torsoCollision) {
                        IvoCintas.LOGGER.info("PARED DETECTADA EN: x="+ legPos.getX()+", y="+ legPos.getY()+", z="+ legPos.getZ());
                        IvoCintas.LOGGER.info("PARED DETECTADA EN: x="+ torsoPos.getX()+", y="+ torsoPos.getY()+", z="+ torsoPos.getZ());
                        player.teleport(player.getServerWorld(), newX, playerY, newZ, player.getYaw(), player.getPitch());
                        pushingPlayers.remove(playerId);
                    } else {
                        // Teletransportamos al jugador a la nueva posición
                        player.teleport(player.getServerWorld(), newX, playerY, newZ, player.getYaw(), player.getPitch());
                    }
                }
            } );
        });
    }

    public static void addPlayer(PlayerEntity player, Vec3d pushVec,BlockPos pos) {
        double centerCoord;
        if (pushVec.x != 0) {
            // Movimiento en X; fijar Z al centro del bloque actual
            centerCoord = Math.floor(player.getZ()) + 0.5;
        } else {
            // Movimiento en Z; fijar X al centro del bloque actual
            centerCoord = Math.floor(player.getX()) + 0.5;
        }
        pushingPlayers.put(player.getUuid(), new PushData(pushVec, centerCoord,pos));
    }
}

