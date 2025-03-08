package com.example;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
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
        public int tickCount = 0; // Contador de ticks desde que se añade el jugador
        public final float startYaw;      // Yaw inicial

        public PushData(PlayerEntity player,Vec3d pushVec, double centerCoord, BlockPos pos) {
            this.pushVec = pushVec;
            this.centerCoord = centerCoord;
            this.pos = pos;
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
                    float playerYaw = player.getYaw();
                    float totalTicks=7;
                	IvoCintas.LOGGER.info("Ticks: "+pushData.tickCount);

                    if (pushData.tickCount < totalTicks) {
        	            double targetX = pushData.pos.getX() + 0.5;
        	            double targetZ = pushData.pos.getZ() + 0.5;
                        float targetYaw = (float)(Math.atan2(pushData.pushVec.z, pushData.pushVec.x) * (180F / Math.PI)) - 90.0F;
                        if(targetYaw<-90) {
                        	targetYaw+=360;
                        }
        	                double progress = (double) pushData.tickCount / totalTicks;
        	                double interpolatedX = MathHelper.lerp(progress, playerX, targetX);
        	                double interpolatedZ = MathHelper.lerp(progress, playerZ, targetZ);
        	            	IvoCintas.LOGGER.info("TargetYaw: "+targetYaw);
                            float deltaYaw = MathHelper.wrapDegrees(targetYaw - pushData.startYaw);
                            float newYaw = (float) (pushData.startYaw + deltaYaw * progress);        	                
                        player.teleport(player.getServerWorld(), interpolatedX, playerY, interpolatedZ, (float) newYaw, 0);
                        pushData.tickCount++;
                    } else {
                        // Nueva posición basada en el vector de empuje
                        double newX = playerX + pushData.pushVec.x;
                        double newZ = playerZ + pushData.pushVec.z;

                        // Mantener la alineación en la coordenada perpendicular
                        if (pushData.pushVec.x != 0) {
                            // Movimiento en X; mantener Z alineada al centro
                            newZ = pushData.pos.getZ()+0.5;
                        } else if (pushData.pushVec.z != 0) {
                            // Movimiento en Z; mantener X alineada al centro
                            newX = pushData.pos.getX()+0.5;
                        }
                        
                        int offsetX = (int) Math.signum(pushData.pushVec.x);
                        int offsetZ = (int) Math.signum(pushData.pushVec.z);
                        BlockPos legPos = new BlockPos((int)Math.floor(playerX) + offsetX, (int)Math.floor(playerY), (int)Math.floor(playerZ) + offsetZ);
                        BlockPos torsoPos = new BlockPos((int)Math.floor(playerX) + offsetX, (int)Math.floor(playerY) + 1, (int)Math.floor(playerZ) + offsetZ);
                        BlockState legState = world.getBlockState(legPos);
                        BlockState torsoState = world.getBlockState(torsoPos);
                        boolean legCollision = !legState.getCollisionShape(world, legPos).isEmpty();
                        boolean torsoCollision = !torsoState.getCollisionShape(world, torsoPos).isEmpty();
                        float newYaw = (float)(Math.atan2(pushData.pushVec.z, pushData.pushVec.x) * (180F / Math.PI)) - 90.0F;

                        if (legCollision && torsoCollision) {
                        	IvoCintas.LOGGER.info("PARED DETECTADA");

                        	player.teleport(player.getServerWorld(), newX+ (pushData.pushVec.x*0.66), playerY, newZ+(pushData.pushVec.z*0.66), newYaw, 0);
                            pushingPlayers.remove(playerId);
                        } else {
                            // Teletransportamos al jugador a la nueva posición
                        	player.teleport(player.getServerWorld(),newX, playerY, newZ, newYaw, 0);
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
        pushingPlayers.put(player.getUuid(), new PushData(player,pushVec, centerCoord, pos));
    }
}
