package kellerstrass.defaultProbability.test;

import java.text.DecimalFormat;

import java.text.DecimalFormatSymbols;

import java.util.Locale;

import org.junit.Test;

import kellerstrass.defaultProbability.bootstrapping.*;

import kellerstrass.useful.PaymentFrequency;

import net.finmath.time.TimeDiscretization;

import net.finmath.time.TimeDiscretizationFromArray;

public class BootstrappingTestOLD {

	private static DecimalFormat formatterParam = new DecimalFormat(" #0.000;-#0.000",
			new DecimalFormatSymbols(Locale.ENGLISH));

	private static DecimalFormat formatterDeviation = new DecimalFormat(" 0.00000E00;-0.00000E00",
			new DecimalFormatSymbols(Locale.ENGLISH));

	@Test
	public void test() throws Exception {

		// Input parameters

		double interestRate = 0.05;

		double Recovery = 0.40; // Recovery Rate

		double endDate = 15.0;
 
		double deltaT = 1.0; //0.5; //0.05;

		TimeDiscretization timeDiscretization = new TimeDiscretizationFromArray(0.0, (int) (endDate / deltaT), deltaT);

		//+++++++++++++++++++++ Forward bootstrapping
		
		double[] cdsSpreads =  { 6, 9, 15, 22, 35, 40,  45, 45.67, 46.33, 47};
			//Main Test: { 254, 262.75, 271.5, 280.25, 289, 290.2, 291.4, 292.6, 293.8, 295 };
		// deutsche bank 71.2 //{20.0, 25.0, 30.0, 35.0, 40.0}
		// The (yearly) CDS spreads in bp {320.0, 57.0, 132.0 , 139.0 , 146.0, 150.0,
		// 154.0}
		// {300.0, 350.0, 400.0, 450.0, 500.0 }
		

		// BayernLB from 2011: 1y: 264, 5y 289, 10y: 295

		// => {254, 262.75, 271.5, 280.25, 289, 290.2, 291.4, 292.6, 293.8, 295}


		// ForwardBootstrap initiation

		ForwardBootstrap bootStrapper = new ForwardBootstrap(interestRate, Recovery, cdsSpreads,
				PaymentFrequency.QUATERLY, timeDiscretization);

		//++++++++++++++++++++++++ SimpleApproximation initiation

		double[] values = { 6, 9, 15, 22, 35, 45, 47};  //Real counterparty test
			
			//Main Test: { 254, 289, 295 };

		String[] maturityCodes = { "1y", "2y", "3y","4y","5y","7y","10y" };
			
			//Main Test; { "1y", "5y", "10y" };

		SimpleApproximation approximator = new SimpleApproximation(Recovery, values, maturityCodes);

		double[] marginalDefaultProbsForwardBootstrap = bootStrapper.getInternMarginalDefaultProbabilities();

		System.out.println("TImeInterval  \t  default Prob Bootstrapped  \t  default prob Simple  \t  deviation");

		for (int i = 1; i < timeDiscretization.getNumberOfTimes(); i++) {

			double timeStart = timeDiscretization.getTime(i - 1);

			double timeEnd = timeDiscretization.getTime(i);

			System.out.print(
					"[" + formatterParam.format(timeStart) + " , " + formatterParam.format(timeEnd) + "]" + "\t");

			System.out.print(marginalDefaultProbsForwardBootstrap[i] + "\t");

			System.out.print(approximator.getDefaultProbForTimeInterval(timeStart, timeEnd) + "\t");

			System.out.print(

					formatterDeviation.format(Math.abs(marginalDefaultProbsForwardBootstrap[i]

							- approximator.getDefaultProbForTimeInterval(timeStart, timeEnd))));

			System.out.println("");

		}

	}

}