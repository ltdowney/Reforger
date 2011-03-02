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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

// import java.util.concurrent.Executors;
// import java.util.concurrent.ExecutorService;
// import java.util.concurrent.TimeUnit;

public final class Reforger implements Runnable {

  private static final int INDEX_DEC = 0;
  private static final int INDEX_INC = 1;
  private static final int INDEX_VAL = 2;

//  private static final int OPTIMAL_THREADS = 4;

  private Float     _optimalEP = 0.0f;
  private int[]     _globalOptions;
  private int[][][] _reforgeMatrix;
  private float[]   _epDeltaMax;

  private Model  _model;
  private String _realm;
  private String _character;

  public Reforger(String realm, String character) {
    _realm     = realm;
    _character = character;
    _model     = new Model();
  }

  @Override public void run() {
    // Get character info
    System.out.println("Downloading character data from Armory.");
    Character character = new Character("us", _realm, _character);
    character.parse();

    StatKVMap mutable    = character.mutableStats(),
              immutable  = character.immutableStats(),
              cumulative = mutable.add(immutable);

    System.out.println("Mutable Secondary Stats (Derived From Base Itemization):");
    System.out.println(mutable + "\n");

    System.out.println("Immutable Secondary Stats (Derived From Bonuses, e.g. Gems, Enchants):");
    System.out.println(immutable + "\n");

    System.out.println("Cumulative Secondary Stats:");
    System.out.println(cumulative + "\n");

    // Determine current / best EP as a baseline.
//        float noReforgingEp = model.calculateEp(noReforgingStats.getData());
//        float currentStatEp = model.calculateEp(currentStats.getData());
//        bestEp = Math.max(currentStatEp, noReforgingEp) - 1;
    _optimalEP = _model.calculateEP(cumulative.data());

    // Max EP Delta per item.
    HashMap<Item, Float> itemEPDeltaMax = new HashMap<Item, Float>(character.items().size());
    // Per item reforge candidate lists.
    ArrayList<ArrayList<Candidate>> options = new ArrayList<ArrayList<Candidate>>(character.items().size());
    // Global candidate list (union over options).
    ArrayList<Candidate> candidates = new ArrayList<Candidate>(character.items().size() * Stat.TYPE_COUNT);

    int maxOptions = 0;
    for (Item item : character.items()) {
      float itemDeltaMax = 0.0f;
      HashSet<StatKVMap>   itemCandidates = item.candidates(_model.candidateMappings());
      ArrayList<Candidate> itemOptions    = new ArrayList<Candidate>(Stat.TYPE_COUNT);

      for (StatKVMap delta : itemCandidates) {
        Candidate option = new Candidate(item, delta);
        itemOptions.add(option);
        candidates.add(option);
        // Calculate the EP delta upper bound for each of this item's possible reforge values.
        for (Stat dec : Stat.values()) {
          int value = delta.value(dec);
          if (value < 0) {
            float itemDelta = _model.calculateEPDeltaMax(dec, Math.abs(value));
            if (itemDelta > itemDeltaMax) {
              itemDeltaMax = itemDelta;
            }
          }
        }
      }

      if (itemOptions.size() > 0) {
        if (itemOptions.size() > maxOptions) {
          maxOptions = itemOptions.size();
        }

        itemEPDeltaMax.put(item, itemDeltaMax);
        options.add(itemOptions);
      }
    }

    // Output candidate count / list.
    System.out.println("Considering " + candidates.size() + " possible reforgings.");
//    for (Candidate o : candidates) {
//      System.out.println("  " + o);
//    }

    // Compute max EP delta for sublists.
    _epDeltaMax = new float[options.size()];

    for (int i = 0; i < options.size(); ++i) {
      float sublistEPDelta = 0.0f;

      for (int j = i + 1; j < options.size(); ++j) {
        Item item = options.get(j).get(0).item();
        sublistEPDelta += itemEPDeltaMax.get(item);
      }

      _epDeltaMax[i] = sublistEPDelta;
    }

    // Create reforging matrix and result array.
    _reforgeMatrix = new int[options.size()][maxOptions][3];
    _globalOptions = new int[options.size()];
    for (int i = 0; i < options.size(); ++i) {
      ArrayList<Candidate> itemOptions = options.get(i);

      for (int j = 0; j < maxOptions; ++j) {
        if (j < itemOptions.size()) {
          Candidate o = itemOptions.get(j);

          _reforgeMatrix[i][j][INDEX_DEC] = o.indexDecreased();
          _reforgeMatrix[i][j][INDEX_INC] = o.indexIncreased();
          _reforgeMatrix[i][j][INDEX_VAL] = o.deltaValue();
        } else {
          _reforgeMatrix[i][j][INDEX_DEC] = -1;
          _reforgeMatrix[i][j][INDEX_INC] = -1;
          _reforgeMatrix[i][j][INDEX_VAL] =  0;
        }
      }
    }

    long startTime, endTime;

    System.out.print("Calculating . . . ");

    startTime = System.currentTimeMillis();
    computeOptimalCandidate(cumulative.data(), 0);
    endTime = System.currentTimeMillis();

    long time = endTime - startTime;

    System.out.println("done.");

    System.out.println("Calculation time: " + time + " ms.");

    // Decode result array.
    ArrayList<Candidate> resultCandidates = new ArrayList<Candidate>(_globalOptions.length);
    for (int i = 0; i < _globalOptions.length; ++i) {
      if (_globalOptions[i] != -1) {
        resultCandidates.add(options.get(i).get(_globalOptions[i]));
      }
    }

    ArrayList<Item> unusedItems = new ArrayList<Item>(character.items());

    System.out.println();
    System.out.println("Optimal reforging:");

    for (Candidate o : resultCandidates) {
      System.out.println(o);
      unusedItems.remove(o.item());
    }

    if (!unusedItems.isEmpty()) {
      System.out.println("Not reforged:");

      for (Item item : unusedItems) {
        System.out.println("  " + item);
      }
    }

    // printStats("Stats with no reforgings (EP = %.1f):", noReforgingStats);
    // printStats("Stats with current reforgings (EP = %.1f):", currentStats);

    // Calculate stats after recommended reforgings.
    StatKVMap resultStats = cumulative;
    for (Candidate o : resultCandidates) {
      resultStats = resultStats.add(o.delta());
    }

    System.out.println();
    System.out.println("Reforged Stats:");
    System.out.println(resultStats);
    System.out.println();

//    double currentEP = _model.calculateEP(cumulative.data());
//    double recommendedEp = _model.calculateEP(recommendStats.data());
//    double improvement = Math.round((recommendedEp - currentEP) * 10) / 10.0;
//    printStats("Stats with recommended reforgings (EP = %.1f):", recommendStats);
//    System.out.println("Improvement over current reforgings: " + improvement);
  }

  private boolean computeOptimalCandidate(int[] stats, int depth) {
    int[][] currentOptions = _reforgeMatrix[depth];
    boolean improved = false;

    // Base case
    if (depth + 1 == _epDeltaMax.length) {
      for (int i = 0; i < currentOptions.length; ++i) {
        int indexDec = currentOptions[i][INDEX_DEC];

        if (indexDec == -1) {
          break;
        }

        int indexInc   = currentOptions[i][INDEX_INC];
        int deltaValue = currentOptions[i][INDEX_VAL];

        stats[indexDec] -= deltaValue;
        stats[indexInc] += deltaValue;

        float resultEP = _model.calculateEP(stats);

        if (resultEP > _optimalEP) {
          _optimalEP = resultEP;
          _globalOptions[depth] = i;
          improved = true;
        }

        stats[indexDec] += deltaValue;
        stats[indexInc] -= deltaValue;
      }

      return improved;
    }

    double currentEPDeltaMax = _epDeltaMax[depth];
    for (int i = 0; i < currentOptions.length; ++i) {
      int indexDec = currentOptions[i][INDEX_DEC];

      if (indexDec == -1) {
        break;
      }

      int indexInc   = currentOptions[i][INDEX_INC];
      int deltaValue = currentOptions[i][INDEX_VAL];

      stats[indexDec] -= deltaValue;
      stats[indexInc] += deltaValue;

      float resultEP = _model.calculateEP(stats);

      if (resultEP + currentEPDeltaMax > _optimalEP) {
        if (computeOptimalCandidate(stats, depth + 1)) {
          _globalOptions[depth] = i;
          improved = true;
        }
      }

      stats[indexDec] += deltaValue;
      stats[indexInc] -= deltaValue;
    }

    float currentEP = _model.calculateEP(stats);

    if (currentEP + currentEPDeltaMax > _optimalEP) {
      if (computeOptimalCandidate(stats, depth + 1)) {
        _globalOptions[depth] = -1;
        improved = true;
      }
    }

    return improved;
  }
}
