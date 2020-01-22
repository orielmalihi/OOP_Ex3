package gameClient;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import utils.ArrayList_Comparator;
/**
 * This class represents a simple example of using MySQL Data-Base.
 * Use this example for writing solution. 
 * @author boaz.benmoshe
 *
 */
public class SimpleDB {
	private final int id = 311364954;
	public static final String jdbcUrl="jdbc:mysql://db-mysql-ams3-67328-do-user-4468260-0.db.ondigitalocean.com:25060/oop?useUnicode=yes&characterEncoding=UTF-8&useSSL=false";
	public static final String jdbcUser="student";
	public static final String jdbcUserPassword="OOP2020student";

	/**
	 * Simple main for demonstrating the use of the Data-base
	 * @param args
	 */
	public static void main(String[] args) {
		int id1 = 311364954;  // "dummy existing ID  
		int level = 13;
		//			allUsers();
		//			printLog();
		String kml = getKML(id1,level);
		//			System.out.println("***** KML file example: ******");
		//			System.out.println(kml);
	}
	/** return statistics of the game as a string.
	 * rules matrice:
	 * row 0 is number of moves accepted for each level
	 * row 1 is the score needed to pass the level.
	 * 
	 * log matrice:
	 * row 0 is the real level name.
	 * row 1 is the best score that the user was able to get.
	 * @param String 
	 * 
	 * 
	 */
	public static List<String> getStatistics() {
		ArrayList<String> ans = new ArrayList<String>();
		int[][] rules = {{290,580,580,500,580,580,580,290,580,290,1140},
				{125,436,713,570,480,1050,310,235,250,200,1000}};

		int [][] log = {{0,1,3,5,9,11,13,16,19,20,23},
				{0,0,0,0,0,0,0,0,0,0,0}};
		int gamesCounter = 0;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			Connection connection = 
					DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcUserPassword);
			Statement statement = connection.createStatement();
			String allCustomersQuery = "SELECT * FROM Logs where UserID = 311364954;";
			ResultSet resultSet = statement.executeQuery(allCustomersQuery);

			while(resultSet.next())
			{
				gamesCounter++;
				int score = resultSet.getInt("score");
				int level_id = resultSet.getInt("levelID");
				int moves = resultSet.getInt("moves");
				for(int i =0 ; i<log[0].length; i++) {
					if(log[0][i]==level_id)
						level_id = i;
				}
				if(score > log[1][level_id] && score>rules[1][level_id] && moves<rules[0][level_id])
					log[1][level_id] = score;
				//					System.out.println("Id: " + resultSet.getInt("UserID")+","+resultSet.getInt("levelID")+",moves: "+resultSet.getInt("moves")+" ,score: "+resultSet.getInt("score")+", "+resultSet.getDate("time"));
			}
			ans.add("Games played: "+gamesCounter);
			for(int i =0; i<log[0].length && log[1][i]!=0; i++) {
				ans.add("Level: "+log[0][i]+" , best score: "+log[1][i]);
			}
			int current_level = 10;
			while(log[1][current_level]==0 && current_level>0)
				current_level--;
			if((current_level+1)!=11)
				ans.add("Current level: "+log[0][current_level+1]);
			else
				ans.add("Current level: Game is Complete! well done!");
			
			ans.add("Ranks:");
			for(int i =0; i<log[0].length && log[1][i]!=0; i++) {
				ans.add(getRank(log[0][i], log[1][i]));
			}
			resultSet.close();
			statement.close();		
			connection.close();		
		}

		catch (SQLException sqle) {
			System.out.println("SQLException: " + sqle.getMessage());
			System.out.println("Vendor Error: " + sqle.getErrorCode());
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return ans;
	}
	/**
	 * this function returns the KML string as stored in the database (userID, level);
	 * @param id
	 * @param level
	 * @return
	 */
	public static String getKML(int id, int level) {
		String ans = null;
		String allCustomersQuery = "SELECT * FROM Users where userID="+id+";";
		try {
			Class.forName("com.mysql.jdbc.Driver");
			Connection connection = 
					DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcUserPassword);		
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(allCustomersQuery);
			if(resultSet!=null && resultSet.next()) {
				ans = resultSet.getString("kml_"+level);
			}
		}
		catch (SQLException sqle) {
			System.out.println("SQLException: " + sqle.getMessage());
			System.out.println("Vendor Error: " + sqle.getErrorCode());
		}

		catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return ans;
	}
	public static int allUsers() {
		int ans = 0;
		String allCustomersQuery = "SELECT * FROM Users;";
		try {
			Class.forName("com.mysql.jdbc.Driver");
			Connection connection = 
					DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcUserPassword);		
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(allCustomersQuery);
			while(resultSet.next()) {
				System.out.println("Id: " + resultSet.getInt("UserID"));
				ans++;
			}
			resultSet.close();
			statement.close();		
			connection.close();
		}
		catch (SQLException sqle) {
			System.out.println("SQLException: " + sqle.getMessage());
			System.out.println("Vendor Error: " + sqle.getErrorCode());
		}

		catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return ans;
	}

	private static String getRank(int level, int user_score) {
		String ans = "Did not found Your Score.";
		int counter = 0;
		ArrayList<Integer> scores = new ArrayList<Integer>();

		try {
			Class.forName("com.mysql.jdbc.Driver");
			Connection connection = 
					DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcUserPassword);
			Statement statement = connection.createStatement();
			String allCustomersQuery = "SELECT * FROM Logs where levelID="+level+";";
			ResultSet resultSet = statement.executeQuery(allCustomersQuery);

			while(resultSet.next())
			{
				counter++;
				int score = resultSet.getInt("score");
				scores.add(score);
			}
			scores.sort(new ArrayList_Comparator());
			System.out.println("sort check : "+scores.get(0)+"<"+scores.get(1)+"<"+scores.get(2));
			for(int i =0; i<scores.size(); i++) {
				if(scores.get(i)==user_score) {
					ans = "Level: "+level+" , rank: "+i+" out of "+counter+" scores";
				}
					
			}
			resultSet.close();
			statement.close();		
			connection.close();		
		}
		catch (SQLException sqle) {
			System.out.println("SQLException: " + sqle.getMessage());
			System.out.println("Vendor Error: " + sqle.getErrorCode());
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		return ans;
	}
}

