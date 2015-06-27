package com.github.sparsick.ssh4j;

public class SshJClientIT extends SshClientIT {

    @Override
    public void setUp() {
        clientUnderTest = new SshJClient();
    }

}
