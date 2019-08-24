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
public class Query {

        public static void main(String[] args) throws Exception {
                if (args.length <= 2) {
                        printUsage();
                        return;
                }
                
                File outputDir = new File(args[0]);
                if (!outputDir.exists()) {
                        System.out.println("couldn't find "
                                        + outputDir.getAbsolutePath());
                        return;
                }
                if (!outputDir.isDirectory()) {
                        System.out.println(outputDir.getAbsolutePath()
                                + " is not a directory!");
                        return;
                }
                if (!outputDir.canRead()) {
                        System.out.println("Can't read to "
                                + outputDir.getAbsolutePath());
                        return;
                }

                // we should be "ok" now

                FSDirectory dir = FSDirectory.open(outputDir.toPath());
                IndexReader reader = DirectoryReader.open(dir);
                IndexSearcher searcher = new IndexSearcher(reader);

                TermQuery query = new TermQuery(new Term("body", args[1]));
                TopDocs hits = searcher.search(query,10);
                for(ScoreDoc hit: hits.scoreDocs) {
                   Document document = searcher.doc(hit.doc);
                   System.out.println("Hit: ");
                   for(IndexableField iff : document.getFields()) {
                      String content = document.get(iff.name());
                      System.out.println(iff.name()+ " : " + content);
                   }
                }
        }


        private static void printUsage() {
                System.out
                        .println("Usage: java -cp <...> me.lemire.lucene.Query Index term");
        }
}
