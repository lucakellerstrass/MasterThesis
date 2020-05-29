package kellerstrass.presentation;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.junit.Test;

import kellerstrass.Calibration.CurveModelCalibrationItem;
import kellerstrass.exposure.ExposureMachine;
import net.finmath.exception.CalculationException;
import net.finmath.marketdata.model.AnalyticModel;
import net.finmath.marketdata.model.AnalyticModelFromCurvesAndVols;
import net.finmath.marketdata.model.curves.Curve;
import net.finmath.marketdata.model.curves.DiscountCurve;
import net.finmath.marketdata.model.curves.ForwardCurve;
import net.finmath.montecarlo.BrownianMotionLazyInit;
import net.finmath.montecarlo.RandomVariableFactory;
import net.finmath.montecarlo.RandomVariableFromDoubleArray;
import net.finmath.montecarlo.interestrate.CalibrationProduct;
import net.finmath.montecarlo.interestrate.LIBORModelMonteCarloSimulationModel;
import net.finmath.montecarlo.interestrate.LIBORMonteCarloSimulationFromLIBORModel;
import net.finmath.montecarlo.interestrate.models.HullWhiteModel;
import net.finmath.montecarlo.interestrate.models.covariance.AbstractShortRateVolatilityModel;
import net.finmath.montecarlo.interestrate.models.covariance.ShortRateVolatilityModelParametric;
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
import net.finmath.optimizer.SolverException;
import net.finmath.stochastic.RandomVariable;
import net.finmath.time.Schedule;
import net.finmath.time.ScheduleGenerator;
import net.finmath.time.TimeDiscretization;
import net.finmath.time.TimeDiscretizationFromArray;
import net.finmath.time.businessdaycalendar.BusinessdayCalendar;
import net.finmath.time.businessdaycalendar.BusinessdayCalendarExcludingTARGETHolidays;
import net.finmath.time.daycount.DayCountConvention_ACT_365;

public class HullWhiteCalibrationExposures {

	private static DecimalFormat formatterValue		= new DecimalFormat(" ##0.000%;-##0.000%", new DecimalFormatSymbols(Locale.ENGLISH));
	private static DecimalFormat formatterParam		= new DecimalFormat(" #0.000;-#0.000", new DecimalFormatSymbols(Locale.ENGLISH));
	private static DecimalFormat formatterDeviation	= new DecimalFormat(" 0.00000E00;-0.00000E00", new DecimalFormatSymbols(Locale.ENGLISH));
	private final static NumberFormat formatter6 = new DecimalFormat("0.000000", new DecimalFormatSymbols(new Locale("en")));
	
	
	
	
	
	
	
	public static void main(String[] args) throws SolverException, CalculationException {
		
		LIBORModelMonteCarloSimulationModel simulationModel = doATMSwaptionCalibration();

		
AbstractLIBORMonteCarloProduct swap = getSwap();
		
		TermStructureMonteCarloProduct swapExposureEstimator = new ExposureMachine(swap);
		
		
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
		final int numberOfPaths		= 100000;
		
		/*
		 * Calibration test
		 */
		System.out.println("Calibration to Swaptions.\n");

		/*
		 * Calibration of rate curves
		 */
		System.out.println("Calibration of rate curves:     (calibration of the discount an forward rate via ATM swatps)");
		
		final AnalyticModel curveModel = CurveModelCalibrationItem.getCalibratedCurve();
		
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
		
		//Create co-terminals (atmExpiry + atmTenor = 11Y)
				String[] atmExpiries = {"1Y", "2Y", "3Y", "4Y", "5Y", "7Y", "10Y"};
				String[] atmTenors = {"1Y", "4Y", "6Y", "7Y", "8Y", "9Y", "10Y"};
				
				double[] atmNormalVolatilities = {0.00504, 0.005, 0.00495, 0.00454, 0.00418, 0.00404, 0.00394};
				
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

					calibrationProducts.add(CurveModelCalibrationItem.createCalibrationItem(weight, exercise, swapPeriodLength, numberOfPeriods, moneyness, targetVolatility, targetVolatilityType, forwardCurve, discountCurve));
					calibrationItemNames.add(atmExpiries[i]+"\t"+atmTenors[i]);
				}
				
				// If simulation time is below libor time, exceptions will be hard to track.
				double lastTime	= 40.0;
				double dt		= 0.25;   //0.1
				TimeDiscretizationFromArray timeDiscretizationFromArray = new TimeDiscretizationFromArray(0.0, (int) (lastTime / dt), dt);
				final TimeDiscretization liborPeriodDiscretization = timeDiscretizationFromArray;

		        
				TimeDiscretization volatilityDiscretization = new TimeDiscretizationFromArray(new double[] {0, 1 ,2, 3, 5, 7, 10, 15});
                
				RandomVariableFactory randomVariableFactory = new RandomVariableFactory();
				
				AbstractShortRateVolatilityModel volatilityModel =
						new ShortRateVolatilityModelPiecewiseConstant(randomVariableFactory, 
								timeDiscretizationFromArray, volatilityDiscretization, 
								new double[] {0.02}, new double[] {0.1}, true);
				
				System.out.println("simple volatility model params");
				double[] param1 = ((ShortRateVolatilityModelParametric)volatilityModel).getParameterAsDouble();
				for (double p : param1) {
					System.out.println(p);
				}
				
				BrownianMotionLazyInit brownianMotion = new BrownianMotionLazyInit(timeDiscretizationFromArray, 2 /* numberOfFactors */, numberOfPaths, 31415 /* seed */);
				EulerSchemeFromProcessModel process = new EulerSchemeFromProcessModel(brownianMotion, EulerSchemeFromProcessModel.Scheme.EULER);

				
				//		//Create map (mainly use calibration defaults)
				Map<String, Object> properties = new HashMap<>();
				Map<String, Object> calibrationParameters = new HashMap<>();
				calibrationParameters.put("brownianMotion", brownianMotion);
				properties.put("calibrationParameters", calibrationParameters);
				
				
				CalibrationProduct[] calibrationItemsHW = new CalibrationProduct[calibrationItemNames.size()];
				for(int i=0; i<calibrationItemNames.size(); i++) {
					calibrationItemsHW[i] = new CalibrationProduct(calibrationProducts.get(i).getProduct(),calibrationProducts.get(i).getTargetValue(),calibrationProducts.get(i).getWeight());
				}
				
				HullWhiteModel hullWhiteModel = HullWhiteModel.of(randomVariableFactory,
						liborPeriodDiscretization, new AnalyticModelFromCurvesAndVols(new Curve[] {discountCurve, forwardCurve}), 
						forwardCurve, discountCurve,
						volatilityModel, calibrationItemsHW, properties);
				
				
				
				// The running time output
				double calibrationEnd = System.currentTimeMillis();
				System.out.println("The calculation took: " + (calibrationEnd - calibrationStart) +  " millis");
				
				
				
				
				
				
				
				
				
				System.out.println("\nCalibrated parameters are:");
				double[] param = ((ShortRateVolatilityModelParametric) hullWhiteModel.getVolatilityModel()).getParameterAsDouble();
				for (double p : param) {
					System.out.println(p);
				}
				

				LIBORModelMonteCarloSimulationModel hullWhiteModelSimulation = new LIBORMonteCarloSimulationFromLIBORModel(hullWhiteModel, process);

				
				System.out.println("\nValuation on calibrated model:");
				double deviationSum			= 0.0;
				double deviationSquaredSum	= 0.0;
				for (int i = 0; i < calibrationProducts.size(); i++) {
					AbstractLIBORMonteCarloProduct calibrationProduct = calibrationProducts.get(i).getProduct();
					try {
						double valueModel = calibrationProduct.getValue(hullWhiteModelSimulation);
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
				System.out.println("Mean Deviation:" + formatterDeviation.format(averageDeviation));
				System.out.println("RMS Error.....:" + formatterDeviation.format(Math.sqrt(deviationSquaredSum/calibrationProducts.size())));
				System.out.println("__________________________________________________________________________________________\n");
				
				
				return hullWhiteModelSimulation;

				
				
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
			double fixedCoupon = 0.0025; //0.0025;
			
			SwapLeg swapLegRec = new SwapLeg(legScheduleRec, notional, null, fixedCoupon /* spread */, false /* isNotionalExchanged */);
			SwapLeg swapLegPay = new SwapLeg(legSchedulePay, notional, index, 0.0 /* spread */, false /* isNotionalExchanged */);
		return new Swap(swapLegRec, swapLegPay);
	}

}
