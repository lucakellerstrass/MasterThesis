package kellerstrass.useful;

import net.finmath.time.TimeDiscretization;

/**
 * This class implements some static >>ckeck<< methods,  <br>
 * often used in constructors to look if the input is valid.
 * @author lucak
 *
 */
public class Checking {

	 /**
	  * Checks if there are negative, or not valid values in the array
	  * @param array
	  * @return boolean (true if the array has only positive or 0 values)
	  */
	 public static boolean hasOnlyPositiveValues(double[] array) {
	  boolean  hasOnlyPositive = true;
	  
	  //go through all then array values
	  for(int i = 0 ; i < array.length; i++) {
	   if(array[i]< 0) {hasOnlyPositive= false;}
	   
	  }
	  
	  return hasOnlyPositive;
	 }

	
	 /**
	  * Checks if the given discount factor curve makes sense and corresponds to the given TimeDiscretization. <br>
	  * If not it throws errors or changes the array (most likely the addition of the value 1.0 for t=0)
	  * @param discountCurve
	  * @param timeDiscretization2
	  * @return
	 * @throws Exception 
	  */
	  public static double[] checkDiscountCurve(double[] discountCurve, TimeDiscretization timeDiscretization2) throws Exception {
	   //Check if the discount curve has only positive values
	   if(!Checking.hasOnlyPositiveValues(discountCurve)) {
	    throw  new Exception("discount curve has negative values. Not valid input");
	   }
	   
	   
	   // Check if the discount curve corresponds to the time discretization
	   if(discountCurve.length == timeDiscretization2.getNumberOfTimes())/* the correspond*/ {
	    return discountCurve;
	   }
	           	else {
	            	// Check if the start value 1.0 at time 0 coulkd be missing is missing. In this case we add it
	            	if(discountCurve.length == timeDiscretization2.getNumberOfTimeSteps()) {
	               	if(discountCurve[0]!= 1.0) {
	             	double[] newDiscountCurve = new double[timeDiscretization2.getNumberOfTimes()];
	             	 newDiscountCurve[0]= 1.0;
	             	for(int i = 1; i < newDiscountCurve.length ; i++ ) {
	          	newDiscountCurve[i]= discountCurve[i-1];
	             	}
	             	return newDiscountCurve;
	               	}
	               	else {
	              	throw new Exception("discount curve too short");
	               	}
	             	}
	            	else{
	           	  throw new Exception("discount has not the correct size");
	            	}
	   }
	    
	    
	  }
	
	
	
	
	
}
