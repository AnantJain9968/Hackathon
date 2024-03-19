package com.st.trex.serviceimpl;
import com.jcraft.jsch.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
public class FileServiceImpl {
	
	
	
	public static void main(String[] args) {/*
        String localFilePath = "C:\\Users\\jainanan\\Music\1.txt";
        String remoteFilePath = "/path/to/remote/file.txt";
        String remoteHost = "dlhsx00002.dlh.st.com";
        String remoteUser = "username";
        String remotePassword = "password";

        JSch jsch = new JSch();
        Session session = null;
        ChannelSftp sftpChannel = null;
        try {
            // Connect to the remote host
            session = jsch.getSession(remoteUser, remoteHost, 22);
            session.setPassword(remotePassword);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            // Create an SFTP channel
            sftpChannel = (ChannelSftp) session.openChannel("sftp");
            sftpChannel.connect();

            // Transfer the file from the local machine to the remote machine
            File localFile = new File(localFilePath);
            FileInputStream fis = new FileInputStream(localFile);
            sftpChannel.put(fis, remoteFilePath);
            fis.close();

            System.out.println("File transfer complete.");
        } catch (JSchException | SftpException | IOException e) {
            e.printStackTrace();
        } finally {
            if (sftpChannel != null) {
                sftpChannel.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        }
    */}
}
