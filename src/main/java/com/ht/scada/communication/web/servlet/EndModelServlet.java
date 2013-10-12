package com.ht.scada.communication.web.servlet;

import com.ht.scada.communication.CommunicationChannel;
import com.ht.scada.communication.CommunicationManager;
import com.ht.scada.communication.model.EndTagWrapper;
import com.ht.scada.communication.model.VarGroupWrapper;

import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

@Singleton
public class EndModelServlet extends TymeleafRenderServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {
        String idxStr = request.getParameter("idx");
        int idx = Integer.parseInt(idxStr);
        CommunicationChannel communicationChannel = CommunicationManager.getInstance().getChannelMap().get(idx);
        EndTagWrapper endTagWrapper = communicationChannel.getEndTagList().get(0);

        request.setAttribute("today", Calendar.getInstance());

        //log.debug("显示采集通道");
        request.setAttribute("channelIdx", idxStr);
        request.setAttribute("endCode", endTagWrapper.getEndTag().getCode());
        request.setAttribute("endTagWrapper", endTagWrapper);

        List<VarGroupWrapper> groupList = new ArrayList<>(endTagWrapper.getVarGroupWrapperMap().values());
        Collections.sort(groupList, new Comparator<VarGroupWrapper>() {
            @Override
            public int compare(VarGroupWrapper o1, VarGroupWrapper o2) {
                return o1.getVarGroupInfo().getName().getValue().compareTo(o2.getVarGroupInfo().getName().getValue());  //To change body of implemented methods use File | Settings | File Templates.
            }
        });
        request.setAttribute("groupList", groupList);
        render.rend(request, resp, "endTag");
    }

}