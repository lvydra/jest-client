package fuse.qe.tools;

import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.util.List;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.client.JestResultHandler;
import io.searchbox.core.Bulk;
import io.searchbox.core.Bulk.Builder;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.indices.CreateIndex;
import io.searchbox.indices.DeleteIndex;

public class BasicOperations {

	private JestClient jestClient;

	public BasicOperations(JestClient jestClient) {
		this.jestClient = jestClient;
	}

	public JestResult createIndex(String indexName) throws Exception {
		final Settings.Builder settings = Settings.settingsBuilder();
		settings.put("number_of_shards", 3);
		settings.put("number_of_replicas", 0);
		final JestResult result = jestClient.execute(new CreateIndex.Builder(indexName).settings(settings.build().getAsMap()).build());

		return result;
	}

	public JestResult deleteIndex(String indexName) throws Exception {
		final DeleteIndex deleteIndex = new DeleteIndex.Builder(indexName).build();
		final JestResult result = jestClient.execute(deleteIndex);

		return result;
	}

	public JestResult queryData(String indexName, String typeName, QueryBuilder query, int size) throws Exception {
		final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(query);

		final Search search = new Search.Builder(searchSourceBuilder.size(size).toString()).addIndex(indexName).addType(typeName).build();
		System.out.println(searchSourceBuilder.toString());
		final JestResult result = jestClient.execute(search);
		System.out.println(result.getJsonString());

		return result;
	}

	public JestResult queryData(String indexName, String typeName, String query) throws IOException {
		Search search = new Search.Builder(query).addIndex(indexName).addType(typeName).build();
		System.out.println(query);
		JestResult result = jestClient.execute(search);
		System.out.println(result.getJsonString());
		
		return result;
	}

	public JestResult indexData(String indexName, String typeName, Object source) throws Exception {
		final Index index = new Index.Builder(source).index(indexName).type(typeName).build();
		final JestResult result = jestClient.execute(index);

		return result;
	}

	public void indexDataAsync(String indexName, String typeName, Object source) throws Exception {
		final Index index = new Index.Builder(source).index(indexName).type(typeName).build();
		jestClient.executeAsync(index, new JestResultHandler<JestResult>() {
			public void failed(Exception e) {
				System.out.println("Indexing failed: " + e);
			}

			public void completed(JestResult result) {
				System.out.println("Indexing completed " + result.getJsonString());
			}
		});
	}

	public JestResult indexDataBulk(String indexName, String typeName, List<Object> sources) throws Exception {
		final Builder bulkIndexBuilder = new Bulk.Builder();
		for (Object source : sources) {
			bulkIndexBuilder.addAction(new Index.Builder(source).index(indexName).type(typeName).build());
		}
		final JestResult result = jestClient.execute(bulkIndexBuilder.build());

		return result;
	}
	
	public JestClient getClient() {
		return jestClient;
	}

	public void setClient(JestClient jestClient) {
		this.jestClient = jestClient;
	}

}