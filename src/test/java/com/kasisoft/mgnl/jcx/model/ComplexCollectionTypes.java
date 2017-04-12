package com.kasisoft.mgnl.jcx.model;

import com.kasisoft.libs.common.annotation.*;

import javax.xml.bind.annotation.*;

import java.util.*;

import lombok.experimental.*;

import lombok.*;

/**
 * @author daniel.kasmeroglu@kasisoft.net
 */
@Data
@EqualsAndHashCode
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class ComplexCollectionTypes {
  
  @XmlElement
  @GenericsType(ComplexObjectType.class)
  List<ComplexObjectType>    objects;

} /* ENCLASS */
