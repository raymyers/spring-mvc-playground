package com.cadrlife.mvc;

import groovy.lang.Writable;
import groovy.text.GStringTemplateEngine;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.view.AbstractUrlBasedView;

import com.cadrlife.jhaml.JHaml;
import com.google.common.io.CharStreams;

public class HamlView extends AbstractUrlBasedView {
	private static final Logger LOG = Logger.getLogger(AbstractUrlBasedView.class);
	private JHaml jhaml = new JHaml();
	
	@Override
	protected void renderMergedOutputModel(Map<String, Object> model,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String hamlString = loadResource();
		String parsedHaml = jhaml.parse(hamlString);
		GStringTemplateEngine engine = new GStringTemplateEngine();
		Writable view = engine.createTemplate(parsedHaml).make(model);
		response.getOutputStream().write(view.toString().getBytes());
	}

	private String loadResource() throws IOException {
		InputStream str = getUrlAsStream();
		if (str == null) {
			LOG.error(getUrl() + " not found");
			return "";
		}
		String hamlString = CharStreams.toString(new InputStreamReader(str));
		return hamlString;
	}

	private InputStream getUrlAsStream() {
		String resource = getUrl();
		InputStream str = getServletContext().getResourceAsStream(resource);
		return str;
	}
	
	@Override
	public boolean checkResource(Locale locale) throws Exception {
		return getUrlAsStream() != null;
	}

}
