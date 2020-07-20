package kellerstrass.ModelCalibration;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import kellerstrass.marketInformation.CalibrationInformation;
import net.finmath.exception.CalculationException;
import net.finmath.montecarlo.BrownianMotion;
import net.finmath.montecarlo.interestrate.LIBORModelMonteCarloSimulationModel;
import net.finmath.montecarlo.process.EulerSchemeFromProcessModel;
import net.finmath.optimizer.SolverException;
import net.finmath.time.TimeDiscretizationFromArray;

/**
 * The class DataSetCalibration is build to calibrate different models using
 * sets of calibration data. <br>
 * A possible operation area is the calibration to a set of different days, e.g.
 * a year of information.
 * 
 * @author lucak
 *
 */
public class DataSetCalibration {
	private static DecimalFormat formatterPercentage = new DecimalFormat(" ##0.000%;-##0.000%",
			new DecimalFormatSymbols(Locale.ENGLISH));

	private int numberOfPaths = 5000;

	// Simulation time discretization
	private double lastTime = 40.0;
	private double dt = 0.25;
	private TimeDiscretizationFromArray timeDiscretizationFromArray = new TimeDiscretizationFromArray(0.0,
			(int) (lastTime / dt), dt);
	private CalibrationInformation[] calibrationInformations;
	private CurveModelCalibrationMachine[] curveModelCalibrationMaschines;

	public DataSetCalibration(CalibrationInformation[] calibrationInformations,
			CurveModelCalibrationMachine[] curveModelCalibrationMaschines) throws SolverException {
		this.curveModelCalibrationMaschines = curveModelCalibrationMaschines;
		this.calibrationInformations = calibrationInformations;
	}

	/**
	 * Does nothing else than calibrating LIBOR Market Models using the given
	 * calibrations information
	 */
	public void calibrateLMMs() {

		for (int i = 0; i < curveModelCalibrationMaschines.length; i++) {
			try {
				System.out.println("Calibration started for " + calibrationInformations[i].getName());

				// brownian motion
				BrownianMotion brownianMotionM1 = new net.finmath.montecarlo.BrownianMotionLazyInit(
						timeDiscretizationFromArray, 3, numberOfPaths, 31415 /* seed */);
				// process
				EulerSchemeFromProcessModel process1 = new EulerSchemeFromProcessModel(brownianMotionM1,
						EulerSchemeFromProcessModel.Scheme.EULER);
				CalibrationMachineInterface Model1CalibrationMaschine = new LmmCalibrationMachine(numberOfPaths, 3,
						calibrationInformations[i], curveModelCalibrationMaschines[i]);
				// simulation machine
				try {
					LIBORModelMonteCarloSimulationModel LmmSimulationModel = Model1CalibrationMaschine
							.getLIBORModelMonteCarloSimulationModel(process1, false);
				} catch (SolverException e) {
					System.out.println(
							"Creating the simulation model for the LMM in DataSetCalibration.calibrateLMMs() failed.");
					e.printStackTrace();
				} catch (CalculationException e) {
					System.out.println(
							"Creating the simulation model for the LMM in DataSetCalibration.calibrateLMMs() failed.");
					e.printStackTrace();
				}

				System.out.println("Calibration successful for \t" + Model1CalibrationMaschine.getModelName());
				System.out.println("The calibration took \t" + Model1CalibrationMaschine.getCalculationDuration());
				System.out.println("Progress: \t"
						+ formatterPercentage.format((double) i / (double) curveModelCalibrationMaschines.length));
			} catch (Exception e) {
				System.out.println("Calibrating the LMM in DataSetCalibration.calibrateLMMs() failed.");
				e.printStackTrace();
			}
		}
	}

	/**
	 * Does nothing else than calibrating Hull White Models using the given
	 * calibrations information
	 */
	public void calibrateHWs() {

		for (int i = 0; i < curveModelCalibrationMaschines.length; i++) {
			try {
				System.out.println("Calibration started for " + calibrationInformations[i].getName());

				// brownian motion
				BrownianMotion brownianMotionM1 = new net.finmath.montecarlo.BrownianMotionLazyInit(
						timeDiscretizationFromArray, 2, numberOfPaths, 31415 /* seed */);
				// process
				EulerSchemeFromProcessModel process1 = new EulerSchemeFromProcessModel(brownianMotionM1,
						EulerSchemeFromProcessModel.Scheme.EULER);
				CalibrationMachineInterface Model1CalibrationMaschine = new HWCalibrationMachine(numberOfPaths, 2,
						calibrationInformations[i], curveModelCalibrationMaschines[i]);
				// simulation machine
				try {
					LIBORModelMonteCarloSimulationModel HwSimulationModel = Model1CalibrationMaschine
							.getLIBORModelMonteCarloSimulationModel(process1, false);
				} catch (SolverException e) {
					System.out.println(
							"Creating the simulation model for the HWM in DataSetCalibration.calibrateHWs() failed.");
					e.printStackTrace();
				} catch (CalculationException e) {
					System.out.println(
							"Creating the simulation model for the HWM in DataSetCalibration.calibrateHWs() failed.");
					e.printStackTrace();
				}

				System.out.println("Calibration successful for \t" + Model1CalibrationMaschine.getModelName());
				System.out.println("The calibration took \t" + Model1CalibrationMaschine.getCalculationDuration());
				System.out.println("Progress: \t"
						+ formatterPercentage.format((double) i / (double) curveModelCalibrationMaschines.length));

			} catch (Exception e) {
				System.out.println("Calibrating the HWM in DataSetCalibration.calibrateHWs() failed.");
				e.printStackTrace();
			}
		}
	}

}
