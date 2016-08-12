package quickchip.blackjack;

import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDialog;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/*
 * I'm God.
 */

/*TODO
 * Pause in between dealCard()s in act()
 * Show amount in pot
 *  
 */

public class Dealer extends AppCompatActivity implements OnClickListener
{
	private static final int INSURANCE_YES_NO_MESSAGE = 1;
	
	private Vibrator vibrator;
	private MediaPlayer shuffleSound;
	private Button playAgainButton;
	
	private GameSettings settings; 
	private Deck shoe; 
	private Wallet pot; 
	private BJHand dealerHand; 
	private TextView dealerSum;
	private TextView dealerCash;
	private Card dealerFaceDownCard;
	
	private Player player; 
	private BJHand currentPlayerHand;
	private LinearLayout currentPlayerHandView;
	private boolean betting, getNextAction;
	
	private LinearLayout dealerHandView;
	
	private LinearLayout playerHandView1;
	private ArrayList<Integer> splitHands;
	private LinearLayout playerHandView2;
	private LinearLayout playerHandView3;
	
	private TextView playerBet;
	private TextView playerSum;
	private TextView playerCash;
	private TextView playerResult;

	private Map<Integer, Action> buttons;
	private Map<BJHand, LinearLayout> hands;
	
	private byte insuranceAfterBlackjack; 
	
	@Override
    protected AppCompatDialog onCreateDialog(int id)
	{
		switch (id)
		{
			case INSURANCE_YES_NO_MESSAGE:
			{
				return new AlertDialog.Builder(Dealer.this)
				.setTitle("Would you like INSURANCE?")
				.setPositiveButton("Yes", new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int whichButton)
					{
						vibrator.vibrate(100);
						insurance(currentPlayerHand);
						boobie();
					}
				})
				.setNegativeButton("No", new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int whichButton)
					{
						vibrator.vibrate(100);
						dialog.cancel();
						boobie();
					}
				})
				.create();
			}
		}

		return null;
    }

	@Override
	public void onCreate(Bundle savedInstanceState)
	{    
		super.onCreate(savedInstanceState);
		
		settings = new GameSettings();
		pot = new Wallet(settings.startCash);	
		shoe = new Deck( (byte)settings.decks, 
						 (byte)settings.burns, 
						 (byte)settings.shuffleTimes 
					   ); 
		dealerHand = new BJHand("Dealer");
			
		player = new Player("Richard",1000f);
		currentPlayerHand = player.getHand((byte)0);
		betting = true;
		
		shuffleSound = MediaPlayer.create(getBaseContext(), R.raw.shuffle1);
		
		initUI(); 
	}
	
	private void initUI()
	{
		buttons = new HashMap<Integer, Action>();
		 
		if (betting)
		{
			setContentView(R.layout.betting);
			 
			buttons.put(R.id.clearButton, Action.CLEAR);
			buttons.put(R.id.dealButton, Action.DEAL);
			buttons.put(R.id.rebetButton, Action.REBET);
			buttons.put(R.id.chip5, Action.FIVE);
			buttons.put(R.id.chip25, Action.TWENTYFIVE);
			buttons.put(R.id.chip100, Action.ONEHUNDRED);
			buttons.put(R.id.chip500, Action.FIVEHUNDRED);

			playerBet=(TextView) findViewById(R.id.playerBet);
			updatePlayerBetlbl();
		}
		else
		{ 
			setContentView(R.layout.playing);
			
			getNextAction=true;
			
			insuranceAfterBlackjack = -1;
			
			buttons.put(R.id.surrenderButton, Action.SURRENDER);
			buttons.put(R.id.standButton, Action.STAND);
			buttons.put(R.id.hitButton, Action.HIT);
			buttons.put(R.id.ddButton, Action.DOUBLEDOWN);
			buttons.put(R.id.splitButton, Action.SPLIT);
			buttons.put(R.id.playAgainButton, Action.PLAYAGAIN);
			
			playAgainButton=(Button) findViewById(R.id.playAgainButton);
			
			dealerHandView = (LinearLayout) findViewById(R.id.dealerHand);
			
			playerHandView1 = (LinearLayout) findViewById(R.id.playerHand1);
			playerHandView2 = (LinearLayout) findViewById(R.id.playerHand2);
			playerHandView3 = (LinearLayout) findViewById(R.id.playerHand3);
			
			splitHands=new ArrayList<Integer>();
			splitHands.add(2);
			splitHands.add(3);
			
			hands=new HashMap<BJHand, LinearLayout>();
			
			hands.put(player.getHand((byte)0),playerHandView1);
			hands.put(dealerHand,dealerHandView);
			
			currentPlayerHandView=playerHandView1;
			
			playerResult=(TextView) findViewById(R.id.playerResult);
			
			playerSum=(TextView) currentPlayerHandView.getChildAt(1);
			dealerSum=(TextView) dealerHandView.getChildAt(1);
		 }	
		 	
		vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
		
		playerCash=(TextView)(((LinearLayout)findViewById(R.id.playerCash)).getChildAt(1));
		dealerCash=(TextView)(((LinearLayout)findViewById(R.id.dealerCash)).getChildAt(1));
		
		updatePlayerCashlbl();
		updateDealerCashlbl();
		
		for (final Integer entry : buttons.keySet())		 ((Button) findViewById(entry)).setOnClickListener(this);    
	}
	 
	void payTransaction ( float betValue, float ratio )
	{
		pot.withdraw(betValue); 
		player.deposit(betValue); 
		
		//calculate reward based on hand's performance in this bj round (indicated by ratio)
		float reward = betValue * ratio; 
		
		pot.withdraw(reward); 
		player.deposit(reward); 
		
		updatePlayerCashlbl();
		updateDealerCashlbl();
	}
	
	void setPlaying(BJHand hand, boolean b)
	{ hand.setPlaying(b); }
	
	void payout ()
	{
		byte dealerHardValue=dealerHand.getHardBJValue(); // the dealer's hand's hard value
		
		for ( BJHand hand: player.getHands() )
		{	
			if ( !hand.toBePayed() )	continue; //if the hand still needs to be payed
				
			byte handHardValue = hand.getHardBJValue(); //the hand's hard value
			float betValue = hand.getBet().getValue(); //the hand's bet
				
			if ( dealerHand.didBust() || handHardValue > dealerHardValue )
			{
				if (hand.hasBJ())		payTransaction(betValue, settings.bjPay);
				else	payTransaction(betValue, settings.winPay); 
					
				hand.setStatus(Status.WON);
			}	
				
			else if(handHardValue < dealerHardValue)	hand.setStatus(Status.LOST);
				
			else
			{
				payTransaction(betValue, 0f); /* if the hands tie in value, the player simply get his money back */
				hand.setStatus(Status.PUSH);
			}	
			
			updatePlayerResultlbl();	
		}
		
		initPlayAgainButton();
	}
	
	void initPlayAgainButton()
	{
		playAgainButton.setVisibility(ImageView.VISIBLE);
		playAgainButton.setOnClickListener(this);
	}
	
	void newGame()
	{
		dealerHand.update();
		player.update();
		currentPlayerHand=player.getHand((byte)0);
		betting=true;
		getNextAction=false;
		initUI();
	}
		
	void clearBet()
	{
		BJHand hand = player.getHand((byte)0);
		float betValue=hand.getBet().getValue();
		hand.clearBet(); //remove the bet from the table
		pot.withdraw(betValue); //take bet out of dealer's wallet
		player.deposit(betValue); //put bet back into player's wallet 
	}
	
	void collectBet(float stake)
	{		
		player.getHand((byte)0).incrementBet(stake);
		
		player.withdraw(stake); //take the bet out of player's wallet
		pot.deposit(stake); // put player's bet into dealer's wallet
	}
	
	void checkPlayerHand(BJHand hand)
	{
		hand.checkIfBusted();
		hand.checkIfHasBJ();

		if(hand.didBust()) // bust animation
		{
			hand.setToBePayed(false);
			hand.setPlaying(false);
			hand.setStatus(Status.LOST);
			updatePlayerResultlbl();
		} 
		else if(hand.hasBJ())	hand.setPlaying(false);			
	}

	void dd(BJHand hand)
	{
		if (hand.getCardCount()==2)
		{
			//check 10/11
			byte handHardBJValue = hand.getHardBJValue(); 
			
			if (handHardBJValue == 10 || handHardBJValue==11)
				if (!settings.dd1011)
					return;
			
			hand.takeDD();
			
			float betValue = hand.getBet().getValue();
			
			hand.incrementBet( betValue );
			
			player.withdraw(betValue);
			pot.deposit(betValue);
			
			updatePlayerCashlbl();
			updateDealerCashlbl();
			
			dealCard(hand, true);
			
			hand.setPlaying(false);
			
			checkPlayerHand(hand);
		}
	}
	
	void surrender(BJHand hand)
	{
		if (hand.getCardCount()==2)
		{
			float betValue = hand.getBet().getValue();
			hand.update(); // clear hand
			payTransaction(betValue, settings.surrenderPay ); //player gets half his bet back via negative ratio
			hand.setStatus(Status.SURRENDERED);
			hand.setToBePayed(false);
			hand.setPlaying(false);
			
			updatePlayerResultlbl();
		}
	}
	
	void split(BJHand hand)
	{
		if (hand.splitable(settings.aceResplit) && player.howManyHands()<(settings.splits+1)) 
		{ 
			//remove the second card from the first hand and make a new hand with it
			float betValue=hand.getBet().getValue();
			Card poppedCard = hand.popCard((byte)1);
			((LinearLayout)(currentPlayerHandView.getChildAt(0))).removeViewAt(1);
			
			BJHand splitHand = new BJHand(hand.ownerName, poppedCard, betValue);
			player.addHand(splitHand);
			
			if (splitHands.get(0)==2)    hands.put(splitHand, playerHandView2);
			else	hands.put(splitHand, playerHandView3);
			
	    	ImageView cardImage=new ImageView(this);
	    	cardImage.setImageResource(poppedCard.getImage());
	    	((LinearLayout)(hands.get(splitHand).getChildAt(0))).addView(cardImage);
			
			dealCard(hand, true);
			checkPlayerHand(hand);
			dealCard(splitHand, true);
			checkPlayerHand(splitHand);
			
			currentPlayerHand=player.getHand((byte)(player.howManyHands()-1));
			
			playerSum=(TextView) hands.get(splitHand).getChildAt(1);
	    	updatePlayerSumlbl();
	    	
	    	currentPlayerHand=player.getHand((byte)(player.howManyHands()-2));
	    	
	    	playerSum=(TextView) currentPlayerHandView.getChildAt(1);
	    	updatePlayerSumlbl();
	    	
	    	splitHands.remove(0);
		}
	}
	
	void insurance(BJHand hand)
	{	
		player.takeInsurance(); //kill insurance button
				
		float betValue=hand.getBet().getValue();
		float insuranceBetValue = (float)(0.5*betValue)+betValue; //insurance is a separate bet from the main bet 	
				
		player.withdraw(insuranceBetValue); //withraw the bet from player's wallet
		pot.deposit(insuranceBetValue); //put in dealers wallet
			
		//reveal dealer's second card
		dealCard(dealerHand,dealerFaceDownCard);
		
		dealerHand.checkIfHasBJ();
				
		if (dealerHand.hasBJ())
		{
			payTransaction(insuranceBetValue, settings.insurancePay);
			hand.setStatus(Status.INSURANCEWON);
		}
		else	hand.setStatus(Status.INSURANCELOST);
		
		updatePlayerCashlbl();
		updateDealerCashlbl();
		updatePlayerResultlbl();
	}
	
	void disableButtons()
	{	
		for (final Integer entry : buttons.keySet())
		{
			((Button) findViewById(entry)).setVisibility(ImageView.GONE);
			((Button) findViewById(entry)).setOnClickListener(null);
		}
	}
	
	void act()
	{
		disableButtons();
		
		//if the player took insurance, the down card is already up
		if (!player.tookInsurance())	dealCard(dealerHand,dealerFaceDownCard);
		
		if (settings.stand17soft)
			while ( dealerHand.getHardBJValue() < 17)
			{
				if (dealerHand.getSoftBJValue() == 17)    break;    // stand on soft 17
				dealCard(dealerHand, true);	
			}	

		else
			while ( dealerHand.getHardBJValue() < 17 )
				dealCard(dealerHand, true);
		
		dealerHand.checkIfHasBJ();
		dealerHand.checkIfBusted();
		
		updateDealerSumlbl();
		
		payout();
	}
	
	void firstDeal()
	{
		//BJHand hand=player.getHand((byte)0); //the player only has one hand at the beginning of the round
		dealCard(currentPlayerHand, true); 
		dealCard(currentPlayerHand, true);	
			
		currentPlayerHand.checkIfHasBJ();
		
		if (currentPlayerHand.hasBJ())    insuranceAfterBlackjack = 0;
		
		updatePlayerSumlbl();
		
		dealCard(dealerHand, true);	// FACE-UP
		dealCard(dealerHand, false); // FACE DOWN
		
		//offer insurance
		if ( settings.insurance && dealerHand.getCard((byte)0).getValue()==1 )    showDialog(INSURANCE_YES_NO_MESSAGE);
		
		else
			boobie();
	}
	
	void boobie()
	{
		updateDealerSumlbl();
		
		if (currentPlayerHand.hasBJ())
		{
			currentPlayerHand.setPlaying(false);
			act();
		}
	}

	void dealCard(BJHand hand, boolean faceUp)
	{	
		if (shoe.cardsLeft()==0)
		{
			shoe.fillDeck();
			shoe.shuffleDeck(); 
			shuffleSound.start();
		}
			
	    Card card=shoe.drawCard();
	    
	    if (!faceUp)
	    {
	    	card.flip(); 
	    	dealerFaceDownCard=card;	
	    }
	    
	    else	hand.addCard(card);
	    	
	    ImageView cardImage=new ImageView(this);
	    cardImage.setImageResource(card.getImage());
        LinearLayout handView=(LinearLayout)(hands.get(hand).getChildAt(0));
    	handView.addView(cardImage);
	}
	
	void dealCard(BJHand hand, Card card)
	{
		LinearLayout handView=(LinearLayout)(hands.get(hand).getChildAt(0));
		handView.removeViewAt(1);
		
		card.flip();
		
		ImageView cardImage=new ImageView(this);
    	cardImage.setImageResource(card.getImage());    	
    	handView.addView(cardImage);
    	
    	hand.addCard(card);
	}

    void updatePlayerSumlbl()
    { 
    	byte soft = currentPlayerHand.getSoftBJValue();
    	String softString = "";
    	
    	if (soft>0)    softString = "/"+ String.valueOf(soft);    
    		
    	playerSum.setText ( String.valueOf( currentPlayerHand.getHardBJValue() )  + softString );
    }
    
    void updateDealerSumlbl()
    { 
	   	//dealerHand.calculateBJValue();
    	byte soft = dealerHand.getSoftBJValue();
    	
    	String softString = "";
    	
	   	if (soft > 0)    softString = "/"+ String.valueOf(soft);
	   		
	   	dealerSum.setText ( String.valueOf( dealerHand.getHardBJValue() )  + softString );
	}
	
    void updatePlayerBetlbl()
    { playerBet.setText( String.valueOf( player.getInitBet() ) ); }
    
 	void updatePlayerCashlbl()
    { playerCash.setText( String.valueOf( player.getBalance() ) ); }

 	void updateDealerCashlbl()
    { dealerCash.setText( String.valueOf( pot.getBalance() ) ); }
 	
    void updatePlayerResultlbl()
    {    	
   		ArrayList<String> result=new ArrayList<String>();
    	
   		
   		for ( BJHand h: player.getHands() )
    	{	
    		switch (h.getStatus())
    		{
    			case PLAYING:	break;
    		
    			case SURRENDERED:
    			{
    				result.add("You SURRENDERED!");
    				break;
    			}
    			case WON:
    			{
    				result.add("You WON!");
    				break;
    			}
    			case PUSH:
    			{
    				result.add("IT'S A PUSH!");	
    				break;
    			}
    			case LOST:
    			{	
    				result.add("You LOST!");
    				break;
    			}
    			case INSURANCEWON:
    			{
    				result.add("You WON your\nINSURANCE bet!");
    				
    				if (insuranceAfterBlackjack == 0)
    					insuranceAfterBlackjack = 1;
    				
    				break;
    			}
    			case INSURANCELOST:
    			{
    				result.add("You LOST your\nINSURANCE bet!");
    				
    				if (insuranceAfterBlackjack == 0)
    					insuranceAfterBlackjack = 2;
    				
    				break;
    			}
    		}
    	}
    	
   		String sum="";
    	
   		for (String s: result)	sum += s + "\n";
    	
    	if (insuranceAfterBlackjack == 1)
			sum += "You WON your\nINSURANCE bet!";
    	
    	else if (insuranceAfterBlackjack == 2)
    		sum += "You LOST your\nINSURANCE bet!";
    	
    	playerResult.setText(sum);
    }
    
	public void onClick(final View view)
    {
       	final Action action = buttons.get(view.getId());
        
        if (betting)
        {
        	switch (action) 
        	{
        		case DEAL:
        		{
        			if (player.getInitBet()>=settings.tableMin && player.getInitBet()<=settings.tableMax)
        			{
        				betting=false;
        				initUI();
        				firstDeal();
        			}
        			break;
        		}
        		case CLEAR:
        		{
	        		clearBet();
        			break;
        		}
        		case REBET:
        		{
        			collectBet(player.getRebet());
        			break;
        		}
        		case FIVE:
        		{
	        		collectBet(5f);
        			break;
        		}
        		case TWENTYFIVE:
	        	{
        			collectBet(25f);
        			break;
        		}
	        	case ONEHUNDRED:
        		{
        			collectBet(100f);
        			break;
	        	}
        		case FIVEHUNDRED:
        		{
        			collectBet(500f);
        			break;
	        	}
        	}
	        
        	vibrator.vibrate(100);
        	updatePlayerBetlbl();
        	updatePlayerCashlbl();
        	updateDealerCashlbl();
        }
        
        else
        {
        	switch (action)
        	{
    			case HIT:
    			{
    				dealCard(currentPlayerHand,true);
    				updatePlayerSumlbl();
    				checkPlayerHand(currentPlayerHand);
    				break;
    			}
    			case STAND:
    			{	
    				currentPlayerHand.setPlaying(false);
	    			break;
    			}
    			case DOUBLEDOWN:
    			{
    				dd(currentPlayerHand);
    				updatePlayerSumlbl();
    				break;
    			}
    			case SURRENDER:
    			{ 
    				surrender(currentPlayerHand);
    				break;
    			}	
    			case SPLIT:
    			{ 
    				split(currentPlayerHand); 
    				break; 
    			}
    			case PLAYAGAIN:
    			{
    				newGame();
    				break;
    			}	
        	}
        	
        	vibrator.vibrate(100);
        	if (getNextAction)	getNextAction();
       	} 
    }
    
	void getNextAction()
    {
    	byte nextHandtoPlay=(byte)-1;
    	
    	for ( byte b=0 ; b < player.howManyHands() ; b++ )
    		if (player.getHand(b).isPlaying())
    		{
    			nextHandtoPlay=b;
    			break;
    		}
    	
		if (nextHandtoPlay == -1)
    	{
    		byte nextHandtoPay=(byte)-1;
    		
    		for ( byte b=0 ; b < player.howManyHands() ; b++ )
        		if (player.getHand(b).toBePayed())
        		{
        			nextHandtoPay=b;
        			break;
        		}
    	
    		if (nextHandtoPay == -1)
    		{
	    		disableButtons();
    			initPlayAgainButton();	
	    	}
    		
    		else	act();	
    	}
    	
    	else
    	{
    		currentPlayerHand=player.getHand(nextHandtoPlay);
			currentPlayerHandView=hands.get(currentPlayerHand);	
			playerSum=(TextView) currentPlayerHandView.getChildAt(1);
    	}
    }	
}