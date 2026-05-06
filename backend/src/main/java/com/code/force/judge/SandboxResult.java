package com.code.force.judge;

public class SandboxResult {

    public final String output;
    public final boolean timedOut;
    public final boolean oomKilled;

    public SandboxResult(String output, boolean timedOut, boolean oomKilled) {
        this.output = output;
        this.timedOut = timedOut;
        this.oomKilled = oomKilled;
    }
}
