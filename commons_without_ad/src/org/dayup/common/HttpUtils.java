/**
 * 
 */
package org.dayup.common;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;

import android.util.Log;

/**
 * @author dato
 *
 */
public class HttpUtils {
	private static final String tag = "HttpUtils";
	
	public static String doHttpGet(String url) {
		HttpClient httpclient = new DefaultHttpClient();
		HttpProtocolParams.setUserAgent(httpclient.getParams(), 
                "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.6; en-US; rv:1.9.2.12) Gecko/20101026 Firefox/3.6.12");
		//httpclient.getParams().setParameter("http.conn-manager.timeout", 20*1000L);
		//HttpConnectionParams.setConnectionTimeout(httpclient.getParams(), 20*1000);
		//HttpConnectionParams.setSoTimeout(httpclient.getParams(), 20*1000);
		HttpGet httpget = new HttpGet(url);
		
		Log.i(tag, "httpGet:  "+url);
		HttpResponse response;
		try {
			response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				return EntityUtils.toString(entity);
			}
		} catch (ClientProtocolException e) {
			Log.e(tag, "",e);
		} catch (IOException e) {
			Log.e(tag, "",e);
		} finally{
			httpclient.getConnectionManager().shutdown();
		}
		return "";
	}
}
