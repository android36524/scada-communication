package com.ht.scada.communication.web.mvc;

import com.ht.scada.common.tag.entity.EndTag;
import com.ht.scada.communication.CommunicationManager;
import com.ht.scada.communication.TagCfgManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * MVC通讯控制器，用于实现遥控、遥调等远程操作
 * 作者: "薄成文"
 * 日期: 13-5-4 上午9:55
 */
@Controller
@RequestMapping("/comm")
public class CommController {
    public static final Logger log = LoggerFactory.getLogger(CommController.class);

    @Inject
    private CommunicationManager communicationController;
    @Inject
    private TagCfgManager tagCfgManager;

    @RequestMapping("yk")
    public void yk(String code, String varName, boolean value) {

        EndTag endTag = tagCfgManager.getByEndTagCode(code);
        if (endTag != null) {
            int channelIndex = endTag.getChannelIdx();
            int deviceAddr = endTag.getDeviceAddr();
            Integer dataID = tagCfgManager.getTagVarDataID(endTag.getTplName(), varName);
            if (dataID == null) {
                log.warn("请求的变量不存在：{}-{}", code, varName);
                return;
            } else {
                communicationController.exeYK(channelIndex, deviceAddr, dataID, value);
            }
        }
    }

    @RequestMapping("yt")
    public void yt(String code, String varName, int value) {
        EndTag endTag = tagCfgManager.getByEndTagCode(code);
        if (endTag != null) {
            int channelIndex = endTag.getChannelIdx();
            int deviceAddr = endTag.getDeviceAddr();
            Integer dataID = tagCfgManager.getTagVarDataID(endTag.getTplName(), varName);
            if (dataID == null) {
                log.warn("请求的变量不存在：{}-{}", code, varName);
                return;
            } else {
                communicationController.exeYT(channelIndex, deviceAddr, dataID, value);
            }
        }
    }

    @RequestMapping("baseInfo")
    public void baseInfo(HttpServletResponse resp) throws IOException {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/x-json");
        resp.getWriter().write("中文");
        //JSON.toJSONString();
    }
}
