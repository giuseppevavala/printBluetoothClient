package my.util;

import android.annotation.SuppressLint;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class MailManager {
	private static MailManager istance = null;
	@SuppressLint("UseSparseArrays")
	private HashMap<Integer, String> mails = new HashMap<Integer, String>();
	private AtomicInteger count = new AtomicInteger(0);
	
	private MailManager(){
		
	}
	
	public static MailManager getIstance(){
		if (istance == null)
			istance = new MailManager();
		return istance;
	}
	
	public int addMail(int ind, String text){
		if (ind == -1)
		{
			mails.put(count.getAndIncrement() , text);
			return 0;
		}
		
		if (mails.get(ind) == null){
			count.getAndIncrement();
			mails.put(ind, text);
			return 0;
		}
		else
			return 1;
			
	}
	
	public String getMail(int ind){
		return mails.get(ind);
	}
	
	public String[] getMails (){
		return mails.values().toArray(new String[]{});
	}
}
