class Product implements MatrixExpression {
    private final MatrixExpression m1;
    private final MatrixExpression m2;
    // RI: m1's column count == m2's row count, or m1 or m2 is scalar
    public Product(MatrixExpression m1, MatrixExpression m2) {
        this.m1 = m1;
        this.m2 = m2;
    }

    public boolean isIdentity() {
        return m1.isIdentity() && m2.isIdentity();
    }

    public MatrixExpression scalars() {
        return MatrixExpression.times(m1.scalars(), m2.scalars());
    }

    public MatrixExpression matrices() {
        return MatrixExpression.times(m1.matrices(), m2.matrices());
    }

    public MatrixExpression optimize() {
        return MatrixExpression.times(scalars(), matrices());
    }
}