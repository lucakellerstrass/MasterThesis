package kellerstrass.exposure.test;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.Month;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import kellerstrass.exposure.ExposureEstimator;
import net.finmath.exception.CalculationException;
import net.finmath.marketdata.model.curves.DiscountCurveInterpolation;
import net.finmath.marketdata.model.curves.ForwardCurveInterpolation;
import net.finmath.montecarlo.RandomVariableFromDoubleArray;
import net.finmath.montecarlo.interestrate.CalibrationProduct;
import net.finmath.montecarlo.interestrate.LIBORMarketModel;
import net.finmath.montecarlo.interestrate.LIBORModelMonteCarloSimulationModel;
import net.finmath.montecarlo.interestrate.LIBORMonteCarloSimulationFromLIBORModel;
import net.finmath.montecarlo.interestrate.models.LIBORMarketModelFromCovarianceModel;
import net.finmath.montecarlo.interestrate.models.LIBORMarketModelFromCovarianceModel.Measure;
import net.finmath.montecarlo.interestrate.models.covariance.LIBORCorrelationModelExponentialDecay;
import net.finmath.montecarlo.interestrate.models.covariance.LIBORCovarianceModelFromVolatilityAndCorrelation;
import net.finmath.montecarlo.interestrate.models.covariance.LIBORVolatilityModelFromGivenMatrix;
import net.finmath.montecarlo.interestrate.products.AbstractLIBORMonteCarloProduct;
import net.finmath.montecarlo.interestrate.products.Swap;
import net.finmath.montecarlo.interestrate.products.SwapLeg;
import net.finmath.montecarlo.interestrate.products.TermStructureMonteCarloProduct;
import net.finmath.montecarlo.interestrate.products.components.AbstractNotional;
import net.finmath.montecarlo.interestrate.products.components.Notional;
import net.finmath.montecarlo.interestrate.products.indices.AbstractIndex;
import net.finmath.montecarlo.interestrate.products.indices.LIBORIndex;
import net.finmath.montecarlo.process.EulerSchemeFromProcessModel;
import net.finmath.stochastic.RandomVariable;
import net.finmath.time.Schedule;
import net.finmath.time.ScheduleGenerator;
import net.finmath.time.TimeDiscretizationFromArray;
import net.finmath.time.businessdaycalendar.BusinessdayCalendar;
import net.finmath.time.businessdaycalendar.BusinessdayCalendarExcludingTARGETHolidays;

public class ExposureEstimatorTest {

	private final NumberFormat formatter6 = new DecimalFormat("0.000000", new DecimalFormatSymbols(new Locale("en")));
	
	@Test
	public  void testExposurePaths() throws CalculationException {
		System.out.println("Single exposure paths ");
		AbstractLIBORMonteCarloProduct swap = getSwap();
		TermStructureMonteCarloProduct swapExposureEstimator = new ExposureEstimator(swap);
		
		LIBORModelMonteCarloSimulationModel simulationModel = getModel();
				
		int numberOfExampleExposurePaths = 10;
		
	
		// store exposure  exposure paths             		
	double[][] exposurePaths  = new double[numberOfExampleExposurePaths][simulationModel.getTimeDiscretization().getNumberOfTimes()];
	double[][] PositiveExposurePaths  = new double[numberOfExampleExposurePaths][simulationModel.getTimeDiscretization().getNumberOfTimes()];
	  
	for(double observationDate : simulationModel.getTimeDiscretization()) {
                    int timeIndex = simulationModel.getTimeDiscretization().getTimeIndex(observationDate);
                    
					if(observationDate == 0) {
						continue;
					}
					/*
					 * Calculate the estimated exposure
					 */
					RandomVariable valuesSwap = swap.getValue(observationDate, simulationModel);
					RandomVariable valuesEstimatedExposure = swapExposureEstimator.getValue(observationDate, simulationModel);
					RandomVariable valuesPositiveExposure = valuesSwap.mult(valuesEstimatedExposure.choose(new RandomVariableFromDoubleArray(1.0), new RandomVariableFromDoubleArray(0.0)));
					
					for(int path = 0; path < numberOfExampleExposurePaths; path ++) {
					double exposureOnPath				= valuesEstimatedExposure.get(path);
					double positiveExposureOnPath       = valuesPositiveExposure.get(path);

					exposurePaths[path][timeIndex]= exposureOnPath;
					PositiveExposurePaths[path][timeIndex] = positiveExposureOnPath;
				}	
		}
		
				//Print exposure paths
				System.out.print("observationDate  \t  ");
				for(int path = 0; path < exposurePaths.length ; path++ ) {
					System.out.print("path("+ path + ") \t  ");					
				}System.out.println("");
				
				
				
					for(double observationDate : simulationModel.getTimeDiscretization()) {
	                    int timeIndex = simulationModel.getTimeDiscretization().getTimeIndex(observationDate);
	                    
						if(observationDate == 0) {
							continue;
						}
					System.out.print(observationDate + " \t  ");
						
						for(int path = 0; path < exposurePaths.length ; path++ ) {
												System.out.print(formatter6.format(exposurePaths[path][timeIndex]) + "\t");
					}
					System.out.println("");		
				}
	}



	@Test
	public  void testExpectedPositiveExposure() throws CalculationException {
		System.out.println("Expected Exposure ");
		
		AbstractLIBORMonteCarloProduct swap = getSwap();
		TermStructureMonteCarloProduct swapExposureEstimator = new ExposureEstimator(swap);
		
		LIBORModelMonteCarloSimulationModel simulationModel = getModel();
		
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
	 * get a pre-defined model
	 * @return
	 * @throws CalculationException 
	 */
	private LIBORModelMonteCarloSimulationModel getModel() throws CalculationException {
		Measure measure = Measure.SPOT;
		 int numberOfPaths = 20000;
		 int numberOfFactors = 5;
		 double correlationDecayParam = 0.1;
		/*
		 * Create the libor tenor structure and the initial values
		 */
		double liborPeriodLength	= 0.25;
		double liborRateTimeHorzion	= 20.0;
		TimeDiscretizationFromArray liborPeriodDiscretization = new TimeDiscretizationFromArray(0.0, (int) (liborRateTimeHorzion / liborPeriodLength), liborPeriodLength);

		// Create the forward curve (initial value of the LIBOR market model)
		ForwardCurveInterpolation forwardCurveInterpolation = ForwardCurveInterpolation.createForwardCurveFromForwards(
				"forwardCurve"								/* name of the curve */,
				new double[] {0.0 , 1.0 , 2.0 , 5.0 , 40.0}	/* fixings of the forward */,
				new double[] {0.025, 0.025, 0.025, 0.025, 0.025}	/* forwards */,
				liborPeriodLength							/* tenor / period length */
				);

		// Create the discount curve
		DiscountCurveInterpolation discountCurveInterpolation = DiscountCurveInterpolation.createDiscountCurveFromZeroRates(
				"discountCurve"								/* name of the curve */,
				new double[] {0.5 , 1.0 , 2.0 , 5.0 , 40.0}	/* maturities */,
				new double[] {0.02, 0.02, 0.02, 0.02, 0.03}	/* zero rates */
				);

		/*
		 * Create a simulation time discretization
		 */
		double lastTime	= 15.0;
		double dt		= 0.125;

		TimeDiscretizationFromArray timeDiscretizationFromArray = new TimeDiscretizationFromArray(0.0, (int) (lastTime / dt), dt);

		/*
		 * Create a volatility structure v[i][j] = sigma_j(t_i)
		 */
		double[][] volatility = new double[timeDiscretizationFromArray.getNumberOfTimeSteps()][liborPeriodDiscretization.getNumberOfTimeSteps()];
		for (int timeIndex = 0; timeIndex < volatility.length; timeIndex++) {
			for (int liborIndex = 0; liborIndex < volatility[timeIndex].length; liborIndex++) {
				// Create a very simple volatility model here
				double time = timeDiscretizationFromArray.getTime(timeIndex);
				double maturity = liborPeriodDiscretization.getTime(liborIndex);
				double timeToMaturity = maturity - time;

				double instVolatility;
				if(timeToMaturity <= 0) {
					instVolatility = 0;				// This forward rate is already fixed, no volatility
				} else {
					instVolatility = 0.3 + 0.2 * Math.exp(-0.25 * timeToMaturity);
				}

				// Store
				volatility[timeIndex][liborIndex] = instVolatility;
			}
		}
		LIBORVolatilityModelFromGivenMatrix volatilityModel = new LIBORVolatilityModelFromGivenMatrix(timeDiscretizationFromArray, liborPeriodDiscretization, volatility);

		/*
		 * Create a correlation model rho_{i,j} = exp(-a * abs(T_i-T_j))
		 */
		LIBORCorrelationModelExponentialDecay correlationModel = new LIBORCorrelationModelExponentialDecay(
				timeDiscretizationFromArray, liborPeriodDiscretization, numberOfFactors,
				correlationDecayParam);


		/*
		 * Combine volatility model and correlation model to a covariance model
		 */
		LIBORCovarianceModelFromVolatilityAndCorrelation covarianceModel =
				new LIBORCovarianceModelFromVolatilityAndCorrelation(timeDiscretizationFromArray,
						liborPeriodDiscretization, volatilityModel, correlationModel);

		// BlendedLocalVolatlityModel (future extension)
		//		AbstractLIBORCovarianceModel covarianceModel2 = new BlendedLocalVolatlityModel(covarianceModel, 0.00, false);

		// Set model properties
		Map<String, String> properties = new HashMap<>();

		// Choose the simulation measure
		properties.put("measure", measure.name());

		// Choose log normal model
		properties.put("stateSpace", LIBORMarketModelFromCovarianceModel.StateSpace.LOGNORMAL.name());

		// Empty array of calibration items - hence, model will use given covariance
		CalibrationProduct[] calibrationItems = new CalibrationProduct[0];

		/*
		 * Create corresponding LIBOR Market Model
		 */
		LIBORMarketModel liborMarketModel = new LIBORMarketModelFromCovarianceModel(
				liborPeriodDiscretization, forwardCurveInterpolation, discountCurveInterpolation, covarianceModel, calibrationItems, properties);

		EulerSchemeFromProcessModel process = new EulerSchemeFromProcessModel(
				new net.finmath.montecarlo.BrownianMotionLazyInit(timeDiscretizationFromArray,
						numberOfFactors, numberOfPaths, 3141 /* seed */), EulerSchemeFromProcessModel.Scheme.PREDICTOR_CORRECTOR);

		return new LIBORMonteCarloSimulationFromLIBORModel(liborMarketModel, process);
	}




    /**
     * Get a swap, with some example input parameters
     * @return
     */
	private AbstractLIBORMonteCarloProduct getSwap() {
	
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
			AbstractIndex index = new LIBORIndex("forwardCurve", 0.0, 0.25);
			double fixedCoupon = 0.025;
			
			SwapLeg swapLegRec = new SwapLeg(legScheduleRec, notional, null, fixedCoupon /* spread */, false /* isNotionalExchanged */);
			SwapLeg swapLegPay = new SwapLeg(legSchedulePay, notional, index, 0.0 /* spread */, false /* isNotionalExchanged */);
		return new Swap(swapLegRec, swapLegPay);
	}

}
