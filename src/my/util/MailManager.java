package my.util;

import android.annotation.SuppressLint;
import android.content.Context;

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
		int ris = 0;
		
		if (mails.containsValue(text))
			ris = -1;
		else
		{
			if (ind == -1)
			{
				mails.put(count.getAndIncrement() , text);
			}
			else
			{
				if (mails.get(ind) == null){
					count.getAndIncrement();
					mails.put(ind, text);
				}
				else
					ris = -1;
			}
		}
		return ris;
	}
	
	public String getMail(int ind){
		return mails.get(ind);
	}
	
	public String[] getMails (){
		return mails.values().toArray(new String[]{});
	}
	
	public void print(int ind, Context context){
		try {
			(new MyBluetooth(context)).sendToServer(mails.get(ind));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	
	public void clear(){
		count = new AtomicInteger(0);
		mails.clear();
	}
}


