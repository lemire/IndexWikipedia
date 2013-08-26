package me.lemire.lucene;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.benchmark.byTask.feeds.DocMaker;
import org.apache.lucene.benchmark.byTask.feeds.EnwikiContentSource;
import org.apache.lucene.benchmark.byTask.utils.Config;
import org.apache.lucene.document.Document;
import org.apache.lucene.util.Version;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.*;

/**
 * A simple utility to create an inverse dictionary using Lucene.
 * 
 * @author Daniel Lemire
 * 
 */
public class CreateFreqSortedDictionary {
        public static void main(String[] args) throws Exception {
                final int MinN = 10;

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
                File dictfile = new File(args[0]);
                PrintStream ps = new PrintStream(dictfile);

                // we should be "ok" now

                StandardAnalyzer analyzer = new StandardAnalyzer(
                        Version.LUCENE_43);// default
                                           // stop
                                           // words
                DocMaker docMaker = new DocMaker();
                Properties properties = new Properties();
                properties.setProperty("content.source.forever", "false"); 
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
                HashMap<String, Integer> hm = new HashMap<String, Integer>();
                try {
                        while ((doc = docMaker.makeDocument()) != null) {

                                TokenStream stream = doc.getField("body")
                                        .tokenStream(analyzer);
                                CharTermAttribute cattr = stream
                                        .addAttribute(CharTermAttribute.class);

                                stream.reset();
                                while (stream.incrementToken()) {
                                        String token = cattr.toString();
                                        if (hm.containsKey(token))
                                                hm.put(token, hm.get(token)
                                                        .intValue() + 1);
                                        else
                                                hm.put(token, 1);

                                }
                                stream.end();
                                stream.close();

                                ++count;
                                if (count % 1000 == 0) {
                                        System.out
                                                .println("Parsed "
                                                        + count
                                                        + " documents in "
                                                        + (System
                                                                .currentTimeMillis() - start)
                                                        + " ms");
                                        System.out.println("We have "
                                                + hm.size()
                                                + " terms so far...");

                                        System.out
                                                .println("Trimming terms appearing less than "
                                                        + MinN
                                                        + " times (to preserve memory)");
                                        ArrayList<String> buffer = new ArrayList<String>();
                                        for (Entry<String, Integer> x : hm
                                                .entrySet()) {
                                                if (x.getValue().intValue() < MinN)
                                                        buffer.add(x.getKey());
                                        }
                                        hm.entrySet().removeAll(buffer);

                                }
                        }
                } catch (org.apache.lucene.benchmark.byTask.feeds.NoMoreDataException nmd) {
                        nmd.printStackTrace();
                }
                {
                        System.out
                                .println("Trimming terms appearing less than "
                                        + MinN + " times (to preserve memory)");
                        ArrayList<String> buffer = new ArrayList<String>();
                        for (Entry<String, Integer> x : hm.entrySet()) {
                                if (x.getValue().intValue() < MinN)
                                        buffer.add(x.getKey());
                        }
                        hm.entrySet().removeAll(buffer);
                }
                System.out.println("Dictionary contains "+hm.size()+" terms");
                System.out.println("Sorting them by frequency... ");
                ArrayList<TermFreq> al = new ArrayList<TermFreq>();
                Set<Entry<String, Integer>> x = hm.entrySet();
                Iterator<Entry<String, Integer>> i = x.iterator();
                while(i.hasNext()) {
                        Entry<String, Integer> X = i.next();
                        al.add(new TermFreq(X.getKey(), X.getValue().intValue()));
                        i.remove();
                }
                hm = null;
                Collections.sort(al);
                for (TermFreq tf : al) {
                        ps.println(tf.term);
                }
                ps.close();
                System.out.println("dictionary written to " + dictfile);
                long finish = System.currentTimeMillis();
                System.out.println("Parsing " + count + " documents took "
                        + (finish - start) + " ms");
                System.out.println("Total data processed: "
                        + source.getTotalBytesCount() + " bytes");
                docMaker.close();

        }

        private static void printUsage() {
                System.out
                        .println("Usage: java -cp <...> me.lemire.lucene.CreateFreqSortedDictionary somewikipediadump.xml.gz dictfile");
        }
}

class TermFreq implements Comparable<TermFreq>{
        public int freq;
        public String term;
        public TermFreq(String t, int f) {
                freq = f;
                term = t;
        }
        
        @Override
        public int compareTo(TermFreq a) {
                return a.freq - this.freq;
        }
}
