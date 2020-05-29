package kellerstrass.useful;

import java.time.LocalDate;

import kellerstrass.marketInformation.DataScope;
import kellerstrass.marketInformation.DataSource;
import net.finmath.time.ScheduleGenerator;
import net.finmath.time.ScheduleGenerator.DaycountConvention;
import net.finmath.time.ScheduleGenerator.Frequency;

/**
 * Class to get useful class instances in the project from String Input <br>
 * mainly used for the communication with the Python GUI.
 * 
 * @author lucak
 *
 */
public class StringToUseful {

	/**
	 * Get the DayCount Convention from a String
	 * 
	 * @param CouponConvention
	 * @return
	 */
	public static DaycountConvention getDayCountFromString(String CouponConvention) {

		if (CouponConvention.equals("ACT/360") ^ CouponConvention.equals("ACT_360")
				^ CouponConvention.equals("ACT360")) {
			return ScheduleGenerator.DaycountConvention.ACT_360;
		} else {
			if (CouponConvention.equals("30/360") ^ CouponConvention.equals("30_360")
					^ CouponConvention.equals("30360")) {
				return ScheduleGenerator.DaycountConvention.E30_360;
			} else {
				if (CouponConvention.equals("ACT/365") ^ CouponConvention.equals("ACT_365")
						^ CouponConvention.equals("ACT365")) {
					return ScheduleGenerator.DaycountConvention.ACT_365;
				} else {
					System.out.println("String to daycountconvention didi not work");
					return null;
				}
			}
		}
	}

	/**
	 * Get the payment frequency from a String
	 * 
	 * @param fixedFrequency
	 * @return
	 */
	public static Frequency getpaymentFrequency(String fixedFrequency) {

		if (fixedFrequency.equals("1d") ^ fixedFrequency.equals("1D")) {
			return ScheduleGenerator.Frequency.DAILY;
		} else {
			if (fixedFrequency.equals("7d") ^ fixedFrequency.equals("7D")) {
				return ScheduleGenerator.Frequency.WEEKLY;
			} else {
				if (fixedFrequency.equals("1m") ^ fixedFrequency.equals("1M")) {
					return ScheduleGenerator.Frequency.MONTHLY;
				} else {
					if (fixedFrequency.equals("3m") ^ fixedFrequency.equals("3M")) {
						return ScheduleGenerator.Frequency.QUARTERLY;
					} else {
						if (fixedFrequency.equals("6m") ^ fixedFrequency.equals("6M")) {
							return ScheduleGenerator.Frequency.SEMIANNUAL;
						} else {
							if (fixedFrequency.equals("1Y") ^ fixedFrequency.equals("1y")) {
								return ScheduleGenerator.Frequency.ANNUAL;
							} else {
								System.out.println("String time to double time did not work");
								return null;
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Get a localDate from a String
	 * 
	 * @param referenceDate
	 * @return LocalDate
	 */
	public static LocalDate referenceDateFromString(String referenceDate) {

		int days = Integer.parseInt((String) referenceDate.subSequence(0, 2));
		int month = Integer.parseInt((String) referenceDate.subSequence(3, 5));
		int year = Integer.parseInt((String) referenceDate.subSequence(6, 10));
		return LocalDate.of(year, month, days);
	}

	/**
	 * Add a String in 1d/1m/1y logic to a localDate.
	 * 
	 * @param referenceDate
	 * @param AddedString
	 * @return
	 */
	public static LocalDate addToDate(LocalDate referenceDate, String AddedString) {

		int last = AddedString.lastIndexOf("");
		String unit = AddedString.substring(AddedString.length() - 1);
		int numberOfUnit = Integer.parseInt(AddedString.substring(0, last - 1));

		long daysToAdd = 0;
		long monthsToAdd = 0;
		long yearsToAdd = 0;

		if (unit.equals("d") ^ unit.equals("D")) {
			daysToAdd = numberOfUnit;
		} else {
			if (unit.equals("m") ^ unit.equals("M")) {
				monthsToAdd = numberOfUnit;
			} else {
				if (unit.equals("y") ^ unit.equals("Y")) {
					yearsToAdd = numberOfUnit;
				} else {
					System.out.println("String adding to date did not work");
				}
			}
		}

		referenceDate = referenceDate.plusDays(daysToAdd);
		referenceDate = referenceDate.plusMonths(monthsToAdd);
		referenceDate = referenceDate.plusYears(yearsToAdd);

		return referenceDate;
	}

	/**
	 * This only works for 1Y, 6M, 3M
	 * 
	 * @param rateFrequency
	 * @return
	 * @return
	 */
	public static double getDoubleFromString(String rateFrequency) {

		if (rateFrequency.equals("3m") ^ rateFrequency.equals("3M")) {
			return 0.25;
		} else {
			if (rateFrequency.equals("6m") ^ rateFrequency.equals("6M")) {
				return 0.6;
			} else {
				if (rateFrequency.equals("1y") ^ rateFrequency.equals("1Y")) {
					return 1.0;
				} else {
					System.out.println("String time to double time did not work");
					return 0;
				}
			}
		}
	}

	/**
	 * Get the DataSource instance from a String
	 * 
	 * @param dataSourceInput
	 * @return
	 */
	public static DataSource dataSourceFromString(String dataSourceInput) {

		if (dataSourceInput.equals("Example")) {
			return DataSource.EXAMPLE;
		} else {
			if (dataSourceInput.equals("volas 24.10.2019")) {
				return DataSource.Market24_10_2019;
			} else {
				if (dataSourceInput.equals("volas 23.10.2019")) {
					return DataSource.Market23_10_2019;
				} else {

					System.out.println("DataSource not Valid");
					return null;
				}

			}
		}
	}

	/**
	 * Get the DataScope instance from a String
	 * 
	 * @param dataSourceInput
	 * @return
	 */
	public static DataScope dataScopeFromString(String dataSourceInput) {

		if (dataSourceInput.equals("Full Surface")) {
			return DataScope.FullSurface;
		} else {
			if (dataSourceInput.equals("Extended Co- Terminals")) {
				return DataScope.ExtendedCoTermindals;
			} else {
				if (dataSourceInput.equals("Co-Terminals")) {
					return DataScope.CoTerminals;
				} else {
					if (dataSourceInput.equals("Rising Terminals")) {
						return DataScope.RisingTerminals;
					} else {
						System.out.println("DataScope not Valid");
						return DataScope.FullSurface;
					}
				}
			}
		}

	}

}
