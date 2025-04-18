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

import io.cdap.cdap.api.annotation.Description;
import io.cdap.cdap.api.annotation.Name;
import io.cdap.cdap.api.annotation.Plugin;
import io.cdap.wrangler.api.Arguments;
import io.cdap.wrangler.api.Directive;
import io.cdap.wrangler.api.DirectiveExecutionException;
import io.cdap.wrangler.api.DirectiveParseException;
import io.cdap.wrangler.api.ExecutorContext;
import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.parser.ByteSize;
import io.cdap.wrangler.api.parser.TimeDuration;
import io.cdap.wrangler.api.parser.TokenType;
import io.cdap.wrangler.api.parser.UsageDefinition;

import java.util.List;

/**
 * A directive for aggregating statistics from ByteSize and TimeDuration columns.
 */
@Plugin(type = Directive.TYPE)
@Name("aggregate-stats")
@Description("Aggregates statistics from ByteSize and TimeDuration columns")
public class AggregateStatsDirective implements Directive {
  public static final String NAME = "aggregate-stats";
  private String sizeColumn;
  private String timeColumn;
  private String totalSizeColumn;
  private String totalTimeColumn;
  private String sizeUnit;
  private String timeUnit;
  private boolean isAverage;

  @Override
  public UsageDefinition define() {
    UsageDefinition.Builder builder = UsageDefinition.builder(NAME);
    builder.define("size_column", TokenType.COLUMN_NAME);
    builder.define("time_column", TokenType.COLUMN_NAME);
    builder.define("total_size_column", TokenType.COLUMN_NAME);
    builder.define("total_time_column", TokenType.COLUMN_NAME);
    builder.define("size_unit", TokenType.TEXT, "MB");
    builder.define("time_unit", TokenType.TEXT, "s");
    builder.define("aggregate_type", TokenType.TEXT, "total");
    return builder.build();
  }

  @Override
  public void initialize(Arguments args) throws DirectiveParseException {
    this.sizeColumn = args.value("size_column").substring(1); // Remove leading ':'
    this.timeColumn = args.value("time_column").substring(1);
    this.totalSizeColumn = args.value("total_size_column").substring(1);
    this.totalTimeColumn = args.value("total_time_column").substring(1);
    this.sizeUnit = args.value("size_unit");
    this.timeUnit = args.value("time_unit");
    this.isAverage = "average".equalsIgnoreCase(args.value("aggregate_type"));
  }

  @Override
  public List<Row> execute(List<Row> rows, ExecutorContext context) throws DirectiveExecutionException {
    if (rows.isEmpty()) {
      return rows;
    }

    long totalBytes = 0;
    long totalNanos = 0;
    int count = 0;

    // Accumulate totals
    for (Row row : rows) {
      Object sizeObj = row.getValue(sizeColumn);
      Object timeObj = row.getValue(timeColumn);

      if (sizeObj instanceof ByteSize) {
        totalBytes += ((ByteSize) sizeObj).getBytes();
      }

      if (timeObj instanceof TimeDuration) {
        totalNanos += ((TimeDuration) timeObj).getNanoseconds();
      }

      count++;
    }

    // Create result row with aggregated values
    Row resultRow = new Row();
    ByteSize totalSize = new ByteSize(totalBytes + "B");
    TimeDuration totalTime = new TimeDuration(totalNanos + "ns");

    if (isAverage && count > 0) {
      totalBytes = totalBytes / count;
      totalNanos = totalNanos / count;
      totalSize = new ByteSize(totalBytes + "B");
      totalTime = new TimeDuration(totalNanos + "ns");
    }

    resultRow.setValue(totalSizeColumn, totalSize.convertTo(sizeUnit));
    resultRow.setValue(totalTimeColumn, totalTime.convertTo(timeUnit));

    return List.of(resultRow);
  }
} 