package com.ht.scada.communication.web;

import com.ht.scada.communication.CommunicationChannel;
import com.ht.scada.communication.CommunicationManager;
import com.ht.scada.communication.DataBaseManager;
import com.ht.scada.communication.entity.OffLimitsRecord;
import com.ht.scada.communication.model.EndTagWrapper;
import org.joda.time.LocalDate;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

public class EndModelOffLimitsRecordController implements IGTVGController {

    public EndModelOffLimitsRecordController() {
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
        ctx.setVariable("idx", idx);

        String dateInput = request.getParameter("date");
        if (dateInput != null) {
            LocalDate localDate = LocalDate.parse(dateInput);


            long count = DataBaseManager.getInstance().getHistoryDataService()
                    .getOfflimitsRecordCount(endTagWrapper.getEndTag().getCode(), localDate.toDate(), localDate.plusDays(1).toDate());
            int pageSize = 20;

            String pageIndexParam = request.getParameter("pageIndex");
            if (pageIndexParam == null) {
                pageIndexParam = "1";
            }
            int pageIndex = Integer.parseInt(pageIndexParam) - 1;

            List<OffLimitsRecord> list =  DataBaseManager.getInstance().getHistoryDataService()
                    .getOffLimitsRecordByActionTime(endTagWrapper.getEndTag().getCode(),
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

        templateEngine.process("offLimitsRecord", ctx, response.getWriter());
    }

}