package kellerstrass.marketInformation.test;

import kellerstrass.marketInformation.CalibrationInformation;
import kellerstrass.marketInformation.DataScope;
import kellerstrass.marketInformation.DataSource;
import net.finmath.optimizer.SolverException;

/**
 * This class tests the class CalibrationInformation
 * 
 * @author lucak
 * 
 * @ToDo extend this
 *
 */
public class CalibrationInformationTester {

	public static void main(String[] args) throws SolverException {

		CalibrationInformation calibrationInformation1 = new CalibrationInformation(DataScope.CoTerminals,
				DataSource.Market24_10_2019);
//		CalibrationInformation calibrationInformation2 = new CalibrationInformation(DataScope.ExtendedCoTermindals,
//				DataSource.Market24_10_2019);
//		CalibrationInformation calibrationInformation3 = new CalibrationInformation(DataScope.ExtendedCoTermindals,
//				DataSource.Market23_10_2019);
//		

		printCalibrationInformation(calibrationInformation1);
//		printCalibrationInformation(calibrationInformation2);
//		printCalibrationInformation(calibrationInformation3);

	} 

	/**
	 * This is the toSring method to be for the CalibrationInformation
	 * 
	 * @param calibrationInformation
	 */
	private static void printCalibrationInformation(CalibrationInformation calibrationInformation) {
		System.out.println("The name of the calibration information is " + calibrationInformation.getName() + "\n");

		int length = Math.max(calibrationInformation.getAtmNormalVolatilities().length,
				Math.max(calibrationInformation.getAtmExpiries().length, calibrationInformation.getAtmTenors().length));
		String[] expiries = calibrationInformation.getAtmExpiries();
		String[] tenors = calibrationInformation.getAtmTenors();
		double[] volatilities = calibrationInformation.getAtmNormalVolatilities();

		System.out.println("Expiries " + "\t" + "Tenors " + "\t" + "Volatilities ");
		for (int i = 0; i < length; i++) {
			System.out.println(expiries[i] + "\t" + tenors[i] + "\t" + volatilities[i]);
		}

	}

}
