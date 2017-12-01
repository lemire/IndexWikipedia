IndexWikipedia
==============

A simple utility to index wikipedia dumps using Lucene.


## Usage:

* install java (JDK) if needed
* install maven if needed
* grap your wikipedia dump: http://en.wikipedia.org/wiki/Wikipedia:Database\_download
* ```mvn compile```
* ```mvn exec:java -Dexec.args="yourdump someoutputdirectory```

Actual example:

```
nohup mvn compile && nohup mvn -e exec:java -Dexec.args="/home/dlemire/WikipediaDump/enwiki-20130102-pages-articles.xml.bz2 /home/dlemire/WikipediaIndex" &
```

By default, Lucene does not store the content of the documents. You can achieve this result by setting the ``doc.store`` property to ``true`` during indexing. It increases considerably the size of the index, however, because you are duplicating the data.


Extracting word-frequency pairs
-------------------------------

There is also a poorly named utility to extract all word-frequency pairs. Invoke it like so (this is an example):

```
java -cp target/classes:target/lib/* me.lemire.lucene.CreateFreqSortedDictionary /home/dlemire/enwiki-20130102-pages-articles.xml.bz2 garbagedict
```

