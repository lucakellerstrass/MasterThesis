package kellerstrass.defaultProbability.bootstrapping;

 

 

import net.finmath.time.TimeDiscretization;

import net.finmath.time.TimeDiscretizationFromArray;

import kellerstrass.defaultProbability.Cds.*;

 

/**

* This class implements a very simple method to extract the default probability from cds spreads.

* @author lucak

*

*/

public class SimpleApproximation {

	 

    // CDS information



    private double recovery; // Recovery Rate



    private double[] cdsSpreads; // The (yearly) CDS spreads in bp

   

    private CdsCurve cdsCurve;



    // The timediscretization



    private double deltaT;



    private int numberOfTimesteps;



    private TimeDiscretization timeDiscretization;



    private double[] marginalDefaultProbabilities;



    /*

    *

     * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    *

     * ++

    *

     */



    /* Constructors */



    /**

    *

     * The most basic Constructor <br>

    *

     * the default time Discretization with NumberOfTimeStepsPerYear of 20

    *

    *

     *

     * @param recovery

    *            The Recovery Rate

    *

     * @param cdsSpreads

    *            The (yearly) CDS spreads in bp

    *

     */



    public SimpleApproximation(double recovery, double[] cdsSpreads,    String[] maturityCodes) {

          this(recovery, cdsSpreads, maturityCodes, 20);
    }



    /**

    *

     * A more advanced Constructor <br>

    *

     * Has the possibility to set the NumberOfTimeStepsPerYear

    *

    *

     *

     * @param recovery

    *            The Recovery Rate

    *

     * @param cdsSpreads

    *            The (yearly) CDS spreads in bp

    *

     * @param NumberOfTimeStepsPerYear

    *

     * @param maturity codes, like "6M" 

     *

     */



    public SimpleApproximation(double recovery, double[] cdsSpreads,



                String[] maturityCodes, int NumberOfTimeStepsPerYear) {



          this(recovery, cdsSpreads, maturityCodes,



                      new TimeDiscretizationFromArray(0.0, (int) (cdsSpreads.length / (1.0 / NumberOfTimeStepsPerYear)),



                                 (double) (1.0 / (double) NumberOfTimeStepsPerYear)));



    }



    /**

    *

     * The most advanced Constructor that uses a Time Discretization

    *

    *

     *

     * @param recovery

    *            The Recovery Rate

    *

     * @param cdsSpreads

    *            The (yearly) CDS spreads in bp

    *

     * @param timeDiscretization

    *            The timeDiscretization for the algorithm. <br>

    *

     *            Has to correspond with the discount curve. <br>

    *

     *            has to start at t=0.0

    * @param maturity codes, like "6M"          

     *

     */



    public SimpleApproximation(double recovery, double[] cdsSpreads,



                String[] maturityCodes, TimeDiscretization timeDiscretization) {



          this.recovery = recovery;



          this.cdsSpreads = cdsSpreads;



          this.timeDiscretization = timeDiscretization;



          this.numberOfTimesteps = timeDiscretization.getNumberOfTimeSteps();



          this.deltaT = (timeDiscretization.getTime(1) - timeDiscretization.getTime(0));



          this.marginalDefaultProbabilities = createMarginalDefaultProbabilities(timeDiscretization,cdsSpreads );



          this.cdsCurve = new CdsCurve(maturityCodes, cdsSpreads,

                      ExtrapolationMethod.CUBIC, InterpolationMethod.CUBIC);

    }



    private double[] createMarginalDefaultProbabilities(TimeDiscretization timeDiscretization2, double[] cdsSpreads2) {

          //double[] = new double[]

          return null;

    }



    /*

    *

     * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    *

     * ++

    *

     */



    /* Getters */



    /**

    *

     * Get the default probability until some time

    *

    *

     *

     * @param year

    *            must be smaller or equal to the last time of the time

    *

     *            Discretization

    *

    *

     *

     * @return

    *

     */



    public double getCumulatedDefaultProbUntil(int year) {



          return 0.0;



    }



    /**

    *

     * Get the Default probability for a specific year. <br>

    *

     * year 1 ost the time from 0 to 1.

    *

    *

     *

     * @param year

    *

     * @return

    *

     */



    public double getOneYearDefaultProb(int year) {



          switch (year) {



          case 0:



                return 0.0;



          case 1:



                return (1.0 - Math.exp(-1 * cdsSpreads[year - 1] / 10000 / (1.0 - recovery)));



          default:



                return (Math.exp(-(year - 1) * cdsSpreads[year - 2] / 10000 / (1.0 - recovery))



                            - Math.exp(-(year) * cdsSpreads[year - 1] / 10000 / (1.0 - recovery)));



          }



    }



    /**

    *

     * Get the Default probabilities for all available years. <br>

    *

     * year 1 is the time from 0 to 1.

    *

    *

     *

     * @param year

    *

     * @return

    *

     */



    public double[] getOneYearDefaultProbs() {



          double[] probabilities = new double[cdsSpreads.length + 1];



          for (int yearIndex = 0; yearIndex < probabilities.length; yearIndex++) {



                probabilities[yearIndex] = getOneYearDefaultProb(yearIndex);



          }



          return probabilities;



    }



    public double getDefaultProbForTimeInterval(double timeStart, double timeEnd) {



          double cdsSpreadAtStart = cdsCurve.getValue(timeStart);



          double defProbUntilStart = (1.0 - Math.exp(-timeStart * cdsSpreadAtStart / 10000 / (1.0 - recovery)));



          double cdsSpreadAtEnd =  cdsCurve.getValue(timeEnd);



          double defProbUntilEnd = (1.0 - Math.exp(-timeEnd * cdsSpreadAtEnd / 10000 / (1.0 - recovery)));



          return (defProbUntilEnd - defProbUntilStart);



    }







}