package kellerstrass.ModelCalibration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import kellerstrass.marketInformation.CalibrationInformation;
import net.finmath.exception.CalculationException;
import net.finmath.marketdata.model.curves.DiscountCurve;
import net.finmath.marketdata.model.curves.ForwardCurve;
import net.finmath.montecarlo.interestrate.CalibrationProduct;
import net.finmath.montecarlo.interestrate.LIBORModelMonteCarloSimulationModel;
import net.finmath.montecarlo.process.MonteCarloProcess;
import net.finmath.optimizer.SolverException;

/**
 * CalibrationMachineInterface collects all methods that have to be implemented
 * in all calibration machines.
 * 
 * @author lucak
 *
 */
public interface CalibrationMachineInterface {

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
	 * Get a LIBORModelMonteCarloSimulationModel with a specific process <br>
	 * If the model is already calibrated it is not done again
	 * 
	 * @param process
	 * @return
	 * @throws SolverException
	 * @throws CalculationException
	 */
	public LIBORModelMonteCarloSimulationModel getLIBORModelMonteCarloSimulationModel(MonteCarloProcess process)
			throws SolverException, CalculationException;

	/**
	 * Get a LIBORModelMonteCarloSimulationModel with a specific process. If the
	 * exact same Model is already stored, it will be loaded and not be calibrated
	 * again, as long as >>force Calculation<< is false.
	 * 
	 * @param process
	 * @param forcedCalculation
	 * @return
	 * @throws SolverException
	 * @throws CalculationException
	 */
	public LIBORModelMonteCarloSimulationModel getLIBORModelMonteCarloSimulationModel(MonteCarloProcess process,
			boolean forcedCalculation) throws SolverException, CalculationException;

	/**
	 * Returns the Calibrated Parameters of the Model. The result can vary,
	 * depending on the Model
	 * 
	 * @return
	 * @throws CalculationException
	 */
	public double[] getCalibratedParameters() throws CalculationException;

	/**
	 * Transform the Calibration information into an Array of Calibration products
	 * 
	 * @return
	 */
	public CalibrationProduct[] getCalibrationProducts();

	/**
	 * Get the number of paths
	 * 
	 * @return
	 */
	public int getNumberOfPaths();

	/**
	 * get the number of factors
	 * 
	 * @return
	 */
	public int getNumberOfFactors();

	/**
	 * Get the calibrationInformation
	 * 
	 * @return
	 */
	public CalibrationInformation getCalibrationInformation();

	/**
	 * Get the curveModelCalibrationmaschine
	 * 
	 * @return
	 */
	public CurveModelCalibrationMachine getCurveModelCalibrationMaschine();

	/**
	 * Returns the names of Calibration Products that are used for the calibration.
	 * <br>
	 * The method getCalibrationProducts() does not count products which have an exercise date before time 1.0.
	 * <br>
	 * Hence This names will match the getCalibrationProducts Array from getCalibrationProducts() and NOT the initial calibration information.
	 * 
	 * @return
	 */
	public String[] getCalibrationItemNames(CalibrationInformation calibrationInformation);

	/**
	 * Get the Tenors of the products used for calibration.
	 * @param calibrationInformation
	 * @return
	 * @ToDo Do this without code duplication
	 */
	public String[] getCalibrationItemTenors(CalibrationInformation calibrationInformation);
	
	/**
	 * Get the Expiries of the products used for calibration.
	 * @param calibrationInformation
	 * @return
	 * @ToDo Do this without code duplication
	 */
	public String[] getCalibrationItemExpiries(CalibrationInformation calibrationInformation);
	
	
	
	
	
	/**
	 * Print Calibration Test
	 */
	public void printCalibrationTest();
	
	
	/**
	 * Get the calibration test table.
	 * <br>
	 * useful for the Python GUI
	 * @param forcedCalculation
	 * @return
	 */
	public ArrayList<Map<String, Object>> getCalibrationTable(boolean forcedCalculation);


	/**
	 * Print Calibration Test using the possibility to add forced calculation.
	 */
	public void printCalibrationTest(boolean forcedCalculation);

	/**
	 * Get the forward curve used in the calibration
	 * 
	 * @return
	 */
	public ForwardCurve getForwardCurve();

	/**
	 * Get the discount curve used in the calibration
	 * 
	 * @return
	 */
	public DiscountCurve getDiscountCurve();

}
