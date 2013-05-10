package com.ht.scada.communication;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

public class EmbededWebServer {
	public static void main(String[] args) throws Exception {
		Server server = new Server(Config.INSTANCE.getWebPort());
		
		String wardir = "src/webapp";
		
		WebAppContext context = new WebAppContext();
		context.setResourceBase(wardir);
		context.setDescriptor(wardir + "/WEB-INF/web.xml");

		context.getInitParams().put("org.eclipse.jetty.servlet.Default.useFileMappedBuffer", "false");
		context.setContextPath("/");
		context.setParentLoaderPriority(true);
		server.setHandler(context);
		server.start();
		server.join();
	}
}
