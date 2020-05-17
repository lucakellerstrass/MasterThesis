package kellerstrass.marketInformation;

import java.time.LocalDate;
import java.time.Month;

import net.finmath.marketdata.model.curves.DiscountCurve;
import net.finmath.marketdata.model.curves.DiscountCurveInterpolation;
import net.finmath.marketdata.model.curves.ForwardCurveInterpolation;
import net.finmath.marketdata.model.curves.CurveInterpolation.ExtrapolationMethod;
import net.finmath.marketdata.model.curves.CurveInterpolation.InterpolationEntity;
import net.finmath.marketdata.model.curves.CurveInterpolation.InterpolationMethod;
import net.finmath.marketdata.model.curves.ForwardCurveInterpolation.InterpolationEntityForward;

public class CurveModelData {

	private CurveModelDataType curveModelDataType;

	private String[] maturitySet;
	private String[] frequencySet;
	private String[] frequencyFloatSet;
	private String[] daycountConventionsSet;
	private String[] daycountConventionsFloatSet;
	private double[] ratesSet;
	private LocalDate localDateSet;
	private String forwardCurveTenorSet;
	private int spotOffsetDaysSet;
	private String forwardStartPeriodSet;

	private boolean initiationOverExistingDiscountValues = false;

	public boolean isInitiationOverExistingDiscountValues() {
		return initiationOverExistingDiscountValues;
	}

	/**
	 * Initiate an instance of the calibration (market) information for the Curve
	 * Model
	 * 
	 * @param CurveModelDataType The data Type or source of the information
	 * @return
	 * @throws SolverException
	 */
	public CurveModelData(CurveModelDataType curveModelDataType) {
		this.curveModelDataType = curveModelDataType;
		if (curveModelDataType == CurveModelDataType.OIS6M) {
			initiationOverExistingDiscountValues = true;
		}
	}

	/**
	 * Initiate an instance of the calibration (market) information to be filled by
	 * setting the values
	 * 
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
	public CurveModelData(String[] maturity, String[] frequency, String[] frequencyFloat, String[] daycountConventions,
			String[] daycountConventionsFloat, double[] rates, LocalDate localDate, String forwardCurveTenor,
			int spotOffsetDays, String forwardStartPeriod) {

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
	 * 
	 * @return
	 */
	public CurveModelDataType getCurveModelDataType() {
		return curveModelDataType;
	}

	/**
	 * Getter for the Maturity
	 * 
	 * @return
	 */
	public String[] getMaturity() {
		switch (curveModelDataType) {
		case Example:
			String[] maturityExample = { "6M", "1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "11Y",
					"12Y", "15Y", "20Y", "25Y", "30Y", "35Y", "40Y", "45Y", "50Y" };
			return maturityExample;

		case SETTEDVALUES:
			return maturitySet;

		default:
			return null;
		}

	}

	/**
	 * Getter for the frequency
	 * 
	 * @return
	 */
	public String[] getFrequency() {
		switch (curveModelDataType) {
		case Example:
			String[] frequencyExample = { "annual", "annual", "annual", "annual", "annual", "annual", "annual",
					"annual", "annual", "annual", "annual", "annual", "annual", "annual", "annual", "annual", "annual",
					"annual", "annual", "annual", "annual" };
			return frequencyExample;

		case SETTEDVALUES:
			return frequencySet;

		default:
			return null;
		}

	}

	/**
	 * Getter for the frequency Float
	 * 
	 * @return
	 */
	public String[] getFrequencyFloat() {
		switch (curveModelDataType) {
		case Example:
			String[] frequencyFloatExample = { "semiannual", "semiannual", "semiannual", "semiannual", "semiannual",
					"semiannual", "semiannual", "semiannual", "semiannual", "semiannual", "semiannual", "semiannual",
					"semiannual", "semiannual", "semiannual", "semiannual", "semiannual", "semiannual", "semiannual",
					"semiannual", "semiannual" };
			return frequencyFloatExample;

		case SETTEDVALUES:
			return frequencyFloatSet;

		default:
			return null;
		}

	}

	/**
	 * Getter for the daycount Conventions
	 * 
	 * @return
	 */
	public String[] getDaycountConventions() {
		switch (curveModelDataType) {
		case Example:
			String[] daycountConventionsExample = { "ACT/360", "E30/360", "E30/360", "E30/360", "E30/360", "E30/360",
					"E30/360", "E30/360", "E30/360", "E30/360", "E30/360", "E30/360", "E30/360", "E30/360", "E30/360",
					"E30/360", "E30/360", "E30/360", "E30/360", "E30/360", "E30/360" };
			return daycountConventionsExample;

		case SETTEDVALUES:
			return daycountConventionsSet;

		default:
			return null;
		}
	}

	/**
	 * Getter for the daycount Conventions Float
	 * 
	 * @return
	 */
	public String[] getDaycountConventionsFloat() {
		switch (curveModelDataType) {
		case Example:
			String[] daycountConventionsFloatExample = { "ACT/360", "ACT/360", "ACT/360", "ACT/360", "ACT/360",
					"ACT/360", "ACT/360", "ACT/360", "ACT/360", "ACT/360", "ACT/360", "ACT/360", "ACT/360", "ACT/360",
					"ACT/360", "ACT/360", "ACT/360", "ACT/360", "ACT/360", "ACT/360", "ACT/360" };
			return daycountConventionsFloatExample;

		case SETTEDVALUES:
			return daycountConventionsFloatSet;

		default:
			return null;
		}
	}

	/**
	 * Getter for the swap rates
	 * 
	 * @return
	 */
	public double[] getRates() {
		switch (curveModelDataType) {
		case Example:
			double[] ratesExample = { -0.00216, -0.00208, -0.00222, -0.00216, -0.0019, -0.0014, -0.00072, 0.00011,
					0.00103, 0.00196, 0.00285, 0.00367, 0.0044, 0.00604, 0.00733, 0.00767, 0.00773, 0.00765, 0.00752,
					0.007138, 0.007 };
			return ratesExample;

		case SETTEDVALUES:
			return ratesSet;

		default:
			return null;
		}
	}

	/**
	 * Getter for the local date
	 * 
	 * @return
	 */
	public LocalDate getLocalDate() {
		switch (curveModelDataType) {
		case Example:
			return LocalDate.of(2016, Month.SEPTEMBER, 30);

		case SETTEDVALUES:
			return localDateSet;

		default:
			return null;
		}
	}

	/**
	 * Getter for the forwardCurveTenor
	 * 
	 * @return
	 */
	public String getForwardCurveTenor() {
		switch (curveModelDataType) {
		case Example:
			return "6M";

		case SETTEDVALUES:
			return forwardCurveTenorSet;

		default:
			return null;
		}
	}

	/**
	 * Getter for the spotOffsetDays
	 * 
	 * @return
	 */
	public int getSpotOffsetDays() {
		switch (curveModelDataType) {
		case Example:
			return 2;

		case SETTEDVALUES:
			return spotOffsetDaysSet;

		default:
			return 0;
		}
	}

	/**
	 * Getter for the forwardStartPeriod
	 * 
	 * @return
	 */
	public String getforwardStartPeriod() {
		switch (curveModelDataType) {
		case Example:
			return "0D";

		case SETTEDVALUES:
			return forwardStartPeriodSet;

		default:
			return null;
		}
	}

	/*
	 * ++++++++++++++++ In the case of existing discount curve Data ++++++++++++++++
	 */

	/**
	 * In the case the market information is stored as a discount curve one can get the discount curve
	 * @return
	 */
	public DiscountCurve getDiscountCurve() {
		if(initiationOverExistingDiscountValues == false) {
			System.out.println("The CurveModelInformation is not stored in a way we can get the discount curve directly.");
			return null;
		}
		else {
			DiscountCurveInterpolation discountCurveInterpolation
			= DiscountCurveInterpolation.createDiscountCurveFromDiscountFactors(
					"discountCurve-" + "EUR",
					LocalDate.of(2019, Month.OCTOBER, 24),
					new double[] {0.00273972602739726,0.512328767123288,0.594520547945205,0.682191780821918,0.761643835616438,1.01369863013699,1.26575342465753,1.51232876712329,1.76164383561644,2.01369863013699,2.26575342465753,2.51780821917808,2.76986301369863,3.01369863013699,3.26575342465753,3.51780821917808,3.76986301369863,4.01917808219178,4.27397260273973,4.52054794520548,4.77260273972603,5.01643835616438,5.26849315068493,5.52054794520548,5.77260273972603,6.01643835616438,6.26849315068493,6.52054794520548,6.77260273972603,7.01643835616438,7.26849315068493,7.52054794520548,7.77260273972603,8.01643835616438,8.26849315068493,8.51780821917808,8.76712328767123,9.02465753424658,9.27945205479452,9.52328767123288,9.77534246575343,10.0219178082192,10.2739726027397,10.5205479452055,10.7698630136986,11.0219178082192,11.2739726027397,11.5205479452055,11.7698630136986,12.0191780821918,12.2712328767123,12.5205479452055,12.7698630136986,13.0219178082192,13.2739726027397,13.5205479452055,13.7698630136986,14.0219178082192,14.2739726027397,14.5205479452055,14.7698630136986,15.027397260274,16.027397260274,17.0301369863014,18.0301369863014,19.0301369863014,20.0246575342466,21.027397260274,22.027397260274,23.027397260274,24.027397260274,25.0301369863014,26.0301369863014,27.0301369863014,28.0301369863014,29.0328767123288,30.0328767123288,31.0328767123288,32.0328767123288,33.0356164383562,34.0356164383562,35.0356164383562,36.0356164383562,37.0383561643836,38.0383561643836,39.0383561643836,40.0383561643836,41.041095890411,42.041095890411,43.041095890411,44.041095890411,45.0438356164384,46.0438356164384,47.0438356164384,48.0438356164384,49.0465753424658,50.0465753424658,51.0465753424658,52.0465753424658,53.0493150684932,54.0493150684932,55.0493150684932,56.0493150684932,57.0520547945206,58.0520547945206,59.0520547945206,60.0575342465753},
					new double[] {1.000009667,1.001810872,1.002101476,1.002442115,1.002758935,1.003724221,1.004720596,1.005659486,1.006578679,1.007482925,1.008351226,1.009176027,1.009951598,1.010650595,1.011316468,1.011921701,1.012464084,1.012937465,1.013355069,1.013691222,1.013960219,1.014143079,1.014246609,1.014260039,1.014181057,1.014014887,1.013749035,1.013387642,1.012931615,1.01240182,1.011764142,1.011035299,1.010215483,1.00933623,1.008339912,1.007272927,1.00613262,1.004886851,1.003594705,1.002303747,1.000912566,0.99949485,0.997987195,0.996457401,0.994858627,0.993192637,0.991480436,0.989764484,0.987992181,0.98618647,0.98433156,0.982470556,0.980586509,0.978661506,0.976719394,0.97480613,0.972861397,0.970888211,0.968911349,0.966977268,0.965024915,0.963015271,0.955332812,0.94787977,0.940761837,0.93400426,0.927668865,0.921675096,0.916102825,0.910926777,0.906124801,0.901649465,0.897483604,0.893611269,0.890031746,0.8867315,0.883719524,0.88097616,0.878497206,0.87627319,0.874310315,0.872594041,0.871114097,0.869854903,0.868808692,0.867954916,0.867274549,0.86674544,0.866350845,0.86606798,0.865873781,0.865742941,0.865649133,0.865562959,0.865453467,0.865287161,0.865029966,0.864651583,0.864155008,0.863549832,0.862852504,0.862076329,0.861237814,0.860352669,0.859445593,0.858535671,0.857641193},
					null,
					InterpolationMethod.LINEAR,
					ExtrapolationMethod.CONSTANT,
					InterpolationEntity.VALUE);

			
			return discountCurveInterpolation;
		}
		
		
		
		
		
	}

}
