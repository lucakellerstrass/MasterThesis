package kellerstrass.swap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.IntToDoubleFunction;
import java.util.stream.IntStream;

import net.finmath.exception.CalculationException;
import net.finmath.marketdata.model.AnalyticModel;
import net.finmath.marketdata.products.AnalyticProduct;
import net.finmath.montecarlo.RandomVariableFromDoubleArray;
import net.finmath.montecarlo.interestrate.LIBORModelMonteCarloSimulationModel;
import net.finmath.montecarlo.interestrate.products.AbstractLIBORMonteCarloProduct;
import net.finmath.stochastic.RandomVariable;

/**
 * Implements the valuation of a portfolio of products implementing
 * <code>AnalyticProductInterface</code>.
 *
 * @author Christian Fries & some Luca Kellerstrass
 * @version 1.0
 */
public class PortfolioExtended extends AbstractLIBORMonteCarloProduct implements AnalyticProduct {

	private ArrayList<AbstractLIBORMonteCarloProduct>	products;
	private ArrayList<Double>					weights;

	/**
	 * Create a portfolio of products implementing
	 * <code>AnalyticProductInterface</code>. The portfolio consists
	 * of an array of products and a corresponding array of weights.
	 * The value of the portfolio is given by the sum over
	 * <code>
	 * 	weights[i] * products.get(i).getValue(evaluationTime, model)
	 * </code>
	 *
	 * Note that a product in the array of products may itself be
	 * a <code>Portfolio</code> (hence you may easily combine portfolios).
	 *
	 * @param products Array of products implementing <code>AnalyticProductInterface</code>.
	 * @param weights Array of weights used in the valuation as a multiplicator.
	 */
	public PortfolioExtended(List<AbstractLIBORMonteCarloProduct> products, List<Double> weights) {
		super();
		this.products = new ArrayList<>();
		this.weights = new ArrayList<>();
		this.products.addAll(products);
		this.weights.addAll(weights);
	}

	/**
	 * Create a portfolio of products implementing
	 * <code>AnalyticProductInterface</code>. The portfolio consists
	 * of an array of products and a corresponding array of weights.
	 * The value of the portfolio is given by the sum over
	 * <code>
	 * 	weights[i] * products.get(i).getValue(evaluationTime, model)
	 * </code>
	 *
	 * The portfolio is created by taking all products and weights of a given portfolio
	 * and adding other given products and weights.
	 *
	 * @param portfolio A given portfolio, which will become part of this portfolio.
	 * @param products Array of products implementing <code>AnalyticProductInterface</code>.
	 * @param weights Array of weights used in the valuation as a multiplicator.
	 */
	public PortfolioExtended(PortfolioExtended portfolio, List<AbstractLIBORMonteCarloProduct> products, List<Double> weights) {
		super();
		this.products = new ArrayList<>();
		this.weights = new ArrayList<>();
		this.products.addAll(portfolio.getProducts());
		this.weights.addAll(portfolio.getWeights());
		this.products.addAll(products);
		this.weights.addAll(weights);
	}

	/**
	 * Create a portfolio consisting of a single product with a given weight.
	 *
	 * @param product A product, implementing  implementing <code>AnalyticProductInterface</code>.
	 * @param weight A weight used in the valuation as a multiplicator.
	 */
	public PortfolioExtended(AbstractLIBORMonteCarloProduct product, double weight) {
		super();
		products = new ArrayList<>();
		weights = new ArrayList<>();
		products.add(product);
		weights.add(weight);
	}

	/**
	 * Create a portfolio of products implementing
	 * <code>AnalyticProductInterface</code>.
	 *
	 * The value of the portfolio is given by the sum over
	 * <code>
	 * 	products.get(i).getValue(evaluationTime, model)
	 * </code>
	 *
	 * Note that a product in the array of products may itself be
	 * a <code>Portfolio</code> (hence you may easily combine portfolios).
	 *
	 * @param products Array of products implementing <code>AnalyticProductInterface</code>.
	 */
	public PortfolioExtended(List<AbstractLIBORMonteCarloProduct> products) {
		this(products, Collections.nCopies(products.size(), new Double(1.0)));
	}


	/**
	 * Returns the list of products as an unmodifiable list.
	 * Calling <code>add</code> on this list will result in an {@link UnsupportedOperationException}.
	 *
	 * @return The list of products as an unmodifiable list.
	 */
	public List<AbstractLIBORMonteCarloProduct> getProducts() {
		return Collections.unmodifiableList(products);
	}

	/**
	 * Returns the list of weights as an unmodifiable list.
	 * Calling <code>add</code> on this list will result in an {@link UnsupportedOperationException}.
	 *
	 * @return The list of weights as an unmodifiable list.
	 */
	public List<Double> getWeights() {
		return Collections.unmodifiableList(weights);
	}

	@Override
	public RandomVariable getValue(double evaluationTime, LIBORModelMonteCarloSimulationModel model)
			throws CalculationException {
		RandomVariable value = new RandomVariableFromDoubleArray(0.0);
		
		for(int i = 0; i <products.size();i++) {
			value = value.add(products.get(i).getValue(evaluationTime, model).mult(weights.get(i)));
					}

		return value;
	}

	@Override
	public double getValue(double evaluationTime, AnalyticModel model) {
		System.out.println("does not work at all");
		return 0;
	}
}