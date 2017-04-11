package com.kasisoft.mgnl.jcx.internal;

import com.kasisoft.libs.common.i18n.*;

/**
 * @author daniel.kasmeroglu@kasisoft.net
 */
public class Messages {

  @I18N("applying content of node '%s' to object '%s'")
  public static I18NFormatter                 msg_applying_node_to_destination;
  
  @I18N("the content of node '%s' is considered to be incomplete")
  public static I18NFormatter                 msg_incomplete_content;
  
  @I18N("the nodes '%s' property '%s' points to a non existing target (workspace='%s', uuid='%s')")
  public static I18NFormatter                 msg_invalid_reference;
  
  static {
    I18NSupport.initialize( Messages.class );
  }
  
} /* ENDCLASS */
