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

import java.net.URL;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;

import org.htmlcleaner.CleanerTransformations;
import org.htmlcleaner.CommentNode;
import org.htmlcleaner.ContentNode;
import org.htmlcleaner.HtmlNode;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.TagNodeVisitor;
import org.htmlcleaner.TagTransformation;

public final class Item implements Comparable<Item>, TagNodeVisitor {

  private static final String TOOLTIP_FORMAT = "tooltip_enus: '(.*)'";
  private static final String TOOLTIP_RATING_NON_RANDOM      = "Equip\\: Increases your ([ \\w]+) rating by (\\d+)";
  private static final String TOOLTIP_RATING_RANDOM_OR_BONUS = "\\+?(\\d+) ([ \\w&&[^\\d]]+) [rR]ating";

  private boolean _parsed = false;

  private int     _slot;
  private String  _name;
  private TagNode _data;

  private final UUID _uniqueID;

  private StatKVMap _mutableStats;
  private StatKVMap _immutableStats;
  private StatKVMap _currentReforging;

  private Item() {
    _uniqueID = UUID.randomUUID();

    _mutableStats     = new StatKVMap();
    _immutableStats   = new StatKVMap();
    _currentReforging = new StatKVMap();
  }

  public Item(int slot, TagNode data) {
    this();

    _slot = slot;
    _data = data;
  }

  @Override public int compareTo(Item o) {
    return Integer.valueOf(_slot).compareTo(o._slot);
  }

  @Override public boolean equals(Object other) {
    if (other instanceof Item) {
      Item o = (Item) other;
      return _uniqueID.equals(o._uniqueID);
    }
    return false;
  }

  @Override public int hashCode() {
    return _uniqueID.hashCode();
  }

  @Override public String toString() {
    parse();

    // StatKVMap stats = _mutableStats.add(_immutableStats);
    // Previously: "[\"" + _name + "\" - Stats: " + stats + "]"
    return _name;
  }

  public synchronized void parse() {
    if (!_parsed) {
      String[] pair;
      String[] elements;

      URL url = null;

      StringBuilder wowhead = new StringBuilder("http://www.wowhead.com/");
      TagNode ref = null;

      //<editor-fold defaultstate="collapsed" desc="Parse name.">
      ref = _data.findElementByAttValue("class", "name-shadow", true, true);
      assert ref != null && ref.getText() != null : "Error: unable to determine item name.";
      _name = (ref != null) ? StringEscapeUtils.unescapeHtml4(ref.getText().toString()) : "";
      //</editor-fold>

      //<editor-fold defaultstate="collapsed" desc="Extract data-item string.">
      ref = _data.findElementByName("a", false);
      assert ref != null : "Error: unable to determine item attributes.";
      String attribute = ref.getAttributeByName("data-item");
      elements = StringEscapeUtils.unescapeHtml4((attribute != null) ? attribute : "").split("&");
      //</editor-fold>

      //<editor-fold defaultstate="collapsed" desc="Parse Armory data-item attributes.">
      for (String e : elements) {
        pair = e.split("=");

        // TODO: For now we assume naïvely that the item ID is always listed first in the data-item attribute.

        if ("i".equals(pair[0])) {
          // Item ID
          wowhead.append("item=").append(pair[1]);
        }

        if ("e".equals(pair[0])) {
          // Permanent Enchantment
          wowhead.append("&ench=").append(pair[1]);
        }

        if ("re".equals(pair[0])) {
          // Reforge ID (not currently supported by Wowhead)
          wowhead.append("&rf=").append(pair[1]);
        }

        if ("es".equals(pair[0])) {
          // Additional Socket
          wowhead.append("&sock");
        }

        if ("r".equals(pair[0])) {
          // Random Itemization
          wowhead.append("&rand=").append(pair[1]);
        }

        if ("set".equals(pair[0])) {
          // Set Pieces Equipped
          wowhead.append("&pcs=").append(pair[1].replace(',', ':'));
        }
      }
      //</editor-fold>

      //<editor-fold defaultstate="collapsed" desc="Parse Armory gem ID's.">
      TagNode[] gems = _data.getElementsByAttValue("class", "gem", true, true);
      final int GEM_COUNT = gems.length;

      if (GEM_COUNT != 0) {
        String suffix;

        wowhead.append("&gems=");

        for (int i = 0; i < GEM_COUNT; ++i) {
          suffix = gems[i].getAttributeByName("href").replace("/wow/en/item/", "");
          wowhead.append(suffix);
          if (i + 1 < GEM_COUNT) {
            wowhead.append(":");
          }
        }
      }
      //</editor-fold>

      wowhead.append("&power");

      System.out.println("  " + _name);

      //<editor-fold defaultstate="collapsed" desc="Download and parse Wowhead JSON data.">
      try {
        url = new URL(wowhead.toString());
      } catch (Exception e) {
        Logger.getLogger(Item.class.getSimpleName()).log(Level.SEVERE, null, e);
      }

      Pattern p = Pattern.compile(TOOLTIP_FORMAT);
      Matcher m = p.matcher(URLRetriever.fetchContents(url));
      String itemPayload = (m.find()) ? m.group(1) : "";

      HtmlCleaner parser = new HtmlCleaner();
      CleanerTransformations transform = new CleanerTransformations();
      TagTransformation strip = new TagTransformation("small");
      transform.addTransformation(strip);
      parser.setTransformations(transform);

      TagNode root = parser.clean(itemPayload);
      //</editor-fold>

      root.traverse(this);

      System.out.println();

      _parsed = true;
    }
  }

  @Override public boolean visit(TagNode parent, HtmlNode current) {
    Pattern p = null;
    Matcher m = null;

    String text  = null;
    Stat   key   = null;
    int    value = -1;

    boolean PARSE_MUTABLE = false;

    int GROUP_INDEX_KEY = -1,
        GROUP_INDEX_VAL = -1;

    if (current instanceof CommentNode) {
      final CommentNode c = (CommentNode) current;
      final String content = c.getCommentedContent();

      if (content.matches("<!--rtg\\d\\d-->")) {
        //<editor-fold defaultstate="collapsed" desc="Secondary Stats for Non-Random Itemization Pieces.">
        //   e.g.: "Equip: Increases your critical strike rating by 168"

        GROUP_INDEX_KEY = 1;
        GROUP_INDEX_VAL = 2;

        PARSE_MUTABLE = true;

        text = parent.getText().toString().replace("&nbsp;", " ");
        p = Pattern.compile(TOOLTIP_RATING_NON_RANDOM);
        //</editor-fold>
      }

      if (content.matches("<!--ee-->")) {
        //<editor-fold defaultstate="collapsed" desc="Permanent enchant effects.">
        //   e.g.: "+190 Attack Power and +55 Critical Strike Rating"

        GROUP_INDEX_KEY = 2;
        GROUP_INDEX_VAL = 1;

        PARSE_MUTABLE = false;

        text = parent.getText().toString();
        p = Pattern.compile(TOOLTIP_RATING_RANDOM_OR_BONUS);
        //</editor-fold>
      }
    }

    if (current instanceof ContentNode) {
      final ContentNode c = (ContentNode) current;
      final String content = c.getContent().toString();

      if ("q1".equals(parent.getAttributeByName("class"))) {
        //<editor-fold defaultstate="collapsed" desc="Secondary Stats for Random Itemization Pieces.">
        //   e.g.: "+168 Critical Strike Rating"

        GROUP_INDEX_KEY = 2;
        GROUP_INDEX_VAL = 1;

        PARSE_MUTABLE = true;

        text = content;
        p = Pattern.compile(TOOLTIP_RATING_RANDOM_OR_BONUS);
        //</editor-fold>
      }

      if (parent.getAttributeByName("class") != null && parent.getAttributeByName("class").startsWith("socket-")) {
        //<editor-fold defaultstate="collapsed" desc="Secondary Stats for gems.">
        //   e.g.: "+20 Agility and +20 Mastery Rating"

        GROUP_INDEX_KEY = 2;
        GROUP_INDEX_VAL = 1;

        PARSE_MUTABLE = false;

        text = content;
        p = Pattern.compile(TOOLTIP_RATING_RANDOM_OR_BONUS);
        //</editor-fold>
      }

      if (content.startsWith("Socket Bonus") && "q2".equals(parent.getAttributeByName("class"))) {
        //<editor-fold defaultstate="collapsed" desc="Secondary Stats for Socket Bonuses.">
        //   e.g.: "Socket Bonus: +30 Haste"

        GROUP_INDEX_KEY = 2;
        GROUP_INDEX_VAL = 1;

        PARSE_MUTABLE = false;

        text = content;
        p = Pattern.compile(TOOLTIP_RATING_RANDOM_OR_BONUS);
        //</editor-fold>
      }
    }

    if (text == null) {
      // Didn't match any known constructs.
      return true;
    }

    m = p.matcher(StringEscapeUtils.unescapeHtml4(text));

    while (m.find()) {
      key   = null;
      value = Integer.parseInt(m.group(GROUP_INDEX_VAL));

      for (Stat s : Stat.values()) {
        if (s.shortName().equalsIgnoreCase(m.group(GROUP_INDEX_KEY))) {
          key = s;
        }
      }

      if (key != null) {
        if (PARSE_MUTABLE) {
          _mutableStats = _mutableStats.add(new StatKVPair(key, value));
          System.out.println("      +" + value + " " + key.shortName());
        } else {
          _immutableStats = _immutableStats.add(new StatKVPair(key, value));
          System.out.println("      +" + value + " " + key.shortName() + " [Immutable]");
        }
      }
    }

    return true;
  }

  public StatKVMap mutableStats() {
    parse();
    return _mutableStats;
  }

  public StatKVMap immutableStats() {
    parse();
    return _immutableStats;
  }

  public StatKVMap currentReforging() {
    parse();
    return _currentReforging;
  }

  public String name() {
    parse();
    return _name;
  }

  public int slot() {
    return _slot;
  }

  public HashSet<StatKVMap> candidates(EnumMap<Stat, EnumSet<Stat>> mappings) {
    parse();

    final HashSet<StatKVMap> result = new HashSet<StatKVMap>(Stat.TYPE_COUNT);

    for (Stat decrease : Stat.values()) {
      for (Stat increase : Stat.values()) {
        if (_mutableStats.value(increase) == 0 && _mutableStats.value(decrease) != 0
                && mappings.containsKey(decrease) && mappings.get(decrease).contains(increase)) {
          int delta = Math.round((float) Math.floor(0.4 * _mutableStats.value(decrease)));
          StatKVMap deltaMap = new StatKVMap(decrease, increase, delta);
          result.add(deltaMap);
        }
      }
    }

    return result;
  }
}
