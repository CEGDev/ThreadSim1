/**
 * 	Queens College
 *	Carl Gentile
 */

import java.util.ArrayList;
import java.util.Random;

/**
 * The wizard class contains the methods necessary 
 * for the wizard to engage in the competition.
 * 
 * @author Carl
 *
 */
public class Wizard extends Thread
{
	/**
	 * The wizard needs access to the competitor array
	 * in order to compute the scores and 
	 * handle the critical thinking obstacle.
	 */
	private Competitor competitors[];
	
	/**
	 * Forest containing the words that competitors need to match
	 * with the magic word.
	 */
	private Forest forest;
	
	/**
	 * Reference to the competition the wizard is hosting.
	 */
	private Competition competition;
	
	/**
	 * The competitor that won the forest obstacle.
	 */
	private Competitor forestWinner;
	
	/**
	 * The competitor that won the mountain obstacle.
	 */
	private Competitor mountainWinner;
	
	/**
	 * The competitor that won the river obstacle.
	 */
	private Competitor riverWinner;
	
	/**
	 * The competitor that won the critical thinking obstacle.
	 */
	private Competitor criticalThinkingWinner;
	
	public Wizard(Competitor comps[], Competition comp)
	{
		super("Wizard");
		
		competition = comp;
		
		competitors = comps;
	}//Wizard Constructor
	
	public void run()
	{
		msg("is creating the forest.");
		
		forest = new Forest();
		
		competition.setForest(forest);
		
		msg("has created the forest.");
		
		awardForestPoints();
		
		criticalThinking();
		
		printScores();
		
		findPrince();
	}//run
	
	/**
	 * Handles the critical thinking obstacle by busy waiting for all the competitors to arrive.
	 * The competitors also busy wait for their score, and meet with the wizard on a first-come 
	 * first-serve basis.
	 */
	public void criticalThinking()
	{
		int total = 0;
		
		int questions[] = new int[3];
		
		Random random = new Random();
		
		while(Competitor.getCriticalThinkingCount() < competition.getNumCompetitors()){};
		
		for(Competitor comp : Competitor.criticalThinkingQueue)
		{
			for(int i = 0; i < questions.length; i++)
			{
				questions[i] = random.nextInt(9) + 1;
				
				if(questions[i] <= 3)
				{
					msg(comp.toString() + " received " + 0 + " points for answering question " + (i+1) + " incorrectly.");
				}//if
				else if(questions[i] <= 6)
				{
					msg(comp.toString() + " received " + 2 + " points for partial credit on question " + (i+1) + ".");
					total += 2;
				}//else if
				else
				{
					msg(comp.toString() + " received " + 3 + " points for answering question " + (i+1) + " correctly.");
					total += 3;
				}//else
			}//for
			
			comp.setCriticalThinkingScore(total);
			
			total = 0;
		}//for
		
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
	 * Randomly generates a 5 letter magic word, for each competitor, from the set { a, b, c, d }.
	 * @return
	 */
	public String getMagicWord()
	{
		String word = "";
		
		Random random = new Random();
		
		for(int j = 0; j < 5; j++)
		{
			word += (char)(random.nextInt(4) + 'a');
		}//for
		
		return word;
		
	}//getMagicWord
	
	/**
	 * Assigns points to the competitors based on FCFS discovery of the magic word.
	 * The forestWinner is set to the first competitor to find the magic word, which
	 * is the first competitor to be taken from the forestQueue.
	 * If the queue is empty, the forestWinner is decided by elapsedForestTime.
	 */
	private void awardForestPoints()
	{
		//Wait for all the competitors to finish the forest before awarding points.
		while(Competitor.goneThroughForest() < 8){};
		
		int score = Competitor.forestCompletionCount();
	
		boolean foundWinner = false;
		
		if( ! (Competitor.forestQueue.isEmpty() ))
		{
			
			
			for(Competitor comp : Competitor.forestQueue )
			{
				try
				{
					if(foundWinner)
					{
						Competitor.forestQueue.take().setForestScore(score);
					}//if
					else
					{
						forestWinner = Competitor.forestQueue.take();
						forestWinner.setForestScore(score);
						foundWinner = true;
					}//else
				}//try
				catch (InterruptedException e)
				{
					System.out.println("Interrupted while using take().");
				}//catch
				
				score--;
				
			}//for
		}
		else
		{
			//If the forestQueue is empty, then nobody found the magic word.
			//forestWinner becomes the competitor who completed the forest the fastest.
			
			forestWinner = competitors[0];
			
			for(int i = 1; i < competitors.length; i++)	
			{
				if(competitors[i].getElapsedForestTime() < forestWinner.getElapsedForestTime())
				{
					forestWinner = competitors[i];
				}
			}
		}
		
	}//awardForestPoints
	
	/**
	 * Print the scores, and find the winners, of each obstacle.
	 */
	private void printScores()
	{
		//Busy wait for all the competitors to finish before printing the scores.
		for(Competitor comp : competitors)
		{
			while(comp.isAlive()){};
		}//for
		
		printForestResults();
		
		printMountainResults();
		
		printRiverResults();
		
		printCriticalThinkingResults();
		
		printObstacleTime();
		
		msg("************** Winners **************");
		
		printWinners();
		
	}//printScores
	
	/**
	 * Prints the results for the forest obstacle.
	 */
	private void printForestResults()
	{
		
		msg("************** Forest Results **************");
		
		for(int i = 0; i < competitors.length; i++)
		{
			
			int index = i;
			
			for(int j = i+1; j < competitors.length; j++)
			{
				if(competitors[j].getForestScore() > competitors[index].getForestScore())
				{
					index = j;
				}//if
			}//for
			
			if(index != i)
			{
				//swap
				Competitor temp = competitors[i];
				competitors[i] = competitors[index];
				competitors[index] = temp;
			}//if
			
		}//for
		
		for(Competitor comp : competitors)
		{
			msg(comp + " completed the forest in " + comp.getElapsedForestTime() 
					+ "ms and was awarded " + comp.getForestScore() + " point(s) for the forest." );
		}//for

	}//printForestResults
	
	/**
	 * Prints the results for the mountain obstacle.
	 */
	private void printMountainResults()
	{
		msg("************** Mountain Results **************");
		
		//Sort the competitors by elapsedMountainTime.
		for(int i = 0; i < competitors.length; i++)
		{
			
			int index = i;
			
			for(int j = i+1; j < competitors.length; j++)
			{
				if(competitors[j].getElapsedMountainTime() < competitors[index].getElapsedMountainTime())
				{
					index = j;
				}//if
			}//for
			
			if(index != i)
			{
				//swap
				Competitor temp = competitors[i];
				competitors[i] = competitors[index];
				competitors[index] = temp;
			}//if
			
		}//for
		
		for(Competitor comp : competitors)
		{
			msg(comp.toString() + " elapsed mountain time:  " + comp.getElapsedMountainTime() + "ms.");
		}//for
		
		mountainWinner = competitors[0];
	}//printMountainResults
	
	
	/**
	 * Prints the results for the river obstacle.
	 */
	private void printRiverResults()
	{
		msg("************** River Results **************");
		
		//Sort the competitors by elapsedRiverTime.
		for(int i = 0; i < competitors.length; i++)
		{
			
			int index = i;
			
			for(int j = i+1; j < competitors.length; j++)
			{
				if(competitors[j].getElapsedRiverTime() < competitors[index].getElapsedRiverTime())
				{
					index = j;
				}//if
			}//for
			
			if(index != i)
			{
				//swap
				Competitor temp = competitors[i];
				competitors[i] = competitors[index];
				competitors[index] = temp;
			}//if
			
		}//for
		
		for(Competitor comp : competitors)
		{
			msg(comp.toString() + " elapsed river time:  " + comp.getElapsedRiverTime() + "ms.");
		}//for
		
		riverWinner = competitors[0];
	}//printRiverResults

	
	/**
	 * Prints the results for the critical thinking obstacle.
	 */
	private void printCriticalThinkingResults()
	{
		msg("************** Critical Thinking Results **************");
		
		
		for(int i = 0; i < competitors.length; i++)
		{
			
			int index = i;
			
			for(int j = i+1; j < competitors.length; j++)
			{
				if(competitors[j].getCriticalThinkingScore() > competitors[index].getCriticalThinkingScore())
				{
					index = j;
				}//if
			}//for
			
			if(index != i)
			{
				//swap
				Competitor temp = competitors[i];
				competitors[i] = competitors[index];
				competitors[index] = temp;
			}//if
			
		}//for
		
		for(Competitor comp : competitors)
		{
			msg(comp.toString() + " completed critical thinking with: " + comp.getCriticalThinkingScore() 
					+ " points in " + comp.getElapsedCriticalThinkingTime() + "ms.");
		}//for
		
		//Handle a tie, and set the winner.
		if(competitors[0].getCriticalThinkingScore() > 0)
		{
			criticalThinkingWinner = competitors[0];
			
			ArrayList<Competitor> winnerList = new ArrayList<Competitor>();
			
			winnerList.add(competitors[0]);
			
			//Check for a tie.
			for(int i = 0; i < competitors.length - 1; i++)
			{
				if(competitors[i].getCriticalThinkingScore() == competitors[i+1].getCriticalThinkingScore())
				{
					winnerList.add(competitors[i+1]);
				}
				else
				{
					break;
				}
			}
			if(winnerList.size() > 1)
			{
				
				for(Competitor comp : winnerList)
				{
					if(comp.getElapsedCriticalThinkingTime() < criticalThinkingWinner.getElapsedCriticalThinkingTime())
					{
						criticalThinkingWinner = comp;
					}//if
					
				}//for
			}//if
		}//if
		else
		{
			for(Competitor comp : competitors)
			{
				if(comp.getElapsedCriticalThinkingTime() < criticalThinkingWinner.getElapsedCriticalThinkingTime())
				{
					criticalThinkingWinner = comp;
				}//if
			}//for
		}//else
		
	}//printCriticalThinkingResults
	
	
	/**
	 * Prints the total time spent of each obstacle and rest stop, 
	 * for each competitor in sorted order.
	 */
	private void printObstacleTime()
	{
		
		msg("************** Total Obstacle And Rest Times **************");
		
		
		//Sort the competitors by total obstacle time.
		for(int i = 0; i < competitors.length; i++)
		{
			
			int index = i;
			
			for(int j = i+1; j < competitors.length; j++)
			{
				if(competitors[j].getTotalObstacleTime() < competitors[index].getTotalObstacleTime())
				{
					index = j;
				}//if
			}//for
			
			if(index != i)
			{
				//swap
				Competitor temp = competitors[i];
				competitors[i] = competitors[index];
				competitors[index] = temp;
			}//if
			
		}//for
		
		for(Competitor comp : competitors)
		{
			msg(comp.toString() + " turn around time: " + comp.getTotalObstacleTime() + "ms.");
		}//for
	}//printObstacleTime
	
	/**
	 * Finds the prince and handles a tie.
	 */
	private void findPrince()
	{
		ArrayList<Competitor> winnerList = new ArrayList<Competitor>();
		
		winnerList.add(competitors[0]);
		
		//Check for a tie.
		for(int i = 0; i < competitors.length - 1; i++)
		{
			if(competitors[i].getTotalObstacleTime() == competitors[i+1].getTotalObstacleTime())
			{
				winnerList.add(competitors[i+1]);
			}
			else
			{
				break;
			}
		}
		
		if(winnerList.size() > 1)
		{
			Random rand = new Random();
			int winner = rand.nextInt(winnerList.size());
			
			msg("There was a tie!");
			msg(winnerList.toString() + " have all finished with the same turnaround time.");
			msg(winnerList.get(winner).toString() + " has become the prince!!");
			msg("The princess chooses to marry: " + winnerList.get(winner) + "!");
		}
		else
		{
			msg(winnerList.get(0).toString() + " has become the prince!!");
			msg("The princess chooses to marry: " + winnerList.get(0) + "!");
		}
	}//findPrince
	
	/**
	 * Awards gold to the winners of each obstacle.
	 */
	private void printWinners()
	{
		msg(forestWinner + " was awarded 1000 gold coins for winning the forest!");
		msg(mountainWinner + " was awarded 1000 gold coins for winning the mountain!");
		msg(riverWinner + " was awarded 1000 gold coins for winning the river!");
		msg(criticalThinkingWinner + " was awarded 1000 gold coins for winning critical thinking!");
	}//printWinners
	
}//Wizard class.
