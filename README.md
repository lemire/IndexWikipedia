IndexWikipedia
==============

A simple utility to index wikipedia dumps using Lucene.


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

By default, Lucene does not store the content of the documents. You can achieve this result by setting the ``doc.store`` property to ``true`` during indexing. It increases considerably the size of the index, however, because you are duplicating the data.


Extracting word-frequency pairs
-------------------------------

There is also a poorly named utility to extract all word-frequency pairs called ``me.lemire.lucene.CreateFreqSortedDictionary``. Deliberately, it is currently undocumented.

