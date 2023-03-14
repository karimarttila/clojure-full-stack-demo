@list:
  just --list

# NOTE: The backend repls scripts are not needed if you use Calva connection "backend + frontend"
# Just start the 'just frontend', since Calva connects to the frontend
# and uses the backend repl via shadow-cljs.
@backend-calva:
  clj -M:dev:test:common:backend:calva:kari
@backend-calva-nrepl:
  clj -M:dev:test:common:backend:calva:kari -m nrepl.cmdline

@backend-run-tests:
  clj -M:dev:test:common:backend -m kaocha.runner

# Init node packages.
@init:
  mkdir -p target
  mkdir -p classes
  rm -rf node_modules
  npm install

# Start frontend auto-compilation, and also needed for Calva to connect to the backend.
# Then in Calva give command: `Calva: Connect to a running REPL Server in the Project`. 
# Then choose: `backend + frontend`.
@frontend:
  npm run dev

@tailwind:
  npx tailwindcss -i ./src/css/app.css -o ./dev-resources/public/index.css --watch

@build-uber:
  echo "***** Building frontend *****"
  rm -rf prod-resources
  mkdir -p prod-resources/public/js  
  npx tailwindcss -i ./src/css/app.css -o ./prod-resources/public/index.css
  npx shadow-cljs release app
  echo "***** Building backend *****"
  clj -T:build uber

@run-uber:
  PROFILE=prod java -jar target/karimarttila/webstore-standalone.jar


# Update dependencies.
@outdated:
  # Backend
  clj -Moutdated --every --write
  # Frontend
  # Install: npm i -g npm-check-updates
  rm -rf node_modules
  ncu -u
  npm install

# Clean .cpcache and .shadow-cljs directories, run npm install
@clean:
    rm -rf .cpcache/*
    rm -rf .shadow-cljs/*
    rm -rf target/*
    rm -rf dev-resources/public/js/*
    rm -rf dev-resources/public/css/*
    rm -rf prod-resources/public/js/*
    rm -rf prod-resources/public/css/*
    rm -rf out/*
    npm install
