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
public final class SimpleObjectTypes {
  
  @XmlAttribute
  Boolean     varBoolean;
  
  @XmlAttribute
  Character   varChar;
  
  @XmlAttribute
  Byte        varByte;
  
  @XmlAttribute
  Short       varShort;
  
  @XmlAttribute
  Integer     varInt;
  
  @XmlAttribute
  Long        varLong;
  
  @XmlAttribute
  Float       varFloat;
  
  @XmlAttribute
  Double      varDouble;
  
  @XmlAttribute
  String      varString;

  @XmlAttribute
  Integer     willStayTheSameAsThereIsNoJcrProperty = 34;

} /* ENCLASS */
