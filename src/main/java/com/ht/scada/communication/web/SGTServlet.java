package com.ht.scada.communication.web;

import com.ht.scada.common.tag.util.VarSubTypeEnum;
import com.ht.scada.communication.CommunicationChannel;
import com.ht.scada.communication.CommunicationManager;
import com.ht.scada.communication.model.EndTagWrapper;
import com.ht.scada.communication.model.YcTagVar;
import com.ht.scada.communication.web.gt.MyLvBo;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.io.IOException;

@WebServlet(urlPatterns = {"/gt"})
public class SGTServlet extends HttpServlet {
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
        float weiyi[] = null;
        float dianliu[] = null;
        float gonglv[] = null;
        float zaihe[] = null;

        EndTagWrapper endTag = null;
        String endCode = req.getParameter("code");
        Integer channelIdx = Integer.parseInt(req.getParameter("channelIdx"));
        CommunicationChannel communicationChannel = CommunicationManager.getInstance().getChannelMap().get(channelIdx);
        if (communicationChannel != null) {
            for (EndTagWrapper endTagWrapper : communicationChannel.getEndTagList()) {
                if(endTagWrapper.getEndTag().getCode().equals(endCode)) {
                    endTag = endTagWrapper;
                    break;
                }
            }
        }

        if (endTag == null) {
            return;
        }

        String name = "(位移：载荷)";
        String yLabel = "载荷 千牛";
        String title = " 地面示功图";

        for (YcTagVar ycTagVar : endTag.getYcVarList()) {
            if (ycTagVar.getLastArrayValue() != null) {
                if (ycTagVar.getTpl().getSubType() == VarSubTypeEnum.WEI_YI_ARRAY) {
                    weiyi = ycTagVar.getLastArrayValue();
                } else if (ycTagVar.getTpl().getSubType() == VarSubTypeEnum.ZAI_HE_ARRAY) {
                    zaihe = ycTagVar.getLastArrayValue();
                } else if (ycTagVar.getTpl().getSubType() == VarSubTypeEnum.GONG_LV_ARRAY) {
                    gonglv = ycTagVar.getLastArrayValue();
                } else if (ycTagVar.getTpl().getSubType() == VarSubTypeEnum.DIAN_LIU_ARRAY) {
                    dianliu = ycTagVar.getLastArrayValue();
                }
            }
        }

        String type = req.getParameter("type");
        float data[] = null;
        if (type == null || type.equals("sgt")) {
            data = zaihe;
        } else if (type.equals("dlt")) {
            name = "(位移：电流)";
            yLabel = "电流";
            title = "电流图";
            data = dianliu;
        } else if (type.equals("glt")) {
            name = "(位移：有功功率)";
            yLabel = "有功功率";
            title = "功率图";
            data = gonglv;
        }

        final int width = Integer.parseInt(req.getParameter("width"));
        final int height = Integer.parseInt(req.getParameter("height"));

        if (data == null || weiyi == null) {
            return;
        }

//        float zaihe[] = new float[]{79.6F,78.9F,78.9F,78.9F,79.3F,79.3F,78.9F,78.8F,78.8F,79F,79F,78.9F,78.8F,79F,79F,79F,79F,79F,78.9F,79F,79F,79.3F,79.3F,79F,79F,79F,79.3F,79.3F,79.3F,79F,79.3F,79F,79.3F,79F,79F,78.9F,78.8F,78.9F,78.9F,78.8F,78.9F,78.9F,79F,78.9F,78.9F,78.8F,78.9F,78.9F,78.9F,78.9F,79F,79F,79F,79F,79F,79F,78.9F,78.9F,78.5F,78.4F,78.4F,78.4F,78.2F,76.6F,76.1F,74.6F,74.1F,74.8F,75.6F,75.3F,75F,74.5F,74.8F,75.3F,75.3F,74.8F,74.6F,74.5F,74.8F,75F,74.8F,74.6F,74.3F,74.6F,74.6F,74.6F,74.1F,74.1F,74.1F,74.3F,74.5F,74.3F,74F,74F,74F,74F,73.6F,73F,72.5F,72.1F,71.6F,70.8F,69.7F,68.8F,68.1F,66.1F,64.9F,63.6F,62F,59.9F,58F,56.7F,55.1F,52.8F,51.1F,48.9F,50.3F,51.7F,52.3F,51.1F,50F,49.8F,50.8F,51.4F,51.1F,50.5F,50.1F,50.3F,50.8F,50.8F,50.1F,50.1F,50.5F,50.7F,50.7F,50.5F,50.1F,50.3F,50.1F,50.5F,50.3F,50.3F,50.3F,50.1F,50.5F,50.3F,50.1F,50.1F,50.1F,50.3F,50.3F,50.3F,50.1F,50.3F,50.3F,50.5F,50.5F,50.5F,50.3F,50.5F,50.8F,51.4F,54.9F,55.6F,56.5F,57.7F,59.2F,60.7F,62.4F,64.1F,66.7F,68.6F,70.8F,73.2F,74.8F,76.9F,80F,81F,79.4F,78F,78F,79F,80.4F,79.6F,78.5F,78.2F,78.8F,79.8F,79.8F,79F,78.5F,78.8F,79.4F,79.8F,79.6F,79F,79F,79.3F,79.8F,79.6F};
//        float weiyi[] = new float[]{2.36F,2.42F,2.49F,2.55F,2.62F,2.68F,2.75F,2.83F,2.89F,2.96F,3.02F,3.08F,3.15F,3.22F,3.27F,3.35F,3.41F,3.46F,3.54F,3.59F,3.65F,3.72F,3.78F,3.84F,3.9F,3.96F,4.03F,4.09F,4.16F,4.22F,4.28F,4.35F,4.41F,4.48F,4.54F,4.6F,4.66F,4.73F,4.8F,4.86F,4.92F,4.99F,5.06F,5.12F,5.19F,5.26F,5.31F,5.37F,5.45F,5.5F,5.56F,5.64F,5.7F,5.76F,5.84F,5.89F,5.96F,6.03F,6.07F,6.12F,6.14F,6.19F,6.22F,6.22F,6.22F,6.21F,6.18F,6.14F,6.09F,6.05F,5.97F,5.9F,5.84F,5.77F,5.7F,5.65F,5.57F,5.51F,5.45F,5.38F,5.32F,5.26F,5.2F,5.13F,5.06F,5F,4.93F,4.87F,4.8F,4.74F,4.67F,4.6F,4.54F,4.48F,4.41F,4.35F,4.29F,4.22F,4.16F,4.09F,4.03F,3.96F,3.9F,3.84F,3.78F,3.71F,3.65F,3.59F,3.53F,3.45F,3.4F,3.33F,3.25F,3.2F,3.13F,3.05F,3F,2.93F,2.86F,2.8F,2.72F,2.66F,2.59F,2.53F,2.46F,2.39F,2.33F,2.26F,2.2F,2.14F,2.07F,2.01F,1.95F,1.89F,1.82F,1.75F,1.69F,1.63F,1.56F,1.5F,1.44F,1.36F,1.3F,1.23F,1.17F,1.11F,1.04F,0.97F,0.91F,0.85F,0.79F,0.72F,0.66F,0.6F,0.54F,0.48F,0.4F,0.34F,0.28F,0.22F,0.17F,0.12F,0.07F,0.04F,0.01F,0F,0F,0.01F,0.03F,0.06F,0.09F,0.15F,0.2F,0.25F,0.31F,0.37F,0.44F,0.5F,0.56F,0.62F,0.68F,0.74F,0.81F,0.87F,0.93F,0.99F,1.05F,1.12F,1.19F,1.25F,1.31F,1.38F,1.45F,1.51F,1.58F,1.64F,1.7F,1.77F,1.83F,1.89F};
        XYDataset categorydataset = createDataset(name, data, weiyi);
        JFreeChart jfreechart = createChart(categorydataset, title, yLabel);
        ChartUtilities.writeChartAsJPEG(resp.getOutputStream(), jfreechart, width, height);
        resp.getOutputStream().close();
	}

    private void createTestData() {

        float zaihe[] = new float[]{79.6F,78.9F,78.9F,78.9F,79.3F,79.3F,78.9F,78.8F,78.8F,79F,79F,78.9F,78.8F,79F,79F,79F,79F,79F,78.9F,79F,79F,79.3F,79.3F,79F,79F,79F,79.3F,79.3F,79.3F,79F,79.3F,79F,79.3F,79F,79F,78.9F,78.8F,78.9F,78.9F,78.8F,78.9F,78.9F,79F,78.9F,78.9F,78.8F,78.9F,78.9F,78.9F,78.9F,79F,79F,79F,79F,79F,79F,78.9F,78.9F,78.5F,78.4F,78.4F,78.4F,78.2F,76.6F,76.1F,74.6F,74.1F,74.8F,75.6F,75.3F,75F,74.5F,74.8F,75.3F,75.3F,74.8F,74.6F,74.5F,74.8F,75F,74.8F,74.6F,74.3F,74.6F,74.6F,74.6F,74.1F,74.1F,74.1F,74.3F,74.5F,74.3F,74F,74F,74F,74F,73.6F,73F,72.5F,72.1F,71.6F,70.8F,69.7F,68.8F,68.1F,66.1F,64.9F,63.6F,62F,59.9F,58F,56.7F,55.1F,52.8F,51.1F,48.9F,50.3F,51.7F,52.3F,51.1F,50F,49.8F,50.8F,51.4F,51.1F,50.5F,50.1F,50.3F,50.8F,50.8F,50.1F,50.1F,50.5F,50.7F,50.7F,50.5F,50.1F,50.3F,50.1F,50.5F,50.3F,50.3F,50.3F,50.1F,50.5F,50.3F,50.1F,50.1F,50.1F,50.3F,50.3F,50.3F,50.1F,50.3F,50.3F,50.5F,50.5F,50.5F,50.3F,50.5F,50.8F,51.4F,54.9F,55.6F,56.5F,57.7F,59.2F,60.7F,62.4F,64.1F,66.7F,68.6F,70.8F,73.2F,74.8F,76.9F,80F,81F,79.4F,78F,78F,79F,80.4F,79.6F,78.5F,78.2F,78.8F,79.8F,79.8F,79F,78.5F,78.8F,79.4F,79.8F,79.6F,79F,79F,79.3F,79.8F,79.6F};
        float weiyi[] = new float[]{2.36F,2.42F,2.49F,2.55F,2.62F,2.68F,2.75F,2.83F,2.89F,2.96F,3.02F,3.08F,3.15F,3.22F,3.27F,3.35F,3.41F,3.46F,3.54F,3.59F,3.65F,3.72F,3.78F,3.84F,3.9F,3.96F,4.03F,4.09F,4.16F,4.22F,4.28F,4.35F,4.41F,4.48F,4.54F,4.6F,4.66F,4.73F,4.8F,4.86F,4.92F,4.99F,5.06F,5.12F,5.19F,5.26F,5.31F,5.37F,5.45F,5.5F,5.56F,5.64F,5.7F,5.76F,5.84F,5.89F,5.96F,6.03F,6.07F,6.12F,6.14F,6.19F,6.22F,6.22F,6.22F,6.21F,6.18F,6.14F,6.09F,6.05F,5.97F,5.9F,5.84F,5.77F,5.7F,5.65F,5.57F,5.51F,5.45F,5.38F,5.32F,5.26F,5.2F,5.13F,5.06F,5F,4.93F,4.87F,4.8F,4.74F,4.67F,4.6F,4.54F,4.48F,4.41F,4.35F,4.29F,4.22F,4.16F,4.09F,4.03F,3.96F,3.9F,3.84F,3.78F,3.71F,3.65F,3.59F,3.53F,3.45F,3.4F,3.33F,3.25F,3.2F,3.13F,3.05F,3F,2.93F,2.86F,2.8F,2.72F,2.66F,2.59F,2.53F,2.46F,2.39F,2.33F,2.26F,2.2F,2.14F,2.07F,2.01F,1.95F,1.89F,1.82F,1.75F,1.69F,1.63F,1.56F,1.5F,1.44F,1.36F,1.3F,1.23F,1.17F,1.11F,1.04F,0.97F,0.91F,0.85F,0.79F,0.72F,0.66F,0.6F,0.54F,0.48F,0.4F,0.34F,0.28F,0.22F,0.17F,0.12F,0.07F,0.04F,0.01F,0F,0F,0.01F,0.03F,0.06F,0.09F,0.15F,0.2F,0.25F,0.31F,0.37F,0.44F,0.5F,0.56F,0.62F,0.68F,0.74F,0.81F,0.87F,0.93F,0.99F,1.05F,1.12F,1.19F,1.25F,1.31F,1.38F,1.45F,1.51F,1.58F,1.64F,1.7F,1.77F,1.83F,1.89F};
        String[] weiyiValue = new String[200];
        String[] zaiheValue = new String[200];

        String[] weiyiKey = new String[200];
        String[] zaiheKey = new String[200];

        for (int i = 0; i < 200; i++) {
            weiyiKey[i] = Integer.toHexString(i + 0x5407);
            zaiheKey[i] = Integer.toHexString(i + 0x54CF);// 0x5407 + 200
            weiyiValue[i] = Integer.toString((int) (weiyi[i] * 1000));
            zaiheValue[i] = Integer.toString((int) (zaihe[i] * 100));
        }
    }

    private XYDataset createDataset(String name, float data[], float weiyi[]) {
        XYSeries xyseries = new XYSeries(name, false);
        if (data == null) {
            return null;
        }
        MyLvBo.myLvBo(weiyi, data);
        int j = 0;
        for (float f : data) {
            xyseries.add(weiyi[j], data[j]);
            j++;
        }
        xyseries.add(weiyi[0],data[0]);
        XYSeriesCollection xyseriescollection = new XYSeriesCollection();
        xyseriescollection.addSeries(xyseries);
        return xyseriescollection;
    }
    private JFreeChart createChart(XYDataset categorydataset, String title, String yLabel) {
        //Font font = new Font("SansSerif", 12, 10);
        Font font = new Font("宋体", 12, 14);
        JFreeChart jfreechart = ChartFactory.createXYLineChart(title,
                "位移  米", yLabel, categorydataset, PlotOrientation.VERTICAL,
                false, true, false);
        TextTitle texttitle = jfreechart.getTitle();
        texttitle.setFont(font);

        // jfreechart.setBackgroundPaint(Color.CYAN); // 设置背景颜色
        XYPlot categoryplot = (XYPlot) jfreechart.getPlot();
        categoryplot.setBackgroundPaint(Color.white);
        categoryplot.setRangeGridlinePaint(Color.red);
        categoryplot.setRangeGridlinesVisible(true);
        categoryplot.getDomainAxis().setLabelFont(font);
        categoryplot.setDomainGridlinePaint(Color.RED);

        NumberAxis numberaxis = (NumberAxis) categoryplot.getRangeAxis();

        numberaxis.setLabelFont(font);

        XYLineAndShapeRenderer lineandshaperenderer = (XYLineAndShapeRenderer) categoryplot
                .getRenderer();
        lineandshaperenderer.setSeriesPaint(0, Color.BLUE);

        return jfreechart;
    }
}
