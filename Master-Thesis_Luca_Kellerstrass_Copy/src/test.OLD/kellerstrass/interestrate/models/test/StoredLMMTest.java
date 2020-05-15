package kellerstrass.interestrate.models.test;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

import kellerstrass.interestrate.models.StoredLMM;
import net.finmath.exception.CalculationException;
import net.finmath.marketdata.model.AnalyticModel;
import net.finmath.marketdata.model.curves.DiscountCurve;
import net.finmath.marketdata.model.curves.ForwardCurve;
import net.finmath.montecarlo.interestrate.LIBORModelMonteCarloSimulationModel;
import net.finmath.time.TimeDiscretization;

/**
 * This class Tests the serialized Libor Market (Simulation) model.
 * If the is no  serialized LiborMarekt model yet, one have to perform <br>
 * @see kellerstrass.Calibration.CalibrateHullWhite
 * @author lucak
 *
 */
public class StoredLMMTest {
	
	private final static NumberFormat formatter3 = new DecimalFormat("0.000", new DecimalFormatSymbols(new Locale("en")));
	private final static NumberFormat formatter6 = new DecimalFormat("0.000000", new DecimalFormatSymbols(new Locale("en")));
	private final static NumberFormat formatter12 = new DecimalFormat("0.000000000000", new DecimalFormatSymbols(new Locale("en")));


	public static void main(String[] args) throws ClassNotFoundException, CalculationException {
		// TODO Auto-generated method stub

		   //getModel
		LIBORModelMonteCarloSimulationModel simulationModel = StoredLMM.getStoredLMM();
		
		//check Libor time discretization
		TimeDiscretization liborTimeDiscretization = simulationModel.getLiborPeriodDiscretization();
		
		//check simulation time discretization
		TimeDiscretization simulationTimeDiscretization  = simulationModel.getTimeDiscretization();
		
		//check AnalyticModel
		AnalyticModel  curveModel =  simulationModel.getModel().getAnalyticModel();
		
		//check discountcurve
		DiscountCurve discountCurve = curveModel.getDiscountCurve("discountCurve-EUR");
		
		//check forward curve
		ForwardCurve forwardCurve = curveModel.getForwardCurve("ForwardCurveFromDiscountCurve(discountCurve-EUR,6M)");
		
		
		// Now test everything
		System.out.println("Index  \t  Time  \t  P(0,t)  \t  f(0, t)  \t  L(0, t , t+0.5) ");
		for(int index = 0 ; index <= liborTimeDiscretization.getNumberOfTimeSteps() ; index++) {
			double time = liborTimeDiscretization.getTime(index);
			
			System.out.print(index + "\t" + formatter3.format(time) + "\t");
			
			System.out.print( formatter12.format(discountCurve.getDiscountFactor(time))+ "\t");
			
			System.out.print( formatter12.format(forwardCurve.getForward(curveModel, time))+ "\t");
			
			System.out.print( formatter12.format(simulationModel.getLIBOR(0, time, (time + 0.5)).getAverage())+ "\t");
			
			
			System.out.println("");
		}
		
	

}

}