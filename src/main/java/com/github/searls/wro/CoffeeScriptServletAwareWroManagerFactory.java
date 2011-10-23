package com.github.searls.wro;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.cadrlife.mvc.HomeController;

import ro.isdc.wro.extensions.processor.js.GoogleClosureCompressorProcessor;
import ro.isdc.wro.http.WroFilter;
import ro.isdc.wro.manager.factory.BaseWroManagerFactory;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory;
import ro.isdc.wro.model.resource.processor.factory.SimpleProcessorsFactory;

public class CoffeeScriptServletAwareWroManagerFactory extends BaseWroManagerFactory {
	private static final Log LOG = LogFactory.getLog(CoffeeScriptServletAwareWroManagerFactory.class);
	  @Override
	  protected ProcessorsFactory newProcessorsFactory() {
		
		LOG.info("newProcessorsFactory");
	//	  throw new RuntimeException();
	    final SimpleProcessorsFactory factory = new SimpleProcessorsFactory();
	    factory.addPreProcessor(new ConditionalCoffeeScriptProcessor());
	    factory.addPostProcessor(new GoogleClosureCompressorProcessor());
	    return factory;
	  }
	  
	  
	  
}