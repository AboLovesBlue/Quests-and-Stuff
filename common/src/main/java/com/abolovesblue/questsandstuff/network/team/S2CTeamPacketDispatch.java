package com.abolovesblue.questsandstuff.network.team;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;

import com.abolovesblue.questsandstuff.QuestsAndStuffMod;
import com.abolovesblue.questsandstuff.client.tablet.teams.ClientTeamCache;
import com.abolovesblue.questsandstuff.client.tablet.ui.factory.UiFactory;
import com.abolovesblue.questsandstuff.network.ModNetwork;
import com.abolovesblue.questsandstuff.team.model.TeamData;
import com.abolovesblue.questsandstuff.team.model.TeamMember;

final class S2CTeamPacketDispatch {
    private S2CTeamPacketDispatch() {
    }

    static void handle(CompoundTag payload) {
        TeamData team = TeamPacketHelper.fromPayload(payload);
        if (team == null) {
            ClientTeamCache.INSTANCE.clear();
            QuestsAndStuffMod.debugLog("[QnS:Team] received empty team sync");
            ModNetwork.sendToServer(new C2STeamCreatePacket());
            UiFactory.refreshActiveTablet();
            return;
        }
        List<ClientTeamCache.ClientMember> members = new ArrayList<>();
        for (TeamMember m : team.members()) {
            ClientTeamCache.ClientMember.Role role = m.role() == TeamMember.Role.OWNER
                    ? ClientTeamCache.ClientMember.Role.OWNER
                    : ClientTeamCache.ClientMember.Role.MEMBER;
            members.add(new ClientTeamCache.ClientMember(m.uuid(), m.name(), role));
        }
        ClientTeamCache.ClientTeamInfo info = new ClientTeamCache.ClientTeamInfo(
                team.teamId(), team.owner(), members, team.inviteCode(), team.inviteExpiryMs());
        ClientTeamCache.INSTANCE.setTeam(info);
        UiFactory.refreshActiveTablet();
        QuestsAndStuffMod.debugLog("[QnS:Team] received team sync: {} members={}",
                team.teamId(), team.members().size());
    }

    static void handleJoinResult(String messageKey, boolean success) {
        if (messageKey == null || messageKey.isBlank()) {
            ClientTeamCache.INSTANCE.setPendingJoinResult("", success);
        } else {
            ClientTeamCache.INSTANCE.setPendingJoinResult(I18n.get(messageKey), success);
        }
        UiFactory.refreshActiveTablet();
    }
}
