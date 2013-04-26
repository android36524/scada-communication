package com.ht.scada.communication;

import com.ht.scada.common.data.service.impl.RealtimeDataServiceImpl;

import java.util.Arrays;

public class JMSTest {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		System.out.println("开始实时数据库测试");
		System.out.println("========================");
		
		final RealtimeDataServiceImpl realtimeDataService = new RealtimeDataServiceImpl();
		realtimeDataService.init();
		
		for (int i = 0; i < 500; i++) {
			final int idx = i;
			new Thread() {
				public void run() {
					
				try {
					
		//pushYX(realtimeDataService, idx);
		//pushYC(realtimeDataService, idx);
		//readYX(realtimeDataService, idx);
		readYC(realtimeDataService, idx);
				} catch (Exception e) {
					e.printStackTrace();
				}
				}

				/**
				 * @param realtimeDataService
				 * @param idx
				 * @throws Exception
				 */
				private void readYC(
						final RealtimeDataServiceImpl realtimeDataService,
						final int idx) throws Exception {
					long start = System.currentTimeMillis();
								getNumData(idx, realtimeDataService);
					long time = System.currentTimeMillis() - start;
					System.out.println("读取10000条遥测数据用时:" + time + "ms");
				}

				/**
				 * @param realtimeDataService
				 * @param idx
				 * @throws Exception
				 */
				private void readYX(
						final RealtimeDataServiceImpl realtimeDataService,
						final int idx) throws Exception {
					long start = System.currentTimeMillis();
								getBoolData(idx, realtimeDataService);
					long time = System.currentTimeMillis() - start;
					System.out.println("读取10000条遥信数据用时:" + time + "ms");
				}

				/**
				 * @param realtimeDataService
				 * @param idx
				 * @throws Exception
				 */
				private void pushYC(
						final RealtimeDataServiceImpl realtimeDataService,
						final int idx) throws Exception {
					long start = System.currentTimeMillis();
								pushNumChangedData(idx, realtimeDataService);
					long time = System.currentTimeMillis() - start;
					System.out.println("写入10000条遥测数据用时:" + time + "ms");
				}

				/**
				 * @param realtimeDataService
				 * @param idx
				 * @throws Exception
				 */
				private void pushYX(
						final RealtimeDataServiceImpl realtimeDataService,
						final int idx) throws Exception {
					long start = System.currentTimeMillis();
								pushBoolChangedData(idx, realtimeDataService);
					long time = System.currentTimeMillis() - start;
					System.out.println("写入10000条遥信数据用时:" + time + "ms");
				};
			}.start();
		}
		
		
		//dataService.
		
	}
	private static int varLen = 20;
	
	private static void getBoolData(int i, final RealtimeDataServiceImpl realtimeDataService) throws Exception {
//		long start = System.currentTimeMillis();
//		for (int i = 0; i < 500; i++) {
			final String code = "史127" + i;
			
//			for (int j = 0; j < varLen; j++) {
//				final String varName = "报警变量" + j;
//				realtimeDataService.getBoolValue(code, varName);
//			}
			String[] name = new String[varLen];
			for (int j = 0; j < varLen; j++) {
				final String varName = "报警变量" + j;
				name[j] = varName;
				//realtimeDataService.putValue(code, varName, true);
			}
			boolean[] v = realtimeDataService.getBatchBoolValue(code, name);
			System.out.println(Arrays.toString(v));
//		}
//		long time = System.currentTimeMillis() - start;
//		System.out.println("读取10000条遥信数据用时:" + time + "ms");
	}
	
	private static void getNumData(int i, final RealtimeDataServiceImpl realtimeDataService) throws Exception {
//		long start = System.currentTimeMillis();
//		for (int i = 0; i < 500; i++) {
			final String code = "史127" + i;
			
//			for (int j = 0; j < varLen; j++) {
//				final String varName = "遥测变量" + j;
//				realtimeDataService.getNumValue(code, varName);
//			}
			
			String[] name = new String[varLen];
			for (int j = 0; j < varLen; j++) {
				final String varName = "遥测变量" + j;
				name[j] = varName;
				//realtimeDataService.putValue(code, varName, true);
			}
			double[] v = realtimeDataService.getBatchNumValue(code, name);
			//System.out.println(Arrays.toString(v));
//		}
//		long time = System.currentTimeMillis() - start;
//		System.out.println("读取10000条遥测数据用时:" + time + "ms");
	}
	
	private static void pushBoolChangedData(int i, final RealtimeDataServiceImpl realtimeDataService) throws Exception {
//		long start = System.currentTimeMillis();
//		
//		for (int i = 0; i < 500; i++) {
			final String code = "史127" + i;
			
			String[] name = new String[varLen];
			boolean[] value = new boolean[varLen];
			for (int j = 0; j < varLen; j++) {
				final String varName = "报警变量" + j;
				name[j] = varName;
				value[j] = Math.random() > 0.5;
				//realtimeDataService.putValue(code, varName, true);
			}
			realtimeDataService.putBatchValue(code, name, value);
//		}
//		long time = System.currentTimeMillis() - start;
//		System.out.println("推送和更新10000条遥信数据用时:" + time + "ms");
	}
	
	private static void pushNumChangedData(int i, final RealtimeDataServiceImpl realtimeDataService) throws Exception {
//		long start = System.currentTimeMillis();
//		
//		for (int i = 0; i < 500; i++) {
			final String code = "史127" + i;
			
			String[] name = new String[varLen];
			double[] value = new double[varLen];
			for (int j = 0; j < varLen; j++) {
				final String varName = "遥测变量" + j;
				name[j] = varName;
				value[j] = Math.random() * 100;
				//realtimeDataService.putValue(code, varName, Math.random() * 100);
			}
			realtimeDataService.putBatchValue(code, name, value);
//		}
//		long time = System.currentTimeMillis() - start;
//		System.out.println("推送和更新10000条遥测数据用时:" + time + "ms");
	}
	


}
