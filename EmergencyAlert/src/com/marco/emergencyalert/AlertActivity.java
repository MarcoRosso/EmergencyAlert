package com.marco.emergencyalert;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;

public class AlertActivity extends Activity{
	private Button alertbottom;
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alertlayout);
	}
}
