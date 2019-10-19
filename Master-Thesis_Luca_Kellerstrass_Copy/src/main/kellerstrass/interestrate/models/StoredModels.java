package kellerstrass.interestrate.models;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import kellerstrass.useful.PaymentOffsetCode;
import net.finmath.exception.CalculationException;
import net.finmath.marketdata.model.AnalyticModel;
import net.finmath.marketdata.model.AnalyticModelFromCurvesAndVols;
import net.finmath.marketdata.model.curves.Curve;
import net.finmath.marketdata.model.curves.DiscountCurve;
import net.finmath.marketdata.model.curves.DiscountCurveFromForwardCurve;
import net.finmath.marketdata.model.curves.ForwardCurve;
import net.finmath.marketdata.model.curves.ForwardCurveFromDiscountCurve;
import net.finmath.montecarlo.BrownianMotion;
import net.finmath.montecarlo.interestrate.CalibrationProduct;
import net.finmath.montecarlo.interestrate.LIBORMarketModel;
import net.finmath.montecarlo.interestrate.LIBORModel;
import net.finmath.montecarlo.interestrate.LIBORModelMonteCarloSimulationModel;
import net.finmath.montecarlo.interestrate.LIBORMonteCarloSimulationFromLIBORModel;
import net.finmath.montecarlo.interestrate.models.HullWhiteModel;
import net.finmath.montecarlo.interestrate.models.LIBORMarketModelFromCovarianceModel;
import net.finmath.montecarlo.interestrate.models.covariance.HullWhiteLocalVolatilityModel;
import net.finmath.montecarlo.interestrate.models.covariance.LIBORCorrelationModelExponentialDecay;
import net.finmath.montecarlo.interestrate.models.covariance.LIBORCovarianceModel;
import net.finmath.montecarlo.interestrate.models.covariance.LIBORCovarianceModelFromVolatilityAndCorrelation;
import net.finmath.montecarlo.interestrate.models.covariance.LIBORVolatilityModelFromGivenMatrix;
import net.finmath.montecarlo.interestrate.models.covariance.ShortRateVolatilityModel;
import net.finmath.montecarlo.interestrate.models.covariance.ShortRateVolatilityModelAsGiven;
import net.finmath.montecarlo.process.EulerSchemeFromProcessModel;
import net.finmath.time.TimeDiscretizationFromArray;

/**
 * This class contains different LiborMarketModels and Hull-White Models
 * @author lucak
 *
 */
public class StoredModels {
	
	private double liborPeriodLength;
	private TimeDiscretizationFromArray liborPeriodDiscretization;

	 // Values for a simulation time discretization
	private TimeDiscretizationFromArray simulationDiscretization;

	private LocalDate referenceDate;
	
	private DiscountCurve discountCurve;
	private ForwardCurve forwardCurve;
	private String paymentOffsetCode;
	
	
	
	
	/**
	 * 
	 * @param liborPeriodLength       
	 * @param liborRateTimeHorzion
	 * @param lastTime
	 * @param dt
	 * @param discountCurve
	 * @param forwardCurve
	 * @param referenceDate
	 * @throws Exception 
	 */
	public StoredModels(double liborPeriodLength, double liborRateTimeHorzion, double lastTime, double dt,
			DiscountCurve discountCurve, ForwardCurve forwardCurve, LocalDate referenceDate) throws Exception {	    
	    this( new TimeDiscretizationFromArray(0.0, (int) (liborRateTimeHorzion / liborPeriodLength), liborPeriodLength),
	    	  new TimeDiscretizationFromArray(0.0, (int) (lastTime / dt), dt),
	    	  discountCurve, forwardCurve, referenceDate);
	}



    /**
     * 
     * @param liborPeriodDiscretization    with liborperiodLenght as delta (take 0.5) and liborRateTimeHorzion as time Horizon
     * @param simulationDiscretization
     * @param discountCurve     may be null. If so, constructed from the forward curve
     * @param forwardCurve      If not existing, use String paymentOffsetCode for an other constructor
     * @param referenceDate
     * @throws Exception 
     */
	public StoredModels(TimeDiscretizationFromArray liborPeriodDiscretization,
			TimeDiscretizationFromArray simulationDiscretization, DiscountCurve discountCurve,
			ForwardCurve forwardCurve, LocalDate referenceDate) throws Exception {

        this.referenceDate = referenceDate;
		if (discountCurve == null) {
			this.discountCurve = new DiscountCurveFromForwardCurve(forwardCurve);
		}
		else {
		this.discountCurve = discountCurve;
		}
			
		this.forwardCurve = forwardCurve;
		
		this.simulationDiscretization = simulationDiscretization;
		this.liborPeriodDiscretization = liborPeriodDiscretization;
		this.liborPeriodLength = liborPeriodDiscretization.getTime(1) - liborPeriodDiscretization.getTime(0);
		this.paymentOffsetCode = PaymentOffsetCode.getPaymentOffsetCodeForDouble(liborPeriodLength);
	}
	
	
	
	/**Activate the stored models
	 * 
	 * @param liborPeriodDiscretization
	 * @param simulationDiscretization
	 * @param discountCurve
	 * @param paymentOffsetCode  (1M, 3M, 6M, ??)     if no forward curve is given, the paymentOffsetCode is used to generate it
	 * @param referenceDate
	 * @throws Exception 
	 */
	public StoredModels(TimeDiscretizationFromArray liborPeriodDiscretization,
			TimeDiscretizationFromArray simulationDiscretization, DiscountCurve discountCurve,
			String paymentOffsetCode, LocalDate referenceDate) throws Exception {
		
		this(liborPeriodDiscretization, simulationDiscretization, 
				discountCurve,
				new ForwardCurveFromDiscountCurve(discountCurve.getName(),referenceDate,paymentOffsetCode),
				referenceDate);
		
		
		
	}

	
	
	
	
	
	
	
	
	
	
	
	/**
	 *  Get a Libor Market model, corresponding to a special Hull white model
	 * @param numberOfPaths
	 * @param shortRateVolatility
	 * @param shortRateMeanreversion
	 * @return
	 * @throws CalculationException
	 */
	public LIBORModelMonteCarloSimulationModel getLiborMarketModellCorrespondingToHullWhite(int numberOfPaths, double shortRateVolatility, double shortRateMeanreversion ) throws CalculationException {
		int numberOfFactors	= 1;
		double correlationDecay = 0.0;	// For LMM Model. If 1 factor, parameter has no effect.
		
		//Create corresponding forward curve
		ForwardCurve forwardCurve2 = new ForwardCurveFromDiscountCurve(discountCurve.getName(),referenceDate,paymentOffsetCode);      //6M!!!
		
		//Create curve model
		AnalyticModel curveModel = new AnalyticModelFromCurvesAndVols(new Curve[] { discountCurve, forwardCurve2 });
		
		
		
		/*
		 * Create corresponding LIBOR Market model
		 */
		
			/*
			 * Create a volatility structure v[i][j] = sigma_j(t_i)
			 */
			double[][] volatility = new double[simulationDiscretization.getNumberOfTimeSteps()][liborPeriodDiscretization.getNumberOfTimeSteps()];
			for (int timeIndex = 0; timeIndex < volatility.length; timeIndex++) {
				for (int liborIndex = 0; liborIndex < volatility[timeIndex].length; liborIndex++) {
					// Create a very simple volatility model here
					double time = simulationDiscretization.getTime(timeIndex);
					double time2 = simulationDiscretization.getTime(timeIndex+1);
					double maturity = liborPeriodDiscretization.getTime(liborIndex);
					double maturity2 = liborPeriodDiscretization.getTime(liborIndex+1);

					double timeToMaturity	= maturity - time;
					double deltaTime		= time2-time;
					double deltaMaturity	= maturity2-maturity;

					double meanReversion = shortRateMeanreversion;

					double instVolatility;
					if(timeToMaturity <= 0) {
						instVolatility = 0;				// This forward rate is already fixed, no volatility
					}
					else {
						instVolatility = shortRateVolatility * Math.exp(-meanReversion * timeToMaturity)
								*
								Math.sqrt((Math.exp(2 * meanReversion * deltaTime) - 1)/ (2 * meanReversion * deltaTime))
								*
								(1-Math.exp(-meanReversion * deltaMaturity))/(meanReversion * deltaMaturity);
					}

					// Store
					volatility[timeIndex][liborIndex] = instVolatility;
				}
			}
			LIBORVolatilityModelFromGivenMatrix volatilityModel = new LIBORVolatilityModelFromGivenMatrix(simulationDiscretization, liborPeriodDiscretization, volatility);

			/*
			 * Create a correlation model rho_{i,j} = exp(-a * abs(T_i-T_j))
			 */
			LIBORCorrelationModelExponentialDecay correlationModel = new LIBORCorrelationModelExponentialDecay(
					simulationDiscretization, liborPeriodDiscretization, numberOfFactors,
					correlationDecay);

			/*
			 * Combine volatility model and correlation model to a covariance model
			 */
			LIBORCovarianceModelFromVolatilityAndCorrelation covarianceModel =
					new LIBORCovarianceModelFromVolatilityAndCorrelation(simulationDiscretization,
							liborPeriodDiscretization, volatilityModel, correlationModel);

			// BlendedLocalVolatlityModel
			LIBORCovarianceModel covarianceModel2 = new HullWhiteLocalVolatilityModel(covarianceModel, liborPeriodLength);

			// Set model properties
			Map<String, String> properties = new HashMap<>();

			// Choose the simulation measure
			properties.put("measure", LIBORMarketModelFromCovarianceModel.Measure.SPOT.name());

			// Choose log normal model
			properties.put("stateSpace", LIBORMarketModelFromCovarianceModel.StateSpace.NORMAL.name());

			// Empty array of calibration items - hence, model will use given covariance
			CalibrationProduct[] calibrationItems = new CalibrationProduct[0];

			/*
			 * Create corresponding LIBOR Market Model
			 */
			LIBORMarketModel liborMarketModel = new LIBORMarketModelFromCovarianceModel(
					liborPeriodDiscretization, curveModel, forwardCurve2, discountCurve, covarianceModel2, calibrationItems, properties);

			BrownianMotion brownianMotion = new net.finmath.montecarlo.BrownianMotionLazyInit(simulationDiscretization, numberOfFactors, numberOfPaths, 3141 /* seed */);

			EulerSchemeFromProcessModel process = new EulerSchemeFromProcessModel(brownianMotion, EulerSchemeFromProcessModel.Scheme.EULER);

			return new LIBORMonteCarloSimulationFromLIBORModel(liborMarketModel, process);
		
		
		
		
		 
		
	}
	
	
	/**Get a Libor Hull-White  model, corresponding to a special Libor Market model
	 * 
	 * @param numberOfPaths
	 * @param shortRateVolatility
	 * @param shortRateMeanreversion
	 * @return
	 */
	public LIBORModelMonteCarloSimulationModel getLiborHullWhiteModellCorrespondingToLMM(int numberOfPaths, double shortRateVolatility, double shortRateMeanreversion) {
		//Create corresponding forward curve
				ForwardCurve forwardCurve2 = new ForwardCurveFromDiscountCurve(discountCurve.getName(),referenceDate,"6M");
				
				//Create curve model
				AnalyticModel curveModel = new AnalyticModelFromCurvesAndVols(new Curve[] { discountCurve, forwardCurve2 });
				
				/*
				 * Create corresponding Hull White model
				 */
				
					/*
					 * Create a volatility model: Hull white with constant coefficients (non time dep.).
					 */
					ShortRateVolatilityModel volatilityModel = new ShortRateVolatilityModelAsGiven(
							new TimeDiscretizationFromArray(0.0),
							new double[] { shortRateVolatility } /* volatility */,
							new double[] { shortRateMeanreversion } /* meanReversion */);

					Map<String, Object> properties = new HashMap<>();
					properties.put("isInterpolateDiscountFactorsOnLiborPeriodDiscretization", false);

					// TODO Left hand side type should be TermStructureModel once interface are refactored
					LIBORModel hullWhiteModel = new HullWhiteModel(
							liborPeriodDiscretization, curveModel, forwardCurve2, discountCurve, volatilityModel, properties);

					BrownianMotion brownianMotion2 = new net.finmath.montecarlo.BrownianMotionLazyInit(simulationDiscretization, 2 /* numberOfFactors */, numberOfPaths, 3141 /* seed */);

					EulerSchemeFromProcessModel process2 = new EulerSchemeFromProcessModel(brownianMotion2, EulerSchemeFromProcessModel.Scheme.EULER);

					return new LIBORMonteCarloSimulationFromLIBORModel(hullWhiteModel, process2);
				
				
				
				
				 
			
	}
	
	

}
