package fuse.qe.tools.dao;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import fuse.qe.tools.deptree.Node;
import fuse.qe.tools.deptree.Parser;
import fuse.qe.tools.model.DependencyTreeDTO;
import fuse.qe.tools.utils.ParseUtils;
import io.searchbox.client.JestClient;

public class DependencyTreeElasticsearchDAO extends AbstractElasticsearchDAO {
	
	private static final String TYPE_NAME = "leaf";
	private static final String REGEX_VALID_LINE = "^\\s*[+|\\\\].*";
	private static final String REGEX_FIRST_LINE = "^[+].*";
	
	public DependencyTreeElasticsearchDAO(JestClient jestClient, String indexName) {
		super(jestClient, indexName, TYPE_NAME);
	}

	public void indexTreeData(String content) throws Exception {
		List<String> forrest = cleanForrest(content);
		
		for (String forrestTree : forrest) {
			StringReader stringReader = new StringReader(forrestTree);
			Parser parser = new Parser();
			Node tree = parser.parse(stringReader);
			
			try {
				indexLeafs("", tree, tree.getArtifactId());
			} catch (Exception e) {
				System.out.println(forrestTree);
				System.out.println(e);
			}
		}
	}

	private void indexLeafs(String parentPath, Node node, String rootArtifact) throws Exception {
		String path = parentPath + "/" + node.getArtifactId();
		
		DependencyTreeDTO dependencyTreeDTO = new DependencyTreeDTO(path, node.getArtifactId(), rootArtifact);
		bscOps.indexData(indexName, TYPE_NAME, dependencyTreeDTO);
		
		LinkedList<Node> childNodes = node.getChildNodes();
		
		for (Node childNode : childNodes) {
			indexLeafs(path, childNode, rootArtifact);
		}
	}

	public List<String> cleanForrest(String content) {
		String cleanTree;
		List<String> forrest = new ArrayList<String>();

		content = content.replaceAll("\\[INFO\\] ", "");

		String[] lines = ParseUtils.splitString(content);
		
		StringBuilder cleanTreeBuilder = new StringBuilder();
		
		for (int i = 0; i < lines.length-1; i++) {
			String line = lines[i]; 
			String nextLine = lines[i+1];
			if (line.matches(REGEX_VALID_LINE)) {
				cleanTreeBuilder.append(line);
				cleanTreeBuilder.append(System.getProperty("line.separator"));
			} else if (nextLine.matches(REGEX_FIRST_LINE)) {
				cleanTree = cleanTreeBuilder.toString();
				if (!cleanTree.isEmpty()) {
					forrest.add(cleanTree);
				}

				cleanTreeBuilder = new StringBuilder();
				cleanTreeBuilder.append(line);
				cleanTreeBuilder.append(System.getProperty("line.separator"));
			}
		}
		
		cleanTree = cleanTreeBuilder.toString();
		if (!cleanTree.isEmpty()) {
			forrest.add(cleanTree);
		}
		
		return forrest;
	}
}
