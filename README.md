# gglclj

Knock-off of https://github.com/mrowa44/ggl. Just wanted to relax writing some nice and simple code.

## Usage

Get one of the scripts from the "Releases" tab. Run it with

```
node gglclj
```

or make it executable (`chmod +x gglclj`), put in your PATH etc.

## Development

Run one of:
* `scripts/build` -- dev version
* `scripts/watch` -- dev version, watches for changes
* `scripts/release` -- production version

Most of the scripts require [rlwrap](http://utopia.knoware.nl/~hlub/uck/rlwrap/) (on OS X installable via brew).

Clean project specific out:

```
lein clean
```

## License

MIT
