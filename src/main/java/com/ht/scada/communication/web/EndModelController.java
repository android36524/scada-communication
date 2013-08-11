package com.ht.scada.communication.web;

import com.ht.scada.communication.CommunicationChannel;
import com.ht.scada.communication.CommunicationManager;
import com.ht.scada.communication.model.EndTagWrapper;
import com.ht.scada.communication.model.VarGroupWrapper;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

public class EndModelController implements IGTVGController {

    public EndModelController() {
        super();
    }

    public void process(
            final HttpServletRequest request, final HttpServletResponse response,
            final ServletContext servletContext, final TemplateEngine templateEngine)
            throws Exception {
        String idxStr = request.getParameter("idx");
        int idx = Integer.parseInt(idxStr);
        CommunicationChannel communicationChannel = CommunicationManager.getInstance().getChannelMap().get(idx);
        EndTagWrapper endTagWrapper = communicationChannel.getEndTagList().get(0);

        WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
        ctx.setVariable("today", Calendar.getInstance());

        //log.debug("显示采集通道");
        ctx.setVariable("channelIdx", idxStr);
        ctx.setVariable("endCode", endTagWrapper.getEndTag().getCode());
        ctx.setVariable("endTagWrapper", endTagWrapper);

        List<VarGroupWrapper> groupList = new ArrayList<>(endTagWrapper.getVarGroupWrapperMap().values());
        Collections.sort(groupList, new Comparator<VarGroupWrapper>() {
            @Override
            public int compare(VarGroupWrapper o1, VarGroupWrapper o2) {
                return o1.getVarGroupInfo().getName().getValue().compareTo(o2.getVarGroupInfo().getName().getValue());  //To change body of implemented methods use File | Settings | File Templates.
            }
        });
        ctx.setVariable("groupList", groupList);

        String tpl = "endTag";
        templateEngine.process(tpl, ctx, response.getWriter());
    }

}