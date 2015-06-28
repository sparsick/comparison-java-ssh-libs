package com.github.sparsick.ssh4j;

import java.nio.file.FileSystems;


public class JschClientIT extends SshClientIT {

    @Override
    public void setUp() {
       clientUnderTest = new JschClient();
       clientUnderTest.setKnownHosts(FileSystems.getDefault().getPath("/home/skosmalla/.ssh/known_hosts"));
    }

}
