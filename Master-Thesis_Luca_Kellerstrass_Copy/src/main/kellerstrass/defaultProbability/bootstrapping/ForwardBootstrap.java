package kellerstrass.defaultProbability.bootstrapping;

import net.finmath.time.TimeDiscretization;
import net.finmath.time.TimeDiscretizationFromArray;
import kellerstrass.useful.*;

public class ForwardBootstrap extends AbstractCdsImpliesPDsTODO {
	// CDS information
	private double recovery; // Recovery Rate
	private double[] cdsSpreads; // The (yearly) CDS spreads in bp
	private int paymentsPerYear;

	// The timediscretization
	private double deltaT;
	private int NumberOfTimesteps;
	private TimeDiscretization timeDiscretization;

	// arrays for bootstrapping
	// Over whole time discretization
	private double[] discountFactors;
	private double[] CumulativeSurvivalProbabilities;
	private double[] MarginalDefaultProbability;
	private double[] defaults;

	// yearly values
	private double[] hazardRates;
	private double[] MTM;
	private double[] UpFront;
	private double[] riskAnnuity;
	private double[] defaultLegs;

	// Length terminated via Payment frequency
	private double Premiums[];

	// Boolean to know If the bootstrapping was already don
	private boolean hasBootstrapped;

	/*
	 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	 * ++
	 */
	/* Constructors */

	/**
	 * The most basic Constructor <br>
	 * uses a simple interest rate (e.g. 5%) for the Discount curve and <br>
	 * the default time Discretization with NumberOfTimeStepsPerYear of 20
	 *
	 * @param interestRate used for the discount factor
	 * @param recovery     The Recovery Rate
	 * @param cdsSpreads   The (yearly) CDS spreads in bp
	 * @param frequency
	 * @throws Exception 
	 */
	public ForwardBootstrap(double interestRate, double recovery, double[] cdsSpreads, PaymentFrequency frequency) throws Exception {
		this(interestRate, recovery, cdsSpreads, frequency, 20);
	}

	/**
	 * A more advanced Constructor <br>
	 * uses a simple interest rate (e.g. 5%) for the Discount curve and <br>
	 * the possibility to set the NumberOfTimeStepsPerYear
	 *
	 * @param interestRate used for the discount factor
	 * @param recovery     The Recovery Rate
	 * @param cdsSpreads   The (yearly) CDS spreads in bp
	 * @param frequency
	 * @param NumberOfTimeStepsPerYear
	 * @throws Exception 
	 */
	public ForwardBootstrap(double interestRate, double recovery, double[] cdsSpreads, PaymentFrequency frequency,
			int NumberOfTimeStepsPerYear) throws Exception {
		// System.out.println("initialisierung");
		this(null, recovery, cdsSpreads, frequency,
				new TimeDiscretizationFromArray(0.0, (int) (cdsSpreads.length / (1.0 / NumberOfTimeStepsPerYear)),
						(double) (1.0 / (double) NumberOfTimeStepsPerYear)));
		this.discountFactors = getDiscountfactors(interestRate, timeDiscretization);
	}

	/**
	 * The most advanced Constructor <br>
	 * uses a given Discount curve and corresponding <br>
	 * Time Discretization
	 *
	 * @param discountCurve      The discount curve to be used in the Algorithm.
	 *                           <br>
	 *                           It has to correspond with the given
	 *                           timeDiscretization
	 * @param recovery           The Recovery Rate
	 * @param cdsSpreads         The (yearly) CDS spreads in bp
	 * @param frequency          The frequency, the premium of the CDS is payed @see
	 *                           PaymentFrequency
	 * @param timeDiscretization The timeDiscretization for the algorithm. <br>
	 *                           Has to correspond with the discount curve. <br>
	 *                           has to start at t=0.0
	 * @throws Exception 
	 */
	public ForwardBootstrap(double[] discountCurve, double recovery, double[] cdsSpreads, PaymentFrequency frequency,
			TimeDiscretization timeDiscretization) throws Exception {
		// System.out.println("initialisierung");

		if (discountCurve != null) {
			this.discountFactors = Checking.checkDiscountCurve(discountCurve, timeDiscretization);
		}
		this.recovery = recovery;
		if(Checking.hasOnlyPositiveValues(cdsSpreads)) {
			this.cdsSpreads = cdsSpreads;}
		else
		{
			throw new Exception("cdsSpreads not valid");
		}

		this.timeDiscretization = timeDiscretization;
		this.NumberOfTimesteps = timeDiscretization.getNumberOfTimeSteps();
		this.deltaT = (timeDiscretization.getTime(1) - timeDiscretization.getTime(0));

		// System.out.println("NumberOfTimesteps= "+ NumberOfTimesteps);
		this.CumulativeSurvivalProbabilities = new double[NumberOfTimesteps + 1];
		CumulativeSurvivalProbabilities[0] = 1.0;
		this.MarginalDefaultProbability = new double[NumberOfTimesteps + 1];
		MarginalDefaultProbability[0] = Double.NaN;
		this.defaults = new double[NumberOfTimesteps + 1];
		defaults[0] = Double.NaN;

		this.paymentsPerYear = frequency.PaymentsPerYear();
		this.Premiums = new double[cdsSpreads.length * paymentsPerYear];

		this.hazardRates = new double[cdsSpreads.length];
		this.MTM = new double[cdsSpreads.length];
		this.UpFront = new double[cdsSpreads.length];
		UpFrontInit();
		this.riskAnnuity = new double[cdsSpreads.length];
		this.defaultLegs = new double[cdsSpreads.length];

		this.hasBootstrapped = false;

	}

	/*
	 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	 * ++
	 */
	/* The bootstrapping methods */
	/**
	 * Starts the BootstrapAlgorithm if not done yet
	 */
	private void reNewBootStrap() { // System.out.println("reNewBootStrap()");
		if (hasBootstrapped == false)
			doBootstrapping();
	}

	/**
	 * The forward bootstrapping algorithm
	 */
	private void doBootstrapping() { // System.out.println("doBootstrapping()");

		// initialize the MTM array with

		double x1 = 0.01;
		double x2 = 0.02;
		double xl = 0.0;

		double fl, f, rtsec, Temp, dx;

		for (int yearIndex = 0; yearIndex < hazardRates.length; yearIndex++) {
			hazardRates[yearIndex] = x1;

			renewCalculations(yearIndex);

			fl = MTM[yearIndex]; // System.out.println("fl calculated");
			hazardRates[yearIndex] = x2;

			renewCalculations(yearIndex);

			f = MTM[yearIndex]; // System.out.println("fl calculated");

			if (Math.abs(fl) < Math.abs(f)) {
				rtsec = x1;
				xl = x2;
				Temp = f;
				f = fl;
				fl = Temp;
				// System.out.println("wir sind im ersten if case " +"f= "+ f +" \t fl= " + fl);
			} else {
				xl = x1;
				rtsec = x2;
				// System.out.println("wir sind im ersten else " +"f= "+ f +" \t fl= " + fl);
			}

			for (int approximationIndex = 0; approximationIndex < 50; approximationIndex++) {
				// System.out.println(" Approximationsdurchlauf "+ approximationIndex);
				// System.out.println("xl= " +x1 + " rtsec= " + rtsec+ " f= " +f + " fl= "+ fl
				// );
				dx = (xl - rtsec) * f / (f - fl);
				// System.out.println(" dx= "+ dx);
				xl = rtsec;
				fl = f;
				rtsec += dx;
				hazardRates[yearIndex] = rtsec; // System.out.println(" hazardRates[yearIndex]= "+
												// hazardRates[yearIndex]);

				renewCalculations(yearIndex);

				f = MTM[yearIndex];

				if ((Math.abs(dx) < 0.0000000001) || (f == 0.0)) {
					approximationIndex = 50;
				}
			}
		}
		hasBootstrapped = true;
	}

	/**
	 * adjust CumulativeSurvivalProbabilities <br>
	 * and MarginalDefaultProbability <br>
	 * and Defaults <br>
	 * and Premiums <br>
	 * and default Leg <br>
	 * and Risky Annuity <br>
	 * and Markt-to-Market MTM
	 * 
	 * @param yearIndex
	 */
	private void renewCalculations(int yearIndex) {

		// System.out.println("Durchlauf der adjust methode");

		int startIndex = timeDiscretization.getTimeIndex(yearIndex) + 1;
		// System.out.println("startIndex= "+ startIndex);
		int endIndex = timeDiscretization.getTimeIndex(yearIndex + 1) + 1;
		// System.out.println("endIndex= "+ endIndex);

		// default Leg
		double defaultLeg = 0.0;

		// Loop Printout
		// System.out.println ("TimeIndex: " + "\t" + " Time: "+ "\t" +
		// "CumulativeSurvivalProbabilities" + "\t" +
		// "MarginalDefaultProbability" + "\t" + "Defaults");
		for (int timeIndex = startIndex; timeIndex < endIndex; timeIndex++) {

			// Loop Printout
			// System.out.print ( timeIndex + "\t" + timeDiscretization.getTime(timeIndex) +
			// "\t");

			// CumulativeSurvivalProbabilities
			CumulativeSurvivalProbabilities[timeIndex] = CumulativeSurvivalProbabilities[timeIndex - 1]
					* Math.exp(-deltaT * hazardRates[yearIndex]);

			// System.out.print(CumulativeSurvivalProbabilities[timeIndex]+ "\t" );
			if (CumulativeSurvivalProbabilities[timeIndex] >= Double.POSITIVE_INFINITY) {
				// System.out.println("hazardRates= " + hazardRates[yearIndex]);
				throw new Error("STOP");
			}

			// MarginalDefaultProbability
			MarginalDefaultProbability[timeIndex] = CumulativeSurvivalProbabilities[timeIndex - 1]
					- CumulativeSurvivalProbabilities[timeIndex];

			// System.out.print(MarginalDefaultProbability[timeIndex]+ "\t" );

			// Defaults
			defaults[timeIndex] = discountFactors[timeIndex] * MarginalDefaultProbability[timeIndex] * (1.0 - recovery);

			// System.out.println(defaults[timeIndex]+ "\t" );

			// Default Leg Sum:
			defaultLeg += defaults[timeIndex];

		}

		// store the default leg
		double previousDefaultLeg = 0.0;
		if (yearIndex > 0) {
			previousDefaultLeg = defaultLegs[yearIndex - 1];
		}
		defaultLegs[yearIndex] = defaultLeg + previousDefaultLeg;

		// System.out.println("defaultLegs["+ yearIndex +"]= " + "\t"+
		// defaultLegs[yearIndex]);

		// Risky annuity
		double riskyAnnuity = 0.0;

		// Premiums
		// System.out.println ("premiumTimeIndex: " + "\t" + " Time: "+ "\t" +
		// "Premiums" + "\t" );
		for (int premiumTimeIndex = yearIndex * paymentsPerYear; premiumTimeIndex < (yearIndex + 1)
				* paymentsPerYear; premiumTimeIndex++) {
			int CorrespondingDiscrIndex = timeDiscretization.getTimeIndexNearestLessOrEqual(
					(1.0 / paymentsPerYear) + premiumTimeIndex * (1.0 / paymentsPerYear));
			Premiums[premiumTimeIndex] = discountFactors[CorrespondingDiscrIndex]
					* CumulativeSurvivalProbabilities[CorrespondingDiscrIndex] * (1.0 / paymentsPerYear);

			// System.out.println ( premiumTimeIndex + "\t" +
			// timeDiscretization.getTime(CorrespondingDiscrIndex) + "\t"
			// +Premiums[premiumTimeIndex] );

			// Risky Annuity Sum
			riskyAnnuity += Premiums[premiumTimeIndex];

		}

		// Store the risky annuity
		double previousRiskAnnuity = 0.0;
		if (yearIndex > 0) {
			previousRiskAnnuity = riskAnnuity[yearIndex - 1];
		}
		riskAnnuity[yearIndex] = riskyAnnuity + previousRiskAnnuity;

		// System.out.println("riskAnnuity["+ yearIndex +"]= " + "\t"+
		// riskAnnuity[yearIndex]);

		// Calculate the MTM values
		MTM[yearIndex] = defaultLegs[yearIndex] - riskAnnuity[yearIndex] * cdsSpreads[yearIndex] / 10000
				- UpFront[yearIndex];
		// System.out.println("MTM["+ yearIndex +"]= " + "\t"+ MTM[yearIndex]);

	}

	/*
	 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	 * ++
	 */
	/* Getters */

	public double[] getHazardRates() {
		reNewBootStrap();
		return hazardRates;
	}

	/**
	 * Get the default probability until some time
	 * 
	 * @param year must be smaller or equal to the last time of the time
	 *             Discretization
	 * 
	 * @return
	 */
	public double getCumulatedDefaultProbUntil(int year) {
		reNewBootStrap();
		return (1.0 - CumulativeSurvivalProbabilities[(int) (year / deltaT)]);
	}

	/**
	 * Get the default probabilities for all available years <br>
	 * Length: Number of years +1
	 * 
	 * @return
	 */
	public double[] getCumulatedYearlyDefaultProbabilities() {
		reNewBootStrap();
		double[] probabilities = new double[cdsSpreads.length + 1];

		for (int yearIndex = 0; yearIndex < probabilities.length; yearIndex++) {
			probabilities[yearIndex] = getCumulatedDefaultProbUntil(yearIndex);
		}
		return probabilities;
	}

	/**
	 * Get the default probabilities for all available time points from the time
	 * discretization <br>
	 * Length: Number of years +1
	 * 
	 * @param year must be smaller or equal to the last time of the time
	 *             Discretization
	 * @return
	 */
	public double[] getCumulatedDefaultProbabilities() {
		reNewBootStrap();
		double[] probabilities = new double[CumulativeSurvivalProbabilities.length];

		for (int timeIndex = 0; timeIndex < probabilities.length; timeIndex++) {
			probabilities[timeIndex] = (1.0 - CumulativeSurvivalProbabilities[timeIndex]);
		}
		return probabilities;
	}

	/**
	 * Get the Default probability for a specific year. <br>
	 * year 1 ost the time from 0 to 1.
	 * 
	 * @param year
	 * @return
	 */
	public double getOneYearDefaultProb(int year) {
		reNewBootStrap();
		return (getCumulatedDefaultProbUntil(year) - getCumulatedDefaultProbUntil(Math.max(0, year - 1)));
	}

	/**
	 * Get the Default probabilities for all available years. <br>
	 * year 1 is the time from 0 to 1.
	 * 
	 * @param year
	 * @return
	 */
	public double[] getOneYearDefaultProbs() {
		reNewBootStrap();
		double[] probabilities = new double[cdsSpreads.length + 1];

		for (int yearIndex = 0; yearIndex < probabilities.length; yearIndex++) {
			probabilities[yearIndex] = getOneYearDefaultProb(yearIndex);
		}
		return probabilities;
	}

	/**
	 * Get the Default probability for an time Interval, given in time Indecies <br>
	 * returns: survival until Tim2(StartIndex) - survival until Tim2(EndIndex)
	 * 
	 * @param StartIndex
	 * @param EndIndex
	 * @return
	 */
	public double getDefaultProbForTimeIndexInterval(int StartIndex, int EndIndex) {
		reNewBootStrap();
		return CumulativeSurvivalProbabilities[StartIndex] - CumulativeSurvivalProbabilities[EndIndex];
	}

	/**
	 * Get the Default probability for a given time Index <br>
	 * returns: survival until (i-1) - survival until i <br>
	 * this is the probability of default in [T_{i-1}, T_i]
	 * 
	 * @param StartIndex
	 * @param EndIndex
	 * @return
	 */
	public double getDefaultProbForTimeIndex(int Index) {
		reNewBootStrap();
		return MarginalDefaultProbability[Index];
	}
	
	public void printMarginalDefaultprobs() {
		reNewBootStrap();
		System.out.println("TimeIndex   \t  time  \t  marginal  ");
		for(int i = 0; i < timeDiscretization.getNumberOfTimes(); i++) {
			System.out.println(i + " \t " + timeDiscretization.getTime(i) + " \t " + MarginalDefaultProbability[i]);
		}
	}
	
	public void printDiscountfactors() {
		reNewBootStrap();
		System.out.println("TimeIndex   \t  time  \t  fiscount Factor  ");
		for(int i = 0; i < timeDiscretization.getNumberOfTimes(); i++) {
			System.out.println(i + " \t " + timeDiscretization.getTime(i) + " \t " + getDiscountFactors()[i]);
		}
	}
	

	public double getRecovery() {
		return recovery;
	}

	public double[] getCdsSpreads() {
		return cdsSpreads;
	}

	public int getPaymentsPerYear() {
		return paymentsPerYear;
	}

	public TimeDiscretization getTimeDiscretization() {
		return timeDiscretization;
	}

	public double[] getDiscountFactors() {
		return discountFactors;
	}

	/*
	 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	 * ++
	 */
	/* Setters */
	public void setRecovery(double recovery) {
		this.recovery = recovery;
		this.hasBootstrapped = false;
	}

	public void setCdsSpreads(double[] cdsSpreads) {
		this.cdsSpreads = cdsSpreads;
		this.hasBootstrapped = false;
	}

	public void setPaymentsPerYear(int paymentsPerYear) {
		this.paymentsPerYear = paymentsPerYear;
		this.hasBootstrapped = false;
	}

	public void setDiscountFactors(double[] discountFactors) {
		this.discountFactors = discountFactors;
		this.hasBootstrapped = false;
	}

	/**
	 * Input yearly LIBORS
	 * 
	 * @param yearlyLIBORS
	 */
	public void setDiscountFactorsFromYearlyLIBORs(double[] yearlyLIBORS) {

		// throw Error if LIBORS dont match to the CDS spreads
		if (yearlyLIBORS.length != cdsSpreads.length) {
			throw new Error("yearly LIBORs dont have the right length");
		}

		double[] InternDiscountFactors = new double[timeDiscretization.getNumberOfTimeSteps() + 1];
		double[] partialDiscountCurve = new double[(int) (1.0 / deltaT)];
		// go through the years
		for (int yearIndex = 0; yearIndex < yearlyLIBORS.length; yearIndex++) {

			// Part timediscretization for one year:
			TimeDiscretization td = new TimeDiscretizationFromArray((double) yearIndex, (int) (1.0 / deltaT), deltaT);
			partialDiscountCurve = getDiscountfactors(yearlyLIBORS[yearIndex], td);

			// Fill the true arry
			int i = 0;
			for (int timeIndex = (int) (yearIndex / deltaT); timeIndex < (int) ((yearIndex + 1)
					/ deltaT); timeIndex++) {
				InternDiscountFactors[timeIndex] = partialDiscountCurve[i];
				i++;
			}
		}
		this.discountFactors = InternDiscountFactors;
	}

	/*
	 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	 * ++
	 */
	/* Constructor methods */

	/*
	 * Some methodes can be found in Useful.Chiecking
	 */

	/**
	 * Calculate the discount curve in the case we only have a given an interest
	 * Rate
	 * 
	 * @param interestRate
	 * @param timeDiscretization2
	 * @return
	 */
	private double[] getDiscountfactors(double interestRate, TimeDiscretization timeDiscretization2) {
		// System.out.println("calculateDiscountfactors()");
		double[] InternDiscountFactors = new double[timeDiscretization2.getNumberOfTimeSteps() + 1];
		for (int timeIndex = 0; timeIndex < timeDiscretization2.getNumberOfTimeSteps() + 1; timeIndex++) {
			InternDiscountFactors[timeIndex] = Math.exp(-timeDiscretization2.getTime(timeIndex) * interestRate);
		}
		return InternDiscountFactors;

	}

	/**
	 * Initiates the Up Front array with zeros
	 */
	private void UpFrontInit() {
		// System.out.println("UpFrontInit()");
		for (int i = 0; i < UpFront.length; i++) {
			UpFront[0] = 0.0;
		}

	}

}
