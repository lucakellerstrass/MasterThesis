package kellerstrass.interestrate.models.test;

import java.time.LocalDate;

import org.junit.Test;

import kellerstrass.interestrate.models.StoredModels;
import net.finmath.marketdata.model.curves.DiscountCurve;
import net.finmath.marketdata.model.curves.DiscountCurveInterpolation;
import net.finmath.marketdata.model.curves.CurveInterpolation.ExtrapolationMethod;
import net.finmath.marketdata.model.curves.CurveInterpolation.InterpolationEntity;
import net.finmath.marketdata.model.curves.CurveInterpolation.InterpolationMethod;
import net.finmath.montecarlo.interestrate.LIBORModelMonteCarloSimulationModel;
import net.finmath.time.TimeDiscretizationFromArray;

public class HullWhiteTest {

	private final static int numberOfPaths		= 20000;
	private final static LocalDate referenceDate = LocalDate.of(2019, 10, 22);
	private static LIBORModelMonteCarloSimulationModel hullWhiteModelSimulation;
	
	
	@Test
	public  void test() throws Exception {
		String paymentOffsetCode  = "6M";          //The Libor paymentOffsetCode
		double  discretizationPointPerPaymentIntervalll = 2.0;
		
		
		
		// Create the discount curve
		DiscountCurve discountCurve = DiscountCurveInterpolation.createDiscountCurveFromZeroRates(
				"discount curve",
				referenceDate,
				new double[] {0.5, 40.00}	/* zero rate end points */,
				new double[] {0.03,  0.04}	/* zeros */,
				new boolean[] {false,  false},
				InterpolationMethod.LINEAR,
				ExtrapolationMethod.CONSTANT,
				InterpolationEntity.LOG_OF_VALUE_PER_TIME
				);
		
		/*
		 * Create the libor tenor structure and the initial values
		 */
		double liborPeriodLength	= 0.5;
		double liborRateTimeHorzion	= 20.0;
		TimeDiscretizationFromArray liborPeriodDiscretization = new TimeDiscretizationFromArray(0.0, (int) (liborRateTimeHorzion / liborPeriodLength), liborPeriodLength);

		/*
		 * Create a simulation time discretization
		 */
		double lastTime	= 20.0;
		double dt		= 0.25;
		TimeDiscretizationFromArray simulationDiscretization = new TimeDiscretizationFromArray(0.0, (int) (lastTime / dt), dt);
		
		// Initialization:
		StoredModels modelStore = new StoredModels(liborPeriodDiscretization, simulationDiscretization, discountCurve, paymentOffsetCode, referenceDate);
		
		double shortRateVolatility = 0.005;
		double shortRateMeanreversion = 0.1;
		hullWhiteModelSimulation = modelStore.getLiborHullWhiteModellCorrespondingToLMM(numberOfPaths, shortRateVolatility, shortRateMeanreversion);
		
		System.out.println("We test some parts of the Hull White model");
		System.out.println("Time \t LiborPeriod \t L(0, T_j) ");

		for(double time : liborPeriodDiscretization) {
			if(time == liborPeriodDiscretization.getTime(liborPeriodDiscretization.getNumberOfTimes()-1)) {
				continue;
			}
			
			System.out.print(time + "\t");
			int LiborPeriodIndex = hullWhiteModelSimulation.getLiborPeriodIndex(time);
			System.out.print(LiborPeriodIndex + "\t");
			
			
			
			System.out.print(hullWhiteModelSimulation.getLIBOR(0, LiborPeriodIndex).getAverage() + "\t");
			
			System.out.println("");
		}
	}

}
