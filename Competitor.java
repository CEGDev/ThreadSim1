/**
 * 	Queens College
 *	Carl Gentile
 */

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;


public class Competitor extends Thread
{
	/**
	 * Represents the total number of competitors 
	 * that successfully found the magic word.
	 */
	private volatile static SynchronizedCount foundMagicWordCount;
	
	/**
	 * This thread safe queue is used to preserve FCFS order for 
	 * competitors that were able to find the magic word.
	 */
	public static ArrayBlockingQueue<Competitor> forestQueue;
	
	/**
	 * This thread safe queue is used to preserve FCFS order for 
	 * competitors that are waiting for the critical thinking results.
	 */
	public static ArrayBlockingQueue<Competitor> criticalThinkingQueue;
	
	/**
	 * This thread safe queue is used to preserve FCFS order for 
	 * competitors that have finished the competition.
	 */
	public static ArrayBlockingQueue<Competitor> finishQueue;
	
	/**
	 * Total number of competitors that have gone through 
	 * the forest, regardless of whether or not they 
	 * found the magic word.
	 */
	private static SynchronizedCount forestCount;
	
	/**
	 * Boolean array used for the mountain passage busy wait.
	 * 
	 * In Java, arrays are created on the heap and every element 
	 * of the array is given a default value depending on its type. 
	 * For the boolean data type the default value is false.
	 */
	private volatile static boolean readyArray[];
	
	/**
	 * Represents the total number of competitors 
	 * that have arrived at the mountain passage.
	 */
	private volatile static SynchronizedCount mountainArrivalCount;
	
	/**
	 * Represents the total number of competitors
	 * that have arrived at the river.
	 */
	private volatile static SynchronizedCount riverArrivalCount;
	
	/**
	 * Represents the total number of competitors
	 * that have arrived at the river.
	 */
	private volatile static SynchronizedCount criticalThinkingArrivalCount;
	
	static
	{
		forestQueue = new ArrayBlockingQueue<Competitor>(8, true);
		
		criticalThinkingQueue = new ArrayBlockingQueue<Competitor>(8, true);
		
		finishQueue = new ArrayBlockingQueue<Competitor>(8, true);
		
		foundMagicWordCount = new SynchronizedCount();
		
		forestCount = new SynchronizedCount();
		
		readyArray = new boolean[8];
		
		mountainArrivalCount = new SynchronizedCount();
		
		riverArrivalCount = new SynchronizedCount();
	
		criticalThinkingArrivalCount = new SynchronizedCount();
		
	}//static initialization block.
	
	private Random random;
	
	/**
	 * Represents the next competitor in the sequence
	 * so the competitor can check if their friend is 
	 * still in the race.
	 */
	private int friend;
	
	private int forestScore;
	
	/**
	 * Represents this Competitor's total score
	 * for the critical thinking obstacle.
	 */
	private volatile int criticalThinkingScore;
	
	
	/**
	 * Represents this competitor's index in the readyArray.
	 * Competitors are assigned a mountainArrivalNumber in the order they arrive.
	 * Used to release the next competitor from the mountainBusyWait.
	 */
	private int mountainArrivalNumber;
	
	
	/**
	 * Represents the time spent in the 
	 * forest for this competitor.
	 */
	private long elapsedForestTime;
	
	
	/**
	 * Represents the mountain crossing
	 * time for this competitor.
	 */
	private long elapsedMountainTime;
	
	
	/**
	 * Represents the river crossing time 
	 * for this competitor.
	 */
	private long elapsedRiverTime;
	
	
	/**
	 * Represents the time it took for 
	 * this competitor to complete the 
	 * critical thinking obstacle.
	 */
	private long elapsedCriticalThinkingTime;
	
	
	/**
	 * totalElapsedObstacleTime = 
	 * (elapsedForestTime 	+ 
	 * 	elapsedMountainTime + 
	 * 	elapsedRiverTime 	+ 
	 * 	elapsedCriticalThinkingTime) 	+
	 * 	the random value from both rests.
	 * 
	 */
	private long totalElapsedObstacleTime;

	/**
	 * Represents the total time each competitor spent resting.
	 */
	private long totalRestTime;
	
	
	/**
	 * Reference to the competition that 
	 * this competitor is in.
	 */
	public Competition competition;

	

	/**
	 * Constructs a competitor for the competition.
	 * @param id - The name of the Competitor.
	 * @param nextCompetitor - value of the next competitor in the Competition's
	 * competitor array.
	 * @param comp - Reference to the competition that this competitor is in.
	 */
	public Competitor(int id, int nextCompetitor, Competition comp) 
	{
		super("Competitor-" + id);
		
		competition = comp;
		
		friend = nextCompetitor;
		
		random = new Random();
		
		forestScore = 0;
		
		criticalThinkingScore = -1;
		
		totalElapsedObstacleTime = 0;
		
		totalRestTime = 0;
		
	}//Constructor
	
	@Override
	public void run()
	{
		
		msg("is resting and eating before the forest.");
		
		rest();	//First rest stop
		
		msg("has finished resting.");
		
		enterForest();
		
		mountainBusyWait();
		
		enterMountainPassage();
		
		crossTheRiver();
		
		msg("is resting and eating before meeting the Wizard.");
		
		rest();	//Second rest stop.
		
		msg("has finished resting.");
	
		criticalThinking();
		
		totalElapsedObstacleTime = (elapsedForestTime + elapsedMountainTime + elapsedRiverTime + elapsedCriticalThinkingTime + totalRestTime);
		
		goHome();
	}//run

	private void goHome()
	{
		if(friend >= 0)
		{
			if(competition.getNextCompetitor(friend).isAlive())
			{
				try
				{
					msg("is waiting to join his friend " + competition.getNextCompetitor(friend) + " to go home.");
					competition.getNextCompetitor(friend).join();
				}
				catch (InterruptedException e)
				{
					msg("failed to wait for his friend to go home.");
				}
			}//if
		}//if
		msg("has gone home.");
	}//goHome

	/**
	 * Simulates a rest stop for each competitor.
	 */
	private void rest()
	{
		long time = random.nextInt(21) + 40 ;
		
		totalRestTime += time;
		
		try
		{
			sleep(time);
		}//try
		catch ( InterruptedException ie )
		{
			msg("Failed to rest due to an interruption");
		}//catch
	}//rest
	
	/**
	 * Simulates the forest obstacle for this competitor.
	 */
	public void enterForest()
	{
		elapsedForestTime = competition.age();
		
		setPriority(random.nextInt(5) + getPriority());
		
		msg("has entered the forest with priority " + getPriority() + ".");
		
		BufferedReader reader = null;
		
		String word = "";
		String magicWord = competition.getMagicWord();
		boolean foundMagicWord = false;
		
		msg("is using the compass to find the map with the magic word " + magicWord + ".");
		
		/*
		 * Search for the magic word.
		 */
		try
		{
			reader = new BufferedReader (
						new InputStreamReader (
							new FileInputStream( competition.getForestFile() )));
			 
			while((word = reader.readLine()) != null)
			{

				if(magicWord.equalsIgnoreCase(word))
				{
					foundMagicWord = true;
					break;
				}//if
			}//while
		
			reader.close();
			
		}//try
		catch(FileNotFoundException fnfe)
		{
			System.out.println("Unable to access the forest");
		}//catch
		catch (IOException e)
		{
			System.out.println("Error: IO Exception.");
		}//catch
		
		
		if(!foundMagicWord)
		{
			
			msg("did not find the magic word " + magicWord + " and is leaving the forest." );
		
			
			msg("is being forced to yield as a penalty for not finding the magic word.");
		
			//Penalty for not finding the magic word.
			yield();
			yield();
		}
		else
		{
			foundMagicWordCount.increment();
			
			msg("** has found the magic word " + magicWord + " and is leaving the forest. ** ");
			
			try
			{
				forestQueue.put(this);
			}
			catch (InterruptedException e)
			{
				System.out.println("Interrupted exception while adding competitor to the forest queue.");
			}//catch
		}
		
		forestCount.increment();
		
		setPriority(5);
		
		elapsedForestTime = competition.age() - elapsedForestTime;
		
		msg("completed the forest in " + elapsedForestTime + "ms.");
	}//enterForest
	
	/**
	 * Busy wait for all the competitors to arrive at the mountain.
	 */
	private void mountainBusyWait()
	{
		
		//Synchronized increment and get.
		mountainArrivalNumber = mountainArrivalCount.getCountAndIncrement();

		msg("is busy waiting at the mountain.");
		
		//If all the competitors have arrived, release first thread.
		if(mountainArrivalCount.getCount() == competition.getNumCompetitors())
		{
			readyArray[0] = true;
		}//if
		
		while(!readyArray[mountainArrivalNumber]){}
		
	}//mountainBusyWait
	
	/**
	 * Simulates the mountain passage for this competitor.
	 */
	private void enterMountainPassage()
	{
		//Arrival Time
		long mountainArrivalTime = competition.age();
		
		msg("is entering the mountain passage.");
		
		try
		{
			sleep(random.nextInt(2000));
		}//catch
		catch (InterruptedException e)
		{
			msg("failed the mountain passage and has recieved a penalty.");
			elapsedMountainTime = 1500; // Give penalty.
			return;
		}//catch
		
		//Calculate the elapsed time.
		elapsedMountainTime = competition.age() - mountainArrivalTime;
		
		msg("has completed the mountain with a travel time of " + elapsedMountainTime + "ms.");
		
		//Release next thread.
		if(this.mountainArrivalNumber < 7)
		{
			readyArray[mountainArrivalNumber + 1] = true;
		}//if
	}//enterMountainPassage

	/**
	 * Simulates the river obstacle for this competitor.
	 */
	private void crossTheRiver()
	{
		long riverArrivalTime = competition.age();
		
		msg(" is crossing the river.");
		
		int arrivalNumber = riverArrivalCount.getCountAndIncrement();
		
		try
		{
			if(arrivalNumber < competition.getNumCompetitors() - 1)
			{
				msg("has fallen asleep at the river.");
				
				sleep(99999);
			}//if
			else
			{
				msg("is waking up the other competitors.");
				
				for(int i = 0; i < competition.getNumCompetitors(); i++)
				{
					Competitor comp = competition.getNextCompetitor(i);
					if(this != comp)
					{
						comp.interrupt();
					}//if
				}//for
			}//else
		}//try
		catch (InterruptedException e)
		{
			msg("has been awakened and is continuing to cross the river.");
		}//catch
		
		elapsedRiverTime = riverArrivalTime + random.nextInt(200);
		
		msg("has crossed the river with a time of " + elapsedRiverTime + "ms.");
		
	}//crossTheRiver
	
	/**
	 * Simulates the critical thinking obstacle for this competitor.
	 */
	private void criticalThinking()
	{
		elapsedCriticalThinkingTime = competition.age();
		
		/*
		 * Queue the competitors to meet with the wizard, FCFS. 
		 */
		try
		{
			criticalThinkingQueue.put(this);
			msg("Is meeting with the wizard in the critical thinking obstacle.");
		}//try
		catch (InterruptedException e)
		{
			msg("failed to meet with the wizard.");
		}//catch
		
		/*
		 * Used a synchronized counter to let the wizard know that
		 * all the competitors have arrived at the critical thinking obstacle.
		 * 
		 * I still have the competitors busy wait for the score, but used this variable 
		 * to have the wizard also busy wait for all the competitors to arrive.
		 * Otherwise the wizard would compute the scores and the competitors would never 
		 * busy wait. Made it more interesting. 
		 * 
		 * Decided not to let the wizard busy wait for the queue to be full, 
		 * since put can throw an exception and the queue would never be full, causing deadlock.
		 */
		criticalThinkingArrivalCount.increment();
		
		msg("is busy waiting for the wizard to compute his score.");
		
		//Busy wait for the wizard to compute their score.
		while(criticalThinkingScore == -1){}
		
		msg("has completed the critical thinking obstacle with a score of " + criticalThinkingScore + " .");
		
		elapsedCriticalThinkingTime = competition.age() - elapsedCriticalThinkingTime;
	}//criticalThinking
	
	/**
	 * Prints a status update to the screen.
	 * @param c
	 */
	public void msg(String c) 
	{
		System.out.println(getName() + " ["+(competition.age())+"] "+": " + c + "\n");
	}//msg

	/**
	 * Returns the number of competitors who have 
	 * successfully found the magic word.
	 * @return
	 */
	public static int forestCompletionCount()
	{
		return foundMagicWordCount.getCount();
	}//forestCompletionCount
	
	public void setForestScore(int score)
	{
		forestScore = score;
	}//setForestScore

	public int getForestScore()
	{
		return forestScore;
	}//getForestScore

	/**
	 * Returns the number of competitors who have 
	 * gone through the forest with or without finding 
	 * the magic word.
	 * @return
	 */
	public static int goneThroughForest()
	{
		return forestCount.getCount();
	}//goneThroughForest
	
	public String toString()
	{
		return getName();
	}//toString

	/**
	 * Returns the number of competitors who 
	 * have arrived at the critical thinking obstacle.
	 * @return
	 */
	public static int getCriticalThinkingCount()
	{
		return criticalThinkingArrivalCount.getCount();
	}//getCriticalThinkingCount
	
	public void setCriticalThinkingScore(int total)
	{
		criticalThinkingScore = total;
	}//setCriticalThinkingScore

	public long getTotalObstacleTime()
	{
		return totalElapsedObstacleTime;
	}//getElapsedTime

	public long getElapsedForestTime()
	{
		return elapsedForestTime;
	}//getElapsedForestTime

	public long getElapsedMountainTime()
	{
		return elapsedMountainTime;
	}//getElapsedMountainTime
	
	public long getElapsedRiverTime()
	{
		return elapsedRiverTime;
	}//getElapsedRiverTime
	
	public long getElapsedCriticalThinkingTime()
	{
		return elapsedCriticalThinkingTime;
	}//getElapsedCriticalThinkingTime
	
	public int getCriticalThinkingScore()
	{
		return criticalThinkingScore;
	}//getCriticalThinkingScore
	
}//Competitor class
