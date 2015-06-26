package com.github.sparsick.ssh4j;

public class VfsSftpClientIT  extends SshClientIT {

    @Override
    public void setUp() {
       clientUnderTest = new VfsSftpClient();
    }
    

}
