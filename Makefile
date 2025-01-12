##@ Run
.PHONY: up-backends
up-backends: build/docker-compose.yml ## Start the backends.
	$(MAKE) down-backends
	docker compose -f build/docker-compose.yml up -d

.PHONY: down-backends
down-backends: ## Stop the backends.
	docker compose -f build/docker-compose.yml down -v

.PHONY: format
format: ## Format the code of jepsen-xa.
	cd jepsen-xa && lein cljfmt fix

##@ Build
build/docker-compose.yml: docker-compose.template jepsen-xa/src/jepsen_xa/docker.clj $(shell find db)
	mkdir -p build
	cd jepsen-xa && lein with-profiles docker-compose run ../docker-compose.template ../build/docker-compose.yml
	docker compose -f build/docker-compose.yml build

##@ Clean
.PHONY: clean
clean: ## Clean the intermediate files.
	rm -rf build

##@ Help
.PHONY: help
help: ## Display this help.
	@awk 'BEGIN {FS = ":.*##"; printf "\nUsage:\n  make \033[36m<target>\033[0m\n"} /^[a-zA-Z_0-9-]+:.*?##/ { printf "  \033[36m%-15s\033[0m %s\n", $$1, $$2 } /^##@/ { printf "\n\033[1m%s\033[0m\n", substr($$0, 5) } ' $(MAKEFILE_LIST)

.DEFAULT_GOAL := help
