package com.kasisoft.mgnl.jcx.model;

import com.kasisoft.mgnl.jcx.*;

import javax.xml.bind.annotation.*;

import lombok.experimental.*;

import lombok.*;

/**
 * @author daniel.kasmeroglu@kasisoft.net
 */
@Data
@EqualsAndHashCode
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class PostProcessObject implements IPostProcessor {
  
  @XmlAttribute
  String      strA;
  
  @XmlAttribute
  String      strB;
  
  String      strC;

  @Override
  public void postprocess() {
    strC = strA + strB;
  }

} /* ENCLASS */
