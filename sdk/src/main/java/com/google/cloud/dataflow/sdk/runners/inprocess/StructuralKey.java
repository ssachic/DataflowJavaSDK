/*
 * Copyright (C) 2016 Google Inc.
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
package com.google.cloud.dataflow.sdk.runners.inprocess;

import com.google.cloud.dataflow.sdk.coders.Coder;
import com.google.cloud.dataflow.sdk.coders.CoderException;
import com.google.cloud.dataflow.sdk.util.CoderUtils;

/**
 * A (Key, Coder) pair that uses the structural value of the key (as provided by
 * {@link Coder#structuralValue(Object)}) to perform equality and hashing.
 */
class StructuralKey<K> {
  /**
   * Create a new Structural Key of the provided key that can be encoded by the provided coder.
   */
  public static <K> StructuralKey<K> of(K key, Coder<K> coder) {
    try {
      return new StructuralKey<>(coder, key);
    } catch (Exception e) {
      throw new IllegalArgumentException(
          "Could not encode a key with its provided coder " + coder.getClass().getSimpleName(), e);
    }
  }

  private final Coder<K> coder;
  private final Object structuralValue;
  private final byte[] encoded;

  private StructuralKey(Coder<K> coder, K key) throws Exception {
    this.coder = coder;
    this.structuralValue = coder.structuralValue(key);
    this.encoded = CoderUtils.encodeToByteArray(coder, key);
  }

  public K getKey() {
    try {
      return CoderUtils.decodeFromByteArray(coder, encoded);
    } catch (CoderException e) {
      throw new IllegalArgumentException(
          "Could not decode Key with coder of type " + coder.getClass().getSimpleName());
    }
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if (other instanceof StructuralKey) {
      StructuralKey that = (StructuralKey) other;
      return structuralValue.equals(that.structuralValue);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return structuralValue.hashCode();
  }
}
