package uk.org.shonky.loadbalancer.webservices;

public abstract class Response<T> {
    private boolean error;
    private String errorMessage;
    private T result;

    public boolean isError() {
        return error;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public T getResult() {
        return result;
    }

    public abstract T run();

    public Response<T> invoke() {
        try {
            result = run();
            error = false;
        } catch(Exception e) {
            errorMessage = e.getMessage();
            error = true;
        }
        return this;
    }
}
