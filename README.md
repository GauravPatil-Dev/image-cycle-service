# Image Service Application

A full-stack image management application with a Java Spring Boot backend and a React (Vite) frontend. Users can upload, view, and delete images in a multi-frame UI with real-time updates.

---

## Features
- Upload, view, and delete images
- Multi-frame image viewer (select number of frames)
- Real-time updates using Server-Sent Events (SSE)
- Responsive, minimalist UI
- Optimistic UI updates and robust error handling

---

## Architecture
- **Backend:** Java Spring Boot, exposes REST API `/api/images` and SSE `/api/images/stream`
- **Frontend:** React (Vite), served via NGINX in Docker
- **Communication:** API and SSE requests proxied from frontend to backend via NGINX

---

## Project Structure
```
├── backend
│   ├── src/main/java/com/example/imageservice/ ...
│   ├── Dockerfile
│   └── ...
├── frontend
│   ├── src/
│   ├── Dockerfile
│   ├── nginx.conf
│   └── ...
├── docker-compose.yml
└── README.md
```

---

## Prerequisites
- [Docker](https://www.docker.com/products/docker-desktop)
- [Docker Compose](https://docs.docker.com/compose/)

---

## Quick Start (Production Mode)

### 1. Build and Run with Docker Compose
From the project root:
```sh
docker compose up --build
```
- This will build both backend and frontend images and start the containers.
- The frontend will be available at: [http://localhost:3000](http://localhost:3000)
- The backend API will be available at: [http://localhost:8080/api/images](http://localhost:8080/api/images)

### 2. Stopping the Application
```sh
docker compose down
```

---

## Development Notes
- To make changes to the frontend or backend, edit the code and rerun `docker compose up --build`.
- Images are stored on disk and metadata are stored in-memory.
- CORS is configured for frontend-backend communication.
- NGINX proxies `/api` requests to the backend container.

---

## Troubleshooting
- **CORS errors:** Ensure both frontend and backend are running in Docker, and CORS is enabled for `http://localhost:3000` in the backend.
- **500 errors on frontend:** Ensure `dist/index.html` is generated and NGINX config has the correct `root` directive.
- **Live reload:** Not enabled in production Docker; for dev, run frontend and backend locally.

---

## Credits
- Spring Boot, React, Vite, NGINX, Docker
