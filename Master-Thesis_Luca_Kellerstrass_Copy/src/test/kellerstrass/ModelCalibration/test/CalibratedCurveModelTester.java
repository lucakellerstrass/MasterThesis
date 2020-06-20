package kellerstrass.ModelCalibration.test;

import kellerstrass.ModelCalibration.CurveModelCalibrationMachine;
import kellerstrass.marketInformation.CurveModelDataType;
import net.finmath.marketdata.model.curves.DiscountCurve;
import net.finmath.optimizer.SolverException;
import net.finmath.time.TimeDiscretizationFromArray;

public class CalibratedCurveModelTester {

	public static void main(String[] args) throws SolverException {

		CurveModelCalibrationMachine curveModelCalibrationMaschineExample = new CurveModelCalibrationMachine(
				CurveModelDataType.Example);
		DiscountCurve exampleDiscountCurve = curveModelCalibrationMaschineExample.getCalibratedCurveModel().getDiscountCurve("discountCurve-" + "EUR");
		
		CurveModelCalibrationMachine curveModelCalibrationMaschineOct24 = new CurveModelCalibrationMachine(
				CurveModelDataType.OIS6M2410);
		DiscountCurve OIS6MDiscountCurve = curveModelCalibrationMaschineOct24.getCalibratedCurveModel().getDiscountCurve("discountCurve-" + "EUR");
	
		// Simulation time discretization
				double lastTime = 40.0;
				double dt = 0.25;
				TimeDiscretizationFromArray timeDiscretizationFromArray = new TimeDiscretizationFromArray(0.0,
						(int) (lastTime / dt), dt);
				
				

				System.out.println(
						"observationDate  \t Example discount curve   \t   24.10.2019 OIS6M ");
				for (double observationDate : timeDiscretizationFromArray) {
					System.out.println(
							observationDate + "   \t   " + 
									exampleDiscountCurve.getDiscountFactor(observationDate) + "   \t   " + 
									OIS6MDiscountCurve.getDiscountFactor(observationDate) + "   \t   "  
					
							);
					
					
					
				}


	}

}
