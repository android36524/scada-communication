package com.ht.scada.communication;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Test {

	/**
	 * @param args
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws InterruptedException, IOException {
		System.out.println("开始NoSQL数据库读写混合测试");
		System.out.println("========================");
        int i = 9;
        System.out.println(9 / 4 * 4);
//		final DataServiceKVImpl dataService = new DataServiceKVImpl();
//		dataService.init();
//
//
//		ExecutorService threadPool = Executors.newFixedThreadPool(50);
//		for (int i = 0; i < 100; i++) {
//			final String code = "史127-" + i;
//			threadPool.execute(new Runnable() {
//				public void run() {
//				}
//			});
//		}
//		threadPool.shutdown();

        ScheduledExecutorService executorService;
        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                //To change body of implemented methods use File | Settings | File Templates.
                System.out.println(new Date());
            }
        }, 0, 1, TimeUnit.SECONDS);

        Thread.sleep(5000);
        executorService.shutdownNow();
        System.out.println("end");
        System.out.println(new Date());
        Thread.sleep(5000);

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
