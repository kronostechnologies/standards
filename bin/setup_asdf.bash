#!/usr/bin/env bash

# DEPRECATED: Projects should use `apps project setup` instead of this remotely calling this script.
# This file is read by external tools. Do not move.
# Usage example in external projects:
#  curl -sSL https://raw.githubusercontent.com/kronostechnologies/standards/master/bin/setup_asdf.bash | bash

if ! command -v asdf &> /dev/null; then
  echo asdf-vm is required: https://asdf-vm.com/#/core-manage-asdf?id=install
  exit 1
fi

if grep -q '^nodejs[[:space:]]' .tool-versions; then
  export ASDF_NODEJS_AUTO_ENABLE_COREPACK=1
  default_npm_packages="${ASDF_NPM_DEFAULT_PACKAGES_FILE:-$HOME/.default-npm-packages}"
  touch "$default_npm_packages" || exit 1
  if ! grep -Eq '^[[:space:]]*corepack(@[^[:space:]]+)?([[:space:]]|$)' "$default_npm_packages"; then
    printf '\ncorepack\n' >> "$default_npm_packages" || exit 1
  fi
fi

awk '{print $1;}' .tool-versions | while read -r tool; do
  asdf plugin add "$tool" || asdf plugin update "$tool"

  if [ "$tool" = "java" ]; then
    asdf_java_path=~/.asdf/plugins/java

    if [ -f ~/.config/fish/config.fish ] && ! grep -rq set-java-home.fish ~/.config/fish/config.fish ~/.config/fish/conf.d/; then
      echo source $asdf_java_path/set-java-home.fish >> ~/.config/fish/config.fish
    fi

    if [ -f ~/.bash_profile ] && ! grep -q set-java-home.bash ~/.bash_profile; then
      echo . $asdf_java_path/set-java-home.bash >> ~/.bash_profile
    elif [ -f ~/.bashrc ] && ! grep -q set-java-home ~/.bashrc; then
      echo . $asdf_java_path/set-java-home.bash >> ~/.bashrc
    fi

    if [ -f ~/.zshrc ] && ! grep -q set-java-home.zsh ~/.zshrc; then
      echo . $asdf_java_path/set-java-home.zsh >> ~/.zshrc
    fi
  fi
done

echo "Installing tool versions..."
asdf install || exit 1

if grep -q '^nodejs[[:space:]]' .tool-versions; then
  if ! asdf which corepack &> /dev/null; then
    echo "Installing Corepack for the existing Node.js installation..."
    asdf exec npm install --global corepack || exit 1
  fi
  asdf reshim nodejs || exit 1
  echo "Enabling Corepack..."
  asdf exec corepack enable || exit 1
  asdf reshim nodejs || exit 1
fi
