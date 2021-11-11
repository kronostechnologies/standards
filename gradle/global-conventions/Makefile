.PHONY: package
package:
	@../gradlew :global-conventions:assemble

.PHONY: clean
clean:
	@../gradlew :global-conventions:clean

.PHONY: publish
publish: clean package
	@../gradlew :global-conventions:publish
