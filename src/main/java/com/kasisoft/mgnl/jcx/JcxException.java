package com.kasisoft.mgnl.jcx;

/**
 * @author daniel.kasmeroglu@kasisoft.net
 */
public class JcxException extends RuntimeException {

  public JcxException() {
    super();
  }

  public JcxException( String message, Throwable cause ) {
    super( message, cause );
  }

  public JcxException( String message ) {
    super( message );
  }

  public JcxException( Throwable cause ) {
    super( cause );
  }

  public static JcxException wrap( Exception ex ) {
    if( ex instanceof JcxException ) {
      return (JcxException) ex;
    } else { 
      return new JcxException( ex );
    }
  }
  
} /* ENDCLASS */
