package bachelorThesisPlayground.readers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import javax.naming.InitialContext;
import javax.sql.DataSource;

public class DBReader {
	private Connection connection = null;
	
	PreparedStatement dayConsumption;
	
	public Map<Integer, Double> readConsumption() {
		Map<Integer, Double> result = new HashMap<>();
		try {
			ResultSet message = dayConsumption.executeQuery();
			while (message.next()) 
				result.put(message.getInt("placecode"), message.getDouble("consumption"));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	
	public void init() {		
		try {			
			Class.forName("org.postgresql.Driver");
			connection = DriverManager.getConnection(
			   "jdbc:postgresql://localhost:5432/postgres","postgres", "postgres");
			
			dayConsumption = connection.prepareStatement(getFile("dayConsumption.sql"));
		} catch (Exception e) {
			e.printStackTrace();		
		}
	}
		
	public void close(){
		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	

	  private String getFile(String fileName) {

		StringBuilder result = new StringBuilder("");

		//Get file from resources folder
		ClassLoader classLoader = getClass().getClassLoader();
		try {
			File file = new File(URLDecoder.decode(classLoader.getResource(fileName).getFile(), "UTF-8"));
		
			Scanner scanner = new Scanner(file);
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				result.append(line).append("\n");
			}

				scanner.close();
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result.toString();

	  }
}
