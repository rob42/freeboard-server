<%--
 Copyright 2004-2009 Alan Burlison
 
 This file is part of MeshCMS.
 
 MeshCMS is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 MeshCMS is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with MeshCMS.  If not, see <http://www.gnu.org/licenses/>.
--%>

<%@ page import="java.io.*" %>
<%@ page import="java.text.*" %>
<%@ page import="java.util.*" %>
<%@ page import="java.util.regex.*" %>
<%@ page import="org.meshcms.core.*" %>
<%@ page import="org.meshcms.util.*" %>
<%@ page import="com.opensymphony.module.sitemesh.*" %>
<%@ page import="com.opensymphony.module.sitemesh.parser.*" %>
<jsp:useBean id="webSite" scope="request" type="org.meshcms.core.WebSite" />

<%--
  Advanced parameters for this module:
  - css = name of a css class, for display.
  - date = none (default) | normal | full - date format to use for output
  - start = start date in "normal" format or offset from today as [+-]NN[dwmy],
    default is today.
  - period = period to display entries for - "+" - all future, "-" - all past,
    "+-" - all, sorted new to old, "-+" = all, sorted old to new,
    [+-]NN[dwmy] = period, default - all past ("-").
  - date-style = css id of date to use for sorting, default = diary-date.
  - words = number of words to display for each item.  Default = 50.
  - items = maximum number of items to display.  -1 = all, default = 5.
--%>

<%!
  // RE for parsing relative dates.
  private Matcher relDateRE =
   Pattern.compile("([+-]?)(\\d+)([dDwWmMyY]?)").matcher("");

   // Files with this value as a date are ignored.
   private static final String IGNORE_FILE = "IGNORE_FILE";

  /*
    * Parse a date.  If it is absolute and matches format, return the date,
    * if it is relative, return it relative to the specified base date.
    * Return null if the date can't be parsed.
    */
  private Date parseDate(String date, DateFormat format, Calendar base) {

    // If the date is relative.
    if (relDateRE.reset(date).matches()) {
      Calendar cal = (Calendar) base.clone();
      int i = Integer.parseInt(relDateRE.group(2));
      if ("-".equals(relDateRE.group(1))) {
        i = -i;
      }
      if ("".equals(relDateRE.group(3)) || "dD".indexOf(relDateRE.group(3)) > -1) {
        cal.add(Calendar.DATE, i);
      } else if ("wW".indexOf(relDateRE.group(3)) > -1) {
        cal.add(Calendar.WEEK_OF_YEAR, i);
      } else if ("mM".indexOf(relDateRE.group(3)) > -1) {
        cal.add(Calendar.MONTH, i);
      } else if ("yY".indexOf(relDateRE.group(3)) > -1) {
        cal.add(Calendar.YEAR, i);
      } else {
        return null;
      }
      return cal.getTime();

    // If the date is absolute.
    } else {
      ParsePosition pos = new ParsePosition(0);
      Date d = format.parse(date, pos);
      if (pos.getIndex() != date.length()) {
        return null;
      }
      return d;
    }
  }

  /*
   * Class for holding diary entry information.
   */
  static private class DiaryEntry {
    private final File file;
    private final Date date;
    private final String title;
    private final String excerpt;

    public DiaryEntry(File file, Date date, String title, String excerpt) {
      this.file = file;
      this.date = date;
      this.title = title;
      this.excerpt = excerpt;
    }

    public File getFile() {
      return file;
    }

    public Date getDate() {
      return date;
    }

    public String getTitle() {
      return title;
    }

    public String getExcerpt() {
      return excerpt;
    }
  }

  /*
   * Class for comparing diary entries by date.
   */
  static private class DiaryEntryComparator implements Comparator {
    private final boolean ascending;

    public DiaryEntryComparator(boolean ascending) {
      this.ascending = ascending;
    }

    public int compare(Object o1, Object o2) {
      try {
        Date d1 = ((DiaryEntry) o1).getDate();
        Date d2 = ((DiaryEntry) o2).getDate();
        return ascending == true ? d1.compareTo(d2) : d2.compareTo(d1);
      } catch (ClassCastException e) {
        return 0;
      }
    }
  }
%>

<%
  // Get the module descriptor, as a parameter or attribute.
  String moduleCode = request.getParameter("modulecode");
  ModuleDescriptor md = null;
  if (moduleCode != null) {
    md = (ModuleDescriptor) request.getAttribute(moduleCode);
  }

  // If md is null, this module has not been called correctly.
  if (md == null) {
    if (!response.isCommitted()) {
      // Report an error if possible.
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }
    return;
  }

  // Get the locale-specific information.
  Locale locale = WebUtils.getPageLocale(pageContext);
  ResourceBundle pageBundle = ResourceBundle.getBundle
   ("org/meshcms/webui/Locales", locale);

  // Get the path information.
  Path argPath = md.getModuleArgumentDirectoryPath(webSite, true);
  Path dirPath = webSite.getDirectory(md.getPagePath());

  // Get the list of files to display.
  File[] files = md.getModuleFiles(webSite, true);
  if (files == null) {
    return;
  }

  // Current date.
  Calendar today = Calendar.getInstance(locale);
  today.set(Calendar.HOUR_OF_DAY, 0);
  today.set(Calendar.MINUTE, 0);
  today.set(Calendar.SECOND, 0);
  today.set(Calendar.MILLISECOND, 0);

  // Output style.
  String css = md.getFullCSSAttribute("css");

  // Start date.
  DateFormat df = md.getDateFormat(locale, "date");
  if (df == null) {
    df = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
  }

  Date start = parseDate(md.getAdvancedParam("start", "0"), df, today);
  if (start == null) {
    return;
  }

  // First and last dates.
  Date first = null;
  Date last = null;
  DiaryEntryComparator diaryCmp = null;
  String period = md.getAdvancedParam("period", "+-");
  if ("+-".equals(period) || "-+".equals(period) ||
   "+".equals(period) || "-".equals(period)) {
     diaryCmp = new DiaryEntryComparator(period.startsWith("+"));
  } else {
    Calendar cal = Calendar.getInstance();
    cal.setTime(start);
    first = parseDate(period, df, cal);
    if (first == null) {
      return;
    }
    if (first.compareTo(start) > 0) {
      last = first;
      first = start;
      diaryCmp = new DiaryEntryComparator(true);
    } else {
      last = start;
      diaryCmp = new DiaryEntryComparator(false);
    }
    period = null;
  }

  // CSS id for date.
  String dateStyle = md.getAdvancedParam("date-style", "diary-date");
  StringBuffer sb = new StringBuffer();
  sb.append("<(?:span|div)\\s+[^>]*?\\bclass\\s*=\\s*[\"'][\\w\\s-]*\\b");
  sb.append(dateStyle);
  sb.append("\\b[\\w\\s-]*[\"'][^>]*?>([^<]+?)</(?:span|div)>");
  Matcher dateRE = Pattern.compile(sb.toString(),
   Pattern.CASE_INSENSITIVE).matcher("");

  // Words to display.
  int words = Utils.parseInt(md.getAdvancedParam("words", null), 50);

  // Maximum items to display.
  int items = Utils.parseInt(md.getAdvancedParam("items", null), 5);

  List diary = new ArrayList();
  HTMLPageParser hpp = new HTMLPageParser();

  // Scan all the files, looking for ones with a date.
  for (int i = 0; i < files.length; i++) {

    // Skip non-pages.
    File f = files[i];
    if (! FileTypes.isPage(f.getName())) {
      continue;
    }
    
    // Skip itself.
    if (dirPath.add(f.getName()).equals(webSite.getSiteMap().getServedPath(md.getPagePath()))) {
      continue;
    }

    // Read in the file, skip if can't be read.
    HTMLPage pg;
    try {
      Reader reader = new InputStreamReader(new FileInputStream(f), Utils.SYSTEM_CHARSET);
      pg = (HTMLPage) hpp.parse(Utils.readAllChars(reader));
      reader.close();
    } catch (IOException e) {
      continue;
    }

    String body = pg.getBody();

    // Parse the date. If a date can't be found, use the 'last modified' date.
    Date date = null;
    if (dateRE.reset(body).find()) {
      String d = dateRE.group(1);
      // Skip files with an "ignore me" date.
      if (d.equalsIgnoreCase(IGNORE_FILE)) {
        continue;
      }
      ParsePosition pos = new ParsePosition(0);
      date = df.parse(d, pos);
      if (pos.getIndex() != d.length()) {
        date = null;
      }
    }
    if (date == null) {
      Calendar cal = Calendar.getInstance(locale);
      cal.setTimeInMillis(f.lastModified());
      cal.set(Calendar.HOUR_OF_DAY, 0);
      cal.set(Calendar.MINUTE, 0);
      cal.set(Calendar.SECOND, 0);
      cal.set(Calendar.MILLISECOND, 0);
      date = cal.getTime();
    }

    // Check the date is in range.
    if (! (
     (period == null &&
      date.compareTo(first) >= 0 && date.compareTo(last) <= 0) ||
     ("+-".equals(period) || "-+".equals(period)) ||
     ("+".equals(period) && date.compareTo(start) >= 0) ||
     ("-".equals(period) && date.compareTo(start) <= 0)
     )) {
      continue;
    }

    // Pull out the required number of words from the file.
    StringTokenizer st =
     new StringTokenizer(Utils.stripHTMLTags(body), "\n\r\t ");
    sb.setLength(0);
    for (int j = 0; j < words && st.hasMoreTokens(); j++) {
      sb.append(st.nextToken());
      sb.append(' ');
    }

    // Save a new DiaryEntry record.
    String title = pg.getTitle();
    title = Utils.isNullOrEmpty(title) ? "&nbsp;" : title;
    diary.add(new DiaryEntry(f, date, title, sb.toString()));
  }

  // Sort the diary entries into date order.
  Object[] sortedDiary = diary.toArray();
  Arrays.sort(sortedDiary, diaryCmp);

  // Output the diary entries.
  String readMore = pageBundle.getString("readMore");
%>
<div<%= css %>><%
  for (int i = 0; i < sortedDiary.length; i++) {
    DiaryEntry d = (DiaryEntry) sortedDiary[i];
    File f = d.getFile();
  %><div class="includeitem">
  <div class="includetitle">
    <%= d.getTitle() %>
  </div>
  <div class="includedate">
    (<%= df.format(d.getDate()) %>)
  </div>
  <div class="includetext">
    <%= d.getExcerpt() %> ... <a href="<%=
     argPath.add(f.getName()).getRelativeTo(dirPath)
    %>"><%= readMore %></a>
  </div>
</div><%
    WebUtils.updateLastModifiedTime(request, f);
    if (items != -1) {
      if (--items == 0) {
        break;
      }
    }
  }
%></div>
