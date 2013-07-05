package com.ht.scada.communication.web;

import com.ht.scada.common.tag.util.VarGroupEnum;
import com.ht.scada.communication.CommunicationChannel;
import com.ht.scada.communication.CommunicationManager;
import com.ht.scada.communication.DataBaseManager;
import com.ht.scada.communication.entity.VarGroupData;
import com.ht.scada.communication.model.EndTagWrapper;
import com.ht.scada.communication.model.VarGroupWrapper;
import org.joda.time.LocalDate;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

public class EndModelHistoryController implements IGTVGController {

    public EndModelHistoryController() {
        super();
    }

    public void process(
            final HttpServletRequest request, final HttpServletResponse response,
            final ServletContext servletContext, final TemplateEngine templateEngine)
            throws Exception {
        String idxStr = request.getParameter("idx");
        if (idxStr == null) {
            idxStr = "0";
        }
        int idx = Integer.parseInt(idxStr);
        CommunicationChannel communicationChannel = CommunicationManager.getInstance().getChannelMap().get(idx);
        EndTagWrapper endTagWrapper = communicationChannel.getEndTagList().get(0);

        WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
        ctx.setVariable("date", new Date());

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
        ctx.setVariable("idx", idx);
        for (VarGroupWrapper varGroupWrapper : endTagWrapper.getVarGroupWrapperMap().values()) {
            ctx.setVariable("varGroupWrapper", varGroupWrapper);
            break;
        }

        String dateInput = request.getParameter("date");
        String varGroup = request.getParameter("varGroup");
        if (dateInput != null && varGroup != null) {
            VarGroupEnum varGroupEnum = VarGroupEnum.valueOf(varGroup);
            LocalDate localDate = LocalDate.parse(dateInput);

            VarGroupWrapper varGroupWrapper = endTagWrapper.getVarGroupWrapperMap().get(varGroupEnum);
            ctx.setVariable("varGroupWrapper", varGroupWrapper);

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
            ctx.setVariable("pageSize", pageSize);
            ctx.setVariable("pageIndex", pageIndexParam);
            if (count % pageSize == 0) {
                ctx.setVariable("pageCount", count / pageSize);
            } else {
                ctx.setVariable("pageCount", count / pageSize + 1);
            }
            ctx.setVariable("dataList", list);
            ctx.setVariable("date", localDate.toDate());
        }

        templateEngine.process("endTagHistory", ctx, response.getWriter());
    }

}