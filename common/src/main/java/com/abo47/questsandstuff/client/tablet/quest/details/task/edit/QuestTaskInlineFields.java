package com.abo47.questsandstuff.client.tablet.quest.details.task.edit;

import org.lwjgl.glfw.GLFW;

import net.minecraft.world.entity.player.Player;

import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.client.tablet.controls.InlineRenameField;
import com.abo47.questsandstuff.client.tablet.controls.StyledTextFields;
import com.abo47.questsandstuff.client.tablet.controls.TextTextures;
import com.abo47.questsandstuff.client.tablet.icons.IconAtlas;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsEditController;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsTransientManager;
import com.abo47.questsandstuff.client.tablet.quest.details.task.QuestTaskEntry;
import com.abo47.questsandstuff.client.tablet.quest.details.task.QuestTaskDisplayText;
import com.abo47.questsandstuff.client.tablet.quest.details.task.QuestTaskEditActions;
import com.abo47.questsandstuff.client.tablet.quest.details.task.interaction.QuestTaskActionWidgets;
import com.abo47.questsandstuff.client.tablet.quest.details.task.interaction.QuestTaskListInteractions;
import com.abo47.questsandstuff.client.tablet.quest.details.task.model.QuestTaskSelectableRewards;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorQuestCommandClient;
import com.abo47.questsandstuff.client.tablet.state.UiState;
import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;
import com.abo47.questsandstuff.client.tablet.theme.tokens.ThemeColors;
import com.abo47.questsandstuff.client.tablet.ui.factory.UiFactory;
import com.abo47.questsandstuff.util.naming.QuestIdentity;

import com.google.gson.JsonObject;

import static com.abo47.questsandstuff.client.tablet.ui.factory.UiFactory.flatHitButton;

public final class QuestTaskInlineFields {
    private QuestTaskInlineFields() {
    }

    public static void renderTaskTitle(WidgetGroup parent, UiState state, Player player, Runnable refresh, String questId, QuestTaskEntry entry, boolean task, int x, int y, int rightX) {
        renderTaskTitle(parent, state, player, refresh, questId, entry, task, x, y, rightX, ThemeColors.TEXT_PRIMARY);
    }

    public static void renderTaskTitle(WidgetGroup parent, UiState state, Player player, Runnable refresh, String questId, QuestTaskEntry entry, boolean task, int x, int y, int rightX, int color) {
        boolean renaming = isRenamingTask(state, questId, entry.id(), task);
        int doneW = renaming ? 12 : 0;
        int fieldW = Math.max(18, rightX - x - doneW - (renaming ? 3 : 0));
        if (!renaming) {
            renderDisplayText(parent, x, y, fieldW, QuestTaskDisplayText.displayName(entry.json(), entry.type()), color, TextTexture.TextType.LEFT_HIDE);
            return;
        }
        InlineRenameField field = new InlineRenameField(
                x,
                y,
                fieldW,
                16,
                () -> state.questDetails.questDetailsTaskRenameDraft,
                value -> state.questDetails.questDetailsTaskRenameDraft = sanitizeTaskTitle(value),
                () -> {
                    commitTaskRename(player, state);
                    refresh.run();
                },
                () -> {
                    closeTaskRenameEditor(state);
                    refresh.run();
                },
                () -> {
                    if (state.questDetails.questDetailsTaskRenameOpen) {
                        commitTaskRename(player, state);
                        refresh.run();
                    }
                },
                null
        );
        field.setClientSideWidget();
        field.setCurrentString(state.questDetails.questDetailsTaskRenameDraft);
        field.setMaxStringLength(QuestIdentity.TITLE_MAX_LENGTH);
        field.setBordered(false);
        field.setTextColor(ThemeColors.TEXT_PRIMARY);
        field.setBackground(SurfaceFactory.bordered(ThemeColors.SURFACE_BASE, ThemeColors.INTERACTIVE));
        parent.addWidget(field);
        int doneX = x + fieldW + 3;
        parent.addWidget(new ImageWidget(doneX, y + 2, UiFactory.ACTION_ICON_SIZE, UiFactory.ACTION_ICON_SIZE, IconAtlas.iconTexture("add.png")));
        parent.addWidget(flatHitButton(doneX, y, doneW, 16, click -> {
            commitTaskRename(player, state);
            refresh.run();
        }));
        if (state.questDetails.questDetailsTaskRenameFocusPending) {
            state.questDetails.questDetailsTaskRenameFocusPending = false;
            field.requestFocusWhenReady();
        }
    }

    public static void renderAmountField(WidgetGroup parent, UiState state, Player player, Runnable refresh, String questId, QuestTaskEntry entry, int x, int y, int w, boolean task) {
        int count = task ? Math.max(0, entry.tag().getInt("count")) : 0;
        int amount = QuestTaskDisplayText.amount(entry.json());
        if (task && QuestTaskDisplayText.isManualTask(entry.json())) {
            QuestTaskActionWidgets.renderManualDoneButton(parent, player, refresh, questId, entry, x - 16, y, w + 16, count > 0);
            return;
        }
        if (!QuestTaskDisplayText.usesAmountField(entry.json(), task)) {
            return;
        }
        if (task && QuestTaskDisplayText.isManualXpTask(entry.json()) && !QuestDetailsEditController.canEdit(state)) {
            QuestTaskActionWidgets.renderManualXpButton(parent, player, refresh, questId, entry, x, y, w, count, amount);
            return;
        }
        if (!QuestDetailsEditController.canEdit(state)) {
            int chipX = x - (task ? 24 : 0);
            int chipW = w + (task ? 24 : 0);
            renderAmountText(parent, chipX, y, chipW, task ? count + " / " + amount : "x" + amount);
            return;
        }
        if (task) {
            String progress = count + " /";
            renderDisplayText(parent, x - 42, y, 36, progress, ThemeColors.TEXT_MUTED, TextTexture.TextType.RIGHT_HIDE);
        }
        final TextFieldWidget[] fieldRef = new TextFieldWidget[1];
        TextFieldWidget field = StyledTextFields.commitField(
                x,
                y,
                w,
                14,
                null,
                value -> {
                },
                () -> {
                    commitAmount(player, questId, entry, task, amount, fieldRef[0] == null ? "" : fieldRef[0].getRawCurrentString());
                    refresh.run();
                },
                () -> {
                },
                () -> {
                    commitAmount(player, questId, entry, task, amount, fieldRef[0] == null ? "" : fieldRef[0].getRawCurrentString());
                    refresh.run();
                }
        );
        field.setClientSideWidget();
        field.setCurrentString(Integer.toString(amount));
        field.setMaxStringLength(5);
        field.setNumbersOnly(1, 99999);
        field.setBordered(false);
        field.setBackground(SurfaceFactory.bordered(ThemeColors.SURFACE_PANEL_ALT, ThemeColors.BORDER_BASE));
        field.setTextColor(ThemeColors.TEXT_PRIMARY);
        fieldRef[0] = field;
        parent.addWidget(field);
    }

    public static boolean isRenamingTask(UiState state, String questId, String entryId, boolean task) {
        return state.questDetails.questDetailsTaskRenameOpen
                && state.questDetails.questDetailsTaskRenameTask == task
                && questId.equals(state.questDetails.questDetailsTaskRenameQuestId)
                && entryId.equals(state.questDetails.questDetailsTaskRenameId)
                && QuestDetailsEditController.canEdit(state);
    }

    public static boolean handleRenameKey(Player player, UiState state, int keyCode, boolean draftUnchanged) {
        if (!state.questDetails.questDetailsTaskRenameOpen || !QuestDetailsEditController.canEdit(state)) {
            return false;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            commitTaskRename(player, state);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            closeTaskRenameEditor(state);
            return true;
        }
        if (draftUnchanged && keyCode == GLFW.GLFW_KEY_BACKSPACE && !state.questDetails.questDetailsTaskRenameDraft.isEmpty()) {
            state.questDetails.questDetailsTaskRenameDraft = state.questDetails.questDetailsTaskRenameDraft.substring(0, state.questDetails.questDetailsTaskRenameDraft.length() - 1);
            return true;
        }
        return false;
    }

    public static boolean handleRenameChar(UiState state, char c, boolean draftUnchanged) {
        if (!draftUnchanged || !state.questDetails.questDetailsTaskRenameOpen || !QuestDetailsEditController.canEdit(state)) {
            return false;
        }
        if (Character.isISOControl(c)) {
            return false;
        }
        state.questDetails.questDetailsTaskRenameDraft = sanitizeTaskTitle(state.questDetails.questDetailsTaskRenameDraft + c);
        if (state.questDetails.questDetailsTaskRenameDraft.length() > 80) {
            state.questDetails.questDetailsTaskRenameDraft = state.questDetails.questDetailsTaskRenameDraft.substring(0, 80);
        }
        return true;
    }

    public static void openTaskRenameEditor(UiState state, String questId, String id, boolean task) {
        QuestTaskListInteractions.openRenameEditor(state, questId, id, task);
    }

    static void closeTaskRenameEditor(UiState state) {
        QuestDetailsTransientManager.closeTaskRename(state);
    }

    private static void commitAmount(Player player, String questId, QuestTaskEntry entry, boolean task, int oldAmount, String rawValue) {
        int parsed = QuestTaskDisplayText.parsePositive(rawValue, oldAmount);
        if (parsed == oldAmount) {
            return;
        }
        JsonObject next = entry.json().deepCopy();
        next.addProperty("amount", parsed);
        if (task) {
            EditorQuestCommandClient.putQuestTaskJson(player, questId, next.toString());
        } else if (QuestTaskSelectableRewards.commitDisplayAmount(player, questId, entry.id(), parsed)) {
            return;
        } else {
            EditorQuestCommandClient.putQuestRewardJson(player, questId, next.toString());
        }
    }

    private static void commitTaskRename(Player player, UiState state) {
        QuestTaskEditActions.putTaskTitle(
                player,
                state.questDetails.questDetailsTaskRenameQuestId,
                state.questDetails.questDetailsTaskRenameId,
                state.questDetails.questDetailsTaskRenameDraft,
                state.questDetails.questDetailsTaskRenameTask
        );
        closeTaskRenameEditor(state);
    }

    private static String sanitizeTaskTitle(String value) {
        return value == null ? "" : value.replace('\n', ' ').replace('\r', ' ');
    }

    private static void renderAmountText(WidgetGroup parent, int x, int y, int maxW, String text) {
        renderDisplayText(parent, x, y, Math.max(12, maxW), text, ThemeColors.TEXT_SECONDARY, TextTexture.TextType.RIGHT_HIDE);
    }

    static void renderDisplayText(WidgetGroup parent, int x, int y, int width, String text, int color, TextTexture.TextType type) {
        parent.addWidget(TextTextures.flatLiteral(x, y, Math.max(1, width), 16, text, color, type));
    }
}
