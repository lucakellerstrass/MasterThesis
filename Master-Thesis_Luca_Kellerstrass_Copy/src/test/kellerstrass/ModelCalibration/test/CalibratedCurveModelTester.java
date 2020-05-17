package kellerstrass.ModelCalibration.test;


import kellerstrass.ModelCalibration.CurveModelCalibrationMaschine;
import kellerstrass.marketInformation.CurveModelDataType;

public class CalibratedCurveModelTester {

	public static void main(String[] args) {


		CurveModelCalibrationMaschine curveModelCalibrationMaschine = new CurveModelCalibrationMaschine(CurveModelDataType.Example);
		System.out.println("Initialization of curveModelCalibrationMaschine worked");
		
		System.out.println("The name of the chosen CurveModelCalibrationMaschine is: "+ curveModelCalibrationMaschine.getCurveModelName());
		

	}

}
