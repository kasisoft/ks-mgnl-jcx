package com.kasisoft.mgnl.jcx.model;

import com.kasisoft.libs.common.text.*;

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
public final class ContentObject implements IContent {
  
  @XmlAttribute
  String      varString;

  @Override
  public boolean hasContent() {
    return StringFunctions.cleanup( varString ) != null;
  }

} /* ENCLASS */
