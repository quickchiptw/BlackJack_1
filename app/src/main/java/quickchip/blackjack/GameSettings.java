package quickchip.blackjack;

class GameSettings
{
	byte decks, splits, burns, shuffleTimes; 
	float startCash, tableMin, tableMax;
	float bjPay, surrenderPay, insurancePay, winPay;
	boolean ddPostSplit, surrender, insurance, stand17soft, aceResplit, dd1011;
	
	private boolean[] bools={true,false};
	private byte[] bytes={6,8};
	
	GameSettings()
	{
		bjPay=1.5f; surrenderPay=-0.5f; insurancePay=2f; winPay=1f;
		decks=1; splits=1; burns=(byte)(5.0*Math.random()); shuffleTimes = 3;
		startCash=10000f; tableMin=5f; tableMax=1000f;
		ddPostSplit=true; surrender=true; insurance=true; stand17soft=true; aceResplit=true; dd1011=true;
	}
	
	private boolean sometimes(boolean[] choices)
	{
		short i=(short)Math.round(Math.random());
		if (i==1){ return choices[0]; }
		else{ return choices[1]; }	
	}
	
	private byte sometimes(byte[] choices)
	{
		short s=(short)Math.round(Math.random());
		if (s==1){ return choices[0]; }
		else{ return choices[1]; }
	}
	
	void vegasStrip()
	{
		insurance=false;
		decks=4;
		stand17soft=false;
		dd1011=false;
	}
	
	void downtownVegas()
	{
		dd1011=false;
		surrender=false;
		ddPostSplit=sometimes(bools);
		aceResplit=sometimes(bools);
	}
	
	void rino()
	{	
		insurance=sometimes(bools);
		surrender=false;
		aceResplit=false;	
	}
	
	void atlanticCity()
	{
		decks=sometimes(bytes);
		stand17soft=false;
		dd1011=false;
		surrender=false;
		aceResplit=sometimes(bools);	
	}
}