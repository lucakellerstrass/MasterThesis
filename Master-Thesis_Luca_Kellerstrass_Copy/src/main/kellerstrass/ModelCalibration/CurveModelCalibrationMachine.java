package kellerstrass.ModelCalibration;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import kellerstrass.Calibration.CurveModelCalibrationItem;
import kellerstrass.marketInformation.CurveModelData;
import kellerstrass.marketInformation.CurveModelDataType;
import net.finmath.marketdata.calibration.ParameterObject;
import net.finmath.marketdata.calibration.Solver;
import net.finmath.marketdata.model.AnalyticModel;
import net.finmath.marketdata.model.AnalyticModelFromCurvesAndVols;
import net.finmath.marketdata.model.curves.Curve;
import net.finmath.marketdata.model.curves.DiscountCurveInterpolation;
import net.finmath.marketdata.model.curves.ForwardCurve;
import net.finmath.marketdata.model.curves.ForwardCurveFromDiscountCurve;
import net.finmath.marketdata.model.curves.ForwardCurveInterpolation;
import net.finmath.marketdata.model.curves.CurveInterpolation.ExtrapolationMethod;
import net.finmath.marketdata.model.curves.CurveInterpolation.InterpolationEntity;
import net.finmath.marketdata.model.curves.CurveInterpolation.InterpolationMethod;
import net.finmath.marketdata.model.curves.DiscountCurve;
import net.finmath.marketdata.products.AnalyticProduct;
import net.finmath.marketdata.products.Swap;
import net.finmath.optimizer.SolverException;
import net.finmath.time.Schedule;
import net.finmath.time.ScheduleGenerator;
import net.finmath.time.businessdaycalendar.BusinessdayCalendarExcludingTARGETHolidays;

/**
 * In the case we have a curveModelDataType or curveModelData this calibrates
 * the curve model. The result is a single curve model, since the forward curve
 * is build from the discount curve.
 * 
 * @author lucak
 *
 */
public class CurveModelCalibrationMachine {

	private static String ForwardCurveName;
	private static CurveModelData curveModelData;

	/**
	 * Initiate an instance of the calibrated Curve Model by using an existing
	 * instance of CurveModelData
	 * 
	 * @param CurveModelDataType The data Type or source of the information
	 * @return
	 * @throws SolverException
	 */
	public CurveModelCalibrationMachine(CurveModelData curveModelData) {
		CurveModelCalibrationMachine.curveModelData = curveModelData;

	}

	/**
	 * Initiate an instance of the calibrated Curve Model
	 * 
	 * @param CurveModelDataType The data Type or source of the information
	 * @return
	 * @throws SolverException
	 */
	public CurveModelCalibrationMachine(CurveModelDataType curveModelDataType) {
		CurveModelCalibrationMachine.curveModelData = new CurveModelData(curveModelDataType);

	}

	/**
	 * Initiate an instance of the calibrated curve Model by using the information:
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
	public CurveModelCalibrationMachine(String[] maturity, String[] frequency, String[] frequencyFloat,
			String[] daycountConventions, String[] daycountConventionsFloat, double[] rates, LocalDate localDate,
			String forwardCurveTenor, int spotOffsetDays, String forwardStartPeriod) {

		CurveModelCalibrationMachine.curveModelData = new CurveModelData(maturity, frequency, frequencyFloat, daycountConventions,
				daycountConventionsFloat, rates, localDate, forwardCurveTenor, spotOffsetDays, forwardStartPeriod);
	}

	public String getCurveModelName() {
		return curveModelData.getCurveModelDataType() + "CurveModel";

	}
	
	/**
	 * get the name of the forward curve to reference it inside the CurveModel
	 * @return
	 */
	public String getForwardCurvelName() {
		return CurveModelCalibrationMachine.ForwardCurveName;

	}


	public AnalyticModel getCalibratedCurveModel() throws SolverException {

		if (curveModelData.isInitiationOverExistingDiscountValues() == true) {

			DiscountCurve discountCurve = curveModelData.getDiscountCurve();

			String curveNameDiscount = discountCurve.getName();
			LocalDate referenceDate = discountCurve.getReferenceDate();

			ForwardCurve forwardCurve = new ForwardCurveFromDiscountCurve(curveNameDiscount, referenceDate, "6M");
			CurveModelCalibrationMachine.ForwardCurveName = forwardCurve.getName();
			AnalyticModel model = new AnalyticModelFromCurvesAndVols(new Curve[] { discountCurve, forwardCurve });

			return model;
		}else {

		final String[] maturity = curveModelData.getMaturity();
		final String[] frequency = curveModelData.getFrequency();
		final String[] frequencyFloat = curveModelData.getFrequencyFloat();
		final String[] daycountConventions = curveModelData.getDaycountConventions();
		final String[] daycountConventionsFloat = curveModelData.getDaycountConventionsFloat();
		final double[] rates = curveModelData.getRates();
		// swaprates

		HashMap<String, Object> parameters = new HashMap<>();

		parameters.put("referenceDate", curveModelData.getLocalDate()); System.out.println("reference date was setted as: "+ curveModelData.getLocalDate());
		parameters.put("currency", "EUR");
		parameters.put("forwardCurveTenor", curveModelData.getForwardCurveTenor());
		parameters.put("maturities", maturity);
		parameters.put("fixLegFrequencies", frequency);
		parameters.put("floatLegFrequencies", frequencyFloat);
		parameters.put("fixLegDaycountConventions", daycountConventions);
		parameters.put("floatLegDaycountConventions", daycountConventionsFloat);
		parameters.put("rates", rates);

		return getCalibratedCurve(null, parameters);
		}
	}

	private static AnalyticModel getCalibratedCurve(AnalyticModel model2, Map<String, Object> parameters)
			throws SolverException {

		final LocalDate referenceDate = (LocalDate) parameters.get("referenceDate");
		final String currency = (String) parameters.get("currency");
		final String forwardCurveTenor = (String) parameters.get("forwardCurveTenor");
		final String[] maturities = (String[]) parameters.get("maturities");
		final String[] frequency = (String[]) parameters.get("fixLegFrequencies");
		final String[] frequencyFloat = (String[]) parameters.get("floatLegFrequencies");
		final String[] daycountConventions = (String[]) parameters.get("fixLegDaycountConventions");
		final String[] daycountConventionsFloat = (String[]) parameters.get("floatLegDaycountConventions");
		final double[] rates = (double[]) parameters.get("rates");

		int spotOffsetDays = curveModelData.getSpotOffsetDays();
		String forwardStartPeriod = curveModelData.getforwardStartPeriod();

		String curveNameDiscount = "discountCurve-" + currency;

		/*
		 * We create a forward curve by referencing the same discount curve, since this
		 * is a single curve setup.
		 *
		 * Note that using an independent NSS forward curve with its own NSS parameters
		 * would result in a problem where both, the forward curve and the discount
		 * curve have free parameters.
		 */
		ForwardCurve forwardCurve = new ForwardCurveFromDiscountCurve(curveNameDiscount, referenceDate,
				forwardCurveTenor);
		ForwardCurveName = forwardCurve.getName();

		// Create a collection of objective functions (calibration products)
		Vector<AnalyticProduct> calibrationProducts = new Vector<>();
		double[] curveMaturities = new double[rates.length + 1];
		double[] curveValue = new double[rates.length + 1];
		boolean[] curveIsParameter = new boolean[rates.length + 1];
		curveMaturities[0] = 0.0;
		curveValue[0] = 1.0;
		curveIsParameter[0] = false;
		for (int i = 0; i < rates.length; i++) {

			Schedule schedulePay = ScheduleGenerator.createScheduleFromConventions(referenceDate, spotOffsetDays,
					forwardStartPeriod, maturities[i], frequency[i], daycountConventions[i], "first", "following",
					new BusinessdayCalendarExcludingTARGETHolidays(), -2, 0);
			Schedule scheduleRec = ScheduleGenerator.createScheduleFromConventions(referenceDate, spotOffsetDays,
					forwardStartPeriod, maturities[i], frequencyFloat[i], daycountConventionsFloat[i], "first",
					"following", new BusinessdayCalendarExcludingTARGETHolidays(), -2, 0);

			curveMaturities[i + 1] = Math.max(schedulePay.getPayment(schedulePay.getNumberOfPeriods() - 1),
					scheduleRec.getPayment(scheduleRec.getNumberOfPeriods() - 1));
			curveValue[i + 1] = 1.0;
			curveIsParameter[i + 1] = true;
			calibrationProducts.add(new Swap(schedulePay, null, rates[i], curveNameDiscount, scheduleRec,
					forwardCurve.getName(), 0.0, curveNameDiscount));
		}

		InterpolationMethod interpolationMethod = InterpolationMethod.LINEAR;

		// Create a discount curve
		DiscountCurveInterpolation discountCurveInterpolation = DiscountCurveInterpolation
				.createDiscountCurveFromDiscountFactors(curveNameDiscount /* name */, referenceDate,
						curveMaturities /* maturities */, curveValue /* discount factors */, curveIsParameter,
						interpolationMethod, ExtrapolationMethod.CONSTANT, InterpolationEntity.LOG_OF_VALUE);

		/*
		 * Model consists of the two curves, but only one of them provides free
		 * parameters.
		 */
		AnalyticModel model = new AnalyticModelFromCurvesAndVols(
				new Curve[] { discountCurveInterpolation, forwardCurve });

		/*
		 * Create a collection of curves to calibrate
		 */
		Set<ParameterObject> curvesToCalibrate = new HashSet<>();
		curvesToCalibrate.add(discountCurveInterpolation);

		/*
		 * Calibrate the curve
		 */
		Solver solver = new Solver(model, calibrationProducts, 0.0, 1E-4 /* target accuracy */);
		AnalyticModel calibratedModel = solver.getCalibratedModel(curvesToCalibrate);
		// System.out.println("Solver reported acccurary....: " + solver.getAccuracy());

		// Get best parameters
		//double[] parametersBest = calibratedModel.getDiscountCurve(discountCurveInterpolation.getName()).getParameter();

		model			= calibratedModel;
		
		/*
		 * We want to create a real forward curve that can be used without refering to
		 * the curveModel
		 */
		// The fixing time points:
//		double[] forwardFixings = new double[51];
//		for (int i = 0; i < forwardFixings.length; i++) {
//			forwardFixings[i] = i;
//		}
//		// The forward Curve values an an array
//		double[] forwardCurveValues = new double[forwardFixings.length];
//		for (int i = 0; i < forwardCurveValues.length; i++) {
//			forwardCurveValues[i] = forwardCurve.getForward(calibratedModel, i);
//		}
//
//		// Create the forward curve (initial value of the LIBOR market model)
//		ForwardCurve forwardCurveInterpolation = ForwardCurveInterpolation.createForwardCurveFromForwards(
//				"forwardCurve", // "ForwardCurveFromDiscountCurve(discountCurve-EUR,6M)" /* name of the curve
//								// */,
//				forwardFixings /* fixings of the forward */, forwardCurveValues /* forwards */,
//				0.5 /* liborPeriodLength */ /* tenor / period length */
//		);


		return model;
	}

}
