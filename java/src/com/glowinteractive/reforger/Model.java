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

import java.util.EnumMap;
import java.util.EnumSet;

public final class Model implements AbstractModel {

  private static final int HIT_CAP = 601;
  private static final int EXP_CAP = 781;

  private static final float HIT_COEFFICIENT_TO_CAP = 1.10f;
  private static final float HIT_COEFFICIENT_CAPPED = 0.60f;

  private static final float CRI_COEFFICIENT = 0.80f;
  private static final float HST_COEFFICIENT = 0.70f;

  private static final float EXP_COEFFICIENT_TO_CAP = 1.05f;
  private static final float EXP_COEFFICIENT_CAPPED = 0.00f;

  private static final float MST_COEFFICIENT = 0.90f;

  // EP values of hit & exp at their respective caps.
  private static final float EP_HIT_CAP = HIT_CAP * HIT_COEFFICIENT_TO_CAP;
  private static final float EP_EXP_CAP = EXP_CAP * EXP_COEFFICIENT_TO_CAP;

  @Override public float calculateEP(int[] stats) {
    float result = 0.0f;

    int hit = stats[Stat.HIT.ordinal()];
    if (hit > HIT_CAP) {
      result += (hit - HIT_CAP) * HIT_COEFFICIENT_CAPPED;
      result += EP_HIT_CAP;
    } else {
      result += hit * HIT_COEFFICIENT_TO_CAP;
    }

    result += stats[Stat.CRI.ordinal()] * CRI_COEFFICIENT;
    result += stats[Stat.HST.ordinal()] * HST_COEFFICIENT;

    int exp = stats[Stat.EXP.ordinal()];
    if (exp > EXP_CAP) {
      result += (exp - EXP_CAP) * EXP_COEFFICIENT_CAPPED;
      result += EP_EXP_CAP;
    } else {
      result += exp * EXP_COEFFICIENT_TO_CAP;
    }

    result += stats[Stat.MST.ordinal()] * MST_COEFFICIENT;

    return result;
  }

  @Override public EnumMap<Stat, EnumSet<Stat>> candidateMappings() {
    EnumMap<Stat, EnumSet<Stat>> result = new EnumMap<Stat, EnumSet<Stat>>(Stat.class);
    EnumSet<Stat> stats;

    stats = EnumSet.noneOf(Stat.class);
    stats.add(Stat.EXP);
    stats.add(Stat.MST);
    stats.add(Stat.CRI);
    result.put(Stat.HIT, stats);

    stats = EnumSet.noneOf(Stat.class);
    stats.add(Stat.HIT);
    stats.add(Stat.EXP);
    stats.add(Stat.MST);
    result.put(Stat.CRI, stats);

    stats = EnumSet.noneOf(Stat.class);
    stats.add(Stat.HIT);
    stats.add(Stat.EXP);
    stats.add(Stat.MST);
    stats.add(Stat.CRI);
    result.put(Stat.HST, stats);

    stats = EnumSet.noneOf(Stat.class);
    stats.add(Stat.HIT);
    stats.add(Stat.MST);
    stats.add(Stat.CRI);
    result.put(Stat.EXP, stats);

    stats = EnumSet.noneOf(Stat.class);
    stats.add(Stat.HIT);
    stats.add(Stat.EXP);
    result.put(Stat.MST, stats);

    return result;
  }

  @Override public float calculateEPDeltaMax(Stat from, int amount) {
    switch (from) {
      case HIT:
        return amount * (EXP_COEFFICIENT_TO_CAP - HIT_COEFFICIENT_CAPPED);      // NOTE: Verify next-best stat
      case CRI:
        return amount * (HIT_COEFFICIENT_TO_CAP - CRI_COEFFICIENT);
      case HST:
        return amount * (HIT_COEFFICIENT_TO_CAP - HST_COEFFICIENT);
      case EXP:
        return amount * (HIT_COEFFICIENT_TO_CAP - EXP_COEFFICIENT_CAPPED);
      case MST:
        return amount * (HIT_COEFFICIENT_TO_CAP - MST_COEFFICIENT);
    }
    return 0.0f;
  }
}
