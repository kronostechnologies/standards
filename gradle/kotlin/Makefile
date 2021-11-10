.PHONY: package
package:
	@../gradlew :kotlin:assemble

.PHONY: clean
clean:
	@../gradlew :kotlin:clean

.PHONY: publish
publish: clean package
	@../gradlew :kotlin:publish
