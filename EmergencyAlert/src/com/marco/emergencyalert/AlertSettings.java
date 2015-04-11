package com.marco.emergencyalert;






import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;



import org.json.JSONObject;




import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.listener.OtherLoginListener;


import com.marco.emergencyalert.SwitchButton.OnCheckedChangeListener;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuth;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;


import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SimpleCursorAdapter;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class AlertSettings extends Activity{
	  private Button alertbottom;
	  private Button configbottom;
	  private Button contactnumberconfirm;
	  private Button contactnumber1search;
	  private Button contactnumber2search;
	  private Button contactnumber3search;
	  private Button resettodefault;
	  private Button showlist;
	  private Button deletelist;
	  
	  private SwitchButton serviceswitch;
	  private SwitchButton soundswitch;
	  private SwitchButton lightswitch;
	  private SwitchButton baiduswitch;
	  private SwitchButton smsswitch;
	  
	  private TextView contactnumber1;
	  private TextView contactnumber2;
	  private TextView contactnumber3;
	  private TextView temperaturenumber;
	  private TextView altitudenumber;
	  private TextView accnumber;
	  private TextView weiboname;
	  private ImageView weiboicon;
	  private LinearLayout weibosetting;
	    
	  private SeekBar temperaturesettingseekbar;
	  private SeekBar altitudesettingseekbar;
	  private SeekBar accsettingseekbar;
	  
	  private String contact1;
	  private String contact2;
	  private String contact3;
	  private String downloadpath;
	  
	  private boolean servicesetting;
	  private int searchbuttonnumber;
	  private int temperaturesetting;
	  private int altitudesetting;
	  private int accsetting;
	  private long exitTime = 0;
  	  SharedPreferences preferences;
  	  SharedPreferences.Editor editor;
  	  SQLiteDatabase db;
  	  JSONObject obj;
  	  String json = "";
  	  private static final int PICK_CONTACT_SUBACTIVITY = 2;
  	  private String APPID = "fcfd0b7fd073f19f9e0fad6c2d59155f";
  	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if(msg.what==0x123){
			String result = (String) msg.obj;
			if (result != null) {
				JsonUtils jsonUtils = new JsonUtils();
				User weibouser=jsonUtils.parseUserFromJson(result);
				preferences = getSharedPreferences("setting", MODE_PRIVATE);
				editor = preferences.edit();
				weiboname.setText(weibouser.getScreen_name());
				editor.putString("weibo_name",weibouser.getScreen_name());
				downloadpath=weibouser.getProfile_image_url();
				editor.putString("weibo_logo", downloadpath);
				editor.commit();
				new Thread(new download()).start();
			} else {
				Toast.makeText(getApplicationContext(), "暂无个人信息，请联系开发者获取测试资格", Toast.LENGTH_LONG).show();
			}
		}else if(msg.what==0x124){
			weiboicon.setImageBitmap((Bitmap) msg.obj);
			}
		};
	};
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settinglayout);
        alertbottom=(Button)findViewById(R.id.alertbottom);
        configbottom=(Button)findViewById(R.id.configbottom);
        contactnumber1search=(Button)findViewById(R.id.contactnumber1search);
        contactnumber2search=(Button)findViewById(R.id.contactnumber2search);
        contactnumber3search=(Button)findViewById(R.id.contactnumber3search);
        contactnumberconfirm=(Button)findViewById(R.id.contactnumberconfirm);
        showlist=(Button)findViewById(R.id.showlist);
        deletelist=(Button)findViewById(R.id.deletelist);
        resettodefault=(Button)findViewById(R.id.resettodefault);
        
        temperaturesettingseekbar=(SeekBar)findViewById(R.id.temperaturesettingseekbar);
        altitudesettingseekbar=(SeekBar)findViewById(R.id.altitudesettingseekbar);
        accsettingseekbar=(SeekBar)findViewById(R.id.accsettingseekbar);
        
        contactnumber1=(TextView)findViewById(R.id.contactnumber1);
        contactnumber2=(TextView)findViewById(R.id.contactnumber2);
        contactnumber3=(TextView)findViewById(R.id.contactnumber3);
        temperaturenumber=(TextView)findViewById(R.id.temperaturenumber);
        altitudenumber=(TextView)findViewById(R.id.altitudenumber);
        accnumber=(TextView)findViewById(R.id.accnumber);
        weiboname=(TextView)findViewById(R.id.weiboname);
        weiboicon=(ImageView)findViewById(R.id.weibologo);
        weibosetting=(LinearLayout)findViewById(R.id.weibosetting);
    
        serviceswitch=(SwitchButton)findViewById(R.id.serviceswitch);
        soundswitch=(SwitchButton)findViewById(R.id.soundswitch);
        lightswitch=(SwitchButton)findViewById(R.id.lightswitch);
        baiduswitch=(SwitchButton)findViewById(R.id.baiduswitch);
        smsswitch=(SwitchButton)findViewById(R.id.smswitch);
        
        preferences = getSharedPreferences("setting", MODE_PRIVATE);
		servicesetting = preferences.getBoolean("servicesetting",true);
		boolean soundsetting = preferences.getBoolean("soundsetting",true);
		boolean lightsetting = preferences.getBoolean("lightsetting",true);
		boolean baidusetting = preferences.getBoolean("baidusetting",true);
		boolean smssetting=preferences.getBoolean("smssetting",true);
		contact1=preferences.getString("contact1", "");
		contact2=preferences.getString("contact2", "");
		contact3=preferences.getString("contact3", "");
		temperaturesetting=preferences.getInt("temperaturesetting", 50);
		altitudesetting=preferences.getInt("altitudesetting", 28);
		accsetting=preferences.getInt("accsetting",40);
		
		serviceswitch.changbuttonstatues(servicesetting);
		soundswitch.changbuttonstatues(soundsetting);
		lightswitch.changbuttonstatues(lightsetting);
		baiduswitch.changbuttonstatues(baidusetting);
		smsswitch.changbuttonstatues(smssetting);
		contactnumber1.setText(contact1);
		contactnumber2.setText(contact2);
		contactnumber3.setText(contact3);
		db = SQLiteDatabase.openOrCreateDatabase(
				this.getFilesDir().toString()
				+ "/my.db3", null);
		Bmob.initialize(this, APPID);
			
		if(temperaturesetting==100&&altitudesetting==100&&accsetting==100)
			serviceswitch.changbuttonstatues(false);
		else serviceswitch.changbuttonstatues(true);
		temperaturesettingseekbar.setProgress(temperaturesetting);
		if(temperaturesetting==100) {
			temperaturenumber.setText("  关闭");
		}else{
			DecimalFormat df1 = new DecimalFormat("##.0");
		    temperaturenumber.setText(df1.format(30+temperaturesetting*0.4)+"℃");
		}
		altitudesettingseekbar.setProgress(altitudesetting);
		if(altitudesetting==100) {
			altitudenumber.setText("  关闭");
		}else{
			DecimalFormat df2 = new DecimalFormat("0.00");
			altitudenumber.setText(df2.format(altitudesetting*2.5/100+0.50)+"m");
		}
		accsettingseekbar.setProgress(accsetting);
		if(accsetting==100) {
			accnumber.setText("          关闭");
		}else{
			DecimalFormat df3 = new DecimalFormat("00.0");
			accnumber.setText(df3.format(accsetting*0.25+5)+"m/s^2");
		}
		
		weibosetting.setOnClickListener(new OnClickListener(){
            @Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				BmobUser.weiboLogin(AlertSettings.this, "2785379416", "https://api.weibo.com/oauth2/default.html", new OtherLoginListener() {
					
					@Override
					public void onSuccess(JSONObject userAuth) {
						// TODO Auto-generated method stub
						Toast.makeText(getApplicationContext(), "登陆成功",Toast.LENGTH_SHORT).show();
						Log.i("login", "weibo登陆成功返回:"+userAuth.toString());				
						json=userAuth.toString();
						editor = preferences.edit();
						editor.putString("weibo_json", json);
						editor.commit();
						getWeiboInfo();
					}
					
					@Override
					public void onFailure(int code, String msg) {
						// TODO Auto-generated method stub
						//若出现授权失败(authData error)，可清除该应用缓存，之后在授权新浪登陆
						Toast.makeText(getApplicationContext(), "登陆失败！", Toast.LENGTH_SHORT).show();
					}
				});
			}
			
		});
		BmobUser bmobUser = BmobUser.getCurrentUser(this);
		if(bmobUser != null){
		    weibosetting.setClickable(false);
		    weiboname.setText(preferences.getString("weibo_name",""));
		    downloadpath=preferences.getString("weibo_logo", "");
		    new Thread(new download()).start();		    
		}else{
		   weibosetting.setClickable(true);
		}
		
        alertbottom.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				startActivity(new Intent (AlertSettings.this, AlertActivity.class));
        		finish();
        		overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out); 
			}      	
        });
        serviceswitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChange(boolean isChecked) {
				if (isChecked) {
		    		editor = preferences.edit();
		    		servicesetting=true;
					editor.putBoolean("servicesetting", isChecked);
					editor.commit();
				} else {
		    		editor = preferences.edit();
		    		servicesetting=false;
					editor.putBoolean("servicesetting", isChecked);
					editor.commit();
				}
			}
		});
        soundswitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChange(boolean isChecked) {
				if (isChecked) {
		    		editor = preferences.edit();
					editor.putBoolean("soundsetting", isChecked);
					editor.commit();
				} else {
		    		editor = preferences.edit();
					editor.putBoolean("soundsetting", isChecked);
					editor.commit();
				}
			}
		});
        lightswitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChange(boolean isChecked) {
				if (isChecked) {
		    		editor = preferences.edit();
					editor.putBoolean("lightsetting", isChecked);
					editor.commit();
				} else {
		    		editor = preferences.edit();
					editor.putBoolean("lightsetting", isChecked);
					editor.commit();
				}
			}
		});
        baiduswitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChange(boolean isChecked) {
				if (isChecked) {
		    		editor = preferences.edit();
					editor.putBoolean("baidusetting", isChecked);
					editor.commit();
				} else {
		    		editor = preferences.edit();
					editor.putBoolean("baidusetting", isChecked);
					editor.commit();
				}
			}
		});
        smsswitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChange(boolean isChecked) {
				if (isChecked) {
		    		editor = preferences.edit();
					editor.putBoolean("smssetting", isChecked);
					editor.commit();
				} else {
		    		editor = preferences.edit();
					editor.putBoolean("smssetting", isChecked);
					editor.commit();
				}
			}
		});
        contactnumber1search.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
		        Uri uri = Uri.parse("content://contacts/people"); 
		        Intent intent = new Intent(Intent.ACTION_PICK, uri);		        
		        startActivityForResult(intent, PICK_CONTACT_SUBACTIVITY);
		        searchbuttonnumber=1;
			}        	
        });
        contactnumber2search.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
		        Uri uri = Uri.parse("content://contacts/people"); 
		        Intent intent = new Intent(Intent.ACTION_PICK, uri);		        
		        startActivityForResult(intent, PICK_CONTACT_SUBACTIVITY);
		        searchbuttonnumber=2;
			}        	
        });
        contactnumber3search.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
		        Uri uri = Uri.parse("content://contacts/people"); 
		        Intent intent = new Intent(Intent.ACTION_PICK, uri);		        
		        startActivityForResult(intent, PICK_CONTACT_SUBACTIVITY);
		        searchbuttonnumber=3;
			}        	
        });
        resettodefault.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				temperaturesetting=50;
				altitudesetting=28;
				accsetting=40;
				temperaturesettingseekbar.setProgress(temperaturesetting);
				altitudesettingseekbar.setProgress(altitudesetting);
				accsettingseekbar.setProgress(accsetting);
				DecimalFormat df1 = new DecimalFormat("##.0");
			    temperaturenumber.setText(df1.format(30+temperaturesetting*0.4)+"℃");
			    DecimalFormat df2 = new DecimalFormat("0.00");
				altitudenumber.setText(df2.format(altitudesetting*2.5/100+0.50)+"m");
				DecimalFormat df3 = new DecimalFormat("00.0");
				accnumber.setText(df3.format(accsetting*0.25+5)+"m/s^2");
				editor = preferences.edit();
				editor.putInt("temperaturesetting", temperaturesetting);
				editor.putInt("altitudesetting", altitudesetting);
				editor.putInt("accsetting", accsetting);
				editor.commit();
			}
        	
        });
        contactnumberconfirm.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				contact1=contactnumber1.getText().toString();
				contact2=contactnumber2.getText().toString();
				contact3=contactnumber3.getText().toString();
				int count=0;
	    		editor = preferences.edit();
				editor.putString("contact1", contact1);
				editor.putString("contact2", contact2);
				editor.putString("contact3", contact3);
				editor.commit();
				if(contact1!=null&&!contact1.equals("")) count++;
				if(contact2!=null&&!contact2.equals("")) count++;
				if(contact3!=null&&!contact3.equals("")) count++;
				  Toast.makeText(AlertSettings.this,   
						  "您共保存了"+ count +"个有效号码",   
  	                        Toast.LENGTH_SHORT).show(); 			
			}      	
        });
        temperaturesettingseekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if(progress==100) {
					temperaturenumber.setText("  关闭");
				}else{
					DecimalFormat df = new DecimalFormat("##.0");
				    temperaturenumber.setText(df.format(30+progress*0.4)+"℃");
				}
				temperaturesetting=progress;
	    		editor = preferences.edit();
				editor.putInt("temperaturesetting", temperaturesetting);
				editor.commit();
				if(temperaturesetting==100&&altitudesetting==100&&accsetting==100)
					serviceswitch.changbuttonstatues(false);
				else serviceswitch.changbuttonstatues(true);
				}
			public void onStartTrackingTouch(SeekBar seekBar) {}
			public void onStopTrackingTouch(SeekBar seekBar) { }     	
        });
        altitudesettingseekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if(progress==100) {
					altitudenumber.setText("  关闭");
				}else{
					DecimalFormat df = new DecimalFormat("0.00");
					altitudenumber.setText(df.format(progress*2.5/100+0.50)+"m");
				}
				altitudesetting=progress;
	    		editor = preferences.edit();
				editor.putInt("altitudesetting", altitudesetting);
				editor.commit();
				if(temperaturesetting==100&&altitudesetting==100&&accsetting==100)
					serviceswitch.changbuttonstatues(false);
				else serviceswitch.changbuttonstatues(true);
			}
			public void onStartTrackingTouch(SeekBar seekBar) {		}
			public void onStopTrackingTouch(SeekBar seekBar) {	   }   	
        });
        accsettingseekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if(progress==100) {
					accnumber.setText("          关闭");
				}else{
					DecimalFormat df = new DecimalFormat("00.0");
					accnumber.setText(df.format(progress*0.25+5)+"m/s^2");
				}
				accsetting=progress;
	    		editor = preferences.edit();
				editor.putInt("accsetting", accsetting);
				editor.commit();
				if(temperaturesetting==100&&altitudesetting==100&&accsetting==100)
					serviceswitch.changbuttonstatues(false);
				else serviceswitch.changbuttonstatues(true);
			}
			public void onStartTrackingTouch(SeekBar seekBar) {  }
			public void onStopTrackingTouch(SeekBar seekBar) {  }
        });
        showlist.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				// TODO Auto-generated method stub
				final Dialog dialog = new Dialog(AlertSettings.this, R.style.MyDialog);
                dialog.setContentView(R.layout.line);
                dialog.show();
                Window window = dialog.getWindow();
                final ListView list=(ListView)window.findViewById(R.id.show);
                try{
				Cursor cursor = db.rawQuery("select * from alertrecord"
						, null);	
				SimpleCursorAdapter adapter = new SimpleCursorAdapter(
						AlertSettings.this,
						R.layout.listshow, cursor,
						new String[] { "date", "latitude","longitude","type","alert","address" }
						, new int[] {R.id.timeshow, R.id.latitudeshow, R.id.longitudeshow,R.id.typeshow,R.id.alertshow,R.id.addressshow},
						CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER); //③
					// 显示数据
					list.setAdapter(adapter);}catch (Exception e){
						Toast.makeText(getApplicationContext(), "无数据", Toast.LENGTH_SHORT);
					}
			}
        	
        });
        deletelist.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				// TODO Auto-generated method stub
				try{
				db.execSQL("delete from alertrecord");}
				catch (Exception e){
					Toast.makeText(getApplicationContext(), "无数据", Toast.LENGTH_SHORT);
				}
			}       	
        });
		if(!isMyServiceRunning()&&servicesetting)
	    	startService(new Intent(this, AlertService.class));	
	}
	public void getWeiboInfo() {
		// 根据http://open.weibo.com/wiki/2/users/show提供的API文档
		new Thread() {
			@Override
			public void run() {
				try {
					obj = new JSONObject(json);
					Map<String, String> params = new HashMap<String, String>();
					if (obj != null) {
						params.put("access_token", obj.getJSONObject("weibo")
								.getString("access_token"));// 此为微博登陆成功之后返回的access_token
						params.put("uid",
								obj.getJSONObject("weibo").getString("uid"));
						String expires=obj.getJSONObject("weibo").getString("expires_in");
						editor = preferences.edit();
						editor.putString("weibo_expire", expires);
						editor.commit();
					}
					String result = NetUtils.getRequest(
							"https://api.weibo.com/2/users/show.json", params);
					Log.d("login", "微博的个人信息：" + result);	
					Message msg = new Message();
					msg.what=0x123;
					msg.obj = result;
					handler.sendMessage(msg);

				} catch (Exception e) {
					// TODO: handle exception
				}
			}

		}.start();
	}
    class download implements Runnable{
        
        @Override
        public void run() {
            // TODO Auto-generated method stub
            String path = downloadpath;
            try {
                 
                URL url = new URL(path);
                HttpURLConnection con = (HttpURLConnection)url.openConnection();
                con.setConnectTimeout(5000);
                con.setRequestMethod("GET");
                con.connect();
                 
                if (con.getResponseCode() == 200){          
                    InputStream is = con.getInputStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(is);
                    handler.obtainMessage(0x124,bitmap).sendToTarget();
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
             
        }
    }


	protected void onDestory(){
		super.onDestroy();
	}
	protected void onPause(){
		super.onPause();
	}
	protected void onResume() {
        super.onResume();
        configbottom.setClickable(false);
        Intent intent= new Intent();
        intent.setClass(AlertSettings.this, AlertService.class);
    	stopService(intent);
	}
	protected void onActivityResult 
	  (int requestCode, int resultCode, Intent data) 
	  { 
         if(data!=null){
	    switch (requestCode) 
	    {  
	      case PICK_CONTACT_SUBACTIVITY: 
	        final Uri uriRet = data.getData(); 
	        if(uriRet != null) 
	        { 
	          try 
	          {
	           @SuppressWarnings("deprecation")
			Cursor c = managedQuery(uriRet, null, null, null, null);
	           c.moveToFirst();
	           String strPhone = "";
	           int contactId = c.
	             getInt(c.getColumnIndex(ContactsContract.Contacts._ID));
	           Cursor curContacts = getContentResolver().
	             query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
	                 null,ContactsContract.CommonDataKinds.Phone.
	                   CONTACT_ID +" = "+ contactId,null, null);
	           if(curContacts.getCount()>0)
	           {
	             curContacts.moveToFirst();
	             strPhone = curContacts.getString(curContacts.
	                 getColumnIndex(ContactsContract.CommonDataKinds.
	                                  Phone.NUMBER));
	           }
	           else
	           {
	           }
	           switch(searchbuttonnumber){
	           case 1:contactnumber1.setText(strPhone);break;
	           case 2:contactnumber2.setText(strPhone);break;
	           case 3:contactnumber3.setText(strPhone);break;
	           }
	          } 
	          catch(Exception e) 
	          {             
	            e.printStackTrace(); 
	          } 
	        } 
	        break; 
	    } }
	    super.onActivityResult(requestCode, resultCode, data);    
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_about) {
        	LayoutInflater layoutInflater = LayoutInflater.from(this);
        View mySaveView = layoutInflater.inflate(R.layout.about, null);     
        Dialog alertDialog = new android.app.AlertDialog.Builder(AlertSettings.this).
            setTitle("About Developer").
            setView(mySaveView).
            setPositiveButton("OK", null).
            create();
           alertDialog.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    
}
