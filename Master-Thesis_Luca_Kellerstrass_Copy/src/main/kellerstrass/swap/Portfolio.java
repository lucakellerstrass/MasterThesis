package kellerstrass.swap;

import net.finmath.exception.CalculationException;
import net.finmath.montecarlo.RandomVariableFromDoubleArray;
import net.finmath.montecarlo.interestrate.LIBORModelMonteCarloSimulationModel;
import net.finmath.montecarlo.interestrate.products.AbstractLIBORMonteCarloProduct;
import net.finmath.stochastic.RandomVariable;

/**
 * Create a portfolio consisting of underlyings.
 *
 * The getValue method of this class returns
 * 
 * the sum over all different getValue methods. <br>
 * COmpare this to <br>
 * net.finmath.montecarlo.interestrate.products.Portfolio
 *
 * @author Luca Kellerstrass
 */

public class Portfolio extends AbstractLIBORMonteCarloProduct {

	AbstractLIBORMonteCarloProduct[] underlyings;
	String name;

	public Portfolio(AbstractLIBORMonteCarloProduct[] underlyings) {
		this.underlyings = underlyings;
		this.name = ConstructName();

	}

	private String ConstructName() {

		name = "[ " + underlyings[0].toString() + "; \n";
		for (int i = 1; i < underlyings.length - 1; i++) {
			name = name + underlyings[i].toString() + " ; \n ";
		}
		name = name + underlyings[underlyings.length - 1] + " ]";
		return name;
	}

	@Override
	public RandomVariable getValue(double evaluationTime, LIBORModelMonteCarloSimulationModel model)
			throws CalculationException {
		RandomVariable value = new RandomVariableFromDoubleArray(0.0);

		for (int i = 0; i < underlyings.length; i++) {
			value = value.add(underlyings[i].getValue(evaluationTime, model));
		}

		return value;
	}

	@Override
	public String toString() {
		return name;
	}

}
