package palaiologos.kamilalisp.runtime.cas.meta;

public class EvaluationResult {
    private final boolean wasSuccessful;
    private final String result;
    private final String type;

    public EvaluationResult(boolean successful, String text, String type) {
        this.wasSuccessful = successful;
        this.result = text;
        this.type = type;
    }

    public boolean isSuccessful() {
        return wasSuccessful;
    }

    public String getResult() {
        return result;
    }

    public String getType() {
        return type;
    }
}
