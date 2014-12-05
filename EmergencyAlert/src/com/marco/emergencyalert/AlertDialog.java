package com.marco.emergencyalert;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.SeekBar;

public class AlertDialog extends Activity{
	private SeekBar countdownprogressbar;
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alertdialog);
        countdownprogressbar=(SeekBar)findViewById(R.id.countdownseekbar);
        countdownprogressbar.setEnabled(false);
        countdownprogressbar.setProgress(50);
	}  

}
