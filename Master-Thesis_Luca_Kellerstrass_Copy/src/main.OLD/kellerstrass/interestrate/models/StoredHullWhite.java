package kellerstrass.interestrate.models;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.time.LocalDate;
import java.time.Month;

import net.finmath.montecarlo.BrownianMotionLazyInit;
import net.finmath.montecarlo.interestrate.LIBORModelMonteCarloSimulationModel;
import net.finmath.montecarlo.interestrate.LIBORMonteCarloSimulationFromLIBORModel;
import net.finmath.montecarlo.interestrate.models.HullWhiteModel;
import net.finmath.montecarlo.process.EulerSchemeFromProcessModel;
import net.finmath.time.TimeDiscretizationFromArray;

public class StoredHullWhite {
	
	
	public static LIBORModelMonteCarloSimulationModel getStoredHullWhite() throws ClassNotFoundException {
		
		String name = "HullWhite";
		
		int numberOfPaths = 10000;
		double lastTime	= 40.0;
		double dt		= 0.1;
		TimeDiscretizationFromArray timeDiscretizationFromArray = new TimeDiscretizationFromArray(0.0, (int) (lastTime / dt), dt);

		BrownianMotionLazyInit brownianMotion = new BrownianMotionLazyInit(timeDiscretizationFromArray, 2 /* numberOfFactors */, numberOfPaths, 3141 /* seed */);
		EulerSchemeFromProcessModel process = new EulerSchemeFromProcessModel(brownianMotion, EulerSchemeFromProcessModel.Scheme.EULER);

		
		// Read stored Hull white
		
		HullWhiteModel hullWhiteModel  = null;
		try {FileInputStream fileIn = new FileInputStream("temp/"+ name+ ".ser");
		ObjectInputStream in = new ObjectInputStream(fileIn);
		hullWhiteModel = (HullWhiteModel)  in.readObject();
		in.close();
		fileIn.close();
			
		} catch (IOException i) {
			i.printStackTrace();
			return null;
		} catch (ClassNotFoundException c) {
			System.out.println("Employee class not found");
			c.printStackTrace();
		}
		
		
       
		
		return new LIBORMonteCarloSimulationFromLIBORModel(hullWhiteModel, process);

		
		
	}

}
