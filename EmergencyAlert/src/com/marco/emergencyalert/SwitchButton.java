package com.marco.emergencyalert;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SwitchButton extends RelativeLayout implements
		View.OnClickListener {

	private OnCheckedChangeListener onCheckedChangeListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChange(boolean isChecked) {
			// TODO Auto-generated method stub

		}
	};

	public void setOnCheckedChangeListener(
			OnCheckedChangeListener onCheckedChangeListener) {
		this.onCheckedChangeListener = onCheckedChangeListener;
	}

	private String onOffString;
	private String onString = "on";
	private String offString = "off";
	private boolean switchStatues = false;

	private RelativeLayout switchLayout;
	private TextView switchOnBgTextView;
	private TextView switchOffBgTextView;
	private TextView switchOnButtonTextView;
	private TextView switchOffButtonTextView;

	public SwitchButton(Context context) {
		super(context);
	}

	public SwitchButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray typedArray = context.obtainStyledAttributes(attrs,
				R.styleable.SwitchButton);
		onOffString = typedArray.getString(R.styleable.SwitchButton_onOff);
		if (onOffString != null && (!"".equals(onOffString))) {
			String[] contentStr = onOffString.split(";");
			if (contentStr.length >= 2) {
				onString = "".equals(contentStr[0]) ? "on" : contentStr[0];
				offString = "".equals(contentStr[1]) ? "off" : contentStr[1];
			} else if (contentStr.length == 1) {
				onString = "".equals(contentStr[0]) ? "on" : contentStr[0];
				offString = "off";
			} else {
				onString = "on";
				offString = "off";
			}
		} else {
			onString = "on";
			offString = "off";
		}
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.view_switchbutton, this);
		switchLayout = (RelativeLayout) findViewById(R.id.switch_layout);
		switchOnBgTextView = (TextView) findViewById(R.id.switch_on_bg_textview);
		switchOffBgTextView = (TextView) findViewById(R.id.switch_off_bg_textview);
		switchOnButtonTextView = (TextView) findViewById(R.id.switch_on_button_textview);
		switchOffButtonTextView = (TextView) findViewById(R.id.switch_off_button_textview);
		switchOnButtonTextView.setTextColor(Color.WHITE);
		switchOnButtonTextView.setText(onString);
		switchOffButtonTextView.setTextColor(Color.GRAY);
		switchOffButtonTextView.setText(offString);
		switchLayout.setOnClickListener(this);
		setView();
	}

	private void setView() {
		if (switchStatues) {
			switchOnBgTextView.setVisibility(View.VISIBLE);
			switchOnButtonTextView.setVisibility(View.VISIBLE);
			switchOffBgTextView.setVisibility(View.GONE);
			switchOffButtonTextView.setVisibility(View.GONE);
		} else {
			switchOnBgTextView.setVisibility(View.GONE);
			switchOnButtonTextView.setVisibility(View.GONE);
			switchOffBgTextView.setVisibility(View.VISIBLE);
			switchOffButtonTextView.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.switch_layout:
			switchStatues = !switchStatues;
			setView();
			onCheckedChangeListener.onCheckedChange(switchStatues);
			break;

		default:
			break;
		}
	}

	public boolean getSwitchStatues() {
		return switchStatues;
	}
	
	public void changbuttonstatues(boolean statues){
		if(statues==true){
			switchStatues=true;
			setView();
		}else{
			switchStatues=false;
			setView();
		}
	}

	public interface OnCheckedChangeListener {

		public void onCheckedChange(boolean isChecked);
	}
}
