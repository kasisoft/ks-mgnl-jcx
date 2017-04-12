package com.kasisoft.mgnl.jcx.unmarshaller;

import com.kasisoft.mgnl.jcx.*;
import com.kasisoft.mgnl.versionhandler.*;

import org.testng.annotations.*;

import javax.xml.bind.annotation.*;

import lombok.experimental.*;

import lombok.*;

/**
 * @author daniel.kasmeroglu@kasisoft.net
 */
public class FieldNameGeneratorTest extends AbstractJcxUnmarshaller {

  @Test
  public void fieldNameGenerator() throws Exception {
    
    // setup the data
    SimpleObjectTypes expected = new SimpleObjectTypes();
    expected.setVarBoolean( true );
    expected.setVarChar( 'a' );
    expected.setVarByte( (byte) 12 );
    expected.setVarShort( (short) 456 );
    expected.setVarInt( (int) 47988 );
    expected.setVarLong( (long) 8819292999112L );
    expected.setVarFloat( (float) 188.2 );
    expected.setVarDouble( -49992727.91 );
    
    TreeBuilder tb = new TreeBuilder()
      .sContentNode( "fieldNameGenerator" )
        .property( "Boolean" , expected.getVarBoolean () )
        .property( "Char"    , expected.getVarChar    () )
        .property( "Byte"    , expected.getVarByte    () )
        .property( "Short"   , expected.getVarShort   () )
        .property( "Int"     , expected.getVarInt     () )
        .property( "Long"    , expected.getVarLong    () )
        .property( "Float"   , expected.getVarFloat   () )
        .property( "Double"  , expected.getVarDouble  () )
      .sEnd();
    
    tb.build( new MockNodeProducer( biboSession ) );

    // run the tests
    JcxUnmarshaller unmarshaller = new JcxUnmarshaller();
    unmarshaller.setFieldNameGenerator( $ -> $.substring( "var".length() ) );
    
    assertCreations( unmarshaller, SimpleObjectTypes.class, "fieldNameGenerator", expected );

  }

  @Getter @Setter
  @EqualsAndHashCode
  @FieldDefaults(level = AccessLevel.PRIVATE)
  public static final class SimpleObjectTypes {
    
    @XmlAttribute
    Boolean     varBoolean;
    
    @XmlAttribute
    Character   varChar;
    
    @XmlAttribute
    Byte        varByte;
    
    @XmlAttribute
    Short       varShort;
    
    @XmlAttribute
    Integer     varInt;
    
    @XmlAttribute
    Long        varLong;
    
    @XmlAttribute
    Float       varFloat;
    
    @XmlAttribute
    Double      varDouble;
    
    @XmlAttribute
    String      varString;

    @XmlAttribute
    Integer     willStayTheSameAsThereIsNoJcrProperty = 34;

  } /* ENCLASS */
  
} /* ENDCLASS */
