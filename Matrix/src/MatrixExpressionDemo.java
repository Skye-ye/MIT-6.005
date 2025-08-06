public class MatrixExpressionDemo {

    public static void main(String[] args) {
        // Create some matrix expressions
        MatrixExpression scalar2 = MatrixExpression.make(2.0);
        MatrixExpression scalar3 = MatrixExpression.make(3.5);

        double[][] matrixA = {{1, 2}, {3, 4}};
        double[][] matrixB = {{2, 0}, {1, 3}};
        MatrixExpression matA = MatrixExpression.make(matrixA);
        MatrixExpression matB = MatrixExpression.make(matrixB);

        // Simple scalar multiplication
        MatrixExpression result1 = MatrixExpression.times(scalar2, scalar3);

        // Scalar times matrix
        MatrixExpression result2 = MatrixExpression.times(scalar2, matA);

        // Matrix times matrix
        MatrixExpression result3 = MatrixExpression.times(matA, matB);

        // Chain multiplication: 2 × matA × matB
        MatrixExpression chain = MatrixExpression.times(scalar2,
                MatrixExpression.times(matA, matB));

        // Using identity matrix
        MatrixExpression withIdentity = MatrixExpression.times(MatrixExpression.I, matA);

        // Complex expression: (2 × 3.5) × (matA × matB)
        MatrixExpression complex = MatrixExpression.times(
                MatrixExpression.times(scalar2, scalar3),
                MatrixExpression.times(matA, matB)
        );

        // Optimize expressions
        MatrixExpression optimized = complex.optimize();

        System.out.println("Matrix expressions created and computed successfully!");
    }
}