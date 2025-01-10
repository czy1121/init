#!/usr/bin/env sh

dest=$(tr [:lower:] [:upper:] <<< "${1:0:1}")${1:1}

function publish() {
  echo "./gradlew :$1:publish$dest"
  ./gradlew ":$1:publish$dest"
}

publish init
publish init-annotation
publish init-ksp
