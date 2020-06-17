package kellerstrass.swap.test;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.Month;

import org.junit.Assert;
import org.junit.Test;

import kellerstrass.marketInformation.CurveModelData;
import kellerstrass.marketInformation.CurveModelDataType;
import net.finmath.functions.AnalyticFormulas;
import net.finmath.marketdata.model.curves.DiscountCurve;

public class LogToNormalSwaptionValueTest {

	static final DecimalFormat formatterReal2 = new DecimalFormat("#0.00");
	private static boolean isPrintOutVerbose = true;

	public static void main(String[] args) {

		// testBachelierRiskNeutralProbabilities();

		LocalDate today = LocalDate.of(2019, Month.OCTOBER, 23);
		LocalDate swapstartDate = today.plusMonths(1);
		LocalDate FinalDate = swapstartDate.plusYears(1);

		System.out.println("today= " + today + "\t swapstartDate = " + swapstartDate + "\t FinalDate = " + FinalDate);

		CurveModelData curveModelData = new CurveModelData(CurveModelDataType.OIS6M);

		DiscountCurve discountCurve = curveModelData.getDiscountCurve();

		double discountFactorAtStart = discountCurve.getDiscountFactor((30 / 365));
		double discountFactorAtMid = discountCurve.getDiscountFactor((30 + 365) / 2 / 365);
		double discountFactorAtEnd = discountCurve.getDiscountFactor(((30 + 365) / 365));

		double swapRate = (discountFactorAtStart - discountFactorAtEnd)
				/ (0.5 * (discountFactorAtMid + discountFactorAtEnd));

		System.out.println("swapRate= " + swapRate);
		
		double initialValue = 0.0; // Since ATM

		double riskFreeRate = 0;

		final double forward = 0; // HIER SHIFTED FORWARD forward+shift

		final double volatility = 6.8 / 1000;

		final double optionMaturity = 1.0;

		final double optionStrike = 0; // HIER SHIFTED STRIKE strike+shift

		final double payoffUnit = 0;

		double swaptionValue = AnalyticFormulas.blackScholesGeneralizedOptionValue(forward, volatility, optionMaturity,
				optionStrike, payoffUnit);

	}
	
	
	
	
	
	

	/**
	 * From fin-math Lib
	 * 
	 * 
	 * This test shows the Bachelier risk neutral probabilities compared to
	 * Black-Scholes risk neutral probabilities.
	 *
	 * The Bachelier model allows for negative values of the underlying.
	 *
	 * The parameters in this test are such that value of the ATM option is similar
	 * in both models.
	 *
	 */
	public static void testBachelierRiskNeutralProbabilities() {
		DecimalFormat numberFormatStrike = new DecimalFormat(" 0.00% ");
		DecimalFormat numberFormatValue = new DecimalFormat(" 0.000%");
		DecimalFormat numberFormatProbability = new DecimalFormat("  0.00%; -0.00%");

		Double riskFreeRate = 0.01;
		Double volatilityN = 0.0065;
		Double volatilityLN = 0.849;
		Double optionMaturity = 10.0;

		// We calculate risk neutral probs using a finite difference approx. of
		// Breden-Litzenberger
		double eps = 1E-8;

		System.out.println("Strike K" + "          \t" + "Bachelier Value " + "     \t" + "Bachelier P(S<K) " + "    \t"
				+ "Black-Scholes Value " + " \t" + "Black-Scholes P(S<K) " + "\t");
		for (double optionStrike = 0.02; optionStrike > -0.10; optionStrike -= 0.005) {

			double payoffUnit = Math.exp(-riskFreeRate * optionMaturity);
			double forward = 0.01;

			double valuePutBa1 = -(forward - optionStrike) * payoffUnit + AnalyticFormulas.bachelierOptionValue(forward,
					volatilityN, optionMaturity, optionStrike, payoffUnit);
			double valuePutBa2 = -(forward - optionStrike - eps) * payoffUnit + AnalyticFormulas
					.bachelierOptionValue(forward, volatilityN, optionMaturity, optionStrike + eps, payoffUnit);
			double probabilityBachelier = Math.max((valuePutBa2 - valuePutBa1) / eps / payoffUnit, 0);

			double valuePutBS1 = -(forward - optionStrike) * payoffUnit
					+ AnalyticFormulas.blackScholesGeneralizedOptionValue(forward, volatilityLN, optionMaturity,
							optionStrike, payoffUnit);
			double valuePutBS2 = -(forward - optionStrike - eps) * payoffUnit
					+ AnalyticFormulas.blackScholesGeneralizedOptionValue(forward, volatilityLN, optionMaturity,
							optionStrike + eps, payoffUnit);
			double probabilityBlackScholes = Math.max((valuePutBS2 - valuePutBS1) / eps / payoffUnit, 0);

			System.out.println(
					numberFormatStrike.format(optionStrike) + "         \t" + numberFormatValue.format(valuePutBa1)
							+ "         \t" + numberFormatProbability.format(probabilityBachelier) + "         \t"
							+ numberFormatValue.format(valuePutBS1) + "         \t"
							+ numberFormatProbability.format(probabilityBlackScholes));

			if (optionStrike > forward) {
				Assert.assertTrue(
						"For strike>forward: Bacherlier probability for high underlying value should be lower than Black Scholes:",
						probabilityBlackScholes > probabilityBachelier);
			}
			if (optionStrike < -eps) {
				Assert.assertTrue(
						"For strike<0: Bacherlier probability for low underlying value should be higher than Black Scholes:",
						probabilityBlackScholes < probabilityBachelier);
				Assert.assertTrue("For strike<0: Black Scholes probability for underlying < 0 should be 0:",
						probabilityBlackScholes < 1E-8);

			}
		}
	}

}
