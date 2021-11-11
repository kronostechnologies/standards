.PHONY: package
package:
	@../gradlew :openapi-sdk:assemble

.PHONY: clean
clean:
	@../gradlew :openapi-sdk:clean

.PHONY: publish
publish: clean package
	@../gradlew :openapi-sdk:publish
