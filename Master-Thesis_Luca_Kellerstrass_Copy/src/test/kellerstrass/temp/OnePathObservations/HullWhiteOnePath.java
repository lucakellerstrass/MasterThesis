package kellerstrass.temp.OnePathObservations;

import java.time.LocalDate;
import java.time.Month;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;

import net.finmath.exception.CalculationException;
import net.finmath.marketdata.model.AnalyticModel;
import net.finmath.marketdata.model.AnalyticModelFromCurvesAndVols;
import net.finmath.marketdata.model.curves.Curve;
import net.finmath.marketdata.model.curves.DiscountCurve;
import net.finmath.marketdata.model.curves.DiscountCurveInterpolation;
import net.finmath.marketdata.model.curves.ForwardCurve;
import net.finmath.marketdata.model.curves.ForwardCurveFromDiscountCurve;
import net.finmath.marketdata.model.curves.ForwardCurveInterpolation;
import net.finmath.marketdata.model.curves.CurveInterpolation.ExtrapolationMethod;
import net.finmath.marketdata.model.curves.CurveInterpolation.InterpolationEntity;
import net.finmath.marketdata.model.curves.CurveInterpolation.InterpolationMethod;
import net.finmath.montecarlo.BrownianMotion;
import net.finmath.montecarlo.RandomVariableFromDoubleArray;
import net.finmath.montecarlo.interestrate.LIBORModel;
import net.finmath.montecarlo.interestrate.LIBORModelMonteCarloSimulationModel;
import net.finmath.montecarlo.interestrate.LIBORMonteCarloSimulationFromLIBORModel;
import net.finmath.montecarlo.interestrate.models.HullWhiteModel;
import net.finmath.montecarlo.interestrate.models.covariance.ShortRateVolatilityModel;
import net.finmath.montecarlo.interestrate.models.covariance.ShortRateVolatilityModelAsGiven;
import net.finmath.montecarlo.interestrate.products.Swap;
import net.finmath.montecarlo.interestrate.products.SwapLeg;
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

public class HullWhiteOnePath {

	public static void main(String[] args) throws CalculationException {

		// initialize a simple Hull White Model
		LIBORModelMonteCarloSimulationModel hullWhiteModelSimulation;
		final int numberOfPaths = 1;
		final double shortRateVolatility = 0.005;
		final double shortRateMeanreversion = 0.1;
		final LocalDate referenceDate = LocalDate.of(2019, 10, 24);

		// (1)
//		System.out.println("Libor Time discretization");
		/*
		 * Create the libor tenor structure and the initial values
		 */
		double liborPeriodLength = 0.5;
		double liborRateTimeHorzion = 20.0;
		TimeDiscretizationFromArray liborPeriodDiscretization = new TimeDiscretizationFromArray(0.0,
				(int) (liborRateTimeHorzion / liborPeriodLength), liborPeriodLength);

//		for(int i=0 ; i< liborPeriodDiscretization.getNumberOfTimes();i++) {
//		System.out.println("libortime "+ i +" = "+ liborPeriodDiscretization.getTime(i));
//		}

//		System.out.println("\n");

		// (2) Test the discount curve
//		System.out.println("Test the discount curve");
		// Create the forward curve (initial value of the LIBOR market model)
		DiscountCurve discountCurve = DiscountCurveInterpolation.createDiscountCurveFromZeroRates("discount curve",
				referenceDate, new double[] { 0.5, 40.00 } /* zero rate end points */,
				new double[] { 0.03, 0.04 } /* zeros */, new boolean[] { false, false }, InterpolationMethod.LINEAR,
				ExtrapolationMethod.CONSTANT, InterpolationEntity.LOG_OF_VALUE_PER_TIME);

		AnalyticModel curveModel = new AnalyticModelFromCurvesAndVols(new Curve[] { discountCurve });

//				for(int i=0 ; i< liborPeriodDiscretization.getNumberOfTimes();i++) {
//					System.out.println(
//							//"libortime index: "+ i +
//							"\t time:   \t"+ liborPeriodDiscretization.getTime(i)+
//							"\t zero: \t"+curveModel.getDiscountCurve("discount curve").getDiscountFactor(liborPeriodDiscretization.getTime(i)) );
//					}	

		// (3) Test the forward curve 2
//				System.out.println("Test the forward curve 2 curve");

		// Create the forward curve (initial value of the LIBOR market model)
		ForwardCurve forwardCurve = ForwardCurveInterpolation.createForwardCurveFromForwards(
				"forwardCurve" /* name of the curve */, referenceDate, "6M",
				ForwardCurveInterpolation.InterpolationEntityForward.FORWARD, "discount curve", curveModel,
				new double[] { 0.5, 1.0, 2.0, 5.0, 40.0 } /* fixings of the forward */,
				new double[] { 0.05, 0.06, 0.07, 0.07, 0.08 } /* forwards */
		);
//				for(int i=0 ; i< liborPeriodDiscretization.getNumberOfTimes();i++) {
//				System.out.println(
//						//"libortime index: "+ i +
//						"\t time:   \t"+ liborPeriodDiscretization.getTime(i)+
//						"\t forwards: \t"+forwardCurve.getValue(liborPeriodDiscretization.getTime(i)) );
//				}	

		// (4) Test the forward curve from discount curve
//		System.out.println("Test the forward curve from discount curve" + "\n");

		// Create the discount curve
		ForwardCurve forwardCurve2 = new ForwardCurveFromDiscountCurve(discountCurve.getName(), referenceDate, "6M");

	
		curveModel = new AnalyticModelFromCurvesAndVols(new Curve[] { discountCurve, forwardCurve2 });

//		for (int i = 0; i < liborPeriodDiscretization.getNumberOfTimes(); i++) {
//			System.out.println(
//					// "libortime index: "+ i +
//					"\t time:   \t" + liborPeriodDiscretization.getTime(i) + "\t forwards: \t"
//							+ forwardCurve2.getForward(curveModel, fixingTime)));
//		}

		/*
		 * Create a simulation time discretization
		 */
		double lastTime = 20.0;
		double dt = 0.5;

		TimeDiscretizationFromArray timeDiscretizationFromArray = new TimeDiscretizationFromArray(0.0,
				(int) (lastTime / dt), dt);

		/*
		 * Create corresponding Hull White model
		 */

		/*
		 * Create a volatility model: Hull white with constant coefficients (non time
		 * dep.).
		 */
		ShortRateVolatilityModel volatilityModel = new ShortRateVolatilityModelAsGiven(
				new TimeDiscretizationFromArray(0.0), new double[] { shortRateVolatility } /* volatility */,
				new double[] { shortRateMeanreversion } /* meanReversion */);

		Map<String, Object> properties = new HashMap<>();
		properties.put("isInterpolateDiscountFactorsOnLiborPeriodDiscretization", false);

		// TODO Left hand side type should be TermStructureModel once interface are
		// refactored
		LIBORModel hullWhiteModel = new HullWhiteModel(liborPeriodDiscretization, curveModel, forwardCurve2,
				discountCurve, volatilityModel, properties);

		// (5) Brownian motion test
		BrownianMotion brownianMotion = new net.finmath.montecarlo.BrownianMotionLazyInit(timeDiscretizationFromArray,
				2 /* numberOfFactors */, numberOfPaths, 3141 /* seed */);

//			for (int i = 0; i < timeDiscretizationFromArray.getNumberOfTimes()-1; i++) {
//			System.out.println(
//					// "libortime index: "+ i +
//					"\t time:   \t" + timeDiscretizationFromArray.getTime(i) + "\t brownian incr.: \t"
//						//	+ brownianMotion.getBrownianIncrement( timeDiscretizationFromArray.getTime(i), 0).get(0));
//		                	+ brownianMotion.getBrownianIncrement(i, 0).get(0));
//			}

		EulerSchemeFromProcessModel process = new EulerSchemeFromProcessModel(brownianMotion,
				EulerSchemeFromProcessModel.Scheme.EULER);

		hullWhiteModelSimulation = new LIBORMonteCarloSimulationFromLIBORModel(hullWhiteModel, process);

//		double value = hullWhiteModelSimulation.getLIBOR(0, 1).get(0);
//		System.out.println("LIBOR(0, 1)= "+ value);
//		
//		for (int i = 0; i < timeDiscretizationFromArray.getNumberOfTimes(); i++) {
//		System.out.println(
//				// "libortime index: "+ i +
//				"\t time:   \t" + timeDiscretizationFromArray.getTime(i) + "\t F(0,"+0.5*i+")= \t"
//	                	+ hullWhiteModelSimulation.getLIBOR(0, 0, 0.5*i).get(0));
//		}

		/*
		 * Create a PAYER swap (receive float, pay fixed)
		 */
		Schedule fixedLegSchedulePay = ScheduleGenerator.createScheduleFromConventions(
				LocalDate.of(2019, Month.OCTOBER, 24) /* referenceDate */,
				LocalDate.of(2019, Month.OCTOBER, 24) /* startDate */,
				LocalDate.of(2029, Month.OCTOBER, 24) /* maturityDate */,
				ScheduleGenerator.Frequency.ANNUAL /* frequency */,
				ScheduleGenerator.DaycountConvention.ACT_365 /* daycountConvention */,
				ScheduleGenerator.ShortPeriodConvention.FIRST /* shortPeriodConvention */,
				BusinessdayCalendar.DateRollConvention.FOLLOWING /* dateRollConvention */,
				new BusinessdayCalendarExcludingTARGETHolidays() /* businessdayCalendar */, 0 /* fixingOffsetDays */,
				0 /* paymentOffsetDays */);

		Schedule floatLegScheduleRec = ScheduleGenerator.createScheduleFromConventions(
				LocalDate.of(2019, Month.OCTOBER, 24) /* referenceDate */,
				LocalDate.of(2019, Month.OCTOBER, 24) /* startDate */,
				LocalDate.of(2029, Month.OCTOBER, 24) /* maturityDate */,
				ScheduleGenerator.Frequency.SEMIANNUAL /* frequency */,
				ScheduleGenerator.DaycountConvention.ACT_365/* daycountConvention */,
				ScheduleGenerator.ShortPeriodConvention.FIRST /* shortPeriodConvention */,
				BusinessdayCalendar.DateRollConvention.FOLLOWING /* dateRollConvention */,
				new BusinessdayCalendarExcludingTARGETHolidays() /* businessdayCalendar */, 0 /* fixingOffsetDays */,
				0 /* paymentOffsetDays */);
		AbstractNotional notional = new Notional(1.0);
		AbstractIndex index = new LIBORIndex(null /* "forwardCurve" */, 0.0, 0.5);
		double fixedCoupon = 0.5086; // old: 0.00547; swap rate: 0.5086843222705326

		SwapLeg swapLegRec = new SwapLeg(floatLegScheduleRec, notional, index, 0.0 /* spread */,
				false /* isNotionalExchanged */);

		SwapLeg swapLegPay = new SwapLeg(fixedLegSchedulePay, notional, null, fixedCoupon /* spread */,
				false /* isNotionalExchanged */);

		Swap swap = new Swap(swapLegRec, swapLegPay);

		// Test the fixed Leg
//		System.out.println("Test the fixed Leg");
//		System.out.println("Period    \t   fixing   \t   PeriodEnd   \t   PeriodLength  \t discount bond");
//		for (int i = 0; i < fixedLegSchedulePay.getNumberOfPeriods(); i++) {
//
//			System.out.println(i + "\t" + 
//			fixedLegSchedulePay.getFixing(i) + "\t" + 
//					fixedLegSchedulePay.getPeriodEnd(i)
//					+ "\t" + fixedLegSchedulePay.getPeriodLength(i)
//					+ "\t" + discountCurve.getDiscountFactor(fixedLegSchedulePay.getPeriodEnd(i))
//					);
//		}
//		System.out.println("Value=" + swapLegPay.getValue(0, hullWhiteModelSimulation).get(0));

		
		
		
		
		// Test the floating Leg
//		
//		System.out.println("Test the floating Leg");
//		System.out.println("Period    \t   fixing   \t   PeriodEnd   \t   PeriodLength  \t discount bond   \t  L(0,T_i, T_i+1");
//		for (int i = 0; i < floatLegScheduleRec.getNumberOfPeriods(); i++) {
//
//			System.out.println(i + "\t" + 
//					floatLegScheduleRec.getFixing(i) + "\t" + 
//					floatLegScheduleRec.getPeriodEnd(i)
//					+ "\t" + floatLegScheduleRec.getPeriodLength(i)
//					+ "\t" + discountCurve.getDiscountFactor(floatLegScheduleRec.getPeriodEnd(i))
//					+ "\t" + hullWhiteModelSimulation.getLIBOR(0, floatLegScheduleRec.getFixing(i), floatLegScheduleRec.getPeriodLength(i)).get(0)
//					);
//		}
//        System.out.println("Value= \t" + swapLegRec.getValue(0, hullWhiteModelSimulation).get(0));
//		
		
    
		
		// Test Payer and Receiver Leg
	
		
		

		// System.out.println("valueRec= " + valueRec);

		// Par swap rate
		double swaprate = net.finmath.marketdata.products.Swap.getForwardSwapRate(fixedLegSchedulePay,
				floatLegScheduleRec, forwardCurve2, curveModel);

		System.out.println("swaprate= \t" + swaprate);

		
		System.out.println("observationDate \t "+ "Libor  \t"+ "float value \t " + "fixed value \t " + "swap value \t " );
		for(double observationDate : hullWhiteModelSimulation.getTimeDiscretization()) {

//			if(observationDate == 0) {
//				continue;
//			}
			

			
			System.out.println(observationDate + "\t"+
					swapLegRec.getValue(observationDate, hullWhiteModelSimulation).get(0) + "\t"+
					hullWhiteModelSimulation.getLIBOR(0, observationDate, observationDate+0.5).get(0) + "\t"+
					swapLegPay.getValue(observationDate, hullWhiteModelSimulation).get(0) + "\t"+
					swap.getValue(observationDate, hullWhiteModelSimulation).get(0) + "\t");
			
			
			
			

		}
		
		
		
		
		
	}

}
