package kellerstrass.ModelCalibration.test;

import kellerstrass.ModelCalibration.CurveModelCalibrationMachine;
import kellerstrass.marketInformation.CurveModelDataType;

public class CalibratedCurveModelTester {

	public static void main(String[] args) {

		CurveModelCalibrationMachine curveModelCalibrationMaschine = new CurveModelCalibrationMachine(
				CurveModelDataType.Example);
		System.out.println("Initialization of curveModelCalibrationMaschine worked");

		System.out.println("The name of the chosen CurveModelCalibrationMaschine is: "
				+ curveModelCalibrationMaschine.getCurveModelName());

	}

}
