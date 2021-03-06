package com.code402;

import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.util.regex.*;
import org.apache.commons.io.IOUtils;
import org.netpreserve.jwarc.*;

class JDK {
  public static void main(String[] args) throws Exception {
    String fileName =
        args.length > 0
            ? args[0]
            : "http://commoncrawl.s3.amazonaws.com/crawl-data/CC-MAIN-2019-30/segments/1563195523840.34/warc/CC-MAIN-20190715175205-20190715200159-00000.warc.gz";
    InputStream is = null;
    if (fileName.indexOf("http://") == 0 || fileName.indexOf("https://") == 0) {
      is = new BufferedInputStream(new URL(fileName).openStream());
    } else {
      is = new BufferedInputStream(new FileInputStream(fileName));
    }

    WarcReader reader = new WarcReader(is);
    reader.next(); // skip the warcinfo record

    int entries = 0;
    int matchingEntries = 0;
    int hits = 0;

    ByteArrayOutputStream result = new ByteArrayOutputStream();
    Pattern re =
        Pattern.compile("(youtu\\.be/|youtube\\.com/(watch\\?(.*&)?v=|(embed|v)/))([^?&\"'>]+)");
    for (WarcRecord entry : reader) {
      if (((WarcTargetRecord) entry).target().indexOf(".com/") < 0) continue;

      entries += 1;
      result.reset();
      IOUtils.copy(entry.body().stream(), result);
      String contents = result.toString("UTF-8");

      Matcher m = re.matcher(contents);
      if (m.find()) {
        matchingEntries += 1;
        hits += 1;
      }
      while (m.find()) hits += 1;
    }

    System.out.println(String.format("JDK: %d of %d/%d", hits, matchingEntries, entries));
  }
}
