package kellerstrass.swap.test;

import java.time.LocalDate;
import java.time.Month;

import kellerstrass.swap.StoredSwap;
import net.finmath.montecarlo.interestrate.products.Swap;
import net.finmath.time.ScheduleGenerator;
import net.finmath.time.ScheduleGenerator.DaycountConvention;
import net.finmath.time.ScheduleGenerator.Frequency;

public class StoredSwapTest {

	public static void main(String[] args) {

		// Initialization of an example Swap

		StoredSwap testStoredSwap = new StoredSwap("Example");
		Swap testSwap = testStoredSwap.getSwap();
		System.out.println("Initialization of the swap worked");

		System.out.println("The name of the swap is " + testStoredSwap.getSwapName());

		// StoredSwap createdSwap = new StoredSwap("My New Swap",
		// "Buy", 1000000, 0.0025, "1d", "10y", "1Y", "3M", null, null, null, null,
		// null)

		String referenceDate = "24.02.2019";
		LocalDate refDate = referenceDateFromString(referenceDate);
		String AddedString = "20M";
		System.out.println(refDate);
		System.out.println(addToDate(refDate, AddedString));

	}

	private static LocalDate referenceDateFromString(String referenceDate) {

		int days = Integer.parseInt((String) referenceDate.subSequence(0, 2));
		int month = Integer.parseInt((String) referenceDate.subSequence(3, 5));
		int year = Integer.parseInt((String) referenceDate.subSequence(6, 10));
		return LocalDate.of(year, month, days);
	}

	private static LocalDate addToDate(LocalDate referenceDate, String AddedString) {

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

}
