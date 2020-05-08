CURRENT_DIR = $(dir $(abspath $(lastword $(MAKEFILE_LIST))))

.PHONY: all
all: setup
	@./gradlew check assemble

.PHONY: setup
setup:
	@asdf plugin update java || asdf plugin add java
	@asdf install

.PHONY: check
check:
	@./gradlew checkStatic

.PHONY: test
test:
	@./gradlew check

.PHONY: compile
compile:
	@./gradlew compile

.PHONY: package
package:
	@./gradlew assemble

.PHONY: package.image
package.image:
	# No container image to build here.
