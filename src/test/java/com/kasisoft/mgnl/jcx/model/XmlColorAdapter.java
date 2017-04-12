package com.kasisoft.mgnl.jcx.model;

import com.kasisoft.libs.common.xml.adapters.*;

/**
 * @author daniel.kasmeroglu@kasisoft.net
 */
public class XmlColorAdapter extends XmlToTypeAdapter {

  public XmlColorAdapter() {
    super( new ColorAdapter() );
  }
  
} /* ENDCLASS */
