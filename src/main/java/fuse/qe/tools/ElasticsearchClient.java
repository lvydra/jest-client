package fuse.qe.tools;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;

public class ElasticsearchClient {
	
	private JestClientFactory factory = new JestClientFactory();

	private HttpClientConfig clientConfig;

	private JestClient jestClient;

	private BasicOperations bscOps;
	
	public ElasticsearchClient(String url, Integer port, String user, String pwd) {
		BasicCredentialsProvider customCredentialsProvider = new BasicCredentialsProvider();
		customCredentialsProvider.setCredentials(
				new AuthScope(url, port),
				new UsernamePasswordCredentials(user, pwd)
		);

		clientConfig = new HttpClientConfig.Builder(url)
				.credentialsProvider(customCredentialsProvider)
				.multiThreaded(true)
				.build();

		factory.setHttpClientConfig(clientConfig);

		jestClient = factory.getObject();

		bscOps = new BasicOperations(jestClient);
	}

	public ElasticsearchClient(String urlWithPort) {
		clientConfig = new HttpClientConfig.Builder(urlWithPort)
				.multiThreaded(true)
				.build();

		factory.setHttpClientConfig(clientConfig);

		jestClient = factory.getObject();

		bscOps = new BasicOperations(jestClient);
	}
	
	public JestClientFactory getFactory() {
		return factory;
	}

	public void setFactory(JestClientFactory factory) {
		this.factory = factory;
	}

	public HttpClientConfig getClientConfig() {
		return clientConfig;
	}

	public void setClientConfig(HttpClientConfig clientConfig) {
		this.clientConfig = clientConfig;
	}

	public JestClient getJestClient() {
		return jestClient;
	}

	public void setJestClient(JestClient jestClient) {
		this.jestClient = jestClient;
	}
	
	public BasicOperations getBscOps() {
		return bscOps;
	}

	public void setBscOps(BasicOperations bscOps) {
		this.bscOps = bscOps;
	}
}
