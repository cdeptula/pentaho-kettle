/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.trans.steps.switchcase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.dummytrans.DummyTransMeta;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidatorFactory;
import org.pentaho.di.trans.steps.loadsave.validator.ListLoadSaveValidator;
import static org.junit.Assert.*;

/**
 * @author nhudak
 */
public class SwitchCaseMetaTest {

  LoadSaveTester loadSaveTester;

  public SwitchCaseMetaTest() {
    //SwitchCaseMeta bean-like attributes
    List<String> attributes = Arrays.asList(
      "fieldname",
      "isContains",
      "caseValueFormat", "caseValueDecimal", /* "caseValueType",*/"caseValueGroup",
      "defaultTargetStepname",
      "caseTargets" );

    //Non-standard getters & setters
    Map<String, String> getterMap = new HashMap<String, String>();
    getterMap.put( "isContains", "isContains" );

    Map<String, String> setterMap = new HashMap<String, String>();
    setterMap.put( "isContains", "setContains" );

    Map<String, FieldLoadSaveValidator<?>> attrValidatorMap =
      new HashMap<String, FieldLoadSaveValidator<?>>();

    Map<String, FieldLoadSaveValidator<?>> typeValidatorMap =
      new HashMap<String, FieldLoadSaveValidator<?>>();

    this.loadSaveTester = new LoadSaveTester( SwitchCaseMeta.class,
      attributes,
      getterMap, setterMap,
      attrValidatorMap, typeValidatorMap );

    FieldLoadSaveValidatorFactory validatorFactory = loadSaveTester.getFieldLoadSaveValidatorFactory();

    FieldLoadSaveValidator<SwitchCaseTarget> targetValidator = new FieldLoadSaveValidator<SwitchCaseTarget>() {
      private final StepMetaInterface targetStepInterface = new DummyTransMeta();

      @Override
      public SwitchCaseTarget getTestObject() {
        return new SwitchCaseTarget() {
          {
            caseValue = UUID.randomUUID().toString();
            caseTargetStepname = UUID.randomUUID().toString();
            caseTargetStep = new StepMeta( caseTargetStepname, targetStepInterface );
          }
        };
      }

      @Override
      public boolean validateTestObject( SwitchCaseTarget testObject, Object actual ) {
        return testObject.caseValue.equals( ( (SwitchCaseTarget) actual ).caseValue )
          && testObject.caseTargetStepname.equals( ( (SwitchCaseTarget) actual ).caseTargetStepname );
      }
    };

    validatorFactory.registerValidator( validatorFactory.getName( SwitchCaseTarget.class ), targetValidator );
    validatorFactory.registerValidator( validatorFactory.getName( List.class, SwitchCaseTarget.class ),
      new ListLoadSaveValidator<SwitchCaseTarget>( targetValidator ) );
  }

  @Test
  public void testLoadSaveXML() throws KettleException {
    loadSaveTester.testXmlRoundTrip();
  }

  @Test
  public void testLoadSaveRepo() throws KettleException {
    loadSaveTester.testRepoRoundTrip();
  }

  @Test
  public void cloneTest() throws Exception {
    SwitchCaseMeta meta = new SwitchCaseMeta();
    meta.allocate();
    List<SwitchCaseTarget> targets = new ArrayList<SwitchCaseTarget>();
    SwitchCaseTarget st1 = new SwitchCaseTarget();
    st1.caseTargetStepname = "step1";
    st1.caseValue = "value1";
    SwitchCaseTarget st2 = new SwitchCaseTarget();
    st2.caseTargetStepname = "step2";
    st2.caseValue = "value2";
    targets.add( st1 );
    targets.add( st2 );
    meta.setCaseTargets( targets );
    // scalars should be cloned using super.clone() - makes sure they're calling super.clone()
    meta.setCaseValueGroup( "somevaluegroup" );
    SwitchCaseMeta aClone = (SwitchCaseMeta) meta.clone();
    assertFalse( aClone == meta );
    List<SwitchCaseTarget> cloneTargets = aClone.getCaseTargets();
    assertEquals( meta.getCaseTargets().size(), cloneTargets.size() );
    SwitchCaseTarget cl1 = cloneTargets.get( 0 );
    SwitchCaseTarget cl2 = cloneTargets.get( 1 );
    assertFalse( cl1 == st1 );
    assertFalse( cl2 == st2 );
    assertEquals( st1.caseTargetStepname, cl1.caseTargetStepname );
    assertEquals( st1.caseValue, cl1.caseValue );
    assertEquals( st2.caseTargetStepname, cl2.caseTargetStepname );
    assertEquals( st2.caseValue, cl2.caseValue );
    assertEquals( meta.getCaseValueGroup(), aClone.getCaseValueGroup() );
  }

}
