#!/usr/bin/env bash

# This file is read by external tools. Do not move.
# Usage example in external projects:
#  curl -sSL https://raw.githubusercontent.com/kronostechnologies/standards/master/bin/setup_asdf.bash | bash

if ! command -v asdf &> /dev/null; then
  echo asdf-vm is required: https://asdf-vm.com/#/core-manage-asdf?id=install
  exit 1
fi

awk '{print $1;}' .tool-versions | while read -r tool; do
  asdf plugin add "$tool" || asdf plugin update "$tool"

  if [ "$tool" = "nodejs" ]; then
    echo "Installing NodeJS' keyring..."
    ~/.asdf/plugins/nodejs/bin/import-release-team-keyring
  elif [ "$tool" = "java" ]; then
    asdf_java_path=~/.asdf/plugins/java

    if [ -f ~/.config/fish/config.fish ] && ! grep -rq set-java-home.fish ~/.config/fish/config.fish ~/.config/fish/conf.d/; then
      echo source $asdf_java_path/set-java-home.fish >> ~/.config/fish/config.fish
    fi

    if [ -f ~/.bash_profile ] && ! grep -q set-java-home.bash ~/.bash_profile; then
      echo . $asdf_java_path/set-java-home.bash >> ~/.bash_profile
    elif [ -f ~/.bashrc ] && ! grep -q set-java-home ~/.bashrc; then
      echo . $asdf_java_path/set-java-home.bash >> ~/.bashrc
    fi

    if [ -f ~/.zshrc ] && ! grep -q set-java-home.zshrc ~/.zshrc; then
      echo . $asdf_java_path/set-java-home.zsh >> ~/.zshrc
    fi
  fi
done

echo "Installing tool versions..."
asdf install
