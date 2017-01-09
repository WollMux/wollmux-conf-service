package de.muenchen.wollmux.conf.service.exceptions;

public class UnknownKonfigurationException extends Exception
{

  private static final long serialVersionUID = 7933406313581456157L;

  public UnknownKonfigurationException()
  {
    super();
  }

  public UnknownKonfigurationException(String message, Throwable cause,
      boolean enableSuppression, boolean writableStackTrace)
  {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  public UnknownKonfigurationException(String message, Throwable cause)
  {
    super(message, cause);
  }

  public UnknownKonfigurationException(String message)
  {
    super(message);
  }

  public UnknownKonfigurationException(Throwable cause)
  {
    super(cause);
  }

}
