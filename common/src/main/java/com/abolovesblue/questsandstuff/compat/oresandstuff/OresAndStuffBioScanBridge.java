package com.abolovesblue.questsandstuff.compat.oresandstuff;

import java.util.UUID;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import com.abolovesblue.questsandstuff.QuestsAndStuffMod;
import com.abolovesblue.questsandstuff.quest.QuestServiceRegistry;
import com.abolovesblue.questsandstuff.quest.runtime.signal.QuestSignalHelper;
import com.abolovesblue.questsandstuff.quest.runtime.signal.QuestSignalType;
import com.abolovesblue.questsandstuff.team.TeamManager;
import com.abolovesblue.questsandstuff.team.model.TeamData;
import com.abolovesblue.questsandstuff.team.model.TeamMember;

final class OresAndStuffBioScanBridge {
    private OresAndStuffBioScanBridge() {
    }

    static void onScan(ServerPlayer player, ResourceLocation entityId) {
        try {
            QuestSignalHelper.send(player, QuestSignalType.BIO_SCANNED, entityId.toString(), 1);
            grantToTeammates(player, entityId);
        } catch (IllegalStateException exception) {
            QuestsAndStuffMod.debugLog(
                    "[QnS] Dropped bio scan while quest services were unavailable player={} entity={}",
                    player.getUUID(),
                    entityId
            );
        }
    }

    private static void grantToTeammates(ServerPlayer player, ResourceLocation entityId) {
        TeamManager manager = new TeamManager(player.serverLevel(), QuestServiceRegistry.engine(player.server));
        TeamData team = manager.getTeam(player);
        if (team == null) {
            return;
        }
        for (TeamMember member : team.members()) {
            UUID memberId = member.uuid();
            if (!memberId.equals(player.getUUID())) {
                OresAndStuffCompat.grantTeamScan(player.serverLevel(), memberId, entityId);
            }
        }
    }
}
