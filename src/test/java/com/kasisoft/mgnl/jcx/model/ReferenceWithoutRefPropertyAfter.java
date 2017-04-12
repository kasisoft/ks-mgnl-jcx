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
@JcxReference(value = "wuppiWorkspace", before = false)
public final class ReferenceWithoutRefPropertyAfter {
  
  @XmlAttribute
  String      title;

  // default name used for the referring property
  String      target;

} /* ENCLASS */
