package com.ireader.util.local;

import com.ireader.util.ByteUtil;

/**
 * Created by zxsted on 15-8-20.、
 *
 *
 * 对字节数组进行封装 ， 方便读取
 */
public class ByteArray {

    byte[] bytes;
    int offset;       // 偏移量

    public ByteArray(byte[] bytes){
        this.bytes = bytes;
    }

    /**
     *  从文件中读取一个字节数组
     * */
    public static ByteArray createByteArray(String path)
    {
        byte[] bytes = IOUtil.readBytes(path);   //
        if (bytes == null) return null;
        return new ByteArray(bytes);
    }


    /**
     *  获取读到的全部字节
     *
     * */
    public byte[] getBytes()
    {
        return bytes;
    }

    /**
     *  读取一个Int
     * */
    public int nextInt()
    {
        int result = ByteUtil.bytesHighFirstToInt(bytes,offset);
        offset+=4;      // 读取后更改偏移量
        return result;
    }

    public double nextDouble()
    {
        double result = ByteUtil.bytesHighFirstToDouble(bytes, offset);
        offset += 8;
        return result;
    }

    /**
     *  读取一个char, 对应与writeChar
     * */
    public char nextChar()
    {
        char result = ByteUtil.bytesHighFirstToChar(bytes, offset);
        offset += 2;
        return result;
    }

    /**
     *  读取一个字节
     * */
    public byte nextByte()
    {
        return bytes[offset++];
    }

    public boolean hasMore()
    {
        return offset < bytes.length;
    }

    /**
     * 读取一个String, 注意这个String是双字节的，在字符前面有一个整形表示长度
     * */
    public String nextString()
    {
        StringBuilder sb = new StringBuilder();
        int length = nextInt();
        for(int i = 0 ; i < length; ++i)
        {
            sb.append(nextChar());
        }
        return sb.toString();
    }

    public float nextFloat()
    {
        float result = ByteUtil.bytesHighFirstToFloat(bytes,offset);
        offset += 4;
        return result;
    }

    /**
     *  读取一个无符号整形
     * */
    public int nextUnisignedShort()
    {
        byte a = nextByte();
        byte b = nextByte();
        return (((a & 0xff) << 8) | (b & 0xff));
    }

    /**
     *  读取一个UTF字符串
     * */
    public String nextUTF()
    {
        int utflen = nextUnisignedShort();
        byte[] bytearr = null;
        char[] chararr = null;
        bytearr = new byte[utflen];
        chararr = new char[utflen];

        int c,char2,char3;
        int count = 0;
        int chararr_count = 0;

        for(int i = 0; i < utflen; ++i)
        {
            bytearr[i] = nextByte();
        }

        while(count < utflen)
        {
            c = (int) bytearr[count] & 0xff;
            if(c > 127) break;
            count++;
            chararr[chararr_count++] = (char) c;
        }

        while (count < utflen)
        {
            c = (int) bytearr[count] & 0xff;
            switch (c >> 4)
            {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                    /* 0xxxxxxx*/
                    count++;
                    chararr[chararr_count++] = (char) c;
                    break;
                case 12:
                case 13:
                    /* 110x xxxx   10xx xxxx*/
                    count += 2;


                    char2 = (int) bytearr[count - 1];


                    chararr[chararr_count++] = (char) (((c & 0x1F) << 6) |
                            (char2 & 0x3F));
                    break;
                case 14:
                    /* 1110 xxxx  10xx xxxx  10xx xxxx */
                    count += 3;


                    char2 = (int) bytearr[count - 2];
                    char3 = (int) bytearr[count - 1];

                    chararr[chararr_count++] = (char) (((c & 0x0F) << 12) |
                            ((char2 & 0x3F) << 6) |
                            ((char3 & 0x3F) << 0));
                    break;
                default:
                    /* 10xx xxxx,  1111 xxxx */

            }
        }
        // The number of chars produced may be less than utflen
        return new String(chararr, 0, chararr_count);
    }

    public int getOffset()
    {
        return offset;
    }

    public int getLength()
    {
        return bytes.length;
    }
}
