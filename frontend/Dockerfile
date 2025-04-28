# Frontend Dockerfile for React (Vite)
# Build stage
FROM node:18-alpine as build
WORKDIR /app
COPY . .
RUN npm install && npm run build

# Production stage (Node static server)
FROM node:18-alpine
WORKDIR /app
RUN npm install -g serve
COPY --from=build /app/dist /app/dist
EXPOSE 3000
CMD ["serve", "-s", "dist", "-l", "3000"]