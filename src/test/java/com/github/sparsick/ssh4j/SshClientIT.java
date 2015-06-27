package com.github.sparsick.ssh4j;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;

public abstract class SshClientIT {

    private static final String SSH_USER = "vagrant";
    private static final String SSH_HOST = "192.168.33.10";

    protected SshClient clientUnderTest;

    @Before
    public abstract void setUp();

    @After
    public void tearDown() {
        clientUnderTest.disconnect();
    }

    @Test
    public void authUserPasswordAndConnect() throws IOException {
        clientUnderTest.authUserPassword(SSH_USER, "vagrant");
        clientUnderTest.connect(SSH_HOST);
    }

    @Test
    public void authUserPublicKeyAndConnect() throws IOException {
        clientUnderTest.authUserPublicKey(SSH_USER, FileSystems.getDefault().getPath("src/test/resources/id_rsa"));
        clientUnderTest.connect(SSH_HOST);
    }

    @Test
    public void listChildrenNames() throws IOException {
        authUserPasswordAndConnect();

        List<String> children = clientUnderTest.listChildrenNames("/home");
        assertThat(children.size()).isGreaterThan(0);
    }

    @Test
    public void listChildrenFolderNames() throws IOException {
        authUserPasswordAndConnect();

        List<String> children = clientUnderTest.listChildrenFolderNames("/home");
        assertThat(children.size()).isGreaterThan(0);
    }

    @Test
    public void listChildrenFileNames() throws IOException {
        authUserPasswordAndConnect();

        List<String> children = clientUnderTest.listChildrenFileNames("/home");
        assertThat(children.size()).isEqualTo(0);
    }

    @Test
    public void uploadAndListFile() throws IOException {
        authUserPasswordAndConnect();
        String remoteDirPath = "/home/vagrant";
        String remoteFilePath = remoteDirPath + "/test.txt";

        clientUnderTest.upload(FileSystems.getDefault().getPath("src/test/resources/test.txt"), remoteFilePath);
        assertThat(clientUnderTest.listChildrenFileNames(remoteDirPath)).contains("test.txt");
    }

    @Test
    public void uploadAndDownloadFile() throws IOException {
        authUserPasswordAndConnect();
        String remotePath = "/home/vagrant/test1.txt";
        Path localPath = FileSystems.getDefault().getPath("target/test1.txt");

        clientUnderTest.upload(FileSystems.getDefault().getPath("src/test/resources/test.txt"), remotePath);
        clientUnderTest.download(remotePath, localPath);
        assertThat(localPath).exists();
    }

    @Test
    public void uploadMoveAndListFile() throws IOException {
        authUserPasswordAndConnect();
        String remotePath = "/home/vagrant/test2.txt";
        String remoteMovePath = "/home/vagrant/test3.txt";

        clientUnderTest.upload(FileSystems.getDefault().getPath("src/test/resources/test.txt"), remotePath);
        clientUnderTest.move(remotePath, remoteMovePath);
        List<String> children = clientUnderTest.listChildrenFileNames("/home/vagrant");
        assertThat(children).contains("test3.txt");
    }

    @Test
    public void uploadAndDeleteFile() throws IOException {
        authUserPasswordAndConnect();
        String remotePath = "/home/vagrant/test4.txt";

        clientUnderTest.upload(FileSystems.getDefault().getPath("src/test/resources/test.txt"), remotePath);
        clientUnderTest.delete(remotePath);
        List<String> children = clientUnderTest.listChildrenFileNames("/home/vagrant");
        assertThat(children).doesNotContain("test4.txt");
    }

}
