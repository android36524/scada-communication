package com.ht.scada.communication.web.cxf;

import com.ht.scada.communication.CommunicationManager;

import javax.ws.rs.QueryParam;

/**
 * Root resource (exposed at "myresource" path)
 */
public class MyResource implements IMyResource {

    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
    @Override
    public String getIt() {
        return "Got it!";
    }

    @Override
    public String m1(String say) {
        System.out.println("do m1");
        return say;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String m2(String id) {
        System.out.println("do m2");
        return id;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String m3(String id) {
        System.out.println("do m3");
        return id;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String yk(@QueryParam("channelIndex") int channelIndex,
                     @QueryParam("endCode") String endCode,
                     @QueryParam("varName") String varName,
                     @QueryParam("value") boolean value) {
        return CommunicationManager.getInstance().exeYK(channelIndex, endCode, varName, value) + "";
    }

    @Override
    public String yt(@QueryParam("channelIndex") int channelIndex, @QueryParam("endCode") String endCode, @QueryParam("varName") String varName, @QueryParam("value") int value) {
        return CommunicationManager.getInstance().exeYT(channelIndex, endCode, varName, value) + "";
    }

}