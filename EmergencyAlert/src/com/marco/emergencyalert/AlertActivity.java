package com.marco.emergencyalert;


import java.text.DecimalFormat;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;






import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;






import android.app.Activity;
import android.app.ActivityManager;

import android.app.Dialog;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;


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


public class AlertActivity extends Activity {
	  private Button alertbottom;
	  private Button configbottom;
	  private SensorManager sensorManager;
	  private TextView temperatureTextView;
	  private TextView altitudeTextView;
	  private TextView accTextView;
	  private TextView latitudeTextView;
	  private TextView longitudeTextView;
	  private TextView addressTextView;
	  private ImageView temperaturealert;
	  private ImageView altitudealert;
	  private ImageView accalert;
	  private float currentAltitude;
	  private float pastAltitude=0;
	  private float[] gravity={0,0,0};
	  private float pasttem[] = new float[50];
	  private float accvalues[]=new float[3];
	  private float currentTemperature ;
	  private long exitTime = 0;
	  private boolean accavoidshake=true;
	  private float maxtemperature;
	  private float maxaltitude;
	  private float maxacc;
	  private static final float ALPHA = 0.8f;
	  private int i=0;
	  private int acccounter=0;
	  private int zerocounter=0;
	  private int temalertcounter;
	  private boolean temalertsent=false;
	  private boolean servicesetting;
	  private boolean temperatureable=true;
	  private boolean altitudeable=true;
	  private boolean accable=true;
	  private boolean baiduable=true;
	  SharedPreferences preferences;
	  private LocationClient mLocationClient;
	  private LocationMode tempMode = LocationMode.Hight_Accuracy;


	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alertlayout);
        mLocationClient = ((LocationApplication)getApplication()).mLocationClient;
        preferences = getSharedPreferences("setting", MODE_PRIVATE);
        servicesetting = preferences.getBoolean("servicesetting",true);
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
        addressTextView=(TextView)findViewById(R.id.address);
        
        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);

        
        temperaturealert.setVisibility(View.INVISIBLE);
        altitudealert.setVisibility(View.INVISIBLE);
        accalert.setVisibility(View.INVISIBLE);
        

       ((LocationApplication)getApplication()).latitude = latitudeTextView;
       ((LocationApplication)getApplication()).longitude = longitudeTextView;
       
        
		baiduable=preferences.getBoolean("baidusetting", true);
		if(!baiduable) addressTextView.setText("请在设置中打开 获取位置 功能");
		else ((LocationApplication)getApplication()).address = addressTextView;
        int temperaturesetting=preferences.getInt("temperaturesetting", 50);
        if(temperaturesetting==100) temperatureable=false;
        else maxtemperature=(float) (30+temperaturesetting*0.4);
        int altitudesetting=preferences.getInt("altitudesetting", 28);
        if(altitudesetting==100) altitudeable=false;
        else maxaltitude=(float) (altitudesetting*2.5/100+0.50);
        int accsetting=preferences.getInt("accsetting",40);
        if(accsetting==100) accable=false;
        else  maxacc=(float) (accsetting*0.25+5);
        
        Timer updateTimer = new Timer("UpdateTemperature");
        updateTimer.scheduleAtFixedRate(new TimerTask() {
          public void run() {  	  
        	updatetemperature();      
          }
        }, 0, 500);
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

        
        configbottom.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				startActivity(new Intent (AlertActivity.this, AlertSettings.class));
        		finish();	
        		overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);  
			}      	
        });
        
		String contact1=preferences.getString("contact1", "");
		String contact2=preferences.getString("contact2", "");
		String contact3=preferences.getString("contact3", "");
		int count=0;
		if(contact1!=null&&!contact1.equals("")) count++;
		if(contact2!=null&&!contact2.equals("")) count++;
		if(contact3!=null&&!contact3.equals("")) count++;

        if (count==0){
        final Dialog dialog = new Dialog(AlertActivity.this, R.style.MyDialogOutside);
        dialog.setContentView(R.layout.fisrtrundialog);
        dialog.show();}
        
        InitLocation();
        mLocationClient.start();

	}
    protected void onResume() {
        super.onResume();
        Intent intent= new Intent();
        intent.setClass(AlertActivity.this, AlertService.class);
    	stopService(intent);
        for (int j=0; j<pasttem.length;j++)  pasttem[j]=0;
        i=0;
        temalertsent=false;
        alertbottom.setClickable(false);
        Sensor temperatureSensor =
          sensorManager.getDefaultSensor(Sensor.TYPE_TEMPERATURE);
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
          }
        Sensor AcceleratorSensor=sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(accSensorEventListener, 
            		AcceleratorSensor,SensorManager.SENSOR_DELAY_NORMAL );
    	accavoidshake=true;
    	acccounter=0;
    }
    
    protected void onPause() {
        sensorManager.unregisterListener(tempSensorEventListener);
        sensorManager.unregisterListener(altiSensorEventListener);
        sensorManager.unregisterListener(accSensorEventListener);
        super.onPause();
      }
	protected void onStop() {
		// TODO Auto-generated method stub
		mLocationClient.stop();
		super.onStop();
	}
    protected void onDestory(){
    	super.onDestroy();
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
              if (i==49) 
            	  i=0;
              else
              {pasttem[i]=currentTemperature;
              i++;}
          for (int j=0; j<pasttem.length;j++)
          { if (pasttem[j]>=maxtemperature)
        	   {temalertcounter++;
               if (temalertcounter>45)
            	   {temalertcounter=0;
            	   if(!temalertsent&&temperatureable){
            	   int type=0; 
                   alert(type);
                   if(servicesetting)
                   temalertsent=true;}
            	   }
        	   }
         }
          for(int k=0;k<10;k++){
          if(pasttem[k]==0.0){
       	   zerocounter++;
       	   if (zerocounter>10){
       		   zerocounter=0;
       		   temperatureTextView.setText("无数据或您的设备不支持");
       	   }}
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
                   if (altitudeDifference>=maxaltitude&&altitudeable)	{
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
        	 if (acceleration>=maxacc&&!accavoidshake&&accable)
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
    	if(servicesetting){
    	Intent intent  = new Intent();
    	intent.putExtra("type", typein);
    	intent.putExtra("latitude", latitudeTextView.getText().toString());
    	intent.putExtra("longitude", longitudeTextView.getText().toString());
    	intent.putExtra("address", addressTextView.getText().toString());
    	intent.setClass(AlertActivity.this,AlertDialog.class);
    	startActivity(intent);}else{
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
    	}}
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){   
            if((System.currentTimeMillis()-exitTime) > 2000){  
                Toast.makeText(getApplicationContext(), "再按一次退出", Toast.LENGTH_SHORT).show();                                
                exitTime = System.currentTimeMillis();   
            } else {
        		if(!isMyServiceRunning()&&servicesetting)
        	    	startService(new Intent(this, AlertService.class));	
               finish();
            }
            return true;   
        }
        return super.onKeyDown(keyCode, event);
    }
    

	private boolean isMyServiceRunning() {
	    ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if ("com.marco..emergencyalert.AlertService".equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}
	private void InitLocation(){
		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(tempMode);//设置定位模式
		option.setCoorType("gcj02");//返回的定位结果是百度经纬度，默认值gcj02
		option.setScanSpan(2000);//设置发起定位请求的间隔时间为5000ms
		option.setIsNeedAddress(true);
		mLocationClient.setLocOption(option);
	}



}
