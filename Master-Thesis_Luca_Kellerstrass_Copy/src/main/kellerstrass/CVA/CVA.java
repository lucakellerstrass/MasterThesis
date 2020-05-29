package kellerstrass.CVA;

import kellerstrass.defaultProbability.ForwardBootstrap;
import kellerstrass.exposure.ExposureMachine;
import kellerstrass.usefulOLD.PaymentFrequencyOLD;
import net.finmath.marketdata.model.curves.DiscountCurve;
import net.finmath.montecarlo.RandomVariableFromDoubleArray;
import net.finmath.montecarlo.interestrate.LIBORModelMonteCarloSimulationModel;
import net.finmath.montecarlo.interestrate.products.AbstractLIBORMonteCarloProduct;
import net.finmath.montecarlo.interestrate.products.TermStructureMonteCarloProduct;
import net.finmath.stochastic.RandomVariable;
import net.finmath.time.TimeDiscretization;


/**
 * This class implements the CVA (Credit Valuation adjustment).
 * <br>
 * It needs the an underlying (for example a swap), the underlying exposures and some counterparty information.
 * <br>  
 * 
 * 
 * 
 * @author lucak
 *
 */
public class CVA {

	private LIBORModelMonteCarloSimulationModel simulationModel;
	private AbstractLIBORMonteCarloProduct swap; // can be reduced, to just an underlying. for example a portfolio
	private TermStructureMonteCarloProduct swapExposureEstimator;
	private TimeDiscretization SimulationTimeDiscretization;
	private double recoveryRate;
	private double[] cdsSpreads;
	private double[] discountCurve;


	
	/** Initiate the CVA class unsing just a vector of CDS spreads
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
		this.swapExposureEstimator = new ExposureMachine(swap);
		this.recoveryRate = recoveryRate;
		this.cdsSpreads = cdsSpreads;
		this.SimulationTimeDiscretization = simulationModel.getTimeDiscretization();
		this.discountCurve = getDiscountCurve(simulationModel);

	}

	/**Initiate the CVA class.
	 * 
	 * @param simulationModel
	 * @param swap
	 * @param recoveryRate
	 * @param cdsSpreads
	 * @param discount        curve
	 */
	public CVA(LIBORModelMonteCarloSimulationModel simulationModel, AbstractLIBORMonteCarloProduct swap,
			double recoveryRate, double[] cdsSpreads, DiscountCurve discountCurve) {
		super();
		this.simulationModel = simulationModel;
		this.swap = swap;
		this.swapExposureEstimator = new ExposureMachine(swap);
		this.recoveryRate = recoveryRate;
		this.cdsSpreads = cdsSpreads;
		this.SimulationTimeDiscretization = simulationModel.getTimeDiscretization();
		this.discountCurve = getDiscountCurve(discountCurve, simulationModel);

	}

	/**
	 * The getValue methode returns the Value of
	 * <br>
	 * CVA(t) = \int_{s=t}^{T} \lambda_C(s) e^{-\int_{u=0}^s \lambda_C(u) du}
      \E_{\W} \left[ e^{-\int_{u=0}^s r(u)du} (1-R) (V_s)^+ \mid \G_t 
       \right] ds,
	 * <br>
	 * which is discretized by 
	 * <br>
	 * CVA(t) = \sum_{i=0}^{n-1} \left[ \Phi(\tau > t_i) - \Phi(\tau > t_{i+1}) \right]
       \E_{\W} \left[ e^{-\int_{u=0}^{t_i} r(u)du} (1-R)(V_{t_i})^+ \mid \G_t 
     \right] ds,
	 * <br>
	 * where $\Phi(\tau > t_i)$ as the survival probability up to time $t_i$, $R$ is the recovery rate of the counterparty and $V_{t_i})^+$ is the positive exposure of the underlying (f.ex. swap).
	 * 
	 * @return
	 * @throws Exception
	 */
	public double getValue() throws Exception {

		ForwardBootstrap bootStrapper = new ForwardBootstrap(discountCurve, recoveryRate, cdsSpreads,
				PaymentFrequencyOLD.MOUNTHLY, SimulationTimeDiscretization);

		double CVA = 0.0;

		for (double observationDate : SimulationTimeDiscretization) {
			if (observationDate == 0) {
				continue;
			}

			int timeIndex = SimulationTimeDiscretization.getTimeIndex(observationDate);

			/*
			 * Calculate expected positive exposure of a swap
			 */
			RandomVariable valuesSwap = swap.getValue(observationDate, simulationModel);
			RandomVariable valuesEstimatedExposure = swapExposureEstimator.getValue(observationDate, simulationModel);
			RandomVariable valuesPositiveExposure = valuesSwap.mult(valuesEstimatedExposure
					.choose(new RandomVariableFromDoubleArray(1.0), new RandomVariableFromDoubleArray(0.0)));
			double expectedPositiveExposure = valuesPositiveExposure.getAverage();

			/*
			 * Calculate the corresponding default probability
			 */
			double defaultProbability = bootStrapper.getDefaultProbForTimeIndex(timeIndex);

			CVA += defaultProbability * expectedPositiveExposure;
		}

		return CVA * (1 - recoveryRate);

	}

	/**
	 * Get a double array from the model given discount curve.
	 * @param model (LIBORModelMonteCarloSimulationModel)
	 * @return
	 */
	private double[] getDiscountCurve(LIBORModelMonteCarloSimulationModel model) {

		double[] discountCurve = new double[model.getTimeDiscretization().getNumberOfTimes()];

		for (int timeIndex = 0; timeIndex < discountCurve.length; timeIndex++) {
			double time = SimulationTimeDiscretization.getTime(timeIndex);
			discountCurve[timeIndex] = model.getModel().getDiscountCurve().getDiscountFactor(time);

			// discountCurve[timeIndex]=
			// model.getModel().getAnalyticModel().getDiscountCurve(null).getDiscountFactor(time);
		}
		return discountCurve;
	}

	/**
	 * Get a double array from the given discount curve
	 * @param discountCurve (DiscountCurve)
	 * @param model (LIBORModelMonteCarloSimulationModel)
	 * @return
	 */
	private double[] getDiscountCurve(DiscountCurve discountCurve, LIBORModelMonteCarloSimulationModel model) {

		double[] discountCurvePoints = new double[model.getTimeDiscretization().getNumberOfTimes()];

		for (int timeIndex = 0; timeIndex < discountCurvePoints.length; timeIndex++) {
			double time = SimulationTimeDiscretization.getTime(timeIndex);
			discountCurvePoints[timeIndex] = discountCurve.getDiscountFactor(time);

		}
		return discountCurvePoints;
	}

}
