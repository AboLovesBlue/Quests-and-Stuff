package com.abo47.questsandstuff.client.tablet.quest.details.task;

public final class TaskSections {
    private TaskSections() {
    }

    public static String normalize(String kind) {
        if (kind == null) {
            return "";
        }
        if ("tasks".equals(kind)) {
            return "task";
        }
        if ("rewards".equals(kind)) {
            return "reward";
        }
        return kind;
    }

    public static boolean isTask(String kind) {
        return "task".equals(normalize(kind));
    }

    public static boolean isReward(String kind) {
        return "reward".equals(normalize(kind));
    }
}
