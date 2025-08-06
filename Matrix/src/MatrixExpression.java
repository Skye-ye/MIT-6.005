public interface MatrixExpression {
    MatrixExpression I = new Identity();

    /**
     * @return a matrix expression consisting of just the scalar value
     */
    static MatrixExpression make(double value) {
        return new Scalar(value);
    }

    /**
     * @return a matrix expression consisting of just the matrix given
     */
    static MatrixExpression make(double[][] array) {
        return new Matrix(array);
    }

    /**
     * @param m1 first matrix expression
     * @param m2 second matrix expression
     * @return the product m1 × m2
     */
    static MatrixExpression times(MatrixExpression m1, MatrixExpression m2) {
        // Handle identity cases for optimization
        if (m1.isIdentity()) {
            return m2;
        }
        if (m2.isIdentity()) {
            return m1;
        }

        // Create a Product to represent the multiplication
        return new Product(m1, m2);
    }

    /**
     * @return true if this expression is an identity matrix
     */
    boolean isIdentity();

    /**
     * @return the product of all the scalars in this expression
     */
    MatrixExpression scalars();

    /**
     * @return the product of all the matrices in this expression.
     * times(scalars(), matrices()) is equivalent to this expression.
     */
    MatrixExpression matrices();

    /**
     * @return a new matrix expression with scalers combined.
     */
    MatrixExpression optimize();
}
