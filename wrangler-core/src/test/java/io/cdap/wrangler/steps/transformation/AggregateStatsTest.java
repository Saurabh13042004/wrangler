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

package io.cdap.wrangler.steps.transformation;

import io.cdap.wrangler.TestingRig;
import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.parser.ByteSize;
import io.cdap.wrangler.api.parser.TimeDuration;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class AggregateStatsTest {

  @Test
  public void testSimpleAggregation() throws Exception {
    // Create test data
    List<Row> rows = new ArrayList<>();
    
    Row row1 = new Row();
    row1.add("data_transfer_size", new ByteSize("100KB"));
    row1.add("response_time", new TimeDuration("200ms"));
    rows.add(row1);
    
    Row row2 = new Row();
    row2.add("data_transfer_size", new ByteSize("2MB"));
    row2.add("response_time", new TimeDuration("500ms"));
    rows.add(row2);
    
    Row row3 = new Row();
    row3.add("data_transfer_size", new ByteSize("1.5MB"));
    row3.add("response_time", new TimeDuration("300ms"));
    rows.add(row3);
    
    // Define recipe
    String[] recipe = new String[] {
      "aggregate-stats :data_transfer_size :response_time total_size_mb total_time_sec"
    };
    
    // Execute recipe
    List<Row> results = TestingRig.execute(recipe, rows);
    
    // Verify results
    Assert.assertEquals(1, results.size());
    
    // Expected total size = 100KB + 2MB + 1.5MB = 3.6MB
    double expectedTotalSizeInMB = (100 * 1024 + 2 * 1024 * 1024 + 1.5 * 1024 * 1024) / (1024.0 * 1024.0);
    
    // Expected total time = 200ms + 500ms + 300ms = 1000ms = 1s
    double expectedTotalTimeInSeconds = (200 + 500 + 300) / 1000.0;
    
    Assert.assertEquals(expectedTotalSizeInMB, results.get(0).getValue("total_size_mb"), 0.001);
    Assert.assertEquals(expectedTotalTimeInSeconds, results.get(0).getValue("total_time_sec"), 0.001);
  }

  @Test
  public void testCustomUnitAggregation() throws Exception {
    // Create test data
    List<Row> rows = new ArrayList<>();
    
    Row row1 = new Row();
    row1.add("size", new ByteSize("1GB"));
    row1.add("time", new TimeDuration("30s"));
    rows.add(row1);
    
    Row row2 = new Row();
    row2.add("size", new ByteSize("500MB"));
    row2.add("time", new TimeDuration("1m"));
    rows.add(row2);
    
    // Define recipe with custom output units
    String[] recipe = new String[] {
      "aggregate-stats :size :time total_size_gb total_time_min GB m"
    };
    
    // Execute recipe
    List<Row> results = TestingRig.execute(recipe, rows);
    
    // Verify results
    Assert.assertEquals(1, results.size());
    
    // Expected total size = 1GB + 500MB = 1.5GB
    double expectedTotalSizeInGB = 1.0 + (500 * 1024 * 1024) / (1024.0 * 1024.0 * 1024.0);
    
    // Expected total time = 30s + 1m = 1.5m
    double expectedTotalTimeInMin = 0.5 + 1.0;
    
    Assert.assertEquals(expectedTotalSizeInGB, results.get(0).getValue("total_size_gb"), 0.001);
    Assert.assertEquals(expectedTotalTimeInMin, results.get(0).getValue("total_time_min"), 0.001);
  }
}