BASE_DIR := $(dir $(realpath $(firstword $(MAKEFILE_LIST))))

exit_error = (>&2 echo -e ">> \x1B[31m$1\x1B[39m" && exit 1)

.PHONY: setup
setup:
	@command -v asdf &> /dev/null || $(call exit_error,asdf-vm is required)
	@asdf plugin update java || asdf plugin add java
	@asdf plugin update nodejs || (asdf plugin add nodejs && ~/.asdf/plugins/nodejs/bin/import-release-team-keyring)
	@asdf plugin update yarn || asdf plugin add yarn
	@asdf install
	@yarn --cwd "$(BASE_DIR)/javascript" install

.PHONY: check
check:

.PHONY: test
test:

.PHONY: compile
compile:

.PHONY: package
package:

.PHONY: all
all: setup check test compile package
.DEFAULT_GOAL := all
