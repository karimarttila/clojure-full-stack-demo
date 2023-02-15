# Javascript-Node (backend) / Typescript-React (frontend) demonstration app

## Introduction

I created a Javascript-Node (backend) / Typescript-React (frontend) demonstration for my personal learning purposes: [js-node-ts-react](https://github.com/karimarttila/js-node-ts-react). I promised to keep a Clojure lecture in my new corporation, and for this lecture, I thought that it might be interesting for the audience to provide them two exact same full-stack applications: one written using the Javascript ecosystem, and one using Clojure. This way my colleagues can, later on, compare these two solutions and make it easier to start learning Clojure when you can compare various backend and frontend solutions to a more familiar Javascript/Typescript example. I have actually created several versions of this webstore example using several languages.


## Prerequisites

- Install [just](https://github.com/casey/just) command line runner.
- See instructions for configuring Calva in [Configuring VSCode/Calva for Clojure programming - Part 3](https://www.karimarttila.fi/clojure/2022/10/18/clojure-calva-part3.html).

## Quick Start Guide for Calva Users

These instructions apply to Calva users. If you are not using Calva, you can still follow the instructions, but you will need to start the backend REPL also.

Both the backend and the frontend use the same port 7171.

Using Calva you just need to start the frontend REPL: `just frontend`.

Then in Calva give command: `Calva: Connect to a running REPL Server in the Project`. Then: `backend + frontend` (see: [Configuring VSCode/Calva for Clojure programming - Part 3](https://www.karimarttila.fi/clojure/2022/10/18/clojure-calva-part3.html)).

Now you have the REPL in Calva, the output window is the same for clj and cljs repls.

Once you have both frontend and backend REPLs running, and you have done the Integrant reset (see in file `user.clj`: `(reset)` ), you can see the frontend in browser at `http://localhost:7171`. The backend is also running at the same port, see examples in the `scripts` directory.

Start Tailwind CSS processing by: `just tailwind`.
