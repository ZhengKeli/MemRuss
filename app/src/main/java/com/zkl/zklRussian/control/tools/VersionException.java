package com.zkl.zklRussian.control.tools;

public class VersionException extends RuntimeException {
    int requestedVersion;
    int currentVersion;

    public int getRequestedVersion() {
        return requestedVersion;
    }

    public int getCurrentVersion() {
        return currentVersion;
    }

    public VersionException(int requestedVersion, int currentVersion, String message){
        super(message+"\nrequestedVersion:"+requestedVersion+"  ; currentVersionCode:"+currentVersion);
        this.currentVersion=currentVersion;
        this.requestedVersion=requestedVersion;
    }
}
