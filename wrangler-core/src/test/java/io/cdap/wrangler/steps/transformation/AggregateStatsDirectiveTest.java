/*
 * Copyright © 2023 Cask Data, Inc.
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

package io.cdap.wrangler.steps.transformation;

import io.cdap.wrangler.TestingRig;
import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.parser.ByteSize;
import io.cdap.wrangler.api.parser.TimeDuration;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * Tests for {@link AggregateStatsDirective}
 */
public class AggregateStatsDirectiveTest {

  @Test
  public void testBasicAggregation() throws Exception {
    String[] directives = new String[] {
      "aggregate-stats :size :time :total_size :total_time MB s total"
    };

    List<Row> rows = Arrays.asList(
      createRow("size", new ByteSize("100MB"), "time", new TimeDuration("30s")),
      createRow("size", new ByteSize("200MB"), "time", new TimeDuration("45s")),
      createRow("size", new ByteSize("150MB"), "time", new TimeDuration("25s"))
    );

    List<Row> results = TestingRig.execute(directives, rows);
    Assert.assertEquals(1, results.size());
    
    Row result = results.get(0);
    Assert.assertEquals(450.0, result.getValue("total_size"), 0.001);
    Assert.assertEquals(100.0, result.getValue("total_time"), 0.001);
  }

  @Test
  public void testAverageAggregation() throws Exception {
    String[] directives = new String[] {
      "aggregate-stats :size :time :avg_size :avg_time GB ms average"
    };

    List<Row> rows = Arrays.asList(
      createRow("size", new ByteSize("1GB"), "time", new TimeDuration("1000ms")),
      createRow("size", new ByteSize("2GB"), "time", new TimeDuration("2000ms")),
      createRow("size", new ByteSize("3GB"), "time", new TimeDuration("3000ms"))
    );

    List<Row> results = TestingRig.execute(directives, rows);
    Assert.assertEquals(1, results.size());
    
    Row result = results.get(0);
    Assert.assertEquals(2.0, result.getValue("avg_size"), 0.001);
    Assert.assertEquals(2000.0, result.getValue("avg_time"), 0.001);
  }

  @Test
  public void testDifferentUnits() throws Exception {
    String[] directives = new String[] {
      "aggregate-stats :size :time :total_size :total_time KB h total"
    };

    List<Row> rows = Arrays.asList(
      createRow("size", new ByteSize("1MB"), "time", new TimeDuration("30m")),
      createRow("size", new ByteSize("2048KB"), "time", new TimeDuration("1h"))
    );

    List<Row> results = TestingRig.execute(directives, rows);
    Assert.assertEquals(1, results.size());
    
    Row result = results.get(0);
    Assert.assertEquals(3072.0, result.getValue("total_size"), 0.001); // 3MB = 3072KB
    Assert.assertEquals(1.5, result.getValue("total_time"), 0.001); // 90m = 1.5h
  }

  @Test
  public void testEmptyInput() throws Exception {
    String[] directives = new String[] {
      "aggregate-stats :size :time :total_size :total_time MB s total"
    };

    List<Row> results = TestingRig.execute(directives, Arrays.asList());
    Assert.assertTrue(results.isEmpty());
  }

  @Test
  public void testMixedUnits() throws Exception {
    String[] directives = new String[] {
      "aggregate-stats :size :time :total_size :total_time GB d total"
    };

    List<Row> rows = Arrays.asList(
      createRow("size", new ByteSize("1024MB"), "time", new TimeDuration("12h")),
      createRow("size", new ByteSize("2GB"), "time", new TimeDuration("36h"))
    );

    List<Row> results = TestingRig.execute(directives, rows);
    Assert.assertEquals(1, results.size());
    
    Row result = results.get(0);
    Assert.assertEquals(3.0, result.getValue("total_size"), 0.001); // 1GB + 2GB = 3GB
    Assert.assertEquals(2.0, result.getValue("total_time"), 0.001); // 48h = 2d
  }

  private Row createRow(String sizeCol, ByteSize size, String timeCol, TimeDuration time) {
    Row row = new Row();
    row.setValue(sizeCol, size);
    row.setValue(timeCol, time);
    return row;
  }
} 