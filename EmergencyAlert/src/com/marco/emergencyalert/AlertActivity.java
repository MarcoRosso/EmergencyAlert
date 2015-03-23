package com.marco.emergencyalert;


import java.text.DecimalFormat;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;





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


public class AlertActivity extends Activity implements LocationListener, OnGetGeoCoderResultListener {
	  private Button alertbottom;
	  private Button configbottom;
	  private SensorManager sensorManager;
	  private LocationManager locationManager;
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
	  private String bestLocationProvider;
	  private boolean temalertsent=false;
	  private boolean servicesetting;
	  private boolean temperatureable=true;
	  private boolean altitudeable=true;
	  private boolean accable=true;
	  private boolean baiduable=true;
	  SharedPreferences preferences;
      GeoCoder mSearch = null;

      
      
      
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.alertlayout);
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
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        
        temperaturealert.setVisibility(View.INVISIBLE);
        altitudealert.setVisibility(View.INVISIBLE);
        accalert.setVisibility(View.INVISIBLE);
        
		mSearch = GeoCoder.newInstance();
		mSearch.setOnGetGeoCodeResultListener(this);
        
		baiduable=preferences.getBoolean("baidusetting", true);
		if(!baiduable) addressTextView.setText("请在设置中打开 获取地址 功能");
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
        List<String> enabledProviders = locationManager.getProviders(true);
        if (enabledProviders.isEmpty()
                || !enabledProviders.contains(LocationManager.GPS_PROVIDER)||
                    !enabledProviders.contains(LocationManager.NETWORK_PROVIDER)||count==0){
        final Dialog dialog = new Dialog(AlertActivity.this, R.style.MyDialogOutside);
        dialog.setContentView(R.layout.fisrtrundialog);
        dialog.show();}
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
            bestLocationProvider =    
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
    			if(baiduable){LatLng ptCenter = new LatLng((Float.valueOf(latitudeTextView.getText()
    					.toString())), (Float.valueOf(longitudeTextView.getText().toString())));
    			mSearch.reverseGeoCode(new ReverseGeoCodeOption()
				.location(ptCenter));}
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
    protected void onDestory(){
    	mSearch.destroy();
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
    
	@Override
	public void onLocationChanged(Location location) {
		latitudeTextView.setText(String.valueOf(location.getLatitude()));
		longitudeTextView.setText(String.valueOf(location.getLongitude()));
		if(baiduable){LatLng ptCenter = new LatLng((Float.valueOf(latitudeTextView.getText()
				.toString())), (Float.valueOf(longitudeTextView.getText().toString())));
		mSearch.reverseGeoCode(new ReverseGeoCodeOption()
		.location(ptCenter));}
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {

	}
	public void onProviderEnabled(String provider) {
		locationManager.requestLocationUpdates(bestLocationProvider,
                1000,0,this,null);  
		latitudeTextView.setText("无数据，正在定位.......");
        longitudeTextView.setText("无数据，正在定位.......");
	}

	@Override
	public void onProviderDisabled(String provider) {
		if (provider.isEmpty()
                || !provider.contains(LocationManager.GPS_PROVIDER)||
                    !provider.contains(LocationManager.NETWORK_PROVIDER))
        {
        	latitudeTextView.setText("请在设置中打开GPS");
    		longitudeTextView.setText("请在设置中打开GPS");
        }
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
	@Override
	public void onGetGeoCodeResult(GeoCodeResult arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
		// TODO Auto-generated method stub
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
           addressTextView.setText("无法找到相应地址");
			return;
		}  addressTextView.setText(result.getAddress());
	}

}
