package com.ht.scada.communication;

import com.ht.scada.communication.service.impl.DataServiceKVImpl;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Test {

	/**
	 * @param args
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws InterruptedException, IOException {
		System.out.println("开始NoSQL数据库读写混合测试");
		System.out.println("========================");
		
		final DataServiceKVImpl dataService = new DataServiceKVImpl();
		dataService.init();
		
		
		ExecutorService threadPool = Executors.newFixedThreadPool(50);
		for (int i = 0; i < 100; i++) {
			final String code = "史127-" + i;
			threadPool.execute(new Runnable() {
				public void run() {
				}
			});
		}
		threadPool.shutdown();
		
//		int faultCount = 0;
//		int ycCount = 0;
//		
//		while (true) {
//			saveFaultData(dataService);
//			faultCount += 1000;
//			System.out.println("当前故障记录共：" + faultCount);
//			System.out.println();
//			
//			saveYCData(dataService);
//			ycCount += 10000;
//			System.out.println("当前遥测记录共：" + ycCount);
//			System.out.println();
//			
//			queryFaultData(dataService);
//			queryYCData(dataService);
//			Thread.sleep(2000);
//			
//			System.out.println("-----------------------------");
//		}
//		
		
		//dataService.
		
	}
	

}
