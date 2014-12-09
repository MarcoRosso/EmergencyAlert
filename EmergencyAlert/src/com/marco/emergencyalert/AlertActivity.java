package com.marco.emergencyalert;


import java.text.DecimalFormat;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;



import android.app.Activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import android.os.Bundle;

import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.Button;

import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class AlertActivity extends Activity implements LocationListener {
	  private Button alertbottom;
	  private Button configbottom;
	  private SensorManager sensorManager;
	  private LocationManager locationManager;
	  private TextView temperatureTextView;
	  private TextView altitudeTextView;
	  private TextView accTextView;
	  private TextView latitudeTextView;
	  private TextView longitudeTextView;
	  private ImageView temperaturealert;
	  private ImageView altitudealert;
	  private ImageView accalert;
	  private float currentAltitude;
	  private float pastAltitude=0;
	  private float[] gravity={0,0,0};
	  private float pasttem[] = new float[16];
	  private float accvalues[]=new float[3];
	  private float currentTemperature ;
	  private long exitTime = 0;
	  private boolean accavoidshake=true;
	  private static final float maxtemperature=50;
	  private static final float maxaltitude=(float) 1.1;
	  private static final float maxacc=(float)8.0;
	  private static final float ALPHA = 0.8f;
	  private int i=0;
	  private int acccounter=0;
	  private int zerocounter=0;
	  private int temalertcounter;
      Timer updateTimer = new Timer("UpdateTemperature");
      Timer updateTimer1 = new Timer("updatealtitude");
      Timer updateTimer2 = new Timer("updateaccelerator");
	
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alertlayout);
        alertbottom=(Button)findViewById(R.id.alertbottom);
        configbottom=(Button)findViewById(R.id.configbottom);
        
        temperatureTextView = (TextView)findViewById(R.id.temperaturenow);
        latitudeTextView=(TextView)findViewById(R.id.Latitude);
        longitudeTextView=(TextView)findViewById(R.id.longitude);
        temperaturealert=(ImageView)findViewById(R.id.temperaturealert);
        altitudealert=(ImageView)findViewById(R.id.altitudealert);
        accalert=(ImageView)findViewById(R.id.accalert);
        altitudeTextView=(TextView)findViewById(R.id.altitudechanged);
        accTextView=(TextView)findViewById(R.id.accnow);
        
        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        
        temperaturealert.setVisibility(View.INVISIBLE);
        altitudealert.setVisibility(View.INVISIBLE);
        accalert.setVisibility(View.INVISIBLE);
        

        updateTimer.scheduleAtFixedRate(new TimerTask() {
          public void run() {  	  
        	updatetemperature();      
          }
        }, 0, 1000);

        updateTimer1.scheduleAtFixedRate(new TimerTask() {
          public void run() {  	  
        	updatealtitude();      
          }
        }, 0, 500);

        updateTimer2.scheduleAtFixedRate(new TimerTask() {
          public void run() {  	  
        	updateaccelerator();      
          }
        }, 0, 100);
        
        configbottom.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				startActivity(new Intent (AlertActivity.this, AlertSettings.class));
        		finish();	
        		 overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);  
			}      	
        });
        final Dialog dialog = new Dialog(AlertActivity.this, R.style.MyDialog);
        dialog.setContentView(R.layout.fisrtrundialog);
        dialog.show();
	}
    protected void onResume() {
        super.onResume();
        alertbottom.setPressed(true);
        alertbottom.setClickable(false);
        Sensor temperatureSensor =
          sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        if (temperatureSensor != null)
          sensorManager.registerListener(tempSensorEventListener,
              temperatureSensor,                                 
              SensorManager.SENSOR_DELAY_NORMAL);
        else
          temperatureTextView.setText("无数据或您的设备不支持");
        Sensor AltitudeiSensor=sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        if (AltitudeiSensor != null)
            sensorManager.registerListener(altiSensorEventListener,
           		 AltitudeiSensor,                                 
                SensorManager.SENSOR_DELAY_NORMAL);
          else{
       	   altitudeTextView.setText("无数据或您的设备不支持");
       	   updateTimer2.cancel();
          }
        Sensor AcceleratorSensor=sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(accSensorEventListener, 
            		AcceleratorSensor,SensorManager.SENSOR_DELAY_NORMAL );
       
        List<String> enabledProviders = locationManager.getProviders(true);
        if (enabledProviders.isEmpty()
                || !enabledProviders.contains(LocationManager.GPS_PROVIDER)||
                    !enabledProviders.contains(LocationManager.NETWORK_PROVIDER))
        {
        	latitudeTextView.setText("请在设置中打开GPS");
    		longitudeTextView.setText("请在设置中打开GPS");
        }
        else
        { 
        	Criteria mCriteria01 = new Criteria();   
            mCriteria01.setAccuracy(Criteria.ACCURACY_COARSE);   
            mCriteria01.setAltitudeRequired(false);   
            mCriteria01.setBearingRequired(false);   
            mCriteria01.setCostAllowed(true);   
            mCriteria01.setPowerRequirement(Criteria.POWER_HIGH);   
            String bestLocationProvider =    
            locationManager.getBestProvider(mCriteria01, true);
            Location pastLocation = locationManager.getLastKnownLocation   
            	      (bestLocationProvider); 
            if ((pastLocation== null))
            	{latitudeTextView.setText("无数据，正在定位.......");
                longitudeTextView.setText("无数据，正在定位.......");
            	}            	
            else
            	{latitudeTextView.setText(String.valueOf(pastLocation.getLatitude()));
                longitudeTextView.setText(String.valueOf(pastLocation.getLongitude()));
            	}
    		locationManager.requestLocationUpdates(bestLocationProvider,
                    1000,0,this,null);          
        } 
    	accavoidshake=true;
    	acccounter=0;
    }
    protected void onPause() {
        sensorManager.unregisterListener(tempSensorEventListener);
        sensorManager.unregisterListener(altiSensorEventListener);
        sensorManager.unregisterListener(accSensorEventListener);
        locationManager.removeUpdates(this);
        super.onPause();
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
	   
    private void updatetemperature() {
        runOnUiThread(new Runnable() {
    	public void run() {
            if (!Float.isNaN(currentTemperature)) {
              temperatureTextView.setText(currentTemperature + "℃");
              if (i==16) 
            	  i=0;
              else
              {pasttem[i]=currentTemperature;
              i++;}
          for (int j=0; j<pasttem.length;j++)
          { if (pasttem[j]>=maxtemperature)
        	   {temalertcounter++;
               if (temalertcounter>10)
            	   {temalertcounter=0;
            	   int type=0; 
                   alert(type);}
        	   }
           if(pasttem[j]==0.0){
        	   zerocounter++;
        	   if (zerocounter>10){
        		   zerocounter=0;
        		   temperatureTextView.setText("无数据或您的设备不支持");
        	   }
           }
         }
          }         
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
                	   int type=1;
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
        	 if (acceleration>=maxacc&&!accavoidshake)
        	 {
        		 int type=2;
        		 alert(type);
        	 }
        	 acccounter++;
        	 if (acccounter>3)
        	 accavoidshake=false;
         }       		  	
        });
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
    public void alert (int typein){

    	switch(typein){
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
    	        break;
    	}
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){   
            if((System.currentTimeMillis()-exitTime) > 2000){  
                Toast.makeText(getApplicationContext(), "再按一次退出", Toast.LENGTH_SHORT).show();                                
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
		// TODO Auto-generated method stub
		latitudeTextView.setText(String.valueOf(location.getLatitude()));
		longitudeTextView.setText(String.valueOf(location.getLongitude()));
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		if (provider.isEmpty()
                || !provider.contains(LocationManager.GPS_PROVIDER)||
                    !provider.contains(LocationManager.NETWORK_PROVIDER))
        {
        	latitudeTextView.setText("请在设置中打开GPS");
    		longitudeTextView.setText("请在设置中打开GPS");
        }
	}
}
