package com.ht.scada.communication.web.controller;

import com.ht.scada.common.tag.util.VarGroupEnum;
import com.ht.scada.communication.CommunicationChannel;
import com.ht.scada.communication.CommunicationManager;
import com.ht.scada.communication.DataBaseManager;
import com.ht.scada.communication.entity.VarGroupData;
import com.ht.scada.communication.model.EndTagWrapper;
import com.ht.scada.communication.model.VarGroupWrapper;
import org.joda.time.LocalDate;
import org.lime.guice.mvc.views.thymeleaf.annotations.ThymeleafView;
import org.zdevra.guice.mvc.annotations.Controller;
import org.zdevra.guice.mvc.annotations.Path;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Controller
@Singleton
public class EndModelHistoryController {


    @Path("/endTagHistory") @ThymeleafView("endTagHistory")
    public void index(HttpServletRequest request) throws Exception {
        String idxStr = request.getParameter("idx");
        if (idxStr == null) {
            idxStr = "0";
        }
        int idx = Integer.parseInt(idxStr);
        CommunicationChannel communicationChannel = CommunicationManager.getInstance().getChannelMap().get(idx);
        EndTagWrapper endTagWrapper = communicationChannel.getEndTagList().get(0);

        request.setAttribute("date", new Date());

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
        request.setAttribute("idx", idx);
        for (VarGroupWrapper varGroupWrapper : endTagWrapper.getVarGroupWrapperMap().values()) {
            request.setAttribute("varGroupWrapper", varGroupWrapper);
            break;
        }

        String dateInput = request.getParameter("date");
        String varGroup = request.getParameter("varGroup");
        if (dateInput != null && varGroup != null) {
            VarGroupEnum varGroupEnum = VarGroupEnum.valueOf(varGroup);
            LocalDate localDate = LocalDate.parse(dateInput);

            VarGroupWrapper varGroupWrapper = endTagWrapper.getVarGroupWrapperMap().get(varGroupEnum);
            request.setAttribute("varGroupWrapper", varGroupWrapper);

            long count = DataBaseManager.getInstance().getHistoryDataService()
                    .getVarGroupDataCount(endTagWrapper.getEndTag().getCode(), varGroupEnum,
                            localDate.toDate(), localDate.plusDays(1).toDate());
            int pageSize = 20;

            String pageIndexParam = request.getParameter("pageIndex");
            if (pageIndexParam == null) {
                pageIndexParam = "1";
            }
            int pageIndex = Integer.parseInt(pageIndexParam) - 1;

            List<VarGroupData> list =  DataBaseManager.getInstance().getHistoryDataService()
                    .getVarGroupData(endTagWrapper.getEndTag().getCode(), varGroupEnum,
                            localDate.toDate(), localDate.plusDays(1).toDate(), pageIndex * pageSize, pageSize);
            request.setAttribute("pageSize", pageSize);
            request.setAttribute("pageIndex", pageIndexParam);
            if (count % pageSize == 0) {
                request.setAttribute("pageCount", count / pageSize);
            } else {
                request.setAttribute("pageCount", count / pageSize + 1);
            }
            request.setAttribute("dataList", list);
            request.setAttribute("date", localDate.toDate());
        }

    }

}