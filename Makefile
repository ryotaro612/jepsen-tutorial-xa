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

##@ Test
.PHONY: test
test: ## Run the tests.
	cd jepsen-xa && lein test

.PHONY: test-refresh
test-refresh: ## Run the tests on save.
	cd jepsen-xa && lein test-refresh

##@ Build
build/docker-compose.yml: docker-compose.template jepsen-xa/dev/docker.clj jepsen-xa/Dockerfile $(shell find db) $(shell find jepsen-xa/src -regex "[^~#]+")
	mkdir -p build
	cd jepsen-xa && lein with-profiles docker-compose run ../docker-compose.template ../build/docker-compose.yml
	docker compose -f build/docker-compose.yml build

jepsen-xa/target/jepsen-xa-app-standalone.jar: $(shell find jepsen-xa/src -regex "[^#~]+")
	cd jepsen-xa && lein uberjar

##@ Clean
.PHONY: clean
clean: ## Clean the intermediate files.
	rm -rf build
	cd jepsen-xa && lein clean

##@ Help
.PHONY: help
help: ## Display this help.
	@awk 'BEGIN {FS = ":.*##"; printf "\nUsage:\n  make \033[36m<target>\033[0m\n"} /^[a-zA-Z_0-9-]+:.*?##/ { printf "  \033[36m%-15s\033[0m %s\n", $$1, $$2 } /^##@/ { printf "\n\033[1m%s\033[0m\n", substr($$0, 5) } ' $(MAKEFILE_LIST)

.DEFAULT_GOAL := help
