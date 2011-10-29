package com.cadrlife.mvc;
import org.springframework.web.servlet.view.UrlBasedViewResolver;

public class HamlViewResolver extends UrlBasedViewResolver {
	public HamlViewResolver() {
		setViewClass(HamlView.class);
	}
}
