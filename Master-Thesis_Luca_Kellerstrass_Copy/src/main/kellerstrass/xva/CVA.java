package kellerstrass.xva;

import kellerstrass.defaultProbability.bootstrapping.ForwardBootstrap;
import kellerstrass.exposure.ExposureEstimator;
import kellerstrass.useful.PaymentFrequency;
import net.finmath.exception.CalculationException;
import net.finmath.montecarlo.RandomVariableFromDoubleArray;
import net.finmath.montecarlo.interestrate.LIBORModelMonteCarloSimulationModel;
import net.finmath.montecarlo.interestrate.products.AbstractLIBORMonteCarloProduct;
import net.finmath.montecarlo.interestrate.products.TermStructureMonteCarloProduct;
import net.finmath.stochastic.RandomVariable;
import net.finmath.time.TimeDiscretization;

public class CVA {

	private  LIBORModelMonteCarloSimulationModel simulationModel; 
	private  AbstractLIBORMonteCarloProduct swap;   //can be reduced, to just an underlying
	private  TermStructureMonteCarloProduct swapExposureEstimator;
	
	private TimeDiscretization SimulationTimeDiscretization;


	private double recoveryRate;
	
	private double[] cdsSpreads;
	
	private double[] discountCurve;
	
	
	/**
	 * 
	 * @param simulationModel
	 * @param swap
	 * @param recoveryRate
	 * @param cdsSpreads
	 */
	public CVA(LIBORModelMonteCarloSimulationModel simulationModel, AbstractLIBORMonteCarloProduct swap,
			double recoveryRate, double[] cdsSpreads) {
		super();
		this.simulationModel = simulationModel;
		this.swap = swap;
		this.swapExposureEstimator =new ExposureEstimator(swap);
		this.recoveryRate = recoveryRate;
		this.cdsSpreads = cdsSpreads;
		this.SimulationTimeDiscretization = simulationModel.getTimeDiscretization();
		this.discountCurve = getDiscountCurve(simulationModel);
		
	}
	
	
	
	






	/**
	 * This class implements:
	 * @return
	 * @throws Exception 
	 */
	public double getCVA() throws Exception {
	
		ForwardBootstrap bootStrapper = new ForwardBootstrap(discountCurve, recoveryRate, cdsSpreads, PaymentFrequency.MOUNTHLY, SimulationTimeDiscretization);
		
		
		double CVA = 0.0;
		
		for(double observationDate : SimulationTimeDiscretization) {
			if(observationDate == 0) {
				continue;
			}
			
			int timeIndex = SimulationTimeDiscretization.getTimeIndex(observationDate);
			
			/*
			 * Calculate expected positive exposure of a swap
			 */
			RandomVariable valuesSwap = swap.getValue(observationDate, simulationModel);
			RandomVariable valuesEstimatedExposure = swapExposureEstimator.getValue(observationDate, simulationModel);
			RandomVariable valuesPositiveExposure = valuesSwap.mult(valuesEstimatedExposure.choose(new RandomVariableFromDoubleArray(1.0), new RandomVariableFromDoubleArray(0.0)));
			double expectedPositiveExposure		= valuesPositiveExposure.getAverage();
			
			
			/*
			 * Calculate the corresponding default probability
			 */
			double defaultProbability = bootStrapper.getDefaultProbForTimeIndex(timeIndex);
			
			
			
			CVA += defaultProbability*expectedPositiveExposure;
		}
		
		
		
		return CVA *(1-recoveryRate);
		
	}
	
	
	
	private double[] getDiscountCurve(LIBORModelMonteCarloSimulationModel model) {

        double[] discountCurve = new double[model.getTimeDiscretization().getNumberOfTimes()];
        
        for(int timeIndex = 0 ; timeIndex < discountCurve.length; timeIndex++) {
      	  double time = SimulationTimeDiscretization.getTime(timeIndex);
      	  discountCurve[timeIndex]= model.getModel().getDiscountCurve().getDiscountFactor(time);
        }
		return discountCurve;
	}

}
