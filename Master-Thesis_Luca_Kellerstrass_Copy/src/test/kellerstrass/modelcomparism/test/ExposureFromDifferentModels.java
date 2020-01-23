package kellerstrass.modelcomparism.test;

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

import kellerstrass.Calibration.CalibrationItem;
import kellerstrass.exposure.ExposureEstimator;
import kellerstrass.interestrate.models.StoredHullWhite;
import net.finmath.exception.CalculationException;
import net.finmath.marketdata.model.AnalyticModel;
import net.finmath.marketdata.model.AnalyticModelFromCurvesAndVols;
import net.finmath.marketdata.model.curves.Curve;
import net.finmath.marketdata.model.curves.DiscountCurve;
import net.finmath.marketdata.model.curves.DiscountCurveFromForwardCurve;
import net.finmath.marketdata.model.curves.ForwardCurve;
import net.finmath.montecarlo.BrownianMotion;
import net.finmath.montecarlo.BrownianMotionLazyInit;
import net.finmath.montecarlo.RandomVariableFactory;
import net.finmath.montecarlo.RandomVariableFromDoubleArray;
import net.finmath.montecarlo.interestrate.CalibrationProduct;
import net.finmath.montecarlo.interestrate.LIBORMarketModel;
import net.finmath.montecarlo.interestrate.LIBORModelMonteCarloSimulationModel;
import net.finmath.montecarlo.interestrate.LIBORMonteCarloSimulationFromLIBORModel;
import net.finmath.montecarlo.interestrate.models.HullWhiteModel;
import net.finmath.montecarlo.interestrate.models.LIBORMarketModelFromCovarianceModel;
import net.finmath.montecarlo.interestrate.models.covariance.AbstractLIBORCovarianceModelParametric;
import net.finmath.montecarlo.interestrate.models.covariance.AbstractShortRateVolatilityModel;
import net.finmath.montecarlo.interestrate.models.covariance.DisplacedLocalVolatilityModel;
import net.finmath.montecarlo.interestrate.models.covariance.LIBORCorrelationModel;
import net.finmath.montecarlo.interestrate.models.covariance.LIBORCorrelationModelExponentialDecay;
import net.finmath.montecarlo.interestrate.models.covariance.LIBORCovarianceModel;
import net.finmath.montecarlo.interestrate.models.covariance.LIBORCovarianceModelFromVolatilityAndCorrelation;
import net.finmath.montecarlo.interestrate.models.covariance.LIBORVolatilityModel;
import net.finmath.montecarlo.interestrate.models.covariance.LIBORVolatilityModelPiecewiseConstant;
import net.finmath.montecarlo.interestrate.models.covariance.ShortRateVolatilityModelPiecewiseConstant;
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

public class ExposureFromDifferentModels {
	
	private final static NumberFormat formatter6 = new DecimalFormat("0.000000", new DecimalFormatSymbols(new Locale("en")));
	private static DecimalFormat formatterValue		= new DecimalFormat(" ##0.000%;-##0.000%", new DecimalFormatSymbols(Locale.ENGLISH));
	private static DecimalFormat formatterParam		= new DecimalFormat(" #0.000;-#0.000", new DecimalFormatSymbols(Locale.ENGLISH));
	private static DecimalFormat formatterDeviation	= new DecimalFormat(" 0.00000E00;-0.00000E00", new DecimalFormatSymbols(Locale.ENGLISH));

	private static double liborPeriodLength;
	
	
	public static void main(String[] args) throws SolverException, CalculationException {
		//getModel
				LIBORModelMonteCarloSimulationModel simulationModel = getModel("LMM");
				
				
				// Check the libor Period Lenght
				liborPeriodLength = simulationModel.getLiborPeriodDiscretization().getTimeStep(1);
				System.out.println("liborPeriodLength = "  +liborPeriodLength);
				
				
				//Create the swap
				AbstractLIBORMonteCarloProduct swap = getSwap();
				TermStructureMonteCarloProduct swapExposureEstimator = new ExposureEstimator(swap);
				
				//Print the exposures
				System.out.println("Expected Exposure ");
	

	System.out.println("observationDate  \t   expected positive Exposure  \t   expected negative Exposure");
	for(double observationDate : simulationModel.getTimeDiscretization()) {

		if(observationDate == 0) {
			continue;
		}

		/*
		 * Calculate expected positive exposure of a swap
		 */
		RandomVariable valuesSwap = swap.getValue(observationDate, simulationModel);
		RandomVariable valuesEstimatedExposure = swapExposureEstimator.getValue(observationDate, simulationModel);
		RandomVariable valuesPositiveExposure = valuesSwap.mult(valuesEstimatedExposure.choose(new RandomVariableFromDoubleArray(1.0), new RandomVariableFromDoubleArray(0.0)));
		RandomVariable valuesNegativeExposure = valuesSwap.mult(valuesEstimatedExposure.choose(new RandomVariableFromDoubleArray(0.0), new RandomVariableFromDoubleArray(1.0)));
		
		double expectedPositiveExposure		= valuesPositiveExposure.getAverage();
		double expectedNegativeExposure		= valuesNegativeExposure.getAverage();

		System.out.println(observationDate + "    \t         " +  formatter6.format(expectedPositiveExposure) + "    \t         " +  formatter6.format(expectedNegativeExposure) );

		double basisPoint = 1E-4;
		Assert.assertTrue("Expected positive exposure", expectedPositiveExposure >= 0-1*basisPoint);
	}
	
	

	
	
	
	}
	
	
	
	
	
	 /**
     * Get a swap, with some example input parameters
     * @return
     */
	private static AbstractLIBORMonteCarloProduct getSwap() {
	
			/*
			 * Create a receiver swap (receive fix, pay float)
			 */
			Schedule legScheduleRec = ScheduleGenerator.createScheduleFromConventions(
					LocalDate.of(2015, Month.JANUARY, 03) /* referenceDate */,
					LocalDate.of(2015, Month.JANUARY, 06) /* startDate */,
					LocalDate.of(2025, Month.JANUARY, 06) /* maturityDate */,
					ScheduleGenerator.Frequency.ANNUAL /* frequency */,
					ScheduleGenerator.DaycountConvention.ACT_365 /* daycountConvention */,
					ScheduleGenerator.ShortPeriodConvention.FIRST /* shortPeriodConvention */,
					BusinessdayCalendar.DateRollConvention.FOLLOWING /* dateRollConvention */,
					new BusinessdayCalendarExcludingTARGETHolidays() /* businessdayCalendar */,
					0 /* fixingOffsetDays */,
					0 /* paymentOffsetDays */);

			Schedule legSchedulePay = ScheduleGenerator.createScheduleFromConventions(
					LocalDate.of(2015, Month.JANUARY, 03) /* referenceDate */,
					LocalDate.of(2015, Month.JANUARY, 06) /* startDate */,
					LocalDate.of(2025, Month.JANUARY, 06) /* maturityDate */,
					ScheduleGenerator.Frequency.QUARTERLY /* frequency */,
					ScheduleGenerator.DaycountConvention.ACT_365 /* daycountConvention */,
					ScheduleGenerator.ShortPeriodConvention.FIRST /* shortPeriodConvention */,
					BusinessdayCalendar.DateRollConvention.FOLLOWING /* dateRollConvention */,
					new BusinessdayCalendarExcludingTARGETHolidays() /* businessdayCalendar */,
					0 /* fixingOffsetDays */,
					0 /* paymentOffsetDays */);
			AbstractNotional notional = new Notional(1.0);
			AbstractIndex index = new LIBORIndex(null /*"forwardCurve"*/, 0.0, 0.25);
			double fixedCoupon = 0.0025;
			
			SwapLeg swapLegRec = new SwapLeg(legScheduleRec, notional, null, fixedCoupon /* spread */, false /* isNotionalExchanged */);
			SwapLeg swapLegPay = new SwapLeg(legSchedulePay, notional, index, 0.0 /* spread */, false /* isNotionalExchanged */);
		return new Swap(swapLegRec, swapLegPay);
	}
	
	
	
	
	
	
	
	
	
	/**
	 * This class calibrates the different models: <\br>
	 * HW for HullWhite, LMM for Libor Market Model
	 * @param string
	 * @return
	 * @throws SolverException 
	 * @throws CalculationException 
	 */
	private static LIBORModelMonteCarloSimulationModel getModel(String modelName) throws SolverException, CalculationException {
		
		//Create the calibration Items
		
		final int numberOfPaths		= 1000;
		/*
		 * Calibration of rate curves
		 */
		final AnalyticModel curveModel = CalibrationItem.getCalibratedCurve();
		
		// Create the forward curve (initial value of the LIBOR market model)
				final ForwardCurve forwardCurve = curveModel.getForwardCurve("ForwardCurveFromDiscountCurve(discountCurve-EUR,6M)");
				final DiscountCurve discountCurve = curveModel.getDiscountCurve("discountCurve-EUR");

				//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++		
				/*
				 * Calibration of model volatilities         (via ATM swap vols normal)
				 */


				/*
				 * Create a set of calibration products.
				 */
				ArrayList<String>					calibrationItemNames	= new ArrayList<>();
				final ArrayList<CalibrationProduct>	calibrationProducts		= new ArrayList<>();

				double	swapPeriodLength	= 0.5;

				//Create co-terminals (atmExpiry + atmTenor = 11Y)
				String[] atmExpiries = {"1Y", "2Y", "3Y", "4Y", "5Y", "7Y", "10Y"};
				String[] atmTenors = {"1Y", "4Y", "6Y", "7Y", "8Y", "9Y", "10Y"};

				double[] atmNormalVolatilities = {0.00504, 0.005, 0.00495, 0.00454, 0.00418, 0.00404, 0.00394};

				LocalDate referenceDate = LocalDate.of(2019, Month.DECEMBER, 15);
				BusinessdayCalendarExcludingTARGETHolidays cal = new BusinessdayCalendarExcludingTARGETHolidays();
				DayCountConvention_ACT_365 modelDC = new DayCountConvention_ACT_365();
				
				
				for(int i=0; i<atmNormalVolatilities.length; i++ ) {

					LocalDate exerciseDate = cal.getDateFromDateAndOffsetCode(referenceDate, atmExpiries[i]);
					LocalDate tenorEndDate = cal.getDateFromDateAndOffsetCode(exerciseDate, atmTenors[i]);
					double	exercise		= modelDC.getDaycountFraction(referenceDate, exerciseDate);
					double	tenor			= modelDC.getDaycountFraction(exerciseDate, tenorEndDate);

					// We consider an idealized tenor grid (alternative: adapt the model grid)
					// To ensure the dates fit into the timediscretization
					exercise	= Math.round(exercise/0.25)*0.25;
					tenor		= Math.round(tenor/0.25)*0.25;

					if(exercise < 1.0) {
						continue;
					}

					int numberOfPeriods = (int)Math.round(tenor / swapPeriodLength);

					double	moneyness			= 0.0;
					double	targetVolatility	= atmNormalVolatilities[i];

					String	targetVolatilityType = "VOLATILITYNORMAL";

					double	weight = 1.0;

					calibrationProducts.add(CalibrationItem.createCalibrationItem(weight, exercise, swapPeriodLength, numberOfPeriods, moneyness, targetVolatility, targetVolatilityType, forwardCurve, discountCurve));
					calibrationItemNames.add(atmExpiries[i]+"\t"+atmTenors[i]);
				}
				
				// If simulation time is below libor time, exceptions will be hard to track.
				double lastTime	= 40.0;
				double dt		= 0.5;
				TimeDiscretizationFromArray timeDiscretizationFromArraylibor = new TimeDiscretizationFromArray(0.0, (int) (lastTime / dt), dt);
				final TimeDiscretization liborPeriodDiscretization = timeDiscretizationFromArraylibor;

				
				double lastTime2	= 40.0;
				double dt2		= 0.25;
				TimeDiscretizationFromArray timeDiscretizationFromArray = new TimeDiscretizationFromArray(0.0, (int) (lastTime / dt), dt);
				
				
				
		switch(modelName){
		
	// HULL WHITE
	case "HW":
			TimeDiscretization volatilityDiscretization = new TimeDiscretizationFromArray(new double[] {0, 1 ,2, 3, 5, 7, 10, 15});

			RandomVariableFactory randomVariableFactory = new RandomVariableFactory();

			AbstractShortRateVolatilityModel volatilityModel =
					new ShortRateVolatilityModelPiecewiseConstant(randomVariableFactory, 
							timeDiscretizationFromArray, volatilityDiscretization, 
							new double[] {0.02}, new double[] {0.1}, true);


			BrownianMotionLazyInit brownianMotion = new BrownianMotionLazyInit(timeDiscretizationFromArray, 2 /* numberOfFactors */, numberOfPaths, 3141 /* seed */);


			//		//Create map (mainly use calibration defaults)
			Map<String, Object> properties = new HashMap<>();
			Map<String, Object> calibrationParametersLMM = new HashMap<>();
			calibrationParametersLMM.put("brownianMotion", brownianMotion);
			properties.put("calibrationParameters", calibrationParametersLMM);


			CalibrationProduct[] calibrationItemsHW = new CalibrationProduct[calibrationItemNames.size()];
			for(int i=0; i<calibrationItemNames.size(); i++) {
				calibrationItemsHW[i] = new CalibrationProduct(calibrationProducts.get(i).getProduct(),calibrationProducts.get(i).getTargetValue(),calibrationProducts.get(i).getWeight());
			}

			HullWhiteModel hullWhiteModel = HullWhiteModel.of(randomVariableFactory,
					liborPeriodDiscretization, new AnalyticModelFromCurvesAndVols(new Curve[] {discountCurve, forwardCurve}), 
					forwardCurve, discountCurve,
					volatilityModel, calibrationItemsHW, properties);

			EulerSchemeFromProcessModel process = new EulerSchemeFromProcessModel(brownianMotion, EulerSchemeFromProcessModel.Scheme.EULER);
			
			
		return new LIBORMonteCarloSimulationFromLIBORModel(hullWhiteModel, process);
	
	// LIBOR MARKET MODEL
	case "LMM":
		
		int numberOfFactors = 3;
		
		/*
		 * Create Brownian motion
		 */
		BrownianMotion brownianMotionLMM = new net.finmath.montecarlo.BrownianMotionLazyInit(timeDiscretizationFromArray, numberOfFactors, numberOfPaths, 31415 /* seed */);
	
		

		//Create a volatility Model
		//LIBORVolatilityModel volatilityModel = new LIBORVolatilityModelPiecewiseConstant(timeDiscretizationFromArray, liborPeriodDiscretization, new TimeDiscretizationFromArray(0.00, 1.0, 2.0, 5.0, 10.0, 20.0, 30.0, 40.0), new TimeDiscretizationFromArray(0.00, 1.0, 2.0, 5.0, 10.0, 20.0, 30.0, 40.0), 0.50 / 100);
		LIBORVolatilityModel volatilityModelLMM = new LIBORVolatilityModelPiecewiseConstant(timeDiscretizationFromArray, liborPeriodDiscretization, new TimeDiscretizationFromArray(0, 1 ,2, 3, 5, 7, 10, 15), new TimeDiscretizationFromArray(0, 1 ,2, 3, 5, 7, 10, 15), 0.50 / 100);
		
		
		
		//Create correlationModel
		LIBORCorrelationModel correlationModel = new LIBORCorrelationModelExponentialDecay(timeDiscretizationFromArray, liborPeriodDiscretization, numberOfFactors, 0.05, false);
		
		// Create a covariance model
		AbstractLIBORCovarianceModelParametric covarianceModelParametric = new LIBORCovarianceModelFromVolatilityAndCorrelation(timeDiscretizationFromArray, liborPeriodDiscretization, volatilityModelLMM, correlationModel);
		
		// Create blended local volatility model with fixed parameter (0=lognormal, > 1 = almost a normal model).
		AbstractLIBORCovarianceModelParametric covarianceModelDisplaced = new DisplacedLocalVolatilityModel(covarianceModelParametric, 1.0/0.25, false /* isCalibrateable */);
		
		
		
		
				// Set model properties
		Map<String, Object> propertiesLMM = new HashMap<>();

		// Choose the simulation measure
		propertiesLMM.put("measure", LIBORMarketModelFromCovarianceModel.Measure.SPOT.name());

		// Choose normal state space for the Euler scheme (the covariance model above carries a linear local volatility model, such that the resulting model is log-normal).
		propertiesLMM.put("stateSpace", LIBORMarketModelFromCovarianceModel.StateSpace.NORMAL.name());

	                      // works also without
		// Set calibration properties (should use our brownianMotion for calibration - needed to have to right correlation).
		Double accuracy = new Double(1E-6);	// Lower accuracy to reduce runtime of the unit test
		int maxIterations = 100;
		int numberOfThreads = 4;
		OptimizerFactory optimizerFactory = new OptimizerFactoryLevenbergMarquardt(maxIterations, accuracy, numberOfThreads);
		
		
		double[] parameterStandardDeviation = new double[covarianceModelParametric.getParameterAsDouble().length];
		double[] parameterLowerBound = new double[covarianceModelParametric.getParameterAsDouble().length];
		double[] parameterUpperBound = new double[covarianceModelParametric.getParameterAsDouble().length];
		Arrays.fill(parameterStandardDeviation, 0.20/100.0);
		Arrays.fill(parameterLowerBound, 0.0);
		Arrays.fill(parameterUpperBound, Double.POSITIVE_INFINITY);
		
		
		
		// Set calibration properties (should use our brownianMotion for calibration - needed to have to right correlation).
		Map<String, Object> calibrationParametersLMM2 = new HashMap<>();
		calibrationParametersLMM2.put("accuracy", accuracy);
		calibrationParametersLMM2.put("brownianMotion", brownianMotionLMM);
		calibrationParametersLMM2.put("optimizerFactory", optimizerFactory);
		calibrationParametersLMM2.put("parameterStep", new Double(1E-4));
		propertiesLMM.put("calibrationParameters", calibrationParametersLMM2);
		
		
		// The calibrationItems
		CalibrationProduct[] calibrationItems = new CalibrationProduct[calibrationItemNames.size()];
		for(int i=0; i<calibrationItemNames.size(); i++) {
			calibrationItems[i] = new CalibrationProduct(calibrationProducts.get(i).getProduct(),calibrationProducts.get(i).getTargetValue(),calibrationProducts.get(i).getWeight());
		}
		
		LIBORMarketModelFromCovarianceModel liborMarketModelCalibrated = new LIBORMarketModelFromCovarianceModel(
				liborPeriodDiscretization,
				curveModel,
				forwardCurve, new DiscountCurveFromForwardCurve(forwardCurve),
				covarianceModelDisplaced,
				calibrationItems,
				propertiesLMM);
		
		
		//LIBORModel liborModelClibrated = l
		LIBORCovarianceModel covModel2 = liborMarketModelCalibrated.getCovarianceModel();
		

		// Set model properties
		Map<String, String> properties2 = new HashMap<>();

		// Choose the simulation measure
		properties2.put("measure", LIBORMarketModelFromCovarianceModel.Measure.SPOT.name());

		// Choose log normal model
		properties2.put("stateSpace", LIBORMarketModelFromCovarianceModel.StateSpace.NORMAL.name());
		
		
		
		
		// Empty array of calibration items - hence, model will use given covariance
		CalibrationProduct[] calibrationItems2 = new CalibrationProduct[0];
		
		LIBORMarketModel liborMarketModel = new LIBORMarketModelFromCovarianceModel(
				liborPeriodDiscretization, curveModel, forwardCurve, discountCurve, covModel2, calibrationItems2, properties2);

		
		/*
		 * Test our calibration
		 */
		System.out.println("\nCalibrated parameters are:");
		double[] param = ((AbstractLIBORCovarianceModelParametric)((LIBORMarketModelFromCovarianceModel) liborMarketModelCalibrated).getCovarianceModel()).getParameterAsDouble();
		//		((AbstractLIBORCovarianceModelParametric) liborMarketModelCalibrated.getCovarianceModel()).setParameter(param);
		for (double p : param) {
			System.out.println(formatterParam.format(p));
		}
		
		
		
		EulerSchemeFromProcessModel processLMM = new EulerSchemeFromProcessModel(brownianMotionLMM, EulerSchemeFromProcessModel.Scheme.EULER);
		
		LIBORModelMonteCarloSimulationModel LMMSimulation = new LIBORMonteCarloSimulationFromLIBORModel(liborMarketModelCalibrated, processLMM);
		
		System.out.println("\nValuation on calibrated model:");
		double deviationSum			= 0.0;
		double deviationSquaredSum	= 0.0;
		for (int i = 0; i < calibrationProducts.size(); i++) {
			AbstractLIBORMonteCarloProduct calibrationProduct = calibrationProducts.get(i).getProduct();
			try {
				double valueModel = calibrationProduct.getValue(LMMSimulation);
				double valueTarget = calibrationProducts.get(i).getTargetValue().getAverage();
				double error = valueModel-valueTarget;
				deviationSum += error;
				deviationSquaredSum += error*error;
				System.out.println(calibrationItemNames.get(i) + "\t" + "Model: " + formatterValue.format(valueModel) + "\t Target: " + formatterValue.format(valueTarget) + "\t Deviation: " + formatterDeviation.format(valueModel-valueTarget));// + "\t" + calibrationProduct.toString());
			}
			catch(Exception e) {
			}
		}
		
		double averageDeviation = deviationSum/calibrationProducts.size();
		System.out.println("Mean Deviation:" + formatterDeviation	.format(averageDeviation));
		System.out.println("RMS Error.....:" + formatterDeviation	.format(Math.sqrt(deviationSquaredSum/calibrationProducts.size())));
		System.out.println("__________________________________________________________________________________________\n");

		
		
		
		return  LMMSimulation;
		
		default:
			System.out.println("Incorrect model name");
			return null;
		}
		//return null;
	}

}
