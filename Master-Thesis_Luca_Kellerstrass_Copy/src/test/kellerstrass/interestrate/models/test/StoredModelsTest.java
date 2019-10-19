package kellerstrass.interestrate.models.test;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Locale;

import org.junit.Test;

import kellerstrass.interestrate.models.StoredModels;
import kellerstrass.useful.PaymentOffsetCode;
import net.finmath.exception.CalculationException;
import net.finmath.marketdata.model.curves.DiscountCurve;
import net.finmath.marketdata.model.curves.DiscountCurveInterpolation;
import net.finmath.marketdata.model.curves.ForwardCurve;
import net.finmath.marketdata.model.curves.ForwardCurveInterpolation;
import net.finmath.marketdata.model.curves.CurveInterpolation.ExtrapolationMethod;
import net.finmath.marketdata.model.curves.CurveInterpolation.InterpolationEntity;
import net.finmath.marketdata.model.curves.CurveInterpolation.InterpolationMethod;
import net.finmath.montecarlo.interestrate.LIBORModelMonteCarloSimulationModel;
import net.finmath.time.TimeDiscretizationFromArray;

public class StoredModelsTest {
	private final static int numberOfPaths		= 20000;
	private final static LocalDate referenceDate = LocalDate.of(2017, 6, 15);
	private static LIBORModelMonteCarloSimulationModel hullWhiteModelSimulation;
	private static LIBORModelMonteCarloSimulationModel liborMarketModelSimulation;

	private static DecimalFormat formatterMaturity	= new DecimalFormat("00.00", new DecimalFormatSymbols(Locale.ENGLISH));
	private static DecimalFormat formatterValue		= new DecimalFormat(" ##0.000%;-##0.000%", new DecimalFormatSymbols(Locale.ENGLISH));
	private static DecimalFormat formatterDeviation	= new DecimalFormat(" 0.00000E00;-0.00000E00", new DecimalFormatSymbols(Locale.ENGLISH));
	private final static NumberFormat formatter6 = new DecimalFormat("0.000000", new DecimalFormatSymbols(new Locale("en")));

	
	@Test
	public void testInitialization() throws Exception {
		
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
				
				// Create the forward curve (initial value of the LIBOR market model)
				ForwardCurve forwardCurve = ForwardCurveInterpolation.createForwardCurveFromForwards(
						"forwardCurve"								/* name of the curve */,
						referenceDate,
						"6M",
						ForwardCurveInterpolation.InterpolationEntityForward.FORWARD,
						"discount curve",
						null,
						new double[] {0.5 , 1.0 , 2.0 , 5.0 , 40.0}	/* fixings of the forward */,
						new double[] {0.05, 0.06, 0.07, 0.07, 0.08}	/* forwards */
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
				double dt		= 0.5;
				TimeDiscretizationFromArray simulationDiscretization = new TimeDiscretizationFromArray(0.0, (int) (lastTime / dt), dt);

				
	// Initialization:
				StoredModels modelStore1 = new StoredModels(liborPeriodDiscretization, simulationDiscretization, discountCurve, forwardCurve, referenceDate);
				StoredModels modelStore2 = new StoredModels(liborPeriodDiscretization, simulationDiscretization, null, forwardCurve, referenceDate);
				StoredModels modelStore3 = new StoredModels(liborPeriodDiscretization, simulationDiscretization, discountCurve, "6M", referenceDate);
	}
	

	@Test
	public void testLMM_SameReturn_HW() throws Exception {
		
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
		double dt		= 0.5;
		TimeDiscretizationFromArray simulationDiscretization = new TimeDiscretizationFromArray(0.0, (int) (lastTime / dt), dt);
		
		// Initialization:
		StoredModels modelStore = new StoredModels(liborPeriodDiscretization, simulationDiscretization, discountCurve, paymentOffsetCode, referenceDate);
		
		double shortRateVolatility = 0.005;
		double shortRateMeanreversion = 0.1;
		hullWhiteModelSimulation = modelStore.getLiborHullWhiteModellCorrespondingToLMM(numberOfPaths, shortRateVolatility, shortRateMeanreversion);
		liborMarketModelSimulation = modelStore.getLiborMarketModellCorrespondingToHullWhite(numberOfPaths, shortRateVolatility, shortRateMeanreversion);
	
		//Test them against each other
		CompareModels.compare(hullWhiteModelSimulation , liborMarketModelSimulation);
	
	}
	

}
