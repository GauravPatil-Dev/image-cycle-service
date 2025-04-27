# Take-home Full-Stack Technical Assessment Task

## Task: Image Service

**Objective:**  
Design and implement a RESTful service using **Kotlin** or **Java** to manage image objects. A simple UI should cycle through all images.

---

## Requirements

### Backend
- Implement an HTTP (REST) service in **Java** or **Kotlin** that can perform CRUD operations on:
  - **Images**: Store image to disk.
  - **ImageMetadata**: Associate metadata with a specific image.
- **ImageMetadata** can be any data, like size, name, MIME type.
- You can use an **in-memory storage mechanism** for simplicity.
- Focus on **service-oriented architecture** design and thinking through **edge cases**.

### Frontend
- Must use the APIs created above with any **modern UI framework**.
- Build a **simple UX** that:
  - Cycles through images on the backend.
  - Shows the image name.
- Should use a **notification mechanism from the backend** (i.e., **not** using images GET API or polling).
  - Example: **Websockets** or **Server-Sent Events (SSE)**.
- **New images** added via API while the UI is running should **show up automatically** in the cycle.
- **Order of images** must be strict and **defined on the backend**.
- **Must support multiple users** looking at the UI at the same time.
- **Bonus**: Implement **multi-frame** view — multiple images cycling independently (but with same frequency) at the same time.

---

## Deliverables

1. A **working RESTful service** that meets the above backend requirements.
2. A **working frontend** that meets the above frontend requirements.
3. A **Dockerfile** and **README** to build and run **both frontend and backend**.

---

## Constraints

- The task should take **no longer than a few hours** to complete.
- Use **your preferred frameworks and tools** for the implementation.
- **Forego Authentication and Authorization**.

---

## Follow-up

During the follow-up session, be prepared to discuss:
- The **architecture** you designed.
- **User interactions**.
- **Corner cases** you considered.

