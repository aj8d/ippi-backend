package com.example.ippi.security;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GoogleTokenVerifier {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    private GoogleIdTokenVerifier verifier;

    public GoogleIdToken.Payload verifyAndParseIdToken(String idTokenString) throws IOException, GeneralSecurityException {
        if (verifier == null) {
            verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();
        }

        GoogleIdToken idToken = verifier.verify(idTokenString);
        if (idToken != null) {
            return idToken.getPayload();
        } else {
            throw new IOException("Invalid ID token");
        }
    }

    public GoogleIdToken.Payload getPayload(String idTokenString) throws IOException, GeneralSecurityException {
        return verifyAndParseIdToken(idTokenString);
    }
}

