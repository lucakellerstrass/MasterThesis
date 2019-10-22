package kellerstrass.defaultProbability.Cds;

public interface MathOperation {
double operation (double a);


static double operate(double a, MathOperation MathOperation) {
	return MathOperation.operation(a);
}

}