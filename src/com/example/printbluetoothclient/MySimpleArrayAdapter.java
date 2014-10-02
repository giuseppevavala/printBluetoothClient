package com.example.printbluetoothclient;


import my.util.MyBluetooth;
import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

public class MySimpleArrayAdapter extends ArrayAdapter<String> {
  private final Context context;
  private final String[] values;

  public MySimpleArrayAdapter(Context context, String[] values) {
    super(context, R.layout.rowlayout, values);
    this.context = context;
    this.values = values;
  }

  @SuppressLint("ViewHolder")
  @Override
  public View getView(final int position, View convertView, ViewGroup parent) {
    LayoutInflater inflater = (LayoutInflater) context
        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View rowView = inflater.inflate(R.layout.rowlayout, parent, false);
    TextView text = (TextView)rowView.findViewById(R.id.textId);
    text.setText(values[position]);
    
    Button printBtn = (Button)rowView.findViewById(R.id.btnPrint);
    printBtn.setOnClickListener(new OnClickListener() {
		@Override
		public void onClick(View v) {
			try {
				(new MyBluetooth(getContext())).sendToServer(values[position]);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	});
    
    return rowView;
  }
} 