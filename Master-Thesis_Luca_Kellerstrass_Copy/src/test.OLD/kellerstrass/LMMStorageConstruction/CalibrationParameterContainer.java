package kellerstrass.LMMStorageConstruction;

import java.io.Serializable;
import java.util.ArrayList;

import net.finmath.marketdata.model.curves.DiscountCurve;
import net.finmath.marketdata.model.curves.ForwardCurve;
import net.finmath.montecarlo.BrownianMotion;
import net.finmath.montecarlo.interestrate.CalibrationProduct;


/**
 * This class stores Information needed to restore a LMM
 * @author lucak
 *
 */
public class CalibrationParameterContainer implements Serializable {

	
	//The name of the model we whant to restore
	String name;
	
	//The brownian motion used the create the model
	BrownianMotion brownianMotion;

	//The curves used in this model
	ForwardCurve forwardCurve;
	DiscountCurve discountCurve;
	
	//The Calibration Products
	ArrayList<String>					calibrationItemNames	= new ArrayList<>();
	ArrayList<CalibrationProduct>	calibrationProducts		= new ArrayList<>();
	
	/**
	 * Constructor only for basic parameters
	 * @param name
	 * @param brownianMotion
	 * @param forwardCurve
	 * @param discountCurve
	 */
	public CalibrationParameterContainer(String name, BrownianMotion brownianMotion, ForwardCurve forwardCurve,
			DiscountCurve discountCurve) {
		super();
		this.name = name;
		this.brownianMotion = brownianMotion;
		this.forwardCurve = forwardCurve;
		this.discountCurve = discountCurve;
	}

	/**
	 * Constructor storing all Information addid the Calibration Items for test reasons
	 * @param name
	 * @param brownianMotion
	 * @param forwardCurve
	 * @param discountCurve
	 * @param calibrationItemNames
	 * @param calibrationProducts
	 */
	public CalibrationParameterContainer(String name, BrownianMotion brownianMotion, ForwardCurve forwardCurve,
			DiscountCurve discountCurve, ArrayList<String> calibrationItemNames,
			ArrayList<CalibrationProduct> calibrationProducts) {
		super();
		this.name = name;
		this.brownianMotion = brownianMotion;
		this.forwardCurve = forwardCurve;
		this.discountCurve = discountCurve;
		this.calibrationItemNames = calibrationItemNames;
		this.calibrationProducts = calibrationProducts;
	}
	
	



	
	
	
	
	
}
