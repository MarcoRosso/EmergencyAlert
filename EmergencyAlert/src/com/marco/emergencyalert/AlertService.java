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

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.mapapi.model.LatLng;



public class AlertService extends Service {
	private SensorManager sensorManager;
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
	private boolean accavoidshake=true;
	private boolean baiduable=true;
	private float maxtemperature;
	private float maxaltitude;
	private float maxacc;
	private String latitude="正在获取.....";
	private String longitude="正在获取.....";
	private String address="打开数据或WiFi连接互联网获取";
	private boolean temperatureable=true;
	private boolean altitudeable=true;
	private boolean accable=true;
	private static final float ALPHA = 0.8f;
	SharedPreferences preferences;
	 private LocationClient locationClient = null;
	 private static final int UPDATE_TIME = 5000;
	 private static int LOCATION_COUTNS = 0;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	 public void onCreate() {
	        super.onCreate();
	        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
	        locationClient = new LocationClient(this);
	        LocationClientOption option = new LocationClientOption();
	        option.setOpenGps(true);        //是否打开GPS
	        option.setCoorType("gcj02");       //设置返回值的坐标类型。
	        option.setLocationMode(LocationMode.Battery_Saving);  //设置定位优先级
	        option.setProdName("LocationDemo"); //设置产品线名称。强烈建议您使用自定义的产品线名称，方便我们以后为您提供更高效准确的定位服务。
	        option.setScanSpan(UPDATE_TIME);    //设置定时定位的时间间隔。单位毫秒
	        option.setIsNeedAddress(true);
	        locationClient.setLocOption(option);
	        
	        preferences = getSharedPreferences("setting", MODE_PRIVATE);
			baiduable=preferences.getBoolean("baidusetting", true);
			if(!baiduable) address="请在设置中打开 获取位置 功能";
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
	          

	           locationClient.registerLocationListener(new BDLocationListener() {
	                      
	                      @Override
	                      public void onReceiveLocation(BDLocation location) {
	                          // TODO Auto-generated method stub
	                          if (location == null) {
	                              return;
	                          }
	                          StringBuffer sb = new StringBuffer(256);
	                          sb.append("Time : ");
	                          sb.append(location.getTime());
	                          sb.append("\nError code : ");
	                          sb.append(location.getLocType());
	                          sb.append("\nLatitude : ");
	                          latitude=String.valueOf(location.getLatitude());
	                          sb.append(location.getLatitude());
	                          sb.append("\nLontitude : ");
	                          longitude=String.valueOf(location.getLongitude());
	                          sb.append(location.getLongitude());
	                          sb.append("\nRadius : ");
	                          sb.append(location.getRadius());
	                          if (location.getLocType() == BDLocation.TypeGpsLocation){
	                              sb.append("\nSpeed : ");
	                              sb.append(location.getSpeed());
	                              sb.append("\nSatellite : ");
	                              sb.append(location.getSatelliteNumber());
	                              if(!baiduable) address="请在设置中打开 获取位置 功能";
	                              else address=location.getAddrStr();
	                          } else if (location.getLocType() == BDLocation.TypeNetWorkLocation){
	                              sb.append("\nAddress : ");
	                              if(!baiduable) address="请在设置中打开 获取位置 功能";
	                              else address=location.getAddrStr();
	                              sb.append(location.getAddrStr());
	                          }
	                          LOCATION_COUTNS ++;
	                          sb.append("\n检查位置更新次数：");
	                          sb.append(String.valueOf(LOCATION_COUTNS));

	                      }                    
	                  });
	           locationClient.start();
	           locationClient.requestLocation();
	              
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
         CharSequence contentText =  "紧急报警正在后台保护您";
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
         manager.cancel(0);
         releaseWakeLock();
         if (locationClient != null && locationClient.isStarted()) {
             locationClient.stop();
             locationClient = null;}
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



}

