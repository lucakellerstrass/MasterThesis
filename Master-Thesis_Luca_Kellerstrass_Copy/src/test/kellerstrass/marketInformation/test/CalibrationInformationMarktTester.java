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
public class CalibrationInformationMarktTester {

	public static void main(String[] args) throws SolverException {

		
		String[] dataDays = {"12.09.2018", "13.09.2018","14.09.2018", "17.09.2018"};
		
		
		//Go through the data days and look if the calibration Information is loaded correctly 
		for (String day : dataDays ) {
			CalibrationInformation calibrationInformation = new CalibrationInformation(DataScope.FullSurface,
					day);
			printCalibrationInformation(calibrationInformation);
		}
	
		
		
		

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
