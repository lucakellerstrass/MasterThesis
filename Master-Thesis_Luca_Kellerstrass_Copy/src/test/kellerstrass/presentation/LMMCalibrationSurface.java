package kellerstrass.presentation;

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

import kellerstrass.Calibration.CalibrationItem;
import kellerstrass.exposure.ExposureEstimator;
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

public class LMMCalibrationSurface {

	private static DecimalFormat formatterValue		= new DecimalFormat(" ##0.000%;-##0.000%", new DecimalFormatSymbols(Locale.ENGLISH));
	private static DecimalFormat formatterParam		= new DecimalFormat(" #0.000;-#0.000", new DecimalFormatSymbols(Locale.ENGLISH));
	private static DecimalFormat formatterDeviation	= new DecimalFormat(" 0.00000E00;-0.00000E00", new DecimalFormatSymbols(Locale.ENGLISH));
	private final static NumberFormat formatter6 = new DecimalFormat("0.000000", new DecimalFormatSymbols(new Locale("en")));
	
	
	
	
	
	
	
	public static void main(String[] args) throws SolverException, CalculationException {
		LIBORModelMonteCarloSimulationModel simulationModel = doATMSwaptionCalibration();
		
		AbstractLIBORMonteCarloProduct swap = getSwap();
		
		TermStructureMonteCarloProduct swapExposureEstimator = new ExposureEstimator(swap);
		
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


		}
		
		

	}








	private static LIBORModelMonteCarloSimulationModel doATMSwaptionCalibration() throws SolverException, CalculationException {
		final int numberOfPaths		= 1000;
		final int numberOfFactors	= 1;
		
		/*
		 * Calibration test
		 */
		System.out.println("Calibration to Swaptions.\n");

		/*
		 * Calibration of rate curves
		 */
		System.out.println("Calibration of rate curves:     (calibration of the discount an forward rate via ATM swatps)");
		
		
		final AnalyticModel curveModel = CalibrationItem.getCalibratedCurve();
		
		// Create the forward curve (initial value of the LIBOR market model)
		final ForwardCurve forwardCurve = curveModel.getForwardCurve("ForwardCurveFromDiscountCurve(discountCurve-EUR,6M)");
		final DiscountCurve discountCurve = curveModel.getDiscountCurve("discountCurve-EUR");
		
        //curveModel.addCurve(discountCurve.getName(), discountCurve);
        //curveModel.addCurve(forwardCurve.getName(), forwardCurve);
		
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++		
		/*
		 * Calibration of model volatilities         (via ATM swap vols normal)
		 */
		System.out.println("Brute force Monte-Carlo calibration of model volatilities:");

		
		double calibrationStart = System.currentTimeMillis();
		
		/*
		 * Create a set of calibration products.
		 */
		ArrayList<String>					calibrationItemNames	= new ArrayList<>();
		final ArrayList<CalibrationProduct>	calibrationProducts		= new ArrayList<>();
		
		double	swapPeriodLength	= 0.5;
		
		//Full Volatilityy
				String[] atmExpiries = { "1M", "1M", "1M", "1M", "1M", "1M", "1M", "1M", "1M", "1M", "1M", "1M", "1M", "1M",
						"3M", "3M", "3M", "3M", "3M", "3M", "3M", "3M", "3M", "3M", "3M", "3M", "3M", "3M", "6M", "6M", "6M",
						"6M", "6M", "6M", "6M", "6M", "6M", "6M", "6M", "6M", "6M", "6M", "1Y", "1Y", "1Y", "1Y", "1Y", "1Y",
						"1Y", "1Y", "1Y", "1Y", "1Y", "1Y", "1Y", "1Y", "2Y", "2Y", "2Y", "2Y", "2Y", "2Y", "2Y", "2Y", "2Y",
						"2Y", "2Y", "2Y", "2Y", "2Y", "3Y", "3Y", "3Y", "3Y", "3Y", "3Y", "3Y", "3Y", "3Y", "3Y", "3Y", "3Y",
						"3Y", "3Y", "4Y", "4Y", "4Y", "4Y", "4Y", "4Y", "4Y", "4Y", "4Y", "4Y", "4Y", "4Y", "4Y", "4Y", "5Y",
						"5Y", "5Y", "5Y", "5Y", "5Y", "5Y", "5Y", "5Y", "5Y", "5Y", "5Y", "5Y", "5Y", "7Y", "7Y", "7Y", "7Y",
						"7Y", "7Y", "7Y", "7Y", "7Y", "7Y", "7Y", "7Y", "7Y", "7Y", "10Y", "10Y", "10Y", "10Y", "10Y", "10Y",
						"10Y", "10Y", "10Y", "10Y", "10Y", "10Y", "10Y", "10Y", "15Y", "15Y", "15Y", "15Y", "15Y", "15Y", "15Y",
						"15Y", "15Y", "15Y", "15Y", "15Y", "15Y", "15Y", "20Y", "20Y", "20Y", "20Y", "20Y", "20Y", "20Y", "20Y",
						"20Y", "20Y", "20Y", "20Y", "20Y", "20Y", "25Y", "25Y", "25Y", "25Y", "25Y", "25Y", "25Y", "25Y", "25Y",
						"25Y", "25Y", "25Y", "25Y", "25Y", "30Y", "30Y", "30Y", "30Y", "30Y", "30Y", "30Y", "30Y", "30Y", "30Y",
						"30Y", "30Y", "30Y", "30Y" };
				String[] atmTenors = { "1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "15Y", "20Y", "25Y", "30Y",
						"1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "15Y", "20Y", "25Y", "30Y", "1Y", "2Y",
						"3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "15Y", "20Y", "25Y", "30Y", "1Y", "2Y", "3Y", "4Y",
						"5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "15Y", "20Y", "25Y", "30Y", "1Y", "2Y", "3Y", "4Y", "5Y", "6Y",
						"7Y", "8Y", "9Y", "10Y", "15Y", "20Y", "25Y", "30Y", "1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y",
						"9Y", "10Y", "15Y", "20Y", "25Y", "30Y", "1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y",
						"15Y", "20Y", "25Y", "30Y", "1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "15Y", "20Y",
						"25Y", "30Y", "1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "15Y", "20Y", "25Y", "30Y",
						"1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "15Y", "20Y", "25Y", "30Y", "1Y", "2Y",
						"3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "15Y", "20Y", "25Y", "30Y", "1Y", "2Y", "3Y", "4Y",
						"5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "15Y", "20Y", "25Y", "30Y", "1Y", "2Y", "3Y", "4Y", "5Y", "6Y",
						"7Y", "8Y", "9Y", "10Y", "15Y", "20Y", "25Y", "30Y", "1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y",
						"9Y", "10Y", "15Y", "20Y", "25Y", "30Y" };
				double[] atmNormalVolatilities = { 0.00151, 0.00169, 0.0021, 0.00248, 0.00291, 0.00329, 0.00365, 0.004, 0.00437,
						0.00466, 0.00527, 0.00571, 0.00604, 0.00625, 0.0016, 0.00174, 0.00217, 0.00264, 0.00314, 0.00355,
						0.00398, 0.00433, 0.00469, 0.00493, 0.00569, 0.00607, 0.00627, 0.00645, 0.00182, 0.00204, 0.00238,
						0.00286, 0.00339, 0.00384, 0.00424, 0.00456, 0.00488, 0.0052, 0.0059, 0.00623, 0.0064, 0.00654, 0.00205,
						0.00235, 0.00272, 0.0032, 0.00368, 0.00406, 0.00447, 0.00484, 0.00515, 0.00544, 0.00602, 0.00629,
						0.0064, 0.00646, 0.00279, 0.00319, 0.0036, 0.00396, 0.00436, 0.00469, 0.00503, 0.0053, 0.00557, 0.00582,
						0.00616, 0.00628, 0.00638, 0.00641, 0.00379, 0.00406, 0.00439, 0.00472, 0.00504, 0.00532, 0.0056,
						0.00582, 0.00602, 0.00617, 0.0063, 0.00636, 0.00638, 0.00639, 0.00471, 0.00489, 0.00511, 0.00539,
						0.00563, 0.00583, 0.006, 0.00618, 0.0063, 0.00644, 0.00641, 0.00638, 0.00635, 0.00634, 0.00544, 0.00557,
						0.00572, 0.00591, 0.00604, 0.00617, 0.0063, 0.00641, 0.00651, 0.00661, 0.00645, 0.00634, 0.00627,
						0.00624, 0.00625, 0.00632, 0.00638, 0.00644, 0.0065, 0.00655, 0.00661, 0.00667, 0.00672, 0.00673,
						0.00634, 0.00614, 0.00599, 0.00593, 0.00664, 0.00671, 0.00675, 0.00676, 0.00676, 0.00675, 0.00676,
						0.00674, 0.00672, 0.00669, 0.00616, 0.00586, 0.00569, 0.00558, 0.00647, 0.00651, 0.00651, 0.00651,
						0.00652, 0.00649, 0.00645, 0.0064, 0.00637, 0.00631, 0.00576, 0.00534, 0.00512, 0.00495, 0.00615,
						0.0062, 0.00618, 0.00613, 0.0061, 0.00607, 0.00602, 0.00596, 0.00591, 0.00586, 0.00536, 0.00491,
						0.00469, 0.0045, 0.00578, 0.00583, 0.00579, 0.00574, 0.00567, 0.00562, 0.00556, 0.00549, 0.00545,
						0.00538, 0.00493, 0.00453, 0.00435, 0.0042, 0.00542, 0.00547, 0.00539, 0.00532, 0.00522, 0.00516,
						0.0051, 0.00504, 0.005, 0.00495, 0.00454, 0.00418, 0.00404, 0.00394 };
				
				LocalDate referenceDate = LocalDate.of(2016, Month.SEPTEMBER, 30);
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
				/*
				 * Create a simulation time discretization
				 */
				// If simulation time is below libor time, exceptions will be hard to track.
				double lastTime	= 40.0;
				double dt		= 0.25;
				TimeDiscretizationFromArray timeDiscretizationFromArray = new TimeDiscretizationFromArray(0.0, (int) (lastTime / dt), dt);
				final TimeDiscretization liborPeriodDiscretization = timeDiscretizationFromArray;

		        
				   //TimeDiscretization volatilityDiscretization = new TimeDiscretizationFromArray(new double[] {0, 1 ,2, 3, 5, 7, 10, 15});
                
				   //RandomVariableFactory randomVariableFactory = new RandomVariableFactory();
				
				/*
				 * Create Brownian motion
				 */
				BrownianMotion brownianMotion = new net.finmath.montecarlo.BrownianMotionLazyInit(timeDiscretizationFromArray, numberOfFactors, numberOfPaths, 31415 /* seed */);
			
				

				//Create a volatility Model
				//LIBORVolatilityModel volatilityModel = new LIBORVolatilityModelPiecewiseConstant(timeDiscretizationFromArray, liborPeriodDiscretization, new TimeDiscretizationFromArray(0.00, 1.0, 2.0, 5.0, 10.0, 20.0, 30.0, 40.0), new TimeDiscretizationFromArray(0.00, 1.0, 2.0, 5.0, 10.0, 20.0, 30.0, 40.0), 0.50 / 100);
				       LIBORVolatilityModel volatilityModel = new LIBORVolatilityModelPiecewiseConstant(timeDiscretizationFromArray, liborPeriodDiscretization, new TimeDiscretizationFromArray(0, 1 ,2, 3, 5, 7, 10, 15), new TimeDiscretizationFromArray(0, 1 ,2, 3, 5, 7, 10, 15), 0.50 / 100);
				//LIBORVolatilityModel volatilityModel = new LIBORVolatilityModelPiecewiseConstant(timeDiscretizationFromArray, liborPeriodDiscretization, new TimeDiscretizationFromArray(0, 1 ,2, 3, 4, 5, 7, 10, 15, 20, 25, 30), new TimeDiscretizationFromArray(0, 1 ,2, 3, 4, 5, 7, 10, 15, 20, 25, 30), 0.50 / 100);
				
				
				
				//Create correlationModel
				LIBORCorrelationModel correlationModel = new LIBORCorrelationModelExponentialDecay(timeDiscretizationFromArray, liborPeriodDiscretization, numberOfFactors, 0.05, false);
				
				// Create a covariance model
				AbstractLIBORCovarianceModelParametric covarianceModelParametric = new LIBORCovarianceModelFromVolatilityAndCorrelation(timeDiscretizationFromArray, liborPeriodDiscretization, volatilityModel, correlationModel);
				
				// Create blended local volatility model with fixed parameter (0=lognormal, > 1 = almost a normal model).
				AbstractLIBORCovarianceModelParametric covarianceModelDisplaced = new DisplacedLocalVolatilityModel(covarianceModelParametric, 1.0/0.25, false /* isCalibrateable */);
				
				
				
				
						// Set model properties
				Map<String, Object> properties = new HashMap<>();

				// Choose the simulation measure
				properties.put("measure", LIBORMarketModelFromCovarianceModel.Measure.SPOT.name());

				// Choose normal state space for the Euler scheme (the covariance model above carries a linear local volatility model, such that the resulting model is log-normal).
				properties.put("stateSpace", LIBORMarketModelFromCovarianceModel.StateSpace.NORMAL.name());

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
				Map<String, Object> calibrationParameters = new HashMap<>();
				calibrationParameters.put("accuracy", accuracy);
				calibrationParameters.put("brownianMotion", brownianMotion);
				calibrationParameters.put("optimizerFactory", optimizerFactory);
				calibrationParameters.put("parameterStep", new Double(1E-4));
				properties.put("calibrationParameters", calibrationParameters);
				
				
				// The calibrationItems
				CalibrationProduct[] calibrationItems = new CalibrationProduct[calibrationItemNames.size()];
				for(int i=0; i<calibrationItemNames.size(); i++) {
					calibrationItems[i] = new CalibrationProduct(calibrationProducts.get(i).getProduct(),calibrationProducts.get(i).getTargetValue(),calibrationProducts.get(i).getWeight());
				}
				
				LIBORModel liborMarketModelCalibrated = new LIBORMarketModelFromCovarianceModel(
						liborPeriodDiscretization,
						curveModel,
						forwardCurve, new DiscountCurveFromForwardCurve(forwardCurve),
						covarianceModelDisplaced,
						calibrationItems,
						properties);

			
				
				// The running time output
				double calibrationEnd = System.currentTimeMillis();
				System.out.println("The calculation took: " + (calibrationEnd - calibrationStart)/100 +  " min");
				
			
				
				
				
					
				
				/*
				 * Test our calibration
				 */
				System.out.println("\nCalibrated parameters are:");
				double[] param = ((AbstractLIBORCovarianceModelParametric)((LIBORMarketModelFromCovarianceModel) liborMarketModelCalibrated).getCovarianceModel()).getParameterAsDouble();
				//		((AbstractLIBORCovarianceModelParametric) liborMarketModelCalibrated.getCovarianceModel()).setParameter(param);
				for (double p : param) {
					System.out.println(formatterParam.format(p));
				}
				
				//The Process
				EulerSchemeFromProcessModel process = new EulerSchemeFromProcessModel(brownianMotion);

				//The simulationModel
				LIBORModelMonteCarloSimulationModel LMMSimulation = new LIBORMonteCarloSimulationFromLIBORModel(liborMarketModelCalibrated, process);

				
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
						System.out.println(calibrationItemNames.get(i) + "\t" + "Model: \t" + formatterValue.format(valueModel) + "\t Target: \t" + formatterValue.format(valueTarget) + "\t Deviation: \t" + formatterDeviation.format(valueModel-valueTarget));// + "\t" + calibrationProduct.toString());
					}
					catch(Exception e) {
					}
				}
				
				double averageDeviation = deviationSum/calibrationProducts.size();
				System.out.println("Mean Deviation:" + formatterDeviation	.format(averageDeviation));
				System.out.println("RMS Error.....:" + formatterDeviation	.format(Math.sqrt(deviationSquaredSum/calibrationProducts.size())));
				System.out.println("__________________________________________________________________________________________\n");
				
				
				return LMMSimulation;

				
				
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

}