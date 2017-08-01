/**
 * 	Queens College
 *	Carl Gentile
 */

import java.io.File;

/**
 * Simulates the competition.
 * 
 * @author Carl
 *
 */
public class Competition
{
	private static long startTime = System.currentTimeMillis();
	private Competitor competitors[];
	private Wizard wizard;
	private Forest forest;
	private int numberOfCompetitors;
	
	public Competition(int size)
	{
		competitors = new Competitor[size];
		numberOfCompetitors = size;
		init();
	}//Competition Constructor
	
	public void init()
	{
		
		wizard = new Wizard(competitors, this);
		
		//Create all the competitor threads for N = 8
		for(int i = 0; i < competitors.length; i++)
		{
			if(i < 7)
			{
				competitors[i] = new Competitor(i, (i+1), this);
			}//if
			else
			{
				competitors[i] = new Competitor(i, -1, this);
			}//else
		}//for
		
		wizard.start();
		
		//Start the threads.
		for(int i = 0; i < 8; i++)
		{
			competitors[i].start();
		}//for

	}//init
	
	/**
	 * Returns the time elapsed.
	 * @return
	 */
	public long age()
	{
		return System.currentTimeMillis() - startTime;
	}//age
	
	public static void main(String args[])
	{
		new Competition(8);
		
	}//main
	
	/**
	 * Returns the file with magic words.
	 * @return
	 */
	public File getForestFile()
	{
		while(forest == null){} // Busy wait in case the forest has not yet been created.
		
		return forest.getForestFile();
	}//getForestFile
	
	/**
	 * Gets the next competitor.
	 * @param i
	 * @return
	 */
	public Competitor getNextCompetitor(int i)
	{
		return competitors[i];
	}//getNextCompetitor

	/**
	 * Sets the forest for the competition 
	 * after the wizard creates it.
	 * 
	 * @param f
	 */
	public void setForest(Forest f)
	{
		forest = f;
	}//setForest

	/**
	 * Gets a randomly generated magic word.
	 * @return
	 */
	public String getMagicWord()
	{
		return wizard.getMagicWord();
	}//getMagicWord
	
	/**
	 * Returns the number of competitors in the competition.
	 * @return
	 */
	public int getNumCompetitors()
	{
		return numberOfCompetitors;
	}//getNumCompetitors


}//Competition class
