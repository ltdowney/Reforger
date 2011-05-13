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

import java.io.FileWriter;
import java.net.URL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.PrettyXmlSerializer;
import org.htmlcleaner.SimpleXmlSerializer;
import org.htmlcleaner.TagNode;

public final class Character {

  private static final String ARMORY_URL = "http://%s.battle.net/wow/en/character/%s/%s/advanced";

  private final String _path;

  // TODO: Convert item array into a FIFO structure.
  private final ArrayList<Item> _items;

  private boolean _parsed;

  public Character(String region, String realm, String name) {
    _path = URLUTF8Encoder.encode(String.format(ARMORY_URL, region, realm, name));

    // NOTE: Current maximum number of items possible.  This includes shirts and tabards,
    //       even though those slots are irrelevant as far as we're concerned.
    _items = new ArrayList<Item>(24);

    _parsed = false;
  }

  @SuppressWarnings("unchecked")
  public synchronized void parse() {
    if (_parsed == false) {
      URL         url     = null;
      HtmlCleaner parser  = new HtmlCleaner();
      TagNode     root    = null;

      List<TagNode> nodes = null;

      CleanerProperties config = parser.getProperties();
      config.setAllowHtmlInsideAttributes(true);
      config.setAllowMultiWordAttributes(true);
      config.setRecognizeUnicodeChars(true);

      // Parse items from the Armory.
      try {
        url  = new URL(_path);
        root = parser.clean(url);
      } catch (Exception e) {
        Debug.fatalError(Character.class.getSimpleName(), e);
      }

      nodes = Collections.checkedList(root.getElementListByAttValue("class", "slot-contents", true, false), TagNode.class);

      assert nodes != null && !nodes.isEmpty(): "Error: unable to download item list.";

      for (TagNode inode : nodes) {
        int slot = Integer.parseInt(inode.getParent().getParent().getAttributeByName("data-id"));

        // Check for empty slots.
        // NOTE: We must take care not to discard valid items
        //       with empty sockets.
        TagNode[] children = inode.getChildTags();
        if (children.length == 1 && "a".equals(children[0].getName()) &&
                "javascript:;".equals(children[0].getAttributeByName("href")) &&
                "empty".equals(children[0].getAttributeByName("class")))
          continue;

        // Ignore tabard and shirt slots.
        if (slot == 3 || slot == 18) continue;

        Item i = new Item(slot, inode);
        _items.add(i);

        if (Debug.DEBUG) {
          // Parse items serially in debug mode.
          i.parse();
        } else {
          // TODO: Parse items on thread pool.
        }
      }

      _parsed = true;
    }
  }

  public ArrayList<Item> items() {
    parse();

    return _items;
  }

  public StatKVMap mutableStats() {
    parse();

    StatKVMap r = new StatKVMap();

    for (Item i : _items) {
      r = r.add(i.mutableStats());
    }

    return r;
  }

  public StatKVMap immutableStats() {
    parse();

    StatKVMap r = new StatKVMap();

    for (Item i : _items) {
      r = r.add(i.immutableStats());
    }

    return r;
  }

//  public StatKVMap currentReforging() {
//    return _currentReforging;
//  }
}
