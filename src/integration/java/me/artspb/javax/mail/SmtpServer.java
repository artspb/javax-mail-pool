package me.artspb.javax.mail;

import lombok.SneakyThrows;
import org.subethamail.smtp.MessageContext;
import org.subethamail.smtp.MessageHandler;
import org.subethamail.smtp.MessageHandlerFactory;
import org.subethamail.smtp.server.SMTPServer;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;

/**
 * @author Artem Khvastunov
 */
public class SmtpServer extends SMTPServer {

    public SmtpServer() {
        super(new NoOpMessageHandlerFactory());
        setPort(findFreePort());
    }

    private static class NoOpMessageHandlerFactory implements MessageHandlerFactory {
        @Override
        public MessageHandler create(MessageContext ctx) {
            return new NoOpMessageHandler();
        }
    }

    private static class NoOpMessageHandler implements MessageHandler {
        @Override
        public void from(String from) {
        }

        @Override
        public void recipient(String recipient) {
        }

        @Override
        public void data(InputStream data) {
        }

        @Override
        public void done() {
        }
    }

    @SneakyThrows(IOException.class)
    private static int findFreePort() {
        ServerSocket socket = null;
        try {
            socket = new ServerSocket(0);
            return socket.getLocalPort();
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }
}
