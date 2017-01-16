package com.kaw;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import com.opencsv.CSVReader;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.JestResult;
import io.searchbox.client.config.HttpClientConfig;

public final class App {

	private static final String TYPE_NAME = "error";
	private static final String INDEX_NAME = "error_db";

	private App() {
	}

	public static void main(String[] args) throws IOException {
		try {
			final HttpClientConfig clientConfig = new HttpClientConfig.Builder("http://localhost:9200").multiThreaded(true).build();

			final JestClientFactory factory = new JestClientFactory();
			factory.setHttpClientConfig(clientConfig);
			final JestClient jestClient = factory.getObject();

			final BasicOperations bscOps = new BasicOperations(jestClient);

			final RecordDTO record1 = new RecordDTO("Record 1 - test 1");
			final RecordDTO record2 = new RecordDTO("Record 2 - test 2");

			final List<Object> sources = readRecordsFromCsv("error_stack_tace.csv");

			try {
				bscOps.deleteIndex(INDEX_NAME);

				bscOps.createIndex(INDEX_NAME);

				bscOps.indexData(INDEX_NAME, TYPE_NAME, record1);

				bscOps.indexDataAsync(INDEX_NAME, TYPE_NAME, record2);

				bscOps.indexDataBulk(INDEX_NAME, TYPE_NAME, sources);

				Thread.sleep(2000);

				final RecordDTO randomRec = (RecordDTO) sources.get(120);

				final QueryBuilder query = QueryBuilders.matchPhraseQuery("record", randomRec.getRecord());

				final JestResult result = bscOps.queryData(INDEX_NAME, TYPE_NAME, query);

				final List<RecordDTO> records = result.getSourceAsObjectList(RecordDTO.class);

				for (RecordDTO record : records) {
					System.out.println(record);
				}
			} finally {
				jestClient.shutdownClient();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static List<Object> readRecordsFromCsv(String path) {

		final List<Object> sources = new ArrayList<Object>();

		final String csvFile = path;

		CSVReader reader = null;
		try {
			reader = new CSVReader(new FileReader(csvFile));
			String[] line;
			line = reader.readNext();
			while ((line = reader.readNext()) != null) {
				final RecordDTO record = new RecordDTO(line[1]);
				sources.add(record);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return sources;
	}
}
