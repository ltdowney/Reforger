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

import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.net.URL;
import java.net.URLConnection;

public final class URLRetriever {
  // NOTE: */advanced seems to be on the order of 100,000+ characters.
  private static final int BUFFER_SIZE = 1 << 17;

  public static String fetchContents(URL url) {
    String inputLine;
    StringBuilder data = new StringBuilder(BUFFER_SIZE);

    try {
      URLConnection connection = url.openConnection();
      BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

      while ((inputLine = in.readLine()) != null) {
        data.append(inputLine);
      }

      in.close();
    } catch (Exception e) {
      Debug.fatalError(URLRetriever.class.getSimpleName(), e);
    }

    return data.toString();
  }

  private URLRetriever() { }
}
