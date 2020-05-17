package kellerstrass.ModelCalibration;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Locale;

import kellerstrass.Calibration.CurveModelCalibrationItem;
import kellerstrass.marketInformation.CalibrationInformation;
import kellerstrass.marketInformation.DataScope;
import kellerstrass.marketInformation.DataSource;
import net.finmath.exception.CalculationException;
import net.finmath.marketdata.model.AnalyticModel;
import net.finmath.marketdata.model.curves.DiscountCurve;
import net.finmath.marketdata.model.curves.ForwardCurve;
import net.finmath.montecarlo.BrownianMotion;
import net.finmath.montecarlo.interestrate.CalibrationProduct;
import net.finmath.montecarlo.interestrate.LIBORModel;
import net.finmath.montecarlo.interestrate.LIBORModelMonteCarloSimulationModel;
import net.finmath.montecarlo.interestrate.products.AbstractLIBORMonteCarloProduct;
import net.finmath.montecarlo.process.EulerSchemeFromProcessModel;
import net.finmath.montecarlo.process.MonteCarloProcess;
import net.finmath.optimizer.SolverException;
import net.finmath.time.TimeDiscretizationFromArray;

public abstract class AbstractCalibrationMaschine implements CalibrationMaschineInterface {
	
	private static DecimalFormat formatterValue		= new DecimalFormat(" ##0.000%;-##0.000%", new DecimalFormatSymbols(Locale.ENGLISH));
	private static DecimalFormat formatterParam		= new DecimalFormat(" #0.0000;-#0.0000", new DecimalFormatSymbols(Locale.ENGLISH));
	private static DecimalFormat formatterDeviation	= new DecimalFormat(" 0.00000E00;-0.00000E00", new DecimalFormatSymbols(Locale.ENGLISH));

	
	
	
	
	
	// Input Parameters
	protected int numberOfPaths;
	protected int numberOfFactors;
	protected CalibrationInformation calibrationInformation;
	protected String modelName;
	protected CurveModelCalibrationMaschine curveModelCalibrationMaschine;

	// Class intern parameters
	protected double calculationDuration;
	protected AnalyticModel curveModel;
	protected ForwardCurve forwardCurve;
	protected DiscountCurve discountCurve;
	
	//private intern used variables
	private int NumberOfCalibrationItems;
	


	
	





	/**The Model name and other model specific input need to be added in the specific CalibrationMaschine class.
	 * 
	 * @param numberOfPaths
	 * @param calibrationInformation
	 * @param curveModelDataType
	 * @param calculationDuration
	 */

	public AbstractCalibrationMaschine(int numberOfPaths,int numberOfFactors, CalibrationInformation calibrationInformation,
			CurveModelCalibrationMaschine curveModelCalibrationMaschine) {
		super();
		this.numberOfPaths = numberOfPaths;
		this.numberOfFactors = numberOfFactors;
		this.calibrationInformation = calibrationInformation;
		this.curveModelCalibrationMaschine = curveModelCalibrationMaschine;
		this.NumberOfCalibrationItems = calibrationInformation.getAtmNormalVolatilities().length;
		
		
		
		// Fill in the class calibration maschine intern parameters
		try {
			this.curveModel = this.curveModelCalibrationMaschine.getCalibratedCurve();
		} catch (SolverException e) {
			System.out.println("adding the curve model to the AbstractCalibrationMaschine failed ");
			e.printStackTrace();
		}
		 this.forwardCurve = curveModel
				.getForwardCurve("ForwardCurveFromDiscountCurve(discountCurve-EUR,6M)");
		 this.discountCurve = curveModel.getDiscountCurve("discountCurve-EUR");
		
		 
		 
		
	}






	public ForwardCurve getForwardCurve() {
		return forwardCurve;
	}






	public DiscountCurve getDiscountCurve() {
		return discountCurve;
	}






	/**
	 * Get the name of the model
	 */
	public String getModelName() {
		return modelName;
	}	
	
	
	
	/**
	 * Get the calculation duration of the calibration in milli seconds
	 * <br>
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
			//x.printStackTrace();

		} catch (IOException k) {
			calculationDuration = -1.0;
			System.out.println("Calibration not done yet. Get the calculation duration after Calibration.");
			//k.printStackTrace();
		}

		return calculationDuration;
	}
	
	
	/**
	 * Transform the Calibration information into an Array of Calibration products 
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

			int numberOfPeriods = (int) Math.round(tenor / calibrationInformation.getSwapPeriodLength());

			double moneyness = 0.0;
			double targetVolatility = calibrationInformation.getAtmNormalVolatilities()[i];

			String targetVolatilityType = calibrationInformation.getTargetVolatilityType();

			double weight = 1.0;

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
			
		}
		
		CalibrationProduct[] calibrationItems = new CalibrationProduct[calibrationItemNames.size()];
		for(int j=0; j<calibrationItemNames.size(); j++) {
			calibrationItems[j] = new CalibrationProduct(calibrationProducts.get(j).getProduct(),calibrationProducts.get(j).getTargetValue(),calibrationProducts.get(j).getWeight());
		}
		
		
		
		return calibrationItems;
	}



	public String[] getCalibrationItemNames(CalibrationInformation calibrationInformation) {
		
		ArrayList<String> calibrationItemNames = new ArrayList<>();
		
		for (int i = 0; i < calibrationInformation.getAtmNormalVolatilities().length; i++) {
			try {
				calibrationItemNames.add(
						calibrationInformation.getAtmExpiries()[i] + "\t" + calibrationInformation.getAtmTenors()[i]);
			} catch (Exception e) {
				System.out.println("Adding the calibration Information to the calibration items in the LMM failed.");
			}	
		}
		String[] calibrationItemNamesString = new String[calibrationItemNames.size()];
		for(int j=0; j<calibrationItemNames.size(); j++) {
			calibrationItemNamesString[j] = calibrationItemNames.get(j);
		}
		
		
		return calibrationItemNamesString;
		
		
		
		
	}


/**
 * Get the number of paths
 * @return
 */
	public int getNumberOfPaths() {
		return numberOfPaths;
	}





/**
 * get the number of factors
 * @return
 */
	public int getNumberOfFactors() {
		return numberOfFactors;
	}





/**
 * Get the calibrationInformation
 * @return
 */
	public CalibrationInformation getCalibrationInformation() {
		return calibrationInformation;
	}





/**
 * Get the curveModelCalibrationmaschine
 * @return
 */
	public CurveModelCalibrationMaschine getCurveModelCalibrationMaschine() {
		return curveModelCalibrationMaschine;
	}



	public void printCalibrationTest() {
		boolean forcedCalculation = false;
		printCalibrationTest(forcedCalculation);
	}
	
	 
		public void printCalibrationTest(boolean forcedCalculation) {
			
			
             // Simulation properties
			double lastTime = 40.0;
			double dt = 0.25;
			TimeDiscretizationFromArray timeDiscretizationFromArray = new TimeDiscretizationFromArray(0.0,(int) (lastTime / dt), dt);
			BrownianMotion brownianMotion = new net.finmath.montecarlo.BrownianMotionLazyInit(timeDiscretizationFromArray, numberOfFactors , numberOfPaths, 31415 /* seed */);
			EulerSchemeFromProcessModel process = new EulerSchemeFromProcessModel(brownianMotion, EulerSchemeFromProcessModel.Scheme.EULER);
			LIBORModelMonteCarloSimulationModel simulationModel = null;
			try {
				simulationModel = getLIBORModelMonteCarloSimulationModel(process,forcedCalculation);
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
			double deviationSum			= 0.0;
			double deviationSquaredSum	= 0.0;
			for (int i = 0; i < calibrationItems.length; i++) {
				AbstractLIBORMonteCarloProduct calibrationProduct = calibrationItems[i].getProduct();
				try {
					double valueModel = calibrationProduct.getValue(simulationModel);
					double valueTarget = calibrationItems[i].getTargetValue().getAverage();
					double error = valueModel-valueTarget;
					deviationSum += error;
					deviationSquaredSum += error*error;
					System.out.println(getCalibrationItemNames(calibrationInformation)[i] + "\t" + "Model: "+ "\t" + formatterValue.format(valueModel) + "\t Target: "+ "\t" + formatterValue.format(valueTarget) + "\t Deviation: "+ "\t" + formatterDeviation.format(valueModel-valueTarget));
				}
				catch(Exception e) {
				}
			}
			double averageDeviation = deviationSum/calibrationItems.length;
			System.out.println("Mean Deviation: \t" + formatterDeviation.format(averageDeviation));
			System.out.println("RMS Error.....: \t" + formatterDeviation.format(Math.sqrt(deviationSquaredSum/calibrationItems.length)));
			System.out.println("__________________________________________________________________________________________\n");
		 
	 }

		/**
		 * Needs to be overwritten!!!
	 * Get a LIBORModelMonteCarloSimulationModel with a specific process
	 * <br>
	 * If the model is already calibrated it is not done again 
	 * @param process
	 * @return
	 * @throws SolverException
	 */
	public LIBORModelMonteCarloSimulationModel getLIBORModelMonteCarloSimulationModel(MonteCarloProcess process) throws SolverException {
		return null;
	}

	
	
	

}
