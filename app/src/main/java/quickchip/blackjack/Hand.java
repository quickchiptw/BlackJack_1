package quickchip.blackjack;

import java.util.ArrayList;

abstract class Hand
{
	ArrayList<Card> hand; // a Hand is an ArrayList of Cards
	
	/*In 21, "playing" is for the dealer to know whether or not he still needs to parse an action on the hand *
	 * If surrender, false
	 * If stand, false
	 * If double down, false
	 * If insurance, false
	 * Else, true
	 * */
	
	String ownerName;
	Status status; // The hand's status (e.g. in blackjack - bust/win/push/blackjack)
	Bet bet; 
	
	boolean playing   = true, 
			toBePayed = true, 
			hasAce	  = false; 
	
	Hand(String name)
	{
		ownerName=name;
		hand=new ArrayList<Card>(); 
		bet=new Bet(); 	
		setStatus(Status.PLAYING);
	}
	
	// Constructor for splitting.
	Hand(String name, Card card, float betValue)
	{
		ownerName=name;
		hand=new ArrayList<Card>();
		addCard(card); 
		bet=new Bet(betValue); 
		setStatus(Status.PLAYING);
	}
	
	//Clear the hand of cards.
	void update()
	{ hand.removeAll(hand); }
	
	void addCard(Card card) 
	{ if (card != null)    hand.add(card); }
	
	void removeCard(byte b)
	{ hand.remove(b); }
	
	Card getCard(byte b)
	{ return hand.get(b); }
	
	Card popCard(byte b)
	{	
		Card card = hand.get(b); 		
		hand.remove(b); 	
		return card; 	
	}
	
    void setStatus(Status status)
	{ this.status=status; }

	void setPlaying(boolean b)
	{ playing = b; }
	
	void setToBePayed(boolean b)
	{ toBePayed=b; }
	
	void incrementBet(float stake)
	{ bet.incrementBet(stake); }
	
	void clearBet()
	{ bet.clear(); }
	
	//Accessors
	String getOwnerName()
	{ return ownerName; }
	
	Status getStatus()
	{ return status; }
	
	boolean isPlaying()
	{ return playing; } 
	
	boolean toBePayed()
	{ return toBePayed; }
	
	Bet getBet()
	{ return bet; }
	
	byte getCardCount()
	{ return (byte)hand.size(); }
	
	boolean isEmpty()
	{ return hand.isEmpty(); }
	
	boolean doesHaveAce()
	{ return hasAce; }	
}