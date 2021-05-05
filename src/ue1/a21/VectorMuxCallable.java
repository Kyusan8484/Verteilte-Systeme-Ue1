package ue1.a21;

import java.util.concurrent.Callable;

class VectorMuxCallable implements Callable<Long> {
    private final double[][] result;
    private final double[] left;
    private final double[] right;
    private int start;

    VectorMuxCallable(double[][] result, double[] left, double[] right, int start) {
        this.result = result;
        this.left = left;
        this.right = right;
        if(start >= VectorMathMultiThreaded.PROCESSOR_COUNT) throw new RuntimeException();
        this.start = start;
    }

    @Override
    public Long call() throws Exception {
        final long startTime = System.currentTimeMillis();
        if(left.length < start) return System.currentTimeMillis() - startTime;

        for (int x = start; x < left.length; x += VectorMathMultiThreaded.PROCESSOR_COUNT) {

            for (int rightIndex = 0; rightIndex < right.length; ++rightIndex) {
                result[x][rightIndex] = left[x] * right[rightIndex];
            }
        }
        return System.currentTimeMillis() - startTime;
    }
}
