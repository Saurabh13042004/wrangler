/*
 * Copyright © 2017-2019 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */


package io.cdap.wrangler.parser;

import io.cdap.wrangler.api.parser.TimeDuration;
import org.junit.Assert;
import org.junit.Test;

public class TimeDurationTest {

  @Test
  public void testTimeDurationParsing() {
    TimeDuration td1 = new TimeDuration("100ms");
    Assert.assertEquals(100_000_000, td1.getNanoseconds());
    Assert.assertEquals(100, td1.getMilliseconds(), 0.001);
    
    TimeDuration td2 = new TimeDuration("5s");
    Assert.assertEquals(5_000_000_000L, td2.getNanoseconds());
    Assert.assertEquals(5, td2.getSeconds(), 0.001);
    
    TimeDuration td3 = new TimeDuration("2.5m");
    Assert.assertEquals((long)(2.5 * 60 * 1_000_000_000), td3.getNanoseconds());
    Assert.assertEquals(2.5, td3.getMinutes(), 0.001);
    
    // Test nanoseconds
    TimeDuration td4 = new TimeDuration("1000ns");
    Assert.assertEquals(1000, td4.getNanoseconds());
    
    // Test microseconds
    TimeDuration td5 = new TimeDuration("500us");
    Assert.assertEquals(500_000, td5.getNanoseconds());
  }
}