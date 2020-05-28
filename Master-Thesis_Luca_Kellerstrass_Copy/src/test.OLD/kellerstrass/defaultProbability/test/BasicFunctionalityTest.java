package kellerstrass.defaultProbability.test;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import kellerstrass.defaultProbability.bootstrapping.ForwardBootstrap;
import kellerstrass.defaultProbability.bootstrapping.SimpleApproximation;
import kellerstrass.usefulOLD.PaymentFrequencyOLD;

public class BasicFunctionalityTest {

    

	private static DecimalFormat formatterDeviation	= new DecimalFormat(" 0.00000E00;-0.00000E00", new DecimalFormatSymbols(Locale.ENGLISH));





    public static void main(String[] args) throws Exception {

                     // Input parameters

                     double interestRate = 0.05;     

                     double Recovery           = 0.40;  //Recovery Rate



                     double[] cdsSpreads = {300.0, 350.0, 400.0, 450.0, 500.0 } ; //{20.0, 25.0, 30.0, 35.0, 40.0}   // The (yearly) CDS spreads in bp {320.0, 57.0, 132.0 , 139.0 , 146.0, 150.0, 154.0}    ||  {300.0, 350.0, 400.0, 450.0, 500.0 }
                     String[] maturitiCodes =   {"1y", "2y","3y","4y","5y" };
                    

                     //double[] yearlyLIBORs = {0.0360758241758242, 0.0336, 0.0343334648351648, 0.03589, 0.0375295128378378, 0.0390253032258064,0.0405272967032967 };

                    

                     //ForwardBootstrap initiation

                     double calculatorTimeStart =System.currentTimeMillis();

                     ForwardBootstrap bootStrapper = new ForwardBootstrap(interestRate, Recovery, cdsSpreads, PaymentFrequencyOLD.QUATERLY);

                     double calculatorTimeEnd =System.currentTimeMillis();

                     double ForwardBootstrapInitialization = (calculatorTimeEnd - calculatorTimeStart);

                     System.out.println("ForwardBootstrap Initialization took  " +ForwardBootstrapInitialization  + " millis");

                    

                     

                     //SimpleApproximation initiation

                     double SimpleApproximationTimeStart =System.currentTimeMillis();

                     SimpleApproximation approximator = new SimpleApproximation( Recovery, cdsSpreads,maturitiCodes ) ;

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

                    

                     

                    

                     

                     System.out.println(" Index" + "\t" + "Time" + "\t" + "defaultProbsForwardBootstrap" + "\t" + "defaultProbsSimpleApproximation" +"\t" + "difference" );

                     for(int i = 0 ; i < defaultProbsForwardBootstrap.length ; i++) {

                     

                      System.out.println( i + "\t" +  i + "\t" + defaultProbsForwardBootstrap[i] + "\t       " + defaultProbsSimpleApproximation[i] + "\t"

                      + formatterDeviation.format(Math.abs(defaultProbsForwardBootstrap[i] - defaultProbsSimpleApproximation[i] )));

                     

                     }

                     System.out.println( "Calculation time:"+ "\t" + "\t" +

                                                   ( ForwardBootstrapInitialization +defaultProbabilitiesFromForwardBootstrap) +  "\t        " +

                                                   (SimpleApproximationInitialization + defaultProbabilitiesFromSimpleApproximation) + "\t           " + "millis" );

                    



                     

                     

                     

                     

                     

                    }

   

     



                   }

