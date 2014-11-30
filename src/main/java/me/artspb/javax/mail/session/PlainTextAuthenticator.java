package me.artspb.javax.mail.session;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

/**
 * @author Artem Khvastunov
 */
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public final class PlainTextAuthenticator extends Authenticator {

    private final String username;
    private final String password;

    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(username, password);
    }
}
