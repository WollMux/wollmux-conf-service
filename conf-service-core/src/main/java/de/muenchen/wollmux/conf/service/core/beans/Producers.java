package de.muenchen.wollmux.conf.service.core.beans;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

@Dependent
public class Producers
{
  /**
   * Liest Konfigurationswerte aus Environmentvariablen oder Java-Properties.
   * Die Namen der Variablen müssen mit "ENV_CONFSERVICE_" starten.
   * Für die Verwendung im Code wird der Präfix weggelassen. Der Name wird beim
   * Abruf in Großbuchstaben umgwewandelt.
   * 
   * Beispiel: @Config("referat") sucht nach einer Variable 
   * ENV_CONFSERVICE_REFERAT.
   * 
   * @param ip
   * @return
   */
  @Config
  @Produces
  String getConfigValue(InjectionPoint ip)
  {
    String name = ip.getAnnotated().getAnnotation(Config.class).value();
    if (!name.isEmpty())
    {
      String value = System.getenv("ENV_CONFSERVICE_" + name.toUpperCase());
      if (value == null) {
	value = System.getProperty("ENV_CONFSERVICE_" + name.toUpperCase());
      }
      return value;
    } else
    {
      return null;
    }
  }
}
