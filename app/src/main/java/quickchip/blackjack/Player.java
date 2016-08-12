package quickchip.blackjack;

import java.util.ArrayList;

class Player
{
	 private final String name;
	 private Wallet wallet; 
	
	 private ArrayList<BJHand> hands=new ArrayList<BJHand>();
	 
	 private boolean insurance = false; //did the player make an insurance bet?
	 private boolean playing; // all players are either playing or not (if not, they are removed from the Game list of players)
	 private float rebet=0f; // Saved last bet 	 
	
	 Player(String name, float initCurrency)
	 {
		 this.name=name;
		 wallet=new Wallet(initCurrency);
		 playing = true;
		 addHand(new BJHand(name));
	 }

	 void setPlaying(boolean b)
	 { playing = b; }

	 void update()
	 {
		 insurance=false;
		 BJHand firstHand = hands.get(0);
		 rebet = firstHand.getBet().getValue();
		 
		 if (firstHand.didDD()) rebet/=2f;
		 
		 clearHands();
		 addHand(new BJHand(name));
	 }
	 
	 void clearHands()
	 { hands.clear(); }
	 
	 void deposit(float amount)
	 { wallet.deposit(amount); }
	
	 void withdraw(float amount)
	 { wallet.withdraw(amount); }

	 void addHand( BJHand hand )
	 { hands.add(hand); }

	 void takeInsurance()
	 { insurance=true; }
	
	 //Accessors
	 String getName()
	 { return name; }
	
	 float getBalance()
	 { return wallet.getBalance(); }
	
	 boolean tookInsurance()
	 { return insurance; }
	
	 float getRebet()
	 { return rebet; }
	 
	 float getInitBet()
	 { return hands.get(0).getBet().getValue(); }
	
	 boolean getPlaying()
	 { return playing; }
	
	 byte howManyHands()
	 { return (byte) hands.size(); }
	
	 ArrayList<BJHand> getHands()
	 { return hands; }
	
	 BJHand getHand(byte b)
	 { return hands.get(b); }                                 
}