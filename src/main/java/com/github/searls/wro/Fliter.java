package com.github.searls.wro;

import java.util.Enumeration;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import ro.isdc.wro.config.jmx.ConfigConstants;
import ro.isdc.wro.http.WroFilter;

public class Fliter extends WroFilter {
@Override
protected void doInit(final FilterConfig config) throws ServletException {
	super.doInit(new FilterConfig() {
		
		public ServletContext getServletContext() {
			return config.getServletContext();
		}
		
		public Enumeration<?> getInitParameterNames() {
			//ConfigConstants.
			return config.getInitParameterNames();
		}
		
		public String getInitParameter(String name) {
			if (ConfigConstants.cacheUpdatePeriod.name().equalsIgnoreCase(name)) {
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
