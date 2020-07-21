package kellerstrass.marketInformation.test;

import kellerstrass.marketInformation.SourceDatabase;

public class SourceDataReader {

	public static void main(String[] args) throws Exception {
		
		
		testdatabase();
		
		

	}

	
	 private static void testdatabase() throws Exception {
	        /**
	         * URL: jdbc:hsqldb:hsql://localhost/masterthesisdb
	         */
	       
	       
	        SourceDatabase db = new SourceDatabase("C:\\Users\\lucak\\git\\MasterThesis\\Master-Thesis_Luca_Kellerstrass_Copy");
	        Boolean result = null;
	        //result = db.insertMarketData("EUR", "SHIFT", "SWOPT_IRFWDVOL", "1Y", "1M", "3.0", "18.09.2018", "DEFEUROP");
	        //result = db.insertZeroRates("EUR", "OIS6M", "15.05.2017", "18.09.2018", "183", "-0.27292", "1.00137");
	        result = db.readZeroRatesToDate("15.05.2017");
	        //db.DayList
	        //db.DiscList
	        //db.ZeroRatesDate
	        return;
	    }



}
