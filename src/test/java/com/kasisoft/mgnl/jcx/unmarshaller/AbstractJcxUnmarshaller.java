package com.kasisoft.mgnl.jcx.unmarshaller;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.MockitoAnnotations.*;
import static org.testng.Assert.*;

import info.magnolia.test.mock.jcr.*;

import info.magnolia.jcr.util.*;

import com.kasisoft.mgnl.jcx.*;
import com.kasisoft.mgnl.versionhandler.*;

import org.testng.annotations.*;

/**
 * @author daniel.kasmeroglu@kasisoft.net
 */
public class AbstractJcxUnmarshaller {

  public static final String WS_BIBO  = "biboWs";
  public static final String WS_WUPPI = "wuppiWs";
  
  MockSession              biboSession;
  MockSession              wuppiSession;
  String                   targetedNodeUuid;
  ExtendedMockWebContext   context;
  
  protected ExtendedMockWebContext.ExtendedMockWebContextBuilder newWebContextBuilder() {
    return ExtendedMockWebContext.builder()
      .workspace( WS_BIBO  )
      .workspace( WS_WUPPI );
  }
  
  protected String getTargetNodeUuid() {
    return targetedNodeUuid;
  }
  
  protected MockSession getBiboSession() {
    return biboSession;
  }
  
  protected MockSession getWuppiSession() {
    return wuppiSession;
  }
  
  protected ExtendedMockWebContext getContext() {
    return context;
  }
  
  @BeforeClass
  public void setup() throws Exception{
    
    initMocks( this );
    
    context = newWebContextBuilder().build();
    
    context.install();
    
    biboSession  = context.getMockSession( WS_BIBO  );
    wuppiSession = context.getMockSession( WS_WUPPI );
    
    TreeBuilder tb = new TreeBuilder()
      .sContentNode( "targetedNode" )
        .property( "title", "zoidberg" )
      .sEnd();
    
    tb.build( new MockNodeProducer( wuppiSession ) );
    
    targetedNodeUuid = SessionUtil.getNode( wuppiSession, "/targetedNode" ).getIdentifier();
    
  }
  
  protected <T> void assertCreations( JcxUnmarshaller unmarshaller, Class<T> type, String name, T expected ) throws Exception {
    
    T read1 = unmarshaller.createCreator( type ).apply( biboSession.getNode( "/" + name ) );
    assertNotNull( read1 );
    assertThat( read1, is( expected ) );
    
    T read2 = unmarshaller.createLoader( type ).apply( biboSession.getNode( "/" + name ), type.newInstance() );
    assertNotNull( read2 );
    assertThat( read2, is( expected ) );

    T read3 = unmarshaller.createSubnodeCreator( type ).apply( biboSession.getRootNode(), name );
    assertNotNull( read3 );
    assertThat( read3, is( expected ) );

    T read4 = unmarshaller.createSubnodeLoader( type ).apply( biboSession.getRootNode(), name, type.newInstance() );
    assertNotNull( read4 );
    assertThat( read4, is( expected ) );
    
  }

  protected <T> void assertCreationsNull( JcxUnmarshaller unmarshaller, Class<T> type, String name ) throws Exception {
    assertNull( unmarshaller.createCreator( type ).apply( biboSession.getNode( "/" + name ) ) );
    assertNull( unmarshaller.createLoader( type ).apply( biboSession.getNode( "/" + name ), type.newInstance() ) );
    assertNull( unmarshaller.createSubnodeCreator( type ).apply( biboSession.getRootNode(), name ) );
    assertNull( unmarshaller.createSubnodeLoader( type ).apply( biboSession.getRootNode(), name, type.newInstance() ) );
  }

} /* ENDCLASS */
