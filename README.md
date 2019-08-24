IndexWikipedia
==============

A simple utility to index wikipedia dumps using Lucene.

This tool can be used to quickly create an index. It is then expected that a programmer will
write some code to use the index. This project does not aim to build an end-user index.

It is useful as part of research projects.

## Usage:

* install java (JDK) if needed
* install maven if needed
* [grap your wikipedia dump](http://en.wikipedia.org/wiki/Wikipedia:Database_download): you might be grab quickly part of the dump by typing a command like ``wget https://dumps.wikimedia.org/enwiki/latest/enwiki-latest-pages-articles10.xml-p2336425p3046511.bz2``. (Sorry the database dumps are not at a fixed location so we cannot provide a precise URI.) Be mindful that there are many types of Wikipedia dumps and not all of them contain the articles: when in doubt, read the documentation.
* ```mvn compile```
* Create a directory where your index will reside, such as ``WikipediaIndex``. E.g., you might be able to type ``mkdir WikipediaIndex``. Be mindful not to reuse the same directory for different projects or different Lucene versions.
* ```mvn exec:java -Dexec.args="yourdump someoutputdirectory```

Actual example:

```
git clone https://github.com/lemire/IndexWikipedia.git
cd IndexWikipedia
wget https://dumps.wikimedia.org/enwiki/latest/enwiki-latest-pages-articles10.xml-p2336425p3046511.bz2
mkdir Index
mvn compile
mvn exec:java -Dexec.args="enwiki-latest-pages-articles10.xml-p2336425p3046511.bz2 Index"
```
Note that this precise example may fail unless you adjust the URI https://dumps.wikimedia.org/enwiki/latest/enwiki-latest-pages-articles10.xml-p2336425p3046511.bz2 since Wikipedia dumps are not guaranteed to stay at the same URI.


The documents have ``title``, ``name``, ``docid`` and ``body`` fields, all of which are stored with the index.

To see how you might then query the index, see the class file 'Query.java' for a working example.

Extracting word-frequency pairs
-------------------------------

There is also a poorly named utility to extract all word-frequency pairs called ``me.lemire.lucene.CreateFreqSortedDictionary``. Deliberately, it is currently undocumented.

