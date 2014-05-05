import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;

public class MongoToMySQLManager {

	static final String MYSQLDB_URL = "jdbc:mysql://localhost/cmpe283";
	static final String MYSQLUSER = "group3";
	static final String MYSQLPASS = "sjsugroup3";
	static final boolean LocalMongo = true;
	static final String MongoURL = "localhost";
	static final String MongoDBName = "logstash";
	static final String MongoDBCollection = "lingdb";
	static final int SleepInterval = 10000;

	public static void main(String[] args) {

		while (true) {
			try {

				System.out.println("Connecting to mongoDB...");
				MongoClient mongoClient = new MongoClient(MongoURL, 27017);
				DB mongodb = mongoClient.getDB(MongoDBName);
				DBCollection mongocoll = mongodb.getCollection(MongoDBCollection);
				System.out.println("Connecting to MySQL...");
				Connection mysqlconn =
						DriverManager.getConnection(MYSQLDB_URL, MYSQLUSER, MYSQLPASS);
				System.out.println("Getting CPU / Memory / IO data from mongoDB to MySQL...");

				storePercentage(mongocoll, mysqlconn, "cpu");
				storePercentage(mongocoll, mysqlconn, "mem");
				storeIOs(mongocoll, mysqlconn);

				System.out.println("Closing connection to MySQL...");
				mysqlconn.close();

				System.out.println("Wait for " + SleepInterval / 1000
						+ " second to retrieve data again...");
				Thread.sleep(SleepInterval);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public static void storePercentage(DBCollection mongocoll, Connection mysqlconn, String tableName) throws Exception {
		// condition list£º
		BasicDBList condList = new BasicDBList();
		condList.add(new BasicDBObject().append("type", tableName));
		BasicDBObject cond = new BasicDBObject();
		cond.put("$and", condList);

		DBCursor cursor = mongocoll.find(cond).sort(new BasicDBObject("@timestamp", -1));

		while (cursor.hasNext()) {
			try {
				BasicDBObject obj = (BasicDBObject) cursor.next();

				String id = obj.getString("_id");
				String machineType = obj.getString("machineType");
				String machineName = obj.getString("machineName");
				Timestamp timestamp = parseDate(obj.getString("@timestamp"));
				Double percent = Double.parseDouble(obj.getString("percent"));

				System.out.println(String.format("%s %s %s Percentage: %s", machineType, machineName, timestamp, percent));
				String sql =
						"insert ignore into "
								+ tableName
								+ " (id, machineType, machineName, timestamp, percent) values (?,?,?,?,?)";
				PreparedStatement prest = mysqlconn.prepareStatement(sql);
				prest.setString(1, id);
				prest.setString(2, machineType);
				prest.setString(3, machineName);
				prest.setTimestamp(4, timestamp);
				prest.setDouble(5, percent);
				prest.executeUpdate();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		cursor.close();
	}

	/*
	 * 2014-05-04T05:42:22.424Z
	 */
	private static Timestamp parseDate(String strDate) throws ParseException {
		SimpleDateFormat formatter = new SimpleDateFormat("\"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'\"");
		formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
		java.util.Date date = formatter.parse(strDate);

		return new Timestamp(date.getTime());
	}

	public static void storeIOs(DBCollection mongocoll, Connection mysqlconn) throws Exception {
		// condition list£º
		BasicDBList condList = new BasicDBList();
		condList.add(new BasicDBObject().append("type", "disk"));
		condList.add(new BasicDBObject().append("type", "virtualDisk"));
		BasicDBObject cond = new BasicDBObject();
		cond.put("$or", condList);

		DBCursor cursor = mongocoll.find(cond).sort(new BasicDBObject("@timestamp", -1));

		while (cursor.hasNext()) {
			try {
				BasicDBObject obj = (BasicDBObject) cursor.next();

				String id = obj.getString("_id");
				String machineType = obj.getString("machineType");
				String machineName = obj.getString("machineName");
				Timestamp timestamp = parseDate(obj.getString("@timestamp"));
				Double read = Double.parseDouble(obj.getString("read"));
				Double write = Double.parseDouble(obj.getString("write"));

				System.out.println(String.format("%s %s %s Read: %s Write: %s", machineType, machineName, timestamp, read, write));
				String sql =
						"insert ignore into io (id, machineType, machineName, timestamp, r, w) values (?,?,?,?,?,?)";
				PreparedStatement prest = mysqlconn.prepareStatement(sql);
				prest.setString(1, id);
				prest.setString(2, machineType);
				prest.setString(3, machineName);
				prest.setTimestamp(4, timestamp);
				prest.setDouble(5, read);
				prest.setDouble(6, write);
				prest.executeUpdate();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		cursor.close();
	}
}
