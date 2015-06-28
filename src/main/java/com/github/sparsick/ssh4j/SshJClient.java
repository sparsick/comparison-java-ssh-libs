package com.github.sparsick.ssh4j;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.sftp.RemoteResourceFilter;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.xfer.FileSystemFile;

public class SshJClient implements SshClient {

    private String user;
    private String password;
    private Path privateKey;
    private Path knownHosts;
    private SSHClient sshClient;

    @Override
    public void authUserPassword(String user, String password) {
        this.user = user;
        this.password = password;
    }

    @Override
    public void authUserPublicKey(String user, Path privateKey) {
        this.user = user;
        this.privateKey = privateKey;
    }

    @Override
    public void setKnownHosts(Path knownHosts) {
        this.knownHosts = knownHosts;
    }

    @Override
    public void connect(String host) throws IOException {
        sshClient = new SSHClient();
        if (knownHosts == null) {
            sshClient.loadKnownHosts();
        } else {
            sshClient.loadKnownHosts(knownHosts.toFile());
        }

        sshClient.connect(host);

        if (privateKey != null) {
            sshClient.authPublickey(user, privateKey.toString());
        } else if (password != null) {
            sshClient.authPassword(user, password);
        } else {
            throw new RuntimeException("Either privateKey nor password is set. Please call one of the auth method.");
        }
    }

    @Override
    public void disconnect() {
        try {
            close();
        } catch (Exception ex) {
            // Ignore because disconnect is quietly
        }
    }

    @Override
    public void download(String remotePath, Path local) throws IOException {
        try (SFTPClient sftpClient = sshClient.newSFTPClient()) {
            sftpClient.get(remotePath, new FileSystemFile(local.toFile()));
        }
    }

    @Override
    public void upload(Path local, String remotePath) throws IOException {
        try (SFTPClient sFTPClient = sshClient.newSFTPClient()) {
            sFTPClient.put(new FileSystemFile(local.toFile()), remotePath);
        }
    }

    @Override
    public void move(String oldRemotePath, String newRemotePath) throws IOException {
        try (SFTPClient sftpClient = sshClient.newSFTPClient()) {
            sftpClient.rename(oldRemotePath, newRemotePath);
        }
    }

    @Override
    public void copy(String oldRemotePath, String newRemotePath) throws IOException {
        try (SFTPClient sftpClient = sshClient.newSFTPClient()) {
            sftpClient.put(oldRemotePath, newRemotePath);
        }
    }

    @Override
    public void delete(String remotePath) throws IOException {
        try (SFTPClient sftpClient = sshClient.newSFTPClient()) {
            sftpClient.rm(remotePath);
        }
    }

    @Override
    public boolean fileExists(String remotePath) throws IOException {
        try (SFTPClient sftpClient = sshClient.newSFTPClient()) {
            return sftpClient.statExistence(remotePath) != null;
        }
    }

    @Override
    public List<String> listChildrenNames(String remotePath) throws IOException {
        return listChildrenNamesByFilter(remotePath, null);
    }

    @Override
    public List<String> listChildrenFolderNames(String remotePath) throws IOException {
        return listChildrenNamesByFilter(remotePath, RemoteResourceInfo::isDirectory);
    }

    @Override
    public List<String> listChildrenFileNames(String remotePath) throws IOException {
        return listChildrenNamesByFilter(remotePath, RemoteResourceInfo::isRegularFile);
    }

    private List<String> listChildrenNamesByFilter(String remotePath, RemoteResourceFilter remoteFolderResourceFilter) throws IOException {
        try (SFTPClient sftpClient = sshClient.newSFTPClient()) {
            List<String> children = new ArrayList<>();
            List<RemoteResourceInfo> childrenInfos = sftpClient.ls(remotePath, remoteFolderResourceFilter);
            childrenInfos.stream().forEach((childInfo) -> {
                children.add(childInfo.getName());
            });
            return children;
        }
    }

    @Override
    public void execute(String command) throws IOException {
        try (Session session = sshClient.startSession()) {
            session.exec(command);
        }
    }

    @Override
    public void close() throws Exception {
        sshClient.close();
    }
}
