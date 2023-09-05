BASE_DIR := $(dir $(realpath $(firstword $(MAKEFILE_LIST))))

# renovate: datasource=npm depName=renovate
RENOVATE_VERSION := 36.81.0

setup_asdf = ./bin/setup_asdf.bash
exit_error = (>&2 echo -e ">> \x1B[31m$1\x1B[39m" && exit 1)

.PHONY: setup
setup:
	@$(setup_asdf)

.PHONY: check
check: check.workflows check.renovate

.PHONY: check.workflows
check.workflows:
	@actionlint -color

.PHONY: check.renovate
check.renovate:
	@npx --package="renovate@${RENOVATE_VERSION}" -- renovate-config-validator --strict renovate-base.json renovate-config.json

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
	cd javascript && yarn workspaces foreach -tv --include '@equisoft/*' npm publish --access public --tag next
