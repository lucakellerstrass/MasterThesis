package kellerstrass.marketInformation;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
import org.hsqldb.persist.HsqlProperties;
import org.hsqldb.server.Server;
import java.text.SimpleDateFormat;

/**
 * This class provides the embedded HyperSQL Database - start/stop of the server
 * - generation of the schema and tables - methods for writing and reading the
 * database with predefined queries - methods to provide data arrays for given
 * dates
 * 
 * @author lucak
 *
 */
public class SourceDatabase {

	private Server server = null;
	private Connection con = null;
	private List<Integer> DayList = new ArrayList<Integer>();
	private List<Double> DiscList = new ArrayList<Double>();	
	private List<String> TermList = new ArrayList<String>();
	private List<String> ToDateList = new ArrayList<String>();
	private List<Double> VolatilityList = new ArrayList<Double>();
	private String MarketDataDate = null;
	private String ZeroRatesDate = null;

	public SourceDatabase(String rootpath) {
		/**
		 * URL: jdbc:hsqldb:hsql://localhost/masterthesisdb
		 */

		try {
			HsqlProperties p = new HsqlProperties();
			p.setProperty("server.database.1", "file:" + rootpath + "/hsqldb/masterthesisdb");
			p.setProperty("server.dbname.1", "masterthesisdb");
			// set up the rest of properties

			// alternative to the above is
			server = new Server();
			server.setProperties(p);
			server.setLogWriter(null); // can use custom writer
			server.setErrWriter(null); // can use custom writer
			server.start();
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}

		try {
			// Registering the HSQLDB JDBC driver
			Class.forName("org.hsqldb.jdbc.JDBCDriver");
			// Creating the connection with HSQLDB
			con = DriverManager.getConnection("jdbc:hsqldb:hsql://localhost/masterthesisdb", "SA", "");
			if (con != null) {
				System.out.println("Database connection created successfully");

			} else {
				System.out.println("Problem with creating database connection");
			}

		} catch (Exception e) {
			e.printStackTrace(System.out);
		}

		PreparedStatement pstmt = null;
		try {
			pstmt = con.prepareStatement("CREATE SCHEMA  IF NOT EXISTS MASTERTHESIS AUTHORIZATION SA");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		boolean result;
		try {
			result = pstmt.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			pstmt = con.prepareStatement(" CREATE TABLE IF NOT EXISTS MASTERTHESIS.MarketData ("
					+ "  ID INT IDENTITY PRIMARY KEY," + "  Ccy varchar(10) NOT NULL, "
					+ "  Index varchar(20) NOT NULL," + "  Type varchar(100) NOT NULL," + "  Term varchar(10) NOT NULL,"
					+ "  To_Date varchar(10) NOT NULL," + "  Volatility float NOT NULL," + "  AsOfDate date NOT NULL,"
					+ "  Curve_ID varchar(100) NOT NULL,)");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			result = pstmt.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			pstmt = con.prepareStatement(" CREATE TABLE IF NOT EXISTS MASTERTHESIS.ZeroRates  ("
					+ "  ID INT IDENTITY PRIMARY KEY," + "  Ccy varchar(10) NOT NULL," + "  Index varchar(20) NOT NULL,"
					+ "  AsOfDate date NOT NULL," + "  Date date NOT NULL," + "  Days int NOT NULL,"
					+ "  Rate float NOT NULL," + "  Disc float NOT NULL,)");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			result = pstmt.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void shutdownDatabase() {
		try {
			server.shutdownCatalogs(1);
			System.out.println("Database connection shutdowm successfully");
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
	}

	private PreparedStatement setMarketData(PreparedStatement pstmt, String Ccy, String Index, String Type, String Term,
			String To_Date, String Volatility, String AsOfDate, String Curve_ID) throws Exception {
		int i = 1;
		if (Ccy != null) {
			pstmt.setString(i, Ccy);
			i++;
		}
		if (Index != null) {
			pstmt.setString(i, Index);
			i++;
		}
		if (Type != null) {
			pstmt.setString(i, Type);
			i++;
		}
		if (Term != null) {
			pstmt.setString(i, Term);
			i++;
		}
		if (To_Date != null) {
			pstmt.setString(i, To_Date);
			i++;
		}
		if (Volatility != null) {
			Double Volatility_D = Double.parseDouble(Volatility);
			pstmt.setDouble(i, Volatility_D);
			i++;
		}
		if (AsOfDate != null) {
			SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
			Date parsed = format.parse(AsOfDate);
			java.sql.Date sqlDate = new java.sql.Date(parsed.getTime());
			pstmt.setDate(i, sqlDate);
			i++;
		}
		if (Curve_ID != null) {
			pstmt.setString(i, Curve_ID);
			i++;
		}
		return pstmt;
	};

	private PreparedStatement setZeroRates(PreparedStatement pstmt, String Ccy, String Index, String AsOfDate,
			String Date, String Days, String Rate, String Disc) throws Exception {
		int i = 1;
		if (Ccy != null) {
			pstmt.setString(i, Ccy);
			i++;
		}
		if (Index != null) {
			pstmt.setString(i, Index);
			i++;
		}
		if (AsOfDate != null) {
			SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
			Date parsed = format.parse(AsOfDate);
			java.sql.Date sqlDate = new java.sql.Date(parsed.getTime());
			pstmt.setDate(i, sqlDate);
			i++;
		}
		if (Date != null) {
			SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
			Date parsed = format.parse(Date);
			java.sql.Date sqlDate = new java.sql.Date(parsed.getTime());
			pstmt.setDate(i, sqlDate);
			i++;
		}
		if (Days != null) {
			Integer Days_I = Integer.parseInt(Days);
			pstmt.setInt(i, Days_I);
			i++;
		}
		if (Rate != null) {
			Double Rate_D = Double.parseDouble(Rate);
			pstmt.setDouble(i, Rate_D);			
			i++;
		}
		if (Disc != null) {
			Double Disc_D = Double.parseDouble(Disc);
			pstmt.setDouble(i, Disc_D);
			i++;
		}
		return pstmt;
	};

	public boolean insertMarketData(String Ccy, String Index, String Type, String Term,
			String To_Date, String Volatility, String AsOfDate, String Curve_ID) {
		java.sql.ResultSet resultSet = null;
		PreparedStatement pstmt = null;
		boolean result = false;

		// Trying to insert MarketData - but only once. Duplicate data is not allowed.
		try {
			pstmt = con.prepareStatement(
					"Select Ccy,Index,Type,Term,To_Date,Volatility,AsOfDate, Curve_ID from MASTERTHESIS.MarketData where "
							+ "Ccy = ? and Index = ? and Type = ? and Term = ? and To_Date = ? and Volatility = ? and AsOfDate = ? and Curve_ID = ?");
		} catch (SQLException e) {
			System.out.println(e.toString());
			return result;
		}
		try {
			pstmt = setMarketData(pstmt, Ccy,Index,Type,Term,To_Date,Volatility,AsOfDate, Curve_ID);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return result;
		}
		try {
			resultSet = pstmt.executeQuery();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return result;
		}
		try {
			if (resultSet.next()) {
				System.out.println("Data already in database - no further action required");
			} else {
				try {
					pstmt = con.prepareStatement(
							"Insert into MASTERTHESIS.MarketData (Ccy,Index,Type,Term, To_Date,Volatility,AsOfDate,Curve_ID) VALUES"
									+ "(?,	?, ?,?,?,?,?,?)");
				} catch (SQLSyntaxErrorException e) {
					System.out.println(e.toString());
					return result;
				}
				pstmt = setMarketData(pstmt, Ccy,Index,Type,Term,To_Date,Volatility,AsOfDate, Curve_ID);
				result = pstmt.execute();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return result;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return result;
		}

		return result;
	}

	public boolean insertZeroRates(String Ccy, String Index, String AsOfDate,
			String Date, String Days, String Rate, String Disc) {
		java.sql.ResultSet resultSet = null;
		PreparedStatement pstmt = null;
		boolean result = false;

		// Trying to insert ZeroRates data - but only once. Duplicate data is not
		// allowed.

		try {
			pstmt = con
					.prepareStatement("Select Ccy,Index,AsOfDate,Date,Days,Rate,Disc from MASTERTHESIS.ZeroRates where "
							+ "Ccy = ? and Index = ? and AsOfDate = ? and Date = ? and Days = ? and Rate = ? and Disc = ? ");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return result;
		}

		try {
			pstmt = setZeroRates(pstmt, Ccy,Index,AsOfDate,Date,Days,Rate,Disc);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return result;
		}
		try {
			resultSet = pstmt.executeQuery();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return result;
		}
		try {
			if (resultSet.next()) {
				System.out.println("Data already in database - no further action required");
			} else {
				try {
					pstmt = con.prepareStatement(
							"Insert into MASTERTHESIS.ZeroRates (Ccy,Index,AsOfDate,Date,Days,Rate,Disc) VALUES"
									+ "(?,	?, ?,?,?,?,?)");
				} catch (SQLSyntaxErrorException e) {
					System.out.println(e.toString());
					return result;
				}
				pstmt = setZeroRates(pstmt, Ccy,Index,AsOfDate,Date,Days,Rate,Disc);
				result = pstmt.execute();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return result;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return result;
		}
		result = true;
		return result;
	}
	
	public boolean readZeroRatesToDate(String date) {
		java.sql.ResultSet resultSet = null;
		PreparedStatement pstmt = null;
		boolean result = false;		
		this.ZeroRatesDate = date;
		this.DayList = null;
		this.DiscList = null;

			try {
				pstmt = con.prepareStatement("Select AsOfDate,Days,Disc from MASTERTHESIS.ZeroRates where "
								+ "AsOfDate = ?");
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return result;
			}
			try {
				pstmt = setZeroRates(pstmt, null, null, date, null, null, null, null);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return result;
			}
			try {
				resultSet = pstmt.executeQuery();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return result;
			}

		try {

			List<Integer> DayListPerDate = new ArrayList<Integer>();
			List<Double> DiscListPerDate = new ArrayList<Double>();
			while (resultSet.next()) {
				int Days = resultSet.getInt("Days");
				double Disc = resultSet.getDouble("Disc");
				DayListPerDate.add(Days);
				DiscListPerDate.add(Disc);
				if (resultSet.isLast()) {
					break;
				}
			}
			this.DayList = DayListPerDate;
			this.DiscList = DiscListPerDate;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return result;
		}
		result = true;
		return result;
		
	}

	public boolean readMarketDataToDate(String date) {
		java.sql.ResultSet resultSet = null;
		PreparedStatement pstmt = null;
		boolean result = false;		
		this.MarketDataDate = date;
		this.TermList = null;
		this.ToDateList = null;
		this.VolatilityList = null;

			try {
				pstmt = con.prepareStatement("Select AsOfDate,Term,To_Date,Volatility from MASTERTHESIS.MarketData where "
								+ "AsOfDate = ?");
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return result;
			}
			try {
				pstmt = setMarketData(pstmt, null, null, null, null, null, null, date, null);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return result;
			}
			try {
				resultSet = pstmt.executeQuery();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return result;
			}

		try {

			List<String> TermListPerDate = new ArrayList<String>();
			List<String> ToDateListPerDate = new ArrayList<String>();
			List<Double> VolatilityListPerDate = new ArrayList<Double>();

			while (resultSet.next()) {
				String Term = resultSet.getString("Term");
				String ToDate = resultSet.getString("To_Date");
				double Volatility = resultSet.getDouble("Volatility");
				TermListPerDate.add(Term);
				ToDateListPerDate.add(ToDate);
				VolatilityListPerDate.add(Volatility);
				if (resultSet.isLast()) {
					break;
				}
			}
			this.TermList = TermListPerDate;
			this.ToDateList = ToDateListPerDate;
			this.VolatilityList = VolatilityListPerDate;
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return result;
		}
		result = true;
		return result;
		
	}
}
