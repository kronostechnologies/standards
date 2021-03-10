# PHP Coding Standard

## Linting with PHP Code Sniffer

1. Install Code Sniffer: `composer require --dev squizlabs/php_codesniffer`
1. Install the Equisoft ruleset: `composer require --dev kronostechnologies/php-code-standard`
1. Add a `phpcs.xml` file at the root of your project:
    ```xml
    <?xml version="1.0"?>
    <ruleset>
        <rule ref="./vendor/kronostechnologies/php-coding-standard/phpcs.xml"/>
    </ruleset>
    ```
1. Add an entry in your Makefile:
    ```makefile
    check: phpcs

    phpcs:
    	./vendor/bin/phpcs --standard="./phpcs.xml" -p -s .
    ```
1. Configure CircleCI with the phpcs Orb
   [Orb](https://github.com/kronostechnologies/circleci-orbs/blob/master/src/jobs/phpcs.yml)

## Building & Publishing

Apply your changes, merge in `master` branch, then execute the following commands from the root:

```bash
# Mirrors php-coding-standard repository in kronostechnologies/php-coding-standard
./bin/split.sh

# Push tag to kronostechnologies/php-coding-standard
./bin/release.sh v(MAJOR.MINOR.PATCH)
```

To get the latest tag without opening the php-coding-standard repository, checkout the file [.tag](./.tag).

Follow [Semantic Versioning](https://semver.org/).

Afterwards, it is automatically published through packagist.
