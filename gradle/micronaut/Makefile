.PHONY: package
package:
	@../gradlew :micronaut:assemble

.PHONY: clean
clean:
	@../gradlew :micronaut:clean

.PHONY: publish
publish: clean package
	@../gradlew :micronaut:publish
