package com.abo47.questsandstuff.chunkclaim;

import java.lang.reflect.Method;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

import com.abo47.questsandstuff.QuestsAndStuffConfig;
import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.chunkclaim.model.ClaimedChunk;
import com.abo47.questsandstuff.chunkclaim.model.TeamChunkData;
import com.abo47.questsandstuff.platform.Services;

public class ChunkClaimService {
    private final MinecraftServer server;

    public ChunkClaimService(MinecraftServer server) {
        this.server = server;
    }

    private ChunkClaimSavedData data() {
        return ChunkClaimSavedData.get(server);
    }

    public enum ClaimResult {
        OK,
        ALREADY_CLAIMED,
        LIMIT_REACHED,
        NOT_CLAIMED
    }

    public ClaimResult claim(UUID teamId, String claimedByName, ResourceLocation dim, int x, int z) {
        UUID owner = data().ownerTeamIdOf(dim, x, z);
        if (owner != null) {
            if (owner.equals(teamId)) {
                return ClaimResult.ALREADY_CLAIMED;
            }
            return ClaimResult.NOT_CLAIMED;
        }
        if (data().countClaimed(teamId) >= QuestsAndStuffConfig.chunkClaimMaxClaimedChunks()) {
            return ClaimResult.LIMIT_REACHED;
        }
        if (data().claim(teamId, dim, x, z, claimedByName)) {
            return ClaimResult.OK;
        }
        return ClaimResult.ALREADY_CLAIMED;
    }

    public ClaimResult unclaim(UUID teamId, ResourceLocation dim, int x, int z) {
        if (!data().isClaimed(teamId, dim, x, z)) {
            return ClaimResult.NOT_CLAIMED;
        }
        data().unclaim(teamId, dim, x, z);
        ServerLevel level = levelFor(dim);
        if (level != null) {
            Services.platform().setForceChunk(level, new ChunkPos(x, z), false);
        }
        return ClaimResult.OK;
    }

    public ClaimResult setForceLoaded(UUID teamId, ResourceLocation dim, int x, int z, boolean force) {
        if (!data().isClaimed(teamId, dim, x, z)) {
            return ClaimResult.NOT_CLAIMED;
        }
        boolean currently = data().isForceLoaded(teamId, dim, x, z);
        if (force && !currently) {
            if (data().countForceLoaded(teamId) >= QuestsAndStuffConfig.chunkClaimMaxForceLoadedChunks()) {
                return ClaimResult.LIMIT_REACHED;
            }
        }
        if (data().setForce(teamId, dim, x, z, force)) {
            applyForceLoad(teamId, dim, x, z, force);
            return ClaimResult.OK;
        }
        return ClaimResult.NOT_CLAIMED;
    }

    public void applyForceLoad(UUID teamId, ResourceLocation dim, int x, int z, boolean forced) {
        ServerLevel level = levelFor(dim);
        if (level != null) {
            Services.platform().setForceChunk(level, new ChunkPos(x, z), forced);
        }
    }

    public void applyAllForceLoads() {
        data().forEachForceChunk((teamId, chunk) -> applyForceLoad(teamId, chunk.dimension(), chunk.x(), chunk.z(), true));
    }

    public void suppressFire() {
        if (!QuestsAndStuffConfig.chunkClaimProtectFire()) {
            return;
        }
        Method igniteOdds = igniteOddsLookup();
        data().forEachClaimed((teamId, chunk) -> {
            ServerLevel level = levelFor(chunk.dimension());
            if (level == null) {
                return;
            }
            int baseX = chunk.x() << 4;
            int baseZ = chunk.z() << 4;
            LevelChunk loaded = level.getChunk(chunk.x(), chunk.z());
            BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
            for (int si = 0; si < loaded.getSections().length; si++) {
                if (loaded.getSections()[si].hasOnlyAir()) {
                    continue;
                }
                int baseY = level.getSectionYFromSectionIndex(si) << 4;
                for (int y = 0; y < 16; y++) {
                    for (int lx = 0; lx < 16; lx++) {
                        for (int lz = 0; lz < 16; lz++) {
                            mutable.set(baseX + lx, baseY + y, baseZ + lz);
                            if (loaded.getBlockState(mutable).getBlock() == Blocks.FIRE && burnsGround(level, mutable, igniteOdds)) {
                                level.removeBlock(mutable, false);
                            }
                        }
                    }
                }
            }
        });
    }

    private static Method igniteOddsLookup() {
        for (Method candidate : FireBlock.class.getDeclaredMethods()) {
            Class<?>[] parameterTypes = candidate.getParameterTypes();
            if (parameterTypes.length == 1 && parameterTypes[0] == BlockState.class && candidate.getReturnType() == int.class) {
                candidate.setAccessible(true);
                try {
                    if ((int) candidate.invoke(Blocks.FIRE, Blocks.OAK_PLANKS.defaultBlockState()) == 5) {
                        return candidate;
                    }
                } catch (ReflectiveOperationException | IllegalArgumentException e) {
                    QuestsAndStuffMod.LOGGER.warn("Failed probing FireBlock flammability accessor", e);
                }
            }
        }
        return null;
    }

    private static boolean burnsGround(ServerLevel level, BlockPos pos, Method igniteOdds) {
        BlockState below = level.getBlockState(pos.below());
        if (igniteOdds == null) {
            return true;
        }
        try {
            return (int) igniteOdds.invoke(Blocks.FIRE, below) > 0;
        } catch (ReflectiveOperationException | IllegalArgumentException e) {
            return true;
        }
    }

    public TeamChunkData claims(UUID teamId) {
        return data().claims(teamId);
    }

    public boolean isClaimed(UUID teamId, ResourceLocation dim, int x, int z) {
        return data().isClaimed(teamId, dim, x, z);
    }

    public boolean isForceLoaded(UUID teamId, ResourceLocation dim, int x, int z) {
        return data().isForceLoaded(teamId, dim, x, z);
    }

    public UUID ownerTeamIdOf(ResourceLocation dim, int x, int z) {
        return data().ownerTeamIdOf(dim, x, z);
    }

    public int countClaimed(UUID teamId) {
        return data().countClaimed(teamId);
    }

    public int countForceLoaded(UUID teamId) {
        return data().countForceLoaded(teamId);
    }

    public boolean removeTeam(UUID teamId) {
        return data().removeTeam(teamId);
    }

    public void removeTeamClaims(UUID teamId) {
        var d = data();
        for (ClaimedChunk c : d.claims(teamId).chunks()) {
            if (c.forceLoaded()) {
                applyForceLoad(teamId, c.dimension(), c.x(), c.z(), false);
            }
        }
        d.removeTeam(teamId);
    }

    /**
     * Moves every claim owned by {@code fromTeam} onto {@code toTeam}. Used when a player joins
     * another team: their solo claims become part of the target team (owned by its leader). The
     * claim name is rewritten to the target team's owner so the map attribution follows the leader.
     */
    public void transferClaims(UUID fromTeam, UUID toTeam, String ownerName) {
        var d = data();
        for (ClaimedChunk c : d.claims(fromTeam).chunks()) {
            d.claim(toTeam, c.dimension(), c.x(), c.z(), ownerName, c.forceLoaded());
        }
        d.removeTeam(fromTeam);
    }

    public void forEachClaim(java.util.function.BiConsumer<UUID, ClaimedChunk> consumer) {
        data().forEachClaimed(consumer);
    }

    private ServerLevel levelFor(ResourceLocation dim) {
        return server.getLevel(ResourceKey.create(Registries.DIMENSION, dim));
    }
}
