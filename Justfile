@list:
  just --list

# NOTE: Not needed if you use Calva connection "backend + frontend"
# Start backend repl.
@backend-calva:
  clj -M:dev:test:common:backend:calva-backend:kari -i -C

# Init node packages.
@init:
  mkdir -p target
  mkdir -p classes
  rm -rf node_modules
  npm install

# Start frontend auto-compilation
@frontend:
  npm run dev

# Update dependencies.
@outdated:
  # Backend
  clj -Moutdated --every --write
  # Frontend
  # Install: npm i -g npm-check-updates
  rm -rf node_modules
  ncu -u
  npm install

