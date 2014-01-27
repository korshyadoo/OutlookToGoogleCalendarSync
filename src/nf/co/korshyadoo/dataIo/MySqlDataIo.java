//TODO Should be reading the private final fields from a file (i.e. use Spring)


//Update examples:
			//		update("INSERT INTO FileLocations (name, location) VALUES (\"thenewest\", \"d:\\\\thenewest\\\\test\");");
//			update("CREATE TABLE IF NOT EXISTS users(" +
//					"userName VARCHAR(255) NOT NULL UNIQUE," +
//					"password VARCHAR(255) NOT NULL," +
//					"calendarID VARCHAR(255) NOT NULL);");
//			update("INSERT INTO users (userName, password, calendarID) " +
//					"VALUES (\"mysql_teet@gmail.com\", \"teet pass\", \"teet\");"); 


package nf.co.korshyadoo.dataIo;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.JOptionPane;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.properties.EncryptableProperties;

import com.korshyadoo.test.NonUser;
import com.korshyadoo.test.User;

public class MySqlDataIo implements DataIo {
//	private final String DATABASE_NAME;
//	private final String DATABASE_HOST;
//	private final String DATABASE_USERNAME;
//	private final String DATABASE_PASSWORD;
	private final String USERS_TABLE = "users";
	private final String NON_USERS_TABLE = "nonUsers";
	private static final String PROPERTIES_LOCATION = "MySqlConnection.properties";
	private Connection connection;
	private Statement statement;
	@SuppressWarnings("deprecation")
	private static SessionFactory factory = new Configuration().configure().buildSessionFactory();;

	private static MySqlDataIo instance;
	

	/**
	 * Initialize the constants and connect to the database
	 */
	private MySqlDataIo()  {
	}
	
	//DEBUG
	@SuppressWarnings("unused")
	private String decrypt(String encryptedString) {
		StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
		encryptor.setPassword("pass");
		return encryptor.decrypt(encryptedString);
	}
	
	//DEBUG
	@SuppressWarnings("unused")
	private void getPasswordFromProperties() {
		StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
		encryptor.setPassword("pass");
		Properties properties = new EncryptableProperties(encryptor);
		try {
			properties.load(this.getClass().getClassLoader().getResourceAsStream(PROPERTIES_LOCATION));
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, e.getCause().getClass());
			JOptionPane.showMessageDialog(null, e.getMessage());
			JOptionPane.showMessageDialog(null, e.getSuppressed());
			e.printStackTrace();
		}
		String DATABASE_NAME = properties.getProperty("databaseName");
		String DATABASE_HOST = properties.getProperty("databaseHost");
		String DATABASE_USERNAME = properties.getProperty("databaseUsername");
		String DATABASE_PASSWORD = properties.getProperty("databasePassword");
	}
	
	//DEBUG
	@SuppressWarnings("unused")
	private void writeEncryptedPassword(String password) throws FileNotFoundException, IOException {
		StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
		encryptor.setPassword("pass");
		String enc = encryptor.encrypt(password);
		
		Properties unenc = new Properties();
		unenc.load(new FileInputStream("config.properties"));
		unenc.setProperty("datasource.password", "ENC(" + enc +")");
		unenc.store(new FileOutputStream("config.properties"), "");
		
		Properties props = new EncryptableProperties(encryptor);
		props.load(new FileInputStream("config.properties"));
		System.out.println(props.getProperty("datasource.username"));
		System.out.println(props.getProperty("datasource.password"));
	}
	
	
	private void createNonUsersTable() throws SQLException {
		Session session = factory.openSession();
		String hql = "CREATE TABLE IF NOT EXISTS fileLocations (" + 
                "name VARCHAR(100) NOT NULL, " +
                "location VARCHAR(255) NULL)";
		Query query = session.createQuery(hql); 
		session.close();
	}
	
	private void createUsersTable() throws SQLException {
		String query = "CREATE TABLE IF NOT EXISTS users (" +
						"userName VARCHAR(255) NOT NULL, " +
						"password VARCHAR(255) NOT NULL, " +
						"calendardID VARCHAR(255) NOT NULL, " +
						"PRIMARY KEY (userName))";
		update(query);
	}
	
	
	private void printAllFileLocations() throws SQLException {
		String fileName = null;
		String location = null;
		ResultSet rs = query("SELECT * FROM FileLocations");
		while(rs.next()) {
			fileName = rs.getString("name");
			location = rs.getString("location");
			System.out.println("Name: " + fileName);
			System.out.println("Location: " + location);
			System.out.println();
		}
		System.out.println("-----------------------");
		statement.close();
	}
	
	private void printAllUsers() throws SQLException {
		ResultSet rs = query("SELECT * FROM " + USERS_TABLE);
		while(rs.next()) {
			System.out.println(rs.getString("userName"));
			System.out.println(rs.getString("password"));
			System.out.println(rs.getString("calendarID"));
			System.out.println();
		}
		System.out.println("-----------------------");
	}

	/**
	 * 
	 * @param q
	 * @return a {@code ResultSet} object that contains the data produced by the given query; never {@code null} 
	 * @throws SQLException
	 */
	private ResultSet query(String q) throws SQLException {
		statement = connection.createStatement();
		ResultSet result = statement.executeQuery(q);
		return result;
	}

	/**
	 * 
	 * @param u
	 * @throws SQLException
	 */
	private void update(String u) throws SQLException {
		statement = connection.createStatement();
		statement.executeUpdate(u);
		statement.close();
	}

	public static MySqlDataIo getInstance() {
		if(instance == null) {
			instance = new MySqlDataIo();
		}
		return instance;
	}

	@Override
	public String getPstLocation() {
		Session session = factory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			NonUser pstLocation = (NonUser)session.get(NonUser.class, "pstLocation"); 
			if(pstLocation != null) {
				return pstLocation.getValue();
			} else {
				return null;
			}
		} catch(HibernateException e) {
			if(tx != null) {
				tx.rollback();
			}
			e.printStackTrace();
		}
		return null;
	}


	@Override
	public String getField(String userName, String field) {
		Session session = factory.openSession();
		String hql = "FROM User U WHERE U.userName = \'" + userName + "\'"; 
		Query query = session.createQuery(hql); 
		User user = (User)query.list().get(0);
		for(Object key : user.getFields().keySet()) {
			if(field.equals(key.toString())) {
				return user.getFields().get(key).toString();
			}
		}
		return null;
	}

	@Override
	public boolean setField(String userName, String field, String value) {
		if(userName == null || field == null || value == null) {
			return false;
		}
		Session session = factory.openSession(); 
		Transaction tx = null; 
		try{ 
			tx = session.beginTransaction(); 
			User user = (User)session.get(User.class, userName);  
			Map fields = user.getFields();
			fields.put(field, value);
			session.update(user);  
			tx.commit(); 
		}catch (HibernateException e) { 
			if (tx!=null) tx.rollback(); 
			e.printStackTrace();  
		}finally { 
			session.close();  
		} 
		return true;
	}


	/**
	 * Add a new user to the database. If the user is a duplicate, it will not be added and will return {@code false}.
	 * @param userName The new user's username
	 * @param names A String array containing the names of the fields to be added with the user
	 * @param values A String array containing the values for the given fields
	 * @throws IllegalArgumentException if there is an unequal number of fields and values, 
	 * or if one specified fields is not a valid table column name.
	 * @returns {@code true} if the user was created successfuly; otherwise, {@code false} 
	 */
	@Override
	public boolean createUser(String userName, String[] names, String[] values)
			throws IllegalArgumentException {
		
		//Make sure there are the same number of fields and values
		if(names.length != values.length) {
			throw new IllegalArgumentException("Unequal number of fields and values");
		}
		
		//Make sure all fields are valid columns

		//Query the list of columns in the users table and store them in a String array
		String query = "SELECT column_name FROM information_schema.columns " +
						"WHERE table_name = \"users\"";
		String[] columns = null;
		try {
			ResultSet rs = query(query);
			rs.last();
			columns = new String[rs.getRow()];
			rs.beforeFirst();
			int index = 0;
			while(rs.next()) {
				String temp = rs.getString("column_name");
				columns[index] = new String(temp);
				index++;
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		
		//Check each field in the array against the column query
		for(String field : names) {
			boolean found = false;
			for(String column : columns) {
				if(field.equals(column)) {
					found = true;
				}
			}
			if(!found) {
				throw new IllegalArgumentException("One of the fields is not a valid column in the database");
			}
		}
//////	
//		//Format the list of fields for the MySQL query
//		StringBuilder fieldList = new StringBuilder("userName, ");
//		for(int x = 0; x < fields.length; x++) {
//			fieldList.append(fields[x]);
//			if(!(x == (fields.length - 1))) {
//				fieldList.append(", ");
//			}
//		}
//
//		//Format the list of values for the MySQL query
//		StringBuilder valueList = new StringBuilder("\"" + userName + "\", \"");
//		for(int x = 0; x < values.length; x++) {
//			valueList.append(values[x]);
//			if(!(x == (values.length - 1))) {
//				valueList.append("\", \"");
//			} else {
//				valueList.append("\"");
//			}
//		}
//
//		//Execute the update query
//		String updateQuery = "INSERT INTO users (" + fieldList.toString() + ") " + 
//				"VALUES (" + valueList.toString() + ")";
//		System.out.println(updateQuery);
//		try {
//			update(updateQuery);
//		} catch (SQLException e) {
//			System.out.println(e.getMessage());
//			return false;
//		}
//
//		return true;


		//Create map from passed Lists
		Map fields = new HashMap();
		for(int x = 0; x < names.length; x++) {
			fields.put(names[x], values[x]);
		}
		
		Session session = factory.openSession(); 
		Transaction tx = null; 
		try{ 
			tx = session.beginTransaction(); 
			User user = new User(userName, fields); 
			session.save(user);  
			tx.commit(); 
		}catch (HibernateException e) { 
			if (tx!=null) tx.rollback(); 
			e.printStackTrace();  
			return false;
		}finally { 
			session.close();  
		} 
		return true;
	}


	@Override
	public boolean isEmpty() {
		return isEmpty(this.NON_USERS_TABLE) && isEmpty(this.USERS_TABLE);
	}
	
	private boolean isEmpty(String tableName) {
		Session session = factory.openSession();
		try {
			String sql = "SELECT * from " + tableName;
			Query query = session.createSQLQuery(sql);
			query.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);
			if(query.list().size() == 0) {
				return true;
			} else {
				return false;
			}
		} catch (HibernateException e) {
			System.out.println("Table " + tableName + " doesn't exist");
			return true;
		} finally {
			session.close();
		}
	}

	@Override
	public boolean deleteAllData() {
		Session session = factory.openSession();
		try {
			String sql = "DELETE FROM " + this.NON_USERS_TABLE;
			session.createSQLQuery(sql);
			sql = "DELETE FROM " + this.USERS_TABLE;
			session.createSQLQuery(sql);
			return true;
		} catch (HibernateException e) {
			System.out.println(e.getMessage());
			return false;
		} finally {
			session.close();
		}
	}

	@Override
	public boolean hasPstLocation() {
		Session session = factory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			NonUser pstLocation = (NonUser)session.get(NonUser.class, "pstLocation"); 
			return pstLocation != null;
		} catch(HibernateException e) {
			if(tx != null) {
				tx.rollback();
			}
			e.printStackTrace();
		} finally {
			session.close();
		}
		return false;
	}

	@Override
	public boolean hasUser() {
		Session session = factory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			List users = session.createQuery("FROM users").list();
			return users != null && users.size() > 0;
		} catch(HibernateException e) {
			if(tx != null) {
				tx.rollback();
			}
			e.printStackTrace();
		} finally {
			session.close();
		}
		return false;
	}

	@Override
	public boolean deleteUser(String userName) {
		Session session = factory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			User user = (User)session.get(User.class, userName);
			session.delete(user);
			tx.commit();
			return true;
		} catch (HibernateException e) {
			if(tx != null) tx.rollback();
			e.printStackTrace();
		} finally {
			session.close();
		}
		return false;
	}

	
	@Override
	public boolean hasUser(String userName) {
		//DEBUG can transaction be removed and have the same result???
		Session session = factory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			User user = (User)session.get(User.class, userName);
			return user != null;
		} catch(HibernateException e) {
			e.printStackTrace();
		} finally {
			session.close();
		}
		return false;
	}

	@Override
	public String getDefaultUserName() {
		//DEBUG can transaction be removed and have the same result???
		Session session = factory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			List users = session.createQuery("FROM users").list();
			for(Object o : users) {
				if(((User)o).getFields().get("default") == "true") {
					return ((User)o).getUserName();
				}
			}
			return null;
		} catch(HibernateException e) {
			if(tx != null) {
				tx.rollback();
			}
			e.printStackTrace();
		} finally {
			session.close();
		}
		return null;
	}

	@Override
	public int getUserCount() {
		//DEBUG can transaction be removed and have the same result???
		Session session = factory.openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			List users = session.createQuery("FROM users").list();
			if(users == null) {
				return 0;
			} else {
				return users.size();
			}
		} catch(HibernateException e) {
			if(tx != null) {
				tx.rollback();
			}
			e.printStackTrace();
		} finally {
			session.close();
		}
		return 0;
	}

	@Override
	public boolean setPstLocation(String pstLocation) {
		if(pstLocation == null) {
			return deletePstLocation();
		} else {
			Session session = factory.openSession(); 
			Transaction tx = null; 
			try{ 
				tx = session.beginTransaction(); 
				NonUser nonUser = (NonUser)session.get(NonUser.class, "pstLocation");
				nonUser.setValue(pstLocation);
				session.update(nonUser);  
				tx.commit(); 
				return true;
			}catch (HibernateException e) { 
				if (tx!=null) tx.rollback(); 
				e.printStackTrace();  
			}finally { 
				session.close();  
			} 
			return false;
		}
	}

	@Override
	public String getNonUserEntry(String entryName) {
		if(entryName == null) {
			return null;
		} else {
			Session session = factory.openSession(); 
			try{ 
				NonUser nonUser = (NonUser)session.get(NonUser.class, entryName);
				return nonUser.getValue();
			}catch (HibernateException e) { 
				e.printStackTrace();  
			}finally { 
				session.close();  
			} 
			return null;
		}
	}

	@Override
	public boolean setNonUserEntry(String entryName, String entryValue) {
		if(entryName == null) {
			return false;
		} else if(entryValue == null) {
			return deleteNonUserField(entryName);
		} else {
			Session session = factory.openSession(); 
			Transaction tx = null; 
			try{ 
				tx = session.beginTransaction(); 
				NonUser nonUser = (NonUser)session.get(NonUser.class, entryName);
				nonUser.setValue(entryValue);
				session.update(nonUser);  
				tx.commit(); 
				return true;
			}catch (HibernateException e) { 
				if (tx!=null) tx.rollback(); 
				e.printStackTrace();  
			}finally { 
				session.close();  
			} 
			return false;
		}
	}

	@Override
	public boolean setDefaultUser(String userName) {
		if(userName == null) {
			return false;
		} else {
			String defaultUser = getDefaultUserName();
			Session session = factory.openSession();
			Transaction tx = null;
			try {
				tx = session.beginTransaction(); 
				User user = (User)session.get(User.class, defaultUser);
				user.getFields().put("default", "false");
				tx.commit();
				
				tx = session.beginTransaction();
				user = (User)session.get(User.class, userName);
				user.getFields().put("default", "true");
				tx.commit();
				
				return true;
			} catch (HibernateException e) { 
				if (tx!=null) tx.rollback(); 
				e.printStackTrace();  
			} finally { 
				session.close();  
			} 
			return false;
		}
	}

	@Override
	public boolean deletePstLocation() {
		Session session = factory.openSession(); 
		Transaction tx = null; 
		try{ 
			tx = session.beginTransaction(); 
			NonUser nonUser = (NonUser)session.get(NonUser.class, "pstLocation");
			session.delete(nonUser);
			tx.commit(); 
			return true;
		} catch (HibernateException e) { 
			if (tx!=null) tx.rollback(); 
			e.printStackTrace();  
		} finally { 
			session.close();  
		} 
		return false;
	}

	@Override
	public boolean deleteNonUserField(String fieldName) {
		if(fieldName == null) {
			return false;
		} else {
			Session session = factory.openSession(); 
			Transaction tx = null; 
			try{ 
				tx = session.beginTransaction(); 
				NonUser nonUser = (NonUser)session.get(NonUser.class, fieldName);
				session.delete(nonUser);
				tx.commit(); 
				return true;
			}catch (HibernateException e) { 
				if (tx!=null) tx.rollback(); 
				e.printStackTrace();  
			}finally { 
				session.close();  
			} 
			return false;
		}
	}
	

}
