package com.github.searls.wro;

import java.util.Enumeration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.http.WroFilter;

public class Fliter extends WroFilter {
@Override
protected void doInit(final FilterConfig config) throws ServletException {
	super.doInit(new FilterConfig() {
		
		public ServletContext getServletContext() {
			return config.getServletContext();
		}
		
		public Enumeration<?> getInitParameterNames() {
			return config.getInitParameterNames();
		}
		
		public String getInitParameter(String name) {
			if ("cacheUpdatePeriod".equalsIgnoreCase(name)) {
				return "1";
			}
			return config.getInitParameter(name);
		}
		
		public String getFilterName() {
			return config.getFilterName();
		}
	});
	
}
}
