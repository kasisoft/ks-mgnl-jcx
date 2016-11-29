package com.kasisoft.mgnl.jcx;

import info.magnolia.jcr.util.*;

import javax.jcr.*;

import java.util.function.*;

import java.util.*;

import lombok.experimental.*;

import lombok.*;

/**
 * Base class which provides several helper functions to access node settings. 
 * 
 * @author daniel.kasmeroglu@kasisoft.net
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
class AbstractJcrUnmarshaller {

  Map<Class<?>, BiFunction<Node, String, ?>>    attributeLoaders;

  public AbstractJcrUnmarshaller() {
    attributeLoaders  = setupAttributeLoaders();
  }
  
  protected <R> BiFunction<Node, String, R> getAttributeLoader( @NonNull Class<R> type ) {
    return (BiFunction<Node, String, R>) attributeLoaders.get( type );
  }
  
  public synchronized void addAttributeLoader( @NonNull Class<?> type, @NonNull BiFunction<Node, String, ?> propertyAccessor ) {
    attributeLoaders.put( type, propertyAccessor );
  }
  
  private Map<Class<?>, BiFunction<Node, String, ?>> setupAttributeLoaders() {
    Map<Class<?>, BiFunction<Node, String, ?>> result = new HashMap<>();
    
    result.put( Boolean   . TYPE , (n, p) -> toBoolean    ( n, p, false     ) );
    result.put( Character . TYPE , (n, p) -> toCharacter  ( n, p, '\0'      ) );
    result.put( Byte      . TYPE , (n, p) -> toByte       ( n, p, (byte) 0  ) );
    result.put( Short     . TYPE , (n, p) -> toShort      ( n, p, (short) 0 ) );
    result.put( Integer   . TYPE , (n, p) -> toInteger    ( n, p, 0         ) );
    result.put( Long      . TYPE , (n, p) -> toLong       ( n, p, 0L        ) );
    result.put( Float     . TYPE , (n, p) -> toFloat      ( n, p, (float) 0 ) );
    result.put( Double    . TYPE , (n, p) -> toDouble     ( n, p, 0.0       ) );
    
    result.put( Boolean   . class, this::toBoolean    );
    result.put( Character . class, this::toCharacter  );
    result.put( Byte      . class, this::toByte       );
    result.put( Short     . class, this::toShort      );
    result.put( Integer   . class, this::toInteger    );
    result.put( Long      . class, this::toLong       );
    result.put( Float     . class, this::toFloat      );
    result.put( Double    . class, this::toDouble     );
    result.put( String    . class, this::toString     );
    
    return result;
  }
  
  protected boolean toBoolean( Node jcr, @NonNull String propertyName, boolean defVal ) {
    Boolean result = toBoolean( jcr, propertyName );
    return result != null ? result.booleanValue() : defVal;
  }

  protected Boolean toBoolean( Node jcr, @NonNull String propertyName ) {
    return toBoolean( jcr, propertyName, null );
  }
  
  protected Boolean toBoolean( Node jcr, @NonNull String propertyName, Boolean defVal ) {
    return toType( jcr, propertyName, defVal, Boolean::parseBoolean );
  }

  protected char toCharacter( Node jcr, @NonNull String propertyName, char defVal ) {
    Character result = toCharacter( jcr, propertyName );
    return result != null ? result.charValue() : defVal;
  }
  
  protected Character toCharacter( Node jcr, @NonNull String propertyName ) {
    return toCharacter( jcr, propertyName, null );
  }
  
  protected Character toCharacter( Node jcr, @NonNull String propertyName, Character defVal ) {
    return toType( jcr, propertyName, defVal, $ -> $.length() > 0 ? Character.valueOf( $.charAt(0) ) : defVal );
  }

  protected byte toByte( Node jcr, @NonNull String propertyName, byte defVal ) {
    Byte result = toByte( jcr, propertyName );
    return result != null ? result.byteValue() : defVal;
  }
  
  protected Byte toByte( Node jcr, @NonNull String propertyName ) {
    return toByte( jcr, propertyName, null );
  }
  
  protected Byte toByte( Node jcr, @NonNull String propertyName, Byte defVal ) {
    return toType( jcr, propertyName, defVal, Byte::parseByte );
  }

  protected short toShort( Node jcr, @NonNull String propertyName, short defVal ) {
    Short result = toShort( jcr, propertyName );
    return result != null ? result.shortValue() : defVal;
  }
  
  protected Short toShort( Node jcr, @NonNull String propertyName ) {
    return toShort( jcr, propertyName, null );
  }
  
  protected Short toShort( Node jcr, @NonNull String propertyName, Short defVal ) {
    return toType( jcr, propertyName, defVal, Short::parseShort );
  }

  protected int toInteger( Node jcr, @NonNull String propertyName, int defVal ) {
    Integer result = toInteger( jcr, propertyName );
    return result != null ? result.intValue() : defVal;
  }
  
  protected Integer toInteger( Node jcr, @NonNull String propertyName ) {
    return toInteger( jcr, propertyName, null ); 
  }
  
  protected Integer toInteger( Node jcr, @NonNull String propertyName, Integer defVal ) {
    return toType( jcr, propertyName, defVal, Integer::parseInt );
  }

  protected long toLong( Node jcr, @NonNull String propertyName, long defVal ) {
    Long result = toLong( jcr, propertyName );
    return result != null ? result.longValue() : defVal;
  }
  
  protected Long toLong( Node jcr, @NonNull String propertyName ) {
    return toLong( jcr, propertyName, null );
  }
  
  protected Long toLong( Node jcr, @NonNull String propertyName, Long defVal ) {
    return toType( jcr, propertyName, defVal, Long::parseLong );
  }

  protected float toFloat( Node jcr, @NonNull String propertyName, float defVal ) {
    Float result = toFloat( jcr, propertyName );
    return result != null ? result.floatValue() : defVal;
  }
  
  protected Float toFloat( Node jcr, @NonNull String propertyName ) {
    return toFloat( jcr, propertyName, null );
  }
  
  protected Float toFloat( Node jcr, @NonNull String propertyName, Float defVal ) {
    return toType( jcr, propertyName, defVal, Float::parseFloat );
  }

  protected double toDouble( Node jcr, @NonNull String propertyName, double defVal ) {
    Double result = toDouble( jcr, propertyName );
    return result != null ? result.doubleValue() : defVal;
  }
  
  protected Double toDouble( Node jcr, @NonNull String propertyName ) {
    return toDouble( jcr, propertyName, null );
  }
  
  protected Double toDouble( Node jcr, @NonNull String propertyName, Double defVal ) {
    return toType( jcr, propertyName, defVal, Double::parseDouble );
  }

  protected String toString( Node jcr, @NonNull String propertyName ) {
    return toString( jcr, propertyName, null );
  }
  
  protected String toString( Node jcr, @NonNull String propertyName, String defVal ) {
    return toType( jcr, propertyName, defVal, $ -> $ );
  }

  protected <T> T toType( Node jcr, String propertyName, T defVal, Function<String, T> function ) {
    T result = null;
    if( jcr != null ) {
      String strValue = PropertyUtil.getString( jcr, propertyName );
      if( strValue != null ) {
        if( strValue.length() > 0 ) {
          result = function.apply( strValue );
        }
      }
    }
    if( result == null ) {
      result = defVal;
    }
    return result;
  }
  
} /* ENDCLASS */
