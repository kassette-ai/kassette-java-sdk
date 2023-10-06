package ai.kassette.sdk.analytics;

public final class AnalyticsVersion {
    private AnalyticsVersion() {
        throw new AssertionError("No instances allowed.");
    }

    public static String get() {
        return "${project.version}";
    }
}