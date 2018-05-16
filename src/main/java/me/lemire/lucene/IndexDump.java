package me.lemire.lucene;

import java.io.File;
import java.util.Properties;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.benchmark.byTask.feeds.DocMaker;
import org.apache.lucene.benchmark.byTask.utils.Config;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.search.*;


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

                FSDirectory dir = FSDirectory.open(outputDir.toPath());

                StandardAnalyzer analyzer = new StandardAnalyzer();
                IndexWriterConfig config = new IndexWriterConfig(analyzer);
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
                int bodycount = 0;
                System.out.println("Starting Indexing of Wikipedia dump "
                        + wikipediafile.getAbsolutePath());
                long start = System.currentTimeMillis();
                Document doc;
                try {
                        while ((doc = docMaker.makeDocument()) != null) {
                                Document mydoc = new Document();
                                if((doc.getField("docid"))!=null) {
                                  mydoc.add(new TextField("docid",
                                  doc.get("docid"),
                                   Field.Store.YES));
                                  bodycount++;
                                }
                                if((doc.getField("docname"))!=null) {
                                  mydoc.add(new TextField("name",
                                  doc.get("docname"),
                                   Field.Store.YES));
                                  bodycount++;
                                }
                                if(doc.getField("doctitle")!=null) {
                                  mydoc.add(new TextField("title",
                                  doc.get("doctitle"),
                                   Field.Store.YES));
                                  bodycount++;
                                }
                                if(doc.getField("body")!=null) {
                                  if(doc.get("body") != null) {
                                    mydoc.add(new TextField("body",
                                    doc.get("body"),
                                    Field.Store.YES));
                                  bodycount++;
                                  }
                                }
                                indexWriter.addDocument(mydoc);

                                ++count;

                                if(count == 100) break;
                                if (count % 1000 == 0)
                                        System.out
                                                .println("Indexed "
                                                        + count
                                                        + " documents ("+bodycount+" bodies) in "
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
                System.out.println("Index should be located at "
                        + dir.getDirectory().toAbsolutePath());
                indexWriter.close();

                System.out.println("We are going to test the index by querying the word 'other' and getting the top 3 documents:");

                IndexReader reader = DirectoryReader.open(dir);
                IndexSearcher searcher = new IndexSearcher(reader);

                Query query = new TermQuery(new Term("body", "other"));
                TopDocs hits = searcher.search(query, 3);
                for(ScoreDoc hit: hits.scoreDocs) {
                   Document document = searcher.doc(hit.doc);
                   System.out.println("Hit: ");
                   for(IndexableField iff : document.getFields()) {
                      String content = document.get(iff.name());
                      if(content.length() > 40) content = content.substring(0,40)+"...";
                      System.out.println(iff.name()+ " : " + content);
                   }
                }
        }


        private static void printUsage() {
                System.out
                        .println("Usage: java -cp <...> me.lemire.lucene.IndexDump somewikipediadump.xml.gz outputdir");
        }
}
