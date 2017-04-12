package com.kasisoft.mgnl.jcx.model;

import com.kasisoft.libs.common.annotation.*;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.*;

import java.util.List;

import java.awt.*;

import lombok.experimental.*;

import lombok.*;

/**
 * @author daniel.kasmeroglu@kasisoft.net
 */
@Data
@EqualsAndHashCode
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class SimpleCollectionTypesWithAdapter {
  
  @XmlAttribute
  @XmlJavaTypeAdapter(XmlColorAdapter.class)
  @GenericsType(Color.class)
  List<Color>      colors;

} /* ENCLASS */
