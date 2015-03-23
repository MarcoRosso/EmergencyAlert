package com.marco.emergencyalert;





import java.text.DecimalFormat;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
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
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;


public class AlertService extends Service implements LocationListener,OnGetGeoCoderResultListener{
	private SensorManager sensorManager;
	private LocationManager locationManager;
	private NotificationManager manager;
	WakeLock mWakeLock = null;
	private float currentAltitude;
	private float pastAltitude=0;
	private float currentTemperature ;
	private float[] gravity={0,0,0};
	float pasttem[] = new float[30];
	float accvalues[]=new float[3];
	private int i=0;
	private int acccounter=0;
	int temalertcounter;
	int type;
	int progressStatus = 0;
	private boolean accavoidshake=true;
	private boolean baiduable=true;
	private float maxtemperature;
	private float maxaltitude;
	private float maxacc;
	private String latitude= null;
	private String longitude= null;
	private String address=null;
	Timer t = new Timer();
	private boolean temperatureable=true;
	private boolean altitudeable=true;
	private boolean accable=true;
	private static final float ALPHA = 0.8f;
	SharedPreferences preferences;
	GeoCoder mSearch = null;
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	 public void onCreate() {
	        super.onCreate();
	        SDKInitializer.initialize(getApplicationContext());
	        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
	        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
	        
	        preferences = getSharedPreferences("setting", MODE_PRIVATE);
			baiduable=preferences.getBoolean("baidusetting", true);
			if(!baiduable) address="请在设置中打开 获取地址 功能";
	        int temperaturesetting=preferences.getInt("temperaturesetting", 50);
	        if(temperaturesetting==100) temperatureable=false;
	        else maxtemperature=(float) (30+temperaturesetting*0.4);
	        int altitudesetting=preferences.getInt("altitudesetting", 28);
	        if(altitudesetting==100) altitudeable=false;
	        else maxaltitude=(float) (altitudesetting*2.5/100+0.50);
	        int accsetting=preferences.getInt("accsetting",40);
	        if(accsetting==100) accable=false;
	        else  maxacc=(float) (accsetting*0.25+5);
	        
			mSearch = GeoCoder.newInstance();
			mSearch.setOnGetGeoCodeResultListener(this);
			
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
	        }, 0, 200);
	        Sensor temperatureSensor =
	                sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
	              if (temperatureSensor != null)
	                sensorManager.registerListener(tempSensorEventListener,
	                    temperatureSensor,                                 
	                    SensorManager.SENSOR_DELAY_NORMAL);
	              Sensor AltitudeiSensor=sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
	              if (AltitudeiSensor != null)
	                  sensorManager.registerListener(altiSensorEventListener,
	                 		 AltitudeiSensor,                                 
	                      SensorManager.SENSOR_DELAY_NORMAL);
	              Sensor AcceleratorSensor=sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	                  sensorManager.registerListener(accSensorEventListener, 
	                  		AcceleratorSensor,SensorManager.SENSOR_DELAY_NORMAL );
	             
	              List<String> enabledProviders = locationManager.getProviders(true);
	              if (enabledProviders.isEmpty()
	                      || !enabledProviders.contains(LocationManager.GPS_PROVIDER)||
	                          !enabledProviders.contains(LocationManager.NETWORK_PROVIDER))
	              {      latitude="请在设置中打开GPS";
	                     longitude="请在设置中打开GPS";            	  
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
	              	{latitude="无数据，正在定位.......";
	                 longitude="无数据，正在定位.......";
	              	}            	
	              else
	              	{latitude=String.valueOf(pastLocation.getLatitude());
	                  longitude=String.valueOf(pastLocation.getLongitude());
	                  if(baiduable){LatLng ptCenter = new LatLng((Float.valueOf(latitude)), 
	                		   (Float.valueOf(longitude)));
	      			mSearch.reverseGeoCode(new ReverseGeoCodeOption()
	  				.location(ptCenter));}
	              	}
	          		  locationManager.requestLocationUpdates(bestLocationProvider,
	                          1000,0,this,null);
	              }
    }
	public int onStartCommand(Intent intent, int flags,int startId){
         super.onStartCommand(intent, flags, startId);
		 String ns = Context.NOTIFICATION_SERVICE;
         manager = (NotificationManager) getSystemService(ns);
         Notification notification = new Notification();
         notification.icon=R.drawable.alert;
         notification.tickerText=getText(R.string.app_name);
         notification.when=System.currentTimeMillis();
         CharSequence contentTitle = getText(R.string.app_name);
         CharSequence contentText =  "紧急报警正在后台运行";
         Intent intent1 = new Intent(AlertService.this, AlertActivity.class);
         PendingIntent contentIntent = PendingIntent.getActivity(AlertService.this, 0, intent1, Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
         notification.setLatestEventInfo(AlertService.this, contentTitle, contentText, contentIntent);
         notification.flags=Notification.FLAG_NO_CLEAR;
         manager.notify(0, notification);
         accavoidshake=true;
         acccounter=0;
         acquireWakeLock();
		 return startId;
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
           if (!Float.isNaN(currentTemperature)) {          
             if (i==29) 
           	  i=0;
             else
             {pasttem[i]=currentTemperature;
             i++;}
         for (int j=0; j<pasttem.length;j++)
         { if (pasttem[j]>=maxtemperature)
       	   {temalertcounter++;
              if (temalertcounter>25&&temperatureable)
           	      {type=0; 
                  alert(type);
                }
       	   }       
        }}
     }
   private void updatealtitude(){
				float altitudeDifference = 0;
				if (!Float.isNaN(currentAltitude)) {
					if (pastAltitude!=0 && currentAltitude>=pastAltitude)
					{altitudeDifference=
					     SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE,pastAltitude)
					     -
					     SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE,currentAltitude);
					pastAltitude=currentAltitude;
					}
					else if (pastAltitude!=0 && currentAltitude<pastAltitude)
					{altitudeDifference=
				     SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE,currentAltitude)
				     -
				     SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE,pastAltitude);
				    pastAltitude=currentAltitude;				
					} else pastAltitude=currentAltitude;
                  if (altitudeDifference>=maxaltitude&&altitudeable)	{
               	   type=1;
               	   alert(type);             	   
                  }
				}				
   }
   public void updateaccelerator(){
       	 float[] values=accvalues;
       	 values=highPass(values[0],values[1],values[2]);
       	 double sumOfSquares=(values[0]*values[0])+(values[1]*values[1]
       			 +values[2]*values[2]);
       	 double acceleration = Math.sqrt(sumOfSquares);
       	 DecimalFormat df = new DecimalFormat("########.0000");
       	 acceleration = Double.parseDouble(df.format(acceleration));
       	 if (acceleration>=maxacc&&!accavoidshake&&accable)
       	 {
       		 type=2;
       		 alert(type);
       	 }
       	acccounter++;
   	     if (acccounter>3)
   	      accavoidshake=false;
   }
   public void onDestroy() {
         sensorManager.unregisterListener(tempSensorEventListener);
         sensorManager.unregisterListener(altiSensorEventListener);
         sensorManager.unregisterListener(accSensorEventListener);
         locationManager.removeUpdates(this);
         manager.cancel(0);
         releaseWakeLock();
         super.onDestroy();
     }
     public void alert (int typein){
    	 for (int k=0; k<pasttem.length;k++)  pasttem[k]=0;
     	Intent intent  = new Intent();
     	intent.putExtra("type", typein);
     	intent.putExtra("latitude", latitude);
     	intent.putExtra("longitude", longitude);
     	intent.putExtra("address", address);
     	intent.setClass(AlertService.this,AlertDialog.class);
   	    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
     	startActivity(intent);
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
   private void acquireWakeLock()
   {
      
	if (null == mWakeLock)
       {
           PowerManager pm = (PowerManager)this.getSystemService(Context.POWER_SERVICE);
           mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK|PowerManager.ON_AFTER_RELEASE,"");
           if (null != mWakeLock)
           {
               mWakeLock.acquire();
           }
       }
    }
   private void releaseWakeLock()
   {
       if (null != mWakeLock)
       {
           mWakeLock.release();
           mWakeLock = null;
       }
   }

@Override
public void onLocationChanged(Location location) {
	// TODO Auto-generated method stub
	latitude=String.valueOf(location.getLatitude());
	longitude=String.valueOf(location.getLongitude());
    if(baiduable){LatLng ptCenter = new LatLng((Float.valueOf(latitude)), 
 		   (Float.valueOf(longitude)));
	mSearch.reverseGeoCode(new ReverseGeoCodeOption()
	.location(ptCenter));}
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
	
}
@Override
public void onGetGeoCodeResult(GeoCodeResult arg0) {
	// TODO Auto-generated method stub
	
}
@Override
public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
	// TODO Auto-generated method stub
	if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
        address="无法找到相应地址";
			return;
		}  address=result.getAddress();
}  
}

