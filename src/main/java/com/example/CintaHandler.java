package com.example;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CintaHandler {
    public static final Map<UUID, PushData> pushingPlayers = new ConcurrentHashMap<>();

    public static class PushData {
        public final Vec3d pushVec;
        public final double centerCoord; // Centro del bloque en la coordenada perpendicular
        public final BlockPos pos;        // Bloque de origen
        public int tickCount = 0;         // Contador de ticks para la interpolación
        public final double startX;       // Posición X inicial
        public final double startZ;       // Posición Z inicial
        public final float startYaw;      // Yaw inicial

        public PushData(PlayerEntity player, Vec3d pushVec, double centerCoord, BlockPos pos) {
            this.pushVec = pushVec;
            this.centerCoord = centerCoord;
            this.pos = pos;
            // Se guarda la posición y el yaw inicial al agregar al jugador
            this.startX = player.getX();
            this.startZ = player.getZ();
            this.startYaw = player.getYaw();
        }
    }

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            server.getPlayerManager().getPlayerList().forEach(player -> {
                UUID playerId = player.getUuid();
                if (pushingPlayers.containsKey(playerId)) {
                    PushData pushData = pushingPlayers.get(playerId);
                    World world = player.getEntityWorld();
                    double playerX = player.getX();
                    double playerY = player.getY();
                    double playerZ = player.getZ();
                    // Calculamos el offset (dirección de empuje) y la posición destino (centro del bloque adyacente)
                    int offsetX = (int) Math.signum(pushData.pushVec.x);
                    int offsetZ = (int) Math.signum(pushData.pushVec.z);
                    int destBlockX = pushData.pos.getX() + offsetX;
                    int destBlockZ = pushData.pos.getZ() + offsetZ;
                    double destX = destBlockX + 0.5;
                    double destZ = destBlockZ + 0.5;

                    // Calculamos el yaw objetivo basado en el vector de empuje
                    float targetYaw = (float)(Math.atan2(pushData.pushVec.z, pushData.pushVec.x) * (180F / Math.PI)) - 90.0F;
                    if(targetYaw<-90) {
                    	targetYaw+=360;
                    }
                    int totalTicks = 5;
                    if (pushData.tickCount < totalTicks) {
                        // Calculamos el progreso de la interpolación (0.0 a 1.0)
                        double progress = (double) pushData.tickCount / totalTicks;
                        // Interpolamos la posición desde la posición inicial (guardada) hasta el destino
                        double newX = MathHelper.lerp(progress, pushData.startX, destX);
                        double newZ = MathHelper.lerp(progress, pushData.startZ, destZ);
                        // Interpolamos el yaw desde el yaw inicial hasta el yaw objetivo
                        float newYaw = MathHelper.lerp((float) progress, pushData.startYaw, targetYaw);
                        player.teleport(player.getServerWorld(), newX, playerY, newZ, newYaw,0);
                        pushData.tickCount++;
                    } else {
                        // Opcional: podemos interpolar el movimiento en varios pasos para suavizarlo.
                        // Aquí se usan 5 pasos; se calcula el progreso (esto puede ajustarse según se necesite)
                        int steps = 5;
                        // Se calcula un incremento fraccional (por tick) entre la posición actual y el destino.
                        double newX = MathHelper.lerp(1.0 / steps, playerX, destX);
                        double newZ = MathHelper.lerp(1.0 / steps, playerZ, destZ);

                        // Teletransportamos al jugador a la nueva posición
                        player.teleport(player.getServerWorld(), newX, playerY, newZ, player.getYaw(),0);

                        // Si la posición actual (después del movimiento) está suficientemente cerca del destino,
                        // se ajusta exactamente y se finaliza el empuje.
                        double epsilon = 0.1;
                        if (Math.abs(newX - destX) < epsilon && Math.abs(newZ - destZ) < epsilon) {
                            player.teleport(player.getServerWorld(), destX, playerY, destZ, player.getYaw(),0);
                            pushingPlayers.remove(playerId);
                        }
                    }
                }
            });
        });
    }

    public static void addPlayer(PlayerEntity player, Vec3d pushVec, BlockPos pos) {
        double centerCoord;
        if (pushVec.x != 0) {
            // Movimiento en X; fijar Z al centro del bloque actual
            centerCoord = Math.floor(player.getZ()) + 0.5;
        } else {
            // Movimiento en Z; fijar X al centro del bloque actual
            centerCoord = Math.floor(player.getX()) + 0.5;
        }
        // Se crea el PushData almacenando la posición y yaw inicial del jugador
        pushingPlayers.put(player.getUuid(), new PushData(player, pushVec, centerCoord, pos));
    }
}
