package kellerstrass.ModelCalibration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import kellerstrass.marketInformation.CalibrationInformation;
import kellerstrass.marketInformation.CurveModelDataType;
import net.finmath.exception.CalculationException;
import net.finmath.marketdata.model.curves.DiscountCurveFromForwardCurve;
import net.finmath.montecarlo.BrownianMotion;
import net.finmath.montecarlo.interestrate.CalibrationProduct;
import net.finmath.montecarlo.interestrate.LIBORModel;
import net.finmath.montecarlo.interestrate.LIBORModelMonteCarloSimulationModel;
import net.finmath.montecarlo.interestrate.LIBORMonteCarloSimulationFromLIBORModel;
import net.finmath.montecarlo.interestrate.models.LIBORMarketModelFromCovarianceModel;
import net.finmath.montecarlo.interestrate.models.covariance.AbstractLIBORCovarianceModelParametric;
import net.finmath.montecarlo.interestrate.models.covariance.DisplacedLocalVolatilityModel;
import net.finmath.montecarlo.interestrate.models.covariance.LIBORCorrelationModel;
import net.finmath.montecarlo.interestrate.models.covariance.LIBORCorrelationModelExponentialDecay;
import net.finmath.montecarlo.interestrate.models.covariance.LIBORCovarianceModelFromVolatilityAndCorrelation;
import net.finmath.montecarlo.interestrate.models.covariance.LIBORVolatilityModel;
import net.finmath.montecarlo.interestrate.models.covariance.LIBORVolatilityModelPiecewiseConstant;
import net.finmath.montecarlo.process.MonteCarloProcess;
import net.finmath.optimizer.OptimizerFactory;
import net.finmath.optimizer.OptimizerFactoryLevenbergMarquardt;
import net.finmath.optimizer.SolverException;
import net.finmath.time.TimeDiscretization;
import net.finmath.time.TimeDiscretizationFromArray;

/**
 * LmmCalibrationMachine extends AbstractCalibrationMachine implements
 * CalibrationMachineInterface <br>
 * Calibrates a LIBOR Market Model
 * 
 * @author lucak
 *
 */
public class LmmCalibrationMachine extends AbstractCalibrationMachine implements CalibrationMachineInterface {

	// Class intern parameters
	// private double calculationDuration; in Abstract Base class

	/*--------------Constructors--------------*/
	/**
	 * Calibrate a LIBOR Market Model using some calibration input and and Curve
	 * Model input. <br>
	 * If the model is already calibrated it is not done again
	 * 
	 * @param numberOfPaths
	 * @param numberOfFactors
	 * @param calibrationInformation
	 * @param curveModelCalibrationMaschine
	 */
	public LmmCalibrationMachine(int numberOfPaths, int numberOfFactors, CalibrationInformation calibrationInformation,
			CurveModelCalibrationMachine curveModelCalibrationMaschine) {
		super(numberOfPaths, numberOfFactors, calibrationInformation, curveModelCalibrationMaschine);

		this.modelName = "LMM P" + numberOfPaths + "F" + numberOfFactors + calibrationInformation.getName()
				+ "CurveModel" + curveModelCalibrationMaschine.getCurveModelName();

	}

	/**
	 * Calibrate a LIBOR Market Model using some calibration input and general Curve
	 * model assumptions. <br>
	 * If the model is already calibrated it is not done again
	 * 
	 * @param numberOfPaths
	 * @param numberOfFactors
	 * @param calibrationInformation
	 */
	public LmmCalibrationMachine(int numberOfPaths, int numberOfFactors,
			CalibrationInformation calibrationInformation) {
		this(numberOfPaths, numberOfFactors, calibrationInformation,
				new CurveModelCalibrationMachine(CurveModelDataType.Example));
	}

	/**
	 * Calibrate a LIBOR Market Model using some calibration input and and a Curve
	 * Model data type. <br>
	 * If the model is already calibrated it is not done again
	 * 
	 * @param numberOfPaths
	 * @param numberOfFactors
	 * @param calibrationInformation
	 * @param forceCalculation
	 */
	public LmmCalibrationMachine(int numberOfPaths, int numberOfFactors, CalibrationInformation calibrationInformation,
			CurveModelDataType curveModelDataType) {
		this(numberOfPaths, numberOfFactors, calibrationInformation,
				new CurveModelCalibrationMachine(curveModelDataType));
	}

	/*--------------END of Constructors--------------*/

	/**
	 * Get a LIBORModelMonteCarloSimulationModel with a specific process <br>
	 * If the model is already calibrated it is not done again
	 * 
	 * @param process
	 * @return
	 * @throws SolverException
	 */
	@Override
	public LIBORModelMonteCarloSimulationModel getLIBORModelMonteCarloSimulationModel(MonteCarloProcess process)
			throws SolverException {
		return getLIBORModelMonteCarloSimulationModel(process, false);
	}

	/**
	 * Get a LIBORModelMonteCarloSimulationModel with a specific process. If the
	 * exact same Model is already stored, it will be loaded and not be calibrated
	 * again, as long as >>force Calculation<< is false.
	 * 
	 * @param process
	 * @param forcedCalculatio
	 * @return
	 * @throws SolverException
	 */
	public LIBORModelMonteCarloSimulationModel getLIBORModelMonteCarloSimulationModel(MonteCarloProcess process,
			boolean forcedCalculation) throws SolverException {
		return new LIBORMonteCarloSimulationFromLIBORModel(getCalibratedModel(forcedCalculation), process);
	}

	/**
	 * Get the Calibrated LIBOR Market Model using the given Information. If the
	 * exact same Model is already stored, it will be loaded and not be calibrated
	 * again.
	 * 
	 * @throws SolverException
	 */
	public LIBORModel getCalibratedModel() throws SolverException {
		return getCalibratedModel(false);

	}

	// ..................The Check if the model is already
	// calibrated....................//

	/**
	 * Get the Calibrated LIBOR Market Model using the given Information. If the
	 * exact same Model is already stored, it will be loaded and not be calibrated
	 * again, as long as >>force Calculation<< is false.
	 * 
	 * @throws SolverException
	 */
	public LIBORModel getCalibratedModel(boolean forcedCalculation) throws SolverException {

		// If we have forced Calibration, we calibrate
		if (forcedCalculation) {
			CalibrateAndStore();
		}

		LIBORModel liborMarketModelCalibrated = null;

		// Read stored LMM
		try {
			FileInputStream fileIn = new FileInputStream("temp/" + super.modelName + ".ser");
			ObjectInputStream in = new ObjectInputStream(fileIn);
			liborMarketModelCalibrated = (LIBORModel) in.readObject();
			in.close();
			fileIn.close();
		}

		// If the file is not found, we calibrate and store.
		catch (IOException k) {

			// calibrate and store
			CalibrateAndStore();

			// Then try to load again
			try {
				FileInputStream fileIn = new FileInputStream("temp/" + super.modelName + ".ser");
				ObjectInputStream in = new ObjectInputStream(fileIn);
				liborMarketModelCalibrated = (LIBORModel) in.readObject();
				in.close();
				fileIn.close();
			}

			catch (ClassNotFoundException x) {
				System.out.println("Employee class not found");
				x.printStackTrace();

			} catch (IOException c) {
				System.out.println("Reading error occured.");
				c.printStackTrace();
			}

		} catch (ClassNotFoundException c) {
			System.out.println("Employee class not found");
			c.printStackTrace();

		}

		return liborMarketModelCalibrated;

	}

	// ..................The Calibration....................//

	/**
	 * Here the LIBOR Market Model is calibrated and stored under its name.
	 * 
	 * @throws SolverException
	 */
	private void CalibrateAndStore() throws SolverException {

		double calibationStart = System.currentTimeMillis();

		// Get the calibrated Analytic model
		// AnalyticModel curveModel =
		// this.curveModelCalibrationMaschine.getCalibratedCurve();
		// Create the forward curve (initial value of the LIBOR market model)
		// final ForwardCurve forwardCurve = super.curveModel
		// .getForwardCurve("ForwardCurveFromDiscountCurve(discountCurve-EUR,6M)");
		// final DiscountCurve discountCurve =
		// super.curveModel.getDiscountCurve("discountCurve-EUR");

		/* Calibration */

		/*
		 * Create a set of calibration products.
		 */
		// We get the calibration information from the "CalibrationInformation" instance
		CalibrationProduct[] calibrationProducts = getCalibrationProducts();

		/*
		 * Create a simulation time discretization
		 */
		// If simulation time is below libor time, exceptions will be hard to track.
		double lastTime = 40.0;
		double dt = 0.25;
		TimeDiscretizationFromArray timeDiscretizationFromArray = new TimeDiscretizationFromArray(0.0,
				(int) (lastTime / dt), dt);
		final TimeDiscretization liborPeriodDiscretization = timeDiscretizationFromArray;

		/*
		 * Create Brownian motion
		 */
		BrownianMotion brownianMotion = new net.finmath.montecarlo.BrownianMotionLazyInit(timeDiscretizationFromArray,
				super.numberOfFactors, super.numberOfPaths, 31415 /* seed */);

		/*
		 * Create a volatility Model Here one can adjust the volatility matrix
		 * dimensions
		 */
		/*
		 * LIBORVolatilityModel volatilityModel = new
		 * LIBORVolatilityModelPiecewiseConstant(timeDiscretizationFromArray,
		 * liborPeriodDiscretization, new TimeDiscretizationFromArray(0, 1, 2, 3, 5, 7,
		 * 10, 15), new TimeDiscretizationFromArray(0, 1, 2, 3, 5, 7, 10, 15), 0.50 /
		 * 100);
		 */
		LIBORVolatilityModel volatilityModel = new LIBORVolatilityModelPiecewiseConstant(timeDiscretizationFromArray,
				liborPeriodDiscretization,
				new TimeDiscretizationFromArray(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 15, 20, 25, 30),
				new TimeDiscretizationFromArray(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 15, 20, 25, 30), 0.30 / 100);

		// Create correlationModel
		LIBORCorrelationModel correlationModel = new LIBORCorrelationModelExponentialDecay(timeDiscretizationFromArray,
				liborPeriodDiscretization, super.numberOfFactors, 0.05, false);

		// Create a covariance model
		AbstractLIBORCovarianceModelParametric covarianceModelParametric = new LIBORCovarianceModelFromVolatilityAndCorrelation(
				timeDiscretizationFromArray, liborPeriodDiscretization, volatilityModel, correlationModel);

		// Create blended local volatility model with fixed parameter (0=lognormal, > 1
		// = almost a normal model).
		AbstractLIBORCovarianceModelParametric covarianceModelDisplaced = new DisplacedLocalVolatilityModel(
				covarianceModelParametric, 1.0 / 0.25, false /* isCalibrateable */);

		// Set model properties
		Map<String, Object> properties = new HashMap<>();

		// Choose the simulation measure
		properties.put("measure", LIBORMarketModelFromCovarianceModel.Measure.SPOT.name());

		// Choose normal state space for the Euler scheme (the covariance model above
		// carries a linear local volatility model, such that the resulting model is
		// log-normal).
		properties.put("stateSpace", LIBORMarketModelFromCovarianceModel.StateSpace.NORMAL.name());

		// works also without
		// Set calibration properties (should use our brownianMotion for calibration -
		// needed to have to right correlation).
		Double accuracy = new Double(1E-6); // Lower accuracy to reduce runtime of the unit test
		int maxIterations = 100;
		int numberOfThreads = 4;
		OptimizerFactory optimizerFactory = new OptimizerFactoryLevenbergMarquardt(maxIterations, accuracy,
				numberOfThreads);

		double[] parameterStandardDeviation = new double[covarianceModelParametric.getParameterAsDouble().length];
		double[] parameterLowerBound = new double[covarianceModelParametric.getParameterAsDouble().length];
		double[] parameterUpperBound = new double[covarianceModelParametric.getParameterAsDouble().length];
		Arrays.fill(parameterStandardDeviation, 0.20 / 100.0);
		Arrays.fill(parameterLowerBound, 0.0);
		Arrays.fill(parameterUpperBound, Double.POSITIVE_INFINITY);

		// Set calibration properties (should use our brownianMotion for calibration -
		// needed to have to right correlation).
		Map<String, Object> calibrationParameters = new HashMap<>();
		calibrationParameters.put("accuracy", accuracy);
		calibrationParameters.put("brownianMotion", brownianMotion);
		calibrationParameters.put("optimizerFactory", optimizerFactory);
		calibrationParameters.put("parameterStep", new Double(1E-4));
		properties.put("calibrationParameters", calibrationParameters);

		LIBORModel liborMarketModelCalibrated = null;

		try {
			liborMarketModelCalibrated = new LIBORMarketModelFromCovarianceModel(liborPeriodDiscretization, curveModel,
					forwardCurve, new DiscountCurveFromForwardCurve(forwardCurve), covarianceModelDisplaced,
					calibrationProducts, properties);
		} catch (CalculationException e) {
			System.out.println(
					" Creation of liborMarketModelCalibrated in the class LmmCalibrationMaschine did not work.");
			e.printStackTrace();
		}

		double calibationEnd = System.currentTimeMillis();

		calculationDuration = calibationEnd - calibationStart;

		/**************************************************************************/

		// Store the LIBOR Market Model under it's name in a temporary folder
		try {
			File directory = new File("temp");
			if (!directory.exists()) {
				directory.mkdir();
			}

			FileOutputStream fileOut = new FileOutputStream("temp/" + super.modelName + ".ser");
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(liborMarketModelCalibrated);
			out.close();
			fileOut.close();
			// System.out.println("Serianlized data is saved in temp/" +super.modelName +
			// ".ser");

		} catch (IOException v) {
			v.printStackTrace();
		}

		// Store the Calculation Time under it's name in a temporary folder
		try {
			File directory = new File("temp");
			if (!directory.exists()) {
				directory.mkdir();
			}

			FileOutputStream fileOut = new FileOutputStream("temp/" + super.modelName + "CalculationDuration.ser");
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(calculationDuration);
			out.close();
			fileOut.close();

		} catch (IOException v) {
			v.printStackTrace();
		}

	}

	@Override
	public double[] getCalibratedParameters() {
		try {
			return ((AbstractLIBORCovarianceModelParametric) ((LIBORMarketModelFromCovarianceModel) getCalibratedModel())
					.getCovarianceModel()).getParameterAsDouble();
		} catch (SolverException e) {
			System.out.println("returning the Model Parameters of the Libor Market Model failed");
			e.printStackTrace();
			return null;
		}

	}

}
