package tn.zeros.zchess.engine.search;

public class SearchDebugConfig {
    private static final SearchDebugConfig INSTANCE = new SearchDebugConfig();

    private boolean metricsEnabled = false;
    private boolean verboseLogging = false;
    private boolean iterationLogging = false;
    private boolean finalSummary = false;

    private SearchDebugConfig() {
    }

    public static SearchDebugConfig getInstance() {
        return INSTANCE;
    }

    // Fluent setters
    public SearchDebugConfig enableMetrics(boolean enable) {
        this.metricsEnabled = enable;
        return this;
    }

    public SearchDebugConfig enableVerboseLogging(boolean enable) {
        this.verboseLogging = enable;
        return this;
    }

    public SearchDebugConfig enableIterationLogging(boolean enable) {
        this.iterationLogging = enable;
        return this;
    }

    public SearchDebugConfig enableFinalSummary(boolean enable) {
        this.finalSummary = enable;
        return this;
    }

    // Getters
    public boolean isMetricsEnabled() {
        return metricsEnabled;
    }

    public boolean isVerboseLogging() {
        return verboseLogging;
    }

    public boolean isIterationLogging() {
        return iterationLogging;
    }

    public boolean isFinalSummary() {
        return finalSummary;
    }

    // Enable/disable all
    public SearchDebugConfig enableAll() {
        return enableMetrics(true)
                .enableVerboseLogging(true)
                .enableIterationLogging(true)
                .enableFinalSummary(true);
    }

    public SearchDebugConfig disableAll() {
        return enableMetrics(false)
                .enableVerboseLogging(false)
                .enableIterationLogging(false)
                .enableFinalSummary(false);
    }
}