package org.iitg.mobileProfiler.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.iitg.mobileProfiler.db.DatabaseConnector;
import org.tartarus.porter.Stemmer;

/**
 * This class takes care of all string related processing. Stemming, stop word
 * removal, punctuation. Everything goes in here.
 * 
 * This class will be used by the Indexer and Query Processor.
 * 
 * @author RB
 * 
 */
public class TextParser {

	private StopWords stopWords;

	public TextParser() {
		stopWords = new StopWords();
	}

	/**
	 * Cleans the string. Splits it and then removes the stop words. The words
	 * that are left are stemmed and returned as an arraylist.
	 * Also, only the words that are features are piped out !
	 * 
	 * @param inputString
	 * @return
	 */
	public ArrayList<String> tokenizeString(String inputString,
			boolean shouldStem) {
		DatabaseConnector databaseConnector = new DatabaseConnector();
		String outputString = cleanString(inputString);
		Map<String, Boolean> isWordFeature = new HashMap<String, Boolean>();
		ArrayList<String> tokens = new ArrayList<String>();
		for (String splitWord : outputString.split("\\s+")) {
			if ((stopWords.isStopWord(splitWord)) || (splitWord.length() < 3)
					|| (isInteger(splitWord))) {
				continue;
			}
			if (shouldStem) {
				splitWord = stemString(splitWord);
			}
			if (stopWords.isStopWord(splitWord)) {
				continue;
			}
			if (!(isWordFeature.containsKey(splitWord))) {
				isWordFeature.put(splitWord,databaseConnector.isTermFeature(splitWord));
			}
			if (isWordFeature.get(splitWord)) {
				tokens.add(splitWord);
			}
		}
		databaseConnector.closeDBConnection();
		Collections.sort(tokens);
		return tokens;
	}

	public ArrayList<String> getAllTokens(String inputString,
			boolean shouldStem) {
		String outputString = cleanString(inputString);
		ArrayList<String> tokens = new ArrayList<String>();
		for (String splitWord : outputString.split("\\s+")) {
			if ((stopWords.isStopWord(splitWord)) || (splitWord.length() < 3)
					|| (isInteger(splitWord))) {
				continue;
			}
			if (shouldStem) {
				splitWord = stemString(splitWord);
			}
			if (stopWords.isStopWord(splitWord)) {
				continue;
			}
			if(!tokens.contains(splitWord)){
				tokens.add(splitWord);
			}
		}
		return tokens;
	}


	/**
	 * Takes in an input String and performs stemming. Note: The word to be
	 * stemmed is expected to be in lower case. Forcing lower case must be done
	 * outside the Stemmer class.
	 * 
	 * @param inputString
	 * @return
	 */
	public String stemString(String inputString) {
		Stemmer stemmer = new Stemmer();
		stemmer.add(inputString.toCharArray(), inputString.length());
		stemmer.stem();
		String stemmedString = stemmer.toString();
		return stemmedString;
	}

	/**
	 * This function converts all characters to lower case and removes
	 * punctuation.
	 * 
	 * @param inputString
	 * @return
	 */
	public String cleanString(String inputString) {
		String outputString = inputString.toLowerCase();
		return outputString.replaceAll("[^a-z0-9]+", " ");
		// TODO Check the punctuation thing. Why ? Consider I'll -> that'll
		// become "I ll" and ll will go through.
	}

	public Boolean isInteger(String inputString) {
		try {
			Integer.parseInt(inputString);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

}
