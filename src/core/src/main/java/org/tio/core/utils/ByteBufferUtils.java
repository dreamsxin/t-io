/**
 * **************************************************************************
 *
 * @说明: 
 * @项目名称: tio-core
 *
 * @author: tanyaowu 
 * @创建时间: 2016年11月15日 下午3:11:09
 *
 * **************************************************************************
 */
package org.tio.core.utils;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author tanyaowu 
 * @创建时间 2016年11月15日 下午3:11:09
 *
 * @操作列表
 *  编号	| 操作时间	| 操作人员	 | 操作说明
 *  (1) | 2016年11月15日 | tanyaowu | 新建类
 *
 */
public class ByteBufferUtils
{
	private static Logger log = LoggerFactory.getLogger(ByteBufferUtils.class);

	/**
	 * 
	 *
	 * @author: tanyaowu
	 * @创建时间:　2016年11月15日 下午3:11:09
	 * 
	 */
	public ByteBufferUtils()
	{

	}

	/**
	 * 
	 * @param byteBuffer1
	 * @param byteBuffer2
	 * @return
	 *
	 * @author: tanyaowu
	 * @创建时间:　2016年11月16日 上午9:46:57
	 *
	 */
	public static ByteBuffer composite(ByteBuffer byteBuffer1, ByteBuffer byteBuffer2)
	{
		int capacity = (byteBuffer1.limit() - byteBuffer1.position()) + (byteBuffer2.limit() - byteBuffer2.position());
		ByteBuffer ret = ByteBuffer.allocate(capacity);
		//		byteBuffer1.position(0);
		//		byteBuffer2.position(0);

		ret.put(byteBuffer1);
		ret.put(byteBuffer2);

		ret.position(0);
		ret.limit(ret.capacity());
		return ret;
	}

	/**
	 * 
	 * @param src
	 * @param startindex 从0开始
	 * @param endindex 
	 * @return
	 *
	 * @author: tanyaowu
	 * @创建时间:　2016年11月16日 上午9:47:01
	 *
	 */
	public static ByteBuffer copy(ByteBuffer src, int startindex, int endindex)
	{
		int size = endindex - startindex;
		byte[] dest = new byte[size];
		System.arraycopy(src.array(), startindex, dest, 0, dest.length);
		ByteBuffer newByteBuffer = ByteBuffer.wrap(dest);
		return newByteBuffer;
	}

	public static void copy(ByteBuffer src, int srcStartindex, ByteBuffer dest, int destStartIndex, int length)
	{
		System.arraycopy(src.array(), srcStartindex, dest.array(), destStartIndex, length);
	}
	public static byte read(ByteBuffer buffer)
	{
		return buffer.get();
	}

	public static int readUB2(ByteBuffer buffer)
	{
		int ret = buffer.get() & 0xff;
		ret |= (buffer.get() & 0xff) << 8;
		return ret;
	}

	public static int readUB3(ByteBuffer buffer)
	{
		int ret = buffer.get() & 0xff;
		ret |= (buffer.get() & 0xff) << 8;
		ret |= (buffer.get() & 0xff) << 16;
		return ret;
	}

	public static long readUB4(ByteBuffer buffer)
	{
		long ret = (long) (buffer.get() & 0xff);
		ret |= (long) (buffer.get() & 0xff) << 8;
		ret |= (long) (buffer.get() & 0xff) << 16;
		ret |= (long) (buffer.get() & 0xff) << 24;
		return ret;
	}

	public static int readInt(ByteBuffer buffer)
	{

		int i = buffer.get() & 0xff;
		i |= (buffer.get() & 0xff) << 8;
		i |= (buffer.get() & 0xff) << 16;
		i |= (buffer.get() & 0xff) << 24;
		return i;
	}

	public static float readFloat(ByteBuffer buffer)
	{
		return Float.intBitsToFloat(readInt(buffer));
	}

	/**
	 * 8个字节
	 * @param buffer
	 * @return
	 *
	 * @author: tanyaowu
	 * @创建时间:　2017年1月23日 下午3:07:31
	 *
	 */
	public static long readLong(ByteBuffer buffer)
	{

		long l = (long) (buffer.get() & 0xff);
		l |= (long) (buffer.get() & 0xff) << 8;
		l |= (long) (buffer.get() & 0xff) << 16;
		l |= (long) (buffer.get() & 0xff) << 24;
		l |= (long) (buffer.get() & 0xff) << 32;
		l |= (long) (buffer.get() & 0xff) << 40;
		l |= (long) (buffer.get() & 0xff) << 48;
		l |= (long) (buffer.get() & 0xff) << 56;
		return l;
	}

	public static double readDouble(ByteBuffer buffer)
	{
		return Double.longBitsToDouble(readLong(buffer));
	}

	public static byte[] readBytes(ByteBuffer buffer, int length)
	{
		byte[] ab = new byte[length];
		buffer.get(ab);
		return ab;
	}

	public static String readString(ByteBuffer buffer)
	{
		return readString(buffer, null);
	}

	public static String readString(ByteBuffer buffer, String charset)
	{
		if (!buffer.hasRemaining())
		{
			return null;
		}
		int length = buffer.limit() - buffer.position();
		String s = readString(buffer, length, charset);
		return s;
	}

	public static String readStringWithNull(ByteBuffer buffer, String charset)
	{

		if (!buffer.hasRemaining())
		{
			return null;
		}
		int offset = -1;
		int position = buffer.position();
		int length = buffer.limit();
		boolean needPlus1 = true;
		for (int i = position; i < length; i++)
		{
			if (buffer.get(i) == 0)
			{
				offset = i;
				break;
			}
		}
		if (offset == -1)
		{
			needPlus1 = false;
			offset = buffer.limit();
		}
		if (offset > position)
		{
			//			String s = new String(b, position, offset - position);
			//			position = offset + 1;
			int bytelength = offset - buffer.position();
			String s = readString(buffer, bytelength, charset);

			if (needPlus1)
			{
				buffer.position(buffer.position() + 1);
			}

			return s;
		} else
		{
			//			position++;
			buffer.position(buffer.position() + 1);
			return null;
		}
	}

	/**
	 * 读取指定长度的String
	 * @param buffer
	 * @param length
	 * @param charset
	 * @return
	 *
	 * @author: tanyaowu
	 * @创建时间:　2017年1月25日 下午12:12:07
	 *
	 */
	public static String readString(ByteBuffer buffer, int length, String charset)
	{
		int bytelength = length;
		byte[] dst = new byte[bytelength];
		buffer.get(dst, 0, bytelength);
		String s = null;
		if (charset != null)
		{
			try
			{
				s = new String(dst, charset);
			} catch (UnsupportedEncodingException e)
			{
				log.error(e.toString(), e);
				s = new String(dst);
			}
		} else
		{
			s = new String(dst);
		}
		return s;
	}

	public static String readStringWithNull(ByteBuffer buffer)
	{
		return readStringWithNull(buffer, null);
	}

	public static java.sql.Time readTime(ByteBuffer buffer)
	{
		move(6, buffer);
		int hour = read(buffer);
		int minute = read(buffer);
		int second = read(buffer);
		Calendar cal = getLocalCalendar();
		cal.set(0, 0, 0, hour, minute, second);
		return new Time(cal.getTimeInMillis());
	}

	public static java.util.Date readDate(ByteBuffer buffer)
	{
		byte length = read(buffer);
		int year = readUB2(buffer);
		byte month = read(buffer);
		byte date = read(buffer);
		int hour = read(buffer);
		int minute = read(buffer);
		int second = read(buffer);
		if (length == 11)
		{
			long nanos = readUB4(buffer);
			Calendar cal = getLocalCalendar();
			cal.set(year, --month, date, hour, minute, second);
			Timestamp time = new Timestamp(cal.getTimeInMillis());
			time.setNanos((int) nanos);
			return time;
		} else
		{
			Calendar cal = getLocalCalendar();
			cal.set(year, --month, date, hour, minute, second);
			return new java.sql.Date(cal.getTimeInMillis());
		}
	}

	public static void move(int i, ByteBuffer buffer)
	{
		buffer.position(buffer.position() + i);
	}

	public static void position(int i, ByteBuffer buffer)
	{
		buffer.position(i);
	}

	public static final void writeUB2(ByteBuffer buffer, int i)
	{
		buffer.put((byte) (i & 0xff));
		buffer.put((byte) (i >>> 8));
	}

	public static final void writeUB3(ByteBuffer buffer, int i)
	{
		buffer.put((byte) (i & 0xff));
		buffer.put((byte) (i >>> 8));
		buffer.put((byte) (i >>> 16));
	}

	public static final void writeInt(ByteBuffer buffer, int i)
	{
		buffer.put((byte) (i & 0xff));
		buffer.put((byte) (i >>> 8));
		buffer.put((byte) (i >>> 16));
		buffer.put((byte) (i >>> 24));
	}

	public static final void writeFloat(ByteBuffer buffer, float f)
	{
		writeInt(buffer, Float.floatToIntBits(f));
	}

	public static final void writeUB4(ByteBuffer buffer, long l)
	{
		buffer.put((byte) (l & 0xff));
		buffer.put((byte) (l >>> 8));
		buffer.put((byte) (l >>> 16));
		buffer.put((byte) (l >>> 24));
	}

	public static final void writeLong(ByteBuffer buffer, long l)
	{
		buffer.put((byte) (l & 0xff));
		buffer.put((byte) (l >>> 8));
		buffer.put((byte) (l >>> 16));
		buffer.put((byte) (l >>> 24));
		buffer.put((byte) (l >>> 32));
		buffer.put((byte) (l >>> 40));
		buffer.put((byte) (l >>> 48));
		buffer.put((byte) (l >>> 56));
	}

	public static final void writeDouble(ByteBuffer buffer, double d)
	{
		writeLong(buffer, Double.doubleToLongBits(d));
	}

	public static final void writeLength(ByteBuffer buffer, long l)
	{
		if (l < 251)
		{
			buffer.put((byte) l);
		} else if (l < 0x10000L)
		{
			buffer.put((byte) 252);
			writeUB2(buffer, (int) l);
		} else if (l < 0x1000000L)
		{
			buffer.put((byte) 253);
			writeUB3(buffer, (int) l);
		} else
		{
			buffer.put((byte) 254);
			writeLong(buffer, l);
		}
	}

	public static final void writeWithNull(ByteBuffer buffer, byte[] src)
	{
		buffer.put(src);
		buffer.put((byte) 0);
	}

	public static final void writeWithLength(ByteBuffer buffer, byte[] src)
	{
		int length = src.length;
		if (length < 251)
		{
			buffer.put((byte) length);
		} else if (length < 0x10000L)
		{
			buffer.put((byte) 252);
			writeUB2(buffer, length);
		} else if (length < 0x1000000L)
		{
			buffer.put((byte) 253);
			writeUB3(buffer, length);
		} else
		{
			buffer.put((byte) 254);
			writeLong(buffer, length);
		}
		buffer.put(src);
	}

	public static final void writeWithLength(ByteBuffer buffer, byte[] src, byte nullValue)
	{
		if (src == null)
		{
			buffer.put(nullValue);
		} else
		{
			writeWithLength(buffer, src);
		}
	}

	public static final int getLength(long length)
	{
		if (length < 251)
		{
			return 1;
		} else if (length < 0x10000L)
		{
			return 3;
		} else if (length < 0x1000000L)
		{
			return 4;
		} else
		{
			return 9;
		}
	}

	public static final int getLength(byte[] src)
	{
		int length = src.length;
		if (length < 251)
		{
			return 1 + length;
		} else if (length < 0x10000L)
		{
			return 3 + length;
		} else if (length < 0x1000000L)
		{
			return 4 + length;
		} else
		{
			return 9 + length;
		}
	}

	private static final ThreadLocal<Calendar> localCalendar = new ThreadLocal<Calendar>();

	private static final Calendar getLocalCalendar()
	{
		Calendar cal = localCalendar.get();
		if (cal == null)
		{
			cal = Calendar.getInstance();
			localCalendar.set(cal);
		}
		return cal;
	}

}
