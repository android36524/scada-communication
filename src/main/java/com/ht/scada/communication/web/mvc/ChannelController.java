package com.ht.scada.communication.web.mvc;

import com.ht.scada.common.tag.entity.AcquisitionChannel;
import com.ht.scada.common.tag.util.CommunicationProtocal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping(value="/channel")
public class ChannelController {
    private static final Logger log = LoggerFactory.getLogger(ChannelController.class);

	@RequestMapping(method = RequestMethod.GET)
    public String channels(Model model) {
        log.debug("显示采集通道");
        List<AcquisitionChannel> list = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            AcquisitionChannel channel = new AcquisitionChannel();
            channel.setName("通道"+i);
            channel.setIdx(i);
            channel.setProtocal(CommunicationProtocal.IEC104);
            channel.setFrames("03034-dsa343");
            list.add(channel);
        }
        model.addAttribute("channels", list);
        return "channels";
    }

}
