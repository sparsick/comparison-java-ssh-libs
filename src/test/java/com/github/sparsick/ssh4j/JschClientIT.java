package com.github.sparsick.ssh4j;


public class JschClientIT extends SshClientIT {

    @Override
    public void setUp() {
       clientUnderTest = new JschClient();
    }

}
