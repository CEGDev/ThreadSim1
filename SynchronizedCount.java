/**
 * This class is used to create synchronized
 * counters that are shared static variables
 * among threads of the same type.
 * 
 * @author Carl
 *
 */
public class SynchronizedCount
{
	private int count = 0;
	
	public synchronized void increment()
	{
		count++;
	}//increment
	
	public synchronized int getCountAndIncrement()
	{
		int temp = count;
		count++;
		return temp;
	}//getCountAndIncrement
	
	public synchronized int decrement()
	{
		count--;
		return count;
	}//decrement
	
	public synchronized int getCount()
	{
		return count;
	}//getCount
	
	public synchronized String toString()
	{
		return Integer.toString(count);
	}//toString
	
	public synchronized void setCount(int c)
	{
		count = c;
	}//setCount
	
}//SynchronizedCount
