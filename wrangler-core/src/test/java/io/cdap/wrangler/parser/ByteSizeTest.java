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

import io.cdap.wrangler.api.parser.ByteSize;
import org.junit.Assert;
import org.junit.Test;

public class ByteSizeTest {

  @Test
  public void testByteSizeParsing() {
    ByteSize bs1 = new ByteSize("10KB");
    Assert.assertEquals(10240, bs1.getBytes());
    Assert.assertEquals(10, bs1.getKilobytes(), 0.001);
    
    ByteSize bs2 = new ByteSize("5MB");
    Assert.assertEquals(5 * 1024 * 1024, bs2.getBytes());
    Assert.assertEquals(5, bs2.getMegabytes(), 0.001);
    
    ByteSize bs3 = new ByteSize("2.5GB");
    Assert.assertEquals((long)(2.5 * 1024 * 1024 * 1024), bs3.getBytes());
    Assert.assertEquals(2.5, bs3.getGigabytes(), 0.001);
    
    // Test case insensitivity
    ByteSize bs4 = new ByteSize("100kb");
    Assert.assertEquals(100 * 1024, bs4.getBytes());
    
    // Test raw bytes
    ByteSize bs5 = new ByteSize("1024B");
    Assert.assertEquals(1024, bs5.getBytes());
  }
}