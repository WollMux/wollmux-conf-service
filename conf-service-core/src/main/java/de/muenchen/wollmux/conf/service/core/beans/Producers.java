package de.muenchen.wollmux.conf.service.core.beans;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

@Dependent
public class Producers {
	@Config
	@Produces
	String getConfigValue(InjectionPoint ip)
	{
		String name = ip.getAnnotated().getAnnotation(Config.class).value();
		if (!name.isEmpty())
		{
			return System.getenv("ENV_CONFSERVICE_" + name.toUpperCase());
		} else
		{
			return null;
		}
	}
}
