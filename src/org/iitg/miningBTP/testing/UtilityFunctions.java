package org.iitg.miningBTP.testing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;
import java.util.ArrayList;

import org.iitg.miningBTP.core.Classifier;
import org.iitg.miningBTP.core.TextParser;
import org.iitg.miningBTP.db.DatabaseConnector;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 * This class will contain functions that perform actions.
 * @author RB
 *
 */
public class UtilityFunctions {

	public static void initializeDatabase() throws IOException {
		System.out.println("Start");
		DatabaseConnector databaseConnector = new DatabaseConnector();
		System.out.println("DB Up");
		databaseConnector.createTables();
		System.out.println("Tables Created");
		databaseConnector.fillClassMappings();
		System.out.println("Mappings Filled");
		databaseConnector.fillClassContents();
		System.out.println("Contents Filled");
		databaseConnector.fillFeaturesList();
		System.out.println("Features List Filled");
		databaseConnector.fillTermDistribution();
		System.out.println("Feature Dist. Done");
		databaseConnector.closeDBConnection();
	}

	public static void recomputeFeatures() {
		DatabaseConnector databaseConnector = new DatabaseConnector();
		databaseConnector.deleteFeatures();
		System.out.println("Features Gone");
		Classifier classifier = new Classifier(databaseConnector);
		ArrayList<String> features = classifier.getFeaturesList();
		databaseConnector.insertFeatures(features);
		databaseConnector.closeDBConnection();
	}
	
	public static void writeFeaturesToFile() throws IOException{
		DatabaseConnector databaseConnector = new DatabaseConnector();
		ArrayList<String> features = databaseConnector.getAllFeaturesList();
		FileWriter fileWriter = new FileWriter("Features.txt");
		for(String feature : features){
			fileWriter.write(feature + "\n");
		}
		fileWriter.close();
	}
	
	public static String classifyUrl(String webpageUrl, Boolean shouldMerge) throws IOException {
		URL url = new URL(webpageUrl);
		//Use GProxy for this !
		Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 8080));
		URLConnection urlConnection = url.openConnection(proxy);
		urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
		urlConnection.connect();
		
		String line = null;
		StringBuffer webPageBuffer = new StringBuffer();
		BufferedReader inputReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
		while ((line = inputReader.readLine()) != null) {
			webPageBuffer.append(line);
		}

		Document document = Jsoup.parse(String.valueOf(webPageBuffer), "UTF-8");
		Elements title = document.select("title");
		Elements body = document.select("body");
		String sourceCode = title.text() + body.text();
		
		DatabaseConnector databaseConnector =  new DatabaseConnector();
		TextParser textParser = new TextParser();
		Classifier classifier = new Classifier(databaseConnector);
		int classId = classifier.classifyDoc(textParser.tokenizeString(sourceCode, true));
		String className = "";
		className = databaseConnector.getClassName(classId);
		if(shouldMerge){
			databaseConnector.updateClassContents(classId);
			databaseConnector.updateTermDistribution(textParser.getAllTokens(sourceCode, true), classId);
			System.out.println("Finished updating distribution");	
		}
		databaseConnector.closeDBConnection();
		return className;
	}
	
	public static String getSoupedPageContent(String fileName) {
		String line = "";
		String sourceCode = "";
		try {
			FileReader fileReader = new FileReader(fileName + ".htm");
			BufferedReader bufferedReader = new BufferedReader(fileReader);

			while ((line = bufferedReader.readLine()) != null) {
				sourceCode += line + "\n";
			}
			bufferedReader.close();
			fileReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Document document = Jsoup.parse(sourceCode);
		Elements title = document.select("title");
		Elements body = document.select("body");
		return title.text() + " " + body.text();
	}

	public static void classifyTestingSet() throws IOException, SQLException {
		int numOfDocsProcessed = 0;
		TextParser textParser = new TextParser();
		FileWriter fileWriter = new FileWriter("Results.txt");
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		File file = new File("testPages");
		for (File fileName : file.listFiles()) {
			FileReader fileReader = new FileReader(fileName);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String line = "";
			String pageContent = "";
			String currentURL = "";
			while ((line = bufferedReader.readLine()) != null) {
				if (line.contains("URL -#- ")) {
					ArrayList<String> tokens = textParser.tokenizeString(
							pageContent, true);
					if (!(pageContent.equals(""))) {
						DatabaseConnector databaseConnector = new DatabaseConnector();
						Classifier classifier = new Classifier(
								databaseConnector);
						bufferedWriter.write(currentURL + "\n");
						bufferedWriter.write("Actual: "
								+ fileName.toString()
										.replace("testPages\\", "")
								+ "\nClassified: "
								+ databaseConnector.getClassName(
										classifier.classifyDoc(tokens))
										.replace("/", "-") + "\n\n");
						numOfDocsProcessed++;
						databaseConnector.closeDBConnection();
					}
					if (numOfDocsProcessed % 100 == 0) {
						System.out.println(numOfDocsProcessed + " - Done");
					}
					currentURL = line.replace("URL -#- ", "").trim();
					pageContent = "";
				}
				line = line.replace("URL -#- ", " ").replace("TITLE -#- ", " ")
						.replace("DESC -#- ", " ");
				pageContent = pageContent + " " + line;
			}
			bufferedReader.close();
			fileReader.close();
		}
		bufferedWriter.close();
		fileWriter.close();
	}

}
