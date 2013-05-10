package com.ht.scada.communication.web.mvc;

import com.ht.scada.communication.CommunicationManager;
import com.ht.scada.communication.Config;
import com.ht.scada.communication.ServiceMode;
import com.ht.scada.communication.cluser.CluserController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.inject.Inject;

@Controller
@RequestMapping(value="/main")
public class MainController {

    private static final Logger log = LoggerFactory.getLogger(MainController.class);
    @Inject
    private CommunicationManager communicationController;
    @Inject
    private CluserController cluserController;

    @RequestMapping
    public String index(Model model) {
        model.addAttribute("running", communicationController.isRunning());
        model.addAttribute("channelSize", 600);
        return "index";
    }

    @RequestMapping(value = "start", method = RequestMethod.GET)
    public String start() {
        communicationController.start();
        cluserController.start();
        return "redirect:/main";
    }

    @RequestMapping(value = "stop", method = RequestMethod.GET)
    public String stop() {
        communicationController.stop();
        cluserController.stop();
        return "redirect:/main";
    }

    @RequestMapping(value = "edit", method = RequestMethod.GET)
    public String edit(Model model) {
        return "edit";
    }

    @RequestMapping(value = "edit", method = RequestMethod.POST)
    public String edit(String mode, @RequestParam(required = false) String masterHost,
                       @RequestParam(required = false) Integer masterPort, Model model) {
        log.info(masterHost);
        if ("single".equals(mode)) {
            Config.INSTANCE.setMode(ServiceMode.SINGLE);
        } else if ("master".equals(mode)) {
            if (masterPort == null) {
                model.addAttribute("message", "端口信息不能为空");
                return "edit";
            }
            Config.INSTANCE.setMode(ServiceMode.MASTER);
            Config.INSTANCE.setMasterPort(masterPort);
        } else if ("slaver".equals(mode)) {
            if (masterPort == null) {
                model.addAttribute("message", "端口信息不能为空");
                return "edit";
            }
            if (masterHost == null || masterHost.isEmpty()) {
                model.addAttribute("message", "主机地址不能为空");
                return "edit";
            }
            Config.INSTANCE.setMode(ServiceMode.SLAVER);
            Config.INSTANCE.setMasterHost(masterHost);
            Config.INSTANCE.setMasterPort(masterPort);
        }

        stop();

        model.addAttribute("message", "新的配置已保存，重启服务后生效");
        mode(model);
        model.addAttribute("running", communicationController.isRunning());
        model.addAttribute("channelSize", 600);
        return "index";
        //return "redirect:/main";
    }

    @ModelAttribute
    public void mode(Model model) {
        model.addAttribute("isSingle", false);
        model.addAttribute("isMaster", false);
        model.addAttribute("isSlaver", false);

        model.addAttribute("isConnected", cluserController.isConnected());
        model.addAttribute("masterHost", Config.INSTANCE.getMasterHost());
        model.addAttribute("masterPort", Config.INSTANCE.getMasterPort());

        switch (Config.INSTANCE.getMode()) {
            case SINGLE:
                model.addAttribute("mode", "单机模式");
                model.addAttribute("isSingle", true);
                break;
            case MASTER:
                model.addAttribute("mode", "主机模式");
                model.addAttribute("isMaster", true);
                break;
            case SLAVER:
                model.addAttribute("mode", "备机模式");
                model.addAttribute("isSlaver", true);
                break;
        }
    }
}
