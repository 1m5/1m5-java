/*
  This is free and unencumbered software released into the public domain.

  Anyone is free to copy, modify, publish, use, compile, sell, or
  distribute this software, either in source code form or as a compiled
  binary, for any purpose, commercial or non-commercial, and by any
  means.

  In jurisdictions that recognize copyright laws, the author or authors
  of this software dedicate any and all copyright interest in the
  software to the public domain. We make this dedication for the benefit
  of the public at large and to the detriment of our heirs and
  successors. We intend this dedication to be an overt act of
  relinquishment in perpetuity of all present and future rights to this
  software under copyright law.

  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
  IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
  OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
  ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
  OTHER DEALINGS IN THE SOFTWARE.

  For more information, please refer to <http://unlicense.org/>
 */
package io.onemfive.desktop.util;

public class Layout {
    public static final double INITIAL_WINDOW_WIDTH = 1200;
    public static final double INITIAL_WINDOW_HEIGHT = 710; //740
    public static final double MIN_WINDOW_WIDTH = 1020;
    public static final double MIN_WINDOW_HEIGHT = 620;
    public static final double FIRST_ROW_DISTANCE = 20d;
    public static final double COMPACT_FIRST_ROW_DISTANCE = 10d;
    public static final double TWICE_FIRST_ROW_DISTANCE = 20d * 2;
    public static final double FLOATING_LABEL_DISTANCE = 20d;
    public static final double GROUP_DISTANCE = 40d;
    public static final double COMPACT_GROUP_DISTANCE = 30d;
    public static final double GROUP_DISTANCE_WITHOUT_SEPARATOR = 20d;
    public static final double FIRST_ROW_AND_GROUP_DISTANCE = GROUP_DISTANCE + FIRST_ROW_DISTANCE;
    public static final double COMPACT_FIRST_ROW_AND_GROUP_DISTANCE = COMPACT_GROUP_DISTANCE + FIRST_ROW_DISTANCE;
    public static final double COMPACT_FIRST_ROW_AND_COMPACT_GROUP_DISTANCE = COMPACT_GROUP_DISTANCE + COMPACT_FIRST_ROW_DISTANCE;
    public static final double COMPACT_FIRST_ROW_AND_GROUP_DISTANCE_WITHOUT_SEPARATOR = GROUP_DISTANCE_WITHOUT_SEPARATOR + COMPACT_FIRST_ROW_DISTANCE;
    public static final double TWICE_FIRST_ROW_AND_GROUP_DISTANCE = GROUP_DISTANCE + TWICE_FIRST_ROW_DISTANCE;
    public static final double PADDING_WINDOW = 20d;
    public static double PADDING = 10d;
    public static double SPACING_H_BOX = 10d;
    public static final double SPACING_V_BOX = 5d;
    public static final double GRID_GAP = 5d;
    public static final double LIST_ROW_HEIGHT = 34;
}
