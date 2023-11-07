package util;

import java.io.*;
import java.nio.channels.FileChannel;

public class StreamUtil {
	public static String readLineString(InputStream in) throws Exception {
		return readLineString(in, null);
	}

	public static String readLineString(InputStream in, String encoding) throws Exception {
		byte[] arr = readLineByteArray(in);
//		System.out.println(new String(arr));
		if(arr == null)
			return null;
		if(encoding == null)
			return new String(arr);
		else
			return new String(arr, encoding);
	}

	public static byte[] readLineByteArray(InputStream in) throws Exception {
		ByteArrayOutputStream bOut = new ByteArrayOutputStream();
		int b = -1;
		while((b=in.read()) != -1) {
			bOut.write(b);
			if(b == '\n') {
				break;
			}
		}
		if(bOut.size() == 0)
			return null;
		return bOut.toByteArray();
	}

	public static byte[] readLineByteArray(InputStream in,byte[] byData,boolean bLine) throws Exception {
		if (bLine)
		{
			return readLineByteArray(in);
		}

		int nReadSzie = in.read(byData);
		if ( -1 == nReadSzie )
		{
			return null;
		}
		if (byData.length != nReadSzie)
		{
			byte []byData1 = new byte[nReadSzie] ;
			System.arraycopy(byData,0,byData1,0,nReadSzie);
			return byData1;
		}
		return byData;
	}


	public static OutputStream writeString(OutputStream out, String str) throws Exception {
		System.out.println("C : " + str);
		out.write(str.getBytes());
		out.flush();
		return out;
	}

	public static OutputStream writeString(OutputStream out, String str, String encoding) throws Exception {
		out.write(str.getBytes(encoding));
		return out;
	}

	public static OutputStream writeString(OutputStream out, byte[] data) throws Exception {
		out.write(data);
		return out;
	}

	public static FileInputStream skipBom(File file)
	{
		FileInputStream inputStream = null;
		final byte  bom[] = new byte[3];

		try
		{
			inputStream = new FileInputStream(file);
			PushbackInputStream in = new PushbackInputStream(inputStream,3);
			in.read(bom);
			if ((bom[0] == (byte)0xEF) &&
		            (bom[1] == (byte)0xBB) &&
		            (bom[2] == (byte)0xBF))
			{
				return inputStream;
			}

			in.unread(bom);
			inputStream.close();
			inputStream = new FileInputStream(file);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return inputStream;
	}





	public static void copyFile(String strSourcePath, String strTargetPath) {
		// 복사 대상이 되는 파일 생성
		File sourceFile = new File(strSourcePath);

		// 스트림, 채널 선언
		FileInputStream inputStream = null;
		FileOutputStream outputStream = null;
		FileChannel fcin = null;
		FileChannel fcout = null;

		try {
			// 스트림 생성
			inputStream = new FileInputStream(sourceFile);
			outputStream = new FileOutputStream(strTargetPath);
			// 채널 생성
			fcin = inputStream.getChannel();
			fcout = outputStream.getChannel();

			// 채널을 통한 스트림 전송
			long size = fcin.size();
			fcin.transferTo(0, size, fcout);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// 자원 해제
			try {
				fcout.close();
			} catch (IOException ioe) {
			}
			try {
				fcin.close();
			} catch (IOException ioe) {
			}
			try {
				outputStream.close();
			} catch (IOException ioe) {
			}
			try {
				inputStream.close();
			} catch (IOException ioe) {
			}

		}
	}

}
