package kellerstrass.temp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.junit.Assert;

import kellerstrass.Calibration.CurveModelCalibrationItem;
import kellerstrass.ModelCalibration.CalibrationMachineInterface;
import kellerstrass.ModelCalibration.LmmCalibrationMachine;
import kellerstrass.exposure.ExposureMachine;
import kellerstrass.marketInformation.CalibrationInformation;
import kellerstrass.marketInformation.DataScope;
import kellerstrass.marketInformation.DataSource;
import kellerstrass.xva.CVA;
import net.finmath.exception.CalculationException;
import net.finmath.marketdata.model.AnalyticModel;
import net.finmath.marketdata.model.curves.DiscountCurve;
import net.finmath.marketdata.model.curves.DiscountCurveFromForwardCurve;
import net.finmath.marketdata.model.curves.ForwardCurve;
import net.finmath.montecarlo.BrownianMotion;
import net.finmath.montecarlo.RandomVariableFromDoubleArray;
import net.finmath.montecarlo.interestrate.CalibrationProduct;
import net.finmath.montecarlo.interestrate.LIBORModel;
import net.finmath.montecarlo.interestrate.LIBORModelMonteCarloSimulationModel;
import net.finmath.montecarlo.interestrate.LIBORMonteCarloSimulationFromLIBORModel;
import net.finmath.montecarlo.interestrate.models.LIBORMarketModelFromCovarianceModel;
import net.finmath.montecarlo.interestrate.models.covariance.AbstractLIBORCovarianceModelParametric;
import net.finmath.montecarlo.interestrate.models.covariance.DisplacedLocalVolatilityModel;
import net.finmath.montecarlo.interestrate.models.covariance.LIBORCorrelationModel;
import net.finmath.montecarlo.interestrate.models.covariance.LIBORCorrelationModelExponentialDecay;
import net.finmath.montecarlo.interestrate.models.covariance.LIBORCovarianceModelFromVolatilityAndCorrelation;
import net.finmath.montecarlo.interestrate.models.covariance.LIBORVolatilityModel;
import net.finmath.montecarlo.interestrate.models.covariance.LIBORVolatilityModelPiecewiseConstant;
import net.finmath.montecarlo.interestrate.products.AbstractLIBORMonteCarloProduct;
import net.finmath.montecarlo.interestrate.products.Swap;
import net.finmath.montecarlo.interestrate.products.SwapLeg;
import net.finmath.montecarlo.interestrate.products.TermStructureMonteCarloProduct;
import net.finmath.montecarlo.interestrate.products.components.AbstractNotional;
import net.finmath.montecarlo.interestrate.products.components.Notional;
import net.finmath.montecarlo.interestrate.products.indices.AbstractIndex;
import net.finmath.montecarlo.interestrate.products.indices.LIBORIndex;
import net.finmath.montecarlo.process.EulerSchemeFromProcessModel;
import net.finmath.optimizer.OptimizerFactory;
import net.finmath.optimizer.OptimizerFactoryLevenbergMarquardt;
import net.finmath.optimizer.SolverException;
import net.finmath.stochastic.RandomVariable;
import net.finmath.time.Schedule;
import net.finmath.time.ScheduleGenerator;
import net.finmath.time.TimeDiscretization;
import net.finmath.time.TimeDiscretizationFromArray;
import net.finmath.time.businessdaycalendar.BusinessdayCalendar;
import net.finmath.time.businessdaycalendar.BusinessdayCalendarExcludingTARGETHolidays;
import net.finmath.time.daycount.DayCountConvention_ACT_365;

public class LMMCalibrationCVA5RebuildAsCalibrationmaschine {

	private static DecimalFormat formatterValue = new DecimalFormat(" ##0.000%;-##0.000%",
			new DecimalFormatSymbols(Locale.ENGLISH));
	private static DecimalFormat formatterParam = new DecimalFormat(" #0.0000;-#0.0000",
			new DecimalFormatSymbols(Locale.ENGLISH));
	private static DecimalFormat formatterDeviation = new DecimalFormat(" 0.00000E00;-0.00000E00",
			new DecimalFormatSymbols(Locale.ENGLISH));
	private final static NumberFormat formatter6 = new DecimalFormat("0.000000",
			new DecimalFormatSymbols(new Locale("en")));

	public static void main(String[] args) throws Exception {

		// Possibility 1 (works)
		// LIBORModelMonteCarloSimulationModel simulationModel =
		// doATMSwaptionCalibration();

		// Possibility 1 (works???)
		boolean forcedCalculation = false;
		int numberOfPaths = 1000;
		int numberOfFactors = 3;
		double lastTime = 40.0;
		double dt = 0.25;
		CalibrationMachineInterface lmmCalibrationMaschine = new LmmCalibrationMachine(numberOfPaths, numberOfFactors,
				new CalibrationInformation(DataScope.CoTerminals, DataSource.EXAMPLE));
		TimeDiscretizationFromArray timeDiscretizationFromArray = new TimeDiscretizationFromArray(0.0,
				(int) (lastTime / dt), dt);
		BrownianMotion brownianMotion = new net.finmath.montecarlo.BrownianMotionLazyInit(timeDiscretizationFromArray,
				numberOfFactors, numberOfPaths, 31415 /* seed */);
		EulerSchemeFromProcessModel process = new EulerSchemeFromProcessModel(brownianMotion,
				EulerSchemeFromProcessModel.Scheme.EULER);
		LIBORModelMonteCarloSimulationModel simulationModel = lmmCalibrationMaschine
				.getLIBORModelMonteCarloSimulationModel(process, forcedCalculation);

		AbstractLIBORMonteCarloProduct swap = getSwap();

		TermStructureMonteCarloProduct swapExposureEstimator = new ExposureMachine(swap);

		System.out.println("observationDate  \t   expected positive Exposure  \t   expected negative Exposure");
		for (double observationDate : simulationModel.getTimeDiscretization()) {

			if (observationDate == 0) {
				continue;
			}

			/*
			 * Calculate expected positive exposure of a swap
			 */
			RandomVariable valuesSwap = swap.getValue(observationDate, simulationModel);
			RandomVariable valuesEstimatedExposure = swapExposureEstimator.getValue(observationDate, simulationModel);
			RandomVariable valuesPositiveExposure = valuesSwap.mult(valuesEstimatedExposure
					.choose(new RandomVariableFromDoubleArray(1.0), new RandomVariableFromDoubleArray(0.0)));
			RandomVariable valuesNegativeExposure = valuesSwap.mult(valuesEstimatedExposure
					.choose(new RandomVariableFromDoubleArray(0.0), new RandomVariableFromDoubleArray(1.0)));

			double expectedPositiveExposure = valuesPositiveExposure.getAverage();
			double expectedNegativeExposure = valuesNegativeExposure.getAverage();

			System.out.println(observationDate + "    \t         " + formatter6.format(expectedPositiveExposure)
					+ "    \t         " + formatter6.format(expectedNegativeExposure));

		}
		System.out.println("The CVA is:");

		double Recovery = 0.40; // Recovery Rate

		double[] cdsSpreads = { 300.0, 350.0, 400.0, 450.0, 500.0, 550.0, 600.0, 650.0, 700.0, 750.0 }; // {20.0, 25.0,
																										// 30.0, 35.0,
																										// 40.0} // The
																										// (yearly) CDS
																										// spreads in bp
																										// {320.0, 57.0,
																										// 132.0 , 139.0
																										// , 146.0,
																										// 150.0, 154.0}
																										// || {300.0,
																										// 350.0, 400.0,
																										// 450.0, 500.0
																										// }

		// Now we test our CVA calculation
		final AnalyticModel curveModel = CurveModelCalibrationItem.getCalibratedCurve();
		CVA cvaGetter = new CVA(simulationModel, swap, Recovery, cdsSpreads,
				curveModel.getDiscountCurve("discountCurve-EUR"));

		double cva = cvaGetter.getValue();

		System.out.println(cva);

	}

	private static LIBORModelMonteCarloSimulationModel doATMSwaptionCalibration()
			throws SolverException, CalculationException {
		final int numberOfPaths = 100;
		final int numberOfFactors = 3;

		/*
		 * Calibration test
		 */
		System.out.println("Calibration to Swaptions.\n");

		/*
		 * Calibration of rate curves
		 */
		System.out.println(
				"Calibration of rate curves:     (calibration of the discount an forward rate via ATM swatps)");

		/*
		 * test of the Calibration maschine
		 */
		CalibrationInformation calibrationInformation = new CalibrationInformation(DataScope.CoTerminals,
				DataSource.EXAMPLE);
		LmmCalibrationMachine lmmCalibrationMaschine = new LmmCalibrationMachine(numberOfPaths, numberOfFactors,
				calibrationInformation);

		/*
		 * Create a simulation time discretization
		 */
		// If simulation time is below libor time, exceptions will be hard to track.
		double lastTime = 40.0;
		double dt = 0.25;
		TimeDiscretizationFromArray timeDiscretizationFromArray = new TimeDiscretizationFromArray(0.0,
				(int) (lastTime / dt), dt);

		/*
		 * Create Brownian motion
		 */
		BrownianMotion brownianMotion = new net.finmath.montecarlo.BrownianMotionLazyInit(timeDiscretizationFromArray,
				numberOfFactors, numberOfPaths, 31415 /* seed */);

		LIBORModel liborMarketModelCalibrated = getCalibratedModel(lmmCalibrationMaschine, numberOfFactors,
				numberOfPaths, calibrationInformation);

		/*
		 * Test our calibration
		 */
		System.out.println("\nCalibrated parameters are:");
		double[] param = ((AbstractLIBORCovarianceModelParametric) ((LIBORMarketModelFromCovarianceModel) liborMarketModelCalibrated)
				.getCovarianceModel()).getParameterAsDouble();
		// ((AbstractLIBORCovarianceModelParametric)
		// liborMarketModelCalibrated.getCovarianceModel()).setParameter(param);
		for (double p : param) {
			System.out.println(formatterParam.format(p));
		}

		// The Process
		EulerSchemeFromProcessModel process = new EulerSchemeFromProcessModel(brownianMotion);

		// The simulationModel
		LIBORModelMonteCarloSimulationModel LMMSimulation = new LIBORMonteCarloSimulationFromLIBORModel(
				liborMarketModelCalibrated, process);

		AnalyticModel curveModelNEW = null;
		try {
			curveModelNEW = lmmCalibrationMaschine.getCurveModelCalibrationMaschine().getCalibratedCurveModel();
		} catch (SolverException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Create the forward curve (initial value of the LIBOR market model)
		final ForwardCurve forwardCurve = curveModelNEW
				.getForwardCurve("ForwardCurveFromDiscountCurve(discountCurve-EUR,6M)");
		final DiscountCurve discountCurve = curveModelNEW.getDiscountCurve("discountCurve-EUR");

		CalibrationProduct[] calibrationProducts = lmmCalibrationMaschine.getCalibrationProducts();

		System.out.println("\nValuation on calibrated model:");
		double deviationSum = 0.0;
		double deviationSquaredSum = 0.0;
		for (int i = 0; i < calibrationProducts.length; i++) {
			AbstractLIBORMonteCarloProduct calibrationProduct = calibrationProducts[i].getProduct();
			try {
				double valueModel = calibrationProduct.getValue(LMMSimulation);
				double valueTarget = calibrationProducts[i].getTargetValue().getAverage();
				double error = valueModel - valueTarget;
				deviationSum += error;
				deviationSquaredSum += error * error;
				System.out.println(lmmCalibrationMaschine.getCalibrationItemNames(calibrationInformation)[i] + "\t"
						+ "Model: \t" + formatterValue.format(valueModel) + "\t Target: \t"
						+ formatterValue.format(valueTarget) + "\t Deviation: \t"
						+ formatterDeviation.format(valueModel - valueTarget));// + "\t" +
																				// calibrationProduct.toString());
			} catch (Exception e) {
			}
		}

		double averageDeviation = deviationSum / calibrationProducts.length;
		System.out.println("Mean Deviation:" + formatterDeviation.format(averageDeviation));
		System.out.println("RMS Error.....:"
				+ formatterDeviation.format(Math.sqrt(deviationSquaredSum / calibrationProducts.length)));
		System.out.println(
				"__________________________________________________________________________________________\n");

		return LMMSimulation;

	}

	/**
	 * Get a swap, with some example input parameters
	 * 
	 * @return
	 */
	public static AbstractLIBORMonteCarloProduct getSwap() {

		/*
		 * Create a receiver swap (receive fix, pay float)
		 */
		Schedule legScheduleRec = ScheduleGenerator.createScheduleFromConventions(
				LocalDate.of(2020, Month.JANUARY, 03) /* referenceDate */,
				LocalDate.of(2020, Month.JANUARY, 06) /* startDate */,
				LocalDate.of(2030, Month.JANUARY, 06) /* maturityDate */,
				ScheduleGenerator.Frequency.ANNUAL /* frequency */,
				ScheduleGenerator.DaycountConvention.ACT_365 /* daycountConvention */,
				ScheduleGenerator.ShortPeriodConvention.FIRST /* shortPeriodConvention */,
				BusinessdayCalendar.DateRollConvention.FOLLOWING /* dateRollConvention */,
				new BusinessdayCalendarExcludingTARGETHolidays() /* businessdayCalendar */, 0 /* fixingOffsetDays */,
				0 /* paymentOffsetDays */);

		Schedule legSchedulePay = ScheduleGenerator.createScheduleFromConventions(
				LocalDate.of(2020, Month.JANUARY, 03) /* referenceDate */,
				LocalDate.of(2020, Month.JANUARY, 06) /* startDate */,
				LocalDate.of(2030, Month.JANUARY, 06) /* maturityDate */,
				ScheduleGenerator.Frequency.QUARTERLY /* frequency */,
				ScheduleGenerator.DaycountConvention.ACT_365 /* daycountConvention */,
				ScheduleGenerator.ShortPeriodConvention.FIRST /* shortPeriodConvention */,
				BusinessdayCalendar.DateRollConvention.FOLLOWING /* dateRollConvention */,
				new BusinessdayCalendarExcludingTARGETHolidays() /* businessdayCalendar */, 0 /* fixingOffsetDays */,
				0 /* paymentOffsetDays */);
		AbstractNotional notional = new Notional(1.0);
		AbstractIndex index = new LIBORIndex(null /* "forwardCurve" */, 0.0, 0.25);
		double fixedCoupon = 0.0025;

		SwapLeg swapLegRec = new SwapLeg(legScheduleRec, notional, null, fixedCoupon /* spread */,
				false /* isNotionalExchanged */);
		SwapLeg swapLegPay = new SwapLeg(legSchedulePay, notional, index, 0.0 /* spread */,
				false /* isNotionalExchanged */);
		return new Swap(swapLegRec, swapLegPay);
	}

	public static LIBORModel getCalibratedModel(LmmCalibrationMachine lmmCalibrationMaschine, int numberOfFactors,
			int numberOfPaths, CalibrationInformation calibrationInformation) {

		AnalyticModel curveModelNEW = null;
		try {
			curveModelNEW = lmmCalibrationMaschine.getCurveModelCalibrationMaschine().getCalibratedCurveModel();
		} catch (SolverException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Create the forward curve (initial value of the LIBOR market model)
		final ForwardCurve forwardCurve = curveModelNEW
				.getForwardCurve("ForwardCurveFromDiscountCurve(discountCurve-EUR,6M)");
		final DiscountCurve discountCurve = curveModelNEW.getDiscountCurve("discountCurve-EUR");

		// curveModel.addCurve(discountCurve.getName(), discountCurve);
		// curveModel.addCurve(forwardCurve.getName(), forwardCurve);

//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++		
		/*
		 * Calibration of model volatilities (via ATM swap vols normal)
		 */
		System.out.println("Brute force Monte-Carlo calibration of model volatilities:");

		double calibrationStart = System.currentTimeMillis();

		/*
		 * Create a simulation time discretization
		 */
		// If simulation time is below libor time, exceptions will be hard to track.
		double lastTime = 40.0;
		double dt = 0.25;
		TimeDiscretizationFromArray timeDiscretizationFromArray = new TimeDiscretizationFromArray(0.0,
				(int) (lastTime / dt), dt);
		final TimeDiscretization liborPeriodDiscretization = timeDiscretizationFromArray;

		/*
		 * Create Brownian motion
		 */
		BrownianMotion brownianMotion = new net.finmath.montecarlo.BrownianMotionLazyInit(timeDiscretizationFromArray,
				numberOfFactors, numberOfPaths, 31415 /* seed */);

		// Create a volatility Model
		LIBORVolatilityModel volatilityModel = new LIBORVolatilityModelPiecewiseConstant(timeDiscretizationFromArray,
				liborPeriodDiscretization, new TimeDiscretizationFromArray(0, 1, 2, 3, 5, 7, 10, 15),
				new TimeDiscretizationFromArray(0, 1, 2, 3, 5, 7, 10, 15), 0.50 / 100);

		// Create correlationModel
		LIBORCorrelationModel correlationModel = new LIBORCorrelationModelExponentialDecay(timeDiscretizationFromArray,
				liborPeriodDiscretization, numberOfFactors, 0.05, false);

		// Create a covariance model
		AbstractLIBORCovarianceModelParametric covarianceModelParametric = new LIBORCovarianceModelFromVolatilityAndCorrelation(
				timeDiscretizationFromArray, liborPeriodDiscretization, volatilityModel, correlationModel);

		// Create blended local volatility model with fixed parameter (0=lognormal, > 1
		// = almost a normal model).
		AbstractLIBORCovarianceModelParametric covarianceModelDisplaced = new DisplacedLocalVolatilityModel(
				covarianceModelParametric, 1.0 / 0.25, false /* isCalibrateable */);

		// Set model properties
		Map<String, Object> properties = new HashMap<>();

		// Choose the simulation measure
		properties.put("measure", LIBORMarketModelFromCovarianceModel.Measure.SPOT.name());

		// Choose normal state space for the Euler scheme (the covariance model above
		// carries a linear local volatility model, such that the resulting model is
		// log-normal).
		properties.put("stateSpace", LIBORMarketModelFromCovarianceModel.StateSpace.NORMAL.name());

		// works also without
		// Set calibration properties (should use our brownianMotion for calibration -
		// needed to have to right correlation).
		Double accuracy = new Double(1E-6); // Lower accuracy to reduce runtime of the unit test
		int maxIterations = 100;
		int numberOfThreads = 4;
		OptimizerFactory optimizerFactory = new OptimizerFactoryLevenbergMarquardt(maxIterations, accuracy,
				numberOfThreads);

		double[] parameterStandardDeviation = new double[covarianceModelParametric.getParameterAsDouble().length];
		double[] parameterLowerBound = new double[covarianceModelParametric.getParameterAsDouble().length];
		double[] parameterUpperBound = new double[covarianceModelParametric.getParameterAsDouble().length];
		Arrays.fill(parameterStandardDeviation, 0.20 / 100.0);
		Arrays.fill(parameterLowerBound, 0.0);
		Arrays.fill(parameterUpperBound, Double.POSITIVE_INFINITY);

		// Set calibration properties (should use our brownianMotion for calibration -
		// needed to have to right correlation).
		Map<String, Object> calibrationParameters = new HashMap<>();
		calibrationParameters.put("accuracy", accuracy);
		calibrationParameters.put("brownianMotion", brownianMotion);
		calibrationParameters.put("optimizerFactory", optimizerFactory);
		calibrationParameters.put("parameterStep", new Double(1E-4));
		properties.put("calibrationParameters", calibrationParameters);

		/*
		 * We want to check if the calibration Products from the calibration maschine
		 * are correct
		 */
		CalibrationProduct[] calibrationProducts = lmmCalibrationMaschine.getCalibrationProducts();

		System.out.println("liborPeriodDiscretization = " + liborPeriodDiscretization);
		System.out.println("curveModel = " + curveModelNEW);
		System.out.println("forwardCurve = " + forwardCurve);
		System.out.println("discountCurve = " + discountCurve);
		System.out.println("calibrationItems = " + calibrationProducts);
		System.out.println("properties = " + properties);

		LIBORModel liborMarketModelCalibrated = null;
		try {
			liborMarketModelCalibrated = new LIBORMarketModelFromCovarianceModel(liborPeriodDiscretization,
					curveModelNEW, forwardCurve, new DiscountCurveFromForwardCurve(forwardCurve),
					covarianceModelDisplaced, calibrationProducts /* calibrationItems */, properties);
		} catch (CalculationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// The running time output
		double calibrationEnd = System.currentTimeMillis();
		System.out.println("The calculation took: " + (calibrationEnd - calibrationStart) / 60000 + " min");

		return liborMarketModelCalibrated;

	}

}