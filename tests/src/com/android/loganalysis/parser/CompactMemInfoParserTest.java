/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.loganalysis.parser;

import com.android.loganalysis.item.CompactMemInfoItem;

import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

public class CompactMemInfoParserTest extends TestCase {

    public void testSingleProcLineWithSwap() {
        List<String> input = Arrays.asList("proc,cached,com.google.android.youtube1,2964,19345,1005,e");

        CompactMemInfoItem item = new CompactMemInfoParser().parse(input);

        assertEquals(1, item.getPids().size());
        assertEquals("com.google.android.youtube1", item.getName(2964));
        assertEquals(19345, item.getPss(2964));
        assertEquals(1005, item.getSwap(2964));
        assertEquals("cached", item.getType(2964));
        assertEquals(false, item.hasActivities(2964));
    }

    public void testSingleProcLineWithoutSwap() {
        List<String> input = Arrays.asList("proc,cached,com.google.android.youtube,2964,19345,e");

        CompactMemInfoItem item = new CompactMemInfoParser().parse(input);

        assertEquals(1, item.getPids().size());
        assertEquals("com.google.android.youtube", item.getName(2964));
        assertEquals(19345, item.getPss(2964));
        assertEquals(0, item.getSwap(2964));
        assertEquals("cached", item.getType(2964));
        assertEquals(false, item.hasActivities(2964));
    }

    public void testSingleLostRamLine() {
        List<String> input = Arrays.asList("lostram,1005");
        CompactMemInfoItem item = new CompactMemInfoParser().parse(input);
        assertEquals(1005, item.getLostRam());
    }

    public void testSomeMalformedLines() {
        List<String> input = Arrays.asList(
                "proc,cached,com.google.android.youtube,a,b,e",
                "proc,cached,com.google.android.youtube,2964,c,e",
                "proc,cached,com.google.android.youtube,2964,e",
                "proc,cached,com.google.android.youtube,2964,19345,a,e",
                "lostram,a,1000",
                "lostram,1000,a");

        CompactMemInfoItem item = new CompactMemInfoParser().parse(input);

        assertEquals(0, item.getPids().size());
    }

    public void testMultipleLines() {
        List<String> input = Arrays.asList(
                "proc,cached,com.google.android.youtube,2964,19345,123,e",
                "proc,cached,com.google.android.apps.plus,2877,9604,N/A,e",
                "proc,cached,com.google.android.apps.magazines,2009,20111,N/A,e",
                "proc,cached,com.google.android.apps.walletnfcrel,10790,11164,100,e",
                "proc,cached,com.google.android.incallui,3410,9491,N/A,e",
                "lostram,1005");

        CompactMemInfoItem item = new CompactMemInfoParser().parse(input);

        assertEquals(5, item.getPids().size());
        assertEquals("com.google.android.youtube", item.getName(2964));
        assertEquals(19345, item.getPss(2964));
        assertEquals(123, item.getSwap(2964));
        assertEquals("cached", item.getType(2964));
        assertEquals(false, item.hasActivities(2964));

        assertEquals(1005, item.getLostRam());
    }

    public void testSkipNonProcLines() {
        // Skip lines which does not start with proc

        List<String> input = Arrays.asList(
                "oom,cached,141357",
                "proc,cached,com.google.android.youtube,2964,19345,54321,e",
                "proc,cached,com.google.android.apps.plus,2877,9604,4321,e",
                "proc,cached,com.google.android.apps.magazines,2009,20111,321,e",
                "proc,cached,com.google.android.apps.walletnfcrel,10790,11164,21,e",
                "proc,cached,com.google.android.incallui,3410,9491,1,e",
                "cat,Native,63169");

        CompactMemInfoItem item = new CompactMemInfoParser().parse(input);

        assertEquals(5, item.getPids().size());
        assertEquals("com.google.android.youtube", item.getName(2964));
        assertEquals(19345, item.getPss(2964));
        assertEquals(54321, item.getSwap(2964));
        assertEquals("cached", item.getType(2964));
        assertEquals(false, item.hasActivities(2964));
    }

    public void testJson() throws JSONException {
        List<String> input = Arrays.asList(
                "oom,cached,141357",
                "proc,cached,com.google.android.youtube,2964,19345,N/A,e",
                "proc,cached,com.google.android.apps.plus,2877,9604,50,e",
                "proc,cached,com.google.android.apps.magazines,2009,20111,100,e",
                "proc,cached,com.google.android.apps.walletnfcrel,10790,11164,0,e",
                "proc,cached,com.google.android.incallui,3410,9491,500,e",
                "lostram,1005",
                "cat,Native,63169");

        CompactMemInfoItem item = new CompactMemInfoParser().parse(input);
        JSONObject json = item.toJson();
        assertNotNull(json);

        JSONArray processes = json.getJSONArray("processes");
        assertEquals(5, processes.length());

        assertEquals(1005, (long)json.get("lostRam"));
    }
}
