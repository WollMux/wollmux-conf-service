package de.muenchen.wollmux.conf.service.core.beans;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

public class Producers {
	@Config
	@Produces
	String getConfigValue(InjectionPoint ip)
	{
		String name = ip.getAnnotated().getAnnotation(Config.class).value();
		if (!name.isEmpty())
		{
			return System.getenv("ENV_" + name.toUpperCase());
		} else
		{
			return null;
		}
	}
}
