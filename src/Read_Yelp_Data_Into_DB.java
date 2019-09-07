//This program will read yelp data and store into a database
//Remove special characters except ', escape it out
import java.io.*;
import java.sql.*;
import java.sql.Statement;
import java.util.Scanner;
import org.hsqldb.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
public class Read_Yelp_Data_Into_DB {
	@SuppressWarnings("resource")
	public static void main(String args[])throws SQLException, Exception, ClassNotFoundException{


		Server hsqlServer = null;
		try {
			hsqlServer = new Server();
			hsqlServer.setLogWriter(null);
			hsqlServer.setSilent(true);

			//Update the database name and path to your own!
			hsqlServer.setDatabaseName(0, "dbName");
			hsqlServer.setDatabasePath(0, "file:C:\\Users\\phunous\\databases\\yelpDBfolder\\yelpdb");

			//Start the database!
			hsqlServer.start();
			Connection connection = null;

			try {
				//Getting a connection to the newly started database
				Class.forName("org.hsqldb.jdbcDriver");
				//Default user of the HSQLDB is 'sa' with an empty password
				connection = DriverManager.getConnection(
						"jdbc:hsqldb:file:C:\\Users\\phunous\\databases\\yelpDBfolder\\yelpdb", "sa", "");

				//can execute youSQL commands here!
				//connection.prepareStatement("").execute();
				connection.prepareStatement(
						"DROP TABLE Labels; ").execute();
				connection.prepareStatement(
						"DROP TABLE PhotoInfo; ").execute();
				connection.prepareStatement(
						"DROP TABLE Reviews; ").execute();
				connection.prepareStatement(
						"DROP TABLE BusinessInfo; ").execute();
				//Create tables
				connection.prepareStatement("CREATE TABLE BusinessInfo("
						+ "business_id VARCHAR(50) NOT NULL,"
						+ "name VARCHAR(100) NOT NULL,"
						+ "address VARCHAR(100) NOT NULL,"
						+ "city VARCHAR(70) NOT NULL,"
						+ "state VARCHAR(3) NOT NULL,"
						+ "stars DECIMAL(3,1),"
						+ "review_count INTEGER DEFAULT 0,"
						+ "CONSTRAINT busPK PRIMARY KEY(business_id));").execute();
				connection.prepareStatement("CREATE TABLE PhotoInfo("
						+ "photo_id VARCHAR(50) NOT NULL, "
						+ "business_id VARCHAR(50) NOT NULL,"
						+ "caption VARCHAR(250) NULL,"
						+ "CONSTRAINT photoPK PRIMARY KEY(photo_id), "
						+ "CONSTRAINT photoFK FOREIGN KEY(business_id) REFERENCES BusinessInfo(business_id)ON DELETE CASCADE ON UPDATE CASCADE);").execute();

				connection.prepareStatement("CREATE TABLE Reviews("
						+ "review_id VARCHAR(50) NOT NULL,"
						+ "user_id VARCHAR(50) NOT NULL,"
						+ "business_id VARCHAR(50) NOT NULL,"
						+ "stars INTEGER, "
						+ "numPositiveWords INTEGER DEFAULT 0,"
						+ "numNegativeWords INTEGER DEFAULT 0,"
						+ "useful INTEGER DEFAULT 0,"
						+ "funny INTEGER DEFAULT 0,"
						+ "cool INTEGER DEFAULT 0,"
						+ "CONSTRAINT revPK PRIMARY KEY(review_id),"
						+ "CONSTRAINT revFK FOREIGN KEY(business_id) REFERENCES BusinessInfo(business_id)ON DELETE CASCADE ON UPDATE CASCADE);").execute();

				connection.prepareStatement("CREATE TABLE Labels("
						+ "photo_id VARCHAR(50) NOT NULL,"
						+ "label VARCHAR(100) NOT NULL,"
						+ "CONSTRAINT labPK PRIMARY KEY(photo_id,label),"
						+ "CONSTRAINT labFK FOREIGN KEY(photo_id) REFERENCES PhotoInfo(photo_id)ON DELETE CASCADE ON UPDATE CASCADE);").execute();

				//Add business data to the DB
				BufferedReader read = new BufferedReader(new FileReader("short_business.json"));
				JSONParser parser = new JSONParser();
				while(read.ready()){
					JSONObject busi = (JSONObject) parser.parse(read.readLine());
					String business_id= (String) busi.get("business_id");
					String name = (String) busi.get("name");
					String address = (String) busi.get("address");
					String city = (String) busi.get("city");
					String state = (String) busi.get("state");
					String stars = Double.toString((Double)busi.get("stars"));
					String review_count = Long.toString((Long)busi.get("review_count"));
					connection.prepareStatement("INSERT INTO BusinessInfo(business_id, name, address, city, state, stars, review_count)"
							+ "VALUES('"
							+business_id+"', '"
							+name+"', '"
							+address+"', '"
							+city+"', '"
							+state+"', "
							+stars+", "
							+review_count+")").execute();
				}
				read.close();
				
				//print the table 
				Statement stm = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
				ResultSet rs = stm.executeQuery("SELECT TOP 5 * from BusinessInfo WHERE stars = 2.0;");
				ResultSetMetaData rsmd = rs.getMetaData();
				printtables(rs,rsmd,50);
				
				//Add reviews to DB
				read = new BufferedReader(new FileReader("short_review.json"));
				while(read.ready()){
					JSONObject rev = (JSONObject) parser.parse(read.readLine());
					String review_id = (String)rev.get("review_id");
					String user_id = (String)rev.get("user_id");
					String business_id = (String)rev.get("business_id");
					String stars = Long.toString((Long)rev.get("stars"));
					String numPos = Long.toString((Long)rev.get("NumPositiveWords"));
					String numNeg = Long.toString((Long)rev.get("NumNegativeWords"));
					String use = Long.toString((Long)rev.get("useful"));
					String fun = Long.toString((Long)rev.get("funny"));
					String coo = Long.toString((Long)rev.get("cool"));
					connection.prepareStatement("INSERT INTO Reviews(review_id,user_id,business_id,stars,numPositiveWords,numNegativeWords,useful,funny,cool)"
							+ "values('"
							+ review_id+"', '"
							+user_id+"', '"
							+business_id+"',"
							+stars+","
							+numPos+","
							+numNeg+","
							+use+","
							+fun+","
							+coo+")").execute();
				}
				
				//print the table 
				stm = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
				rs = stm.executeQuery("SELECT TOP 5 * from Reviews WHERE cool=1;");
				rsmd = rs.getMetaData();
				printtables(rs,rsmd,50);
				
				read = new BufferedReader(new FileReader("short_photos.json"));
				while(read.ready()){
					JSONObject pho = (JSONObject) parser.parse(read.readLine());
					String photo_id = (String)pho.get("photo_id");
					String business_id= (String)pho.get("business_id");
					String caption = (String)pho.get("caption");
					connection.prepareStatement("INSERT INTO PhotoInfo(photo_id,business_id,caption)"
							+ "values('"
							+ photo_id+"', '"
							+business_id+"', '"
							+caption+"')").execute();
					}
				stm = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
				rs = stm.executeQuery("SELECT TOP 5 * from PhotoInfo WHERE caption <> '';");
				rsmd = rs.getMetaData();
				printtables(rs,rsmd,50);
				
				read = new BufferedReader(new FileReader("labels.json"));
				while(read.ready()){
					JSONObject lab = (JSONObject) parser.parse(read.readLine());
					String photo = (String) lab.get("photo_id");
					String label =(String) lab.get("label");
					connection.prepareStatement("INSERT INTO Labels(photo_id, label)"
							+ "values('"
							+ photo+"', '"
							+label+"')").execute();
					
					}
				stm = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
				rs = stm.executeQuery("SELECT TOP 5 * from Labels;");
				rsmd = rs.getMetaData();
				printtables(rs,rsmd,50);

				System.out.println("Here are the top labels that people are most likely to take pictures of:");
				stm = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
				rs = stm.executeQuery("SELECT TOP 5 Label, COUNT(*) AS NumPictures from Labels GROUP BY label ORDER BY COUNT(*) DESC;");
				rsmd = rs.getMetaData();
				printtables(rs,rsmd,50);
				
				System.out.println("Total Number of positive and negative words used in 1 star reviews:");
				stm = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
				rs = stm.executeQuery("SELECT SUM(numPositiveWords) AS TotalPos, SUM(numNegativeWords) AS TotalNeg from Reviews WHERE stars = 1;");
				rsmd = rs.getMetaData();
				printtables(rs,rsmd,20);
				
				System.out.println("Total Number of positive and negative words used in 5 star reviews:");
				stm = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
				rs = stm.executeQuery("SELECT SUM(numPositiveWords) AS TotalPos, SUM(numNegativeWords) AS TotalNeg from Reviews WHERE stars = 5;");
				rsmd = rs.getMetaData();
				printtables(rs,rsmd,20);
			}finally {
				//closing the connection
				if(connection != null)
					connection.close();
			}
		}finally{
			//closing the server
			if(hsqlServer != null)
				hsqlServer.stop();
		}
	}
	public static void printtables(ResultSet rs, ResultSetMetaData rsmd, int maxlength) throws SQLException{
		int numberOfColumns = rsmd.getColumnCount();
		String max = String.valueOf(-maxlength);
		//print the column names from RSMD
		for (int i = 1; i <= numberOfColumns; i++) {
			String columnName = rsmd.getColumnName(i);
			System.out.printf("%"+(max)+"s", columnName);

		}
		System.out.println("");

		//print row values from RS
		while (rs.next()) {
			for (int i = 1; i <= numberOfColumns; i++) {
				String columnValue = rs.getString(i);
				System.out.printf("%"+max+"s",columnValue);
			}
			System.out.println("");
		}
		System.out.println("");
	}


}

