package ue1.a21;

import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public final class VectorMathMultiThreaded {
	
	private static final class VectorAddCallable implements Callable<Long> {
		private final double[] result;
		private final double[] left;
		private final double[] right;
		private int start;

		private VectorAddCallable(double[] result, double[] left, double[] right, int start) {
			this.result = result;
			this.left = left;
			this.right = right;
			if(start >= PROCESSOR_COUNT) throw new RuntimeException();
			this.start = start;
		}

		@Override
		public Long call() throws Exception {
			final long startTime = System.currentTimeMillis();
			if(left.length < start) return System.currentTimeMillis() - startTime;
			for (int x = start; x < left.length; x=x+4) {					
				result[x] = left[x] + right[x];
			}
			return System.currentTimeMillis() - startTime;
		}
	}


	static private final int DEFAULT_SIZE = 100;
	static private final int WARMUP_LOOPS = 25000;
	
	static private final int PROCESSOR_COUNT = 4;
	static final ExecutorService THREAD_POOL = Executors.newFixedThreadPool(PROCESSOR_COUNT);
	
	/**
	 * Sums two vectors within a single thread.
	 * @param left the first operand
	 * @param right the second operand
	 * @return the resulting vector
	 * @throws NullPointerException if one of the given parameters is {@code null}
	 * @throws IllegalArgumentException if the given parameters do not share the same length
	 */
	static public double[] add (final double[] left, final double[] right) {
		if (left.length != right.length) throw new IllegalArgumentException();		
		final double[] result = new double[left.length];
		
		final Future<Long>[] futures = new Future[PROCESSOR_COUNT];
		for(int i = 0; i < PROCESSOR_COUNT; i++) {
			futures[i] = THREAD_POOL.submit(new VectorAddCallable(result, left, right, i));
		}
		
		// IO slows down too much
		// System.out.format("Main-Thread: Waiting for child threads to finish!\n");
		
		try {
			for (final Future<Long> future : futures) {
				try {
					final long timestamp = getUninterruptibly(future);
					// System.out.format("child thread ended after %.2fs\n", timestamp * 0.001);
				} catch (final ExecutionException exception) {
					final Throwable cause = exception.getCause();	// manual precise rethrow for cause!
					if (cause instanceof Error) throw (Error) cause;
					if (cause instanceof RuntimeException) throw (RuntimeException) cause;
					throw new AssertionError();
				}
			}
		} catch (final Throwable exception) {
			for (final Future<Long> future : futures) {
				future.cancel(true);
			}
		}

		// System.out.format("Main-Thread: All child threads are done!\n");
		return result;
	}


	/**
	 * Multiplexes two vectors within a single thread.
	 * @param left the first operand
	 * @param right the second operand
	 * @return the resulting matrix
	 * @throws NullPointerException if one of the given parameters is {@code null}
	 */
	static public double[][] mux (final double[] left, final double[] right) {
		final double[][] result = new double[left.length][right.length];
		for (int x = 0; x < left.length; ++x) {
			for (int rightIndex = 0; rightIndex < right.length; ++rightIndex) {
				result[x][rightIndex] = left[x] * right[rightIndex];
			}
		}
		return result;
	}


	/**
	 * Runs both vector summation and vector multiplexing for demo purposes.
	 * @param args the argument array
	 */
	static public void main (final String[] args) {
		try {					
			final int size = args.length == 0 ? DEFAULT_SIZE : Integer.parseInt(args[0]);
			System.out.format("Computation is performed using a single thread for operand size %d.\n", size);
		
			// initialize operand vectors
			final double[] a = new double[size], b = new double[size];
			for (int index = 0; index < size; ++index) {
				a[index] = index + 1.0;
				b[index] = index + 2.0;
			}
		
			// Warm-up phase to force hot-spot translation of byte-code into machine code, code-optimization, etc!
			// Computation of resultHash prevents JIT from over-optimizing the warmup-phase (by complete removal),
			// which would happen if the loop does not compute something that is used outside of it.
			int resultHash = 0;
			for (int loop = 0; loop < WARMUP_LOOPS; ++loop) {
				double[] c = add(a, b);
				resultHash ^= c.hashCode();
		
				double[][] d = mux(a, b);
				resultHash ^= d.hashCode();
			}
			System.out.format("warm-up phase ended with result hash %d.\n", resultHash);
		
			final long timestamp0 = System.currentTimeMillis();
			for (int loop = 0; loop < 10000; ++loop) {
				final double[] sum = add(a, b);
				resultHash ^= sum.hashCode();
			}
		
			final long timestamp1 = System.currentTimeMillis();
			for (int loop = 0; loop < 10000; ++loop) {
				final double[][] mux = mux(a, b);
				resultHash ^= mux.hashCode();
			}
			final long timestamp2 = System.currentTimeMillis();
			System.out.format("timing phase ended with result hash %d.\n", resultHash);
			System.out.format("a + b computed in %.4fms.\n", (timestamp1 - timestamp0) * 0.0001);
			System.out.format("a x b computed in %.4fms.\n", (timestamp2 - timestamp1) * 0.0001);
		
			if (size <= 100) {
				final double[] sum = add(a, b);
				final double[][] mux = mux(a, b);
				System.out.print("a = ");
				System.out.println(Arrays.toString(a));
				System.out.print("b = ");
				System.out.println(Arrays.toString(b));
				System.out.print("a + b = ");
				System.out.println(Arrays.toString(sum));
				System.out.print("a x b = [");
				for (int index = 0; index < mux.length; ++index) {
					System.out.print(Arrays.toString(mux[index]));
				}
				System.out.println("]");
			}
		} finally {
			THREAD_POOL.shutdownNow();
		}
	}
	
	static public <T> T getUninterruptibly (final Future<T> future) throws NullPointerException, ExecutionException {
		T result;

		boolean interrupted = false;
		try {
			while (true) {
				try {
					result = future.get();
					break;
				} catch (final InterruptedException exception) {
					interrupted = true;
				}
			}
		} finally {
			if (interrupted) Thread.currentThread().interrupt();
		}

		return result;
	}
}