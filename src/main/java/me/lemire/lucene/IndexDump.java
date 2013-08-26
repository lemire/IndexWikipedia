package me.lemire.lucene;

import java.io.File;
import java.util.Properties;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.benchmark.byTask.feeds.DocMaker;
import org.apache.lucene.benchmark.byTask.feeds.EnwikiContentSource;
import org.apache.lucene.benchmark.byTask.utils.Config;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.*;

/**
 * A simple utility to index wikipedia dumps using Lucene.
 * 
 * @author Daniel Lemire
 * 
 */
public class IndexDump {

        public static void main(String[] args) throws Exception {
                if (args.length <= 1) {
                        printUsage();
                        return;
                }
                File wikipediafile = new File(args[0]);
                if (!wikipediafile.exists()) {
                        System.out.println("Can't find "
                                + wikipediafile.getAbsolutePath());
                        return;
                }
                if (!wikipediafile.canRead()) {
                        System.out.println("Can't read "
                                + wikipediafile.getAbsolutePath());
                        return;
                }
                File outputDir = new File(args[1]);
                if (!outputDir.exists()) {
                        if (!outputDir.mkdirs()) {
                                System.out.println("couldn't create "
                                        + outputDir.getAbsolutePath());
                                return;
                        }
                }
                if (!outputDir.isDirectory()) {
                        System.out.println(outputDir.getAbsolutePath()
                                + " is not a directory!");
                        return;
                }
                if (!wikipediafile.canWrite()) {
                        System.out.println("Can't write to "
                                + outputDir.getAbsolutePath());
                        return;
                }

                // we should be "ok" now

                FSDirectory dir = FSDirectory.open(outputDir);

                StandardAnalyzer analyzer = new StandardAnalyzer(
                        Version.LUCENE_43);// default
                                           // stop
                                           // words
                IndexWriterConfig config = new IndexWriterConfig(
                        Version.LUCENE_43, analyzer);
                config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);// overwrites
                                                                      // if
                                                                      // needed
                IndexWriter indexWriter = new IndexWriter(dir, config);

                DocMaker docMaker = new DocMaker();
                Properties properties = new Properties();
                properties.setProperty("content.source.forever", "false"); // will
                                                                           // parse
                                                                           // each
                                                                           // document
                                                                           // only
                                                                           // once
                properties.setProperty("docs.file",
                        wikipediafile.getAbsolutePath());
                properties.setProperty("keep.image.only.docs", "false");
                Config c = new Config(properties);
                EnwikiContentSource source = new EnwikiContentSource();
                source.setConfig(c);
                source.resetInputs();// though this does not seem needed, it is
                                     // (gets the file opened?)
                docMaker.setConfig(c, source);
                int count = 0;
                System.out.println("Starting Indexing of Wikipedia dump "
                        + wikipediafile.getAbsolutePath());
                long start = System.currentTimeMillis();
                Document doc;
                try {
                        while ((doc = docMaker.makeDocument()) != null) {

 TokenStream stream = doc.getField("body").tokenStream(analyzer);
CharTermAttribute cattr = stream.addAttribute(CharTermAttribute.class);

OffsetAttribute offsetAttribute = stream.addAttribute(OffsetAttribute.class);

stream.reset();
while (stream.incrementToken()) {
  System.out.print(cattr.toString()+"["+offsetAttribute.startOffset()+":"+offsetAttribute.endOffset()+"]\t");
}
System.out.println();
stream.end();
stream.close();

                                indexWriter.addDocument(doc);
                                ++count;
                                if (count % 1000 == 0)
                                        System.out
                                                .println("Indexed "
                                                        + count
                                                        + " documents in "
                                                        + (System
                                                                .currentTimeMillis() - start)
                                                        + " ms");
                        }
                } catch (org.apache.lucene.benchmark.byTask.feeds.NoMoreDataException nmd) {
                        nmd.printStackTrace();
                }
                long finish = System.currentTimeMillis();
                System.out.println("Indexing " + count + " documents took "
                        + (finish - start) + " ms");
                System.out.println("Total data processed: "
                        + source.getTotalBytesCount() + " bytes");
                System.out.println("Index should be located at "
                        + dir.getDirectory().getAbsolutePath());
                docMaker.close();
                indexWriter.close();
        }

        private static void printUsage() {
                System.out
                        .println("Usage: java -cp <...> me.lemire.lucene.IndexDump somewikipediadump.xml.gz outputdir");
        }
}
