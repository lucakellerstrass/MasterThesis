package kellerstrass.defaultProbability.test;


import kellerstrass.defaultProbability.bootstrapping.*;
import kellerstrass.useful.PaymentFrequency;
import net.finmath.time.ScheduleGenerator.Frequency;
import net.finmath.time.TimeDiscretization;
import net.finmath.time.TimeDiscretizationFromArray;




public class BootstrappingTest {

	 public static void main(String[] args) throws Exception {
		  // Input parameters
		  double interestRate = 0.05; 	
		  double Recovery 	= 0.40;	//Recovery Rate

		  double[] cdsSpreads = {300.0, 350.0, 400.0, 450.0, 500.0 } ; //{20.0, 25.0, 30.0, 35.0, 40.0}   // The (yearly) CDS spreads in bp {320.0, 57.0, 132.0 , 139.0 , 146.0, 150.0, 154.0}    ||  {300.0, 350.0, 400.0, 450.0, 500.0 }
		  
		  //double[] yearlyLIBORs = {0.0360758241758242, 0.0336, 0.0343334648351648, 0.03589, 0.0375295128378378, 0.0390253032258064,0.0405272967032967 };
		  
		  //ForwardBootstrap initiation
		  double calculatorTimeStart =System.currentTimeMillis();
		  ForwardBootstrap bootStrapper = new ForwardBootstrap(interestRate, Recovery, cdsSpreads, PaymentFrequency.QUATERLY);
		  double calculatorTimeEnd =System.currentTimeMillis();
		  double ForwardBootstrapInitialization = (calculatorTimeEnd - calculatorTimeStart);
		  System.out.println("ForwardBootstrap Initialization took  " +ForwardBootstrapInitialization  + " millis");
		  
		  
		  //SimpleApproximation initiation
		  double SimpleApproximationTimeStart =System.currentTimeMillis();
		  SimpleApproximation approximator = new SimpleApproximation( Recovery, cdsSpreads);
		  double SimpleApproximationTimeEnd =System.currentTimeMillis();
		  double SimpleApproximationInitialization = (SimpleApproximationTimeEnd - SimpleApproximationTimeStart);
		  System.out.println("SimpleApproximation Initialization took  " + SimpleApproximationInitialization  + " millis");
		  
		  
		 		  
		  
		  // get the ForwardBootstrap  default Probabilities
		  double ForwardBootstrapDefaultProbabilitiesTimeStart =System.currentTimeMillis();
		  double[] defaultProbsForwardBootstrap = bootStrapper.getOneYearDefaultProbs();
		  double ForwardBootstrapDefaultProbabilitiesTimeEnd =System.currentTimeMillis();
		  double defaultProbabilitiesFromForwardBootstrap = (ForwardBootstrapDefaultProbabilitiesTimeEnd - ForwardBootstrapDefaultProbabilitiesTimeStart);
		  System.out.println(" default Probabilities from ForwardBootstrap took  " + defaultProbabilitiesFromForwardBootstrap + " millis");

		  
		// get the SimpleApproximation  default Probabilities
		  double SimpleApproximationDefaultProbabilitiesTimeStart =System.currentTimeMillis();
		  double[] defaultProbsSimpleApproximation = approximator.getOneYearDefaultProbs();
		  double SimpleApproximationDefaultProbabilitiesTimeEnd =System.currentTimeMillis();
		  double defaultProbabilitiesFromSimpleApproximation = (SimpleApproximationDefaultProbabilitiesTimeEnd - SimpleApproximationDefaultProbabilitiesTimeStart);
		  System.out.println(" default Probabilities from SimpleApproximation took  " + defaultProbabilitiesFromSimpleApproximation + " millis");
		  
		  
		 /* 
		  
		  System.out.println(" Index" + "\t" + "Time" + "\t" + "defaultProbsForwardBootstrap" + "\t" + "defaultProbsSimpleApproximation" );
		  for(int i = 0 ; i < defaultProbsForwardBootstrap.length ; i++) {
		   
		   System.out.println( i + "\t" +  i + "\t" + defaultProbsForwardBootstrap[i] + "\t       " + defaultProbsSimpleApproximation[i]);
		   
		  }
		  System.out.println( "Calculation time:"+ "\t" + "\t" +
				 ( ForwardBootstrapInitialization +defaultProbabilitiesFromForwardBootstrap) +  "\t        " +
				 (SimpleApproximationInitialization + defaultProbabilitiesFromSimpleApproximation) + "\t           " + "millis" );
		  
          */
		  
		  
		  /*Now we want to test if the marginal default probs which are calculated here, 
		   * are the same as they are in the excel sheet	  
			 */
			double lastTime	= 15.0;
			double dt		= 0.05;
			TimeDiscretizationFromArray timeDiscretization = new TimeDiscretizationFromArray(0.0, (int) (lastTime / dt), dt);
            
			double[] discountCurve = getDiscountfactors(interestRate, timeDiscretization);
			
			ForwardBootstrap bootStrapper2 = new ForwardBootstrap(discountCurve, Recovery, cdsSpreads, PaymentFrequency.QUATERLY, timeDiscretization);
			
			bootStrapper2.printMarginalDefaultprobs();
			// Check if anything changed, but at 18.10.2019   13:51  it was correct
			
			
	
		  
		  
		  
		  
		  
		  
		  
		 }
	 
	 
		private static double[] getDiscountfactors(double interestRate, TimeDiscretization timeDiscretization2) {
			// System.out.println("calculateDiscountfactors()");
			double[] InternDiscountFactors = new double[timeDiscretization2.getNumberOfTimeSteps() + 1];
			for (int timeIndex = 0; timeIndex < timeDiscretization2.getNumberOfTimeSteps() + 1; timeIndex++) {
				InternDiscountFactors[timeIndex] = Math.exp(-timeDiscretization2.getTime(timeIndex) * interestRate);
			}
			return InternDiscountFactors;

		}
		}

