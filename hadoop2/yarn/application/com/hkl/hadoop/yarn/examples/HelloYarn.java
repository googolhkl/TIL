package com.hkl.hadoop.yarn.examples;

/**
 * Created by hkl on 16. 7. 27.
 */

public class HelloYarn
{
	private static final long MEGABYTE = 1024L * 1024L;

	public HelloYarn()
	{
		System.out.println("HelloYarn!");
	}

	public static long bytesToMegabytes(long bytes)
	{
		return bytes / MEGABYTE;
	}

	public void printMemoryStats()
	{
		long freeMemory = bytesToMegabytes(Runtime.getRuntime().freeMemory());
		long totalMemory = bytesToMegabytes(Runtime.getRuntime().totalMemory());
		long maxMemory = bytesToMegabytes(Runtime.getRuntime().maxMemory());

		System.out.println("The amoun of free memory in the java Virtual Machine: " + freeMemory);
		System.out.println("The total amount of memory in the java virtual machine: " + totalMemory);
		System.out.println("The maximum amount of memory that the java virtual machine: " + maxMemory);
	}

	public static void main(String[] args)
	{
		HelloYarn helloYarn = new HelloYarn();
		helloYarn.printMemoryStats();
	}
}
