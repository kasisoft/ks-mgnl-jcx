package com.kasisoft.mgnl.jcx.internal;

import com.kasisoft.libs.common.i18n.*;

/**
 * @author daniel.kasmeroglu@kasisoft.net
 */
public class Messages {

  @I18N("applying content of node '%s' to object '%s'")
  public static I18NFormatter                 msg_applying_node_to_destination;
  
  @I18N("componentProvider '%s' in use (injection probably not working)")
  public static I18NFormatter                 msg_non_guice_component_provider;
  
  @I18N("failed to create jcx unmarshaller for type '%s'. Cause: %s")
  public static I18NFormatter                 msg_failed_to_create_unmarshaller;
  
  @I18N("the content of node '%s' is considered to be incomplete")
  public static I18NFormatter                 msg_incomplete_content;
  
  @I18N("the nodes '%s' property '%s' points to a non existing target (workspace='%s', uuid='%s')")
  public static I18NFormatter                 msg_invalid_reference;

  @I18N("the following elements are incomplete: %s")
  public static I18NFormatter                 msg_incomplete_type;
  
  @I18N("%s [property: %s] - it's necessery to provide a GenericsType annotation with the generic type")
  public static I18NFormatter                 msg_missing_generics_type;

  @I18N("%s [property: %s] - the required property cannot be set as the node '%s' doesn't provide this value")
  public static I18NFormatter                 msg_missing_required_property;

  @I18N("%s [property: %s] - missing getter method")
  public static I18NFormatter                 msg_missing_getter_method;

  @I18N("%s [property: %s] - missing setter method")
  public static I18NFormatter                 msg_missing_setter_method;

  static {
    I18NSupport.initialize( Messages.class );
  }
  
} /* ENDCLASS */
