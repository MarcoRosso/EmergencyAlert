package com.marco.emergencyalert;




import java.io.IOException;


import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;

import android.widget.Button;

import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class AlertDialog extends Activity{
	private SeekBar countdownprogressbar;
	private Vibrator vibrator=null;
	private TextView gpsinfo;
	private TextView alerttype;
	private TextView countdown;
	private TextView addressshow;
	private Button confirm;
	private Button cancel;
	private String latitude;
	private String longitude;
	private String address;
	private MediaPlayer mMediaPlayer;
	private boolean stopThread=false;
	private int status = -10;	
	private int type;

	private Handler mHandler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			if (msg.what == 0x111&&!stopThread)
			{
				countdownprogressbar.setProgress(status);
				countdown.setText(10-(status/10)+"秒");
		        if(status==100)	{			  
					Toast.makeText(AlertDialog.this,   
							"报警启动",   
			                    Toast.LENGTH_SHORT).show(); 
				    Intent intent  = new Intent();
				    intent.putExtra("type", type);
			    	intent.putExtra("latitude", latitude);
			    	intent.putExtra("longitude", longitude);
			    	intent.setClass(AlertDialog.this,AlertAction.class);
			    	startActivity(intent);
		        	finish();
		        }
		    }
		}
	};	
	private Runnable mRunnable = new Runnable() {	           
		         public void run() {	             
		             while (!stopThread)
		             {
		 				while (status < 100)
						{
							status = doWork();
							mHandler.sendEmptyMessage(0x111);
						}
		             }
		         }  
		     };

	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alertdialog);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        Intent intent = getIntent(); 
        type = intent.getIntExtra("type", 2);
        latitude = intent.getStringExtra("latitude");
        longitude=intent.getStringExtra("longitude");
		address=intent.getStringExtra("address");
		 
		vibrator=(Vibrator)getSystemService(Service.VIBRATOR_SERVICE);
        mMediaPlayer=new MediaPlayer();
        mMediaPlayer=MediaPlayer.create(this, R.raw.alertbeep);
        if (!mMediaPlayer.isPlaying())
        {
    	try {
			mMediaPlayer.prepare();
		} catch (IllegalStateException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
        mMediaPlayer.setLooping(true);
        mMediaPlayer.start();
        }
        countdownprogressbar=(SeekBar)findViewById(R.id.countdownseekbar);
        gpsinfo=(TextView)findViewById(R.id.gpsinfo);
        alerttype=(TextView)findViewById(R.id.alerttype);
        countdown=(TextView)findViewById(R.id.countdown);
        addressshow=(TextView)findViewById(R.id.addressinfo);
        confirm=(Button)findViewById(R.id.confirm);
        cancel=(Button)findViewById(R.id.cancel);
        
        vibrator.vibrate(new long[]{0,500,300}, 1);
        switch(type){
        case 0:alerttype.setText("火灾！"); break;
        case 1:alerttype.setText("跌落！"); break;
        case 2:alerttype.setText("撞击！"); break;
        }
        gpsinfo.setText(latitude+"\n"+longitude);
        addressshow.setText(address);
        countdownprogressbar.setEnabled(false);
        
        cancel.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				Toast.makeText(AlertDialog.this,   
						"报警取消",   
		                    Toast.LENGTH_SHORT).show(); 
				finish();
			}
        });
        confirm.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
		    Intent intent  = new Intent();
		    intent.putExtra("type", type);
	    	intent.putExtra("latitude", latitude);
	    	intent.putExtra("longitude", longitude);
	    	intent.putExtra("address", address);
	    	intent.setClass(AlertDialog.this,AlertAction.class);
	    	startActivity(intent);
        	finish();
			}      	
        });
        new Thread(mRunnable).start();
	}
	  protected void onDestroy() {
		 stopThread=true;
		 vibrator.cancel();
		 super.onDestroy();
  	     if (mMediaPlayer.isPlaying())
 		     mMediaPlayer.release();
		      };  
	public int doWork()
	{	
		status=status+10;
		try
		{
			Thread.sleep(1000);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		return status;
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if(keyCode == KeyEvent.KEYCODE_BACK) {
	    } else if(keyCode == KeyEvent.KEYCODE_MENU) {
	    } else if(keyCode == KeyEvent.KEYCODE_HOME) {
	    }
	    return super.onKeyDown(keyCode, event);
	}


}
