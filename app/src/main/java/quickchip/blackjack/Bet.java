package quickchip.blackjack;

class Bet 
{
	private float betValue;
	
	Bet()
	{ betValue=0; }
	
	Bet(float betValue) //used when splitting
	{ this.betValue = betValue; }
	
	void clear()
	{ betValue=0f; }
	
	void incrementBet(float stake)
	{ betValue+=stake; }
	
	//Accessors
	float getValue()
	{ return betValue; }
}