package com.zkl.memruss.control.note_old;

public class VersionException extends RuntimeException {
    private int requestedVersion;
    private int currentVersion;

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
