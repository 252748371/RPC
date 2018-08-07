package com.ozy.rpc.core.vo;

public class Response {

    private String id;

    private Object result;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "Response{" +
                "id='" + id + '\'' +
                ", result=" + result +
                '}';
    }
}
