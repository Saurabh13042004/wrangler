
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
 * Token representing a byte size with unit (e.g., 10KB, 5MB).
 */
public class ByteSize implements Token {
  private final String originalValue;
  private final double value;
  private final String unit;
  private final long bytes;

  public ByteSize(String value) {
    this.originalValue = value;
    String numPart = value.replaceAll("[a-zA-Z]", "").trim();
    String unitPart = value.replaceAll("[0-9.]", "").trim();
    
    this.value = Double.parseDouble(numPart);
    this.unit = unitPart.toUpperCase();
    this.bytes = convertToBytes(this.value, this.unit);
  }

  @Override
  public Object value() {
    return originalValue;
  }

  @Override
  public TokenType type() {
    return TokenType.BYTE_SIZE;
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
   * Returns the value converted to bytes.
   */
  public long getBytes() {
    return bytes;
  }

  /**
   * Returns the value in kilobytes.
   */
  public double getKilobytes() {
    return bytes / 1024.0;
  }

  /**
   * Returns the value in megabytes.
   */
  public double getMegabytes() {
    return bytes / (1024.0 * 1024.0);
  }

  /**
   * Returns the value in gigabytes.
   */
  public double getGigabytes() {
    return bytes / (1024.0 * 1024.0 * 1024.0);
  }

  /**
   * Converts a value with the specified unit to bytes.
   */
  private long convertToBytes(double value, String unit) {
    switch (unit) {
      case "B":
        return (long) value;
      case "KB":
        return (long) (value * 1024);
      case "MB":
        return (long) (value * 1024 * 1024);
      case "GB":
        return (long) (value * 1024 * 1024 * 1024);
      case "TB":
        return (long) (value * 1024 * 1024 * 1024 * 1024);
      case "PB":
        return (long) (value * 1024 * 1024 * 1024 * 1024 * 1024);
      default:
        throw new IllegalArgumentException("Unknown byte unit: " + unit);
    }
  }
}