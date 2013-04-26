package com.company.foo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class EmbedMe {
	public static void main(String[] args) throws Exception {
		int i = 0; 
		System.out.println(i++ % 10);
		System.out.println(i);
		System.out.println();
		
		List<String> s = new ArrayList<>(0);
		System.out.println(0 % 3);
		System.out.println(1 % 3);
		System.out.println(3 % 3);
		System.out.println(4 % 3);
		
		final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MINUTE, 10);
		
		System.out.println(dateFormat.format(cal.getTime()));
		int minute = cal.get(Calendar.MINUTE);
		minute = minute - (minute % 3);
		cal.set(Calendar.MINUTE, minute);
		System.out.println(dateFormat.format(cal.getTime()));
//		int port = 8080;
//		Server server = new Server(port);
//		
//		String wardir = "target/sample-webapp-1-SNAPSHOT";
//		
//		WebAppContext context = new WebAppContext();
//		context.setResourceBase(wardir);
//		context.setDescriptor(wardir + "WEB-INF/web.xml");
//		context.setConfigurations(new Configuration[] {
//				new AnnotationConfiguration(), new WebXmlConfiguration(),
//				new WebInfConfiguration(), new TagLibConfiguration(),
//				new PlusConfiguration(), new MetaInfConfiguration(),
//				new FragmentConfiguration(), new EnvConfiguration() });
//
//		context.setContextPath("/");
//		context.setParentLoaderPriority(true);
//		server.setHandler(context);
//		server.start();
//		server.join();
		
	}
}
