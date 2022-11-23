package server.lib.model;

public class ErrorModel {

    private String line;
    private String method;
    private String className;
    private String reason;
    private String message;

    public ErrorModel(String line, String method, String className, String reason, String message) {
        this.line = line;
        this.method = method;
        this.className = className;
        this.reason = reason;
        this.message = message;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
