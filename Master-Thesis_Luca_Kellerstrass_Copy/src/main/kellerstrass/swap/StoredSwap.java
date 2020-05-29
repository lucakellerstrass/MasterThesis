package kellerstrass.swap;

import java.time.LocalDate;
import java.time.Month;

import kellerstrass.useful.StringToUseful;
import net.finmath.montecarlo.interestrate.products.Swap;
import net.finmath.montecarlo.interestrate.products.SwapLeg;
import net.finmath.montecarlo.interestrate.products.components.AbstractNotional;
import net.finmath.montecarlo.interestrate.products.components.Notional;
import net.finmath.montecarlo.interestrate.products.indices.AbstractIndex;
import net.finmath.montecarlo.interestrate.products.indices.LIBORIndex;
import net.finmath.time.Schedule;
import net.finmath.time.ScheduleGenerator;
import net.finmath.time.ScheduleGenerator.DaycountConvention;
import net.finmath.time.ScheduleGenerator.Frequency;
import net.finmath.time.businessdaycalendar.BusinessdayCalendar;
import net.finmath.time.businessdaycalendar.BusinessdayCalendarExcludingTARGETHolidays;
import net.finmath.time.daycount.DayCountConvention;

/**
 * This class contains different swap information and gives the possibility
 * 
 * @author lucak
 *
 */
public class StoredSwap {

	private Schedule legScheduleRec;
	private Schedule legSchedulePay;
	private AbstractNotional notional;
	private AbstractIndex index;
	private double fixedCoupon;

	public void setFixedCoupon(double fixedCoupon) {
		this.fixedCoupon = fixedCoupon;
	}

	private String swapName;

	/**
	 * Initialize a stored swap using the Input Parameters to create your own swap
	 * 
	 * @param legScheduleRec
	 * @param legSchedulePay
	 * @param notional
	 * @param index
	 * @param fixedCoupon
	 */
	public StoredSwap(Schedule legScheduleRec, Schedule legSchedulePay, AbstractNotional notional, AbstractIndex index,
			double fixedCoupon) {
		super();
		this.legScheduleRec = legScheduleRec;
		this.legSchedulePay = legSchedulePay;
		this.notional = notional;
		this.index = index;
		this.fixedCoupon = fixedCoupon;
	}

	/**
	 * Create your own swap
	 * 
	 * @param SwapName
	 * @param BuySell
	 * @param notionalInput
	 * @param fixedRate
	 * @param referenceDate
	 * @param swapStart
	 * @param swapEnd
	 * @param fixedFrequency
	 * @param floatFrequency
	 * @param RateFrequency
	 * @param discountCurve
	 * @param forecastCurve
	 * @param fixedCouponConvention
	 * @param xiborCouponConvention
	 */
	public StoredSwap(String SwapName, String BuySell, int notionalInput, double fixedRate, String referenceDate,
			String swapStart, String swapEnd, String fixedFrequency, String floatFrequency, String RateFrequency,
			String discountCurve, String forecastCurve, String fixedCouponConvention, String xiborCouponConvention) {

		Frequency fixFrequency = StringToUseful.getpaymentFrequency(fixedFrequency);
		Frequency floatingFrequency = StringToUseful.getpaymentFrequency(fixedFrequency);

		LocalDate refDate = StringToUseful.referenceDateFromString(referenceDate);

		DaycountConvention fixDayCount = StringToUseful.getDayCountFromString(fixedCouponConvention);
		DaycountConvention floatDayCount = StringToUseful.getDayCountFromString(xiborCouponConvention);

		legSchedulePay = ScheduleGenerator.createScheduleFromConventions(refDate /* referenceDate */,
				StringToUseful.addToDate(refDate, swapStart) /* startDate */,
				StringToUseful.addToDate(refDate, swapEnd) /* maturityDate */, fixFrequency /* frequency */,
				fixDayCount /* daycountConvention */,
				ScheduleGenerator.ShortPeriodConvention.FIRST /* shortPeriodConvention */,
				BusinessdayCalendar.DateRollConvention.FOLLOWING /* dateRollConvention */,
				new BusinessdayCalendarExcludingTARGETHolidays() /* businessdayCalendar */, 0 /* fixingOffsetDays */,
				0 /* paymentOffsetDays */);

		legScheduleRec = ScheduleGenerator.createScheduleFromConventions(refDate /* referenceDate */,
				StringToUseful.addToDate(refDate, swapStart) /* startDate */,
				StringToUseful.addToDate(refDate, swapEnd) /* maturityDate */,
				floatingFrequency /* ScheduleGenerator.Frequency.SEMIANNUAL /* frequency */,
				floatDayCount /* daycountConvention */,
				ScheduleGenerator.ShortPeriodConvention.FIRST /* shortPeriodConvention */,
				BusinessdayCalendar.DateRollConvention.FOLLOWING /* dateRollConvention */,
				new BusinessdayCalendarExcludingTARGETHolidays() /* businessdayCalendar */, 0 /* fixingOffsetDays */,
				0 /* paymentOffsetDays */);

		if (BuySell.equals("Buy")) {
			notionalInput = -notionalInput;
		}
		double notionalBuySelladjusted = notionalInput;
		notional = new Notional(notionalBuySelladjusted);

		double rateFrequency = StringToUseful.getDoubleFromString(RateFrequency);

		index = new LIBORIndex(null /* "forwardCurve" */, 0.0, rateFrequency);

		fixedCoupon = fixedRate; // 0.00547;
		swapName = SwapName;
	}

	/**
	 * Get a stored swap, choosing from one of this names: <br>
	 * Example
	 * 
	 * @ToDo: Do this as if-else
	 * @param swapName
	 */
	public StoredSwap(String swapName) {
		switch (swapName) {
		case "Example":
			activateExampleSwap();
			break;
		case "Example 2":
			activateExampleSwap2();
			break;
		case "TrueSwap1":
			activateTrueSwap1();

		}

	}

	/**
	 * activates a specified (true) swap
	 */
	private void activateTrueSwap1() {
		/*
		 * Create a PAYER swap (receive float, pay fixed)
		 */
		legSchedulePay = ScheduleGenerator.createScheduleFromConventions(
				LocalDate.of(2019, Month.OCTOBER, 24) /* referenceDate */,
				LocalDate.of(2019, Month.OCTOBER, 29) /* startDate */,
				LocalDate.of(2029, Month.OCTOBER, 29) /* maturityDate */,
				ScheduleGenerator.Frequency.ANNUAL /* frequency */,
				ScheduleGenerator.DaycountConvention.E30_360 /* daycountConvention */,
				ScheduleGenerator.ShortPeriodConvention.FIRST /* shortPeriodConvention */,
				BusinessdayCalendar.DateRollConvention.FOLLOWING /* dateRollConvention */,
				new BusinessdayCalendarExcludingTARGETHolidays() /* businessdayCalendar */, 0 /* fixingOffsetDays */,
				0 /* paymentOffsetDays */);

		legScheduleRec = ScheduleGenerator.createScheduleFromConventions(
				LocalDate.of(2019, Month.OCTOBER, 24) /* referenceDate */,
				LocalDate.of(2019, Month.OCTOBER, 29) /* startDate */,
				LocalDate.of(2029, Month.OCTOBER, 29) /* maturityDate */,
				ScheduleGenerator.Frequency.QUARTERLY /* frequency */,
				ScheduleGenerator.DaycountConvention.ACT_360 /* daycountConvention */,
				ScheduleGenerator.ShortPeriodConvention.FIRST /* shortPeriodConvention */,
				BusinessdayCalendar.DateRollConvention.FOLLOWING /* dateRollConvention */,
				new BusinessdayCalendarExcludingTARGETHolidays() /* businessdayCalendar */, 0 /* fixingOffsetDays */,
				0 /* paymentOffsetDays */);
		notional = new Notional(1000000.0);
		index = new LIBORIndex(null /* "forwardCurve" */, 0.0, 0.25);
		fixedCoupon = 0.00547;

		swapName = "TrueSwap1";

	}

	/**
	 * activates a specified (example) swap
	 */
	private void activateExampleSwap() {
		/*
		 * Create a receiver swap (receive fix, pay float)
		 */
		legScheduleRec = ScheduleGenerator.createScheduleFromConventions(
				LocalDate.of(2015, Month.JANUARY, 03) /* referenceDate */,
				LocalDate.of(2015, Month.JANUARY, 06) /* startDate */,
				LocalDate.of(2025, Month.JANUARY, 06) /* maturityDate */,
				ScheduleGenerator.Frequency.ANNUAL /* frequency */,
				ScheduleGenerator.DaycountConvention.ACT_365 /* daycountConvention */,
				ScheduleGenerator.ShortPeriodConvention.FIRST /* shortPeriodConvention */,
				BusinessdayCalendar.DateRollConvention.FOLLOWING /* dateRollConvention */,
				new BusinessdayCalendarExcludingTARGETHolidays() /* businessdayCalendar */, 0 /* fixingOffsetDays */,
				0 /* paymentOffsetDays */);

		legSchedulePay = ScheduleGenerator.createScheduleFromConventions(
				LocalDate.of(2015, Month.JANUARY, 03) /* referenceDate */,
				LocalDate.of(2015, Month.JANUARY, 06) /* startDate */,
				LocalDate.of(2025, Month.JANUARY, 06) /* maturityDate */,
				ScheduleGenerator.Frequency.QUARTERLY /* frequency */,
				ScheduleGenerator.DaycountConvention.ACT_365 /* daycountConvention */,
				ScheduleGenerator.ShortPeriodConvention.FIRST /* shortPeriodConvention */,
				BusinessdayCalendar.DateRollConvention.FOLLOWING /* dateRollConvention */,
				new BusinessdayCalendarExcludingTARGETHolidays() /* businessdayCalendar */, 0 /* fixingOffsetDays */,
				0 /* paymentOffsetDays */);
		notional = new Notional(1.0);
		index = new LIBORIndex(null /* "forwardCurve" */, 0.0, 0.5);
		fixedCoupon = 0.001252;

		swapName = "ExampleSwap1";
	}

	/**
	 * activates a specified (second example) swap
	 */
	private void activateExampleSwap2() {
		/*
		 * Create a receiver swap (receive fix, pay float)
		 */
		legScheduleRec = ScheduleGenerator.createScheduleFromConventions(
				LocalDate.of(2015, Month.JANUARY, 03) /* referenceDate */,
				LocalDate.of(2015, Month.JANUARY, 06) /* startDate */,
				LocalDate.of(2035, Month.JANUARY, 06) /* maturityDate */,
				ScheduleGenerator.Frequency.ANNUAL /* frequency */,
				ScheduleGenerator.DaycountConvention.ACT_365 /* daycountConvention */,
				ScheduleGenerator.ShortPeriodConvention.FIRST /* shortPeriodConvention */,
				BusinessdayCalendar.DateRollConvention.FOLLOWING /* dateRollConvention */,
				new BusinessdayCalendarExcludingTARGETHolidays() /* businessdayCalendar */, 0 /* fixingOffsetDays */,
				0 /* paymentOffsetDays */);

		legSchedulePay = ScheduleGenerator.createScheduleFromConventions(
				LocalDate.of(2015, Month.JANUARY, 03) /* referenceDate */,
				LocalDate.of(2015, Month.JANUARY, 06) /* startDate */,
				LocalDate.of(2035, Month.JANUARY, 06) /* maturityDate */,
				ScheduleGenerator.Frequency.ANNUAL /* frequency */,
				ScheduleGenerator.DaycountConvention.ACT_365 /* daycountConvention */,
				ScheduleGenerator.ShortPeriodConvention.FIRST /* shortPeriodConvention */,
				BusinessdayCalendar.DateRollConvention.FOLLOWING /* dateRollConvention */,
				new BusinessdayCalendarExcludingTARGETHolidays() /* businessdayCalendar */, 0 /* fixingOffsetDays */,
				0 /* paymentOffsetDays */);
		notional = new Notional(1.0);
		index = new LIBORIndex(null /* "forwardCurve" */, 0.0, 0.5);
		fixedCoupon = 0.005;

		swapName = "ExampleSwap2";

	}

	/**
	 * Get the required swap.
	 * 
	 * @return
	 */
	public Swap getSwap() {
		SwapLeg swapLegRec = new SwapLeg(legScheduleRec, notional, null, fixedCoupon /* spread */,
				false /* isNotionalExchanged */);
		SwapLeg swapLegPay = new SwapLeg(legSchedulePay, notional, index, 0.0 /* spread */,
				false /* isNotionalExchanged */);
		return new Swap(swapLegRec, swapLegPay);
	}

	public Schedule getLegScheduleRec() {
		return legScheduleRec;
	}

	public Schedule getLegSchedulePay() {
		return legSchedulePay;
	}

	public AbstractNotional getNotional() {
		return notional;
	}

	public AbstractIndex getIndex() {
		return index;
	}

	public double getFixedCoupon() {
		return fixedCoupon;
	}

	public String getSwapName() {
		return swapName;
	}

}
