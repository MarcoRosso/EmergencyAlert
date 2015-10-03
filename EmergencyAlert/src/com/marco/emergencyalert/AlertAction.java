package com.marco.emergencyalert;



import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobUser;

import com.marco.emergencyalert.AlertSettings.download;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

public class AlertAction extends Activity {
	Camera m_Camera;
	Camera.Parameters mParameters;
	private int[] bgcolor={
			Color.rgb(255,0,0),
			Color.rgb(0,0,0),
			Color.rgb(255,0,0),
			Color.rgb(0,0,0),
			Color.rgb(255,0,0), 
			
			Color.rgb(0,0,0),
			
			Color.rgb(255,0,0),
			Color.rgb(0,0,0),			
			Color.rgb(255,0,0),
			Color.rgb(0,0,0),
			Color.rgb(255,0,0),
			
			Color.rgb(0,0,0),
			
			Color.rgb(255,0,0),
			Color.rgb(0,0,0),
			Color.rgb(255,0,0),
			Color.rgb(0,0,0),
			Color.rgb(255,0,0),
			
			Color.rgb(0,0,0)
			
			};
	private int[] bgflashtime = new int[] {
			300,
			300,
			300,	// ...	S
			300,
			300,
			//
			900,
			//
			900,
			300,
			900,	// ---	O
			300,
			900,
			//
			900,
			//
			300,
			300,
			300,	// ...	S
			300,
			300,
			//
			2100
	};
	private TextView warmingtv;
	private Handler show_handler;
	private Runnable show_runnable;
	private boolean firsttime=true;
	private boolean lightsetting=true;
	private boolean soundsetting=true;
	private boolean smssetting=true;
	private boolean servicesetting;
	private boolean dbfirst=true;
	private String contact1;
	private String contact2;
	private String contact3;
	private String msg;
	private String latitude;
	private String longitude;
	private int warmingcounter=-1;
	private long exitTime = 0;
	private TextView titletv;
	private MediaPlayer mMediaPlayer;
	private int volumenow;
	SharedPreferences preferences;
	SharedPreferences.Editor editor;
	SQLiteDatabase db;
	JSONObject obj;
	private String APPID = "fcfd0b7fd073f19f9e0fad6c2d59155f";
  	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if(msg.what==0x123){
                Toast.makeText(AlertAction.this, "微博发送失败！", Toast.LENGTH_SHORT).show();
		    }else if (msg.what==0x124){
		    	Toast.makeText(AlertAction.this, "微博发送成功！", Toast.LENGTH_SHORT).show();
		    }
	};};
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        setContentView(R.layout.fun4);
		warmingtv= (TextView) findViewById(R.id.warmingtv);
		titletv= (TextView) findViewById(R.id.titletv);
		
        Intent intent = getIntent(); 
        int type = intent.getIntExtra("type", 2);
        latitude = intent.getStringExtra("latitude");
        longitude=intent.getStringExtra("longitude");
		String address=intent.getStringExtra("address");
		preferences = getSharedPreferences("setting", MODE_PRIVATE);
		lightsetting = preferences.getBoolean("lightsetting",true);
		soundsetting = preferences.getBoolean("soundsetting",true);
		smssetting=preferences.getBoolean("smssetting",true);
		contact1=preferences.getString("contact1", "");
		contact2=preferences.getString("contact2", "");
		contact3=preferences.getString("contact3", "");
		servicesetting = preferences.getBoolean("servicesetting",true);
		boolean baiduable=preferences.getBoolean("baidusetting", true);
		dbfirst=preferences.getBoolean("dbfirst", true);
		db = SQLiteDatabase.openOrCreateDatabase(
				this.getFilesDir().toString()
				+ "/my.db3", null); 
		Bmob.initialize(this, APPID);
		
		String typestring=null;
		titletv.setText("报警功能正在工作！");
		switch(type){
		case 0: typestring="火灾";
			    if(baiduable)
			    msg="我可能遇到火灾，请迅速联系我！以下是我的位置信息："+"\n经度："+latitude+"\n纬度："+longitude+"\n可能的地址："+address;
		        else
			    msg="我可能遇到火灾，请迅速联系我！以下是我的位置信息："+"\n经度："+latitude+"\n纬度："+longitude;break;
		case 1: typestring="坠落";
			    if(baiduable)
			    msg="我可能急速下坠，请迅速联系我！以下是我的位置信息："+"\n经度："+latitude+"\n纬度："+longitude+"\n可能的地址："+address;
		       else
			    msg="我可能急速下坠，请迅速联系我！以下是我的位置信息："+"\n经度："+latitude+"\n纬度："+longitude;break;
		case 2: typestring="撞击";
			    if(baiduable)
			    msg="我可能遭到撞击，请迅速联系我！以下是我的位置信息："+"\n经度："+latitude+"\n纬度："+longitude+"\n可能的地址："+address;
		       else
			    msg="我可能遭到撞击，请迅速联系我！以下是我的位置信息："+"\n经度："+latitude+"\n纬度："+longitude;break;
		}
		if(smssetting){
			int count=0;
			if(contact1!=null&&!contact1.equals("")) {
				count++;
				sendmessage(contact1,msg);
			}
			if(contact2!=null&&!contact2.equals("")) {
				count++;
				sendmessage(contact2,msg);
			}
			if(contact3!=null&&!contact3.equals("")) {
				count++;
				sendmessage(contact3,msg);
			}
			if(count==0)
		    Toast.makeText(AlertAction.this, "您未填写有效号码，报警短息无法发送！" , Toast.LENGTH_SHORT).show();
			else
			Toast.makeText(AlertAction.this, count+"条报警信息已发送!" , Toast.LENGTH_SHORT).show();
		}
		
		if(soundsetting){
			AudioManager am = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
			volumenow=am.getStreamVolume(AudioManager.STREAM_MUSIC);
	        int max = am.getStreamMaxVolume( AudioManager.STREAM_MUSIC );
	        am.setSpeakerphoneOn(true);
	        am.setMicrophoneMute(false);   
	        am.setStreamVolume(AudioManager.STREAM_MUSIC, max,      
	                AudioManager.FLAG_PLAY_SOUND);
	        am.setMode(AudioManager.STREAM_MUSIC);
	        mMediaPlayer=new MediaPlayer();
	        mMediaPlayer=MediaPlayer.create(this, R.raw.alertbeep);
	        if (!mMediaPlayer.isPlaying()&&soundsetting)
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
		}
		
		if(lightsetting){
		m_Camera = Camera.open();
		mParameters = m_Camera.getParameters();
		warmingtv.setBackgroundColor(bgcolor[1]);
		setBrightness((float) 1.0);
		show_handler = new Handler();
		show_runnable = new Runnable() {
			public void run() {
				warmingcounter++;
				warmingtv.setBackgroundColor(bgcolor[warmingcounter%18]);				
				if(warmingcounter%2==0)
				{
					mParameters = m_Camera.getParameters();
					mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
					m_Camera.setParameters(mParameters);
				}
				else
				{
					mParameters = m_Camera.getParameters();
					mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
					m_Camera.setParameters(mParameters);
				}
				show_handler.postDelayed(this,bgflashtime[warmingcounter%18]);
			}
		};}else warmingtv.setBackgroundColor(bgcolor[1]);
		
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());       
		String date = sdf.format(new Date());
		String alertstring="否";	
		if(smssetting){
			if((contact1==null||contact1.equals(""))&&(contact2==null||contact2.equals(""))
					&&(contact3==null||contact3.equals("")))
			alertstring="否";
			else alertstring="是";
		}
		if(dbfirst){
		db.execSQL("create table alertrecord(_id integer"
				+ " primary key autoincrement,"
				+ " date varchar(25),"
				+ " latitude varchar(20),"
				+ " longitude varchar(20),"
				+ " type varchar(20),"
				+ " alert varchar(20),"
				+ " address varchar(255))");
		editor = preferences.edit();
		editor.putBoolean("dbfirst", false);
		editor.commit();}
		insertData(db,date,latitude,longitude,
			 typestring,alertstring,address);
		String expires=preferences.getString("weibo_expire","");
		if(!expires.equals("")){
			if(System.currentTimeMillis()>=Long.parseLong(expires)){
				BmobUser bmobUser = BmobUser.getCurrentUser(this);
				if(bmobUser != null){
					BmobUser.logOut(this);   //清除缓存用户对象
					Toast.makeText(getApplicationContext(), "微博绑定已过期，请重新登录！", Toast.LENGTH_LONG).show();
					editor = preferences.edit();
					editor.putString("weibo_json", "");
					editor.commit();
				}
			}else		
				sendWeibo();
		}

	}
	public void sendWeibo() {
		new Thread() {
			@Override
			public void run() {
				String json=preferences.getString("weibo_json", "");
				if(!json.equals("")){
				try {
					obj = new JSONObject(json);
				} catch (JSONException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				List<BasicNameValuePair> params = new LinkedList<BasicNameValuePair>();
				params.add(new BasicNameValuePair("status", msg));
				params.add(new BasicNameValuePair("lat",latitude));
				params.add(new BasicNameValuePair("long",longitude));
				try {
					params.add(new BasicNameValuePair("access_token", obj.getJSONObject("weibo")
							.getString("access_token")));
				} catch (JSONException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				HttpClient httpClient = new DefaultHttpClient();				
				//传入post方法的请求地址，即发送微博的api接口
				HttpPost postMethod = new HttpPost("https://api.weibo.com/2/statuses/update.json");
				try {
					postMethod.setEntity(new UrlEncodedFormEntity(params, "utf-8"));
					HttpResponse httpResponse = httpClient.execute(postMethod);
					//将返回结果转为字符串，通过文档可知返回结果为json字符串，结构请参考文档
					String resultStr=EntityUtils.toString(httpResponse.getEntity());
					Log.e("", resultStr);					
					//从json字符串中建立JSONObject
					JSONObject resultJson = new JSONObject(resultStr);					
					//如果发送微博失败的话，返回字段中有"error"字段，通过判断是否存在该字段即可知道是否发送成功
					if (resultJson.has("error")) {
						handler.sendEmptyMessage(0x123);
					} else {
						handler.sendEmptyMessage(0x124);
					}

				} catch (UnsupportedEncodingException e) {
					Log.d("",e.getLocalizedMessage());
				} catch (ClientProtocolException e) {
					Log.e("", e.getLocalizedMessage());
				} catch (IOException e) {
					Log.e("", e.getLocalizedMessage());
				} catch (ParseException e) {
					Log.e("", e.getLocalizedMessage());
				} catch (JSONException e) {
					Log.e("", e.getLocalizedMessage());
				}
			    }
		 }
		}.start();
	}
	
	private void insertData(SQLiteDatabase db, String timein, String latitudein
			, String longitudein, String typein, String alertin, String addressin)
		{
			db.execSQL("insert into alertrecord values(null ,?,?,?,?,?,?)"
				, new String[] {timein,latitudein,longitudein,typein,alertin,addressin});
			System.out.println("-----data stored--------");
		}
    public void onDestroy() 
	{
        super.onDestroy();
        if(lightsetting){
    	show_handler.removeCallbacks(show_runnable);
    	m_Camera.release();}
        AudioManager am = (AudioManager) AlertAction.this.getSystemService(Context.AUDIO_SERVICE);
	    am.setStreamVolume(AudioManager.STREAM_MUSIC, volumenow,      
                AudioManager.FLAG_PLAY_SOUND);
        if(soundsetting)
  	     if (mMediaPlayer.isPlaying()){
  	    	 mMediaPlayer.stop();
 		     mMediaPlayer.release();
  	     }
        db.close();

    }
    protected void onResume() {
        super.onResume();
        if(firsttime&&lightsetting)
        {
        	firsttime=false;
        	show_handler.postDelayed(show_runnable,50);
        }
        Intent intent= new Intent();
        intent.setClass(AlertAction.this, AlertService.class);
    	stopService(intent);
	}
	protected void onPause(){
		super.onPause();
 	     if (mMediaPlayer.isPlaying())
		     mMediaPlayer.release();
	}
	public void setBrightness(float f){
		WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.screenBrightness = f;   
		getWindow().setAttributes(lp);
    } 
    public void sendmessage(String contactnumber, String alertmsg){
		SmsManager smsManager = SmsManager.getDefault(); 
  		if (contactnumber==null||contactnumber.equals("")){
  			Toast.makeText(this, "联系人号码为空!" , Toast.LENGTH_SHORT).show();
  		}else{
  			String SENT_SMS_ACTION = "SENT_SMS_ACTION";  
  			Intent sentIntent = new Intent(SENT_SMS_ACTION);  
  			PendingIntent sentPI = PendingIntent.getBroadcast(getApplicationContext(), 0, sentIntent, 0);  
  			// register the Broadcast Receivers  
  			getApplicationContext().registerReceiver(new BroadcastReceiver() {  
  			    @Override  
  			    public void onReceive(Context _context, Intent _intent) {  
  			        switch (getResultCode()) {  
  			              case Activity.RESULT_OK:  
  			            	Log.i("====>", "Activity.RESULT_OK");
  			        break;  
  			          case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
  			        	Log.i("====>", "RESULT_ERROR_GENERIC_FAILURE");
  			        break;  
  			          case SmsManager.RESULT_ERROR_RADIO_OFF: 
  			        	Log.i("====>", "RESULT_ERROR_RADIO_OFF");
  			        break;  
  			          case SmsManager.RESULT_ERROR_NULL_PDU: 
  			        	Log.i("====>", "RESULT_ERROR_NULL_PDU");
  			        break;
  			          case SmsManager.RESULT_ERROR_NO_SERVICE:
  			        Log.i("====>", "RESULT_ERROR_NO_SERVICE");
  			        }  
  			    }  
  			}, new IntentFilter(SENT_SMS_ACTION));

  		  ArrayList<String> messageArray=smsManager.divideMessage(alertmsg);
  		  ArrayList<PendingIntent> sentIntents=new ArrayList<PendingIntent>();
  		  for (int i=0; i<messageArray.size();i++)
  			  sentIntents.add(sentPI);
  			 smsManager.sendMultipartTextMessage(contactnumber, null, messageArray, sentIntents, null);

  			ContentValues values = new ContentValues();  
  			values.put("date", System.currentTimeMillis());   
  			values.put("read", 0);  
  			values.put("type", 2);  
  			values.put("address", contactnumber);  
  			values.put("body", alertmsg);  
  			getContentResolver().insert(Uri.parse("content://sms"),values);
  		} }
    
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){   
            if((System.currentTimeMillis()-exitTime) > 2000){  
                Toast.makeText(getApplicationContext(), "再按一次退出报警", Toast.LENGTH_SHORT).show();                                
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
}
