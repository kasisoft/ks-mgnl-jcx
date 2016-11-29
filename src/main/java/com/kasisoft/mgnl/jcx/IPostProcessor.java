package com.kasisoft.mgnl.jcx;

/**
 * Each implementor requires a certain function to be executed after the loading took place.
 * 
 * Note: It would have been possible to use the generic {@link javax.annotation.PostConstruct} mechanims 
 *       instead. I've decided against it as the unmarshaller is setting the properties after the object had
 *       been created, so an annotated method would already have been executed. Therefore it would have been
 *       necessary to execute the method again and the method would be required to distinguish between the
 *       state after creation and the state after the properties have been set.
 * 
 * @author daniel.kasmeroglu@kasisoft.net
 */
public interface IPostProcessor {

  /**
   * This function will be executed after properties had been loaded.
   */
  void postprocess();
  
} /* ENDINTERFACE */
