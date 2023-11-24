package com.funnysalt.service;

import util.StreamUtil;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImapWork {

    private Socket m_Socket;
	//imap command index
	private int nCommondIndex;
	private OutputStream out = null;
	private InputStream in = null;

    public boolean connect(String strServer, int nPort, boolean bSSL) {

        boolean bResult = false;
		if (bSSL)
		{
			bResult = connectFakeSSL(strServer,nPort);
		}
		else
		{
			InetSocketAddress socketAddress = new InetSocketAddress(strServer,nPort);
			try
			{
				m_Socket = new Socket();
				m_Socket.connect(socketAddress, 6000);

				bResult = true;
			} catch (IOException e) {
				e.printStackTrace();
				bResult = false;
			}
		}



		//imap recv
		//* OK IMAP4 ready
		try {
			out = m_Socket.getOutputStream();
			in = m_Socket.getInputStream();

			String strRecv = StreamUtil.readLineString(in);
			System.out.println("S : " + strRecv);
		}catch ( Exception	e){
			e.printStackTrace();
		}
		return bResult;
    }


	public boolean auth( String strID, String strPass) {
		try {
			StreamUtil.writeString(out, nCommondIndex + " LOGIN \"" + strID + "\" \"" + strPass + "\"\r\n");
			String strRecv = "";
			while (true){
				try {
					strRecv = StreamUtil.readLineString(in);
					System.out.println("S : " + strRecv);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return false;
				}
				if (false == strRecv.startsWith("*"))
				{
					break;
				}
			}
			if (nCommondIndex != Integer.parseInt(strRecv.split(" ")[0]))
			{
				return false;
			}

			if (!strRecv.split(" ")[1].equalsIgnoreCase("OK"))
			{
				return false;
			}

			nCommondIndex++;
		} catch ( Exception	e){
			e.printStackTrace();
		}
		return true;
	}

	public ArrayList<String> listSync() throws Exception {
		Pattern cmdPatternLIST = Pattern.compile("\\*\\s[\\p{Graph}]*\\s([\\s()\\p{Graph}]*)\\s[\"]([\\p{Graph}]*)[\"]\\s[\"]?([\\p{Alnum}-&@./()\\s\\[\\]]*)[\"]?\r\n", Pattern.CASE_INSENSITIVE);

		StreamUtil.writeString(out, nCommondIndex + " LIST \"\" \"*\"\r\n");
		nCommondIndex++;

		ArrayList<String> aryExternalBoxList = new ArrayList<String>();
		String strTemp = "";

		strTemp = StreamUtil.readLineString(in);
		System.out.print(strTemp);
		if (-1 == strTemp.indexOf("*"))
		{
			StreamUtil.writeString(out,nCommondIndex + " LSUB \"\" \"*\"\r\n");
			strTemp = StreamUtil.readLineString(in);
		}

		while (true)
		{
			System.out.print(strTemp);

			if (strTemp.startsWith("*"))
			{
				if (5 > strTemp.split(" ").length)
				{
					break;
				}

				Matcher m = cmdPatternLIST.matcher(strTemp);

				if (m.matches())
				{
					String strMBoxTemp = m.group(3);

//					System.out.println(strMBoxTemp);
					aryExternalBoxList.add(strMBoxTemp);
				}

			}
			else if (nCommondIndex == Integer.parseInt(strTemp.split(" ")[0]))
			{
				break;
			}
			else
			{
				break;
			}

			strTemp = StreamUtil.readLineString(in);
		}

		return aryExternalBoxList;
	}

	public int getBoxMailCount(String strBoxName) throws Exception{
		StreamUtil.writeString(out, nCommondIndex + " select \"" + strBoxName + "\"\r\n");
		int exitCount = 0;
		String strTemp = "";
		while (true)
		{
			strTemp = StreamUtil.readLineString(in);
//System.out.println(strTemp);
			if (strTemp.startsWith("*") )
			{
				if (strTemp.toUpperCase().endsWith("EXISTS\r\n")) {
					String exists = strTemp.split(" ")[1];
					try {
						exitCount = Integer.parseInt(exists);
						System.out.println(strBoxName + " exiexts " + exitCount);
					} catch (NumberFormatException e) {
						e.printStackTrace();
						return -1;
					}
				}
			}
			else if (nCommondIndex == Integer.parseInt(strTemp.split(" ")[0]))
			{
				nCommondIndex++;
				if (-1 >= strTemp.toUpperCase().indexOf("OK"))
				{
					return -1;
				}
				break;
			}
			else
			{
				return -1;
			}
		}

		return exitCount;
	}

	public int crateMBox(String boxName) throws Exception{
		String strTemp;
		StreamUtil.writeString(out, nCommondIndex + " CREATE \"" + boxName + "\"\r\n");
		while (true)
		{
			strTemp = StreamUtil.readLineString(in);
//System.out.println(strTemp);
			if (strTemp.startsWith("*") )
			{
				System.out.println(strTemp);

			}
			else if (nCommondIndex == Integer.parseInt(strTemp.split(" ")[0]))
			{
				nCommondIndex++;
				if (-1 >= strTemp.toUpperCase().indexOf("OK"))
				{
					return -1;
				}
				break;
			}
			else
			{
				return -1;
			}
		}
		return 1;
	}


	public static class FakeX509TrustManager implements X509TrustManager
	{
	    public boolean isClientTrusted(java.security.cert.X509Certificate[] chain)
	    {
	        return true;
	    }

	    public boolean isServerTrusted(java.security.cert.X509Certificate[] chain)
	    {
	        return true;
	    }

        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

        }

        public java.security.cert.X509Certificate[] getAcceptedIssuers()
	    {
	        return null;
	    }

		@Override
		public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException
		{
		}
	}

    private boolean connectFakeSSL(String strServer, int nPort)
	{
		try
		{
			SSLContext sc = SSLContext.getInstance("TLS");
			SSLSocketFactory sslFactory = null;
	        sc.init(null,
	                    new TrustManager[]{new FakeX509TrustManager()},
	                    new java.security.SecureRandom());
	        sslFactory = sc.getSocketFactory();
	        m_Socket = new Socket();
	        InetSocketAddress socketAddress = new InetSocketAddress(strServer,nPort);
//			 m_Socket.connect(socketAddress, 6000);
//	        sslFactory.createSocket(m_Socket, strServer, nPort, true);
	        m_Socket = sslFactory.createSocket(strServer, nPort);
	        ((SSLSocket)m_Socket).startHandshake();
		}
		catch (SSLHandshakeException e)
		{
			return connectSSL(strServer,nPort);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}


		return true;
	}

	private boolean connectSSL(String strServer, int nPort)
	{

		SSLSocketFactory sslFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();


		try
		{
			m_Socket = sslFactory.createSocket(strServer, nPort);
			((SSLSocket)m_Socket).startHandshake();

		} catch (UnknownHostException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}


	public void close(){
		try {
			if (null != out) out.close();
			out = null;
			if (null != in) in.close();
			in = null;
			m_Socket.close();
		}catch (Exception e){
			e.printStackTrace();
		}


	}
    
}
