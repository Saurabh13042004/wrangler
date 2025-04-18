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

package io.cdap.wrangler.api.parser;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/**
 * Token representing a time duration with unit (e.g., 10ms, 5s).
 */
public class TimeDuration implements Token {
  private final String originalValue;
  private final double value;
  private final String unit;
  private final long nanoseconds;

  public TimeDuration(String value) {
    this.originalValue = value;
    String numPart = value.replaceAll("[a-zA-Z]", "").trim();
    String unitPart = value.replaceAll("[0-9.]", "").trim();
    
    this.value = Double.parseDouble(numPart);
    this.unit = unitPart.toLowerCase();
    this.nanoseconds = convertToNanos(this.value, this.unit);
  }

  @Override
  public Object value() {
    return originalValue;
  }

  @Override
  public TokenType type() {
    return TokenType.TIME_DURATION;
  }

  @Override
  public JsonElement toJson() {
    return new JsonPrimitive(originalValue);
  }

  /**
   * Returns the original numeric value before unit conversion.
   */
  public double getValue() {
    return value;
  }

  /**
   * Returns the unit specified in the original token.
   */
  public String getUnit() {
    return unit;
  }

  /**
   * Returns the value converted to nanoseconds.
   */
  public long getNanoseconds() {
    return nanoseconds;
  }

  /**
   * Returns the value in milliseconds.
   */
  public double getMilliseconds() {
    return nanoseconds / 1_000_000.0;
  }

  /**
   * Returns the value in seconds.
   */
  public double getSeconds() {
    return nanoseconds / 1_000_000_000.0;
  }

  /**
   * Returns the value in minutes.
   */
  public double getMinutes() {
    return nanoseconds / (60.0 * 1_000_000_000.0);
  }

  /**
   * Returns the value in hours.
   */
  public double getHours() {
    return nanoseconds / (3600.0 * 1_000_000_000.0);
  }

  /**
   * Returns the value in days.
   */
  public double getDays() {
    return nanoseconds / (24.0 * 3600.0 * 1_000_000_000.0);
  }

  /**
   * Returns the value in weeks.
   */
  public double getWeeks() {
    return nanoseconds / (7.0 * 24.0 * 3600.0 * 1_000_000_000.0);
  }

  /**
   * Returns the value in months (assuming 30.44 days per month).
   */
  public double getMonths() {
    return nanoseconds / (30.44 * 24.0 * 3600.0 * 1_000_000_000.0);
  }

  /**
   * Returns the value in years (assuming 365.25 days per year).
   */
  public double getYears() {
    return nanoseconds / (365.25 * 24.0 * 3600.0 * 1_000_000_000.0);
  }

  /**
   * Converts the value to the specified unit.
   * @param targetUnit The target unit to convert to (ns, us, ms, s, m, h, d, w, mo, y)
   * @return The value in the target unit
   */
  public double convertTo(String targetUnit) {
    switch (targetUnit.toLowerCase()) {
      case "ns":
        return nanoseconds;
      case "us":
        return nanoseconds / 1_000.0;
      case "ms":
        return getMilliseconds();
      case "s":
        return getSeconds();
      case "m":
        return getMinutes();
      case "h":
        return getHours();
      case "d":
        return getDays();
      case "w":
        return getWeeks();
      case "mo":
        return getMonths();
      case "y":
        return getYears();
      default:
        throw new IllegalArgumentException(
          String.format("Unknown target unit: %s. Supported units are: ns, us, ms, s, m, h, d, w, mo, y", targetUnit));
    }
  }

  @Override
  public String toString() {
    return String.format("%s (%d ns)", originalValue, nanoseconds);
  }

  private long convertToNanos(double value, String unit) {
    switch (unit) {
      case "ns":
        return (long) value;
      case "us":
        return (long) (value * 1_000);
      case "ms":
        return (long) (value * 1_000_000);
      case "s":
        return (long) (value * 1_000_000_000);
      case "m":
        return (long) (value * 60 * 1_000_000_000);
      case "h":
        return (long) (value * 3600L * 1_000_000_000L);
      case "d":
        return (long) (value * 24 * 3600L * 1_000_000_000L);
      case "w":
        return (long) (value * 7 * 24 * 3600L * 1_000_000_000L);
      case "mo":
        return (long) (value * 30.44 * 24 * 3600L * 1_000_000_000L);
      case "y":
        return (long) (value * 365.25 * 24 * 3600L * 1_000_000_000L);
      default:
        throw new IllegalArgumentException(
          String.format("Unknown time unit: %s. Supported units are: ns, us, ms, s, m, h, d, w, mo, y", unit));
    }
  }
}