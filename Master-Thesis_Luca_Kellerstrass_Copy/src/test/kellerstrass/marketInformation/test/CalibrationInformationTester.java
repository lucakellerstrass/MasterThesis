package kellerstrass.marketInfromation.test;

import kellerstrass.marketInformation.CalibrationInformation;
import kellerstrass.marketInformation.DataScope;
import kellerstrass.marketInformation.DataSource;

/**
 * This class tests the class CalibrationInformation
 * 
 * @author lucak
 * 
 * @ToDo extend this
 *
 */
public class CalibrationInformationTester {

	public static void main(String[] args) {

		CalibrationInformation calibrationInformation = new CalibrationInformation(DataScope.CoTerminals,
				DataSource.EXAMPLE);

		System.out.println(calibrationInformation.getName());

	}

}
