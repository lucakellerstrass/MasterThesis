package kellertrass.ModelCalibration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import kellerstrass.Calibration.CurveModelCalibrationItem;
import kellerstrass.marketInformation.CalibrationInformation;
import kellerstrass.marketInformation.CurveModelDataType;
import net.finmath.exception.CalculationException;
import net.finmath.marketdata.model.AnalyticModel;
import net.finmath.marketdata.model.AnalyticModelFromCurvesAndVols;
import net.finmath.marketdata.model.curves.Curve;
import net.finmath.marketdata.model.curves.DiscountCurve;
import net.finmath.marketdata.model.curves.ForwardCurve;
import net.finmath.montecarlo.BrownianMotion;
import net.finmath.montecarlo.RandomVariableFactory;
import net.finmath.montecarlo.interestrate.CalibrationProduct;
import net.finmath.montecarlo.interestrate.LIBORModelMonteCarloSimulationModel;
import net.finmath.montecarlo.interestrate.LIBORMonteCarloSimulationFromLIBORModel;
import net.finmath.montecarlo.interestrate.models.HullWhiteModel;
import net.finmath.montecarlo.interestrate.models.covariance.AbstractShortRateVolatilityModel;
import net.finmath.montecarlo.interestrate.models.covariance.ShortRateVolatilityModelParametric;
import net.finmath.montecarlo.interestrate.models.covariance.ShortRateVolatilityModelPiecewiseConstant;
import net.finmath.montecarlo.process.MonteCarloProcess;
import net.finmath.optimizer.SolverException;
import net.finmath.time.TimeDiscretization;
import net.finmath.time.TimeDiscretizationFromArray;





public class HWCalibrationMaschine extends AbstractCalibrationMaschine  implements  CalibrationMaschineInterface  {

	
	
	/*--------------Constructors--------------*/
	
	/**
	 * Calibrate a Hull White Model using some calibration input and and Curve Model input.
	 * @param numberOfPaths
	 * @param numberOfFactors
	 * @param calibrationInformation
	 * @param curveModelCalibrationMaschine
	 */
	public HWCalibrationMaschine(int numberOfPaths,int numberOfFactors, CalibrationInformation calibrationInformation,
			CurveModelCalibrationMaschine curveModelCalibrationMaschine) {
		super(numberOfPaths,numberOfFactors, calibrationInformation, curveModelCalibrationMaschine);
		super.modelName = "HW P" + numberOfPaths + calibrationInformation.getName() + "CurveModel"
				+ curveModelCalibrationMaschine.getCurveModelName();

	}

	
	/**
	 * Calibrate a Hull White Model using some calibration input and general Curve model assumptions.
	 * <br>
	 * If the model is already calibrated it is not done again
	 * 
	 * @param numberOfPaths
	 * @param numberOfFactors
	 * @param calibrationInformation
	 */
	public HWCalibrationMaschine(int numberOfPaths, int numberOfFactors,
			CalibrationInformation calibrationInformation) {
		this(numberOfPaths,numberOfFactors, calibrationInformation, new CurveModelCalibrationMaschine(CurveModelDataType.Example));
	}
	
	
	/**
	 * Calibrate a LIBOR Market Model using some calibration input and and a Curve Model data type.
	 * <br>
	 * If the model is already calibrated it is not done again
	 * 
	 * @param numberOfPaths
	 * @param numberOfFactors
	 * @param calibrationInformation
	 * @param forceCalculation
	 */
	public HWCalibrationMaschine(int numberOfPaths, int numberOfFactors, CalibrationInformation calibrationInformation,
			CurveModelDataType curveModelDataType) {
		this(numberOfPaths,numberOfFactors, calibrationInformation, new CurveModelCalibrationMaschine(curveModelDataType));
	}
	
	
	/*--------------END of Constructors--------------*/
	
	
	
	
	
	public LIBORModelMonteCarloSimulationModel getLIBORModelMonteCarloSimulationModel(MonteCarloProcess process)
			throws SolverException {
		return getLIBORModelMonteCarloSimulationModel(process, false);
	}

	
	public LIBORModelMonteCarloSimulationModel getLIBORModelMonteCarloSimulationModel(MonteCarloProcess process,
			boolean forcedCalculation) throws SolverException {
		return new LIBORMonteCarloSimulationFromLIBORModel(getCalibratedModel(forcedCalculation), process );
	}
	

	public HullWhiteModel getCalibratedModel() throws SolverException {
		return  getCalibratedModel(false);
		
	}
	
	
	
	//..................The Check if the model is already calibrated....................//	

		/**
		 * Get the Calibrated LIBOR Market Model using the given Information. If the
		 * exact same Model is already stored, it will be loaded and not be calibrated
		 * again, as long as >>force Calculation<< is false.
		 * @throws SolverException 
		 */
	public HullWhiteModel getCalibratedModel(boolean forcedCalculation) throws SolverException {

		// If we have forced Calibration, we calibrate
		if (forcedCalculation) {
			CalibrateAndStore();
		}

		HullWhiteModel hullWhiteModelalibrated = null;

		// Read stored LMM
		try {
			FileInputStream fileIn = new FileInputStream("temp/" + super.modelName + ".ser");
			ObjectInputStream in = new ObjectInputStream(fileIn);
			hullWhiteModelalibrated = (HullWhiteModel) in.readObject();
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
				hullWhiteModelalibrated = (HullWhiteModel) in.readObject();
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

		return hullWhiteModelalibrated;

	}

	
	
    //..................The Calibration....................//	
	
	
	/**
	 * Here the Hull White Model is calibrated and stored under its name.
	 * @throws SolverException 
	 */
	private void CalibrateAndStore() throws SolverException {
		double calibationStart = System.currentTimeMillis();

		// Get the calibrated Analytic model
	/*	AnalyticModel curveModel;
		try {
			curveModel = curveModelCalibrationMaschine.getCalibratedCurve();
		} catch (SolverException e1) {
			System.out.println("It failed to get the Calibrated curve from the curveModelCalibrationMaschine");
			curveModel = null;
			e1.printStackTrace();
		}
		// Create the forward curve (initial value of the LIBOR market model)
		final ForwardCurve forwardCurve =  curveModel.getForwardCurve("forwardCurve");       //("ForwardCurveFromDiscountCurve(discountCurve-EUR,6M)");
		
		final DiscountCurve discountCurve = curveModel.getDiscountCurve("discountCurve-EUR");
*/
		/* Calibration */

		/*
		 * Create a set of calibration products.
		 */
		// We get the calibration information from the "CalibrationInformation" instance
		CalibrationProduct[] calibrationProducts =  getCalibrationProducts();
		
		
		
			
			
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
					numberOfFactors , numberOfPaths, 31415 /* seed */);
			
			
			
			
			TimeDiscretization volatilityDiscretization = new TimeDiscretizationFromArray(new double[] {0, 1 ,2, 3, 5, 7, 10, 15});
            
			RandomVariableFactory randomVariableFactory = new RandomVariableFactory();
			
			AbstractShortRateVolatilityModel volatilityModel =
					new ShortRateVolatilityModelPiecewiseConstant(randomVariableFactory, 
							timeDiscretizationFromArray, volatilityDiscretization, 
							new double[] {0.02}, new double[] {0.1}, true);
			
			
			//		//Create map (mainly use calibration defaults)
			Map<String, Object> properties = new HashMap<>();
			Map<String, Object> calibrationParameters = new HashMap<>();
			calibrationParameters.put("brownianMotion", brownianMotion);
			properties.put("calibrationParameters", calibrationParameters);
			
			

			
			HullWhiteModel hullWhiteModelCalibrated = null;
			try {
				hullWhiteModelCalibrated = HullWhiteModel.of(randomVariableFactory,
						liborPeriodDiscretization, new AnalyticModelFromCurvesAndVols(new Curve[] {discountCurve, forwardCurve}), 
						forwardCurve, discountCurve,
						volatilityModel, calibrationProducts, properties);
			} catch (CalculationException e) {
				System.out.println("Creating the Hull White Model failed");
				e.printStackTrace();
			}
			
			double calibationEnd = System.currentTimeMillis();

			calculationDuration = calibationEnd - calibationStart;
			
			
			/**************************************************************************/

			// Store the Hull White Model under it's name in a temporary folder
			try {
				File directory = new File("temp");
				if (!directory.exists()) {
					directory.mkdir();
				}

				FileOutputStream fileOut = new FileOutputStream("temp/" + super.modelName + ".ser");
				ObjectOutputStream out = new ObjectOutputStream(fileOut);
				out.writeObject(hullWhiteModelCalibrated);
				out.close();
				fileOut.close();
				// System.out.println("Serianlized data is saved in temp/" +super.modelName + ".ser");

			} catch (IOException v) {
				v.printStackTrace();
			}
			
			// Store the Hull White Model Parameters under it's name in a temporary folder
			try {
				File directory = new File("temp");
				if (!directory.exists()) {
					directory.mkdir();
				}

				FileOutputStream fileOut = new FileOutputStream("temp/" +"Parameters"+ super.modelName + ".ser");
				ObjectOutputStream out = new ObjectOutputStream(fileOut);
				out.writeObject(   ((ShortRateVolatilityModelParametric) hullWhiteModelCalibrated.getVolatilityModel()) .getParameterAsDouble());
				out.close();
				fileOut.close();
				// System.out.println("Serianlized data is saved in temp/" +super.modelName + ".ser");

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
			return ((ShortRateVolatilityModelParametric) getCalibratedModel().getVolatilityModel()) .getParameterAsDouble();
		} catch (SolverException e) {
			System.out.println("returning the Model Parameters of the Hull White Model failed");
			e.printStackTrace();
			return null;
		}
		
	}
	

	

}
