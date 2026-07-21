BASE_DIR := $(dir $(realpath $(firstword $(MAKEFILE_LIST))))

setup_asdf = ./bin/setup_asdf.bash
exit_error = (>&2 echo -e ">> \x1B[31m$1\x1B[39m" && exit 1)

ALL_WORKFLOWS := $(filter-out $(wildcard .github/workflows/*.lock.yml) .github/workflows/agentics-maintenance.yml, $(wildcard .github/workflows/*.yml))

.PHONY: setup
setup:
	@$(setup_asdf)

.PHONY: check
check: check.workflows

.PHONY: check.workflows
check.workflows:
	@actionlint -color $(ALL_WORKFLOWS)

.PHONY: test
test:

.PHONY: compile
compile:

.PHONY: package
package:

.PHONY: all
all: setup check test compile package
.DEFAULT_GOAL := all

.PHONY: publish-javascript
publish-javascript:
	cd javascript && yarn workspaces foreach -Atv --include '@equisoft/*' npm publish --access public --tag next
