FILES := benchmark_result.xml dataset.nt dataset_update.nt filesForDocker.tar.gz queryMix.query steadystate.tsv
DIRS := td_data/ target/

.PHONY: clean
clean:
	rm -rf $(FILES) $(DIRS)

test:
	mvn validate
	mvn test

buildimages:
	make clean
	./packFilesForDocker.sh
	mvn clean
	mvn validate
	mvn package -DskipTests=true
	DOCKER=0 BUILD=1 mvn test

dockertest:
	DOCKER=1 BUILD=0 mvn test

