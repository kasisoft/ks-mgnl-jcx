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
public final class CustomName {
  
  @XmlAttribute(name = "oopsi")
  String      varString;

} /* ENCLASS */
