package quickchip.blackjack;

public class Wallet
{
	private float balance;
	
	public Wallet()
	{ balance=0f; }
	
	public Wallet(float initCurrency)
	{ this.balance=initCurrency; }
	
	public void deposit(float amount) 
	{ balance+=amount; }
	
	public void withdraw(float amount)
	{ balance-=amount; }
	
	//Accessors
	public float getBalance()
	{ return balance; }
}