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

package com.glowinteractive.reforger;

public final class StatKVPair implements Comparable<StatKVPair> {

  private final Stat    _key;
  private final Integer _value;

  public StatKVPair(Stat key) {
    _key   = key;
    _value = 0;
  }

  public StatKVPair(Stat key, Integer value) {
    _key   = key;
    _value = value;
  }

  public Stat key() {
    return _key;
  }

  public Integer value() {
    return _value;
  }

  @Override public boolean equals(Object other) {
    if (other instanceof StatKVPair) {
      StatKVPair o = (StatKVPair) other;
      return _key == o._key && _value == o._value;
    }
    return false;
  }

  @Override public int hashCode() {
    int hash = 7;
    hash += (  _key != null) ?   _key.hashCode() : 0;
    hash += (_value != null) ? _value.hashCode() : 0;
    return hash;
  }

  @Override public String toString() {
    return String.format("%s: %d", _key, _value);
  }

  @Override public int compareTo(StatKVPair other) {
    if (_key == other._key) {
      return _value.compareTo(other._value);
    }
    return _key.compareTo(other._key);
  }
}
