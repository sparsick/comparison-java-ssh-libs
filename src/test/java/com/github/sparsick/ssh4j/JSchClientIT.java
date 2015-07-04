package com.github.sparsick.ssh4j;


public class JSchClientIT extends SshClientIT {

    @Override
    public void setUp() {
       clientUnderTest = new JSchClient();
    }

}
