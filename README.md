# Simple web2static files export

Dumb web exporter to static files:

```shell
$ run http://example.org example
```

Please DO be sensible and do not download huge websites. It's for your own sake :p. Might add a limit anyway

Finally you could just simply boot up php built-in server with `docroot` pointint to your chosen static directory (unless not provided - `_tmp`):

```shell
php -S localhost:8080 -t static/example/
```
