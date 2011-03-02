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

import java.util.logging.Level;
import java.util.logging.Logger;

import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.PrettyXmlSerializer;
import org.htmlcleaner.TagNode;

public class Debug {

  public static final boolean DEBUG = true;

  public static Debug sharedInstance() {
    return Shared.INSTANCE;
  }

  public static void writeXMLItem(CleanerProperties config, TagNode item) {
    String filename = "./items/" + item.findElementByName("b", true).getText() + ".xml";
    PrettyXmlSerializer s = new PrettyXmlSerializer(config);
    try {
      s.writeToFile(item, filename);
    } catch (Exception e) {
      fatalError(PrettyXmlSerializer.class.getName(), e);
    }
  }

  public static void fatalError(String m, Throwable e) {
    Logger.getLogger((m != null) ? m : "No message available. ").log(Level.SEVERE, null, e);
    System.exit(1);
  }

  private Debug() { }

  private static class Shared {
    private static final Debug INSTANCE = new Debug();

    private Shared() { }
  }
}
