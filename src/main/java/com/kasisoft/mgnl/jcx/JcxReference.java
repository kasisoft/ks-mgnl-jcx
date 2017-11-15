package com.kasisoft.mgnl.jcx;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import info.magnolia.repository.*;

import java.lang.annotation.*;

/**
 * @author daniel.kasmeroglu@kasisoft.net
 */
@Retention(RUNTIME)
@Target({TYPE, FIELD})
public @interface JcxReference {
  
  enum UseOriginalNode {
    dont,
    before,
    after;
  }

  String value() default RepositoryConstants.WEBSITE;
  
  String property() default "target";
  
  UseOriginalNode useOriginalNode() default UseOriginalNode.dont;
  
} /* ENDANNOTATION */
