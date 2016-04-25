package es.mediplus.ssh;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * Created by jon on 20/04/16.
 */
public class SSH {

    private String host;
    private String user;
    private String password;

    private int remotePort;
    private int localPort;

    private Session session = null;

    public SSH(String remoteHost, int remotePort, String user, String password) {
        this.host = remoteHost;
        this.remotePort = remotePort;
        this.localPort = remotePort;
        this.user = user;
        this.password = password;
    }

    public SSH(String remoteHost, int remotePort, int localPort, String user, String password) {
        this.host = remoteHost;
        this.remotePort = remotePort;
        this.localPort = localPort;
        this.user = user;
        this.password = password;
    }

    public int connect() throws JSchException {
        int assigned_port = -1;
        JSch jSch = new JSch();
        session = jSch.getSession(user, host, 22);
        session.setPassword(password);

        //Opciones de SSH
        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        config.put("Compression", "yes");
        config.put("ConnectionAttempts", "2");

        session.setConfig(config);
        session.connect();

        assigned_port = session.setPortForwardingL(localPort,
                host, remotePort);

        if (assigned_port == 0)
            return -1;
        else
            return 0;
    }

    public void disconnect() {
        if (session != null)
            session.disconnect();
    }
}
