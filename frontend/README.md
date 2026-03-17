# Cric Scorer Frontend (Vite + React)

## What This UI Covers
- Login (username / phone / email)
- Register (with optional email, optional phone + country dial code)
- First time login or incomplete profile -> Profile completion
- Logout
- Profile draft autosave to `localStorage` and resume on next login

## API Base
By default the app calls `"/newEra/crick-scorer"` (same as your backend).

If you want to change it:
- set `VITE_API_BASE` (example: `http://localhost:2032/newEra/crick-scorer`)

## How To Run (Frontend)
In Windows, it is easiest to run in CMD (not PowerShell) if `npm.ps1` execution policy is blocking.

```bat
cd frontend
npm install
npm run dev
```

Open:
- http://localhost:5173

## How To Run (Backend)
From repo root:

```bat
mvnw.cmd spring-boot:run
```

Backend runs at:
- http://localhost:2032