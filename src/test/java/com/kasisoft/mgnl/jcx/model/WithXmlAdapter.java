package com.kasisoft.mgnl.jcx.model;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.*;

import java.awt.*;

import lombok.experimental.*;

import lombok.*;

/**
 * @author daniel.kasmeroglu@kasisoft.net
 */
@Data
@EqualsAndHashCode
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class WithXmlAdapter {
  
  @XmlAttribute
  @XmlJavaTypeAdapter(XmlColorAdapter.class)
  Color   varColor;

} /* ENCLASS */
