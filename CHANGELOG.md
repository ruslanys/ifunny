# Changelog

All notable changes to this project will be documented in this file.

The format based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [2.3.1] - 2020-02-21
### Fixed
- BastardidentroChannel: Parse a page with meme without header

## [2.3.0] - 2020-02-21
### Added
- Bastardidentro.it channel

## [2.2.0] - 2020-02-21
### Added
- Besti channel

## [2.1.1] - 2020-02-21
### Fixed
- Debeste channel changed its markup for adv boxes and broke pages parsing

## [2.1.0] - 2020-02-19
### Added
- Orschlurch channel

### Changed
- Event channel size extracted into properties variable (`grab.channel-size`)

## [2.0.3] - 2020-01-28
### Changed
- Dockerfile `CMD` replaced with `ENTRYPOINT`
- Dockerfile shell form replaced with exec form

## [2.0.2] - 2020-01-22
### Fixed
- Rigolotes channel changed its markup for rating (votes) and broke pages parsing

## [2.0.1] - 2020-01-22
### Fixed
- Rigolotes channel changed its markup for publish date and broke pages parsing

## [2.0.0] - 2020-01-19
### Changed
- Blocking IO changed with NIO

### Added
- Reactive Approach
- Coroutines

## [1.0.0] - 2020-01-11
### Added
- Feed API Endpoint
- Open API 3 Specification (including Swagger UI)
- Frontend: Feed Page
- Frontend: Meme individual page
- Crawler itself
- Memes deduplication
- S3 Integration
- Yatahonga.com channel
- Rigolotes.fr channel
- Lachschon.de channel
- Funpot.net channel
- Debeste.de channel
- Prometheus metrics
- GitLab Pipeline: CD

### Fixed
- Tag v0.0.1 reference fix

## [0.0.1] - 2019-12-26
### Added
- Application Skeleton
- GitLab CI: Build job
- GitLab CI: Package Docker image job

[unreleased]: https://gitlab.com/ruslanys/ifunny/compare/v2.3.1...master
[2.3.1]: https://gitlab.com/ruslanys/ifunny/compare/v2.3.0...v2.3.1
[2.3.0]: https://gitlab.com/ruslanys/ifunny/compare/v2.2.0...v2.3.0
[2.2.0]: https://gitlab.com/ruslanys/ifunny/compare/v2.1.1...v2.2.0
[2.1.1]: https://gitlab.com/ruslanys/ifunny/compare/v2.1.0...v2.1.1
[2.1.0]: https://gitlab.com/ruslanys/ifunny/compare/v2.0.3...v2.1.0
[2.0.3]: https://gitlab.com/ruslanys/ifunny/compare/v2.0.2...v2.0.3
[2.0.2]: https://gitlab.com/ruslanys/ifunny/compare/v2.0.1...v2.0.2
[2.0.1]: https://gitlab.com/ruslanys/ifunny/compare/v2.0.0...v2.0.1
[2.0.0]: https://gitlab.com/ruslanys/ifunny/compare/v1.0.0...v2.0.0
[1.0.0]: https://gitlab.com/ruslanys/ifunny/compare/v0.0.1...v1.0.0
[0.0.1]: https://gitlab.com/ruslanys/ifunny/-/tags/v0.0.1
