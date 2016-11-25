package org.linphone;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import jcifs.ntlmssp.NtlmFlags;
import jcifs.ntlmssp.Type1Message;
import jcifs.ntlmssp.Type2Message;
import jcifs.ntlmssp.Type3Message;
import jcifs.util.Base64;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthSchemeFactory;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.NTLMEngine;
import org.apache.http.impl.auth.NTLMEngineException;
import org.apache.http.impl.auth.NTLMScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import android.util.Log;

import org.linphone.QuEws.NTLMSchemeFactory;

public class QuEws{
	final String LOG_TAG = "QuEWS";
    static int serverPort = 443;//init
    static String serverProtocol = "https";//init
    String workstation = "";//init
    String domain = "";//init

	public static String query(String id, String pw, String server, String requestAbsoluteUrl, String requestString){
		String requestData = "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:t=\"http://schemas.microsoft.com/exchange/services/2006/types\">\n"
		+ "<soap:Body>\n"
        + "<ResolveNames xmlns=\"http://schemas.microsoft.com/exchange/services/2006/messages\" xmlns:t=\"http://schemas.microsoft.com/exchange/services/2006/types\" ReturnFullContactData=\"true\">\n" 
		+ "<UnresolvedEntry>"+requestString+"</UnresolvedEntry>\n"
        + "</ResolveNames>\n"
		+ "</soap:Body>\n"
        + "</soap:Envelope>";
		String result="";
		try {
			//sendRequestUsingNtlmAuthentication(METHOD_NAME, myID, myPassword, "","", server, serverPort, serverProtocol, requestAbsoluteUrl, body);
        	result = sendRequestUsingNtlmAuthentication( id, pw, requestAbsoluteUrl, requestData);
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
    
	/* LIVEHD-SSteven, 2014/04/24, create javax.net.ssl.SSLSocketFactory to use by HttpUrlConnection <-- */
	public static javax.net.ssl.SSLSocketFactory getSSLSocketFactory() throws Exception {
		final java.security.cert.X509Certificate[] _AcceptedIssuers = new java.security.cert.X509Certificate[] {};
		try {
			SSLContext ctx = SSLContext.getInstance("TLS");
			X509TrustManager tm = new X509TrustManager() {
				@Override
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return _AcceptedIssuers;
				}
		
				@Override
				public void checkClientTrusted(
						java.security.cert.X509Certificate[] chain,
						String authType)
						throws java.security.cert.CertificateException {
					// TODO Auto-generated method stub
					
				}
				@Override
				public void checkServerTrusted(
						java.security.cert.X509Certificate[] chain,
						String authType)
						throws java.security.cert.CertificateException {
					// TODO Auto-generated method stub
					
				}
			};
			
			ctx.init(null, new TrustManager[]{tm}, new SecureRandom());
			return ctx.getSocketFactory();
		} catch (Exception e) {
			throw e;
		}
	}
	/* LIVEHD-SSteven, 2014/04/24, create javax.net.ssl.SSLSocketFactory to use by HttpUrlConnection --> */
    public static DefaultHttpClient getSecuredHttpClient(HttpClient httpClient) throws Exception {
		final java.security.cert.X509Certificate[] _AcceptedIssuers = new java.security.cert.X509Certificate[] {};
		try {
			SSLContext ctx = SSLContext.getInstance("TLS");
			X509TrustManager tm = new X509TrustManager() {
				@Override
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return _AcceptedIssuers;
				}
		
				@Override
				public void checkClientTrusted(
						java.security.cert.X509Certificate[] chain,
						String authType)
						throws java.security.cert.CertificateException {
					// TODO Auto-generated method stub
					
				}
				@Override
				public void checkServerTrusted(
						java.security.cert.X509Certificate[] chain,
						String authType)
						throws java.security.cert.CertificateException {
					// TODO Auto-generated method stub
					
				}
			};
			
			ctx.init(null, new TrustManager[]{tm}, new SecureRandom());
			SSLSocketFactory ssf = new MySSLSocketFactory(ctx);
			ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			ClientConnectionManager ccm = httpClient.getConnectionManager();
			SchemeRegistry sr = ccm.getSchemeRegistry();
			sr.register(new Scheme(serverProtocol, ssf, serverPort));

			return new DefaultHttpClient(ccm, httpClient.getParams());
		} catch (Exception e) {
			throw e;
		}
	}

    private static String sendRequestUsingNtlmAuthentication(String username, String password, String requestAbsoluteUrl, String requestData) throws Throwable
	{
		System.out.println("sendRequestUsingNtlmAuthentication");
		//Log.i("BODY", "id="+username+";pw="+password);
		Log.i("BODY", requestData);

		DefaultHttpClient httpClient = getSecuredHttpClient(new DefaultHttpClient());
			
		httpClient.getAuthSchemes().register("ntlm", new NTLMSchemeFactory());
		httpClient.getCredentialsProvider().setCredentials(
                // Limit the credentials only to the specified domain and port
                new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
                // Specify credentials, most of the time only user/pass is needed
                new NTCredentials(username, password, "", "")//NTCredentials(username, password, host, domain)
        );

		String result = sendRequest(httpClient, requestData, requestAbsoluteUrl, null);

		return result;
	}

	private static String sendRequest(DefaultHttpClient httpClient, String requestContent, String serviceUrl, String authHeaderValue) throws Throwable 
	{
		// initialize HTTP post
		System.out.println("sendRequest");

		HttpPost httpPost = null;
		try {
			httpPost = new HttpPost(serviceUrl);
			httpPost.addHeader("Content-Type", "text/xml; charset=utf-8");
			httpPost.addHeader("Accept", "text/xml");
			httpPost.addHeader("User-Agent", "ExchangeServicesClient/14.03.0067.001");
			httpPost.addHeader("Accept-Encoding", "gzip,deflate");
			httpPost.setHeader("SOAPAction", "ResolveNames");
			//httpPost.setEntity(new UrlEncodedFormEntity(new ArrayList<NameValuePair>(), HTTP.UTF_8));
			if (authHeaderValue != null)
			{
				//httpPost.setHeader("Authorization", authHeaderValue);
			}
		} catch (Throwable e) {
			throw e;
		}
		/*
		System.out.println("------------------Post header------------------");
		for (int i = 0 ; i < httpPost.getAllHeaders().length ; i++)
		{
			System.out.println("" + httpPost.getAllHeaders()[i].toString());
		}
		System.out.println("------------------Post header End------------------");
		*/
		// load content to be sent
		try {
			//System.out.println("set request content:\n" + requestContent);
			HttpEntity postEntity = new StringEntity(requestContent, HTTP.UTF_8);
			httpPost.setEntity(postEntity);
		} catch (UnsupportedEncodingException e) {
			throw e;
		}

		// send request
		HttpResponse httpResponse = null;
		try {
			//System.out.println("execute Http");
			httpResponse = httpClient.execute(httpPost);
		} catch (Throwable e) {
			System.out.println("Error:"+e.toString());
			throw e;
		}

		// get SOAP response
		try {
			// get response code
			int responseStatusCode = httpResponse.getStatusLine().getStatusCode();

			// if the response code is not 200 - OK, or 500 - Internal error,
			// then communication error occurred
			if (responseStatusCode != 200 && responseStatusCode != 500) {
				String errorMsg = "Error:" + responseStatusCode + " "
						+ httpResponse.getStatusLine().getReasonPhrase();

				System.out.println(errorMsg);
			}
			String reMsg = "Got SOAP response code:" + responseStatusCode + " "
					+ httpResponse.getStatusLine().getReasonPhrase();
			System.out.println(reMsg);
			if( responseStatusCode == 500) {
				return "500";
			}
			/*
			String respondContent = "";
			respondContent = EntityUtils.toString(httpResponse.getEntity(), HTTP.UTF_8);
			Log.i("RESPONSE", respondContent);
			*/
			InputStream is = httpResponse.getEntity().getContent();
			GZIPInputStream gzin= new GZIPInputStream(is);
			BufferedReader buf = new BufferedReader(new InputStreamReader(gzin, "UTF-8"));
			String xmldata = buf.readLine(); 
			//Log.i("RESPONSE", xmldata);
			return xmldata;
		} catch (Throwable e) {
			System.out.println("Error:"+e.toString());
			throw e;
		}
	}

    public static class MySSLSocketFactory extends SSLSocketFactory {
        SSLContext sslContext = SSLContext.getInstance("TLS");

        public MySSLSocketFactory(SSLContext context) throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException {
           super(null);
           sslContext = context;
        }

        @Override
        public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
            return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
        }

        @Override
        public Socket createSocket() throws IOException {
            return sslContext.getSocketFactory().createSocket();
        }
	}

    public static class JCIFSEngine implements NTLMEngine {

        private static final int TYPE_1_FLAGS = NtlmFlags.NTLMSSP_NEGOTIATE_56
                | NtlmFlags.NTLMSSP_NEGOTIATE_128
                | NtlmFlags.NTLMSSP_NEGOTIATE_NTLM2
                | NtlmFlags.NTLMSSP_NEGOTIATE_ALWAYS_SIGN
                | NtlmFlags.NTLMSSP_REQUEST_TARGET;

        @Override
        public String generateType1Msg(String domain, String workstation)
                throws NTLMEngineException {
            final Type1Message type1Message = new Type1Message(TYPE_1_FLAGS,
                    domain, workstation);
            return Base64.encode(type1Message.toByteArray());
        }

        @Override
        public String generateType3Msg(String username, String password,
                String domain, String workstation, String challenge)
                throws NTLMEngineException {
            Type2Message type2Message;

            try {
                type2Message = new Type2Message(Base64.decode(challenge));
            } catch (final IOException exception) {
                throw new NTLMEngineException("Error in type2 message", exception);
            }

            final int type2Flags = type2Message.getFlags();
            final int type3Flags = type2Flags
                    & (0xffffffff ^ (NtlmFlags.NTLMSSP_TARGET_TYPE_DOMAIN | NtlmFlags.NTLMSSP_TARGET_TYPE_SERVER));
            final Type3Message type3Message = new Type3Message(type2Message,
                    password, domain, username, workstation, type3Flags);
            return Base64.encode(type3Message.toByteArray());
        }
    }

    
	public static class NTLMSchemeFactory implements AuthSchemeFactory
	{
	    @Override
	    public AuthScheme newInstance(HttpParams params)
	    {
	        return new NTLMScheme(new JCIFSEngine());
	    }
	}
}