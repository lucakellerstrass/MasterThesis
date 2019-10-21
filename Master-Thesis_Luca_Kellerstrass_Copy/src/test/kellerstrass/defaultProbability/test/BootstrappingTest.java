package kellerstrass.defaultProbability.test;

 

 

import java.text.DecimalFormat;

import java.text.DecimalFormatSymbols;

import java.util.Locale;

 

import kellerstrass.defaultProbability.bootstrapping.*;

import kellerstrass.useful.PaymentFrequency;

import net.finmath.time.ScheduleGenerator.Frequency;

import net.finmath.time.TimeDiscretization;

import net.finmath.time.TimeDiscretizationFromArray;

 

 

 

 

public class BootstrappingTest {

               

                private static DecimalFormat formatterValue                    = new DecimalFormat(" ##0.000%;-##0.000%", new DecimalFormatSymbols(Locale.ENGLISH));

                private static DecimalFormat formatterParam                   = new DecimalFormat(" #0.000;-#0.000", new DecimalFormatSymbols(Locale.ENGLISH));

                private static DecimalFormat formatterDeviation            = new DecimalFormat(" 0.00000E00;-0.00000E00", new DecimalFormatSymbols(Locale.ENGLISH));

 

 

                public static void main(String[] args) throws Exception {

                                 // Input parameters

                                 double interestRate = 0.05;     

                                 double Recovery           = 0.40;  //Recovery Rate

                                

                                 double endDate = 15.0;

                                 double deltaT = 0.05;

                                 TimeDiscretization  timeDiscretization = new TimeDiscretizationFromArray(0.0, (int)(endDate/deltaT), deltaT);

 

                                

                                 double[] cdsSpreads = {254, 262.75, 271.5, 280.25, 289, 290.2, 291.4, 292.6, 293.8, 295} ; //deutsche bank 71.2 //{20.0, 25.0, 30.0, 35.0, 40.0}   // The (yearly) CDS spreads in bp {320.0, 57.0, 132.0 , 139.0 , 146.0, 150.0, 154.0}    ||  {300.0, 350.0, 400.0, 450.0, 500.0 }

                                 // BayernLB from 2011: 1y: 264, 5y 289, 10y: 295

                                 //=> {254, 262.75, 271.5, 280.25, 289, 290.2, 291.4, 292.6, 293.8, 295}

                                

                                 //double[] yearlyLIBORs = {0.0360758241758242, 0.0336, 0.0343334648351648, 0.03589, 0.0375295128378378, 0.0390253032258064,0.0405272967032967 };

                                

                                 //ForwardBootstrap initiation

                                 ForwardBootstrap bootStrapper = new ForwardBootstrap(interestRate, Recovery, cdsSpreads, PaymentFrequency.QUATERLY,timeDiscretization );

                                                 

                                 //SimpleApproximation initiation

                                 SimpleApproximation approximator = new SimpleApproximation( Recovery, cdsSpreads);

 

                                 

                                 double[]  marginalDefaultProbsForwardBootstrap = bootStrapper.getInternMarginalDefaultProbabilities();

 

 

                                

                                 System.out.println("TImeInterval  \t  default Prob Bootstrapped  \t  default prob Simple  \t  deviation");

                                 for(int i = 1; i < timeDiscretization.getNumberOfTimes() ; i++) {

                                                

                                                 

                                                 double timeStart = timeDiscretization.getTime(i-1);

                                                 double timeEnd = timeDiscretization.getTime(i);

                                                

                                                 System.out.print("["+ formatterParam.format(timeStart) + " , "+ formatterParam.format(timeEnd) + "]" + "\t");

                                                 System.out.print( marginalDefaultProbsForwardBootstrap[i] + "\t" );

                                                 System.out.print( approximator.getDefaultProbForTimeInterval(timeStart, timeEnd) + "\t" );

                                                 System.out.print(
                                                		 formatterDeviation.format(Math.abs( marginalDefaultProbsForwardBootstrap[i]
                                                				 - approximator.getDefaultProbForTimeInterval(timeStart, timeEnd))));
                                                 
                                                 System.out.println("");

                                 }

                                

               

                              

                                 

 

                                

                                 

                                 

                                 

                 }

                               }