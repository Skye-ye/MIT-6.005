class Identity implements MatrixExpression {
    public Identity() {
    }

    public boolean isIdentity() { return true; }

    public MatrixExpression scalars() { return this; }

    public MatrixExpression matrices() { return this; }

    public MatrixExpression optimize() { return this; }
}