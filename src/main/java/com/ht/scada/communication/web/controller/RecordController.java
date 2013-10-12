package com.ht.scada.communication.web.controller;

import com.ht.scada.communication.CommunicationChannel;
import com.ht.scada.communication.CommunicationManager;
import com.ht.scada.communication.DataBaseManager;
import com.ht.scada.communication.entity.FaultRecord;
import com.ht.scada.communication.entity.OffLimitsRecord;
import com.ht.scada.communication.entity.YxRecord;
import com.ht.scada.communication.model.EndTagWrapper;
import org.joda.time.LocalDate;
import org.lime.guice.mvc.views.thymeleaf.annotations.ThymeleafView;
import org.thymeleaf.TemplateEngine;
import org.zdevra.guice.mvc.annotations.Controller;
import org.zdevra.guice.mvc.annotations.Path;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

@Controller
@Singleton
public class RecordController extends AbstractController {

    @Inject
    public RecordController(TemplateEngine templateEngine) {
        super(templateEngine);
    }

    @Path("/yxRecord") @ThymeleafView("yxRecord")
    public void yxRecord(HttpServletRequest request) throws Exception {

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
        request.setAttribute("idx", idx);

        String dateInput = request.getParameter("date");
        if (dateInput != null) {
            LocalDate localDate = LocalDate.parse(dateInput);


            long count = DataBaseManager.getInstance().getHistoryDataService()
                    .getYxRecordCount(endTagWrapper.getEndTag().getCode(), localDate.toDate(), localDate.plusDays(1).toDate());
            int pageSize = 20;

            String pageIndexParam = request.getParameter("pageIndex");
            if (pageIndexParam == null) {
                pageIndexParam = "1";
            }
            int pageIndex = Integer.parseInt(pageIndexParam) - 1;

            List<YxRecord> list =  DataBaseManager.getInstance().getHistoryDataService()
                    .getYxRecordByDatetime(endTagWrapper.getEndTag().getCode(),
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

    @Path("/faultRecord") @ThymeleafView("faultRecord")
    public void faultRecord(HttpServletRequest request) throws Exception {
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
        request.setAttribute("idx", idx);

        String dateInput = request.getParameter("date");
        if (dateInput != null) {
            LocalDate localDate = LocalDate.parse(dateInput);


            long count = DataBaseManager.getInstance().getHistoryDataService()
                    .getFaultRecordCount(endTagWrapper.getEndTag().getCode(), localDate.toDate(), localDate.plusDays(1).toDate());
            int pageSize = 20;

            String pageIndexParam = request.getParameter("pageIndex");
            if (pageIndexParam == null) {
                pageIndexParam = "1";
            }
            int pageIndex = Integer.parseInt(pageIndexParam) - 1;

            List<FaultRecord> list =  DataBaseManager.getInstance().getHistoryDataService()
                    .getFaultRecordByActionTime(endTagWrapper.getEndTag().getCode(),
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

    @Path("/offLimitsRecord") @ThymeleafView("offLimitsRecord")
    public void offLimitsRecord(HttpServletRequest request) throws Exception {

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
        request.setAttribute("idx", idx);

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