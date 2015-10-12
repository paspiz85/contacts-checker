package it.paspiz85.contacts.auth;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.FileDataStoreFactory;

public final class GoogleAuthSupport {

    public static final String API_CONTACTS_SCOPE = "https://www.google.com/m8/feeds";

    private final Set<String> authScopes;

    private final GoogleClientSecrets clientSecrets;

    private final DataStoreFactory dataStoreFactory;

    private final HttpTransport httpTransport = new NetHttpTransport();

    private final JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

    public GoogleAuthSupport(Reader clientSecretsReader, Set<String> authScopes, File authFolder) throws IOException {
        this.clientSecrets = GoogleClientSecrets.load(jsonFactory, clientSecretsReader);
        this.authScopes = authScopes;
        this.dataStoreFactory = new FileDataStoreFactory(authFolder);
    }

    /**
     * Authorizes the installed application to access user's protected data.
     * 
     * @param username
     *            user id.
     * @param authCodeRequestUrlConsumer
     *            consume the authCode request URL.
     * @param authCodeSupplier
     *            provide the authCode.
     * @return credential for user id.
     * @throws IOException
     *             authorization error.
     */
    public Credential authorize(String username, Consumer<String> authCodeRequestUrlConsumer,
            Supplier<String> authCodeSupplier) throws IOException {
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, jsonFactory,
                clientSecrets, authScopes).setDataStoreFactory(dataStoreFactory).setAccessType("offline").build();
        Credential credential = flow.loadCredential(username);
        if (credential == null) {
            GoogleAuthorizationCodeRequestUrl authCodeRequestUrl = flow.newAuthorizationUrl();
            authCodeRequestUrl.setRedirectUri(clientSecrets.getInstalled().getRedirectUris().get(0));
            authCodeRequestUrlConsumer.accept(authCodeRequestUrl.toString());
            String authCode = authCodeSupplier.get();
            GoogleAuthorizationCodeTokenRequest i = flow.newTokenRequest(authCode);
            i.setRedirectUri(clientSecrets.getInstalled().getRedirectUris().get(0));
            credential = flow.createAndStoreCredential(i.execute(), username);
        }
        return credential;
    }
}
