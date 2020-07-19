package kellerstrass.marketInformation;

import java.time.LocalDate;
import java.time.Month;

import kellerstrass.useful.StringToUseful;
import net.finmath.marketdata.model.curves.DiscountCurve;
import net.finmath.marketdata.model.curves.DiscountCurveInterpolation;
import net.finmath.marketdata.model.curves.ForwardCurveInterpolation;
import net.finmath.marketdata.model.curves.CurveInterpolation.ExtrapolationMethod;
import net.finmath.marketdata.model.curves.CurveInterpolation.InterpolationEntity;
import net.finmath.marketdata.model.curves.CurveInterpolation.InterpolationMethod;
import net.finmath.marketdata.model.curves.ForwardCurveInterpolation.InterpolationEntityForward;

/**
 * CurveModelData is a container-class and contains Curve Model information, or
 * information for Curve Model calibration.
 * 
 * @author lucak
 *
 */
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
	private String dataDay;

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
		if ((curveModelDataType == CurveModelDataType.OIS6M2410) | (curveModelDataType == CurveModelDataType.OIS6M2310)
				| (curveModelDataType == CurveModelDataType.Market)) {

			if (curveModelDataType == CurveModelDataType.Market) {
				System.out.println("For the usage of Market data use the constructor disigned for this purpose.");
			}
			initiationOverExistingDiscountValues = true;
		}
	}

	/**
	 * Initiate an instance of the calibration market information for the Curve
	 * Model using then given day as String.
	 * 
	 * @param CurveModelDataType The data Type or source of the information
	 * @return
	 * @throws SolverException
	 */
	public CurveModelData(String dataDay) {
		this.curveModelDataType = CurveModelDataType.Market;
		initiationOverExistingDiscountValues = true;
		this.dataDay = dataDay;

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
			return LocalDate.of(2019, Month.OCTOBER, 24);

		case SETTEDVALUES:
			return localDateSet;

		case OIS6M2310:
			return LocalDate.of(2019, Month.OCTOBER, 23);

		case OIS6M2410:
			return LocalDate.of(2019, Month.OCTOBER, 24);

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
		case OIS6M2310:
		case OIS6M2410:
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
		case OIS6M2310:
		case OIS6M2410:
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
	 * In the case the market information is stored as a discount curve one can get
	 * the discount curve
	 * 
	 * @return
	 */
	public DiscountCurve getDiscountCurve() {
		if (initiationOverExistingDiscountValues == false) {
			System.out.println(
					"The CurveModelInformation is not stored in a way we can get the discount curve directly.");
			return null;
		} else {

			switch (curveModelDataType) {
			case OIS6M2410:

				DiscountCurveInterpolation discountCurveInterpolation2410 = DiscountCurveInterpolation
						.createDiscountCurveFromDiscountFactors("discountCurve-" + "EUR",
								LocalDate.of(2019, Month.OCTOBER, 24),
								new double[] { 0.00273972602739726, 0.512328767123288, 0.594520547945205,
										0.682191780821918, 0.761643835616438, 1.01369863013699, 1.26575342465753,
										1.51232876712329, 1.76164383561644, 2.01369863013699, 2.26575342465753,
										2.51780821917808, 2.76986301369863, 3.01369863013699, 3.26575342465753,
										3.51780821917808, 3.76986301369863, 4.01917808219178, 4.27397260273973,
										4.52054794520548, 4.77260273972603, 5.01643835616438, 5.26849315068493,
										5.52054794520548, 5.77260273972603, 6.01643835616438, 6.26849315068493,
										6.52054794520548, 6.77260273972603, 7.01643835616438, 7.26849315068493,
										7.52054794520548, 7.77260273972603, 8.01643835616438, 8.26849315068493,
										8.51780821917808, 8.76712328767123, 9.02465753424658, 9.27945205479452,
										9.52328767123288, 9.77534246575343, 10.0219178082192, 10.2739726027397,
										10.5205479452055, 10.7698630136986, 11.0219178082192, 11.2739726027397,
										11.5205479452055, 11.7698630136986, 12.0191780821918, 12.2712328767123,
										12.5205479452055, 12.7698630136986, 13.0219178082192, 13.2739726027397,
										13.5205479452055, 13.7698630136986, 14.0219178082192, 14.2739726027397,
										14.5205479452055, 14.7698630136986, 15.027397260274, 16.027397260274,
										17.0301369863014, 18.0301369863014, 19.0301369863014, 20.0246575342466,
										21.027397260274, 22.027397260274, 23.027397260274, 24.027397260274,
										25.0301369863014, 26.0301369863014, 27.0301369863014, 28.0301369863014,
										29.0328767123288, 30.0328767123288, 31.0328767123288, 32.0328767123288,
										33.0356164383562, 34.0356164383562, 35.0356164383562, 36.0356164383562,
										37.0383561643836, 38.0383561643836, 39.0383561643836, 40.0383561643836,
										41.041095890411, 42.041095890411, 43.041095890411, 44.041095890411,
										45.0438356164384, 46.0438356164384, 47.0438356164384, 48.0438356164384,
										49.0465753424658, 50.0465753424658, 51.0465753424658, 52.0465753424658,
										53.0493150684932, 54.0493150684932, 55.0493150684932, 56.0493150684932,
										57.0520547945206, 58.0520547945206, 59.0520547945206, 60.0575342465753 },
								new double[] { 1.000009667, 1.001810872, 1.002101476, 1.002442115, 1.002758935,
										1.003724221, 1.004720596, 1.005659486, 1.006578679, 1.007482925, 1.008351226,
										1.009176027, 1.009951598, 1.010650595, 1.011316468, 1.011921701, 1.012464084,
										1.012937465, 1.013355069, 1.013691222, 1.013960219, 1.014143079, 1.014246609,
										1.014260039, 1.014181057, 1.014014887, 1.013749035, 1.013387642, 1.012931615,
										1.01240182, 1.011764142, 1.011035299, 1.010215483, 1.00933623, 1.008339912,
										1.007272927, 1.00613262, 1.004886851, 1.003594705, 1.002303747, 1.000912566,
										0.99949485, 0.997987195, 0.996457401, 0.994858627, 0.993192637, 0.991480436,
										0.989764484, 0.987992181, 0.98618647, 0.98433156, 0.982470556, 0.980586509,
										0.978661506, 0.976719394, 0.97480613, 0.972861397, 0.970888211, 0.968911349,
										0.966977268, 0.965024915, 0.963015271, 0.955332812, 0.94787977, 0.940761837,
										0.93400426, 0.927668865, 0.921675096, 0.916102825, 0.910926777, 0.906124801,
										0.901649465, 0.897483604, 0.893611269, 0.890031746, 0.8867315, 0.883719524,
										0.88097616, 0.878497206, 0.87627319, 0.874310315, 0.872594041, 0.871114097,
										0.869854903, 0.868808692, 0.867954916, 0.867274549, 0.86674544, 0.866350845,
										0.86606798, 0.865873781, 0.865742941, 0.865649133, 0.865562959, 0.865453467,
										0.865287161, 0.865029966, 0.864651583, 0.864155008, 0.863549832, 0.862852504,
										0.862076329, 0.861237814, 0.860352669, 0.859445593, 0.858535671, 0.857641193 },
								null, InterpolationMethod.LINEAR, ExtrapolationMethod.CONSTANT,
								InterpolationEntity.LOG_OF_VALUE);

				return discountCurveInterpolation2410;

			case OIS6M2310:

				DiscountCurveInterpolation discountCurveInterpolation2310 = DiscountCurveInterpolation
						.createDiscountCurveFromDiscountFactors("discountCurve-" + "EUR",
								LocalDate.of(2019, Month.OCTOBER, 23),
								new double[] { 0.00273972602739726, 0.512328767123288, 0.589041095890411,
										0.673972602739726, 0.761643835616438, 1.01095890410959, 1.26027397260274,
										1.50958904109589, 1.75890410958904, 2.00821917808219, 2.26027397260274,
										2.50684931506849, 2.75616438356164, 3.00821917808219, 3.26027397260274,
										3.50684931506849, 3.75616438356164, 4.00821917808219, 4.26027397260274,
										4.50958904109589, 4.75890410958904, 5.01095890410959, 5.26301369863014,
										5.50958904109589, 5.75890410958904, 6.01643835616438, 6.26849315068493,
										6.51506849315069, 6.76438356164384, 7.01369863013699, 7.26575342465753,
										7.51232876712329, 7.76164383561644, 8.01095890410959, 8.26301369863014,
										8.51232876712329, 8.76164383561644, 9.01369863013699, 9.26575342465753,
										9.51232876712329, 9.76164383561644, 10.013698630137, 10.2657534246575,
										10.5123287671233, 10.7616438356164, 11.013698630137, 11.2657534246575,
										11.5123287671233, 11.7616438356164, 12.0191780821918, 12.2712328767123,
										12.5205479452055, 12.7698630136986, 13.0219178082192, 13.2739726027397,
										13.5205479452055, 13.7698630136986, 14.0219178082192, 14.2739726027397,
										14.5205479452055, 14.7698630136986, 15.0164383561644, 16.0164383561644,
										17.0191780821918, 18.0191780821918, 19.0191780821918, 20.0191780821918,
										21.0219178082192, 22.0219178082192, 23.0219178082192, 24.0219178082192,
										25.0246575342466, 26.0246575342466, 27.0246575342466, 28.0246575342466,
										29.027397260274, 30.027397260274, 31.027397260274, 32.027397260274,
										33.0301369863014, 34.0301369863014, 35.0301369863014, 36.0301369863014,
										37.0328767123288, 38.0328767123288, 39.0328767123288, 40.0383561643836,
										41.041095890411, 42.041095890411, 43.041095890411, 44.041095890411,
										45.0438356164384, 46.0438356164384, 47.0438356164384, 48.0438356164384,
										49.0465753424658, 50.041095890411, 51.041095890411, 52.041095890411,
										53.0438356164384, 54.0438356164384, 55.0438356164384, 56.0438356164384,
										57.0465753424658, 58.0465753424658, 59.0465753424658, 60.0465753424658 },
								new double[] { 1.00000975009506, 1.0018265450089, 1.00206455413664, 1.00239809002812,
										1.0027466585196, 1.00370911297352, 1.00467166524531, 1.00565063334262,
										1.00657761580419, 1.00744351680441, 1.008285477381, 1.00907914142092,
										1.00984423036869, 1.01057053744607, 1.01124011955333, 1.01183598642696,
										1.01237682135904, 1.01285936127959, 1.01327603436783, 1.01361857968824,
										1.01388579801999, 1.01407271957827, 1.01417029837946, 1.01417989700089,
										1.01410644643642, 1.01394750422783, 1.01371416064931, 1.01340792315249,
										1.01301388028669, 1.01252832753568, 1.01193877446441, 1.0112673532177,
										1.01049697113743, 1.00963908996825, 1.00868801793317, 1.00766932462311,
										1.00657836408281, 1.00540792030998, 1.00417543465058, 1.00291253996356,
										1.00158026816813, 1.00017909486494, 0.99872583385452, 0.99725581252134,
										0.99572297923153, 0.99412803396019, 0.99248989232916, 0.99084784601979,
										0.98915014943143, 0.98735951771494, 0.9855737051911, 0.98377762032491,
										0.98195527481022, 0.98008961250398, 0.97820405565722, 0.976343719371099,
										0.974450424460189, 0.97252749402799, 0.97059954530113, 0.96871245165648,
										0.96680721021907, 0.96492957994297, 0.95743904848906, 0.95018328153091,
										0.94326341316593, 0.93670113150979, 0.930519719635839, 0.92470528408279,
										0.91930265966124, 0.9142914647678, 0.909654772791979, 0.90535168226841,
										0.901368939410439, 0.897686917675179, 0.89429792041311, 0.89118093722946,
										0.88833499373714, 0.88573289347375, 0.88337006196393, 0.88123906031315,
										0.8793478011535, 0.87768464577505, 0.876242344142, 0.87500880095665,
										0.873979963854639, 0.87313963522507, 0.87247028803202, 0.87196141120463,
										0.87159212228706, 0.87133889716078, 0.871177749479009, 0.87108241207063,
										0.87102515999951, 0.87097534827673, 0.87090054145897, 0.8707656964129,
										0.870536393715099, 0.87017857709467, 0.869696005073689, 0.86909850232435,
										0.868402732914, 0.86762232803853, 0.86677412291042, 0.86587414277953,
										0.864947565191259, 0.864013865090989, 0.86309656369568 },
								null, InterpolationMethod.LINEAR, ExtrapolationMethod.CONSTANT,
								InterpolationEntity.LOG_OF_VALUE);

				return discountCurveInterpolation2310;

			case Market:

				DiscountCurveInterpolation discountCurveInterpolationMarekt = DiscountCurveInterpolation
						.createDiscountCurveFromDiscountFactors("discountCurve-" + "EUR",
								StringToUseful.referenceDateFromString(dataDay), getTimesFromDataDate(dataDay), // times
																												// from
																												// dataDate
								getDiscountValuesFromDataDate(dataDay), // discount values from dataDate
								null, InterpolationMethod.LINEAR, ExtrapolationMethod.CONSTANT,
								InterpolationEntity.LOG_OF_VALUE);

				return discountCurveInterpolationMarekt;

			default:
				System.out.println(
						"The discount curve is not implemented. Look up in kellerstrass.marketInformation.CurveModelData");
				return null;

			}
		}

	}

	/**
	 * Returns the OIS6M discount curve values for a given data Day provided as
	 * String.
	 * 
	 * @param day
	 * @return
	 */
	private double[] getTimesFromDataDate(String day) {
		// TODO Auto-generated method stub

		// generally looks like this:
//		return new double[] { 0.00273972602739726, 0.501369863013699, 0.597260273972603, 0.671232876712329,
//				0.753424657534247, 1.00547945205479, 1.25479452054795, 1.5041095890411, 1.75616438356164,
//				2.01369863013699, 2.26301369863014, 2.50958904109589, 2.76164383561644, 3.01095890410959,
//				3.26027397260274, 3.50684931506849, 3.75890410958904, 4.00821917808219, 4.25753424657534,
//				4.5041095890411, 4.75616438356164, 5.00821917808219, 5.25753424657534, 5.50684931506849,
//				5.75890410958904, 6.01095890410959, 6.26027397260274, 6.50684931506849, 6.75890410958904,
//				7.01095890410959, 7.26027397260274, 7.50684931506849, 7.75890410958904, 8.01643835616438,
//				8.26575342465753, 8.51232876712329, 8.76438356164384, 9.01369863013699, 9.26301369863014,
//				9.51232876712329, 9.76438356164384, 10.013698630137, 10.2630136986301, 10.5095890410959,
//				10.7616438356164, 11.013698630137, 11.2630136986301, 11.5095890410959, 11.7616438356164,
//				12.013698630137, 12.2630136986301, 12.5095890410959, 12.7616438356164, 13.013698630137,
//				13.2630136986301, 13.5123287671233, 13.7643835616438, 14.0164383561644, 14.2657534246575,
//				14.5123287671233, 14.7643835616438, 15.0164383561644, 16.0164383561644, 17.0164383561644,
//				18.0191780821918, 19.0191780821918, 20.0219178082192, 21.0219178082192, 22.0246575342466,
//				23.0246575342466, 24.0246575342466, 25.027397260274, 26.0301369863014, 27.0301369863014,
//				28.0301369863014, 29.0301369863014, 30.0328767123288, 31.0328767123288, 32.0328767123288,
//				33.0328767123288, 34.0356164383562, 35.0356164383562, 36.0356164383562, 37.0356164383562,
//				38.0383561643836, 39.0383561643836, 40.0328767123288, 41.0328767123288, 42.0356164383562,
//				43.0356164383562, 44.0356164383562, 45.0356164383562, 46.0383561643836, 47.0383561643836,
//				48.0383561643836, 49.0383561643836, 50.041095890411, 51.041095890411, 52.041095890411, 53.041095890411,
//				54.0438356164384, 55.0438356164384, 56.0438356164384, 57.0438356164384, 58.0465753424658,
//				59.0465753424658, 60.0465753424658 };
		
		return null;
	}

	/**
	 * Returns the OIS6M discount curve dates for a given data Day provided as
	 * String.
	 * 
	 * @param day
	 * @return
	 */
	private double[] getDiscountValuesFromDataDate(String day) {
		// TODO Auto-generated method stub

		// Example
		//return new double[] { 1.00001, 1.00137, 1.00161, 1.00177, 1.00196, 1.00256, 1.00294, 1.00317, 1.00318, 1.00291,
		//		1.00243, 1.00174, 1.00082, 0.99969, 0.99835, 0.9968, 0.99503, 0.99308, 0.99096, 0.9887, 0.98623,
//				0.98362, 0.9809, 0.97804, 0.97502, 0.97187, 0.96862, 0.96528, 0.96175, 0.95811, 0.95442, 0.95068,
//				0.94676, 0.94265, 0.93859, 0.93449, 0.93022, 0.92595, 0.92162, 0.91726, 0.91281, 0.90838, 0.90391,
//				0.89948, 0.89492, 0.89034, 0.88581, 0.88131, 0.87671, 0.87211, 0.86756, 0.86307, 0.85848, 0.85391,
//				0.8494, 0.84491, 0.84039, 0.83588, 0.83145, 0.82709, 0.82266, 0.81826, 0.80112, 0.78454, 0.76855,
//				0.75329, 0.73871, 0.72485, 0.71164, 0.69909, 0.68711, 0.67556, 0.66439, 0.6536, 0.64313, 0.63295,
//				0.62301, 0.61332, 0.60385, 0.5946, 0.58555, 0.57673, 0.56813, 0.55974, 0.55153, 0.54356, 0.53583,
//				0.52826, 0.52085, 0.51363, 0.50658, 0.49967, 0.49287, 0.4862, 0.47963, 0.47315, 0.46671, 0.46033,
//				0.45401, 0.44775, 0.44152, 0.43538, 0.4293, 0.4233, 0.41736, 0.41152, 0.40576 };
		
		return null;
				
	}

}
