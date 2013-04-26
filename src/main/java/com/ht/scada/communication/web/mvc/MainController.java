package com.ht.scada.communication.web.mvc;

import com.ht.scada.common.tag.entity.AcquisitionChannel;
import com.ht.scada.common.tag.util.CommunicationProtocal;
import com.ht.scada.communication.CommunicationController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping(value="/main")
public class MainController {

    private static final Logger log = LoggerFactory.getLogger(MainController.class);
    //@Inject
    private CommunicationController communicationController = new CommunicationController();

    @RequestMapping(method = RequestMethod.GET)
    public String index(Model model) {
        model.addAttribute("running", communicationController.isRunning());
        model.addAttribute("channelSize", 600);
        switch (communicationController.getMode()) {
            case SINGLE:
                model.addAttribute("mode", "单机模式");
                break;
            case MASTER:
                model.addAttribute("mode", "双机热备主机模式");
                break;
            case SLAVER:
                model.addAttribute("mode", "双机热备备机模式");
                break;
        }

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
        return "index";
    }

    @RequestMapping(value = "start", method = RequestMethod.GET)
    public String start(Model model) {
        communicationController.start();
        return "redirect:/main";
    }

    @RequestMapping(value = "stop", method = RequestMethod.GET)
    public String stop(Model model) {
        communicationController.stop();
        return "redirect:/main";
    }

	@RequestMapping(value = "channels", method = RequestMethod.GET)
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
