package kellerstrass.marketInformation;

import java.time.LocalDate;
import java.time.Month;

import kellerstrass.ModelCalibration.CurveModelCalibrationMachine;
import kellerstrass.useful.StringToUseful;
import net.finmath.functions.AnalyticFormulas;
import net.finmath.marketdata.model.AnalyticModel;
import net.finmath.marketdata.model.curves.ForwardCurve;
import net.finmath.optimizer.SolverException;
import net.finmath.time.Schedule;
import net.finmath.time.SchedulePrototype;
import net.finmath.time.ScheduleGenerator.DaycountConvention;
import net.finmath.time.ScheduleGenerator.Frequency;
import net.finmath.time.ScheduleGenerator.ShortPeriodConvention;
import net.finmath.time.businessdaycalendar.BusinessdayCalendar;
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

	private String[] atmExpiriesFullSurface = null;
	private String[] atmTenorsFullSurface = null;
	private double[] atmVolatilitiesFullSurface = null;

	private String targetVolatilityType; // "VOLATILITYNORMAL";
	private LocalDate referenceDate = null;
	private BusinessdayCalendarExcludingTARGETHolidays cal = null;
	private DayCountConvention modelDC = null;

	private DataScope dataScope; // Co-terminals, extended Co-terminals, full surface
	private DataSource dataSource; // Example, true market Data, (Date)

	private CurveModelCalibrationMachine curveModelCalibrationMaschine;

	private String DataName;

	/**
	 * Constructs an instance of calibration Information using the data scope and
	 * data source Information <br>
	 * 
	 * @param dataScope  || Co-terminals, extended Co-terminals, full surface; if
	 *                   null, Co-Terminals
	 * @param dataSource || example data, true market Data; if null example data is
	 *                   used
	 * @throws SolverException
	 */
	public CalibrationInformation(DataScope dataScope, DataSource dataSource) throws SolverException {

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

		fillDataSurfaces(dataSource);

		changeForDataScope(dataScope);

		DataName = dataSource.toString() + dataScope.toString();

	}

	private void changeForDataScope(DataScope dataScope2) {
		switch (dataScope2) {

		case FullSurface:
			this.atmExpiries = this.atmExpiriesFullSurface;
			this.atmTenors = this.atmTenorsFullSurface;
			this.atmVolatilities = this.atmVolatilitiesFullSurface;
			break;

		case CoTerminals:
			String[] atmExpiriesCoTerminals = { "1Y", "2Y", "3Y", "4Y", "5Y", "7Y", "10Y" };
			String[] atmTenorsCoTerminals = { "10Y", "9Y", "8Y", "7Y", "6Y", "4Y", "1Y" };
			double[] atmVolatilitiesCoTerminals = getVolatilitiesForExpiriesAndTenors(atmExpiriesCoTerminals,
					atmTenorsCoTerminals, this.atmExpiriesFullSurface, this.atmTenorsFullSurface,
					this.atmVolatilitiesFullSurface);

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

			double[] atmVolatilitiesRisingTerminals = getVolatilitiesForExpiriesAndTenors(atmExpiriesRisingTerminals,
					atmTenorsRisingTerminals, this.atmExpiriesFullSurface, this.atmTenorsFullSurface,
					this.atmVolatilitiesFullSurface);

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
					atmExpiriesExtendedCoTermindals, atmTenorsExtendedCoTermindals, this.atmExpiriesFullSurface,
					this.atmTenorsFullSurface, this.atmVolatilitiesFullSurface);

			this.atmExpiries = atmExpiriesExtendedCoTermindals;
			this.atmTenors = atmTenorsExtendedCoTermindals;
			this.atmVolatilities = atmVolatilitiesExtendedCoTermindals;
			break;

		default:
			System.out.println("The data scope has the implementation.");
			break;

		}

	}

	/**
	 * Fill the Data Vectors with information based on the data scope and data
	 * source. Not all combinations will work
	 * 
	 * @param dataScope2
	 * @param dataSource2
	 * @throws SolverException
	 */
	private void fillDataSurfaces(DataSource dataSource2) throws SolverException {

		switch (dataSource2) {

		case EXAMPLE:
			this.swapPeriodLength = 0.5;
			this.targetVolatilityType = "VOLATILITYNORMAL";
			this.referenceDate = LocalDate.of(2019, Month.OCTOBER, 24); // 24.10.2019
			this.cal = new BusinessdayCalendarExcludingTARGETHolidays();
			this.modelDC = new DayCountConvention_ACT_365();

			String[] ExampleExpiriesFullSurface = { "1M", "1M", "1M", "1M", "1M", "1M", "1M", "1M", "1M", "1M", "1M",
					"1M", "1M", "1M", "3M", "3M", "3M", "3M", "3M", "3M", "3M", "3M", "3M", "3M", "3M", "3M", "3M",
					"3M", "6M", "6M", "6M", "6M", "6M", "6M", "6M", "6M", "6M", "6M", "6M", "6M", "6M", "6M", "1Y",
					"1Y", "1Y", "1Y", "1Y", "1Y", "1Y", "1Y", "1Y", "1Y", "1Y", "1Y", "1Y", "1Y", "2Y", "2Y", "2Y",
					"2Y", "2Y", "2Y", "2Y", "2Y", "2Y", "2Y", "2Y", "2Y", "2Y", "2Y", "3Y", "3Y", "3Y", "3Y", "3Y",
					"3Y", "3Y", "3Y", "3Y", "3Y", "3Y", "3Y", "3Y", "3Y", "4Y", "4Y", "4Y", "4Y", "4Y", "4Y", "4Y",
					"4Y", "4Y", "4Y", "4Y", "4Y", "4Y", "4Y", "5Y", "5Y", "5Y", "5Y", "5Y", "5Y", "5Y", "5Y", "5Y",
					"5Y", "5Y", "5Y", "5Y", "5Y", "7Y", "7Y", "7Y", "7Y", "7Y", "7Y", "7Y", "7Y", "7Y", "7Y", "7Y",
					"7Y", "7Y", "7Y", "10Y", "10Y", "10Y", "10Y", "10Y", "10Y", "10Y", "10Y", "10Y", "10Y", "10Y",
					"10Y", "10Y", "10Y", "15Y", "15Y", "15Y", "15Y", "15Y", "15Y", "15Y", "15Y", "15Y", "15Y", "15Y",
					"15Y", "15Y", "15Y", "20Y", "20Y", "20Y", "20Y", "20Y", "20Y", "20Y", "20Y", "20Y", "20Y", "20Y",
					"20Y", "20Y", "20Y", "25Y", "25Y", "25Y", "25Y", "25Y", "25Y", "25Y", "25Y", "25Y", "25Y", "25Y",
					"25Y", "25Y", "25Y", "30Y", "30Y", "30Y", "30Y", "30Y", "30Y", "30Y", "30Y", "30Y", "30Y", "30Y",
					"30Y", "30Y", "30Y" };
			String[] ExampleTenorsFullSurface = { "1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "15Y",
					"20Y", "25Y", "30Y", "1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "15Y", "20Y",
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
					"30Y" };
			double[] ExampleVolatilitiesFullSurface = { 0.00151, 0.00169, 0.0021, 0.00248, 0.00291, 0.00329, 0.00365,
					0.004, 0.00437, 0.00466, 0.00527, 0.00571, 0.00604, 0.00625, 0.0016, 0.00174, 0.00217, 0.00264,
					0.00314, 0.00355, 0.00398, 0.00433, 0.00469, 0.00493, 0.00569, 0.00607, 0.00627, 0.00645, 0.00182,
					0.00204, 0.00238, 0.00286, 0.00339, 0.00384, 0.00424, 0.00456, 0.00488, 0.0052, 0.0059, 0.00623,
					0.0064, 0.00654, 0.00205, 0.00235, 0.00272, 0.0032, 0.00368, 0.00406, 0.00447, 0.00484, 0.00515,
					0.00544, 0.00602, 0.00629, 0.0064, 0.00646, 0.00279, 0.00319, 0.0036, 0.00396, 0.00436, 0.00469,
					0.00503, 0.0053, 0.00557, 0.00582, 0.00616, 0.00628, 0.00638, 0.00641, 0.00379, 0.00406, 0.00439,
					0.00472, 0.00504, 0.00532, 0.0056, 0.00582, 0.00602, 0.00617, 0.0063, 0.00636, 0.00638, 0.00639,
					0.00471, 0.00489, 0.00511, 0.00539, 0.00563, 0.00583, 0.006, 0.00618, 0.0063, 0.00644, 0.00641,
					0.00638, 0.00635, 0.00634, 0.00544, 0.00557, 0.00572, 0.00591, 0.00604, 0.00617, 0.0063, 0.00641,
					0.00651, 0.00661, 0.00645, 0.00634, 0.00627, 0.00624, 0.00625, 0.00632, 0.00638, 0.00644, 0.0065,
					0.00655, 0.00661, 0.00667, 0.00672, 0.00673, 0.00634, 0.00614, 0.00599, 0.00593, 0.00664, 0.00671,
					0.00675, 0.00676, 0.00676, 0.00675, 0.00676, 0.00674, 0.00672, 0.00669, 0.00616, 0.00586, 0.00569,
					0.00558, 0.00647, 0.00651, 0.00651, 0.00651, 0.00652, 0.00649, 0.00645, 0.0064, 0.00637, 0.00631,
					0.00576, 0.00534, 0.00512, 0.00495, 0.00615, 0.0062, 0.00618, 0.00613, 0.0061, 0.00607, 0.00602,
					0.00596, 0.00591, 0.00586, 0.00536, 0.00491, 0.00469, 0.0045, 0.00578, 0.00583, 0.00579, 0.00574,
					0.00567, 0.00562, 0.00556, 0.00549, 0.00545, 0.00538, 0.00493, 0.00453, 0.00435, 0.0042, 0.00542,
					0.00547, 0.00539, 0.00532, 0.00522, 0.00516, 0.0051, 0.00504, 0.005, 0.00495, 0.00454, 0.00418,
					0.00404, 0.00394 };

			this.atmExpiriesFullSurface = ExampleExpiriesFullSurface;
			this.atmTenorsFullSurface = ExampleTenorsFullSurface;
			this.atmVolatilitiesFullSurface = ExampleVolatilitiesFullSurface;

			break;

		case Market24_10_2019:

			String[] atmExpiriesFullSurface24102019 = { "1M", "3M", "6M", "1Y", "2Y", "3Y", "4Y", "5Y", "7Y", "10Y",
					"15Y", "20Y", "30Y", "1M", "3M", "6M", "1Y", "2Y", "3Y", "4Y", "5Y", "7Y", "10Y", "15Y", "20Y",
					"30Y", "1M", "3M", "6M", "1Y", "2Y", "3Y", "4Y", "5Y", "7Y", "10Y", "15Y", "20Y", "30Y", "1M", "3M",
					"6M", "1Y", "2Y", "3Y", "4Y", "5Y", "7Y", "10Y", "15Y", "20Y", "30Y", "1M", "3M", "6M", "1Y", "2Y",
					"3Y", "4Y", "5Y", "7Y", "10Y", "15Y", "20Y", "30Y", "1M", "3M", "6M", "1Y", "2Y", "3Y", "4Y", "5Y",
					"7Y", "10Y", "15Y", "20Y", "30Y", "1M", "3M", "6M", "1Y", "2Y", "3Y", "4Y", "5Y", "7Y", "10Y",
					"15Y", "20Y", "30Y", "1M", "3M", "6M", "1Y", "2Y", "3Y", "4Y", "5Y", "7Y", "10Y", "15Y", "20Y",
					"30Y", "1M", "3M", "6M", "1Y", "2Y", "3Y", "4Y", "5Y", "7Y", "10Y", "15Y", "20Y", "30Y", "1M", "3M",
					"6M", "1Y", "2Y", "3Y", "4Y", "5Y", "7Y", "10Y", "15Y", "20Y", "30Y", "1M", "3M", "6M", "1Y", "2Y",
					"3Y", "4Y", "5Y", "7Y", "10Y", "15Y", "20Y", "30Y", "1M", "3M", "6M", "1Y", "2Y", "3Y", "4Y", "5Y",
					"7Y", "10Y", "15Y", "20Y", "30Y", "1M", "3M", "6M", "1Y", "2Y", "3Y", "4Y", "5Y", "7Y", "10Y",
					"15Y", "20Y", "30Y", "1M", "3M", "6M", "1Y", "2Y", "3Y", "4Y", "5Y", "7Y", "10Y", "15Y", "20Y",
					"30Y" };

			String[] atmTenorsFullSurface24102019 = { "1Y", "1Y", "1Y", "1Y", "1Y", "1Y", "1Y", "1Y", "1Y", "1Y", "1Y",
					"1Y", "1Y", "2Y", "2Y", "2Y", "2Y", "2Y", "2Y", "2Y", "2Y", "2Y", "2Y", "2Y", "2Y", "2Y", "3Y",
					"3Y", "3Y", "3Y", "3Y", "3Y", "3Y", "3Y", "3Y", "3Y", "3Y", "3Y", "3Y", "4Y", "4Y", "4Y", "4Y",
					"4Y", "4Y", "4Y", "4Y", "4Y", "4Y", "4Y", "4Y", "4Y", "5Y", "5Y", "5Y", "5Y", "5Y", "5Y", "5Y",
					"5Y", "5Y", "5Y", "5Y", "5Y", "5Y", "6Y", "6Y", "6Y", "6Y", "6Y", "6Y", "6Y", "6Y", "6Y", "6Y",
					"6Y", "6Y", "6Y", "7Y", "7Y", "7Y", "7Y", "7Y", "7Y", "7Y", "7Y", "7Y", "7Y", "7Y", "7Y", "7Y",
					"8Y", "8Y", "8Y", "8Y", "8Y", "8Y", "8Y", "8Y", "8Y", "8Y", "8Y", "8Y", "8Y", "9Y", "9Y", "9Y",
					"9Y", "9Y", "9Y", "9Y", "9Y", "9Y", "9Y", "9Y", "9Y", "9Y", "10Y", "10Y", "10Y", "10Y", "10Y",
					"10Y", "10Y", "10Y", "10Y", "10Y", "10Y", "10Y", "10Y", "15Y", "15Y", "15Y", "15Y", "15Y", "15Y",
					"15Y", "15Y", "15Y", "15Y", "15Y", "15Y", "15Y", "20Y", "20Y", "20Y", "20Y", "20Y", "20Y", "20Y",
					"20Y", "20Y", "20Y", "20Y", "20Y", "20Y", "25Y", "25Y", "25Y", "25Y", "25Y", "25Y", "25Y", "25Y",
					"25Y", "25Y", "25Y", "25Y", "25Y", "30Y", "30Y", "30Y", "30Y", "30Y", "30Y", "30Y", "30Y", "30Y",
					"30Y", "30Y", "30Y", "30Y" };
			double[] atmShiftedLogVolatilitiesFullSurface24102019 = { 0.06, 0.115, 0.149, 0.193, 0.212, 0.244, 0.256,
					0.281, 0.307, 0.333, 0.336, 0.339, 0.339, 0.345, 0.065, 0.124, 0.146, 0.177, 0.189, 0.215, 0.24,
					0.265, 0.289, 0.315, 0.314, 0.319, 0.317, 0.327, 0.066, 0.122, 0.141, 0.167, 0.182, 0.206, 0.226,
					0.249, 0.272, 0.295, 0.293, 0.294, 0.297, 0.301, 0.074, 0.129, 0.151, 0.174, 0.185, 0.207, 0.223,
					0.243, 0.263, 0.284, 0.277, 0.279, 0.282, 0.288, 0.091, 0.154, 0.172, 0.187, 0.195, 0.212, 0.224,
					0.241, 0.258, 0.277, 0.267, 0.267, 0.27, 0.277, 0.113, 0.18, 0.191, 0.202, 0.207, 0.215, 0.23,
					0.241, 0.255, 0.271, 0.259, 0.26, 0.264, 0.269, 0.133, 0.199, 0.208, 0.212, 0.213, 0.217, 0.229,
					0.241, 0.253, 0.264, 0.254, 0.254, 0.257, 0.262, 0.144, 0.208, 0.208, 0.213, 0.214, 0.218, 0.227,
					0.237, 0.248, 0.259, 0.249, 0.251, 0.252, 0.257, 0.154, 0.213, 0.211, 0.213, 0.211, 0.214, 0.221,
					0.229, 0.239, 0.251, 0.245, 0.247, 0.248, 0.252, 0.157, 0.212, 0.21, 0.209, 0.207, 0.211, 0.218,
					0.227, 0.236, 0.249, 0.247, 0.249, 0.251, 0.255, 0.15, 0.205, 0.205, 0.208, 0.208, 0.213, 0.222,
					0.231, 0.244, 0.257, 0.256, 0.257, 0.257, 0.258, 0.154, 0.217, 0.221, 0.22, 0.221, 0.228, 0.237,
					0.249, 0.262, 0.278, 0.275, 0.272, 0.268, 0.266, 0.163, 0.244, 0.248, 0.248, 0.248, 0.257, 0.269,
					0.281, 0.298, 0.319, 0.311, 0.3, 0.287, 0.271 };

			double[] logVolShiftsFullSurface24102019 = { 0.03, 0.02, 0.02, 0.02, 0.02, 0.019, 0.018, 0.017, 0.016,
					0.015, 0.015, 0.015, 0.015, 0.015, 0.03, 0.02, 0.02, 0.02, 0.02, 0.019, 0.018, 0.017, 0.016, 0.015,
					0.015, 0.015, 0.015, 0.015, 0.03, 0.02, 0.02, 0.02, 0.02, 0.019, 0.018, 0.017, 0.016, 0.015, 0.015,
					0.015, 0.015, 0.015, 0.03, 0.02, 0.02, 0.02, 0.02, 0.019, 0.018, 0.017, 0.016, 0.015, 0.015, 0.015,
					0.015, 0.015, 0.03, 0.02, 0.02, 0.02, 0.02, 0.019, 0.018, 0.017, 0.016, 0.015, 0.015, 0.015, 0.015,
					0.015, 0.03, 0.02, 0.02, 0.02, 0.02, 0.019, 0.018, 0.017, 0.016, 0.015, 0.015, 0.015, 0.015, 0.015,
					0.03, 0.02, 0.02, 0.02, 0.02, 0.019, 0.018, 0.017, 0.016, 0.015, 0.015, 0.015, 0.015, 0.015, 0.03,
					0.02, 0.02, 0.02, 0.02, 0.019, 0.018, 0.017, 0.016, 0.015, 0.015, 0.015, 0.015, 0.015, 0.03, 0.02,
					0.02, 0.02, 0.02, 0.019, 0.018, 0.017, 0.016, 0.015, 0.015, 0.015, 0.015, 0.015, 0.03, 0.02, 0.02,
					0.02, 0.02, 0.019, 0.018, 0.017, 0.016, 0.015, 0.015, 0.015, 0.015, 0.015, 0.03, 0.02, 0.02, 0.02,
					0.02, 0.019, 0.018, 0.017, 0.016, 0.015, 0.015, 0.015, 0.015, 0.015, 0.03, 0.02, 0.02, 0.02, 0.02,
					0.019, 0.018, 0.017, 0.016, 0.015, 0.015, 0.015, 0.015, 0.015, 0.03, 0.02, 0.02, 0.02, 0.02, 0.019,
					0.018, 0.017, 0.016, 0.015, 0.015, 0.015, 0.015, 0.015 };

			// get The corresponding curve Model with discount curve and forward curve
			CurveModelCalibrationMachine curveModelCalibrationMaschineOct24 = new CurveModelCalibrationMachine(
					CurveModelDataType.OIS6M2410);

			AnalyticModel curveModel2410 = curveModelCalibrationMaschineOct24.getCalibratedCurveModel();
			ForwardCurve forwardCurve2410 = curveModel2410
					.getForwardCurve(curveModelCalibrationMaschineOct24.getForwardCurvelName());

			this.swapPeriodLength = 0.5;
			this.referenceDate = LocalDate.of(2019, Month.OCTOBER, 24); // 24.10.2019
			this.cal = new BusinessdayCalendarExcludingTARGETHolidays();
			this.modelDC = new DayCountConvention_ACT_365();

			// transform shifted LogVol into LogVol
			double[] atmNormalVolatilitiesFullSurface24102019 = volatilityConversionShiftedLognormalATMtoNormalATM(
					atmExpiriesFullSurface24102019, atmTenorsFullSurface24102019,
					atmShiftedLogVolatilitiesFullSurface24102019, logVolShiftsFullSurface24102019, curveModel2410,
					forwardCurve2410, swapPeriodLength, referenceDate/* , cal, modelDC */);

			this.atmExpiriesFullSurface = atmExpiriesFullSurface24102019;
			this.atmTenorsFullSurface = atmTenorsFullSurface24102019;
			this.atmVolatilitiesFullSurface = atmNormalVolatilitiesFullSurface24102019;
			this.targetVolatilityType = "VOLATILITYNORMAL";

			break;

		case Market23_10_2019:

			String[] atmExpiriesFullSurface23102019 = { "1M", "3M", "6M", "1Y", "2Y", "3Y", "4Y", "5Y", "7Y", "10Y",
					"15Y", "20Y", "30Y", "1M", "3M", "6M", "1Y", "2Y", "3Y", "4Y", "5Y", "7Y", "10Y", "15Y", "20Y",
					"30Y", "1M", "3M", "6M", "1Y", "2Y", "3Y", "4Y", "5Y", "7Y", "10Y", "15Y", "20Y", "30Y", "1M", "3M",
					"6M", "1Y", "2Y", "3Y", "4Y", "5Y", "7Y", "10Y", "15Y", "20Y", "30Y", "1M", "3M", "6M", "1Y", "2Y",
					"3Y", "4Y", "5Y", "7Y", "10Y", "15Y", "20Y", "30Y", "1M", "3M", "6M", "1Y", "2Y", "3Y", "4Y", "5Y",
					"7Y", "10Y", "15Y", "20Y", "30Y", "1M", "3M", "6M", "1Y", "2Y", "3Y", "4Y", "5Y", "7Y", "10Y",
					"15Y", "20Y", "30Y", "1M", "3M", "6M", "1Y", "2Y", "3Y", "4Y", "5Y", "7Y", "10Y", "15Y", "20Y",
					"30Y", "1M", "3M", "6M", "1Y", "2Y", "3Y", "4Y", "5Y", "7Y", "10Y", "15Y", "20Y", "30Y", "1M", "3M",
					"6M", "1Y", "2Y", "3Y", "4Y", "5Y", "7Y", "10Y", "15Y", "20Y", "30Y", "1M", "3M", "6M", "1Y", "2Y",
					"3Y", "4Y", "5Y", "7Y", "10Y", "15Y", "20Y", "30Y", "1M", "3M", "6M", "1Y", "2Y", "3Y", "4Y", "5Y",
					"7Y", "10Y", "15Y", "20Y", "30Y", "1M", "3M", "6M", "1Y", "2Y", "3Y", "4Y", "5Y", "7Y", "10Y",
					"15Y", "20Y", "30Y", "1M", "3M", "6M", "1Y", "2Y", "3Y", "4Y", "5Y", "7Y", "10Y", "15Y", "20Y",
					"30Y" };

			String[] atmTenorsFullSurface23102019 = { "1Y", "1Y", "1Y", "1Y", "1Y", "1Y", "1Y", "1Y", "1Y", "1Y", "1Y",
					"1Y", "1Y", "2Y", "2Y", "2Y", "2Y", "2Y", "2Y", "2Y", "2Y", "2Y", "2Y", "2Y", "2Y", "2Y", "3Y",
					"3Y", "3Y", "3Y", "3Y", "3Y", "3Y", "3Y", "3Y", "3Y", "3Y", "3Y", "3Y", "4Y", "4Y", "4Y", "4Y",
					"4Y", "4Y", "4Y", "4Y", "4Y", "4Y", "4Y", "4Y", "4Y", "5Y", "5Y", "5Y", "5Y", "5Y", "5Y", "5Y",
					"5Y", "5Y", "5Y", "5Y", "5Y", "5Y", "6Y", "6Y", "6Y", "6Y", "6Y", "6Y", "6Y", "6Y", "6Y", "6Y",
					"6Y", "6Y", "6Y", "7Y", "7Y", "7Y", "7Y", "7Y", "7Y", "7Y", "7Y", "7Y", "7Y", "7Y", "7Y", "7Y",
					"8Y", "8Y", "8Y", "8Y", "8Y", "8Y", "8Y", "8Y", "8Y", "8Y", "8Y", "8Y", "8Y", "9Y", "9Y", "9Y",
					"9Y", "9Y", "9Y", "9Y", "9Y", "9Y", "9Y", "9Y", "9Y", "9Y", "10Y", "10Y", "10Y", "10Y", "10Y",
					"10Y", "10Y", "10Y", "10Y", "10Y", "10Y", "10Y", "10Y", "15Y", "15Y", "15Y", "15Y", "15Y", "15Y",
					"15Y", "15Y", "15Y", "15Y", "15Y", "15Y", "15Y", "20Y", "20Y", "20Y", "20Y", "20Y", "20Y", "20Y",
					"20Y", "20Y", "20Y", "20Y", "20Y", "20Y", "25Y", "25Y", "25Y", "25Y", "25Y", "25Y", "25Y", "25Y",
					"25Y", "25Y", "25Y", "25Y", "25Y", "30Y", "30Y", "30Y", "30Y", "30Y", "30Y", "30Y", "30Y", "30Y",
					"30Y", "30Y", "30Y", "30Y" };
			
			double[] atmShiftedLogVolatilitiesFullSurface23102019 = { 0.068, 0.07, 0.069, 0.078, 0.094, 0.116, 0.135,
					0.148, 0.158, 0.158, 0.152, 0.157, 0.164, 0.128, 0.132, 0.128, 0.135, 0.158, 0.185, 0.202, 0.214,
					0.218, 0.215, 0.208, 0.219, 0.247, 0.163, 0.152, 0.147, 0.157, 0.176, 0.195, 0.208, 0.213, 0.215,
					0.213, 0.208, 0.221, 0.248, 0.193, 0.177, 0.167, 0.174, 0.187, 0.201, 0.212, 0.212, 0.213, 0.21,
					0.208, 0.22, 0.248, 0.212, 0.189, 0.182, 0.185, 0.195, 0.207, 0.212, 0.213, 0.211, 0.208, 0.208,
					0.22, 0.248, 0.244, 0.215, 0.206, 0.207, 0.211, 0.219, 0.222, 0.221, 0.217, 0.214, 0.217, 0.232,
					0.26, 0.275, 0.245, 0.233, 0.23, 0.228, 0.234, 0.233, 0.23, 0.225, 0.222, 0.225, 0.242, 0.272,
					0.303, 0.271, 0.256, 0.25, 0.245, 0.245, 0.246, 0.241, 0.233, 0.231, 0.235, 0.254, 0.286, 0.331,
					0.295, 0.28, 0.271, 0.263, 0.26, 0.258, 0.252, 0.244, 0.241, 0.248, 0.267, 0.302, 0.359, 0.322,
					0.303, 0.293, 0.282, 0.275, 0.27, 0.264, 0.256, 0.254, 0.262, 0.283, 0.324, 0.362, 0.321, 0.302,
					0.286, 0.274, 0.265, 0.26, 0.254, 0.25, 0.252, 0.261, 0.281, 0.315, 0.365, 0.327, 0.304, 0.289,
					0.275, 0.267, 0.261, 0.256, 0.252, 0.255, 0.262, 0.277, 0.304, 0.366, 0.325, 0.307, 0.292, 0.278,
					0.27, 0.263, 0.258, 0.253, 0.256, 0.261, 0.273, 0.291, 0.373, 0.335, 0.311, 0.298, 0.286, 0.276,
					0.268, 0.262, 0.257, 0.259, 0.262, 0.27, 0.274 };

			double[] logVolShiftsFullSurface23102019 = { 0.03, 0.03, 0.03, 0.03, 0.03, 0.03, 0.03, 0.03, 0.03, 0.03,
					0.03, 0.03, 0.03, 0.02, 0.02, 0.02, 0.02, 0.02, 0.02, 0.02, 0.02, 0.02, 0.02, 0.02, 0.02, 0.02,
					0.02, 0.02, 0.02, 0.02, 0.02, 0.02, 0.02, 0.02, 0.02, 0.02, 0.02, 0.02, 0.02, 0.02, 0.02, 0.02,
					0.02, 0.02, 0.02, 0.02, 0.02, 0.02, 0.02, 0.02, 0.02, 0.02, 0.02, 0.02, 0.02, 0.02, 0.02, 0.02,
					0.02, 0.02, 0.02, 0.02, 0.02, 0.02, 0.02, 0.019, 0.019, 0.019, 0.019, 0.019, 0.019, 0.019, 0.019,
					0.019, 0.019, 0.019, 0.019, 0.019, 0.018, 0.018, 0.018, 0.018, 0.018, 0.018, 0.018, 0.018, 0.018,
					0.018, 0.018, 0.018, 0.018, 0.017, 0.017, 0.017, 0.017, 0.017, 0.017, 0.017, 0.017, 0.017, 0.017,
					0.017, 0.017, 0.017, 0.016, 0.016, 0.016, 0.016, 0.016, 0.016, 0.016, 0.016, 0.016, 0.016, 0.016,
					0.016, 0.016, 0.015, 0.015, 0.015, 0.015, 0.015, 0.015, 0.015, 0.015, 0.015, 0.015, 0.015, 0.015,
					0.015, 0.015, 0.015, 0.015, 0.015, 0.015, 0.015, 0.015, 0.015, 0.015, 0.015, 0.015, 0.015, 0.015,
					0.015, 0.015, 0.015, 0.015, 0.015, 0.015, 0.015, 0.015, 0.015, 0.015, 0.015, 0.015, 0.015, 0.015,
					0.015, 0.015, 0.015, 0.015, 0.015, 0.015, 0.015, 0.015, 0.015, 0.015, 0.015, 0.015, 0.015, 0.015,
					0.015, 0.015, 0.015, 0.015, 0.015, 0.015, 0.015, 0.015, 0.015, 0.015, 0.015};

			// get The corresponding curve Model with discount curve and forward curve
			CurveModelCalibrationMachine curveModelCalibrationMaschineOct23 = new CurveModelCalibrationMachine(
					CurveModelDataType.OIS6M2310);

			AnalyticModel curveModel2310 = curveModelCalibrationMaschineOct23.getCalibratedCurveModel();
			ForwardCurve forwardCurve2310 = curveModel2310
					.getForwardCurve(curveModelCalibrationMaschineOct23.getForwardCurvelName());

			this.swapPeriodLength = 0.5;
			this.referenceDate = LocalDate.of(2019, Month.OCTOBER, 23);
			this.cal = new BusinessdayCalendarExcludingTARGETHolidays();
			this.modelDC = new DayCountConvention_ACT_365();

			// transform shifted LogVol into LogVol
			double[] atmNormalVolatilitiesFullSurface23102019 = volatilityConversionShiftedLognormalATMtoNormalATM(
					atmExpiriesFullSurface23102019, atmTenorsFullSurface23102019,
					atmShiftedLogVolatilitiesFullSurface23102019, logVolShiftsFullSurface23102019, curveModel2310,
					forwardCurve2310, swapPeriodLength, referenceDate/* , cal, modelDC */);

			this.atmExpiriesFullSurface = atmExpiriesFullSurface23102019;
			this.atmTenorsFullSurface = atmTenorsFullSurface23102019;
			this.atmVolatilitiesFullSurface = atmNormalVolatilitiesFullSurface23102019;
			this.targetVolatilityType = "VOLATILITYNORMAL";

			break;

			

		default:
			System.out.println("data Source not available.");
			break;
		}

	}

	/**
	 * Convert shifted log normal swaption volas into Normal volas.
	 * 
	 * @param atmExpiries
	 * @param atmTenors
	 * @param atmShiftedLogVolatilities
	 * @param logVolShifts
	 * @param curveModel
	 * @param swapPeriodLength
	 * @param referenceDate
	 * @return
	 */
	private double[] volatilityConversionShiftedLognormalATMtoNormalATM(String[] atmExpiries, String[] atmTenors3,
			double[] atmShiftedLogVolatilities3, double[] logVolShifts, AnalyticModel curveModel,
			ForwardCurve forwardCurve, double swapPeriodLength2,
			LocalDate referenceDate2/*
									 * , BusinessdayCalendarExcludingTARGETHolidays cal2, DayCountConvention
									 * modelDC2
									 */) {

		double[] unshiftetedATMVolatilities = new double[atmExpiries.length];

		// Set properties of input swaptions
		SchedulePrototype fixMetaSchedule = new SchedulePrototype(Frequency.ANNUAL, DaycountConvention.ACT_365,
				ShortPeriodConvention.FIRST, BusinessdayCalendar.DateRollConvention.MODIFIED_FOLLOWING,
				new BusinessdayCalendarExcludingTARGETHolidays(), 0, 0, false);

		SchedulePrototype floatMetaSchedule = new SchedulePrototype(Frequency.SEMIANNUAL, DaycountConvention.ACT_365,
				ShortPeriodConvention.FIRST, BusinessdayCalendar.DateRollConvention.MODIFIED_FOLLOWING,
				new BusinessdayCalendarExcludingTARGETHolidays(), 0, 0, false);

//		System.out.println("referenceDate is " + referenceDate2);
//		System.out.println(
//				"Expiry   \t   ExperyDate   \t   Tenor   \t   TenorDate   \t   swaprate   \t   shifted lognormal vola   \t   shift   \t   normalvola");
		for (int j = 0; j < unshiftetedATMVolatilities.length; j++) {

			LocalDate startDate = StringToUseful.addToDate(referenceDate2, atmExpiries[j]);
			LocalDate endDate = StringToUseful.addToDate(startDate, atmTenors3[j]);

			Schedule fixLegSchedule = fixMetaSchedule.generateSchedule(referenceDate, startDate, endDate);
			Schedule floatLegSchedule = floatMetaSchedule.generateSchedule(referenceDate, startDate, endDate);

			double swaptionMaturity = fixLegSchedule.getDaycountconvention().getDaycountFraction(referenceDate2,
					startDate);

			double swaprate = net.finmath.marketdata.products.Swap.getForwardSwapRate(fixLegSchedule, floatLegSchedule,
					forwardCurve, curveModel);

			unshiftetedATMVolatilities[j] = AnalyticFormulas.volatilityConversionLognormalATMtoNormalATM(swaprate,
					logVolShifts[j], swaptionMaturity, atmShiftedLogVolatilities3[j]);

//			System.out.println(
//					atmExpiries[j]   +"\t" + 
//					startDate   +"\t" + 
//					atmTenors3[j]   +"\t" + 
//					endDate  +"\t" + 
//					swaprate  +"\t" + 
//					atmShiftedLogVolatilities3[j]   +"\t" + 
//					logVolShifts[j]   +"\t" + 
//					unshiftetedATMVolatilities[j]
//							);

		}

		return unshiftetedATMVolatilities;
	}

	/**
	 * Get The corresponding volatilities inside a volatility surface for given
	 * exopiries and tenors. <br>
	 * This is usefull to get eg. Co-terminals from a vola surface
	 * 
	 * @param Expiries
	 * @param Tenors
	 * @param expiriesFullSurface
	 * @param tenorsFullSurface
	 * @param volatilitiesFullSurface
	 * @return
	 */
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
