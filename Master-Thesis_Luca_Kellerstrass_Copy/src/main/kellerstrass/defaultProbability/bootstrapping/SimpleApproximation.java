package kellerstrass.defaultProbability.bootstrapping;


import net.finmath.time.TimeDiscretization;
import net.finmath.time.TimeDiscretizationFromArray;


/**
 * This class implements a very simple method to extract the default probability from cds spreads.
 * @author lucak
 *
 */
public class SimpleApproximation extends AbstractCdsImpliesPDsTODO {

	// CDS information
		private double recovery; // Recovery Rate
		private double[] cdsSpreads; // The (yearly) CDS spreads in bp
		
   
		// The timediscretization
		private double deltaT;
		private int NumberOfTimesteps;
		private TimeDiscretization timeDiscretization;
	
		
		
		/*
		 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		 * ++
		 */
		/* Constructors */
		
		
		/**
		 * The most basic Constructor <br>
		 * the default time Discretization with NumberOfTimeStepsPerYear of 20
		 *
		 * @param recovery     The Recovery Rate
		 * @param cdsSpreads   The (yearly) CDS spreads in bp
		 */
		public SimpleApproximation(double recovery, double[] cdsSpreads) {
			this( recovery, cdsSpreads, 20);
		}
		
				
		/**
		 * A more advanced Constructor <br>
		 * Has the possibility to set the NumberOfTimeStepsPerYear
		 *
		 * @param recovery     The Recovery Rate
		 * @param cdsSpreads   The (yearly) CDS spreads in bp
		 * @param NumberOfTimeStepsPerYear
		 */
		public SimpleApproximation( double recovery, double[] cdsSpreads, 
				int NumberOfTimeStepsPerYear) {
			this( recovery, cdsSpreads, 
					new TimeDiscretizationFromArray(0.0, (int) (cdsSpreads.length / (1.0 / NumberOfTimeStepsPerYear)),
							(double) (1.0 / (double) NumberOfTimeStepsPerYear)));
		}

		
		
		
		/**
		 * The most advanced Constructor that uses a Time Discretization
		 *
		 * @param recovery           The Recovery Rate
		 * @param cdsSpreads         The (yearly) CDS spreads in bp
		 * @param timeDiscretization The timeDiscretization for the algorithm. <br>
		 *                           Has to correspond with the discount curve. <br>
		 *                           has to start at t=0.0
		 */
		public SimpleApproximation(double recovery, double[] cdsSpreads,
				TimeDiscretization timeDiscretization) {


			this.recovery = recovery;
			this.cdsSpreads = cdsSpreads;

			this.timeDiscretization = timeDiscretization;
			this.NumberOfTimesteps = timeDiscretization.getNumberOfTimeSteps();
			this.deltaT = (timeDiscretization.getTime(1) - timeDiscretization.getTime(0));

			

		}


		/*
		 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		 * ++
		 */
		/* Getters */
		
		/**
		 * Get the default probability until some time
		 * 
		 * @param year must be smaller or equal to the last time of the time
		 *             Discretization
		 * 
		 * @return
		 */
		public double getCumulatedDefaultProbUntil(int year) {
			return 0.0;
		}
		
		
		/**
		 * Get the Default probability for a specific year. <br>
		 * year 1 ost the time from 0 to 1.
		 * 
		 * @param year
		 * @return
		 */
		public double getOneYearDefaultProb(int year) {
			switch (year) {
			case 0:
				return 0.0;
			case 1:
			return (1.0 - Math.exp(- 1* cdsSpreads[year-1]/10000 /(1.0 - recovery) ));	
			default:
				return (Math.exp(- (year -1)* cdsSpreads[year-2]/10000 /(1.0 - recovery) ) - Math.exp(- (year )* cdsSpreads[year-1]/10000 /(1.0 - recovery) ));
			}
			
			
			
		}

		

		/**
		 * Get the Default probabilities for all available years. <br>
		 * year 1 is the time from 0 to 1.
		 * 
		 * @param year
		 * @return
		 */
		public double[] getOneYearDefaultProbs() {
			double[] probabilities = new double[cdsSpreads.length + 1];

			for (int yearIndex = 0; yearIndex < probabilities.length; yearIndex++) {
				probabilities[yearIndex] = getOneYearDefaultProb(yearIndex);
			}
			return probabilities;
		}
		
}
