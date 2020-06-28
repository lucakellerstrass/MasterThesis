package kellerstrass.ModelCalibration;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import kellerstrass.Calibration.CurveModelCalibrationItem;
import kellerstrass.marketInformation.CalibrationInformation;
import net.finmath.exception.CalculationException;
import net.finmath.marketdata.model.AnalyticModel;
import net.finmath.marketdata.model.curves.DiscountCurve;
import net.finmath.marketdata.model.curves.ForwardCurve;
import net.finmath.montecarlo.BrownianMotion;
import net.finmath.montecarlo.interestrate.CalibrationProduct;
import net.finmath.montecarlo.interestrate.LIBORModelMonteCarloSimulationModel;
import net.finmath.montecarlo.interestrate.products.AbstractLIBORMonteCarloProduct;
import net.finmath.montecarlo.process.EulerSchemeFromProcessModel;
import net.finmath.montecarlo.process.MonteCarloProcess;
import net.finmath.optimizer.SolverException;
import net.finmath.time.TimeDiscretizationFromArray;

/**
 * AbstractCalibrationMaschine implements CalibrationMaschineInterface and has
 * already some model-independent methods implemented.
 * 
 * 
 * 
 * @author lucak
 *
 */
public abstract class AbstractCalibrationMachine implements CalibrationMachineInterface {

	private static DecimalFormat formatterValue = new DecimalFormat(" ##0.000%;-##0.000%",
			new DecimalFormatSymbols(Locale.ENGLISH));
	private static DecimalFormat formatterVolatility = new DecimalFormat(" #0.000000000;-#0.000000000",
			new DecimalFormatSymbols(Locale.ENGLISH));
	private static DecimalFormat formatterDeviation = new DecimalFormat(" 0.000E00;-0.00E00",
			new DecimalFormatSymbols(Locale.ENGLISH));

	// Input Parameters
	protected int numberOfPaths;
	protected int numberOfFactors;
	protected CalibrationInformation calibrationInformation;
	protected String modelName;
	protected CurveModelCalibrationMachine curveModelCalibrationMaschine;

	// Class intern parameters
	protected double calculationDuration;
	protected AnalyticModel curveModel;
	protected ForwardCurve forwardCurve;
	protected DiscountCurve discountCurve;

	// private intern used variables
	private int NumberOfCalibrationItems;

	/**
	 * The Model name and other model specific input need to be added in the
	 * specific CalibrationMaschine class.
	 * 
	 * @param numberOfPaths
	 * @param calibrationInformation
	 * @param curveModelDataType
	 * @param calculationDuration
	 */

	public AbstractCalibrationMachine(int numberOfPaths, int numberOfFactors,
			CalibrationInformation calibrationInformation, CurveModelCalibrationMachine curveModelCalibrationMaschine) {
		super();
		this.numberOfPaths = numberOfPaths;
		this.numberOfFactors = numberOfFactors;
		this.calibrationInformation = calibrationInformation;
		this.curveModelCalibrationMaschine = curveModelCalibrationMaschine;
		this.NumberOfCalibrationItems = calibrationInformation.getAtmNormalVolatilities().length;

		// Fill in the class calibration maschine intern parameters
		try {
			this.curveModel = this.curveModelCalibrationMaschine.getCalibratedCurveModel();
		} catch (SolverException e) {
			System.out.println("adding the curve model to the AbstractCalibrationMaschine failed ");
			e.printStackTrace();
		}
		this.forwardCurve = curveModel.getForwardCurve("ForwardCurveFromDiscountCurve(discountCurve-EUR,6M)");
		this.discountCurve = curveModel.getDiscountCurve("discountCurve-EUR");

	}

	public ForwardCurve getForwardCurve() {
		return forwardCurve;
	}

	public DiscountCurve getDiscountCurve() {
		return discountCurve;
	}

	public AnalyticModel getCurveModel() {
		return curveModel;
	}

	/**
	 * Get the name of the model
	 */
	public String getModelName() {
		return modelName;
	}

	/**
	 * Get the calculation duration of the calibration in milli seconds <br>
	 * If the model is not calibrated yet, the time will be zero
	 */
	public double getCalculationDuration() {
		try {
			FileInputStream fileIn = new FileInputStream("temp/" + modelName + "CalculationDuration.ser");
			ObjectInputStream in = new ObjectInputStream(fileIn);
			calculationDuration = (double) in.readObject();
			in.close();
			fileIn.close();
		}

		catch (ClassNotFoundException x) {
			System.out.println("Employee class not found");
			calculationDuration = -1.0;
			// x.printStackTrace();

		} catch (IOException k) {
			calculationDuration = -1.0;
			System.out.println("Calibration not done yet. Get the calculation duration after Calibration.");
			// k.printStackTrace();
		}

		return calculationDuration;
	}

	/**
	 * Transform the Calibration information into an Array of Calibration products
	 * 
	 * @return
	 */
	public CalibrationProduct[] getCalibrationProducts() {

		/*
		 * Create a set of calibration products.
		 */
		ArrayList<String> calibrationItemNames = new ArrayList<>();
		final ArrayList<CalibrationProduct> calibrationProducts = new ArrayList<>();

		// We get the calibration information from the "CalibrationInformation" instance

		for (int i = 0; i < calibrationInformation.getAtmNormalVolatilities().length; i++) {

			LocalDate exerciseDate = calibrationInformation.getCal().getDateFromDateAndOffsetCode(
					calibrationInformation.getReferenceDate(), calibrationInformation.getAtmExpiries()[i]);
			LocalDate tenorEndDate = calibrationInformation.getCal().getDateFromDateAndOffsetCode(exerciseDate,
					calibrationInformation.getAtmTenors()[i]);
			double exercise = calibrationInformation.getModelDC()
					.getDaycountFraction(calibrationInformation.getReferenceDate(), exerciseDate);
			double tenor = calibrationInformation.getModelDC().getDaycountFraction(exerciseDate, tenorEndDate);

			// We consider an idealized tenor grid (alternative: adapt the model grid)
			// To ensure the dates fit into the timediscretization
			exercise = Math.round(exercise / 0.25) * 0.25;
			tenor = Math.round(tenor / 0.25) * 0.25;

			if (exercise < 1.0) {
				continue;
			}

			// System.out.println("exerciseDate= "+ exerciseDate + ", tenorEndDate = "+
			// tenorEndDate + ", exercise= " + exercise + "tenor= " + tenor);

			int numberOfPeriods = (int) Math.round(tenor / calibrationInformation.getSwapPeriodLength());

			double moneyness = 0.0;
			double targetVolatility = calibrationInformation.getAtmNormalVolatilities()[i];

			String targetVolatilityType = calibrationInformation.getTargetVolatilityType();

			// check if there are any weights for the calibration information
			double weight = 1.0;
			if (calibrationInformation.getWeights() == null) {
				weight = 1.0;
			} else {
				weight = calibrationInformation.getWeights()[i];
			}

			try {
				calibrationProducts.add(CurveModelCalibrationItem.createCalibrationItem(weight, exercise,
						calibrationInformation.getSwapPeriodLength(), numberOfPeriods, moneyness, targetVolatility,
						targetVolatilityType, forwardCurve, discountCurve));
				calibrationItemNames.add(
						calibrationInformation.getAtmExpiries()[i] + "\t" + calibrationInformation.getAtmTenors()[i]);
			} catch (Exception e) {
				System.out.println("Adding the calibration Information to the calibration items in the LMM failed.");
				e.printStackTrace();
			}

			// System.out.println(" 2) calibrationItemNames.size= " +
			// calibrationItemNames.size());

		}

		CalibrationProduct[] calibrationItems = new CalibrationProduct[calibrationItemNames.size()];
		for (int j = 0; j < calibrationItemNames.size(); j++) {
			calibrationItems[j] = new CalibrationProduct(calibrationProducts.get(j).getProduct(),
					calibrationProducts.get(j).getTargetValue(), calibrationProducts.get(j).getWeight());
		}
		return calibrationItems;
	}

	public String[] getCalibrationItemNames(CalibrationInformation calibrationInformation) {

		ArrayList<String> calibrationItemNames = new ArrayList<>();

		for (int i = 0; i < calibrationInformation.getAtmNormalVolatilities().length; i++) {

			LocalDate exerciseDate = calibrationInformation.getCal().getDateFromDateAndOffsetCode(
					calibrationInformation.getReferenceDate(), calibrationInformation.getAtmExpiries()[i]);
			double exercise = calibrationInformation.getModelDC()
					.getDaycountFraction(calibrationInformation.getReferenceDate(), exerciseDate);

			if (exercise < 1.0) {
				continue;
			}

			try {
				calibrationItemNames.add(
						calibrationInformation.getAtmExpiries()[i] + "\t" + calibrationInformation.getAtmTenors()[i]);
			} catch (Exception e) {
				System.out.println("Adding the calibration Information to the calibration items in the LMM failed.");
			}
		}
		String[] calibrationItemNamesString = new String[calibrationItemNames.size()];
		for (int j = 0; j < calibrationItemNames.size(); j++) {
			calibrationItemNamesString[j] = calibrationItemNames.get(j);
		}

		return calibrationItemNamesString;

	}

	/**
	 * Get the Expiries of the products used for calibration.
	 * 
	 * @param calibrationInformation
	 * @return
	 * @ToDo Do this without code duplication
	 */
	public String[] getCalibrationItemExpiries(CalibrationInformation calibrationInformation) {

		ArrayList<String> calibrationItemExpiries = new ArrayList<>();

		for (int i = 0; i < calibrationInformation.getAtmNormalVolatilities().length; i++) {

			LocalDate exerciseDate = calibrationInformation.getCal().getDateFromDateAndOffsetCode(
					calibrationInformation.getReferenceDate(), calibrationInformation.getAtmExpiries()[i]);
			double exercise = calibrationInformation.getModelDC()
					.getDaycountFraction(calibrationInformation.getReferenceDate(), exerciseDate);

			if (exercise < 1.0) {
				continue;
			}

			try {
				calibrationItemExpiries.add(calibrationInformation.getAtmExpiries()[i]);
			} catch (Exception e) {
				System.out.println("Adding the calibration Information to the calibration items in the LMM failed.");
			}
		}
		String[] calibrationItemExpiriesString = new String[calibrationItemExpiries.size()];
		for (int j = 0; j < calibrationItemExpiries.size(); j++) {
			calibrationItemExpiriesString[j] = calibrationItemExpiries.get(j);
		}
		return calibrationItemExpiriesString;
	}

	/**
	 * Get the Tenors of the products used for calibration.
	 * 
	 * @param calibrationInformation
	 * @return
	 * @ToDo Do this without code duplication
	 */
	public String[] getCalibrationItemTenors(CalibrationInformation calibrationInformation) {

		ArrayList<String> calibrationItemTenors = new ArrayList<>();

		for (int i = 0; i < calibrationInformation.getAtmNormalVolatilities().length; i++) {

			LocalDate exerciseDate = calibrationInformation.getCal().getDateFromDateAndOffsetCode(
					calibrationInformation.getReferenceDate(), calibrationInformation.getAtmExpiries()[i]);
			double exercise = calibrationInformation.getModelDC()
					.getDaycountFraction(calibrationInformation.getReferenceDate(), exerciseDate);

			if (exercise < 1.0) {
				continue;
			}

			try {
				calibrationItemTenors.add(calibrationInformation.getAtmTenors()[i]);
			} catch (Exception e) {
				System.out.println("Adding the calibration Information to the calibration items in the LMM failed.");
			}
		}
		String[] calibrationItemExpiriesTenors = new String[calibrationItemTenors.size()];
		for (int j = 0; j < calibrationItemTenors.size(); j++) {
			calibrationItemExpiriesTenors[j] = calibrationItemTenors.get(j);
		}
		return calibrationItemExpiriesTenors;
	}

	/**
	 * Get the number of paths
	 * 
	 * @return
	 */
	public int getNumberOfPaths() {
		return numberOfPaths;
	}

	/**
	 * get the number of factors
	 * 
	 * @return
	 */
	public int getNumberOfFactors() {
		return numberOfFactors;
	}

	/**
	 * Get the calibrationInformation
	 * 
	 * @return
	 */
	public CalibrationInformation getCalibrationInformation() {
		return calibrationInformation;
	}

	/**
	 * Get the curveModelCalibrationmaschine
	 * 
	 * @return
	 */
	public CurveModelCalibrationMachine getCurveModelCalibrationMaschine() {
		return curveModelCalibrationMaschine;
	}

	/**
	 * Print the calibration test without forced calculation
	 */
	public void printCalibrationTest() {
		boolean forcedCalculation = false;
		printCalibrationTest(forcedCalculation);
	}

	/**
	 * @param forcedCalculation (boolean)
	 */
	public void printCalibrationTest(boolean forcedCalculation) {

		// Simulation properties
		double lastTime = 40.0;
		double dt = 0.25;
		TimeDiscretizationFromArray timeDiscretizationFromArray = new TimeDiscretizationFromArray(0.0,
				(int) (lastTime / dt), dt);
		BrownianMotion brownianMotion = new net.finmath.montecarlo.BrownianMotionLazyInit(timeDiscretizationFromArray,
				numberOfFactors, numberOfPaths, 31415 /* seed */);
		EulerSchemeFromProcessModel process = new EulerSchemeFromProcessModel(brownianMotion,
				EulerSchemeFromProcessModel.Scheme.EULER);
		LIBORModelMonteCarloSimulationModel simulationModel = null;
		try {
			simulationModel = getLIBORModelMonteCarloSimulationModel(process, forcedCalculation);
		} catch (SolverException e1) {
			System.out.println("Initiating the simulationModel in the calibration test failed.");
			e1.printStackTrace();
		} catch (CalculationException e1) {
			System.out.println("Initiating the simulationModel in the calibration test failed.");
			e1.printStackTrace();
		}

		CalibrationProduct[] calibrationItems = getCalibrationProducts();

		System.out.println("\nModel Name: " + getModelName());
		System.out.println("\nValuation on calibrated model:");
		double deviationSum = 0.0;
		double deviationSquaredSum = 0.0;
		String[] ItemNames = getCalibrationItemNames(calibrationInformation);
		for (int i = 0; i < calibrationItems.length; i++) {
			AbstractLIBORMonteCarloProduct calibrationProduct = calibrationItems[i].getProduct();
			try {
				double valueModel = calibrationProduct.getValue(simulationModel);
				double valueTarget = calibrationItems[i].getTargetValue().getAverage();
				double error = valueModel - valueTarget;
				deviationSum += error;
				deviationSquaredSum += error * error;
				System.out.println(ItemNames[i] + "\t" + "Model: " + "\t" + formatterValue.format(valueModel)
						+ "\t Target: " + "\t" + formatterValue.format(valueTarget) + "\t Deviation: " + "\t"
						+ formatterDeviation.format(valueModel - valueTarget));
			} catch (Exception e) {
				// System.out.println(ItemNames[i] + " did not work");
			}
		}

		double averageDeviation = deviationSum / calibrationItems.length;
		System.out.println("Mean Deviation: \t" + formatterDeviation.format(averageDeviation));
		System.out.println("RMS Error.....: \t"
				+ formatterDeviation.format(Math.sqrt(deviationSquaredSum / calibrationItems.length)));
		System.out.println("The calibration took " + getCalculationDuration() / 60000 + " min");
		System.out.println(
				"__________________________________________________________________________________________\n");

	}

	/**
	 * Get the Calibration Table (used for the Python GUI)
	 * 
	 * @param forcedCalculation (boolean)
	 * @return
	 * 
	 */
	public ArrayList<Map<String, Object>> getCalibrationTable(boolean forcedCalculation) {

		ArrayList<Map<String, Object>> OutTable = new ArrayList<Map<String, Object>>();

		// Simulation properties
		double lastTime = 40.0;
		double dt = 0.25;
		TimeDiscretizationFromArray timeDiscretizationFromArray = new TimeDiscretizationFromArray(0.0,
				(int) (lastTime / dt), dt);
		BrownianMotion brownianMotion = new net.finmath.montecarlo.BrownianMotionLazyInit(timeDiscretizationFromArray,
				numberOfFactors, numberOfPaths, 31415 /* seed */);
		EulerSchemeFromProcessModel process = new EulerSchemeFromProcessModel(brownianMotion,
				EulerSchemeFromProcessModel.Scheme.EULER);
		LIBORModelMonteCarloSimulationModel simulationModel = null;
		try {
			simulationModel = getLIBORModelMonteCarloSimulationModel(process, forcedCalculation);
		} catch (SolverException e1) {
			System.out.println("Initiating the simulationModel in the calibration test failed.");
			e1.printStackTrace();
		} catch (CalculationException e1) {
			System.out.println("Initiating the simulationModel in the calibration test failed.");
			e1.printStackTrace();
		}

		CalibrationProduct[] calibrationItems = getCalibrationProducts();
		String[] calibrationItemExpiries = getCalibrationItemExpiries(calibrationInformation);
		String[] calibrationItemTenors = getCalibrationItemTenors(calibrationInformation);

		double deviationSum = 0.0;
		double deviationSquaredSum = 0.0;
		int indexForOutTable = 0;
		for (int i = 0; i < calibrationItems.length; i++) {
			AbstractLIBORMonteCarloProduct calibrationProduct = calibrationItems[i].getProduct();
			try {
				double valueModel = calibrationProduct.getValue(simulationModel);
				double valueTarget = calibrationItems[i].getTargetValue().getAverage();
				double error = valueModel - valueTarget;
				deviationSum += error;
				deviationSquaredSum += error * error;

				Map<String, Object> OutTableRow = new HashMap<>();

				OutTableRow.put("Expiry", calibrationItemExpiries[i]);
				OutTableRow.put("Tenor", calibrationItemTenors[i]);
				OutTableRow.put("Model_Value", valueModel);
				OutTableRow.put("Target", valueTarget);
				OutTableRow.put("Deviation", formatterVolatility.format(Math.abs(valueModel - valueTarget)));

				OutTable.add(indexForOutTable, OutTableRow);
				indexForOutTable += 1;

			} catch (Exception e) {

			}
		}

		Map<String, Object> OutTableRow = new HashMap<String, Object>();

		double averageDeviation = deviationSum / calibrationItems.length;
		OutTableRow.put("Mean Deviation", formatterDeviation.format(averageDeviation));
		OutTableRow.put("RMS Error",
				formatterDeviation.format(Math.sqrt(deviationSquaredSum / calibrationItems.length)));

		OutTable.add(OutTable.size(), OutTableRow);

		return OutTable;

	}

	/**
	 * Get the Calibration Table (used for the Python GUI)
	 * 
	 * @param forcedCalculation (boolean)
	 * @return
	 * 
	 */

	/**
	 * Get the Calibration table with a different input for the calibration Items.
	 * For example if the model is calibrated on co-terminals you can use this to
	 * observe how good the calibration works on the full surface.
	 * 
	 * @param forcedCalculation
	 * @param calibrationItems
	 * @param calibrationItemExpiries
	 * @param calibrationItemTenors
	 * @return
	 */
	public ArrayList<Map<String, Object>> getCalibrationTableForDifferntCalibrationInput(boolean forcedCalculation,
			CalibrationProduct[] calibrationItems, String[] calibrationItemExpiries, String[] calibrationItemTenors) {

		ArrayList<Map<String, Object>> OutTable = new ArrayList<Map<String, Object>>();

		// Simulation properties
		double lastTime = 40.0;
		double dt = 0.25;
		TimeDiscretizationFromArray timeDiscretizationFromArray = new TimeDiscretizationFromArray(0.0,
				(int) (lastTime / dt), dt);
		BrownianMotion brownianMotion = new net.finmath.montecarlo.BrownianMotionLazyInit(timeDiscretizationFromArray,
				numberOfFactors, numberOfPaths, 31415 /* seed */);
		EulerSchemeFromProcessModel process = new EulerSchemeFromProcessModel(brownianMotion,
				EulerSchemeFromProcessModel.Scheme.EULER);
		LIBORModelMonteCarloSimulationModel simulationModel = null;
		try {
			simulationModel = getLIBORModelMonteCarloSimulationModel(process, forcedCalculation);
		} catch (SolverException e1) {
			System.out.println("Initiating the simulationModel in the calibration test failed.");
			e1.printStackTrace();
		} catch (CalculationException e1) {
			System.out.println("Initiating the simulationModel in the calibration test failed.");
			e1.printStackTrace();
		}

//		CalibrationProduct[] calibrationItems = getCalibrationProducts();
//		String[] calibrationItemExpiries = getCalibrationItemExpiries(calibrationInformation);
//		String[] calibrationItemTenors = getCalibrationItemTenors(calibrationInformation);

		double deviationSum = 0.0;
		double deviationSquaredSum = 0.0;
		int indexForOutTable = 0;
		for (int i = 0; i < calibrationItems.length; i++) {
			AbstractLIBORMonteCarloProduct calibrationProduct = calibrationItems[i].getProduct();
			try {
				double valueModel = calibrationProduct.getValue(simulationModel);
				double valueTarget = calibrationItems[i].getTargetValue().getAverage();
				double error = valueModel - valueTarget;
				deviationSum += error;
				deviationSquaredSum += error * error;

				Map<String, Object> OutTableRow = new HashMap<>();

				OutTableRow.put("Expiry", calibrationItemExpiries[i]);
				OutTableRow.put("Tenor", calibrationItemTenors[i]);
				OutTableRow.put("Model_Value", valueModel);
				OutTableRow.put("Target", valueTarget);
				OutTableRow.put("Deviation", formatterVolatility.format(Math.abs(valueModel - valueTarget)));

				OutTable.add(indexForOutTable, OutTableRow);
				indexForOutTable += 1;

			} catch (Exception e) {

			}
		}

		Map<String, Object> OutTableRow = new HashMap<String, Object>();

		double averageDeviation = deviationSum / calibrationItems.length;
		OutTableRow.put("Mean Deviation", formatterDeviation.format(averageDeviation));
		OutTableRow.put("RMS Error",
				formatterDeviation.format(Math.sqrt(deviationSquaredSum / calibrationItems.length)));

		OutTable.add(OutTable.size(), OutTableRow);

		return OutTable;

	}

	/**
	 * Needs to be overwritten!!! Get a LIBORModelMonteCarloSimulationModel with a
	 * specific process <br>
	 * If the model is already calibrated it is not done again
	 * 
	 * @param process
	 * @return
	 * @throws SolverException
	 */
	public LIBORModelMonteCarloSimulationModel getLIBORModelMonteCarloSimulationModel(MonteCarloProcess process)
			throws SolverException {
		System.out.println(
				"The method getLIBORModelMonteCarloSimulationModel(MonteCarloProcess process) needs to be overwritten inside the calibration machine.");
		return null;
	}

}
