package com.ht.scada.communication.iec104;

import com.ht.scada.communication.Config;
import com.ht.scada.communication.entity.ChannelInfo;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

public class IEC104ChannelTest {
    @Test
    public void test() {
        Config.INSTANCE.getMode();
    }

//  @Test
  public void execute() throws Exception {
	  ChannelInfo channel = new ChannelInfo();
	  channel.setName("测试通道");
	  channel.setFrames("0x64-2|总召唤");
	  channel.setIntvl(200);
	  channel.setPortInfo("tcp/ip|127.0.0.1:2404");
	  List<IEC104Channel> channelList = new ArrayList<>();
	  long start = System.currentTimeMillis();
	  for (int i = 0; i < 100; i++) {
		  IEC104Channel commChannel = new IEC104Channel(channel, null);
		  channelList.add(commChannel);
		  commChannel.start();
//	  Thread.sleep(100);
//	  
//	  int i = 0;
//	  while(i < 100) {
//		  commChannel.execute();
//		  Thread.sleep(10);
//		  i++;
//	  }
	  }
	  
	  long end = System.currentTimeMillis();
	  
	  Thread.sleep(10000);
	  
	  System.out.println("启动100个采集通道用时：" + (end - start) + "ms");
	  start = System.currentTimeMillis();
	  for (IEC104Channel commChannel : channelList) {
		  commChannel.stop();
	  }
	  end = System.currentTimeMillis();
	  System.out.println("停止100个采集通道用时：" + (end - start) + "ms");
  }
}
