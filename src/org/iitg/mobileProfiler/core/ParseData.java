package org.iitg.mobileProfiler.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.ardverk.collection.Trie;
import org.ardverk.collection.PatriciaTrie;
import org.ardverk.collection.StringKeyAnalyzer;
import org.iitg.mobileProfiler.dal.PostingList;

public class ParseData {

	public Trie<String, PostingList> indexTrie;
	private Map<String, Integer> classIdMapping;
	private Map<String, Integer> urlIdMapping;
	private TextParser textParser;

	public ParseData() throws IOException {
		indexTrie = new PatriciaTrie<String, PostingList>(StringKeyAnalyzer.BYTE);
		classIdMapping = new HashMap<String, Integer>();
		urlIdMapping = new HashMap<String, Integer>();
		loadClassIds();
		loadUrlIds();
		textParser = new TextParser();
	}

	public void loadUrlIds() throws IOException {
		FileReader fileReader = new FileReader("url.list");
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		String line = "";
		int cnt = 0;
		while ((line = bufferedReader.readLine()) != null) {
			urlIdMapping.put(line.trim(), cnt);
			cnt++;
		}
		bufferedReader.close();
		fileReader.close();
	}

	public void loadClassIds() throws IOException {
		FileReader fileReader = new FileReader("classthreshold.txt");
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		String line = "";
		int cnt = 0;
		while ((line = bufferedReader.readLine()) != null) {
			classIdMapping.put(line.split("-")[0].trim().replace("/", "-"), cnt);
			cnt++;
		}
		bufferedReader.close();
		fileReader.close();
	}

	public void parseFile(File fileName) throws IOException {
		FileReader fileReader = new FileReader(fileName);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		String line = "";
		String pageContent = "";
		String currentURL = "";
		while ((line = bufferedReader.readLine()) != null) {
			if (line.contains("URL -#- ")) {
				currentURL = line.replace("URL -#- ", "").trim();
				ArrayList<String> tokens = textParser.tokenizeString(
						pageContent, true);
				indexTokens(tokens,currentURL,fileName.toString());
				pageContent = "";
			}
			line = line.replace("URL -#- ", " ").replace("TITLE -#- ", " ")
					.replace("DESC -#- ", " ");
			pageContent = pageContent + " " + line;
		}
		bufferedReader.close();
		fileReader.close();
	}

	public void indexTokens(ArrayList<String> tokens, String currentURL, String fileName) {
		if(!(this.urlIdMapping.containsKey(currentURL))){
			return;
		}
		int urlId = this.urlIdMapping.get(currentURL);
		int classId = this.classIdMapping.get(fileName.split("\\\\")[1].trim());
		for(String token : tokens){
			if(this.indexTrie.containsKey(token)){
				this.indexTrie.get(token).addDocument(urlId, classId);
			}
			else{
				PostingList postingList = new PostingList();
				postingList.addDocument(urlId, classId);
				this.indexTrie.put(token, postingList);
			}
		}
	}

	public Trie<String, PostingList> getIndexTrie() {
		return indexTrie;
	}

	public void setIndexTrie(Trie<String, PostingList> indexTrie) {
		this.indexTrie = indexTrie;
	}

	public Map<String, Integer> getClassIdMapping() {
		return classIdMapping;
	}

	public void setClassIdMapping(Map<String, Integer> classIdMapping) {
		this.classIdMapping = classIdMapping;
	}

	public Map<String, Integer> getUrlIdMapping() {
		return urlIdMapping;
	}

	public void setUrlIdMapping(Map<String, Integer> urlIdMapping) {
		this.urlIdMapping = urlIdMapping;
	}

	public TextParser getTextParser() {
		return textParser;
	}

	public void setTextParser(TextParser textParser) {
		this.textParser = textParser;
	}

}