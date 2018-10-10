# Kronos Technologies Standards

## Supported languages

### PHP

Located in [php/php-coding-standard](php/php-coding-standard).

Supports Squizlab's [PHP CodeSniffer](https://github.com/squizlabs/PHP_CodeSniffer).


### JavaScript (NodeJS)

For use in a NodeJS environment without TypeScript. Located under [node/eslint-config-kt](node/eslint-config-kt).

Applied through [ESLint](https://eslint.org/).


## Building & Publishing

### PHP

Apply your changes, merge in `master` branch, then execute the following commands from the root of the directory:

```bash
# Mirrors php-coding-standard repository in kronostechnologies/php-coding-standard
bin/split.sh

# Push tag to kronostechnologies/php-coding-standard
bin/release.sh v(MAJOR.MINOR.PATCH)
```

To get the latest tag without opening the php-coding-standard repository, checkout the file [.tag](php/php-coding-standard/.tag).

Prefer updating minor versions. 

Afterwards, it is automatically published through packagist.


### JavaScript (NodeJS)

Increment version in the corresponding [package.json](node/eslint-config-kt/package.json).

Automatically updated and published through npm. Make sure you're in `master` branch to see your changes.