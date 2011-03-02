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

// TODO: Refactor / restyle legacy code.
public final class Candidate implements Comparable<Candidate> {

  private final Item _item;
  private final int  _indexInc;
  private final int  _indexDec;
  private final int  _delta;

  private final StatKVMap _deltaMap;

  public Candidate(Item item) {
    _item     = item;
    _deltaMap = null;
    _indexInc = -1;
    _indexDec = -1;
    _delta    =  0;
  }

  public Candidate(Item item, StatKVMap deltaMap) {
    _item     = item;
    _deltaMap = deltaMap;

    int[] deltas = deltaMap.data();

    int indexInc = 0;
    int indexDec = 0;
    int delta    = 0;

    final int END = deltas.length;
    for (int i = 0; i < END; ++i) {
      if (deltas[i] < 0) {
        indexDec = i;
      }
      if (deltas[i] > 0) {
        indexInc = i;
        delta = deltas[i];
      }
    }

    _indexInc = indexInc;
    _indexDec = indexDec;
    _delta    = delta;
  }

  public Item item() {
    return _item;
  }

  public StatKVMap delta() {
    return _deltaMap;
  }

  public int deltaValue() {
    return _delta;
  }

  @Override public String toString() {
//    StringBuilder result = new StringBuilder();
//    result.append("[\"");
//    result.append(_item.name());
//    result.append("\" ");
//    for (Stat stat : Stat.values()) {
//      if (_deltaMap.value(stat) < 0) {
//        result.append(-1 * _deltaMap.value(stat));
//        result.append(" ");
//        result.append(stat.name());
//        break;
//      }
//    }
//    result.append(" to ");
//    for (Stat stat : Stat.values()) {
//      if (_deltaMap.value(stat) > 0) {
//        result.append(stat.name());
//      }
//    }
//    result.append("]");
    StringBuilder result = new StringBuilder(60);

    result.append("  ").append(_item.name()).append("\n");

    if (_deltaMap == null) {
      result.append("      None.");
    } else {
      for (Stat s : Stat.values()) {
        if (_deltaMap.value(s) < 0) {
          result.append("      From ")
                .append(s.shortName())
                .append(" (")
                .append(_deltaMap.value(s))
                .append(")");
          break;
        }
      }

      result.append(" to ");

      for (Stat s : Stat.values()) {
        if (_deltaMap.value(s) > 0) {
          result.append(s.shortName())
                .append(" (+")
                .append(_deltaMap.value(s))
                .append(").");
        }
      }
    }

    return result.toString();
  }

  @Override public int compareTo(Candidate o) {
    return _item.compareTo(o._item);
  }

  public int indexIncreased() {
    return _indexInc;
  }

  public int indexDecreased() {
    return _indexDec;
  }
}
