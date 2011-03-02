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

public enum Stat {

  // TODO: Additional ID's:
  //     13 - Dodge
  //     14 - Parry

                                // Wowhead:       Blizzard:
  HIT(16, "Hit"             ),  // "hitrtng"      "hitRating"
  CRI(19, "Critical Strike" ),  // "critstrkrtng" "critRating"
  HST(28, "Haste"           ),  // "hastertng"    "hasteRating"
  EXP(37, "Expertise"       ),  // "exprtng"      "expertiseRating"
  MST(49, "Mastery"         );  // "mastrtng"     "masteryRating"

  public static final int TYPE_COUNT = values().length;

  private final int    _id;
  private final String _shortName;
//  private final String _fullname;
//  private final String _blizzard;

  Stat(int id, String shortName /*, String blizzard, String fullname */) {
    _id        = id;
    _shortName = shortName;
//  _fullname  = fullname;
//  _blizzard  = blizzard;
  }

  @Override public final String toString() {
    return _shortName + " Rating";
  }

  public final String shortName() {
    return _shortName;
  }

//  public final String blizzard() {
//    return _blizzard;
//  }

  public final int id() {
    return _id;
  }
}
