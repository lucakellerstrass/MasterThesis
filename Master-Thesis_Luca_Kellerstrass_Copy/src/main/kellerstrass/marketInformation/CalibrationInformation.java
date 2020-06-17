package kellerstrass.marketInformation;

import java.time.LocalDate;
import java.time.Month;

import net.finmath.time.businessdaycalendar.BusinessdayCalendarExcludingTARGETHolidays;
import net.finmath.time.daycount.DayCountConvention;
import net.finmath.time.daycount.DayCountConvention_ACT_365;

/**
 * This class contains various different market Information sets such as normal
 * swap volatilities. <br>
 * This Information can be used to calibrate a short-rate model or a LIBOR
 * Market Model.
 * 
 * @author lucak
 *
 */
public class CalibrationInformation {

	// The market information we need for calibration
	private double swapPeriodLength;
	private String[] atmExpiries = null;
	private String[] atmTenors = null;
	private double[] atmVolatilities = null;
	private String targetVolatilityType; // "VOLATILITYNORMAL";
	private LocalDate referenceDate = null;
	private BusinessdayCalendarExcludingTARGETHolidays cal = null;
	private DayCountConvention modelDC = null;

	private DataScope dataScope; // Co-terminals, extended Co-terminals, full surface
	private DataSource dataSource; // Example, true market Data, (Date)

	private String DataName;

	/**
	 * Constructs an instance of calibration Information using the data scope and
	 * data source Information <br>
	 * Not all combinations will work.
	 * 
	 * @param dataScope  || Co-terminals, extended Co-terminals, full surface; if
	 *                   null, Co-Terminals
	 * @param dataSource || example data, true market Data; if null example data is
	 *                   used
	 */
	public CalibrationInformation(DataScope dataScope, DataSource dataSource) {

		if (dataScope == null) {
			this.dataScope = DataScope.CoTerminals;
		} else {
			this.dataScope = dataScope;
		}

		if (dataScope == null) {
			this.dataSource = DataSource.EXAMPLE;
		} else {
			this.dataSource = dataSource;
		}

		fillDataVectors(dataScope, dataSource);

		DataName = dataSource.toString() + dataScope.toString();

	}

	/**
	 * Fill the Data Vectors with information based on the data scope and data
	 * source. Not all combinations will work
	 * 
	 * @param dataScope2
	 * @param dataSource2
	 */
	private void fillDataVectors(DataScope dataScope2, DataSource dataSource2) {

		switch (dataSource2) {

		case EXAMPLE:
			this.swapPeriodLength = 0.5;
			this.targetVolatilityType = "VOLATILITYNORMAL";
			this.referenceDate = LocalDate.of(2019, Month.OCTOBER, 24); // 24.10.2019
			this.cal = new BusinessdayCalendarExcludingTARGETHolidays();
			this.modelDC = new DayCountConvention_ACT_365();

			String[] ExpiriesFullSurface = { "1M", "1M", "1M", "1M", "1M", "1M", "1M", "1M", "1M", "1M", "1M", "1M",
					"1M", "1M", "3M", "3M", "3M", "3M", "3M", "3M", "3M", "3M", "3M", "3M", "3M", "3M", "3M", "3M",
					"6M", "6M", "6M", "6M", "6M", "6M", "6M", "6M", "6M", "6M", "6M", "6M", "6M", "6M", "1Y", "1Y",
					"1Y", "1Y", "1Y", "1Y", "1Y", "1Y", "1Y", "1Y", "1Y", "1Y", "1Y", "1Y", "2Y", "2Y", "2Y", "2Y",
					"2Y", "2Y", "2Y", "2Y", "2Y", "2Y", "2Y", "2Y", "2Y", "2Y", "3Y", "3Y", "3Y", "3Y", "3Y", "3Y",
					"3Y", "3Y", "3Y", "3Y", "3Y", "3Y", "3Y", "3Y", "4Y", "4Y", "4Y", "4Y", "4Y", "4Y", "4Y", "4Y",
					"4Y", "4Y", "4Y", "4Y", "4Y", "4Y", "5Y", "5Y", "5Y", "5Y", "5Y", "5Y", "5Y", "5Y", "5Y", "5Y",
					"5Y", "5Y", "5Y", "5Y", "7Y", "7Y", "7Y", "7Y", "7Y", "7Y", "7Y", "7Y", "7Y", "7Y", "7Y", "7Y",
					"7Y", "7Y", "10Y", "10Y", "10Y", "10Y", "10Y", "10Y", "10Y", "10Y", "10Y", "10Y", "10Y", "10Y",
					"10Y", "10Y", "15Y", "15Y", "15Y", "15Y", "15Y", "15Y", "15Y", "15Y", "15Y", "15Y", "15Y", "15Y",
					"15Y", "15Y", "20Y", "20Y", "20Y", "20Y", "20Y", "20Y", "20Y", "20Y", "20Y", "20Y", "20Y", "20Y",
					"20Y", "20Y", "25Y", "25Y", "25Y", "25Y", "25Y", "25Y", "25Y", "25Y", "25Y", "25Y", "25Y", "25Y",
					"25Y", "25Y", "30Y", "30Y", "30Y", "30Y", "30Y", "30Y", "30Y", "30Y", "30Y", "30Y", "30Y", "30Y",
					"30Y", "30Y" };
			String[] TenorsFullSurface = { "1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "15Y", "20Y",
					"25Y", "30Y", "1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "15Y", "20Y", "25Y",
					"30Y", "1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "15Y", "20Y", "25Y", "30Y",
					"1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "15Y", "20Y", "25Y", "30Y", "1Y", "2Y",
					"3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "15Y", "20Y", "25Y", "30Y", "1Y", "2Y", "3Y", "4Y",
					"5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "15Y", "20Y", "25Y", "30Y", "1Y", "2Y", "3Y", "4Y", "5Y", "6Y",
					"7Y", "8Y", "9Y", "10Y", "15Y", "20Y", "25Y", "30Y", "1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y",
					"9Y", "10Y", "15Y", "20Y", "25Y", "30Y", "1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y",
					"10Y", "15Y", "20Y", "25Y", "30Y", "1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y",
					"15Y", "20Y", "25Y", "30Y", "1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "15Y",
					"20Y", "25Y", "30Y", "1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "15Y", "20Y",
					"25Y", "30Y", "1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "15Y", "20Y", "25Y",
					"30Y", "1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "15Y", "20Y", "25Y", "30Y" };
			double[] VolatilitiesFullSurface = { 0.00151, 0.00169, 0.0021, 0.00248, 0.00291, 0.00329, 0.00365, 0.004,
					0.00437, 0.00466, 0.00527, 0.00571, 0.00604, 0.00625, 0.0016, 0.00174, 0.00217, 0.00264, 0.00314,
					0.00355, 0.00398, 0.00433, 0.00469, 0.00493, 0.00569, 0.00607, 0.00627, 0.00645, 0.00182, 0.00204,
					0.00238, 0.00286, 0.00339, 0.00384, 0.00424, 0.00456, 0.00488, 0.0052, 0.0059, 0.00623, 0.0064,
					0.00654, 0.00205, 0.00235, 0.00272, 0.0032, 0.00368, 0.00406, 0.00447, 0.00484, 0.00515, 0.00544,
					0.00602, 0.00629, 0.0064, 0.00646, 0.00279, 0.00319, 0.0036, 0.00396, 0.00436, 0.00469, 0.00503,
					0.0053, 0.00557, 0.00582, 0.00616, 0.00628, 0.00638, 0.00641, 0.00379, 0.00406, 0.00439, 0.00472,
					0.00504, 0.00532, 0.0056, 0.00582, 0.00602, 0.00617, 0.0063, 0.00636, 0.00638, 0.00639, 0.00471,
					0.00489, 0.00511, 0.00539, 0.00563, 0.00583, 0.006, 0.00618, 0.0063, 0.00644, 0.00641, 0.00638,
					0.00635, 0.00634, 0.00544, 0.00557, 0.00572, 0.00591, 0.00604, 0.00617, 0.0063, 0.00641, 0.00651,
					0.00661, 0.00645, 0.00634, 0.00627, 0.00624, 0.00625, 0.00632, 0.00638, 0.00644, 0.0065, 0.00655,
					0.00661, 0.00667, 0.00672, 0.00673, 0.00634, 0.00614, 0.00599, 0.00593, 0.00664, 0.00671, 0.00675,
					0.00676, 0.00676, 0.00675, 0.00676, 0.00674, 0.00672, 0.00669, 0.00616, 0.00586, 0.00569, 0.00558,
					0.00647, 0.00651, 0.00651, 0.00651, 0.00652, 0.00649, 0.00645, 0.0064, 0.00637, 0.00631, 0.00576,
					0.00534, 0.00512, 0.00495, 0.00615, 0.0062, 0.00618, 0.00613, 0.0061, 0.00607, 0.00602, 0.00596,
					0.00591, 0.00586, 0.00536, 0.00491, 0.00469, 0.0045, 0.00578, 0.00583, 0.00579, 0.00574, 0.00567,
					0.00562, 0.00556, 0.00549, 0.00545, 0.00538, 0.00493, 0.00453, 0.00435, 0.0042, 0.00542, 0.00547,
					0.00539, 0.00532, 0.00522, 0.00516, 0.0051, 0.00504, 0.005, 0.00495, 0.00454, 0.00418, 0.00404,
					0.00394 };

			switch (dataScope2) {

			case FullSurface:
				this.atmExpiries = ExpiriesFullSurface;
				this.atmTenors = TenorsFullSurface;
				this.atmVolatilities = VolatilitiesFullSurface;
				break;

			case CoTerminals:
				String[] atmExpiriesCoTerminals = { "1Y", "2Y", "3Y", "4Y", "5Y", "7Y", "10Y" };
				String[] atmTenorsCoTerminals = { "10Y", "9Y", "8Y", "7Y", "6Y", "4Y", "1Y" };
				double[] atmVolatilitiesCoTerminals = getVolatilitiesForExpiriesAndTenors(atmExpiriesCoTerminals,
						atmTenorsCoTerminals, ExpiriesFullSurface, TenorsFullSurface, VolatilitiesFullSurface);

				this.atmExpiries = atmExpiriesCoTerminals;
				this.atmTenors = atmTenorsCoTerminals;
				this.atmVolatilities = atmVolatilitiesCoTerminals;

				break;

			// combination of 1Y,1Y, 2Y, 2Y stc.
			case RisingTerminals:
				String[] atmExpiriesRisingTerminals = { "1Y", "2Y", "3Y", "4Y", "5Y", "7Y", "10Y", "15Y", "20Y", "25Y",
						"30Y" };

				String[] atmTenorsRisingTerminals = { "1Y", "2Y", "3Y", "4Y", "5Y", "7Y", "10Y", "15Y", "20Y", "25Y",
						"30Y" };

				double[] atmVolatilitiesRisingTerminals = getVolatilitiesForExpiriesAndTenors(
						atmExpiriesRisingTerminals, atmTenorsRisingTerminals, ExpiriesFullSurface, TenorsFullSurface,
						VolatilitiesFullSurface);

				this.atmExpiries = atmExpiriesRisingTerminals;
				this.atmTenors = atmTenorsRisingTerminals;
				this.atmVolatilities = atmVolatilitiesRisingTerminals;

				break;
			// 10,11 and 12 year Co-Terminals
			case ExtendedCoTermindals:
				String[] atmExpiriesExtendedCoTermindals = { "1Y", "1Y", "2Y", "2Y", "2Y", "3Y", "3Y", "3Y", "4Y", "4Y",
						"4Y", "5Y", "5Y", "5Y", "7Y", "7Y", "7Y", "10Y", "10Y", };

				String[] atmTenorsExtendedCoTermindals = { "9Y", "10Y", "8Y", "9Y", "10Y", "7Y", "8Y", "9Y", "6Y", "7Y",
						"8Y", "5Y", "6Y", "7Y", "3Y", "4Y", "5Y", "1Y", "2Y" };

				double[] atmVolatilitiesExtendedCoTermindals = getVolatilitiesForExpiriesAndTenors(
						atmExpiriesExtendedCoTermindals, atmTenorsExtendedCoTermindals, ExpiriesFullSurface,
						TenorsFullSurface, VolatilitiesFullSurface);

				this.atmExpiries = atmExpiriesExtendedCoTermindals;
				this.atmTenors = atmTenorsExtendedCoTermindals;
				this.atmVolatilities = atmVolatilitiesExtendedCoTermindals;
				break;

			default:
				break;

			}
			break;

		case Market24_10_2019:

			switch (dataScope2) {
			case FullSurface:
				// Full Volatilityy
				String[] atmExpiries3 = { "1M", "1M", "1M", "1M", "1M", "1M", "1M", "1M", "1M", "1M", "1M", "1M", "1M",
						"1M", "3M", "3M", "3M", "3M", "3M", "3M", "3M", "3M", "3M", "3M", "3M", "3M", "3M", "3M", "6M",
						"6M", "6M", "6M", "6M", "6M", "6M", "6M", "6M", "6M", "6M", "6M", "6M", "6M", "1Y", "1Y", "1Y",
						"1Y", "1Y", "1Y", "1Y", "1Y", "1Y", "1Y", "1Y", "1Y", "1Y", "1Y", "2Y", "2Y", "2Y", "2Y", "2Y",
						"2Y", "2Y", "2Y", "2Y", "2Y", "2Y", "2Y", "2Y", "2Y", "3Y", "3Y", "3Y", "3Y", "3Y", "3Y", "3Y",
						"3Y", "3Y", "3Y", "3Y", "3Y", "3Y", "3Y", "4Y", "4Y", "4Y", "4Y", "4Y", "4Y", "4Y", "4Y", "4Y",
						"4Y", "4Y", "4Y", "4Y", "4Y", "5Y", "5Y", "5Y", "5Y", "5Y", "5Y", "5Y", "5Y", "5Y", "5Y", "5Y",
						"5Y", "5Y", "5Y", "7Y", "7Y", "7Y", "7Y", "7Y", "7Y", "7Y", "7Y", "7Y", "7Y", "7Y", "7Y", "7Y",
						"7Y", "10Y", "10Y", "10Y", "10Y", "10Y", "10Y", "10Y", "10Y", "10Y", "10Y", "10Y", "10Y", "10Y",
						"10Y", "15Y", "15Y", "15Y", "15Y", "15Y", "15Y", "15Y", "15Y", "15Y", "15Y", "15Y", "15Y",
						"15Y", "15Y", "20Y", "20Y", "20Y", "20Y", "20Y", "20Y", "20Y", "20Y", "20Y", "20Y", "20Y",
						"20Y", "20Y", "20Y", "25Y", "25Y", "25Y", "25Y", "25Y", "25Y", "25Y", "25Y", "25Y", "25Y",
						"25Y", "25Y", "25Y", "25Y", "30Y", "30Y", "30Y", "30Y", "30Y", "30Y", "30Y", "30Y", "30Y",
						"30Y", "30Y", "30Y", "30Y", "30Y" };
				String[] atmTenors3 = { "1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "15Y", "20Y",
						"25Y", "30Y", "1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "15Y", "20Y", "25Y",
						"30Y", "1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "15Y", "20Y", "25Y", "30Y",
						"1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "15Y", "20Y", "25Y", "30Y", "1Y",
						"2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "15Y", "20Y", "25Y", "30Y", "1Y", "2Y",
						"3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "15Y", "20Y", "25Y", "30Y", "1Y", "2Y", "3Y",
						"4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "15Y", "20Y", "25Y", "30Y", "1Y", "2Y", "3Y", "4Y",
						"5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "15Y", "20Y", "25Y", "30Y", "1Y", "2Y", "3Y", "4Y", "5Y",
						"6Y", "7Y", "8Y", "9Y", "10Y", "15Y", "20Y", "25Y", "30Y", "1Y", "2Y", "3Y", "4Y", "5Y", "6Y",
						"7Y", "8Y", "9Y", "10Y", "15Y", "20Y", "25Y", "30Y", "1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y",
						"8Y", "9Y", "10Y", "15Y", "20Y", "25Y", "30Y", "1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y",
						"9Y", "10Y", "15Y", "20Y", "25Y", "30Y", "1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y",
						"10Y", "15Y", "20Y", "25Y", "30Y", "1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y",
						"15Y", "20Y", "25Y", "30Y" };
				double[] atmLogVolatilities3 = { 0.006, 0.0115, 0.0149, 0.0193, 0.0212, 0.0244, 0.0256, 0.0281, 0.0307,
						0.0333, 0.0336, 0.0339, 0.0339, 0.0345, 0.0065, 0.0124, 0.0146, 0.0177, 0.0189, 0.0215, 0.024,
						0.0265, 0.0289, 0.0315, 0.0314, 0.0319, 0.0317, 0.0327, 0.0066, 0.0122, 0.0141, 0.0167, 0.0182,
						0.0206, 0.0226, 0.0249, 0.0272, 0.0295, 0.0293, 0.0294, 0.0297, 0.0301, 0.0074, 0.0129, 0.0151,
						0.0174, 0.0185, 0.0207, 0.0223, 0.0243, 0.0263, 0.0284, 0.0277, 0.0279, 0.0282, 0.0288, 0.0091,
						0.0154, 0.0172, 0.0187, 0.0195, 0.0212, 0.0224, 0.0241, 0.0258, 0.0277, 0.0267, 0.0267, 0.027,
						0.0277, 0.0113, 0.018, 0.0191, 0.0202, 0.0207, 0.0215, 0.023, 0.0241, 0.0255, 0.0271, 0.0259,
						0.026, 0.0264, 0.0269, 0.0133, 0.0199, 0.0208, 0.0212, 0.0213, 0.0217, 0.0229, 0.0241, 0.0253,
						0.0264, 0.0254, 0.0254, 0.0257, 0.0262, 0.0144, 0.0208, 0.0208, 0.0213, 0.0214, 0.0218, 0.0227,
						0.0237, 0.0248, 0.0259, 0.0249, 0.0251, 0.0252, 0.0257, 0.0154, 0.0213, 0.0211, 0.0213, 0.0211,
						0.0214, 0.0221, 0.0229, 0.0239, 0.0251, 0.0245, 0.0247, 0.0248, 0.0252, 0.0157, 0.0212, 0.021,
						0.0209, 0.0207, 0.0211, 0.0218, 0.0227, 0.0236, 0.0249, 0.0247, 0.0249, 0.0251, 0.0255, 0.015,
						0.0205, 0.0205, 0.0208, 0.0208, 0.0213, 0.0222, 0.0231, 0.0244, 0.0257, 0.0256, 0.0257, 0.0257,
						0.0258, 0.0154, 0.0217, 0.0221, 0.022, 0.0221, 0.0228, 0.0237, 0.0249, 0.0262, 0.0278, 0.0275,
						0.0272, 0.0268, 0.0266, 0.0163, 0.0244, 0.0248, 0.0248, 0.0248, 0.0257, 0.0269, 0.0281, 0.0298,
						0.0319, 0.0311, 0.03, 0.0287, 0.0271 };

				this.swapPeriodLength = 0.5;
				this.atmExpiries = atmExpiries3;
				this.atmTenors = atmTenors3;
				this.atmVolatilities = atmLogVolatilities3;
				this.targetVolatilityType = "VOLATILITYLOGNORMAL";
				this.referenceDate = LocalDate.of(2019, Month.OCTOBER, 24); // 24.10.2019
				this.cal = new BusinessdayCalendarExcludingTARGETHolidays();
				this.modelDC = new DayCountConvention_ACT_365();
				break;

			default:
				System.out.println("data Scope not available");
				break;

			}
			break;

		case Market23_10_2019:

			// First define the whole volatility surface
			double[] atmLogShiftedVolatilities23102019 = { 0.0068, 0.007, 0.0069, 0.0078, 0.0094, 0.0116, 0.0135,
					0.0148, 0.0158, 0.0158, 0.0152, 0.0157, 0.0164, 0.0128, 0.0132, 0.0128, 0.0135, 0.0158, 0.0185,
					0.0202, 0.0214, 0.0218, 0.0215, 0.0208, 0.0219, 0.0247, 0.0163, 0.0152, 0.0147, 0.0157, 0.0176,
					0.0195, 0.0208, 0.0213, 0.0215, 0.0213, 0.0208, 0.0221, 0.0248, 0.0193, 0.0177, 0.0167, 0.0174,
					0.0187, 0.0201, 0.0212, 0.0212, 0.0213, 0.021, 0.0208, 0.022, 0.0248, 0.0212, 0.0189, 0.0182,
					0.0185, 0.0195, 0.0207, 0.0212, 0.0213, 0.0211, 0.0208, 0.0208, 0.022, 0.0248, 0.0244, 0.0215,
					0.0206, 0.0207, 0.0211, 0.0219, 0.0222, 0.0221, 0.0217, 0.0214, 0.0217, 0.0232, 0.026, 0.0275,
					0.0245, 0.0233, 0.023, 0.0228, 0.0234, 0.0233, 0.023, 0.0225, 0.0222, 0.0225, 0.0242, 0.0272,
					0.0303, 0.0271, 0.0256, 0.025, 0.0245, 0.0245, 0.0246, 0.0241, 0.0233, 0.0231, 0.0235, 0.0254,
					0.0286, 0.0331, 0.0295, 0.028, 0.0271, 0.0263, 0.026, 0.0258, 0.0252, 0.0244, 0.0241, 0.0248,
					0.0267, 0.0302, 0.0359, 0.0322, 0.0303, 0.0293, 0.0282, 0.0275, 0.027, 0.0264, 0.0256, 0.0254,
					0.0262, 0.0283, 0.0324, 0.0362, 0.0321, 0.0302, 0.0286, 0.0274, 0.0265, 0.026, 0.0254, 0.025,
					0.0252, 0.0261, 0.0281, 0.0315, 0.0365, 0.0327, 0.0304, 0.0289, 0.0275, 0.0267, 0.0261, 0.0256,
					0.0252, 0.0255, 0.0262, 0.0277, 0.0304, 0.0366, 0.0325, 0.0307, 0.0292, 0.0278, 0.027, 0.0263,
					0.0258, 0.0253, 0.0256, 0.0261, 0.0273, 0.0291, 0.0373, 0.0335, 0.0311, 0.0298, 0.0286, 0.0276,
					0.0268, 0.0262, 0.0257, 0.0259, 0.0262, 0.027, 0.0274 };

			double[] atmShift23102019 = { 0.00003, 0.00003, 0.00003, 0.00003, 0.00003, 0.00003, 0.00003, 0.00003,
					0.00003, 0.00003, 0.00003, 0.00003, 0.00003, 0.00002, 0.00002, 0.00002, 0.00002, 0.00002, 0.00002,
					0.00002, 0.00002, 0.00002, 0.00002, 0.00002, 0.00002, 0.00002, 0.00002, 0.00002, 0.00002, 0.00002,
					0.00002, 0.00002, 0.00002, 0.00002, 0.00002, 0.00002, 0.00002, 0.00002, 0.00002, 0.00002, 0.00002,
					0.00002, 0.00002, 0.00002, 0.00002, 0.00002, 0.00002, 0.00002, 0.00002, 0.00002, 0.00002, 0.00002,
					0.00002, 0.00002, 0.00002, 0.00002, 0.00002, 0.00002, 0.00002, 0.00002, 0.00002, 0.00002, 0.00002,
					0.00002, 0.00002, 0.000019, 0.000019, 0.000019, 0.000019, 0.000019, 0.000019, 0.000019, 0.000019,
					0.000019, 0.000019, 0.000019, 0.000019, 0.000019, 0.000018, 0.000018, 0.000018, 0.000018, 0.000018,
					0.000018, 0.000018, 0.000018, 0.000018, 0.000018, 0.000018, 0.000018, 0.000018, 0.000017, 0.000017,
					0.000017, 0.000017, 0.000017, 0.000017, 0.000017, 0.000017, 0.000017, 0.000017, 0.000017, 0.000017,
					0.000017, 0.000016, 0.000016, 0.000016, 0.000016, 0.000016, 0.000016, 0.000016, 0.000016, 0.000016,
					0.000016, 0.000016, 0.000016, 0.000016, 0.000015, 0.000015, 0.000015, 0.000015, 0.000015, 0.000015,
					0.000015, 0.000015, 0.000015, 0.000015, 0.000015, 0.000015, 0.000015, 0.000015, 0.000015, 0.000015,
					0.000015, 0.000015, 0.000015, 0.000015, 0.000015, 0.000015, 0.000015, 0.000015, 0.000015, 0.000015,
					0.000015, 0.000015, 0.000015, 0.000015, 0.000015, 0.000015, 0.000015, 0.000015, 0.000015, 0.000015,
					0.000015, 0.000015, 0.000015, 0.000015, 0.000015, 0.000015, 0.000015, 0.000015, 0.000015, 0.000015,
					0.000015, 0.000015, 0.000015, 0.000015, 0.000015, 0.000015, 0.000015, 0.000015, 0.000015, 0.000015,
					0.000015, 0.000015, 0.000015, 0.000015, 0.000015, 0.000015, 0.000015, 0.000015, 0.000015 };

			switch (dataScope2) {
			case FullSurface:
				// Full Volatilityy
				String[] atmExpiries23102019 = { "1M", "3M", "6M", "1Y", "2Y", "3Y", "4Y", "5Y", "7Y", "10Y", "15Y",
						"20Y", "30Y", "1M", "3M", "6M", "1Y", "2Y", "3Y", "4Y", "5Y", "7Y", "10Y", "15Y", "20Y", "30Y",
						"1M", "3M", "6M", "1Y", "2Y", "3Y", "4Y", "5Y", "7Y", "10Y", "15Y", "20Y", "30Y", "1M", "3M",
						"6M", "1Y", "2Y", "3Y", "4Y", "5Y", "7Y", "10Y", "15Y", "20Y", "30Y", "1M", "3M", "6M", "1Y",
						"2Y", "3Y", "4Y", "5Y", "7Y", "10Y", "15Y", "20Y", "30Y", "1M", "3M", "6M", "1Y", "2Y", "3Y",
						"4Y", "5Y", "7Y", "10Y", "15Y", "20Y", "30Y", "1M", "3M", "6M", "1Y", "2Y", "3Y", "4Y", "5Y",
						"7Y", "10Y", "15Y", "20Y", "30Y", "1M", "3M", "6M", "1Y", "2Y", "3Y", "4Y", "5Y", "7Y", "10Y",
						"15Y", "20Y", "30Y", "1M", "3M", "6M", "1Y", "2Y", "3Y", "4Y", "5Y", "7Y", "10Y", "15Y", "20Y",
						"30Y", "1M", "3M", "6M", "1Y", "2Y", "3Y", "4Y", "5Y", "7Y", "10Y", "15Y", "20Y", "30Y", "1M",
						"3M", "6M", "1Y", "2Y", "3Y", "4Y", "5Y", "7Y", "10Y", "15Y", "20Y", "30Y", "1M", "3M", "6M",
						"1Y", "2Y", "3Y", "4Y", "5Y", "7Y", "10Y", "15Y", "20Y", "30Y", "1M", "3M", "6M", "1Y", "2Y",
						"3Y", "4Y", "5Y", "7Y", "10Y", "15Y", "20Y", "30Y", "1M", "3M", "6M", "1Y", "2Y", "3Y", "4Y",
						"5Y", "7Y", "10Y", "15Y", "20Y", "30Y"};
				String[] atmTenors23102019 = { "1Y", "1Y", "1Y", "1Y", "1Y", "1Y", "1Y", "1Y", "1Y", "1Y", "1Y", "1Y",
						"1Y", "2Y", "2Y", "2Y", "2Y", "2Y", "2Y", "2Y", "2Y", "2Y", "2Y", "2Y", "2Y", "2Y", "3Y", "3Y",
						"3Y", "3Y", "3Y", "3Y", "3Y", "3Y", "3Y", "3Y", "3Y", "3Y", "3Y", "4Y", "4Y", "4Y", "4Y", "4Y",
						"4Y", "4Y", "4Y", "4Y", "4Y", "4Y", "4Y", "4Y", "5Y", "5Y", "5Y", "5Y", "5Y", "5Y", "5Y", "5Y",
						"5Y", "5Y", "5Y", "5Y", "5Y", "6Y", "6Y", "6Y", "6Y", "6Y", "6Y", "6Y", "6Y", "6Y", "6Y", "6Y",
						"6Y", "6Y", "7Y", "7Y", "7Y", "7Y", "7Y", "7Y", "7Y", "7Y", "7Y", "7Y", "7Y", "7Y", "7Y", "8Y",
						"8Y", "8Y", "8Y", "8Y", "8Y", "8Y", "8Y", "8Y", "8Y", "8Y", "8Y", "8Y", "9Y", "9Y", "9Y", "9Y",
						"9Y", "9Y", "9Y", "9Y", "9Y", "9Y", "9Y", "9Y", "9Y", "10Y", "10Y", "10Y", "10Y", "10Y", "10Y",
						"10Y", "10Y", "10Y", "10Y", "10Y", "10Y", "10Y", "15Y", "15Y", "15Y", "15Y", "15Y", "15Y",
						"15Y", "15Y", "15Y", "15Y", "15Y", "15Y", "15Y", "20Y", "20Y", "20Y", "20Y", "20Y", "20Y",
						"20Y", "20Y", "20Y", "20Y", "20Y", "20Y", "20Y", "25Y", "25Y", "25Y", "25Y", "25Y", "25Y",
						"25Y", "25Y", "25Y", "25Y", "25Y", "25Y", "25Y", "30Y", "30Y", "30Y", "30Y", "30Y", "30Y",
						"30Y", "30Y", "30Y", "30Y", "30Y", "30Y", "30Y" };

				this.swapPeriodLength = 0.5;
				this.atmExpiries = atmExpiries23102019;
				this.atmTenors = atmTenors23102019;
				
		
				
				this.atmVolatilities = atmLogShiftedVolatilities23102019;
				this.targetVolatilityType = "VOLATILITYLOGNORMAL";
				this.referenceDate = LocalDate.of(2019, Month.OCTOBER, 24); // 24.10.2019
				this.cal = new BusinessdayCalendarExcludingTARGETHolidays();
				this.modelDC = new DayCountConvention_ACT_365();

				break;

			default:
				System.out.println("data Scope not available");
				break;

			}
			break;
		default:
			System.out.println("data Source not available.");
			break;
		}
	}

	private double[] getVolatilitiesForExpiriesAndTenors(String[] Expiries, String[] Tenors,
			String[] expiriesFullSurface, String[] tenorsFullSurface, double[] volatilitiesFullSurface) {

		double[] volatilities = new double[Expiries.length];

		// ExperiesLoop
		for (int j = 0; j < Expiries.length; j++) {

			// Surface Experies Loop
			for (int i = 0; i < expiriesFullSurface.length; i++) {

				if ((expiriesFullSurface[i] == Expiries[j]) & (tenorsFullSurface[i] == Tenors[j])) {
					volatilities[j] = volatilitiesFullSurface[i];
					continue;
				}
			}
		}
		return volatilities;
	}

	/**
	 * Set all the parameters
	 * 
	 * @param swapPeriodLength
	 * @param atmExpiries
	 * @param atmTenors
	 * @param atmNormalVolatilities
	 * @param targetVolatilityType
	 * @param referenceDate
	 * @param cal                   (BusinessdayCalendarExcludingTARGETHolidays)
	 * @param modelDC
	 * @param dataName
	 */
	public CalibrationInformation(double swapPeriodLength, String[] atmExpiries, String[] atmTenors,
			double[] atmNormalVolatilities, String targetVolatilityType, LocalDate referenceDate,
			BusinessdayCalendarExcludingTARGETHolidays cal, DayCountConvention modelDC, String dataName) {
		this.swapPeriodLength = swapPeriodLength;
		this.atmExpiries = atmExpiries;
		this.atmTenors = atmTenors;
		this.atmVolatilities = atmNormalVolatilities;
		this.targetVolatilityType = targetVolatilityType;
		this.referenceDate = referenceDate;
		this.cal = cal;
		this.modelDC = modelDC;
		this.DataName = dataName;

	}

	public double getSwapPeriodLength() {
		return swapPeriodLength;
	}

	public String[] getAtmExpiries() {
		return atmExpiries;
	}

	public String[] getAtmTenors() {
		return atmTenors;
	}

	public double[] getAtmNormalVolatilities() {
		return atmVolatilities;
	}

	public String getTargetVolatilityType() {
		return targetVolatilityType;
	}

	public LocalDate getReferenceDate() {
		return referenceDate;
	}

	public BusinessdayCalendarExcludingTARGETHolidays getCal() {
		return cal;
	}

	public DayCountConvention getModelDC() {
		return modelDC;
	}

	public DataScope getDataScope() {
		return dataScope;
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	/**
	 * Get the name of the calibration data set: <br>
	 * dataScope + dataSource
	 * 
	 * @return
	 */
	public String getName() {
		return DataName;
	}

}
