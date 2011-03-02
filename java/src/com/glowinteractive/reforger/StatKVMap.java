/**
 * @author Luke Tyler Downey
 * Copyright 2011 Glow Interactive
 *
 * This software contains original work and/or modifications to
 * original work, which are redistributed under the following terms.
 *
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Copyright 2011 Brian Cairns
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.glowinteractive.reforger;

import java.util.Arrays;
import java.util.Map.Entry;

public final class StatKVMap {

  private final int[] _data;

  public StatKVMap() {
    _data = new int[Stat.TYPE_COUNT];
  }

  public StatKVMap(StatKVPair... pairs) {
    this();

    assert pairs.length - 1 < Stat.TYPE_COUNT :
            "Error: attempted to initialize a StatMap with invalid data size.";

    for (StatKVPair s : pairs) {
      _data[s.key().ordinal()] = s.value();
    }
  }

  public StatKVMap(Entry<Stat, Integer>... pairs) {
    this();

    assert pairs.length - 1 < Stat.TYPE_COUNT :
            "Error: attempted to initialize a StatMap with invalid data size.";

    for (Entry<Stat, Integer> s : pairs) {
      _data[s.getKey().ordinal()] = s.getValue();
    }
  }

  public StatKVMap(Stat decrease, Stat increase, int delta) {
    this();

    _data[decrease.ordinal()] = -delta;
    _data[increase.ordinal()] =  delta;
  }

  public StatKVMap(int data[]) {
    assert data.length == Stat.TYPE_COUNT :
            "Error: attempted to initialize a StatMap with invalid data size.";

    _data = data;
  }

  public StatKVMap add(StatKVMap other) {
    int[] sum = new int[Stat.TYPE_COUNT];
    for (int i = 0; i < Stat.TYPE_COUNT; ++i) {
      sum[i] = _data[i] + other._data[i];
    }
    return new StatKVMap(sum);
  }

  public StatKVMap add(StatKVPair... pairs) {
    int[] sum = _data.clone();
    for (StatKVPair s : pairs) {
      sum[s.key().ordinal()] += s.value();
    }
    return new StatKVMap(sum);
  }

  public StatKVMap add(Entry<Stat, Integer>... pairs) {
    int[] sum = _data.clone();
    for (Entry<Stat, Integer> s : pairs) {
      sum[s.getKey().ordinal()] += s.getValue();
    }
    return new StatKVMap(sum);
  }

  public StatKVMap subtract(StatKVMap other) {
    int[] diff = new int[Stat.TYPE_COUNT];
    for (int i = 0; i < Stat.TYPE_COUNT; ++i) {
      diff[i] = _data[i] - other._data[i];
    }
    return new StatKVMap(diff);
  }

  public StatKVMap subtract(StatKVPair... pairs) {
    int[] diff = _data.clone();
    for (StatKVPair s : pairs) {
      diff[s.key().ordinal()] -= s.value();
    }
    return new StatKVMap(diff);
  }

  public StatKVMap subtract(Entry<Stat, Integer>... pairs) {
    int diff[] = _data.clone();
    for (Entry<Stat, Integer> s : pairs) {
      diff[s.getKey().ordinal()] -= s.getValue();
    }
    return new StatKVMap(diff);
  }

  public int[] data() {
    return _data.clone();
  }

  public int value(Stat stat) {
    return _data[stat.ordinal()];
  }

  @Override public String toString() {
    StringBuilder builder = new StringBuilder(60);

    for (Stat s : Stat.values()) {
      builder.append(String.format("    %5d", _data[s.ordinal()]))
             .append(" ")
             .append(s.shortName());

      if (s.ordinal() + 1 != Stat.TYPE_COUNT) {
        builder.append("\n");
      }
    }

    return builder.toString();
  }

  public String deltaString() {
    StringBuilder builder = new StringBuilder(60);

    for (Stat s : Stat.values()) {
      builder.append(String.format("    %+4d", _data[s.ordinal()]))
             .append(" ")
             .append(s.shortName());

      if (s.ordinal() + 1 != Stat.TYPE_COUNT) {
        builder.append("\n");
      }
    }

    return builder.toString();
  }

  @Override public boolean equals(Object other) {
    if (other instanceof StatKVMap) {
      StatKVMap o = (StatKVMap) other;
      for (int i = 0; i < o._data.length; ++i) {
        if (_data[i] != o._data[i]) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  @Override public int hashCode() {
    return Arrays.hashCode(_data);
  }
}
