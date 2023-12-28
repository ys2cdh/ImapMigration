package com.funnysalt.service;

import util.StreamUtil;

import javax.net.ssl.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImapWork {

	private static Pattern cmdPatternFETCH = Pattern.compile("BODY[\\s\\[\\]\\{]*([\\p{Digit}]*)", Pattern.CASE_INSENSITIVE);
    private Socket m_Socket;
	//imap command index
	private int nCommondIndex;
	private OutputStream out = null;
	private InputStream in = null;

	private String mBoxName;

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

	public boolean select(String mBoxName) throws Exception{
		StreamUtil.writeString(out, nCommondIndex + " SELECT \"" + mBoxName + "\"\r\n");

		String strTemp = "";
		while (true)
		{
			strTemp = StreamUtil.readLineString(in);

			if (strTemp.startsWith("*"))
			{

			}
			else if (nCommondIndex == Integer.parseInt(strTemp.split(" ")[0]))
			{
				nCommondIndex++;
				if (-1 >= strTemp.toUpperCase().indexOf("OK"))
				{
					return false;
				}
				break;
			}
			else
			{
				return false;
			}
		}
		this.mBoxName = mBoxName;
		return true;
	}
	//테스트용 코드
	private ArrayList<Long> getAllUIDs() throws Exception{
		StreamUtil.writeString(out, nCommondIndex + " UID FETCH 1:* (UID FLAGS)\r\n");

		String strTemp = "";
		ArrayList<Long> aryUIDs = null;
		aryUIDs = new ArrayList<Long>();
		while (true)
		{
			strTemp = StreamUtil.readLineString(in);

			if (strTemp.startsWith("*"))
			{
				String[] strUIDS = strTemp.split(" ");
				aryUIDs.add(Long.parseLong(strUIDS[4].replace("\r\n", "").trim()));

			}
			else if (nCommondIndex == Integer.parseInt(strTemp.split(" ")[0]))
			{
				nCommondIndex++;
				if (-1 >= strTemp.toUpperCase().indexOf("OK"))
				{
					return aryUIDs;
				}
				break;
			}
			else
			{
				return aryUIDs;
			}
		}
		System.out.println(aryUIDs);
		return aryUIDs;

	}

	public ArrayList<Long>  getAfterFewTimes(long startUID) throws Exception{
		StreamUtil.writeString(out, nCommondIndex + " UID FETCH "+ startUID+":* (UID FLAGS)\r\n");

		String strTemp = "";
		ArrayList<Long> aryUIDs = new ArrayList<Long>();
		while (true)
		{
			strTemp = StreamUtil.readLineString(in);

			if (strTemp.startsWith("*"))
			{
				String[] strUIDS = strTemp.split(" ");
				if (4 < strUIDS.length) {
					aryUIDs.add(Long.parseLong(strUIDS[4].replace("\r\n", "")));
				}
//				for (int i = 2; i < strUIDS.length; i++)
//				{
//					aryUIDs.add(Long.parseLong(strUIDS[i].replace("\r\n", "")));
//				}
			}
			else if (nCommondIndex == Integer.parseInt(strTemp.split(" ")[0]))
			{
				nCommondIndex++;
				if (-1 >= strTemp.toUpperCase().indexOf("OK"))
				{
					return aryUIDs;
				}
				break;
			}
			else
			{
				return aryUIDs;
			}
		}
		System.out.println(aryUIDs);
		return aryUIDs;
	}

	public boolean downloadEml(String strDownloadPath,long uid) throws Exception {
		FileOutputStream fileout = null;
		File file = null;
		try {
			StreamUtil.writeString(out, nCommondIndex + " UID FETCH " + uid + " (FLAGS UID BODY.PEEK[])\r\n");
//			addLogString(m_nCommondIndex + " UID FETCH " + nExternalUID + " (FLAGS UID BODY.PEEK[])\r\n");
			String strTemp = "";
			String strTempEmail = strDownloadPath + "/" + nCommondIndex + ".eml";
			file = new File(strTempEmail);

			byte[] buf = new byte[81920];
			boolean bSeen = false;

			long nSize = 0;
			fileout = new FileOutputStream(file);
			while (true) {
				if (0 == nSize) {
					strTemp = StreamUtil.readLineString(in);
				} else {
					long nReadSize = (buf.length > nSize) ? nSize : buf.length;
					int nReadSize1 = in.read(buf, 0, (int) nReadSize);
					fileout.write(buf, 0, (int) nReadSize1);
					nSize -= nReadSize1;
					// System.out.println(nSize + " Read Size : " + nReadSize +
					// " Read1 Size " + nReadSize1);
					continue;
				}
//				addLogString(strTemp);
				String[] strCommands = strTemp.split(" ");
				Matcher m = cmdPatternFETCH.matcher(strTemp);
				if (2 <= strCommands.length && 0 == String.valueOf(nCommondIndex).compareTo(strCommands[0]) && (strCommands[1].equalsIgnoreCase("OK") || strCommands[1].equalsIgnoreCase("NO") || strCommands[1].equalsIgnoreCase("BAD"))) {
					nCommondIndex++;
					//하나의 파일이 다운로드가 완료된 후
					if (strCommands[1].equalsIgnoreCase("OK") && 0 < file.length()) {


					}

					break;
				} else if (strTemp.startsWith("*")) {
					bSeen = -1 < strTemp.toUpperCase().indexOf("\\SEEN");
					if (true == m.find()) {
						nSize = Long.parseLong(m.group(1));
					}
					// System.out.println(nSize);
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			if (null != fileout) {
				fileout.close();
			}
		}
		return true;
	}

	//테스트용 코드
	private boolean downloadEml(String strDownloadPath,ArrayList<Long> aryUIDs) throws Exception {

		for ( Long uid : aryUIDs ) {
			downloadEml(strDownloadPath,uid);
		}

		return true;
	}

	public void uploadEml(File f) throws Exception{

		FileInputStream fileIn = null;

		try {
			StreamUtil.writeString(out, nCommondIndex + " APPEND \"" + mBoxName + "\" (\\Seen) {" + f.length() + "}\r\n");

			String strTemp = StreamUtil.readLineString(in);

			if (!strTemp.toLowerCase().startsWith("+")){
				return;
			}

			byte[] buf = new byte[81920];
			boolean bSeen = false;

			int nReadSize = 0;
			fileIn = new FileInputStream(f);
			while (0 < (nReadSize = fileIn.read(buf)) ) {
				out.write(buf,0,nReadSize);
			}
			StreamUtil.writeString(out,"\r\n");

			strTemp = StreamUtil.readLineString(in);
			if (nCommondIndex == Integer.parseInt(strTemp.split(" ")[0]))
			{
				nCommondIndex++;
				fileIn.close();
				fileIn=null;
				f.renameTo(new File(f.getAbsolutePath()+"cfg"));
//					if (-1 >= strTemp.toUpperCase().indexOf("OK"))
//					{
//						return aryUIDs;
//					}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != fileIn) {
				fileIn.close();
			}
		}
	}

	public void uploadEml(String strUploadPath) throws Exception {

		File[] files = new File(strUploadPath).listFiles(new FileFilter() {
			public boolean accept(File f)
			{
				return f.getName().endsWith("eml");
			}
		});
		for ( File f : files) {


			FileInputStream fileIn = null;

			try {
				StreamUtil.writeString(out, nCommondIndex + " APPEND \"" + mBoxName + "\" (\\Seen) {" + f.length() + "}\r\n");

				String strTemp = StreamUtil.readLineString(in);

				if (!strTemp.toLowerCase().startsWith("+")){
					continue;
				}

				byte[] buf = new byte[81920];
				boolean bSeen = false;

				int nReadSize = 0;
				fileIn = new FileInputStream(f);
				while (0 < (nReadSize = fileIn.read(buf)) ) {
					out.write(buf,0,nReadSize);
				}
				StreamUtil.writeString(out,"\r\n");

				strTemp = StreamUtil.readLineString(in);
				if (nCommondIndex == Integer.parseInt(strTemp.split(" ")[0]))
				{
					nCommondIndex++;
					fileIn.close();
					fileIn=null;
					f.renameTo(new File(f.getAbsolutePath()+"cfg"));
//					if (-1 >= strTemp.toUpperCase().indexOf("OK"))
//					{
//						return aryUIDs;
//					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (null != fileIn) {
					fileIn.close();
				}
			}
		}
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
