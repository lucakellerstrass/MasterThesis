package kellerstrass.marketInformation;

import java.time.LocalDate;
import java.time.Month;


public class CurveModelData {
	
	private  CurveModelDataType curveModelDataType;
	
	
	private String[]  maturitySet;
	private String[]  frequencySet;
	private String[]  frequencyFloatSet;
	private String[]  daycountConventionsSet;
	private String[]  daycountConventionsFloatSet;
	private double[]  ratesSet;
	private LocalDate localDateSet;
	private String    forwardCurveTenorSet;
	private int		  spotOffsetDaysSet;
	private String	  forwardStartPeriodSet;
	
	
	
	
	
	
	/**
	 * Initiate an instance of the calibration (market) information for the Curve Model 
	 * @param CurveModelDataType The data Type or source of the information
	 * @return
	 * @throws SolverException
	 */
	public  CurveModelData(CurveModelDataType curveModelDataType){   
		this.curveModelDataType = curveModelDataType;
	}
	

	
	/**
	 * Initiate an instance of the calibration (market) information to be filled by setting the values
	 * @param maturity
	 * @param frequency
	 * @param frequencyFloat
	 * @param daycountConventions
	 * @param daycountConventionsFloat
	 * @param rates
	 * @param localDate
	 * @param forwardCurveTenor
	 * @param spotOffsetDays
	 * @param forwardStartPeriod
	 */
	public  CurveModelData(String[] maturity, String[] frequency, String[] frequencyFloat,String[] daycountConventions,
			String[] daycountConventionsFloat, double[] rates, LocalDate localDate,  String forwardCurveTenor,
			 int spotOffsetDays, String forwardStartPeriod){   
		
		this.curveModelDataType = CurveModelDataType.SETTEDVALUES;
		this.maturitySet = maturity;
		this.frequencySet = frequency;
		this.frequencyFloatSet = frequencyFloat;
		this.daycountConventionsSet = daycountConventions;
		this.daycountConventionsFloatSet = daycountConventionsFloat;
		this.ratesSet = rates;
		this.localDateSet = localDate;
		this.forwardCurveTenorSet = forwardCurveTenor;
		this.spotOffsetDaysSet = spotOffsetDays;
		this.forwardStartPeriodSet = forwardStartPeriod;
	}
	


	
	
	
	
	
/**
 * Getter for the selected Curve Model Data Type
 * @return
 */
	public CurveModelDataType getCurveModelDataType() {
		return curveModelDataType;
	}

	
	/**
	 * Getter for the Maturity
	 * @return
	 */
	public String[] getMaturity() {
		switch(curveModelDataType) {
		case Example:
			String[] maturityExample	= { "6M", "1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "11Y", "12Y", "15Y", "20Y", "25Y", "30Y", "35Y", "40Y", "45Y", "50Y" };
			return maturityExample;
			
		case REALDATA:
			String[] maturityRealData	= null;
			return maturityRealData;
			
		case SETTEDVALUES:
			return maturitySet;	
			
		default: return null;			
		}
		
		
		
	}
/**
 * Getter for the frequency
 * @return
 */
	public String[] getFrequency() {
		switch(curveModelDataType) {
		case Example:
			String[] frequencyExample	={ "annual", "annual", "annual", "annual", "annual", "annual", "annual", "annual", "annual", "annual", "annual", "annual", "annual", "annual", "annual", "annual", "annual", "annual", "annual", "annual", "annual" };
			return frequencyExample;
			
		case REALDATA:
			String[] frequencyRealData	= null;
			return frequencyRealData;
			
		case SETTEDVALUES:
			return frequencySet;	
			
		default: return null;			
		}

	}
	
	/**
	 * Getter for the frequency Float
	 * @return
	 */
		public String[] getFrequencyFloat() {
			switch(curveModelDataType) {
			case Example:
				String[] frequencyFloatExample	= { "semiannual", "semiannual", "semiannual", "semiannual", "semiannual", "semiannual", "semiannual", "semiannual", "semiannual", "semiannual", "semiannual", "semiannual", "semiannual", "semiannual", "semiannual", "semiannual", "semiannual", "semiannual", "semiannual", "semiannual", "semiannual" };
				return frequencyFloatExample;
				
			case REALDATA:
				String[] frequencyFloatRealData	= null;
				return frequencyFloatRealData;
				
			case SETTEDVALUES:
				return frequencyFloatSet;	
				
			default: return null;			
			}

		}
	
		/**
		 * Getter for the daycount Conventions
		 * @return
		 */
	public String[] getDaycountConventions() {
		switch(curveModelDataType) {
		case Example:
			String[] daycountConventionsExample	={ "ACT/360", "E30/360", "E30/360", "E30/360", "E30/360", "E30/360", "E30/360", "E30/360", "E30/360", "E30/360", "E30/360", "E30/360", "E30/360", "E30/360", "E30/360", "E30/360", "E30/360", "E30/360", "E30/360", "E30/360", "E30/360" };
			return daycountConventionsExample;
			
		case REALDATA:
			String[] daycountConventionsRealData	= null;
			return daycountConventionsRealData;
			
		case SETTEDVALUES:
			return daycountConventionsSet;	
			
		default: return null;			
		}
	}

	/**
	 * Getter for the daycount Conventions Float
	 * @return
	 */
	public String[] getDaycountConventionsFloat() {
		switch(curveModelDataType) {
		case Example:
			String[] daycountConventionsFloatExample	= { "ACT/360", "ACT/360", "ACT/360", "ACT/360", "ACT/360", "ACT/360", "ACT/360", "ACT/360", "ACT/360", "ACT/360", "ACT/360", "ACT/360", "ACT/360", "ACT/360", "ACT/360", "ACT/360", "ACT/360", "ACT/360", "ACT/360", "ACT/360", "ACT/360" };
			return daycountConventionsFloatExample;
			
		case REALDATA:
			String[] daycountConventionsFloatRealData	= null;
			return daycountConventionsFloatRealData;
			
		case SETTEDVALUES:
			return daycountConventionsFloatSet;	
			
		default: return null;			
		}
	}

	/**
	 * Getter for the swap rates
	 * @return
	 */
	public double[] getRates() {
		switch(curveModelDataType) {
		case Example:
			double[] ratesExample	={ -0.00216 ,-0.00208 ,-0.00222 ,-0.00216 ,-0.0019 ,-0.0014 ,-0.00072 ,0.00011 ,0.00103 ,0.00196 ,0.00285 ,0.00367 ,0.0044 ,0.00604 ,0.00733 ,0.00767 ,0.00773 ,0.00765 ,0.00752 ,0.007138 ,0.007 };
			return ratesExample;
			
		case REALDATA:
			double[] ratesRealData	= null;
			return ratesRealData;
			
		case SETTEDVALUES:
			return ratesSet;	
			
		default: return null;			
		}
	}

	/**
	 * Getter for the local date
	 * @return
	 */
	public LocalDate getLocalDate() {
		switch(curveModelDataType) {
		case Example:
			return LocalDate.of(2016, Month.SEPTEMBER, 30);
			
		case REALDATA:
			LocalDate localDateRealData	= null;
			return localDateRealData;
			
		case SETTEDVALUES:
			return localDateSet;	
			
		default: return null;			
		}
	}

	/**
	 * Getter for the forwardCurveTenor
	 * @return
	 */
	public String getForwardCurveTenor() {
		switch(curveModelDataType) {
		case Example:
			return "6M";
			
		case REALDATA:
			String forwardCurveTenorRealData	= null;
			return forwardCurveTenorRealData;
			
		case SETTEDVALUES:
			return forwardCurveTenorSet;	
			
		default: return null;			
		}
	}
	
	
	/**
	 * Getter for the spotOffsetDays
	 * @return
	 */
	public int getSpotOffsetDays() {
		switch(curveModelDataType) {
		case Example:
			return 2;
			
		case REALDATA:
			int spotOffsetDaysRealData	= 0;
			return spotOffsetDaysRealData;
			
		case SETTEDVALUES:
			return spotOffsetDaysSet;	
			
		default: return 0;			
		}
	}


	/**
	 * Getter for the forwardStartPeriod
	 * @return
	 */
	public String getforwardStartPeriod() {
		switch(curveModelDataType) {
		case Example:
			return "0D";
			
		case REALDATA:
			String forwardStartPeriodRealData	= null;
			return forwardStartPeriodRealData;
			
		case SETTEDVALUES:
			return forwardStartPeriodSet;	
			
		default: return null;			
		}
	}
	




}
