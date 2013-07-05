package com.company.foo;

import com.ht.scada.common.tag.util.VarGroupEnum;
import com.ht.scada.communication.entity.VarGroupData;
import org.xerial.snappy.Snappy;

import java.io.*;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class KVTest {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws UnsupportedEncodingException 
	 */
	public static void main(String[] args) throws UnsupportedEncodingException, IOException {
		System.out.println("saf dsaf dsaf".split("\\s").length);
		System.out.println("============");
		
		String input = "Hello snappy-java! Snappy-java is a JNI-based wrapper of "
			     + "Snappy, a fast compresser/decompresser.";
		byte[] compressed = Snappy.compress(input.getBytes("UTF-8"));
		System.out.println(compressed.length);
		byte[] uncompressed = Snappy.uncompress(compressed);
		System.out.println(uncompressed.length);

		String result = new String(uncompressed, "UTF-8");
		System.out.println(result);
		System.out.println();
		
		
		float[] f = new float[200];
		for (int i = 0; i < f.length; i++) {
			f[i] = BigDecimal.valueOf(Math.random() * 10).setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
		}
		
		VarGroupData data = new VarGroupData();
		data.setCode("youjing_bianhao");
		data.setGroup(VarGroupEnum.DIAN_XB);
		data.setDatetime(new Date());
		
		data.getYxValueMap().put("varA", true);
		data.getYmValueMap().put("varB", 12.0D);
		data.getArrayValueMap().put("array1", f);
		float[] f2 = new float[200];
		for (int i = 0; i < f2.length; i++) {
			f2[i] = BigDecimal.valueOf(Math.random() * 10).setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
		}
		data.getArrayValueMap().put("array2", f2);
		
		System.out.println(data.makeKey().toString());
		long start = System.currentTimeMillis();
		System.out.println("压缩后大小：" + data.makeValue().toByteArray().length);
		System.out.println("压缩用时:" + (System.currentTimeMillis() - start) + "ms");
		
		start = System.currentTimeMillis();
		data.parseValue(data.makeValue());
		System.out.println("解压用时:" + (System.currentTimeMillis() - start) + "ms");
		System.out.println(data.getArrayValueMap().size());
		System.out.println();
		
		
		testSnappy(f);
		System.out.println();
		testGzip(f);

	}
	
	/**
	 * @param f
	 * @throws IOException
	 */
	private static void testGzip(float[] f) throws IOException {
		System.out.println("测试Gzip压缩");
		
		long start = System.currentTimeMillis();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		GZIPOutputStream gos = new GZIPOutputStream(baos);
     	DataOutputStream dos = new DataOutputStream(gos);
     	dos.writeInt(f.length);
     	for (float ff : f) {
     		dos.writeFloat(ff);
     	}
     	dos.close();
		System.out.println("用时:" + (System.currentTimeMillis() - start) + "ms");
		System.out.println("压缩后的长度为:" + baos.toByteArray().length);
		
		System.out.println("测试Gzip解压");
		start = System.currentTimeMillis();
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        GZIPInputStream gis = new GZIPInputStream(bais);
        DataInputStream dis = new DataInputStream(gis);
        float[] array = new float[dis.readInt()];
		for (int j = 0; j < array.length; j++) {
			array[j] = dis.readFloat();
		}
		System.out.println("用时:" + (System.currentTimeMillis() - start) + "ms");
		System.out.println(Arrays.toString(array));
	}

	/**
	 * @param f
	 * @throws IOException
	 */
	private static void testSnappy(float[] f) throws IOException {
		System.out.println("测试Snappy压缩");
		long start = System.currentTimeMillis();
		 ByteArrayOutputStream baos = new ByteArrayOutputStream();
		 DataOutputStream dos = new DataOutputStream(baos);
     	dos.writeInt(f.length);
     	for (float ff : f) {
     		dos.writeFloat(ff);
     	}
		byte[] compressed = Snappy.compress(baos.toByteArray());
		System.out.println("用时:" + (System.currentTimeMillis() - start) + "ms");
		System.out.println("压缩后的长度为:" + compressed.length);
		
		System.out.println("测试Snappy解压");
		start = System.currentTimeMillis();
		ByteArrayInputStream bais = new ByteArrayInputStream(Snappy.uncompress(compressed));
        DataInputStream dis = new DataInputStream(bais);
        float[] array = new float[dis.readInt()];
		for (int j = 0; j < array.length; j++) {
			array[j] = dis.readFloat();
		}
		System.out.println("用时:" + (System.currentTimeMillis() - start) + "ms");
		System.out.println(Arrays.toString(array));
	}

}
