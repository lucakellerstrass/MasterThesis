package kellerstrass.interestrate.models.test;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;

import net.finmath.exception.CalculationException;
import net.finmath.functions.AnalyticFormulas;
import net.finmath.marketdata.model.AnalyticModel;
import net.finmath.marketdata.model.curves.DiscountCurve;
import net.finmath.marketdata.model.curves.DiscountCurveFromForwardCurve;
import net.finmath.marketdata.model.curves.ForwardCurve;
import net.finmath.montecarlo.interestrate.LIBORModelMonteCarloSimulationModel;
import net.finmath.montecarlo.interestrate.models.HullWhiteModel;
import net.finmath.montecarlo.interestrate.models.HullWhiteModelWithDirectSimulation;
import net.finmath.montecarlo.interestrate.models.HullWhiteModelWithShiftExtension;
import net.finmath.montecarlo.interestrate.products.AbstractLIBORMonteCarloProduct;
import net.finmath.montecarlo.interestrate.products.BermudanSwaption;
import net.finmath.montecarlo.interestrate.products.Bond;
import net.finmath.montecarlo.interestrate.products.Caplet;
import net.finmath.montecarlo.interestrate.products.SimpleZeroSwap;
import net.finmath.montecarlo.interestrate.products.Swap;
import net.finmath.montecarlo.interestrate.products.SwapLeg;
import net.finmath.montecarlo.interestrate.products.Swaption;
import net.finmath.montecarlo.interestrate.products.SwaptionAnalyticApproximation;
import net.finmath.montecarlo.interestrate.products.TermStructureMonteCarloProduct;
import net.finmath.montecarlo.interestrate.products.components.AbstractNotional;
import net.finmath.montecarlo.interestrate.products.components.Notional;
import net.finmath.montecarlo.interestrate.products.components.Numeraire;
import net.finmath.montecarlo.interestrate.products.components.Option;
import net.finmath.montecarlo.interestrate.products.indices.AbstractIndex;
import net.finmath.montecarlo.interestrate.products.indices.CappedFlooredIndex;
import net.finmath.montecarlo.interestrate.products.indices.ConstantMaturitySwaprate;
import net.finmath.montecarlo.interestrate.products.indices.FixedCoupon;
import net.finmath.montecarlo.interestrate.products.indices.LIBORIndex;
import net.finmath.montecarlo.interestrate.products.indices.LaggedIndex;
import net.finmath.montecarlo.interestrate.products.indices.LinearCombinationIndex;
import net.finmath.stochastic.RandomVariable;
import net.finmath.time.Schedule;
import net.finmath.time.ScheduleGenerator;
import net.finmath.time.SchedulePrototype;
import net.finmath.time.TimeDiscretizationFromArray;
import net.finmath.time.ScheduleGenerator.DaycountConvention;
import net.finmath.time.ScheduleGenerator.Frequency;
import net.finmath.time.ScheduleGenerator.ShortPeriodConvention;
import net.finmath.time.businessdaycalendar.BusinessdayCalendar;
import net.finmath.time.businessdaycalendar.BusinessdayCalendarExcludingTARGETHolidays;

/**
 * This class is a variante of the HullWhiteModelTest in net.finmath.montecarlo.interestrate
 * by Christian Fries
 * @author lucak
 *
 */
public class CompareModels {
	private static DecimalFormat formatterMaturity	= new DecimalFormat("00.00", new DecimalFormatSymbols(Locale.ENGLISH));
	private static DecimalFormat formatterValue		= new DecimalFormat(" ##0.000%;-##0.000%", new DecimalFormatSymbols(Locale.ENGLISH));
	private static DecimalFormat formatterDeviation	= new DecimalFormat(" 0.00000E00;-0.00000E00", new DecimalFormatSymbols(Locale.ENGLISH));

	private static LIBORModelMonteCarloSimulationModel SimulationModel1;
	private static LIBORModelMonteCarloSimulationModel SimulationModel2;
	
	private final static LocalDate referenceDate = LocalDate.of(2017, 6, 15);
	
	@Test
	public static void compare(LIBORModelMonteCarloSimulationModel SimulationModel11, LIBORModelMonteCarloSimulationModel SimulationModel22 ) throws CalculationException {
		SimulationModel1 = SimulationModel11;
		SimulationModel2 = SimulationModel22;
        
		System.out.println("models to compare: :");
		System.out.println("\t" + SimulationModel1.getModel().getClass().getName() + "  (M1)");
		System.out.println("\t" + SimulationModel2.getModel().getClass().getName() + "  (M2)");
		
		testBond();
		testSwap();
		testCaplet();
		testSwaption();
		testBermudanSwaption();
	                //testCapletSmile();   not implemented. Only useful for M1 = Hull-white
		testZeroCMSSwap();
		testLIBORInArrearsFloatLeg();
		testPutOnMoneyMarketAccount();
	}
    
	@Test
	private static void testBond() throws CalculationException {
		/*
		 * Value a bond
		 */

		System.out.println("Bond prices:\n");
		System.out.println("Maturity       Simulation (M1)   Simulation (M2) Analytic         Deviation (M1-M2)    Deviation (M1-Analytic)");

		long startMillis	= System.currentTimeMillis();

		double maxAbsDeviation = 0.0;
		for (int maturityIndex = 0; maturityIndex <= SimulationModel1.getNumberOfLibors(); maturityIndex++) {
			double maturity = SimulationModel1.getLiborPeriod(maturityIndex);
			System.out.print(formatterMaturity.format(maturity) + "          ");

			// Create a bond
			Bond bond = new Bond(maturity);

			// Value with Hull-White Model Monte Carlo
			double valueSimulationM1 = bond.getValue(SimulationModel1);
			System.out.print(formatterValue.format(valueSimulationM1) + "          ");

			// Value with LIBOR Market Model Monte Carlo
			double valueSimulationM2 = bond.getValue(SimulationModel2);
			System.out.print(formatterValue.format(valueSimulationM2) + "          ");

			// Bond price analytic
			DiscountCurve discountCurve = SimulationModel1.getModel().getDiscountCurve();
			if(discountCurve == null) {
				discountCurve = new DiscountCurveFromForwardCurve(SimulationModel1.getModel().getForwardRateCurve());
			}
			double valueAnalytic = discountCurve.getDiscountFactor(maturity);
			System.out.print(formatterValue.format(valueAnalytic) + "          ");

			// Absolute deviation
			double deviationHWLMM = (valueSimulationM1 - valueSimulationM2);
			System.out.print(formatterDeviation.format(deviationHWLMM) + "          ");

			// Absolute deviation
			double deviationHWAnalytic = (valueSimulationM1 - valueAnalytic);
			System.out.print(formatterDeviation.format(deviationHWAnalytic) + "          ");

			System.out.println();

			maxAbsDeviation = Math.max(maxAbsDeviation, Math.abs(deviationHWAnalytic));
		}

		long endMillis		= System.currentTimeMillis();

		System.out.println("Maximum abs deviation  : " + formatterDeviation.format(maxAbsDeviation));
		System.out.println("Calculation time (sec) : " + ((endMillis-startMillis) / 1000.0));

		System.out.println("__________________________________________________________________________________________\n");


		// jUnit assertion: condition under which we consider this test successful
		Assert.assertTrue(maxAbsDeviation < 5E-03);
		
	}

	@Test
	public static void testSwap() throws CalculationException {
		/*
		 * Value a swap
		 */

		System.out.println("Par-Swap prices:\n");
		System.out.println("Swap                             \tValue (M1)       Value (M2)     Deviation");

		long startMillis	= System.currentTimeMillis();


		/*
		 * Set up the derivative
		 */
		//Set properties of input swaptions
		SchedulePrototype fixMetaSchedule = new SchedulePrototype(
				Frequency.ANNUAL,
				DaycountConvention.E30_360,
				ShortPeriodConvention.FIRST,
				BusinessdayCalendar.DateRollConvention.MODIFIED_FOLLOWING,
				new BusinessdayCalendarExcludingTARGETHolidays(),
				0,
				0,
				false);

		SchedulePrototype floatMetaSchedule = new SchedulePrototype(
				Frequency.SEMIANNUAL,
				DaycountConvention.ACT_360,
				ShortPeriodConvention.FIRST,
				BusinessdayCalendar.DateRollConvention.MODIFIED_FOLLOWING,
				new BusinessdayCalendarExcludingTARGETHolidays(),
				0,
				0,
				false);

		double maxAbsDeviation = 0.0;
		double maxMaturity = SimulationModel1.getLiborPeriod(SimulationModel1.getNumberOfLibors()-1) - 10.0;
		for (int maturityIndex = 1; maturityIndex <= maxMaturity*12; maturityIndex++) {

			LocalDate startDate = referenceDate.plusMonths(maturityIndex);
			LocalDate endDate = startDate.plusYears(5);

			Schedule fixLegSchedule = fixMetaSchedule.generateSchedule(referenceDate, startDate, endDate);
			Schedule floatLegSchedule = floatMetaSchedule.generateSchedule(referenceDate, startDate, endDate);

			ForwardCurve forwardCurve = SimulationModel2.getModel().getForwardRateCurve();
			//hullWhiteModelSimulation.getModel().getForwardRateCurve();
			AnalyticModel model = SimulationModel1.getModel().getAnalyticModel();

			// Par swap rate
			double swaprate = net.finmath.marketdata.products.Swap.getForwardSwapRate(fixLegSchedule, floatLegSchedule, forwardCurve, model);

			//Create libor index for Monte-Carlo swap
			AbstractIndex libor = new LIBORIndex(forwardCurve.getName(), "EUR", "6M", floatMetaSchedule.getBusinessdayCalendar(), floatMetaSchedule.getDateRollConvention());

			//Create legs
			TermStructureMonteCarloProduct floatLeg = new SwapLeg(floatLegSchedule, new Notional(1.0), libor, 0, false);
			double FloatingLegValue = floatLeg.getValue(SimulationModel1);
			
			
			TermStructureMonteCarloProduct fixLeg = new SwapLeg(fixLegSchedule, new Notional(1.0), null, swaprate, false);

			//Create swap
			AbstractLIBORMonteCarloProduct swap = new Swap(floatLeg, fixLeg);

			System.out.print("(" + startDate + "," + endDate + "," + floatMetaSchedule.getFrequency().name() + ")" + "\t");

			// Value with Model 1
			double valueSimulationM1 = swap.getValue(SimulationModel1);
			System.out.print(formatterValue.format(valueSimulationM1) + "          ");

			// Value with Model 2
			double valueSimulationM2 = swap.getValue(SimulationModel2);
			System.out.print(formatterValue.format(valueSimulationM2) + "          ");

			// Absolute deviation
			double deviationHWLMM = (valueSimulationM1 - valueSimulationM2);
			System.out.print(formatterDeviation.format(deviationHWLMM) + "          ");

			System.out.println();

			maxAbsDeviation = Math.max(maxAbsDeviation, Math.abs(valueSimulationM1));
		}

		long endMillis		= System.currentTimeMillis();

		System.out.println("Maximum abs deviation  : " + formatterDeviation.format(maxAbsDeviation));
		System.out.println("Calculation time (sec) : " + ((endMillis-startMillis) / 1000.0));
		System.out.println("__________________________________________________________________________________________\n");

		/*
		 * jUnit assertion: condition under which we consider this test successful
		 * The swap should be at par (close to zero)
		 */
		Assert.assertEquals(0.0, maxAbsDeviation, 1.5E-3);
	}

	@Test
	public static void testCaplet() throws CalculationException {
		/*
		 * Value a caplet
		 */
		System.out.println("Caplet prices:\n");
		System.out.println("Maturity       Simulation (M1)   Simulation (M2) Analytic         Deviation (M1-M2)    Deviation (M1-Analytic)");

		long startMillis	= System.currentTimeMillis();

		double maxAbsDeviation = 0.0;
		for (int maturityIndex = 1; maturityIndex <= SimulationModel1.getNumberOfLibors() - 10; maturityIndex++) {

			double optionMaturity = SimulationModel1.getLiborPeriod(maturityIndex);
			System.out.print(formatterMaturity.format(optionMaturity) + "          ");

			double periodStart	= SimulationModel1.getLiborPeriod(maturityIndex);
			double periodEnd	= SimulationModel1.getLiborPeriod(maturityIndex+1);
			double periodLength	= periodEnd-periodStart;
			double daycountFraction = periodEnd-periodStart;

			String		tenorCode;
			if(periodLength == 0.5) {
				tenorCode = "6M";
			} else if(periodLength == 1.0) {
				tenorCode = "1Y";
			} else {
				throw new IllegalArgumentException("Unsupported period length.");
			}

			double forward			= getParSwaprate(SimulationModel1, new double[] { periodStart , periodEnd}, tenorCode);
			double discountFactor	= getSwapAnnuity(SimulationModel1, new double[] { periodStart , periodEnd}) / periodLength;

			double strike = forward + 0.01;

			// Create a caplet
			Caplet caplet = new Caplet(optionMaturity, periodLength, strike, daycountFraction, false /* isFloorlet */, Caplet.ValueUnit.VALUE);

			// Value with Hull-White Model Monte Carlo
			double valueSimulationM1 = caplet.getValue(SimulationModel1);
			valueSimulationM1 = AnalyticFormulas.bachelierOptionImpliedVolatility(forward, optionMaturity, strike, discountFactor * periodLength /* payoffUnit */, valueSimulationM1);
			System.out.print(formatterValue.format(valueSimulationM1) + "          ");

			// Value with LIBOR Market Model Monte Carlo
			double valueSimulationM2 = caplet.getValue(SimulationModel2);
			valueSimulationM2 = AnalyticFormulas.bachelierOptionImpliedVolatility(forward, optionMaturity, strike, discountFactor * periodLength /* payoffUnit */, valueSimulationM2);
			System.out.print(formatterValue.format(valueSimulationM2) + "          ");

			// Value with analytic formula
			double forwardBondVolatility = Double.NaN;
			if(SimulationModel1.getModel() instanceof HullWhiteModel) {
				forwardBondVolatility = Math.sqrt(((HullWhiteModel)(SimulationModel1.getModel())).getIntegratedBondSquaredVolatility(optionMaturity, optionMaturity+periodLength).doubleValue()/optionMaturity);
			}
			else if(SimulationModel1.getModel() instanceof HullWhiteModelWithDirectSimulation) {
				forwardBondVolatility = Math.sqrt(((HullWhiteModelWithDirectSimulation)(SimulationModel1.getModel())).getIntegratedBondSquaredVolatility(optionMaturity, optionMaturity+periodLength)/optionMaturity);
			}
			else if(SimulationModel1.getModel() instanceof HullWhiteModelWithShiftExtension) {
				forwardBondVolatility = Math.sqrt(((HullWhiteModelWithShiftExtension)(SimulationModel1.getModel())).getIntegratedBondSquaredVolatility(optionMaturity, optionMaturity+periodLength)/optionMaturity);
			}

			double bondForward = (1.0+forward*periodLength);
			double bondStrike = (1.0+strike*periodLength);

			double zeroBondPut = net.finmath.functions.AnalyticFormulas.blackModelCapletValue(bondForward, forwardBondVolatility, optionMaturity, bondStrike, periodLength, discountFactor);

			double valueAnalytic = zeroBondPut / bondStrike / periodLength;
			valueAnalytic = AnalyticFormulas.bachelierOptionImpliedVolatility(forward, optionMaturity, strike, discountFactor * periodLength /* payoffUnit */, valueAnalytic);
			System.out.print(formatterValue.format(valueAnalytic) + "          ");

			// Absolute deviation
			double deviationHWLMM = (valueSimulationM1 - valueSimulationM2);
			System.out.print(formatterDeviation.format(deviationHWLMM) + "          ");

			// Absolute deviation
			double deviationHWAnalytic = (valueSimulationM1 - valueAnalytic);
			System.out.print(formatterDeviation.format(deviationHWAnalytic) + "          ");

			System.out.println();

			maxAbsDeviation = Math.max(maxAbsDeviation, Math.abs(deviationHWLMM));
			if(!Double.isNaN(valueAnalytic)) {
				maxAbsDeviation = Math.max(maxAbsDeviation, Math.abs(deviationHWAnalytic));
			}
		}

		long endMillis		= System.currentTimeMillis();

		System.out.println("Maximum abs deviation: " + formatterDeviation.format(maxAbsDeviation));
		System.out.println("Calculation time (sec) : " + ((endMillis-startMillis) / 1000.0));
		System.out.println("__________________________________________________________________________________________\n");

		/*
		 * jUnit assertion: condition under which we consider this test successful
		 */
		Assert.assertTrue(Math.abs(maxAbsDeviation) < 1E-3);
	}

	@Test
	public static void testSwaption() throws CalculationException {
		/*
		 * Value a swaption
		 */
		System.out.println("Swaption prices:\n");
		System.out.println("Maturity      Simulation (M1)  Simulation (M2)  Analytic         Deviation          ");

		long startMillis	= System.currentTimeMillis();

		double maxAbsDeviation = 0.0;
		for (int maturityIndex = 1; maturityIndex <= SimulationModel1.getNumberOfLibors() - 10; maturityIndex++) {

			double exerciseDate = SimulationModel1.getLiborPeriod(maturityIndex);
			System.out.print(formatterMaturity.format(exerciseDate) + "          ");

			int numberOfPeriods = 5;

			// Create a swaption

			double[] fixingDates = new double[numberOfPeriods];
			double[] paymentDates = new double[numberOfPeriods];
			double[] swapTenor = new double[numberOfPeriods + 1];
			double swapPeriodLength = 0.5;
			String tenorCode = "6M";

			for (int periodStartIndex = 0; periodStartIndex < numberOfPeriods; periodStartIndex++) {
				fixingDates[periodStartIndex] = exerciseDate + periodStartIndex * swapPeriodLength;
				paymentDates[periodStartIndex] = exerciseDate + (periodStartIndex + 1) * swapPeriodLength;
				swapTenor[periodStartIndex] = exerciseDate + periodStartIndex * swapPeriodLength;
			}
			swapTenor[numberOfPeriods] = exerciseDate + numberOfPeriods * swapPeriodLength;

			// Swaptions swap rate
			double swaprate = getParSwaprate(SimulationModel1, swapTenor, tenorCode);
			double swapAnnuity = getSwapAnnuity(SimulationModel1, swapTenor);

			// Set swap rates for each period
			double[] swaprates = new double[numberOfPeriods];
			for (int periodStartIndex = 0; periodStartIndex < numberOfPeriods; periodStartIndex++) {
				swaprates[periodStartIndex] = swaprate;
			}

			// Create a swaption
			Swaption swaptionMonteCarlo	= new Swaption(exerciseDate, fixingDates, paymentDates, swaprates);

			// Value with Hull-White Model Monte Carlo
			double valueSimulationM1 = swaptionMonteCarlo.getValue(SimulationModel1);
			System.out.print(formatterValue.format(valueSimulationM1) + "          ");

			// Value with LIBOR Market Model Monte Carlo
			double valueSimulationM2 = swaptionMonteCarlo.getValue(SimulationModel2);
			System.out.print(formatterValue.format(valueSimulationM2) + "          ");

			// Value with analytic formula (approximate, using Bachelier formula)
			SwaptionAnalyticApproximation swaptionAnalytic = new SwaptionAnalyticApproximation(swaprate, swapTenor, SwaptionAnalyticApproximation.ValueUnit.VOLATILITYLOGNORMAL);
			double volatilityAnalytic = swaptionAnalytic.getValue(SimulationModel2);
			double valueAnalytic = AnalyticFormulas.bachelierOptionValue(swaprate, volatilityAnalytic, exerciseDate, swaprate, swapAnnuity);
			System.out.print(formatterValue.format(valueAnalytic) + "          ");

			// Absolute deviation
			double deviationM1M2 = (valueSimulationM1 - valueSimulationM2);
			System.out.print(formatterDeviation.format(deviationM1M2) + "          ");

			System.out.println();

			maxAbsDeviation = Math.max(maxAbsDeviation, Math.abs(deviationM1M2));
		}
	
		long endMillis		= System.currentTimeMillis();

		System.out.println("Maximum abs deviation  : " + formatterDeviation.format(maxAbsDeviation));
		System.out.println("Calculation time (sec) : " + ((endMillis-startMillis) / 1000.0));
		System.out.println("__________________________________________________________________________________________\n");

		/*
		 * jUnit assertion: condition under which we consider this test successful
		 */
		Assert.assertTrue(Math.abs(maxAbsDeviation) < 8E-3);
	}
		
	@Test
	public static void testBermudanSwaption() throws CalculationException {
		/*
		 * Value a swaption
		 */
		System.out.println("Bermudan Swaption prices:\n");
		System.out.println("Maturity      Simulation(M1)  Simulation(M2)  AnalyticSwaption  Deviation         L(0, T, T+delta)          ");

		long startMillis	= System.currentTimeMillis();

		double maxAbsDeviation = 0.0;
		for (int maturityIndex = 1; maturityIndex <= SimulationModel1.getNumberOfLibors() - 10; maturityIndex++) {

			double exerciseDate = SimulationModel1.getLiborPeriod(maturityIndex);
			System.out.print(formatterMaturity.format(exerciseDate) + "          ");

			int numberOfPeriods = 5;

			// Create a swaption

			double[] fixingDates = new double[numberOfPeriods];
			double[] paymentDates = new double[numberOfPeriods];
			double[] swapTenor = new double[numberOfPeriods + 1];
			double swapPeriodLength = 0.5;
			String tenorCode = "6M";

			for (int periodStartIndex = 0; periodStartIndex < numberOfPeriods; periodStartIndex++) {
				fixingDates[periodStartIndex] = exerciseDate + periodStartIndex * swapPeriodLength;
				paymentDates[periodStartIndex] = exerciseDate + (periodStartIndex + 1) * swapPeriodLength;
				swapTenor[periodStartIndex] = exerciseDate + periodStartIndex * swapPeriodLength;
			}
			swapTenor[numberOfPeriods] = exerciseDate + numberOfPeriods * swapPeriodLength;

			// Swaptions swap rate
			double swaprate = getParSwaprate(SimulationModel1, swapTenor, tenorCode);
			double swapAnnuity = getSwapAnnuity(SimulationModel1, swapTenor);

			// Set swap rates for each period
			double[] swaprates = new double[numberOfPeriods];
			Arrays.fill(swaprates, swaprate);

			double[] periodLength = new double[numberOfPeriods];
			Arrays.fill(periodLength, swapPeriodLength);

			double[] periodNotionals = new double[numberOfPeriods];
			Arrays.fill(periodNotionals, 1.0);

			boolean[] isPeriodStartDateExerciseDate = new boolean[numberOfPeriods];
			Arrays.fill(isPeriodStartDateExerciseDate, true);

			// Create a bermudan swaption
			BermudanSwaption bermudanSwaption = new BermudanSwaption(isPeriodStartDateExerciseDate, fixingDates, periodLength, paymentDates, periodNotionals, swaprates);

			// Value with Hull-White Model Monte Carlo
			double valueSimulationM1 = bermudanSwaption.getValue(SimulationModel1);
			System.out.print(formatterValue.format(valueSimulationM1) + "          ");

			// Value with LIBOR Market Model Monte Carlo
			double valueSimulationM2 = bermudanSwaption.getValue(SimulationModel2);
			System.out.print(formatterValue.format(valueSimulationM2) + "          ");

			// Value the underlying swaption with analytic formula (approximate, using Bachelier formula)
			SwaptionAnalyticApproximation swaptionAnalytic = new SwaptionAnalyticApproximation(swaprate, swapTenor, SwaptionAnalyticApproximation.ValueUnit.VOLATILITYLOGNORMAL);
			double volatilityAnalytic = swaptionAnalytic.getValue(SimulationModel2);
			double valueAnalytic = AnalyticFormulas.bachelierOptionValue(swaprate, volatilityAnalytic, exerciseDate, swaprate, swapAnnuity);
			System.out.print(formatterValue.format(valueAnalytic) + "          ");

			// Absolute deviation
			double deviationM1M2 = (valueSimulationM1 - valueSimulationM2);
			System.out.print(formatterDeviation.format(deviationM1M2) + "          ");

			
			System.out.print(SimulationModel1.getLIBOR(0, maturityIndex, maturityIndex+1).getAverage());
			
			System.out.println();

			maxAbsDeviation = Math.max(maxAbsDeviation, Math.abs(deviationM1M2));
		}

		long endMillis		= System.currentTimeMillis();

		System.out.println("Maximum abs deviation  : " + formatterDeviation.format(maxAbsDeviation));
		System.out.println("Calculation time (sec) : " + ((endMillis-startMillis) / 1000.0));
		System.out.println("__________________________________________________________________________________________\n");

		/*
		 * jUnit assertion: condition under which we consider this test successful
		 */
		Assert.assertTrue(Math.abs(maxAbsDeviation) < 8E-3);
	}
	
	@Test
	public static void testZeroCMSSwap() throws CalculationException {
		/*
		 * Value a swap
		 */

		System.out.println("Zero-CMS-Swap prices:\n");
		System.out.println("Swap             \tValue (M1)       Value (M2)     Deviation");

		long startMillis	= System.currentTimeMillis();

		double maxAbsDeviation = 0.0;
		for (int maturityIndex = 1; maturityIndex <= SimulationModel1.getNumberOfLibors()-10-20; maturityIndex++) {

			double startDate = SimulationModel1.getLiborPeriod(maturityIndex);

			int numberOfPeriods = 10;

			// Create a swap
			double[]	fixingDates			= new double[numberOfPeriods];
			double[]	paymentDates		= new double[numberOfPeriods];
			double[]	swapTenor			= new double[numberOfPeriods + 1];
			double		swapPeriodLength	= 0.5;
			String		tenorCode			= "6M";

			for (int periodStartIndex = 0; periodStartIndex < numberOfPeriods; periodStartIndex++) {
				fixingDates[periodStartIndex]	= startDate + periodStartIndex * swapPeriodLength;
				paymentDates[periodStartIndex]	= startDate + (periodStartIndex + 1) * swapPeriodLength;
				swapTenor[periodStartIndex]		= startDate + periodStartIndex * swapPeriodLength;
			}
			swapTenor[numberOfPeriods] = startDate + numberOfPeriods * swapPeriodLength;

			System.out.print("(" + formatterMaturity.format(swapTenor[0]) + "," + formatterMaturity.format(swapTenor[numberOfPeriods-1]) + "," + swapPeriodLength + ")" + "\t");

			// Par swap rate
			double swaprate = getParSwaprate(SimulationModel1, swapTenor, tenorCode);

			// Set swap rates for each period
			double[] swaprates = new double[numberOfPeriods];
			for (int periodStartIndex = 0; periodStartIndex < numberOfPeriods; periodStartIndex++) {
				swaprates[periodStartIndex] = swaprate;
			}

			// Create a swap
			AbstractIndex index = new ConstantMaturitySwaprate(10.0, 0.5);
			index = new CappedFlooredIndex(index, new FixedCoupon(0.1) /* cap */, new FixedCoupon(0.04) /* Floor */);
			SimpleZeroSwap swap = new SimpleZeroSwap(fixingDates, paymentDates, swaprates, index, true);

			// Value the swap
			double valueSimulationM1 = swap.getValue(SimulationModel1);
			System.out.print(formatterValue.format(valueSimulationM1) + "          ");

			double valueSimulationM2 = swap.getValue(SimulationModel2);
			System.out.print(formatterValue.format(valueSimulationM2) + "          ");

			// Absolute deviation
			double deviationM1M2 = (valueSimulationM1 - valueSimulationM2);
			System.out.print(formatterDeviation.format(deviationM1M2) + "          ");

			System.out.println();

			maxAbsDeviation = Math.max(maxAbsDeviation, Math.abs(deviationM1M2));
		}

		long endMillis		= System.currentTimeMillis();

		System.out.println("Maximum abs deviation  : " + formatterDeviation.format(maxAbsDeviation));
		System.out.println("Calculation time (sec) : " + ((endMillis-startMillis) / 1000.0));
		System.out.println("__________________________________________________________________________________________\n");

		/*
		 * jUnit assertion: condition under which we consider this test successful
		 */
		Assert.assertTrue(maxAbsDeviation < 1E-3);
	}

	@Test
	public static void testLIBORInArrearsFloatLeg() throws CalculationException {

		/*
		 * Create a payment schedule from conventions
		 */
		LocalDate	referenceDate = LocalDate.of(2014, Month.AUGUST, 12);
		int			spotOffsetDays = 2;
		String		forwardStartPeriod = "6M";
		String		maturity = "6M";
		String		frequency = "semiannual";
		String		daycountConvention = "30/360";

		Schedule schedule = ScheduleGenerator.createScheduleFromConventions(referenceDate, spotOffsetDays, forwardStartPeriod, maturity, frequency, daycountConvention, "first", "following", new BusinessdayCalendarExcludingTARGETHolidays(), -2, 0);

		/*
		 * Create the leg with a notional and index
		 */
		AbstractNotional notional = new Notional(1.0);
		AbstractIndex liborIndex = new LIBORIndex(0.0, 0.5);
		AbstractIndex index = new LinearCombinationIndex(-1.0, liborIndex, +1, new LaggedIndex(liborIndex, +0.5 /* fixingOffset */));
		double spread = 0.0;

		SwapLeg leg = new SwapLeg(schedule, notional, index, spread, false /* isNotionalExchanged */);

		System.out.println("LIBOR in Arrears Swap prices:\n");
		System.out.println("Value (M1)               Value (M2)              Deviation");

		// Value the swap
		RandomVariable valueSimulationM1 = leg.getValue(0,SimulationModel1);
		System.out.print(formatterValue.format(valueSimulationM1.getAverage()) + " " + formatterValue.format(valueSimulationM1.getStandardError()) + "          ");

		RandomVariable valueSimulationM2 = leg.getValue(0,SimulationModel2);
		System.out.print(formatterValue.format(valueSimulationM2.getAverage()) + " " + formatterValue.format(valueSimulationM2.getStandardError()) + "          ");

		// Absolute deviation
		double deviationM1M2 = (valueSimulationM1.getAverage() - valueSimulationM2.getAverage());
		System.out.print(formatterDeviation.format(deviationM1M2) + "          ");

		System.out.println();

		System.out.println("__________________________________________________________________________________________\n");

		/*
		 * jUnit assertion: condition under which we consider this test successful
		 */
		Assert.assertTrue(deviationM1M2 < 1E-3);
	}
	
	@Test
	public static void testPutOnMoneyMarketAccount() throws CalculationException {

		/*
		 * Create the product
		 */
		Option product = new Option(0.5, 1.025, new Numeraire());

		System.out.println("Put-on-Money-Market-Account prices:\n");
		System.out.println("Value (M1)               Value (M2)              Deviation");

		// Value the product
		RandomVariable valueSimulationM1 = product.getValue(0,SimulationModel1);
		System.out.print(formatterValue.format(valueSimulationM1.getAverage()) + " " + formatterValue.format(valueSimulationM1.getStandardError()) + "          ");

		RandomVariable valueSimulationM2 = product.getValue(0,SimulationModel2);
		System.out.print(formatterValue.format(valueSimulationM2.getAverage()) + " " + formatterValue.format(valueSimulationM2.getStandardError()) + "          ");

		// Absolute deviation
		double deviationM1M2 = (valueSimulationM1.getAverage() - valueSimulationM2.getAverage());
		System.out.print(formatterDeviation.format(deviationM1M2) + "          ");

		System.out.println();

		System.out.println("__________________________________________________________________________________________\n");

		/*
		 * jUnit assertion: condition under which we consider this test successful
		 */
		Assert.assertEquals("Valuation of put on MMA", valueSimulationM2.getAverage(), valueSimulationM1.getAverage(), 1E-5);
	}
	
	
	
	
	
	
	private static double getParSwaprate(LIBORModelMonteCarloSimulationModel liborMarketModel, Schedule fixLeg, Schedule floatLeg, String tenorCode) {
		ForwardCurve forwardCurve = liborMarketModel.getModel().getForwardRateCurve();
		AnalyticModel analyticModel = liborMarketModel.getModel().getAnalyticModel();
		return net.finmath.marketdata.products.Swap.getForwardSwapRate(fixLeg, floatLeg, forwardCurve, analyticModel);
	}

	private static double getParSwaprate(LIBORModelMonteCarloSimulationModel liborMarketModel, double[] swapTenor, String tenorCode) {
		DiscountCurve discountCurve = liborMarketModel.getModel().getDiscountCurve();
		ForwardCurve forwardCurve = liborMarketModel.getModel().getForwardRateCurve();
		AnalyticModel analyticModel = liborMarketModel.getModel().getAnalyticModel();
		return net.finmath.marketdata.products.Swap.getForwardSwapRate(new TimeDiscretizationFromArray(swapTenor), new TimeDiscretizationFromArray(swapTenor),
				forwardCurve,
				discountCurve);
	}

	private static double getSwapAnnuity(LIBORModelMonteCarloSimulationModel liborMarketModel, double[] swapTenor) {
		DiscountCurve discountCurve = liborMarketModel.getModel().getDiscountCurve();
		if(discountCurve == null) {
			discountCurve = new DiscountCurveFromForwardCurve(liborMarketModel.getModel().getForwardRateCurve());
		}
		return net.finmath.marketdata.products.SwapAnnuity.getSwapAnnuity(new TimeDiscretizationFromArray(swapTenor), discountCurve);
	}
}

