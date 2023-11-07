package com.funnysalt.util;


import com.funnysalt.util.beetstra.ModifiedUTF7Charset;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;




public class UTF7Coder extends Charset {
    private static final int MAX_UTF7_CHAR_VALUE = 0x7f;

    protected char BEGIN_SHIFT;
    protected char END_SHIFT;

    protected final byte[] BASE_64 = {
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
            'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f',
            'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
            'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'
        };
    protected final byte INVERSE_BASE_64[] = new byte[128];
    protected static final byte NON_BASE_64 = -1;

    protected final boolean NO_SHIFT_REQUIRED[] = new boolean[128];


    public class UTF7Decoder extends CharsetDecoder {
        private boolean shifted = false, first = false;
        private int decoder = 0, bits = 0;

        protected UTF7Decoder(Charset cs) {
                        super(cs, (float) 0.4, 1);
                }

        protected void implReset() {
            shifted = first = false;
            decoder = bits = 0;
        }

        protected CoderResult implFlush(CharBuffer out) {
            if (shifted && decoder != 0) {
                return CoderResult.malformedForLength(0);
            }
            return CoderResult.UNDERFLOW;
        }

        protected CoderResult decodeLoop(ByteBuffer in, CharBuffer out) {
            while (in.hasRemaining()) {
                if (!out.hasRemaining()) {
                    return CoderResult.OVERFLOW;
                }
                byte c = in.get();
                if (c > MAX_UTF7_CHAR_VALUE) {
                    return CoderResult.malformedForLength(0);
                }
                if (shifted) {
                    byte decodedChar = INVERSE_BASE_64[c];
                    if (decodedChar == NON_BASE_64) {
                        shifted = false;
                        if (first && c == END_SHIFT) {
                            out.put(BEGIN_SHIFT);
                        }
                        if (decoder != 0) {
                            return CoderResult.malformedForLength(0);
                        }
                        bits = 0;
                        if (c == END_SHIFT) {
                            continue;
                        }
                    } else {
                        decoder = (decoder << 6) | decodedChar;
                        first = false;
                        bits += 6;
                        if (bits >= 16) {
                            out.put((char) (decoder >> (bits - 16)));
                            decoder &= ~(0xFFFF << (bits - 16));
                            bits -= 16;
                        }
                    }
                }

                if (!shifted) {
                    if (c == BEGIN_SHIFT) {
                        shifted = first = true;
                    }
                    else {
                        out.put((char) c);
                    }
                }
            }
            return CoderResult.UNDERFLOW;
                }
    }

    public class UTF7Encoder extends CharsetEncoder {
        private boolean shifted = false;
        private int encoder = 0, bits = 0;

        protected UTF7Encoder(Charset cs) {
                        super(cs, (float) 2.5, 5);
                }

        protected void implReset() {
            shifted = false;
            encoder = bits = 0;
        }

        protected CoderResult implFlush(ByteBuffer out) {
            if (shifted) {
                if (out.remaining() < 2) {
                    return CoderResult.OVERFLOW;
                }
                if (bits > 0) {
                    encoder <<= (6-bits);
                    out.put(BASE_64[encoder]);
                    encoder = bits = 0;
                }
                out.put((byte) END_SHIFT);
                shifted = false;
            }
            return CoderResult.UNDERFLOW;
        }

        protected CoderResult encodeLoop(CharBuffer in, ByteBuffer out) {
                        while (in.hasRemaining()) {
                if (out.remaining() < 4) {
                    return CoderResult.OVERFLOW;
                }
                char c = in.get();
                boolean needsShift = c > MAX_UTF7_CHAR_VALUE || !NO_SHIFT_REQUIRED[c];

                if (needsShift && !shifted) {
                    out.put((byte) BEGIN_SHIFT);
                    if (c == BEGIN_SHIFT) {
                        out.put((byte) END_SHIFT);
                    }
                    else {
                        shifted = true;
                    }
                }

                if (shifted) {
                    if (needsShift) {
                        encoder = (encoder << 16) | c;
                        bits += 16;
                        do {
                            out.put(BASE_64[0x3F & (encoder >> (bits-6))]);
                            bits -= 6;
                        } while (bits >= 6);
                        encoder &= (0x3F >> (6-bits));
                    } else {
                        implFlush(out);
                    }
                }

                if (!needsShift) {
                    out.put((byte) c);
                }
            }
            // need to force a flush (sigh)
            // return CoderResult.UNDERFLOW;
            return implFlush(out);
                }
    }

    UTF7Coder(String canonicalName, String[] aliases) {
        super(canonicalName, aliases);

        BEGIN_SHIFT = '&';
        END_SHIFT   = '-';

        for (int i = 0; i < INVERSE_BASE_64.length; i++) {
            INVERSE_BASE_64[i] = NON_BASE_64;
        }
        for (byte i = 0; i < BASE_64.length; i++) {
            INVERSE_BASE_64[BASE_64[i]] = i;
        }

        final String unshifted = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789'(),-./:? \t\r\n";
        for (int i = 0; i < unshifted.length(); i++) {
            NO_SHIFT_REQUIRED[unshifted.charAt(i)] = true;
        }
     }
    
    private static String[] ALIASES = {"UTF7", "UNICODE-1-1-UTF-7", "csUnicode11UTF7", "UNICODE-2-0-UTF-7" };

    public UTF7Coder() {
    	this("UTF-7", ALIASES);
    }

    
    public boolean contains(Charset cs) {
            return true;
    }

    public CharsetDecoder newDecoder() {
            return new UTF7Decoder(this);
    }

    public CharsetEncoder newEncoder() {
            return new UTF7Encoder(this);
    }

    
    public static String e(String str) {
    	UTF7Coder utf7 = new UTF7Coder();
    	String strResult = "";
    	try
    	{
    		// /을 처리
    		if (-1 < str.indexOf("/")) {
    			String[] nameArr = str.split("/");
				for(String splitName : nameArr) {
					strResult += UTF7Coder.e(splitName) + "/";
				}
				strResult = strResult.substring(0,strResult.length()-1);
    		} else {
    			strResult =new String(utf7.encode(str).array()).trim().replaceAll("/", ",");
    		}
    	}
    	catch (Exception e)
    	{
	    	str += " ";
	    	ModifiedUTF7Charset tested = new ModifiedUTF7Charset("X-MODIFIED-UTF-7", new String[] {});
	    	CharsetEncoder encoder = tested.newEncoder();
	    	byte[] bytes = new byte[10240];
	    	ByteBuffer in = ByteBuffer.wrap(bytes);
	    	CharBuffer out = CharBuffer.allocate(str.length());
	    	out = CharBuffer.allocate(str.length()*3);
			out.put(str);
			out.flip();
	    	encoder.encode(out, in, false);
	    	strResult = new String(bytes).trim();
	    	bytes = null;
	    	encoder = null;
	    	tested = null;
    	}
    	return strResult;
    	
    	
    	
    }
    
    public static String d(String str) {
    	UTF7Coder utf7 = new UTF7Coder();
    	String strResult = "";
    	try
    	{
    		strResult = new String(utf7.decode(ByteBuffer.wrap(str.getBytes())).array()).trim();
    	}
    	catch (Exception e)
    	{
    	ModifiedUTF7Charset tested = new ModifiedUTF7Charset("X-MODIFIED-UTF-7", new String[] {});
    	CharsetDecoder decoder = tested.newDecoder();
    	byte[] bytes = null;
    	try
		{
			bytes = str.getBytes("US-ASCII");
		}
		catch (UnsupportedEncodingException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	
		ByteBuffer in = ByteBuffer.wrap(bytes);
		CharBuffer out = CharBuffer.allocate(bytes.length);
		decoder.decode(in, out, true);
		out.flip();
		strResult  = new String(out.toString().trim());
    	}
    	
		return strResult;
//    	UTF7Coder utf7 = new UTF7Coder();
//    	return new String(utf7.decode(ByteBuffer.wrap(str.getBytes())).array()).trim();
    }
    
    public static void main(String[] args) {
    	String ori = "&j,dg0TDhMPww6w-";
    	String enc = e(ori);
    	String dec = d(ori);
    	//System.out.println("'"+ori+"'");
    	//System.out.println("'"+enc+"'");
    	//System.out.println("'"+dec+"'");
    	/*
    	MDUTF7Coder utf7 = new MDUTF7Coder();
    	
    	String tmp = new String(utf7.encode("개 인").array());
    	System.out.println(tmp);
        	
    	
    	String tmp2 = new String(utf7.decode(ByteBuffer.wrap(tmp.getBytes())).array());
    	System.out.println(tmp2);
        */	
     }
    
    
    
    
    
    
}

