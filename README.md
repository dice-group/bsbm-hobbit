# Readme

## Introduction
This repository contains the *Berlin SPARQL Benchmark* implementation for the [HOBBIT Platform](https://project-hobbit.eu/).

* [platform description](https://project-hobbit.eu/outcomes/hobbit-platform/)
* [platform components](https://project-hobbit.eu/wp-content/uploads/2017/08/platform_components.png)

It is build on top of the [Java SDK Example](https://github.com/hobbit-project/java-sdk-example/), which uses the [Hobbit Java SDK](https://github.com/hobbit-project/java-sdk/).

## Berlin SPARQL Benchmark (BSBM)
The BSBM was developed to compare the performance of different SPARQL endpoints. The benchmark itself is built around an e-commerce use case.

The original implementation of the BSBM can be found at [wifo5-03.informatik.uni-mannheim.de/bizer/berlinsparqlbenchmark/](http://wifo5-03.informatik.uni-mannheim.de/bizer/berlinsparqlbenchmark/)

* Sourcecode: [sourceforge.net/projects/bsbmtools/](https://sourceforge.net/projects/bsbmtools/)
* direct link: [bsbmtools-v0.2.zip](https://netix.dl.sourceforge.net/project/bsbmtools/bsbmtools/bsbmtools-0.2/bsbmtools-v0.2.zip)

This sourcecode can be found in *src/main/java/benchmark/*.

## Building & Testing
You need Oracle Java 1.8 (or higher), Docker v17 (or higher) and Apache Maven v3 (or higher) installed.

See [Hobbit Java SDK Wiki](https://github.com/hobbit-project/java-sdk/wiki) and [Java SDK Example Readme](https://github.com/hobbit-project/java-sdk-example/blob/master/README.md) for further details.

* `make clean`
* `mvn clean`
* `mvn validate`
* `mvn package -DskipTest=true`
* `mvn test`

For creating the *benchmark.ttl* use the script *helpertools/generateBenchmarkTTL.sh*.

Before creating the docker images, use *pack-stuff.sh* to prepare all necessary files.

## License
* This imlementation of the Benchmark is licensed under [GNU AGPL 3.0](https://www.gnu.org/licenses/agpl-3.0.html)
* The original Berlin SPARQL Benchmark is licensed under [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0), e. g. the following files and folders:
  * *queries/*
  * *src/main/java/benchmark/*
  * *usecases/*
  * *givennames.txt*
  * *titlewords.txt*
* The Hobbit Java SDK is licensed under [GNU GPL 2.0](https://www.gnu.org/licenses/gpl-2.0.html)
