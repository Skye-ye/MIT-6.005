class Scalar implements MatrixExpression {
    private final double value;
    public Scalar(double value) {
        this.value = value;
    }

    public boolean isIdentity() { return value == 1; }

    public MatrixExpression scalars() { return this; }

    public MatrixExpression matrices() { return I; }

    public MatrixExpression optimize() { return this; }
}