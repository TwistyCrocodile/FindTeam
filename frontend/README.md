# FindTeam — frontend (Telegram Mini App)

React + TypeScript + Vite + `@tma.js/sdk-react`.

## Install

```bash
cd frontend
npm install
```

## Run locally (browser)

1. Start the Spring Boot API on port **8080** (default in this repo).
2. From `frontend/`:

```bash
npm run dev
```

3. Open the URL Vite prints (usually `http://localhost:5173`).

Vite proxies `/api` → `http://localhost:8080`, so leave `VITE_API_BASE_URL` empty in `.env` for this setup.

## API base URL

- **Dev with proxy (recommended):** create `.env` with:

  ```env
  VITE_API_BASE_URL=
  ```

  or omit the variable. Requests use relative `/api/...`.

- **Direct to backend (no proxy):** e.g.

  ```env
  VITE_API_BASE_URL=http://localhost:8080
  ```

  Your Spring app must allow **CORS** from the Vite origin, or the browser will block responses.

## Telegram Mini App later

1. Build: `npm run build` — static files in `dist/`.
2. Host `dist/` over **HTTPS** (Telegram requires it for Mini Apps).
3. In [@BotFather](https://t.me/BotFather), set the Mini App URL to that HTTPS origin.
4. Users open the app from Telegram; `init()` + `isTMA()` provide real launch data.

For local Telegram testing, tools like ngrok can expose your Vite dev server (still need to configure the bot URL to the tunnel).

## Project layout

```
src/
  app/           # Shell layout
  components/    # PostCard, CreatePostForm, Telegram strip
  pages/         # Home (feed + form)
  api/           # fetch wrappers
  types/         # DTO shapes
  hooks/         # Telegram environment
```
