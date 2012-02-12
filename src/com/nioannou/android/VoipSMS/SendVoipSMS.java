package com.nioannou.android.VoipSMS;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SendVoipSMS extends Activity {
	SharedPreferences preferences;

	String chosenNumber="";
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);


	    // this opens the activity. note the  Intent.ACTION_GET_CONTENT
	    // and the intent.setType
	    ((Button)findViewById(R.id.PickNumber)).setOnClickListener( new OnClickListener() {
	        @Override
	        public void onClick(View v) {
	            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
	            intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
	            startActivityForResult(intent, 1);                
	        }
	    });

		Button button = (Button) findViewById(R.id.SendButton);
		// Initialize preferences
		preferences = PreferenceManager.getDefaultSharedPreferences(this);

		// Called when the Send button is pressed
		button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Get the parameters from the preferences, should have added them using the menu button
				String voipprovider = preferences.getString("voipprovider", "n/a");
				String username = preferences.getString("username", "n/a");
				String password = preferences.getString("password", "n/a");
				if(voipprovider == "n/a" || username == "n/a" || password == "n/a") {
					/* On any Error we want to display it. */
					Toast.makeText(SendVoipSMS.this, "Not enough credentials provided, please press the menu button and enter the required info", 
								Toast.LENGTH_LONG).show();					
					return;
				}
				EditText text = (EditText) findViewById(R.id.ToNumberText);
				String toNumber = text.getText().toString();
				String fromNumber = preferences.getString("number", "");
				text = (EditText) findViewById(R.id.SMSText);
				String smsText  = text.getText().toString();
				//Toast.makeText(
				//		SendVoipSMS.this,
				//		"You entered user: " + username + " and password: "
				//		+ password + " voip: " + voipprovider + " text: " + smsText, Toast.LENGTH_LONG).show();

				String postURL = "https://www." + voipprovider + ".com/myaccount/sendsms.php";
				Integer nSend = 0;
				
				for (String token: StringUtility.splitEqually(smsText, 159)) {
					Log.e("Log", token);
					try {
						// *** BELOW IS FAILSAFE BETTER VERSION UNCOMMENT IF OTHER FAILS***
						//SchemeRegistry schemeRegistry = new SchemeRegistry();
						//schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
						//schemeRegistry.register(new Scheme("https", new EasySSLSocketFactory(), 443));
						 
						//HttpParams params = new BasicHttpParams();
						//params.setParameter(ConnManagerPNames.MAX_TOTAL_CONNECTIONS, 30);
						//params.setParameter(ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE, new ConnPerRouteBean(30));
						//params.setParameter(HttpProtocolParams.USE_EXPECT_CONTINUE, false);
						//HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
						 
						//ClientConnectionManager cm = new SingleClientConnManager(params, schemeRegistry);
						//DefaultHttpClient httpClient = new DefaultHttpClient(cm, params);
						// ***FAILSAFE CONNECTION ENDS***
						
						// create a secure ssl connection that accepts all certificates (how secure is that :))
						HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
						DefaultHttpClient client = new DefaultHttpClient();
						SchemeRegistry registry = new SchemeRegistry();
						SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();
						socketFactory.setHostnameVerifier((X509HostnameVerifier) hostnameVerifier);
						registry.register(new Scheme("https", socketFactory, 443));
						// single thread for now, no need for thread safe conn manager
						//ThreadSafeClientConnManager mgr = new ThreadSafeClientConnManager (client.getParams(), registry);
						SingleClientConnManager mgr = new SingleClientConnManager(client.getParams(), registry);
						// the actual client
						DefaultHttpClient httpClient = new DefaultHttpClient(mgr, client.getParams());
						// Set verifier     
						HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);

						if(token.charAt(token.length()-1) == ' ') {
							token = token.substring(0, token.length()-1);
						}
						
						// ***HTTP POST STOPPED WORKING - COMMENTED OUT***
						// setup the HTTP POST Request
						//HttpPost post = new HttpPost(postURL);
						//List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(5);
						//nameValuePairs.add(new BasicNameValuePair("username", username));
						//nameValuePairs.add(new BasicNameValuePair("password", password));
						//nameValuePairs.add(new BasicNameValuePair("to", toNumber));
						//nameValuePairs.add(new BasicNameValuePair("from", fromNumber));
						//nameValuePairs.add(new BasicNameValuePair("text", token));
						//post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
						// ***HTTP POST ENDS HERE ***

						String strURL = postURL + "?username="+username+"&password="+password+"&to="+toNumber+"&from="+fromNumber+"&text="+token;
						Log.e("Log",strURL +" " + token.length());
						HttpGet httpGet = new HttpGet(strURL);
						//HttpGet httpGet = new HttpGet();
						//Uri uri = new Uri.Builder()
					    //.scheme("https")
					    //.authority("www."+ voipprovider+".com")
					    //.path("myaccount/sendsms.php")
					    //.appendQueryParameter("username", username)
					    //.appendQueryParameter("password", password)
					    //.appendQueryParameter("to", toNumber)
					    //.appendQueryParameter("from", fromNumber)
					    //.appendQueryParameter("text", token)
					    //.build();
						//httpGet.setURI(new URI(uri.toString()));
						//Log.e("Log",uri.toString() +" " + token.length());
						
						// Execute the request
						HttpResponse response = httpClient.execute(httpGet);
						HttpEntity entity = response.getEntity();

						String responseText = EntityUtils.toString(entity);

						//Toast.makeText(SendVoipSMS.this, "Result" + response.getStatusLine(), Toast.LENGTH_LONG).show();
						Log.e("Error", "Result" + responseText);

						nSend++;
					} catch (Exception e) {
						Log.e("log_tag", "Error in http connection, did you enter the correct user/password ?. error trace: "+e.toString());
						e.printStackTrace ();		        	
						/* On any Error we want to display it. */
						Toast.makeText(SendVoipSMS.this, "Error in sending Voip SMS" + e.getStackTrace(), Toast.LENGTH_LONG).show();
						return;
					}

				}
				Toast.makeText(SendVoipSMS.this, nSend.toString() + " SMS send successfully!", Toast.LENGTH_LONG).show();
				//Toast.makeText(
				//		SendVoipSMS.this,
				//		"Server responed with " + myString, Toast.LENGTH_LONG).show();				
			}
		});                  


	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	// This method is called once the menu is selected
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		// We have only one menu option
		case R.id.preferences:
			// Launch Preference activity
			Intent i = new Intent(SendVoipSMS.this, Preferences.class);
			startActivity(i);
			// Some feedback to the user
			Toast.makeText(SendVoipSMS.this,
					"Here you can enter your user credentials.",
					Toast.LENGTH_LONG).show();
			break;

		}
		return true;
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                Cursor c = null;
                try {
                	c = getContentResolver().query(uri, new String[]{ 
                            ContactsContract.CommonDataKinds.Phone.NUMBER,  
                            ContactsContract.CommonDataKinds.Phone.TYPE },
                        null, null, null);
                	
                    //c = getContentResolver().query(uri, new String[] { BaseColumns._ID },
                    //        null, null, null);
                    if (c != null && c.moveToFirst()) {
                    	// put the number back to the edittext field
                    	String number = c.getString(0); 
                    	EditText text = (EditText) findViewById(R.id.ToNumberText);
                    	text.setText(number);
                    }
                } finally {
                    if (c != null) {
                        c.close();
                    }
                }
            }
        }
    }
	
}