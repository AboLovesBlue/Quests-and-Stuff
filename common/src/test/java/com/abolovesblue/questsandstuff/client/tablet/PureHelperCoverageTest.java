package com.abolovesblue.questsandstuff.client.tablet;import com.abolovesblue.questsandstuff.client.tablet.quest.canvas.render.card.QuestCardBackgroundRendererTest;
import com.abolovesblue.questsandstuff.client.tablet.quest.canvas.render.card.QuestMiniCardRendererTest;
import com.abolovesblue.questsandstuff.client.tablet.quest.canvas.render.geometry.CanvasLayerOrderingTest;
import com.abolovesblue.questsandstuff.client.tablet.quest.canvas.transform.CanvasTransformSessionsTest;


import java.util.Map;

import org.junit.jupiter.api.Test;

import static java.util.Map.entry;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class PureHelperCoverageTest {
    @Test
    void fastCommonCoverageSuitesStayPresentForTabletRefactors() {
        Map<String, String> coverage = Map.ofEntries(
                entry("modal sessions", "com.abolovesblue.questsandstuff.client.tablet.ui.ModalStateTest"),
                entry("picker sessions", "com.abolovesblue.questsandstuff.client.tablet.modal.ModalPickerStatesTest"),
                entry("settings descriptors", "com.abolovesblue.questsandstuff.client.tablet.modal.SettingsTabDescriptorsTest"),
                entry("transform sessions", "com.abolovesblue.questsandstuff.client.tablet.quest.canvas.transform.CanvasTransformSessionsTest"),
                entry("sync keys and chunks", "com.abolovesblue.questsandstuff.quest.sync.SyncPayloadBuilderTest"),
                entry("client sync inbox", "com.abolovesblue.questsandstuff.client.sync.packet.ClientSyncInboxTest"),
                entry("optimistic mutations", "com.abolovesblue.questsandstuff.client.sync.mutation.ClientOptimisticMutationsTest"),
                entry("text field builders", "com.abolovesblue.questsandstuff.client.tablet.controls.StyledTextFieldsTest"),
                entry("tablet state queries", "com.abolovesblue.questsandstuff.client.tablet.ui.StateQueriesTest"),
                entry("quest details picker sessions", "com.abolovesblue.questsandstuff.client.tablet.quest.details.QuestDetailsPickerSessionTest"),
                entry("recipe choice index", "com.abolovesblue.questsandstuff.client.tablet.modal.RecipeChoiceIndexTest"),
                entry("recipe picker mode controller", "com.abolovesblue.questsandstuff.client.tablet.modal.RecipePickerModeControllerTest"),
                entry("recipe viewer capabilities", "com.abolovesblue.questsandstuff.client.compat.recipeviewer.RecipeViewerCapabilityMatrixTest"),
                entry("recipe viewer selection rules", "com.abolovesblue.questsandstuff.client.compat.recipeviewer.RecipeViewerSelectionUtilsTest"),
                entry("prerequisite connection model", "com.abolovesblue.questsandstuff.client.tablet.quest.prerequisite.PrerequisiteConnectionModelTest"),
                entry("shared smart snap engine", "com.abolovesblue.questsandstuff.client.tablet.quest.canvas.snap.CanvasSnapEngineTest"),
                entry("main canvas smart snap adapter", "com.abolovesblue.questsandstuff.client.tablet.quest.canvas.viewport.CanvasSmartSnapperTest"),
                entry("quest card background defaults", "com.abolovesblue.questsandstuff.client.tablet.quest.canvas.render.card.QuestCardBackgroundRendererTest"),
                entry("mini quest card renderer", "com.abolovesblue.questsandstuff.client.tablet.quest.canvas.render.card.QuestMiniCardRendererTest"),
                entry("canvas layer ordering", "com.abolovesblue.questsandstuff.client.tablet.quest.canvas.render.geometry.CanvasLayerOrderingTest"),
                entry("canvas layer selection", "com.abolovesblue.questsandstuff.client.tablet.quest.canvas.selection.CanvasLayerSelectionTest"),
                entry("description layer selection", "com.abolovesblue.questsandstuff.client.tablet.quest.details.description.QuestDetailsDescriptionSelectionStateTest"),
                entry("context action tones", "com.abolovesblue.questsandstuff.client.tablet.contextmenu.ContextActionToneTest"),
                entry("context action layout", "com.abolovesblue.questsandstuff.client.tablet.contextmenu.ContextActionLayoutTest"),
                entry("task json diagnostics", "com.abolovesblue.questsandstuff.client.tablet.quest.details.task.model.QuestTaskJsonFactoryTest"),
                entry("task defaults", "com.abolovesblue.questsandstuff.client.tablet.quest.details.task.model.QuestTaskTypeCatalogTest")
        );

        coverage.forEach((area, className) ->
                assertDoesNotThrow(() -> Class.forName(className), area + " coverage class is missing"));
    }
}
