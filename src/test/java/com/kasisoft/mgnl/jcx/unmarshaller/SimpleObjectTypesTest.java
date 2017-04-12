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
public class SimpleObjectTypesTest extends AbstractJcxUnmarshaller {

  @Test
  public void simpleObjectTypes() throws Exception {
    
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
    expected.setVarString( "dodo" );
    
    TreeBuilder tb = new TreeBuilder()
      .sContentNode( "simplePrimitiveTypes" )
        .property( "varBoolean" , expected.getVarBoolean () )
        .property( "varChar"    , expected.getVarChar    () )
        .property( "varByte"    , expected.getVarByte    () )
        .property( "varShort"   , expected.getVarShort   () )
        .property( "varInt"     , expected.getVarInt     () )
        .property( "varLong"    , expected.getVarLong    () )
        .property( "varFloat"   , expected.getVarFloat   () )
        .property( "varDouble"  , expected.getVarDouble  () )
        .property( "varString"  , expected.getVarString  () )
      .sEnd();
    
    tb.build( new MockNodeProducer( biboSession ) );

    // run the tests
    JcxUnmarshaller unmarshaller = new JcxUnmarshaller();
    
    assertCreations( unmarshaller, SimpleObjectTypes.class, "simplePrimitiveTypes", expected );

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
