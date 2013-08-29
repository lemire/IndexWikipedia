package me.lemire.lucene;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Properties;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.benchmark.byTask.feeds.DocMaker;
import org.apache.lucene.benchmark.byTask.feeds.EnwikiContentSource;
import org.apache.lucene.benchmark.byTask.utils.Config;
import org.apache.lucene.document.Document;
import org.apache.lucene.util.Version;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.*;

/**
 * A simple utility to convert wikipedia documents to vectors
 * of integers. 
 * 
 * Prior to using this utility, 
 * you may need to generate a dictionary file using me.lemire.lucene.CreateFrequencyDictionary.
 * 
 * 
 * @author Daniel Lemire
 * 
 */
public class NormalizeWikipedia {
        
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
                File dictfile = new File(args[1]);
                if (!dictfile.exists()) {
                        System.out.println("Can't find "
                                + dictfile.getAbsolutePath());
                        return;
                }
                if (!dictfile.canRead()) {
                        System.out.println("Can't read "
                                + dictfile.getAbsolutePath());
                        return;
                }

                // we should be "ok" now

                int MaxN = 1000*1000; //dictionary is limited to 1000000 words
                BufferedReader br = new BufferedReader(new FileReader(dictfile));
                
                System.out.println("#Loading first "+MaxN+" words from dictionary");
                String line;
                HashMap<String,Integer> hm = new HashMap<String,Integer>();
                int code = 0;
                while((line = br.readLine())!= null) {
                        String[] words = line.split("\t");
                        if(words.length!=2) throw new RuntimeException("Format of dictionary should be freq<tab>term"); 
                        hm.put(words[1],code++);
                        if(code > MaxN) break;
                }
                br.close();
                System.out.println("#Loaded "+hm.size()+" words from dictionary.");

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
                System.out.println("#Parsing of Wikipedia dump "
                        + wikipediafile.getAbsolutePath());
                Document doc;
                IntArray ia = new IntArray();
                try {
                        while ((doc = docMaker.makeDocument()) != null) {
                                ia.clear();
                                TokenStream stream = doc.getField("body")
                                        .tokenStream(analyzer);
                                CharTermAttribute cattr = stream
                                        .addAttribute(CharTermAttribute.class);

                                stream.reset();
                                while (stream.incrementToken()) {
                                        String token = cattr.toString();
                                        if (hm.containsKey(token)) {
                                            ia.add(hm.get(token));
                                        }

                                }
                                
                                stream.end();
                                stream.close();
                                if(ia.size() == 0) continue;
                                for(int k = 0; k < ia.size() - 1; ++k ) {
                                        System.out.print(ia.get(k)+",");
                                }
                                System.out.println(ia.get(ia.size() - 1));
                        }
                } catch (org.apache.lucene.benchmark.byTask.feeds.NoMoreDataException nmd) {
                        nmd.printStackTrace();
                }
                docMaker.close();
        }

        private static void printUsage() {
                System.out
                        .println("Usage: java -cp <...> me.lemire.lucene.NormalizeWikipedia somewikipediadump.xml.gz dictfile");
        }
}


