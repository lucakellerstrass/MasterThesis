package kellerstrass.xva.test;

import java.time.LocalDate;
import java.time.Month;
import java.util.HashMap;
import java.util.Map;

import kellerstrass.xva.CVA;
import net.finmath.exception.CalculationException;
import net.finmath.marketdata.model.curves.DiscountCurveInterpolation;
import net.finmath.marketdata.model.curves.ForwardCurveInterpolation;
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
import net.finmath.montecarlo.interestrate.products.components.AbstractNotional;
import net.finmath.montecarlo.interestrate.products.components.Notional;
import net.finmath.montecarlo.interestrate.products.indices.AbstractIndex;
import net.finmath.montecarlo.interestrate.products.indices.LIBORIndex;
import net.finmath.montecarlo.process.EulerSchemeFromProcessModel;
import net.finmath.time.Schedule;
import net.finmath.time.ScheduleGenerator;
import net.finmath.time.TimeDiscretizationFromArray;
import net.finmath.time.businessdaycalendar.BusinessdayCalendar;
import net.finmath.time.businessdaycalendar.BusinessdayCalendarExcludingTARGETHolidays;

public class CVATest {

	private final static int numberOfPaths		= 1;
	
	public static void main(String[] args) throws Exception {
		
		
		
		 double Recovery 	= 0.40;	//Recovery Rate

		  double[] cdsSpreads = {300.0, 350.0, 400.0, 450.0, 500.0 } ; //{20.0, 25.0, 30.0, 35.0, 40.0}   // The (yearly) CDS spreads in bp {320.0, 57.0, 132.0 , 139.0 , 146.0, 150.0, 154.0}    ||  {300.0, 350.0, 400.0, 450.0, 500.0 }
		
		  AbstractLIBORMonteCarloProduct swap = getSwap();			
			LIBORModelMonteCarloSimulationModel simulationModel = getModel();
			
			//Now we test our CVA calculation
			
			CVA cvaGetter = new CVA(simulationModel, swap, Recovery, cdsSpreads);
			
			double cva = cvaGetter.getCVA();
			
			System.out.println(cva);
		  

	}

	
	
	
	
	/**
	 * get a pre-defined model
	 * @return
	 * @throws CalculationException 
	 */
	private static LIBORModelMonteCarloSimulationModel getModel() throws CalculationException {
		Measure measure = Measure.SPOT;
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
		double dt		= 0.05;

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
			AbstractIndex index = new LIBORIndex("forwardCurve", 0.0, 0.25);
			double fixedCoupon = 0.025;
			
			SwapLeg swapLegRec = new SwapLeg(legScheduleRec, notional, null, fixedCoupon /* spread */, false /* isNotionalExchanged */);
			SwapLeg swapLegPay = new SwapLeg(legSchedulePay, notional, index, 0.0 /* spread */, false /* isNotionalExchanged */);
		return new Swap(swapLegRec, swapLegPay);
	}
	
}
