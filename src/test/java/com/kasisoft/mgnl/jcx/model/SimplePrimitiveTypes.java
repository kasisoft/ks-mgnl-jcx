package com.kasisoft.mgnl.jcx.model;

import javax.xml.bind.annotation.*;

import lombok.experimental.*;

import lombok.*;

/**
 * @author daniel.kasmeroglu@kasisoft.net
 */
@Data
@EqualsAndHashCode
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class SimplePrimitiveTypes {
  
  @XmlAttribute
  boolean     varBoolean;
  
  @XmlAttribute
  char        varChar;
  
  @XmlAttribute
  byte        varByte;
  
  @XmlAttribute
  short       varShort;
  
  @XmlAttribute
  int         varInt;
  
  @XmlAttribute
  long        varLong;
  
  @XmlAttribute
  float       varFloat;
  
  @XmlAttribute
  double      varDouble;
  
  @XmlAttribute
  int         willStayTheSameAsThereIsNoJcrProperty = 34;

} /* ENCLASS */
