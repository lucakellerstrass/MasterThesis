package kellerstrass.ModelCalibration;

import kellerstrass.marketInformation.CalibrationInformation;
import net.finmath.exception.CalculationException;
import net.finmath.marketdata.model.curves.DiscountCurve;
import net.finmath.marketdata.model.curves.ForwardCurve;
import net.finmath.montecarlo.interestrate.CalibrationProduct;
import net.finmath.montecarlo.interestrate.LIBORModelMonteCarloSimulationModel;
import net.finmath.montecarlo.process.MonteCarloProcess;
import net.finmath.optimizer.SolverException;

public interface CalibrationMaschineInterface {
	
	
	/**
	 * Get the name of the model
	 */
	public String getModelName();

	
	/**
	 * Get the number of milli seconds the calculation took. <br>
	 * This method does not start a new calibration. If the calibration was not done
	 * yet, we wont get a calculation Duration.
	 */
	public double getCalculationDuration();
	
	/**
	 * Get a LIBORModelMonteCarloSimulationModel with a specific process
	 * <br>
	 * If the model is already calibrated it is not done again 
	 * @param process
	 * @return
	 * @throws SolverException
	 * @throws CalculationException 
	 */
	public LIBORModelMonteCarloSimulationModel getLIBORModelMonteCarloSimulationModel(MonteCarloProcess process) throws SolverException, CalculationException;
	
	
	/**
	 * Get a LIBORModelMonteCarloSimulationModel with a specific process. If the
	 * exact same Model is already stored, it will be loaded and not be calibrated
	 * again, as long as >>force Calculation<< is false.
	 * @param process
	 * @param forcedCalculation
	 * @return
	 * @throws SolverException
	 * @throws CalculationException 
	 */
	public LIBORModelMonteCarloSimulationModel getLIBORModelMonteCarloSimulationModel(MonteCarloProcess process, boolean forcedCalculation) throws SolverException, CalculationException;
	
	
	/**
	 * Returns the Calibrated Parameters of the Model.
	 * The result can vary, depending on the Model
	 * @return 
	 * @throws CalculationException 
	 */
	public double[] getCalibratedParameters() throws CalculationException;
	
	
	/**
	 * Transform the Calibration information into an Array of Calibration products 
	 * @return
	 */
	public CalibrationProduct[] getCalibrationProducts();
	
	/**
	 * Get the number of paths
	 * @return
	 */
		public int getNumberOfPaths();




	/**
	 * get the number of factors
	 * @return
	 */
		public int getNumberOfFactors();



	/**
	 * Get the calibrationInformation
	 * @return
	 */
		public CalibrationInformation getCalibrationInformation();





	/**
	 * Get the curveModelCalibrationmaschine
	 * @return
	 */
		public CurveModelCalibrationMaschine getCurveModelCalibrationMaschine();


		/**
		 * Add the possibility to get the names of the calibration products
		 * @return
		 */
		public String[] getCalibrationItemNames(CalibrationInformation calibrationInformation);
	
		
		/**
		 * Print Calibration Test
		 */
		public void printCalibrationTest();
		
		/**
		 * Print Calibration Test using the possibility to add forced calculation.
		 */
		public void printCalibrationTest(boolean forcedCalculation);
		
		
		/**
		 * Get the forward curve used in the calibration 
		 * @return
		 */
		public ForwardCurve getForwardCurve();





		/**
		 * Get the discount curve used in the calibration 
		 * @return
		 */
		public DiscountCurve getDiscountCurve();
		

		
		
}
