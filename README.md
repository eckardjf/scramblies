
## Development Mode

### Backend

Launch a repl:

```sh
clj -A:dev
```

Use `go` to initiate and start the server:

```sh
user=> (go)
...
:initiated
```

The server should now be running at <http://localhost/3000>.

When you make changes to the source, use `reset` to reload any
modified files and reset the server.

```sh
user=> (reset)
...
:resumed
```
### Frontend

install dependencies

```sh
npm install
```

run in development mode using shadow-cljs

```sh
npm run dev
```

The application should be available at <http://localhost:8280>.

The browser tests should be available at <http://localhost:8290>.


Changes will be hot-loaded into the browser.