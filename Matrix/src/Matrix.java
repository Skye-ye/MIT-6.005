class Matrix implements MatrixExpression {
    private final double[][] array;
    // RI: array.length > 0, and all array[i] are equal nonzero length
    public Matrix(double[][] array) {
        this.array = array; // note: danger!
    }

    public boolean isIdentity() {
        for (int row = 0; row < array.length; row++) {
            for (int col = 0; col < array[row].length; col++) {
                double expected = (row == col) ? 1 : 0;
                if (array[row][col] != expected) return false;
            }
        }
        return true;
    }

    public MatrixExpression scalars() { return I; }

    public MatrixExpression matrices() { return this; }

    public MatrixExpression optimize() { return this; }
}