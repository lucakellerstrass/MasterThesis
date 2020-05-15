package kellerstrass.defaultProbability.Cds;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;

import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

 

/**

* This class gives the possibility to create a CDS curve with inter - and

* extrapolation methode

*

 * @author y30650

*

*/

public class CdsCurve {

 

      private double[] times;

      private double[] values;

 

      private ExtrapolationMethod extrapolationMethod;

      private InterpolationMethod interpolationMethod;

     

      private MathOperation[] mathOperations;   //functions interpolating between the time steps

                                                // The last MathOperation is the extrapolation mothod

     

      boolean hasInterExtraPolated;

     

      private double SafetyStepForCUBICInterpol =2000.0;

 

      /**

      * Create a CDS curve using:

      *

       * @param times

      * @param values

      * @param extrapolationMethod

      *            If null, default is used

      * @param interpolationMethod

      *            If null, default is used

      */

      public CdsCurve(double[] times, double[] values, ExtrapolationMethod extrapolationMethod,

                  InterpolationMethod interpolationMethod) {

 

            this.times = times;  

            this.values = values;

            if (extrapolationMethod == null) {

                  this.extrapolationMethod = ExtrapolationMethod.CUBIC;

            } else {

                  this.extrapolationMethod = extrapolationMethod;

            }

            if (interpolationMethod == null) {

                  this.interpolationMethod = InterpolationMethod.CUBIC;

            } else {

                  this.interpolationMethod = interpolationMethod;

            }

            this.hasInterExtraPolated = false;

            this.mathOperations = new MathOperation[times.length +1];

           

 

      }

 

      /**

      * Create a CDS curve using:

      *

       * @param maturitiesCodes

      *            String codes, like "6M"

      * @param values

      * @param extrapolationMethod

      * @param interpolationMethod

      */

      public CdsCurve(String[] maturitiesCodes, double[] values, ExtrapolationMethod extrapolationMethod,

                  InterpolationMethod interpolationMethod) {

 

            this(getTimesFromMaturitiesCodes(maturitiesCodes), values, extrapolationMethod, interpolationMethod);

 

      }

 

      /**

      * Create a CDS curve. Uses default extra- and interpolation methods

      *

       * @param times

      * @param values

      */

      public CdsCurve(double[] times, double[] values) {

            this(times, values, null, null);

      }

 

      /**

      * Create a CDS curve from maturity Codes. Uses default extra- and interpolation

      * mothods

      *

       * @param maturitiesCodes

      *            String codes, like "6M"

      * @param values

      */

      public CdsCurve(String[] maturitiesCodes, double[] values) {

            this(maturitiesCodes, values, null, null);

      }

 

     

     

     

   private void reNewInterExtraPolation(){

        

         

         if (hasInterExtraPolated == false)

         {

           doExtraPolation();

             doInterPolation();

             hasInterExtraPolated = true;}

            

             

   }

     

     

 

      private void doExtraPolation() {

 

       switch(extrapolationMethod) {

      

       case CONSTANT:

         mathOperations[mathOperations.length-1] =  (double time) -> values[values.length-1];

         break;

       case FORECAST:

         double leftValue = values[Math.max(0, values.length -2)];

         double leftTime = times[Math.max(0, values.length -2)];

         double rightValue = values[values.length -1];

         double rightTime = times[times.length-1];

         double slope =  (rightValue - leftValue)/(rightTime - leftTime);

         

         mathOperations[mathOperations.length-1] =  (double time) ->

         rightValue + slope* (time - rightTime);

         

         break;

         

       case MOVINGAVERAGE:

         

         break;

       case CUBIC:

         double[] newTimes = addSafetyStepAtEnd(times);

                  double[] newValues = addValueAtSafetyStep(values);

                 

                  SplineInterpolator spline = new SplineInterpolator();                 

                  PolynomialSplineFunction splineFunction = spline.interpolate(newTimes, newValues);

                  mathOperations[mathOperations.length-1] =  (double time) -> splineFunction.value(time);

         

        break;

       default:

         break;

       }

           

}

 

 

 

 

 

      private void doInterPolation() {

           

            // Go throw all the Intervalls [T_i, T_{i+1}]

            for(int interval = 0; interval < values.length; interval++) {

                  switch(interpolationMethod) {

                 

                  case LINEAR:

                        if(interval == 0

                        ) {

                              double leftValue = values[0];

                           double leftTime = times[0];

                           double rightValue = values[1];

                           double rightTime = times[1];

                           double slope =  (rightValue - leftValue)/(rightTime - leftTime);

                           mathOperations[interval] =  (double time) ->

                           leftValue - slope* (leftTime -time) ;

                        }else

                        {

                              double leftValue = values[interval-1];

                           double leftTime = times[interval -1];

                           double rightValue = values[interval];

                           double rightTime = times[interval ];

                           double slope =  (rightValue - leftValue)/(rightTime - leftTime);

                           mathOperations[interval] =  (double time) ->

                           leftValue + slope* (time - leftTime) ;     

                        }

                       

                        break;

                 

                  case CUBIC:

                       

                        double[] newTimes = addZeroAtBeginning(times);

                        double[] newValues = addValueAtZero(values);

                       

                        SplineInterpolator spline = new SplineInterpolator();                 

                        PolynomialSplineFunction splineFunction = spline.interpolate(newTimes, newValues);

                        mathOperations[interval] =  (double time) -> splineFunction.value(time);

                  }

            }

           

           

           

                 

     

}

     

     

     

     

     

 

     

     

     

   private double[] addValueAtZero(double[] values2) {

         double[] array = new double[values2.length+1];

         array[0] =  values[0] - (values[1] - values[0])/(times[1] - times[0]);

            for(int i = 1; i < array.length; i++) {

                  array[i] = values2[i-1];

            }

            return array;

      }

 

      private double[] addValueAtSafetyStep(double[] values2) {

            double[] array = new double[values2.length+1];

            //constant extrapol

            array[array.length-1]= values2[values2.length-1];

           

            //Forcast extrapol

            /*double leftValue = values[Math.max(0, values.length -2)];

        double leftTime = times[Math.max(0, values.length -2)];

        double rightValue = values[values.length -1];

        double rightTime = times[times.length-1];

        double slope =  (rightValue - leftValue)/(rightTime - leftTime);

        

        array[array.length-1]= rightValue + slope* (SafetyStepForCUBICInterpol - rightTime);

        */

           

           

            for(int i = 0; i < array.length -1; i++) {

                  array[i] = values2[i];

            }

            return array;

      }

  

   

private double[] addZeroAtBeginning(double[] times2) {

            double[] array = new double[times2.length+1];

            array[0]= 0.0;

            for(int i = 1; i < array.length; i++) {

                  array[i] = times2[i-1];

            }

            return array;

      }

  

private double[] addSafetyStepAtEnd(double[] times2) {

      double[] array = new double[times2.length+1];

      array[array.length-1]= SafetyStepForCUBICInterpol;

     

      for(int i = 0; i < array.length -1; i++) {

            array[i] = times2[i];

      }

     

     

      return array;

}

 

  

   

 

public double getValue(double time ) {

         reNewInterExtraPolation();

        

         //System.out.println("Extrapolation with: "+  mathOperations[mathOperations.length-1].operation(time));

         //

         if(time >= times[times.length -1]) {

              

               return mathOperations[mathOperations.length-1].operation(time);

         }else {

              

               if(time < times[0]) {

                    

                     return mathOperations[0].operation(time);

               }else {

 

        

         for( int i = 1 ; i < times.length ; i++) {

              

               if((times[i-1] <= time)&&(time < times[i])) {

                      int j=i;

                     i= times.length;

                     //System.out.println("wir geben mathop Nr " + j + " aus");

                     return mathOperations[j].operation(time);  

                    

               }

         }

        

         return 0.0;

         }

 

        

   }

     

   }

     

     

     

     

     

     

     

     

     

     

     

     

     

 

 

      /**

      * Returns the maturity vector in double for a given String vector<br>

      * e.g. "6M" = 0.5 .....

      *

       * @param maturitiesCodes

      * @return

      */

      private static double[] getTimesFromMaturitiesCodes(String[] maturitiesCodes) {

            double[] times = new double[maturitiesCodes.length];

            for (int i = 0; i < times.length; i++) {

                  times[i] = getTimeFromMaturitiesCode(maturitiesCodes[i]);

            }

            return times;

      }

 

      /**

      * Returns the maturity in double for a given String <br>

      * e.g. "6M" = 0.5

      *

       * @param string

      * @return

      */

      private static double getTimeFromMaturitiesCode(String string) {

       

     

                 

           

            int Number = Integer.parseInt(string.substring(0, string.length()-1));

            String Letter = string.substring(string.length()-1, string.length());

            double multiplicatorFromLetter;

 

            switch (Letter) {

            case "D":

            case "d":

                  multiplicatorFromLetter = (1.0 / 365.0);

                  break;

            case "M":

            case "m":

                  multiplicatorFromLetter = (1.0 / 12.0);

                  break;

            case "Y":

            case "y":

                  multiplicatorFromLetter = 1.0;

                  break;

            default:

                  multiplicatorFromLetter = 0.0;

            }

 

            return multiplicatorFromLetter * Number;

      }

 

}

 