package fuse.qe.tools;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import com.opencsv.CSVReader;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import fuse.qe.tools.model.TestExceptionDTO;
import fuse.qe.tools.utils.ElasticClientUtils;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.JestResult;
import io.searchbox.client.config.HttpClientConfig;

public final class ElasticApp {

	private static final String TYPE_NAME = "error";
	private static final String INDEX_NAME = "error_db";

	private ElasticApp() {
	}

	public static void main(String[] args) throws IOException {
		try {
			List<Object> sources = readRecordsFromCsv("/home/lvydra/Stažené/error_stack_tace.csv");
			
			TestExceptionDTO randomRec = (TestExceptionDTO) sources.get(600);
			
			ElasticClientUtils elasticClientUtils = new ElasticClientUtils("http://localhost:9200", "error_db", "error");

			//elasticClientUtils.deleteIndex();
			//elasticClientUtils.indexData(sources);
			
			//Thread.sleep(2000);
			
			Integer groupId = elasticClientUtils.findGroupId(randomRec, 10, "98%");
			
			//elasticClientUtils.updateElasticDB(randomRec);
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
				final TestExceptionDTO record = new TestExceptionDTO(line[0], line[1], line[2]);
				sources.add(record);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return sources;
	}
}
