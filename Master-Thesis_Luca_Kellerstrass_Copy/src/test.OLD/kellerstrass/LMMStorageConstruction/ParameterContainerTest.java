package kellerstrass.LMMStorageConstruction;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;

import kellerstrass.Calibration.CurveModelCalibrationItem;
import net.finmath.exception.CalculationException;
import net.finmath.marketdata.model.AnalyticModel;
import net.finmath.marketdata.model.curves.DiscountCurve;
import net.finmath.marketdata.model.curves.ForwardCurve;
import net.finmath.montecarlo.BrownianMotion;
import net.finmath.montecarlo.interestrate.CalibrationProduct;
import net.finmath.optimizer.SolverException;
import net.finmath.time.TimeDiscretization;
import net.finmath.time.TimeDiscretizationFromArray;
import net.finmath.time.businessdaycalendar.BusinessdayCalendarExcludingTARGETHolidays;
import net.finmath.time.daycount.DayCountConvention_ACT_365;

public class ParameterContainerTest {

	public static void main(String[] args) throws SolverException, CalculationException {
	
	//Name	
		String name = "Testname";
	//Brownian motion	
		double lastTime	= 40.0;
		double dt		= 0.25;
		TimeDiscretizationFromArray timeDiscretizationFromArray = new TimeDiscretizationFromArray(0.0, (int) (lastTime / dt), dt);
		final TimeDiscretization liborPeriodDiscretization = timeDiscretizationFromArray;
		final int numberOfPaths		= 1000;
		final int numberOfFactors	= 3;
		
		/*
		 * Create Brownian motion
		 */
		BrownianMotion brownianMotion = new net.finmath.montecarlo.BrownianMotionLazyInit(timeDiscretizationFromArray, numberOfFactors, numberOfPaths, 31415 /* seed */);
	//Curves
		final AnalyticModel curveModel = CurveModelCalibrationItem.getCalibratedCurve();
		// Create the forward curve (initial value of the LIBOR market model)
				final ForwardCurve forwardCurve = curveModel.getForwardCurve("ForwardCurveFromDiscountCurve(discountCurve-EUR,6M)");
				final DiscountCurve discountCurve = curveModel.getDiscountCurve("discountCurve-EUR");
					
				/*
				 * Create a set of calibration products.
				 */
				ArrayList<String>					calibrationItemNames	= new ArrayList<>();
				final ArrayList<CalibrationProduct>	calibrationProducts		= new ArrayList<>();
				
				double	swapPeriodLength	= 0.5;
				
				//Create co-terminals (atmExpiry + atmTenor = 11Y)
						String[] atmExpiries = {"1Y", "2Y", "3Y", "4Y", "5Y", "7Y", "10Y"};
						String[] atmTenors = {"1Y", "4Y", "6Y", "7Y", "8Y", "9Y", "10Y"};
						
						double[] atmNormalVolatilities = {0.00504, 0.005, 0.00495, 0.00454, 0.00418, 0.00404, 0.00394};
						
						LocalDate referenceDate = LocalDate.of(2016, Month.SEPTEMBER, 30);
						BusinessdayCalendarExcludingTARGETHolidays cal = new BusinessdayCalendarExcludingTARGETHolidays();
						DayCountConvention_ACT_365 modelDC = new DayCountConvention_ACT_365();
						
				
						for(int i=0; i<atmNormalVolatilities.length; i++ ) {

							LocalDate exerciseDate = cal.getDateFromDateAndOffsetCode(referenceDate, atmExpiries[i]);
							LocalDate tenorEndDate = cal.getDateFromDateAndOffsetCode(exerciseDate, atmTenors[i]);
							double	exercise		= modelDC.getDaycountFraction(referenceDate, exerciseDate);
							double	tenor			= modelDC.getDaycountFraction(exerciseDate, tenorEndDate);

							// We consider an idealized tenor grid (alternative: adapt the model grid)
							// To ensure the dates fit into the timediscretization
							exercise	= Math.round(exercise/0.25)*0.25;
							tenor		= Math.round(tenor/0.25)*0.25;

							if(exercise < 1.0) {
								continue;
							}

							int numberOfPeriods = (int)Math.round(tenor / swapPeriodLength);

							double	moneyness			= 0.0;
							double	targetVolatility	= atmNormalVolatilities[i];

							String	targetVolatilityType = "VOLATILITYNORMAL";

							double	weight = 1.0;

							calibrationProducts.add(CurveModelCalibrationItem.createCalibrationItem(weight, exercise, swapPeriodLength, numberOfPeriods, moneyness, targetVolatility, targetVolatilityType, forwardCurve, discountCurve));
							calibrationItemNames.add(atmExpiries[i]+"\t"+atmTenors[i]);
						
						}
		CalibrationParameterContainer parameterContainer1 =
				new CalibrationParameterContainer(name, brownianMotion, forwardCurve, discountCurve);
		






		
		
/***********Store the the ParamterContainer*********************************************************************/
		//Store the the ParamterContainer
		String nameOfStoredItem = "ParameterContainer1";
		
		try {
			File directory = new File("temp");
			if (!directory.exists()){
				directory.mkdir();
			}

			FileOutputStream fileOut = new FileOutputStream ("temp/"+ nameOfStoredItem+ ".ser");
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(parameterContainer1);
			out.close();
			fileOut.close();
			System.out.println("Serianlized data is saved in temp/" +nameOfStoredItem + ".ser");

		} catch (IOException j) {
			j.printStackTrace();
		}
/********************************************************************************/		

		
		
		
/***********Restore the the ParamterContainer*********************************************************************/	
		CalibrationParameterContainer parameterContainer2;
		
		
		try {FileInputStream fileIn = new FileInputStream("temp/"+ nameOfStoredItem+ ".ser");
		ObjectInputStream in = new ObjectInputStream(fileIn);
		parameterContainer2 = (CalibrationParameterContainer)  in.readObject();
		in.close();
		fileIn.close();
			
		} catch (IOException k) {
			k.printStackTrace();
			parameterContainer2 = null;
		} catch (ClassNotFoundException c) {
			System.out.println("Employee class not found");
			c.printStackTrace();
			parameterContainer2 = null;
		}
		
		
/******************************************************************************/	

		
// Test it
		System.out.println("name = "+ parameterContainer2.name);
		System.out.println("Brownian Information = "+ parameterContainer2.brownianMotion);
		System.out.println("forward Curve Information = "+ parameterContainer2.forwardCurve);
		System.out.println("discount Curve Information = "+ parameterContainer2.discountCurve);
		
		System.out.println("Calibration Names Information = "+ parameterContainer2.calibrationItemNames.size());
		System.out.println("Calibration Products Information = "+ parameterContainer2.calibrationProducts.size());
	}

}
