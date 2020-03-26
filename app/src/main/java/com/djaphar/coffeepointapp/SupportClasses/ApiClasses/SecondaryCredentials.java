package com.djaphar.coffeepointapp.SupportClasses.ApiClasses;

public class SecondaryCredentials {

    private String codeId, code;

    public SecondaryCredentials(String codeId, String code) {
        this.codeId = codeId;
        this.code = code;
    }

    public String getCodeId() {
        return codeId;
    }

    public String getCode() {
        return code;
    }

    public void setCodeId(String codeId) {
        this.codeId = codeId;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
