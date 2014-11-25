package com.marco.emergencyalert;






import java.io.IOException;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;



public class MainActivity extends Activity implements LocationListener {
	  private SensorManager sensorManager;
	  private LocationManager locationManager;
	  private TextView temperatureTextView;
	  private TextView altitudeTextView;
	  private TextView accTextView;
	  private TextView latitudeTextView;
	  private TextView longitudeTextView;
	  private ToggleButton togglebutton;
  	  private ProgressDialog pd1;
	  private float currentTemperature ;
	  private long exitTime = 0;
	  private ImageView temperaturealert;
	  private ImageView altitudealert;
	  private ImageView accalert;
	  private float currentAltitude;
	  private float pastAltitude=0;
	  private float[] gravity={0,0,0};
	  private MediaPlayer mMediaPlayer;
	  private boolean sendmsg;
	  private boolean isshow=false;
	  private boolean cancelbyueser=false;
	  private boolean soundenable;
	  private boolean runatfirsttime=true;
	  private String contactnumber= null;
  	  private String alertmsg = null;
  	  SharedPreferences preferences;
  	  SharedPreferences.Editor editor;
	  float pasttem[] = new float[16];
	  float accvalues[]=new float[3];
	  private MyReceiver receiver=null;
	  private static final float maxtemperature=50;
	  private static final float maxaltitude=(float) 1.1;
	  private static final float maxacc=(float)8.0;
	  private static final float ALPHA = 0.8f;
	  int i=0;
	  int j=0;
	  int acccounter=0;
	  int temalertcounter;
	  int type;
  	  int progressStatus = 0;
 
    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
         Bundle bundle=intent.getExtras();
         type =bundle.getInt("type");
         alert(type);
         String latitude=bundle.getString("latitude");
         String longtitude=bundle.getString("longititude");
         latitudeTextView.setText(latitude);
 		 longitudeTextView.setText(longtitude);
        }
      }
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        setContentView(R.layout.activity_main);
        temperatureTextView = (TextView)findViewById(R.id.temperaturenow);
        latitudeTextView=(TextView)findViewById(R.id.Latitude);
        longitudeTextView=(TextView)findViewById(R.id.longitude);
        temperaturealert=(ImageView)findViewById(R.id.temperaturealert);
        altitudealert=(ImageView)findViewById(R.id.altitudealert);
        accalert=(ImageView)findViewById(R.id.accalert);
        altitudeTextView=(TextView)findViewById(R.id.altitudechanged);
        accTextView=(TextView)findViewById(R.id.accnow);
        togglebutton = (ToggleButton)findViewById(R.id.button1);
        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        temperaturealert.setVisibility(View.INVISIBLE);
        altitudealert.setVisibility(View.INVISIBLE);
        accalert.setVisibility(View.INVISIBLE);
        Timer updateTimer = new Timer("UpdateTemperature");
        updateTimer.scheduleAtFixedRate(new TimerTask() {
          public void run() {  	  
        	updatetemperature();      
          }
        }, 0, 1000);
        Timer updateTimer1 = new Timer("updatealtitude");
        updateTimer1.scheduleAtFixedRate(new TimerTask() {
          public void run() {  	  
        	updatealtitude();      
          }
        }, 0, 500);
        Timer updateTimer2 = new Timer("updateaccelerator");
        updateTimer2.scheduleAtFixedRate(new TimerTask() {
          public void run() {  	  
        	updateaccelerator();      
          }
        }, 0, 100);
        mMediaPlayer=new MediaPlayer();
        mMediaPlayer=MediaPlayer.create(this, R.raw.alertbeep);
        receiver=new MyReceiver();
        IntentFilter filter=new IntentFilter();
        filter.addAction("com.marco.AlertService");
        MainActivity.this.registerReceiver(receiver,filter);
    	preferences = getSharedPreferences("sendmsgsaved", MODE_PRIVATE);
		sendmsg = preferences.getBoolean("sendmsgsaved",false);
		togglebutton.setChecked(sendmsg);
    	preferences = getSharedPreferences("savecontactnumber", MODE_PRIVATE);
    	contactnumber = preferences.getString("savecontactnumber",null);
    } 
	public void onUserInteraction (){
        if(mMediaPlayer.isPlaying())     
        	mMediaPlayer.pause();
	}
	private boolean isMyServiceRunning() {
	    ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if ("com.marco.AlertService".equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}
    private final SensorEventListener tempSensorEventListener = new SensorEventListener() {
        public void onAccuracyChanged(Sensor sensor, int accuracy) { }
        public void onSensorChanged(SensorEvent event) {
          currentTemperature = event.values[0];
        }
    };
    private final SensorEventListener altiSensorEventListener = new SensorEventListener() {
	    public void onAccuracyChanged(Sensor sensor, int accuracy) { }
	    public void onSensorChanged(SensorEvent event) {
	    	currentAltitude = event.values[0];    	
	 }
   };
   private final SensorEventListener accSensorEventListener =new SensorEventListener(){
		public void onAccuracyChanged(Sensor sensor, int accuracy) {}
	   public void onSensorChanged(SensorEvent event) {   
		   accvalues =event.values.clone();		   
	   }	   
   };
    protected void onResume() {
        super.onResume();
        Sensor temperatureSensor =
          sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        if (temperatureSensor != null)
          sensorManager.registerListener(tempSensorEventListener,
              temperatureSensor,                                 
              SensorManager.SENSOR_DELAY_NORMAL);
        else
          temperatureTextView.setText("Unavailable");
        Sensor AltitudeiSensor=sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        if (AltitudeiSensor != null)
            sensorManager.registerListener(altiSensorEventListener,
           		 AltitudeiSensor,                                 
                SensorManager.SENSOR_DELAY_NORMAL);
          else
       	   altitudeTextView.setText("Unavailable");
        Sensor AcceleratorSensor=sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(accSensorEventListener, 
            		AcceleratorSensor,SensorManager.SENSOR_DELAY_NORMAL );
       
        List<String> enabledProviders = locationManager.getProviders(true);
        if (enabledProviders.isEmpty()
                || !enabledProviders.contains(LocationManager.GPS_PROVIDER)||
                    !enabledProviders.contains(LocationManager.NETWORK_PROVIDER))
        {
        	latitudeTextView.setText("Unavaliable");
    		longitudeTextView.setText("Unavaliable");
        }
        else
        { 
        	Criteria mCriteria01 = new Criteria();   
            mCriteria01.setAccuracy(Criteria.ACCURACY_MEDIUM);   
            mCriteria01.setAltitudeRequired(false);   
            mCriteria01.setBearingRequired(false);   
            mCriteria01.setCostAllowed(true);   
            mCriteria01.setPowerRequirement(Criteria.POWER_LOW);   
            String bestLocationProvider =    
            locationManager.getBestProvider(mCriteria01, true);
            Location pastLocation = locationManager.getLastKnownLocation   
            	      (bestLocationProvider); 
            if ((pastLocation== null))
            	{latitudeTextView.setText("Unavaliable");
                longitudeTextView.setText("Unavaliable");
            	}            	
            else
            	{latitudeTextView.setText(String.valueOf(pastLocation.getLatitude()));
                longitudeTextView.setText(String.valueOf(pastLocation.getLongitude()));
            	}
    		locationManager.requestLocationUpdates(bestLocationProvider,
                    1000,0,this,null);          
        } 
        Intent intent= new Intent();
        intent.setClass(MainActivity.this, AlertService.class);
    	stopService(intent);
    	runatfirsttime=true;
    	acccounter=0;
    }
    protected void onPause() {
        sensorManager.unregisterListener(tempSensorEventListener);
        sensorManager.unregisterListener(altiSensorEventListener);
        sensorManager.unregisterListener(accSensorEventListener);
        locationManager.removeUpdates(this);
        if(!isMyServiceRunning()&&sendmsg)
    	startService(new Intent(this, AlertService.class));
        super.onPause();
      }
    protected void onDestory(){
    	super.onDestroy();
        if(!isMyServiceRunning()&&sendmsg)
      	startService(new Intent(this, AlertService.class));
    }
    public void finish() {
        moveTaskToBack(true); 
    }   
    private float[] highPass(float x, float y, float z)
    {
        float[] filteredValues = new float[3];
        
        gravity[0] = ALPHA * gravity[0] + (1 - ALPHA) * x;
        gravity[1] = ALPHA * gravity[1] + (1 - ALPHA) * y;
        gravity[2] = ALPHA * gravity[2] + (1 - ALPHA) * z;

        filteredValues[0] = x - gravity[0];
        filteredValues[1] = y - gravity[1];
        filteredValues[2] = z - gravity[2];
        
        return filteredValues;
    }
    private void updatetemperature() {
        runOnUiThread(new Runnable() {
    	public void run() {
            if (!Float.isNaN(currentTemperature)) {
              temperatureTextView.setText(currentTemperature + "C");
              if (i==16) 
            	  i=0;
              else
              {pasttem[i]=currentTemperature;
              i++;}
          for (int j=0; j<=pasttem.length;j++);
          { if (pasttem[j]>=maxtemperature)
        	   {temalertcounter++;
               if (temalertcounter>10)
            	   {type=0; 
                   alert(type);}
        	   }       
         }}
        }       
        });
      }
    private void updatealtitude(){
    	runOnUiThread(new Runnable() {
			public void run() {
				float altitudeDifference = 0;
				if (!Float.isNaN(currentAltitude)) {
					if (pastAltitude!=0 && currentAltitude>=pastAltitude)
					{altitudeDifference=
					     SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE,pastAltitude)
					     -
					     SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE,currentAltitude);
					altitudeTextView.setText(Float.toString(altitudeDifference)+"m");
					pastAltitude=currentAltitude;
					}
					else if (pastAltitude!=0 && currentAltitude<pastAltitude)
					{altitudeDifference=
				     SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE,currentAltitude)
				     -
				     SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE,pastAltitude);
				    altitudeTextView.setText(Float.toString(altitudeDifference)+"m");
				    pastAltitude=currentAltitude;				
					} else pastAltitude=currentAltitude;
                   if (altitudeDifference>=maxaltitude)	{
                	   type=1;
                	   alert(type);             	   
                   }
				}				
			} }
    	);
    }
    public void updateaccelerator(){
         runOnUiThread(new Runnable(){
         public void run(){
        	 float[] values=accvalues;
        	 values=highPass(values[0],values[1],values[2]);
        	 double sumOfSquares=(values[0]*values[0])+(values[1]*values[1]
        			 +values[2]*values[2]);
        	 double acceleration = Math.sqrt(sumOfSquares);
        	 DecimalFormat df = new DecimalFormat("########.0000");
        	 acceleration = Double.parseDouble(df.format(acceleration));
        	 accTextView.setText(Double.toString(acceleration)+"m/s^2");
        	 if (acceleration>=maxacc&&!runatfirsttime)
        	 {
        		 type=2;
        		 alert(type);
        	 }
        	 acccounter++;
        	 if (acccounter>3)
        	 runatfirsttime=false;
         }       		  	
        });
    }
    public void alert (int typein){
        if (!mMediaPlayer.isPlaying()&&soundenable)
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
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int max = am.getStreamMaxVolume( AudioManager.STREAM_MUSIC );
        am.setStreamVolume(AudioManager.STREAM_MUSIC, max,      
                AudioManager.FLAG_PLAY_SOUND); 
        }
    	int alerttype=this.type;
    	switch(alerttype){
    	case 0: final Animation alpha=AnimationUtils.loadAnimation(this,R.anim.anim_alpha);
    		    temperaturealert.setVisibility(View.VISIBLE);
    	         temperaturealert.startAnimation(alpha);
			    alpha.setAnimationListener (new AnimationListener(){
				@Override
				public void onAnimationStart(Animation animation) {	}
				public void onAnimationEnd(Animation animation) {
					temperaturealert.setVisibility(View.INVISIBLE);
				}
				public void onAnimationRepeat(Animation animation) { }	 
    	         });
			    if (sendmsg&&!isshow) showsendmsgDialog(type);
    	        break;
    	case 1:final Animation alpha1=AnimationUtils.loadAnimation(this,R.anim.anim_alpha);
    		    altitudealert.setVisibility(View.VISIBLE);
    		    altitudealert.startAnimation(alpha1);
    		    alpha1.setAnimationListener(new AnimationListener(){
					public void onAnimationStart(Animation animation) {   }
					public void onAnimationEnd(Animation animation) {
						altitudealert.setVisibility(View.INVISIBLE);					
					}
					public void onAnimationRepeat(Animation animation) {   }  				
    		    });
    		    if (sendmsg&&!isshow) showsendmsgDialog(type);
    	       break;
    	case 2: final Animation alpha2=AnimationUtils.loadAnimation(this,R.anim.anim_alpha);
    	        accalert.setVisibility(View.VISIBLE);
    	        accalert.startAnimation(alpha2);
    	        alpha2.setAnimationListener(new AnimationListener(){
					public void onAnimationStart(Animation animation) {		}
					public void onAnimationEnd(Animation animation) {
			    accalert.setVisibility(View.INVISIBLE);	
					}
					public void onAnimationRepeat(Animation animation) {	}    	
    	        });
    	        if (sendmsg&&!isshow) showsendmsgDialog(type);
    	        break;
    	}
    }
    public void onToggleClick(View view){
    	preferences = getSharedPreferences("sendmsgsaved", MODE_PRIVATE);
    	if (((ToggleButton)view).isChecked())
    	{
    		sendmsg=true;
    		editor = preferences.edit();
			editor.putBoolean("sendmsgsaved", sendmsg);
			editor.commit();
            preferences = getSharedPreferences("savecontactnumber", MODE_PRIVATE);
            contactnumber = preferences.getString("contactnumbersaved", null);
            if (contactnumber == null||contactnumber.equals(""))
            {	Toast.makeText(this, "No ContactNumber!\n    Please Input!" , Toast.LENGTH_SHORT).show();
                 final EditText meditText = new EditText(this);
                 meditText.setInputType(InputType.TYPE_CLASS_NUMBER);
       	          new AlertDialog.Builder(this)  
       	          .setTitle("Input Emergency Number")  
       	          .setIcon(android.R.drawable.ic_dialog_info)  
       	          .setView(meditText)
       	          .setCancelable(false)
       	          .setPositiveButton("OK", new OnClickListener(){
    			public void onClick(DialogInterface dialog, int which) {
    				contactnumber=meditText.getText().toString();
    				if (contactnumber == null||contactnumber.equals(""))
    				{
    				try {
                        Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                        field.setAccessible(true);
                        field.set(dialog, false);
                        Toast.makeText(MainActivity.this, "No ContactNumber!\n    Please Input!" , Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
    				}
    				else{
    					try {
    						Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
    						field.setAccessible(true);
    						field.set(dialog, true);
    						} catch (Exception e) {
    						e.printStackTrace();
    						}    			    
    				editor = preferences.edit();
    				editor.putString("contactnumbersaved", contactnumber);
    				editor.commit();
    				}
    			}
    			})
    			  .show();;
    		}
    	}
    	else {
    		sendmsg=false;
			editor = preferences.edit();
			editor.putBoolean("sendmsgsaved", sendmsg);
			editor.commit();
    	}
    }
    public void showsendmsgDialog(int typein){
    	int alerttype=this.type;
    	switch(alerttype){
    	case 0: alertmsg="Overheat! Maybe Catch Fire in Latitude:"+latitudeTextView.getText()
    			          + "\n Longitude:"+longitudeTextView.getText(); break;
    	case 1: alertmsg="Height Changed Abruptly! Maybe Drop off in \n Lattitude:"+latitudeTextView.getText()
		          + "\n Longitude:"+longitudeTextView.getText(); break;
    	case 2: alertmsg="Intense Movement! Maybe Get Injured by Accident in \n Latitude:"+latitudeTextView.getText()
    			          + "\n Longitude:"+longitudeTextView.getText(); break;
    	}
		pd1 = new ProgressDialog(MainActivity.this);
		pd1.setMax(10);
		pd1.setCancelable(false);
		pd1.setCanceledOnTouchOutside(false);
		pd1.setTitle(R.string.sendmsg);
		pd1.setMessage(alertmsg+"\n The Message Above Will be Sent Automatically In 10s if You Don't Cancel!");
		pd1.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pd1.setIndeterminate(false);
		pd1.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which){
	  			    cancelbyueser=false;
	  			    isshow=false;
			}
		});
		pd1.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which){
	 			    isshow=false;
	 			    cancelbyueser=true;
			}
		});
        pd1.setOnDismissListener(new AlertDialog.OnDismissListener(){
			public void onDismiss(DialogInterface dialog) {
				if(!cancelbyueser)sendmessage();	
				cancelbyueser=false;
				isshow=false;
				progressStatus=0;
			}  	
        });
		pd1.show(); 
		isshow=pd1.isShowing();
		final Timer t = new Timer();
        t.schedule(new TimerTask() {
           public void run() {
         if (pd1.isShowing()&&progressStatus<10)
         { 
        	 progressStatus=progressStatus+1;
        	 pd1.setProgress(progressStatus);
         }else
         {  t.cancel();
           pd1.dismiss();
         }
         }           
            }, 0,1000);
    }
    public void sendmessage(){
		SmsManager msmsManager = SmsManager.getDefault(); 
  		if (contactnumber==null||contactnumber.equals("")){
  			Toast.makeText(this, "No ContactNumber!\nAlert Message Sent Error!" , Toast.LENGTH_SHORT).show();
  		}else{
		     try 
  			{     
  			 PendingIntent mPI = PendingIntent.getBroadcast(MainActivity.this, 0, new Intent(), 0); 
  			 msmsManager.sendTextMessage(contactnumber, null, alertmsg, mPI, null);
  			} 
  			 catch(Exception e) 
  			 {
  			   e.printStackTrace();
  		   Toast.makeText(this, "Alert Message Sent Error!" , Toast.LENGTH_SHORT).show();
  			  }
  			ContentValues values = new ContentValues();  
  			values.put("date", System.currentTimeMillis());   
  			values.put("read", 0);  
  			values.put("type", 2);  
  			values.put("address", contactnumber);  
  			values.put("body", alertmsg);  
  			getContentResolver().insert(Uri.parse("content://sms"),values);
  			Toast.makeText(MainActivity.this, "Alert Message Sent!" , Toast.LENGTH_SHORT).show();
  		} }
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
			preferences = getSharedPreferences("alertsound", MODE_PRIVATE);
			soundenable = preferences.getBoolean("alertsound", true);
	        menu.findItem(R.id.action_sound).setChecked(!soundenable);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       
        int id = item.getItemId();
        if (id == R.id.action_settings) {
        	final EditText meditText = new EditText(this);
             meditText.setInputType(InputType.TYPE_CLASS_NUMBER);
             preferences = getSharedPreferences("savecontactnumber", MODE_PRIVATE);
             contactnumber = preferences.getString("contactnumbersaved", null);
             meditText.setHint(contactnumber);
        	new AlertDialog.Builder(this)  
        	.setTitle("Change Emergency Number")  
        	.setIcon(android.R.drawable.ic_dialog_info)  
        	.setView(meditText)  
        	.setPositiveButton("OK", new OnClickListener(){
				public void onClick(DialogInterface dialog, int which) {
					contactnumber=meditText.getText().toString();
					if (contactnumber == null||contactnumber.equals(""))
    				{
    				try {
                        Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                        field.setAccessible(true);
                        field.set(dialog, false);
                        Toast.makeText(MainActivity.this, "No ContactNumber!\n    Please Input!" , Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
    				}
    				else{
    					try {
    						Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
    						field.setAccessible(true);
    						field.set(dialog, true);
    						} catch (Exception e) {
    						e.printStackTrace();
    						}
    				editor = preferences.edit();
    				editor.putString("contactnumbersaved", contactnumber);
    				editor.commit();
    				}
				}  
        	})
        	.setNegativeButton("Cancel", null)  
        	.show();
        }
       if (id == R.id.action_about) {
    	   LayoutInflater layoutInflater = LayoutInflater.from(this);
           View mySaveView = layoutInflater.inflate(R.layout.about, null);     
           Dialog alertDialog = new AlertDialog.Builder(this).
               setTitle("About Developer").
               setView(mySaveView).
               setPositiveButton("OK", null).
               create();
              alertDialog.show();
       }
       if (id == R.id.action_sound) {  
    	   if(item.isChecked()){
        	   soundenable=true;
        	   item.setChecked(!soundenable);
				editor = preferences.edit();
				editor.putBoolean("alertsound", soundenable);
				editor.commit();	
           }
           else 
        	   {soundenable=false;
        	    item.setChecked(!soundenable);
				editor = preferences.edit();
				editor.putBoolean("alertsound", soundenable);
				editor.commit();
         	   if (mMediaPlayer.isPlaying())
        		   mMediaPlayer.pause();	
        	   }

       }
        return super.onOptionsItemSelected(item);
    }
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){   
            if((System.currentTimeMillis()-exitTime) > 2000){  
                Toast.makeText(getApplicationContext(), "Press Again to Exit", Toast.LENGTH_SHORT).show();                                
                exitTime = System.currentTimeMillis();   
            } else {
               finish();
            }
            return true;   
        }
        return super.onKeyDown(keyCode, event);
    }
	@Override
	public void onLocationChanged(Location location) {
		latitudeTextView.setText(String.valueOf(location.getLatitude()));
		longitudeTextView.setText(String.valueOf(location.getLongitude()));
	}
	public void onStatusChanged(String provider, int status, Bundle extras) {

	}
	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}
}
