package it.paspiz85.contacts;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.Api;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseOAuthProvider {

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	protected final OAuthService service;

	private Token token;

	private final String TOKEN_FILE = getClass().getSimpleName() + "_token.txt";

	public BaseOAuthProvider(Class<? extends Api> apiClass, String apiKey, String apiSecret) {
		service = new ServiceBuilder().provider(apiClass).apiKey(apiKey).apiSecret(apiSecret).build();
	}

	protected final Token getToken() {
		if (token == null) {
			try {
				token = readToken();
			} catch (Exception e) {
				token = requestToken();
				try {
					writeToken(token);
				} catch (IOException e1) {
					logger.error("unable to save token", e1);
				}
			}
		}
		return token;
	}

	private Token readToken() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(TOKEN_FILE));
		Token token = new Token(reader.readLine(), reader.readLine());
		reader.close();
		return token;
	}

	private Token requestToken() {
		Scanner in = new Scanner(System.in);
		Token requestToken = service.getRequestToken();
		System.out.println("Now go and authorize Scribe here:");
		System.out.println(service.getAuthorizationUrl(requestToken));
		System.out.println("And paste the verifier here");
		System.out.print(">>");
		Verifier verifier = new Verifier(in.nextLine());
		Token accessToken = service.getAccessToken(requestToken, verifier);
		in.close();
		logger.info("accessToken is " + accessToken);
		return accessToken;
	}

	private void writeToken(Token token) throws IOException {
		PrintWriter writer = new PrintWriter(new FileWriter(TOKEN_FILE));
		writer.println(token.getToken());
		writer.println(token.getSecret());
		writer.close();
	}

}
