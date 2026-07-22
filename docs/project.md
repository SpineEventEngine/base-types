# Project: base-types

## Overview

`base-types` is a JVM library of popular value-object types for the Spine SDK. It
defines Protobuf-based types — email addresses, internet domains, URLs, person names,
and UI values such as colors and languages — together with the Java code that parses,
validates, and stringifies them. It gives Spine-based projects (and the SDK's own
modules) a shared, reusable vocabulary of domain value objects built on top of
`spine-base`.

## Architecture

Role in the organisation: a **library** (module `base-types`, published under the
`io.spine` group).

- **Protobuf-first value types.** The types are declared as Protobuf messages under
  `src/main/proto/spine/{net,people,ui}` and turned into rich domain types by the Spine
  Compiler; the Java code under `src/main/java/io/spine/net` adds parsers, validators,
  and `Stringifier`s for them.
- **Part of the SDK dependency graph.** It depends on `spine-base` and `spine-validation`
  (and, transitively, `spine-time` and `spine-format`), so it is not self-contained: it
  builds against those sibling modules at the versions the shared `config` pins, and is
  normally built in dependency order with the rest of the SDK.
- **Published artifact.** Consumers get the value types plus their conversion utilities.

Read [`.agents/guidelines/jvm-project.md`](.agents/guidelines/jvm-project.md) for the
build stack, coding style, tests, and versioning that govern this repository.
