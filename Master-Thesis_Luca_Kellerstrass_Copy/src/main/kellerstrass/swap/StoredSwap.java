package kellerstrass.swap;

import java.time.LocalDate;
import java.time.Month;

import net.finmath.montecarlo.interestrate.products.Swap;
import net.finmath.montecarlo.interestrate.products.SwapLeg;
import net.finmath.montecarlo.interestrate.products.components.AbstractNotional;
import net.finmath.montecarlo.interestrate.products.components.Notional;
import net.finmath.montecarlo.interestrate.products.indices.AbstractIndex;
import net.finmath.montecarlo.interestrate.products.indices.LIBORIndex;
import net.finmath.time.Schedule;
import net.finmath.time.ScheduleGenerator;
import net.finmath.time.businessdaycalendar.BusinessdayCalendar;
import net.finmath.time.businessdaycalendar.BusinessdayCalendarExcludingTARGETHolidays;

public class StoredSwap {
	
	private Schedule legScheduleRec;
	private Schedule legSchedulePay;
	private AbstractNotional notional;
	private AbstractIndex index;
	private double fixedCoupon;
	private String swapName;
	
	

	/**
	 * Initialize a stored swap using the Input Parameters to create your own swap
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
	 * Get a stored swap, choosing from one of this names:
	 * <br>
	 * Example
	 * @param swapName
	 */
	public StoredSwap(String swapName) {
		switch(swapName) {
		case "Example":
			activateExampleSwap();
			break;
		case "Example 2":
			activateExampleSwap2();
		}
		
	}
	
	
	
	


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
				new BusinessdayCalendarExcludingTARGETHolidays() /* businessdayCalendar */,
				0 /* fixingOffsetDays */,
				0 /* paymentOffsetDays */);

		legSchedulePay = ScheduleGenerator.createScheduleFromConventions(
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
		notional = new Notional(1.0);
		index = new LIBORIndex(null /*"forwardCurve"*/, 0.0, 0.25);
		fixedCoupon = 0.001252;
			
		swapName = "ExampleSwap1";
	}

	
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
				new BusinessdayCalendarExcludingTARGETHolidays() /* businessdayCalendar */,
				0 /* fixingOffsetDays */,
				0 /* paymentOffsetDays */);

		legSchedulePay = ScheduleGenerator.createScheduleFromConventions(
				LocalDate.of(2015, Month.JANUARY, 03) /* referenceDate */,
				LocalDate.of(2015, Month.JANUARY, 06) /* startDate */,
				LocalDate.of(2035, Month.JANUARY, 06) /* maturityDate */,
				ScheduleGenerator.Frequency.ANNUAL /* frequency */,
				ScheduleGenerator.DaycountConvention.ACT_365 /* daycountConvention */,
				ScheduleGenerator.ShortPeriodConvention.FIRST /* shortPeriodConvention */,
				BusinessdayCalendar.DateRollConvention.FOLLOWING /* dateRollConvention */,
				new BusinessdayCalendarExcludingTARGETHolidays() /* businessdayCalendar */,
				0 /* fixingOffsetDays */,
				0 /* paymentOffsetDays */);
		notional = new Notional(1.0);
		index = new LIBORIndex(null /*"forwardCurve"*/, 0.0, 0.5);
		fixedCoupon = 0.005;
			
		swapName = "ExampleSwap2";
		
	}

	
	
	
	
	
	
	
	public Swap getSwap() {
		SwapLeg swapLegRec = new SwapLeg(legScheduleRec, notional, null, fixedCoupon /* spread */, false /* isNotionalExchanged */);
		SwapLeg swapLegPay = new SwapLeg(legSchedulePay, notional, index, 0.0 /* spread */, false /* isNotionalExchanged */);
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
