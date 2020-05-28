package kellerstrass.CVA;


import kellerstrass.defaultProbability.ForwardBootstrap;
import kellerstrass.exposure.ExposureMaschine;
import kellerstrass.usefulOLD.PaymentFrequencyOLD;
import net.finmath.marketdata.model.curves.DiscountCurve;
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
		this.swapExposureEstimator =new ExposureMaschine(swap);
		this.recoveryRate = recoveryRate;
		this.cdsSpreads = cdsSpreads;
		this.SimulationTimeDiscretization = simulationModel.getTimeDiscretization();
		this.discountCurve = getDiscountCurve(simulationModel);
		
	}
	
	
	/**
	 * 
	 * @param simulationModel
	 * @param swap
	 * @param recoveryRate
	 * @param cdsSpreads
	 * @param discount curve
	 */
	public CVA(LIBORModelMonteCarloSimulationModel simulationModel, AbstractLIBORMonteCarloProduct swap,
			double recoveryRate, double[] cdsSpreads, DiscountCurve discountCurve) {
		super();
		this.simulationModel = simulationModel;
		this.swap = swap;
		this.swapExposureEstimator =new ExposureMaschine(swap);
		this.recoveryRate = recoveryRate;
		this.cdsSpreads = cdsSpreads;
		this.SimulationTimeDiscretization = simulationModel.getTimeDiscretization();
		this.discountCurve = getDiscountCurve(discountCurve, simulationModel);
		
	}
	






	/**
	 * This class implements:
	 * @return
	 * @throws Exception 
	 */
	public double getValue() throws Exception {
	
		ForwardBootstrap bootStrapper = new ForwardBootstrap(discountCurve, recoveryRate, cdsSpreads, PaymentFrequencyOLD.MOUNTHLY, SimulationTimeDiscretization);
		
		
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
			
      	//discountCurve[timeIndex]= model.getModel().getAnalyticModel().getDiscountCurve(null).getDiscountFactor(time);
        }
		return discountCurve;
	}
	
	private double[] getDiscountCurve(DiscountCurve discountCurve, LIBORModelMonteCarloSimulationModel model) {

        double[] discountCurvePoints = new double[model.getTimeDiscretization().getNumberOfTimes()];
        
        for(int timeIndex = 0 ; timeIndex < discountCurvePoints.length; timeIndex++) {
      	  double time = SimulationTimeDiscretization.getTime(timeIndex);
      	   discountCurvePoints[timeIndex]= discountCurve.getDiscountFactor(time);
			
        }
		return discountCurvePoints;
	}
	
	

}
